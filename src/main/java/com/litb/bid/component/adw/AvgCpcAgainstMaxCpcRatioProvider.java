package com.litb.bid.component.adw;


import com.litb.bid.object.GapData;
import com.litb.bid.object.adw.BiddableObject;
import com.litb.adw.lib.enums.AdwordsChannel;

import java.util.HashMap;
import java.util.Map;

public class AvgCpcAgainstMaxCpcRatioProvider implements
		AvgCpcAgainstMaxCpcRatioProviderInterface {
	
	private static final double MAX_BOOSTED_RATIO = 1.3;
	private static final double MIN_BOOSTED_RATIO = 1.0;
	private static final double MAX_GAP = 1.5;
	private static final double MIN_GAP = 0.0;
	
	private Map<String, GapData> keyGapMap = new HashMap<String, GapData>();

	// constructor
	public AvgCpcAgainstMaxCpcRatioProvider(){
//		try {
//			String inputFilePath = GapCpcComputer.getOutputFilePath(channel, endDate);
//			System.out.println("input: " + inputFilePath);
//			
//			BufferedReader br = FileHelper.readFile(inputFilePath);
//			String line;
//			while((line = br.readLine()) != null){
//				String[] strArr = line.split("\t");
//				int index = 0;
//				long accountId = Long.parseLong(strArr[index++]);
//				long campaignId = Long.parseLong(strArr[index++]);
//				long adgroupId = Long.parseLong(strArr[index++]);
//				long criterionId = Long.parseLong(strArr[index++]);
//				double gap = Double.parseDouble(strArr[index++]);
//				index++;
//				double bid = Double.parseDouble(strArr[index++]);
//				
//				String key = accountId + "\t" + campaignId + "\t" + adgroupId + "\t" + criterionId;
//				GapData gapData = new GapData(gap,gap/bid);
//				keyGapMap.put(key, gapData);
//			}
//			br.close();
//			System.out.println("finish, size: " + keyGapMap.size());
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
	}
	
	@Override
	public double getAvgCpcAgaistMaxCpcRatio(BiddableObject biddableObject) {
		AdwordsChannel channel = biddableObject.getChannel();
//		if(channel == AdwordsChannel.search)
//			return 1.3;
		if(channel == AdwordsChannel.pla || channel == AdwordsChannel.search){
			double maxCpc = biddableObject.getMaxCpc();
			double avgCpc = biddableObject.getFirstIntervalStatItem().getPcDeviceStatItem().getReportMetric().getCpc();
			if(maxCpc > 0.001 && avgCpc > 0.001){
				double boosted = maxCpc / avgCpc;
				if(boosted > MAX_BOOSTED_RATIO)
					boosted = MAX_BOOSTED_RATIO;
				if(boosted < MIN_BOOSTED_RATIO)
					boosted = MIN_BOOSTED_RATIO;
				return boosted;
			}
			else {
				return 1.05;
			}
		}
		return 1.2;
	}

	@Override
	public GapData getMaxCpcAndAvgCpcGap(BiddableObject biddableObject, double newBid) {// try to get from history
		long accountId = biddableObject.getAccountId();
		long campaignId = biddableObject.getCampaignId();
		long adgroupId = biddableObject.getAdGroupId();
		long criterionId = biddableObject.getCriterionId();
		String key = accountId + "\t" + campaignId + "\t" + adgroupId + "\t" + criterionId;
		GapData gapData = keyGapMap.get(key);
		if(gapData != null)
			return gapData;
		
		
		AdwordsChannel channel = biddableObject.getChannel();
//		if(channel == AdwordsChannel.search)
//			return newBid * 0.3;
		if(channel == AdwordsChannel.pla || channel == AdwordsChannel.search){
			double maxCpc = biddableObject.getMaxCpc();
			double avgCpc = biddableObject.getFirstIntervalStatItem().getPcDeviceStatItem().getReportMetric().getCpc();
			if(maxCpc > 0.001 && avgCpc > 0.001){
				double gap = maxCpc - avgCpc;
				if(gap > MAX_GAP)
					gap = MAX_GAP;
				if(gap < MIN_GAP)
					gap = MIN_GAP;
				
				return new GapData(gap, gap/maxCpc);
			}
			else {
				return new GapData(newBid * 0.05, newBid * 0.05/maxCpc);
			}
		}
		return new GapData(newBid * 0.2, 0.2);
	}
}
