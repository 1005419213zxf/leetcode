package com.litb.bid.component.adw.delay;

import com.litb.bid.component.adw.delay.create.DelayInfoItem;
import com.litb.bid.util.CpTreeFactory;
import com.litb.adw.lib.enums.LitbAdChannel;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

abstract class DelayRateProviderBase implements DelayRateProviderInterface {
	private static final double MAX_DELAY_RATE = 15.0;
	private static final double MIN_1_DAY_CONV_VALUE = 1.0;
	private static final long MIN_30_DAY_CONVERSIONS = 10;
	
	private SiteType siteType;
	private CPTree cpTree;
	
	protected Map<String, double[]> keyConvValueArrMap = new HashMap<String, double[]>();
	protected Map<String, long[]> keyConversionsArrMap = new HashMap<String, long[]>();
	
	// constructor
	public DelayRateProviderBase(SiteType siteType) throws SQLException{
		this.siteType = siteType;
		this.cpTree = CpTreeFactory.getCategoryCpTree(siteType);
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
	
	protected void refindData(){
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
		for(String key : keyToBeDeletedList)
			keyConvValueArrMap.remove(key);
		keyConversionsArrMap.clear();
	}
	
	protected void initMap(BufferedReader br) throws IOException{
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
	
	// for test
	public static void main(String[] args) {
		
	}
	
}
