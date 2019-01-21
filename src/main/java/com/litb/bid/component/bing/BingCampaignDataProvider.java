package com.litb.bid.component.bing;


import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;
import com.litb.bid.BiddingConf;
import com.litb.bid.BingDirDef;
import com.litb.bid.object.bing.BingAggregation;
import com.litb.bid.object.bing.BingDeviceStatItem;
import com.litb.bing.lib.enums.BingChannel;
import com.litb.bing.lib.operation.report.BingMetric;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BingCampaignDataProvider implements BingCampaignDataProviderInterface {
    private static final int INTERVAL = BiddingConf.MAX_INTERVAL_DAYS;
    private Map<String, BingAggregation> keyMap = new HashMap<String, BingAggregation>();

    public BingCampaignDataProvider(SiteType siteType, BingChannel channel, Date endDate) throws IOException, SQLException {
        String campaignPath = BingDirDef.getLocalBiddableObjectCampaignGroupFilePath(siteType, channel, endDate);
        System.out.println("init CamaignDataProvider...");
        BufferedReader br = FileHelper.readFile(campaignPath);
        String line = null;
        while ((line = br.readLine()) != null) {
            BingAggregation aggregation = BingAggregation.parse(line);
            if (!keyMap.containsKey(aggregation.getKey()))
                keyMap.put(aggregation.getKey(), aggregation);
        }
        br.close();
        System.out.println("init CampaignDataProvider...done. " + keyMap.size());
    }

    @Override
    public BingDeviceStatItem[] getCampaignData(long accountId, long campaignId) {
        BingAggregation aggregation = null;
        aggregation = keyMap.get(accountId + "\t" + campaignId);
        if (aggregation == null)
            return null;
        BingDeviceStatItem deviceStatItem = aggregation.getStatItems()[BiddingConf.getIntervalIndex(INTERVAL)];
        BingMetric metric = deviceStatItem.getAllStatItem().getReportMetric();
        if (metric.getConversions() >= BiddingConf.EXPECT_CONVERSION && metric.getCr() <= BiddingConf.MAX_CR) {
            return aggregation.getStatItems();
        }
        return null;
    }

    public static void main(String[] args) throws IOException, SQLException {
//        long accoutId = -1L;
//        long campaignId = -1L;
//        Date endDate = null;
//        try {
//            accoutId = Long.valueOf(args[0]);
//            campaignId = Long.valueOf(args[1]);
//            endDate = DateHelper.getShortDate(args[2]);
//        } catch (Exception e) {
//            System.out.println("usage: accountid campaignid enddate");
//            System.exit(1);
//        }
//        BingCampaignDataProvider searchCategoryDataProvider = new BingCampaignDataProvider(SiteType.litb, AdwordsChannel.search, endDate);
//        BingDeviceStatItem[] deviceStatItems = searchCategoryDataProvider.getCampaignData(accoutId, campaignId);
//        BingAggregation aggregation = new BingAggregation();
//        aggregation.setStatItems(deviceStatItems);
//        System.out.println(aggregation);
    }

}
