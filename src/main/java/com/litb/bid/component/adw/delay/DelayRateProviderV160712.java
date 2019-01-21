package com.litb.bid.component.adw.delay;

import com.litb.bid.component.adw.delay.create.DelayInfoItem;
import com.litb.bid.util.CpTreeFactory;
import com.litb.adw.lib.enums.LitbAdChannel;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.dm.aws.s3.AS3FileHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class DelayRateProviderV160712 implements DelayRateProviderInterface {
	private static final double MAX_DELAY_RATE = 10.0;
	private static final double MIN_1_DAY_CONV_VALUE = 10.0;
	private static final long MIN_1_DAY_CONVERSIONS = 20;
	
	private static final String S3DIR = "s3://litb.adwords.test/";
	
	private SiteType siteType;
	private CPTree cpTree;
	
	private Map<String, double[]> keyConvValueArrMap = new HashMap<String, double[]>();
	private Map<String, long[]> keyConversionsArrMap = new HashMap<String, long[]>();
	private static Scanner in;
	
	private int nodeInCpTreeCount = 0;	// used to test number of nodes in cp tree
	private int nodeTotalCount = 0;		// combined with nodeInCpTreeCount for test
	
	// constructor
	public DelayRateProviderV160712(SiteType siteType) throws NumberFormatException, IOException, SQLException  {
		this.siteType = siteType;
		this.cpTree = CpTreeFactory.getCategoryCpTree(siteType);
		
		// find target AS3 input directory
//		Date nowDate = new Date();
//		Date statDate = null;
//		for(Date date=DateHelper.addDays(-31, nowDate); date.after(DateHelper.getShortDate("2015-08-01")); date=DateHelper.addDays(-1, date)) {
//			String inputPath = DelayInfoMerger.getAs3OutputDir(date, BiddingConf.DELAY_RATE_STAT_INTERVAL);
//			if(AS3FileHelper.isFileExist(inputPath)) {
//				statDate = date;
//				break;
//			}
//		}
//		if(statDate == null) {
//			throw new NullPointerException("can not find the result from delay rate merger since 2015-08-01");
//		} else {
//			System.out.println("get the latest delay rate merger result on date " + DateHelper.getShortDateString(statDate));
//		}
		// scan each file and sum
		String s3Dir = "s3://litb.adwords.test/delayRateInfo_history_v2/test/";//DelayInfoMerger.getAs3OutputDir(statDate, BiddingConf.DELAY_RATE_STAT_INTERVAL);
		List<String> inputS3FilePathList = AS3FileHelper.getObjectList(s3Dir);
		for(String inputS3FilePath : inputS3FilePathList){
			BufferedReader br = AS3FileHelper.readFile(S3DIR + inputS3FilePath);
			initMap(br);
			br.close();
		}
		System.out.println("key size(before refine): " + keyConvValueArrMap.size());
		
		// refine data (do something here)
		refindData();
		
		System.out.println("finish, size(after refine): " + keyConvValueArrMap.size());
		
		System.out.println( siteType + String.format(" : total %d line information, %d line data the correspond node not in cptree, " + 
				"%.2f percent line of node in cptree", nodeTotalCount, nodeInCpTreeCount, 
				100*(1.0 - (double)nodeInCpTreeCount / (double)nodeTotalCount)) );
	}
	
	// public methods
	@Override
	public DelayRateInfo getDelayRate(LitbAdChannel channel, SiteType siteType, LanguageType languageType, int categoryId,
			int daysInterval, int getOffsetDays, int predictOffsetDays) {
		
		List<Integer> pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(categoryId);
//		if(pcidList.size() > 0)
//			pcidList.add(-1);
		if (pcidList.contains(-1) == false) {
			pcidList.add(-1);
		}
		
		double[] convValueArray = null;
		for(LitbAdChannel ch : new LitbAdChannel[] { channel, null}){
			for(int pcid : pcidList){
				for(LanguageType lang : new LanguageType[] { languageType, null}){
					String key = getKey(ch, lang, pcid);
					convValueArray = keyConvValueArrMap.get(key);
					if(convValueArray != null){
						// compute delay rate
						double delayRateSum = 0;
						int i = 0;
						for(; i < daysInterval; i++) {
							int segmentGetOffsetDays = getOffsetDays + i;
							if(segmentGetOffsetDays >= 30)
								break;
							int segmentPredictOffsetDays = predictOffsetDays + i;
							if(segmentPredictOffsetDays > 30) 
								segmentPredictOffsetDays = 30;
							
							delayRateSum += convValueArray[segmentPredictOffsetDays-1] / convValueArray[segmentGetOffsetDays-1];
						}
						delayRateSum += (daysInterval - i);
						double avgDelayRate = delayRateSum / daysInterval;
						
						// threshold cut
						if(avgDelayRate < 1)
							avgDelayRate = 1.0;
						else if(avgDelayRate > MAX_DELAY_RATE)
							avgDelayRate = MAX_DELAY_RATE;

						// return result
						DelayRateInfo info = new DelayRateInfo();
						info.setSiteType(siteType);
						info.setChannel(channel);
						info.setLanguageType(lang);
						info.setCid(pcid);
						info.setDelayRate(avgDelayRate);
						
						return info;
					}
				}
			}
		}
		return null;
		
	}

	
	// private methods
	private static String getKey(LitbAdChannel channel, LanguageType languageType, int cid){
		return (channel != null ? channel.getChannelId() : -1) + "\t" + (languageType != null ? languageType.getLanguageId() : -1) + "\t" + cid;
	}
	private void refindData(){
		List<String> keyToBeDeletedList = new ArrayList<String>();
		for(Map.Entry<String, double[]> entry : keyConvValueArrMap.entrySet()){
			String key = entry.getKey();
			double[] convValueArr = entry.getValue();
			if(convValueArr[0] <= MIN_1_DAY_CONV_VALUE){
				keyToBeDeletedList.add(key);
				continue;
			}
			long[] conversionsArr = keyConversionsArrMap.get(key);
			if(conversionsArr != null && conversionsArr[0] < MIN_1_DAY_CONVERSIONS){
				keyToBeDeletedList.add(key);
				continue;
			}
		}
		System.out.println("map size(before refine): " + keyConvValueArrMap.size());
		for(String key : keyToBeDeletedList)
			keyConvValueArrMap.remove(key);
		System.out.println("map size(after refine): " + keyConvValueArrMap.size());
		keyConversionsArrMap.clear();
	}
	
	private void initMap(BufferedReader br) throws NumberFormatException, IOException{
		String line = null;
		while((line = br.readLine()) != null) {
			// for each line
			StringTokenizer st = new StringTokenizer(line, "\t");
			if(st.countTokens() != 6) 
				continue;
			// parse dimensions
			LitbAdChannel channel = LitbAdChannel.valueOf(st.nextToken());
			SiteType siteType = SiteType.valueOf(st.nextToken());;
			LanguageType languageType = LanguageType.valueOf(st.nextToken());
			int categoryId = -1;
			try {
				categoryId = Integer.parseInt(st.nextToken());
			} catch (NullPointerException e) {
				categoryId = -1;
			}
			double[] selfConvValueArr = DelayInfoItem.parseConvValueArrayString(st.nextToken());
			long[] selfConversionsArr = DelayInfoItem.parseConversionsArrayString(st.nextToken());
			
			// filter
			if(siteType != this.siteType) 
				continue;
			
			// sum all
			List<Integer> pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(categoryId);
			nodeTotalCount ++;
			if (pcidList.size() == 0) {
				nodeInCpTreeCount ++;
//				System.err.println("err no category id ( " + categoryId + " ) of site type : " + line);
			}
			if (pcidList.contains(-1) == false) {
				pcidList.add(-1);
			}
			for(LitbAdChannel ch : new LitbAdChannel[] { channel, null}){
				for(LanguageType lang : new LanguageType[] { languageType, null}){
					for(int pcid : pcidList){
						// for each key 
						String key = getKey(ch, lang, pcid);
						// get sum arrays
						double[] convValueArr = keyConvValueArrMap.get(key);
						if(convValueArr == null){
							convValueArr = new double[30];
							Arrays.fill(convValueArr, 0.0);
							keyConvValueArrMap.put(key, convValueArr);
						}
						long[] conversionArr = keyConversionsArrMap.get(key);
						if(conversionArr == null){
							conversionArr = new long[30];
							Arrays.fill(conversionArr, 0);
							keyConversionsArrMap.put(key, conversionArr);
						}
						
						// sum up
						for(int i = 0; i < 30; i++){
							convValueArr[i] += selfConvValueArr[i];
							conversionArr[i] += selfConversionsArr[i];
						}
					}
				}
			}
		}
	}
	
	
	public static void main(String[] args) throws Exception {
//		DBHelper dbHelper = DBPool.getUsMcMainSlaveDbHelper(SiteType.litb);
//		boolean onlyCategory = true;
//		boolean onlyValidCategory = true;
//		boolean considerSoftLink = false;
//		boolean considerSoftCopy = false;
//		ProductStatus productStatus = ProductStatus.onSale;
//		CPTree litbCpTree = new CPTree(dbHelper, onlyCategory, onlyValidCategory, considerSoftLink, considerSoftCopy, productStatus);
//		dbHelper = DBPool.getUsMcMainSlaveDbHelper(SiteType.mini);
//		CPTree miniCpTree = new CPTree(dbHelper, onlyCategory, onlyValidCategory, considerSoftLink, considerSoftCopy, productStatus);
		DelayRateProviderV160712 litbProvider = new DelayRateProviderV160712(SiteType.litb);
		DelayRateProviderV160712 miniProvider = new DelayRateProviderV160712(SiteType.mini);
		in = new Scanner(System.in);
		System.out.println("input : [Site Type] [channel] [language Type] [category Id] [days Interval] [predict days(30/90)]");
		while(in.hasNext()) {
			try {
				String line = in.nextLine();
				if(line.equals("exit") || line.equals("quit"))
					break;
				if(line.equals("test")) {
					System.out.println("test begin...");
					for(String key : litbProvider.keyConvValueArrMap.keySet()) {
						double[] value = litbProvider.keyConvValueArrMap.get(key);
						for(double d : value) System.out.print(" " + d);
						System.err.println("");
					}
					for(String key : miniProvider.keyConvValueArrMap.keySet()) {
						double[] value = miniProvider.keyConvValueArrMap.get(key);
						for(double d : value) System.out.print(" " + d);
						System.err.println("");
					}
					System.out.println("test end.");
				}
				if(line.substring(0, 3).equals("all")) {
					StringTokenizer stringTokenizer = new StringTokenizer(line, " ");
					int leftCount = 1000000;
					if(stringTokenizer.countTokens() == 2) {
						stringTokenizer.nextToken();
						leftCount = Integer.parseInt(stringTokenizer.nextToken());
					}
					System.out.println("all begin...");
					for(DelayRateProviderV160712 provider : new DelayRateProviderV160712[] {litbProvider, miniProvider}) {
						for(LitbAdChannel channel : LitbAdChannel.class.getEnumConstants()) {
							for(LanguageType languageType : LanguageType.class.getEnumConstants()) {
								for(int categoryId : provider.cpTree.getAllCategories()) {
									for(int getOffsetDays=1; getOffsetDays<=30; getOffsetDays++) {
										SiteType siteType = provider.equals(litbProvider) ? SiteType.litb : SiteType.mini;
										DelayRateInfo info = provider.getDelayRate(channel, siteType, languageType, categoryId, 3, getOffsetDays, 30);
										if(info == null) 
											System.out.print(getOffsetDays + ":" + "null" + " ");
										else
											System.out.print(getOffsetDays + ":" + info.getDelayRate() + " ");
										if(getOffsetDays == 30) {
											System.out.println(info.getSiteType() + " " + info.getChannel() + " "
													   + info.getLanguageType() + " " + info.getCid());
										}
									}
									System.out.println("");
									leftCount --;
									if(leftCount <= 0) {
										break;
									}
								}
								if(leftCount <= 0) {
									break;
								}
							}
							if(leftCount <= 0) {
								break;
							}
						}
						if(leftCount <= 0) {
							break;
						}
					}
					System.out.println("all end.");
					continue;
				}
				StringTokenizer st = new StringTokenizer(line, " ");
				if(st.countTokens() != 6) {
					System.err.println("usage : [Site Type] [channel] [language Type] [category Id] [days Interval] [predict days30/90]");
					continue;
				}
				SiteType siteType = null;
				LitbAdChannel channel = null;
				LanguageType languageType = null;
				int categoryId = -1;
				
				int daysInterval = 0;
				int predictDays = 0;
				
				String s = null;
				s = st.nextToken();
				siteType = (s.equals("-1")) ? null : SiteType.valueOf(s);
				s = st.nextToken();
				channel = (s.equals("-1")) ? null : LitbAdChannel.valueOf(s);
				s = st.nextToken();
				languageType = (s.equals("-1")) ? null : LanguageType.valueOf(s);
				s = st.nextToken();
				categoryId = Integer.valueOf(s);
				s = st.nextToken();
				daysInterval = Integer.valueOf(s);
				if(daysInterval < 1) {
					System.err.println("days Interval should >= 1 !!!");
					continue;
				}
				s = st.nextToken();
				predictDays = Integer.valueOf(s);
                if(predictDays < 1) {
                    System.err.println("predictDays should >= 1 !!!");
                    continue;
                }
				
				DelayRateInfo info = null;
				for(int getOffsetDays=1; getOffsetDays<=30; getOffsetDays++) {
					DelayRateProviderV160712 provider = (siteType == SiteType.litb) ? litbProvider : miniProvider;
					info = provider.getDelayRate(channel, siteType, languageType, categoryId, daysInterval, getOffsetDays, predictDays);
					if(info != null) {
						System.out.println(getOffsetDays + ":" + info.getDelayRate());
						if(getOffsetDays == 30) {
							System.out.println(info.getSiteType() + " " + info.getChannel() + " "
											   + info.getLanguageType() + " " + info.getCid());
						}
					} else {
						System.out.print(getOffsetDays + ":" + null + " ");
						if(getOffsetDays == 30)
							System.out.println("");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("usage : [Site Type] [channel] [language Type] [category Id] [days Interval]");
			}
			
		}
	}

}
