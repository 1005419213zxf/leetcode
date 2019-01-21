package com.litb.bid.component.adw.delay;

import com.litb.bid.component.adw.delay.create.DelayInfoItem;
import com.litb.bid.util.CpTreeFactory;
import com.litb.adw.lib.enums.LitbAdChannel;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DelayRateProvider2 implements DelayRateProviderInterface {
	private static final double MAX_DELAY_RATE = 15.0;
	private static final double MIN_1_DAY_CONV_VALUE = 1.0;
	private static final long MIN_30_DAY_CONVERSIONS = 10;
	
//	private static final String S3DIR = "s3://litb.adwords.test/";
	
	private SiteType siteType;
	private CPTree cpTree;
	
	private Map<String, double[]> keyConvValueArrMap = new HashMap<String, double[]>();
	private Map<String, long[]> keyConversionsArrMap = new HashMap<String, long[]>();
	private static Scanner in;
	
	// constructor
	public DelayRateProvider2(SiteType siteType) throws Exception  {
		this.siteType = siteType;
		this.cpTree = CpTreeFactory.getCategoryCpTree(siteType);
		
		// scan local file and sum
		BufferedReader br = new BufferedReader(new FileReader(new File("D:\\book.txt")));
		initMap(br);
		br.close();
		System.out.println("key size(before refine): " + keyConvValueArrMap.size());
		
		// refine data (do something here)
		refindData();
		
		System.out.println("finish, size(after refine): " + keyConvValueArrMap.size());
	}
	
	// public methods
	@Override
	public DelayRateInfo getDelayRate(LitbAdChannel channel, SiteType siteType, LanguageType languageType, int categoryId,
			int daysInterval, int getOffsetDays, int predictOffsetDays) {
		String key = getKey(channel, languageType, categoryId);
		
		double[] convValueArray = null;
		// find directly
		convValueArray = keyConvValueArrMap.get(key);
		// remove language dimension
		if(convValueArray == null){
			languageType = null;
			key = getKey(channel, languageType, categoryId);
			convValueArray = keyConvValueArrMap.get(key);
		}
		// remove category
		if(convValueArray == null){
			List<Integer> pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(categoryId);
			pcidList.add(-1);
			for(int pcid : pcidList){
				categoryId = pcid;
				key = getKey(channel, languageType, categoryId);
				convValueArray = keyConvValueArrMap.get(key);
				if(convValueArray != null)
					break;
			}
		}
		// remove channel
		if(convValueArray == null){
			channel = null;
			key = getKey(channel, languageType, categoryId);
			convValueArray = keyConvValueArrMap.get(key);
		}
		
		if(convValueArray == null)
			return null;
		
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
		info.setLanguageType(languageType);
		info.setCid(categoryId);
		info.setDelayRate(avgDelayRate);
		
		return info;
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
			if(conversionsArr != null && conversionsArr[29] < MIN_30_DAY_CONVERSIONS){
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
	private void initMap(BufferedReader br) throws NumberFormatException, IOException, Exception{
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
			if(pcidList.size() > 0)
				pcidList.add(-1);
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
		System.out.println("start litb ...");
		DelayRateProvider2 litbProvider = new DelayRateProvider2(SiteType.litb);
		System.out.println("start mini ...");
		DelayRateProvider2 miniProvider = new DelayRateProvider2(SiteType.mini);
		in = new Scanner(System.in);
		System.out.println("input : [Site Type] [channel] [language Type] [category Id] [days Interval]");
		while(in.hasNext()) {
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
				for(DelayRateProvider2 provider : new DelayRateProvider2[] {litbProvider, miniProvider}) {
					for(LitbAdChannel channel : LitbAdChannel.class.getEnumConstants()) {
						for(LanguageType languageType : LanguageType.class.getEnumConstants()) {
							for(int categoryId : provider.cpTree.getAllCategories()) {
								for(int getOffsetDays=1; getOffsetDays<=30; getOffsetDays++) {
									SiteType siteType = provider.equals(litbProvider) ? SiteType.litb : SiteType.mini;
									DelayRateInfo info = provider.getDelayRate(channel, siteType, languageType, categoryId, 3, getOffsetDays, 30);
									if(info == null) 
										System.out.print(getOffsetDays + ":" + "null" + " ");
									else
										System.out.println(info.getDelayRate());
									//										System.out.print(getOffsetDays + ":" + info.getDelayRate() + " ");
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
			if(st.countTokens() != 5) {
				System.err.println("usage : [Site Type] [channel] [language Type] [category Id] [days Interval]");
				continue;
			}
			SiteType siteType = null;
			LitbAdChannel channel = null;
			LanguageType languageType = null;
			int categoryId = -1;
			int daysInterval = 0;
			
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
			
			DelayRateInfo info = null;
			for(int getOffsetDays=1; getOffsetDays<=30; getOffsetDays++) {
				DelayRateProvider2 provider = (siteType == SiteType.litb) ? litbProvider : miniProvider;
				info = provider.getDelayRate(channel, siteType, languageType, categoryId, 3, getOffsetDays, 30);
				if(info != null) {
//					System.out.println(getOffsetDays + ":" + info.getDelayRate());
					System.out.println(info.getDelayRate());
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
		}
	}

}
