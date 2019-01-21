package com.litb.bid.component.bing;


import com.litb.bid.object.bing.BingBiddableObject;
import com.litb.bid.object.GapData;
import com.litb.bing.lib.enums.BingChannel;

import java.util.HashMap;
import java.util.Map;

public class BingAvgCpcAgainstMaxCpcRatioProvider implements  BingAvgCpcAgainstMaxCpcRatioProviderInterface {

    private static final double MAX_BOOSTED_RATIO = 1.3;
    private static final double MIN_BOOSTED_RATIO = 1.0;
    private static final double MAX_GAP = 1.5;
    private static final double MIN_GAP = 0.0;

    private Map<String, GapData> keyGapMap = new HashMap<String,GapData>();


    public BingAvgCpcAgainstMaxCpcRatioProvider(){



    }


    @Override
    public double getAvgCpcAgaistMaxCpcRatio(BingBiddableObject biddableObject) {
        BingChannel channel = biddableObject.getChannel();
        if(channel == BingChannel.bing_pla || channel == BingChannel.bing_search){
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
    public GapData getMaxCpcAndAvgCpcGap(BingBiddableObject biddableObject, double newBid) {
        long accountId = biddableObject.getAccountId();
        long campaignId = biddableObject.getCampaignId();
        long adgroupId = biddableObject.getAdGroupId();
        long criterionId = biddableObject.getCriterionId();
        String key = accountId + "\t" + campaignId + "\t" + adgroupId + "\t" + criterionId;
        GapData gapData = keyGapMap.get(key);
        if(gapData != null)
            return gapData;

        BingChannel channel = biddableObject.getChannel();
//		if(channel == AdwordsChannel.search)
//			return newBid * 0.3;
        if(channel == BingChannel.bing_pla || channel == BingChannel.bing_search){
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
