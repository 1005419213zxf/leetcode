package com.litb.bid.component.bing;


import com.litb.basic.cptree.CPTree;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;
import com.litb.basic.util.DateHelper;
import com.litb.bid.BiddingConf;
import com.litb.bid.BingDirDef;
import com.litb.bid.object.bingreport.BingCampaignPlatformIntervalReport;
import com.litb.bid.object.bing.BingDeviceMetric;
import com.litb.bid.util.CpTreeFactory;
import com.litb.bing.lib.enums.BingChannel;
import com.litb.bing.lib.enums.BingDevice;
import com.litb.bing.lib.enums.BingReportType;
import com.litb.bing.lib.operation.report.BingCampaignPlatformPerformanceAttribute;
import com.litb.bing.lib.operation.report.BingMetric;
import com.litb.bing.lib.util.BingCampaignNameHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;


public class BingMetricProvider implements BingMetricProviderInterface {
    private static final int DEFAULT_ID = -1;
    private static final int MIN_CLICK = 2000;
    private static final int MIN_CONVERSION = 1;
    private static final BingDevice TARGET_DEVICE = null;
    private CPTree litbCpTree;
    private CPTree miniCpTree;
    private Map<String, BingDeviceMetric> campaignKeyMetricMap = new HashMap<String, BingDeviceMetric>();
    private Map<String, BingDeviceMetric> summaryKeyMetricMap = new HashMap<String, BingDeviceMetric>();

    @Override
    public BingDeviceMetricInfo getDeviceMetric(long accountId, long campaignId, SiteType siteType, BingChannel channel, LanguageType languageType, int categoryId, int minAllDeviceConversions, int minPcConverisons, int minMobileConversions) {
        // try to find campaign data
        if (accountId > 0 && campaignId > 0) {
            BingDeviceMetric metric = campaignKeyMetricMap.get(getCampaignKey(accountId, campaignId));
            if (metric != null) {
                boolean isOk = true;
                if (minPcConverisons > 0 && metric.getPcMetric().getConversions() < minPcConverisons)
                    isOk = false;
                else if (minMobileConversions > 0 && metric.getSmartPhone().getConversions() < minMobileConversions)
                    isOk = false;
                else if (minAllDeviceConversions > 0 && metric.getAllDeviceMetric().getConversions() < minAllDeviceConversions)
                    isOk = false;
                if (isOk) {
                    BingDeviceMetricInfo info = new BingDeviceMetricInfo();
                    info.setCampaignData(true);
                    info.setAccountId(accountId);
                    info.setCampaignId(campaignId);
                    info.setDeviceMetric(metric);
                    return info;
                }
            }
        }
        // try to find summary data
        CPTree cpTree = (siteType == SiteType.mini ? miniCpTree : litbCpTree);
        List<Integer> pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(categoryId);
        if (!pcidList.contains(BiddingConf.ROOT_CID))
            pcidList.add(BiddingConf.ROOT_CID);

        for (BingChannel ch : Arrays.asList(channel, null)) {
            for (int pcid : pcidList) {
                for (LanguageType lang : Arrays.asList(languageType, null)) {
                    String key = getSummaryKey(siteType, ch, lang, pcid);
                    BingDeviceMetric metric = summaryKeyMetricMap.get(key);
                    if (metric != null) {
                        boolean isOk = true;
                        if (minPcConverisons > 0 && metric.getPcMetric().getConversions() < minPcConverisons)
                            isOk = false;
                        else if (minMobileConversions > 0 && metric.getSmartPhone().getConversions() < minMobileConversions)
                            isOk = false;
                        else if (minAllDeviceConversions > 0 && metric.getAllDeviceMetric().getConversions() < minAllDeviceConversions)
                            isOk = false;
                        if (isOk) {
                            BingDeviceMetricInfo info = new BingDeviceMetricInfo();
                            info.setCampaignData(false);
                            info.setSiteType(siteType);
                            info.setChannel(ch);
                            info.setLanguageType(lang);
                            info.setCategoryId(pcid);
                            info.setDeviceMetric(metric);
                            return info;
                        }
                    }
                }
            }
        }
        return null;
    }

    public BingMetricProvider(Date endDate, int interval) throws SQLException, IOException {
        System.out.println("initializing " + this.getClass().getSimpleName() + ", end date: " + DateHelper.getShortDateString(endDate) + " " + interval + "...");
        this.litbCpTree = CpTreeFactory.getCategoryCpTree(SiteType.litb);
        this.miniCpTree = CpTreeFactory.getCategoryCpTree(SiteType.mini);

        // initialize
        String inputDir = BingDirDef.getFormattedReportDir(endDate, BingReportType.bing_campaign_platform_performance, interval, TARGET_DEVICE);
        System.out.println("input dir: " + inputDir);
        for (String inputFilePath : FileHelper.getFilePathsInOneDir(inputDir))
            init(inputFilePath);
        System.out.println("after init size(campaign): " + campaignKeyMetricMap.size());
        System.out.println("after init size(summary): " + summaryKeyMetricMap.size());
        // filter
        filter();
        System.out.println("after filter size(campaign): " + campaignKeyMetricMap.size());
        System.out.println("after filter size(summary): " + summaryKeyMetricMap.size());
    }

    public BingDeviceMetricInfo getDeviceMetricExact(SiteType siteType, BingChannel channel, LanguageType languageType, int categoryId) {
        BingDeviceMetric metric = summaryKeyMetricMap.get(getSummaryKey(siteType, channel, languageType, categoryId));
        if (metric == null)
            return null;
        BingDeviceMetricInfo info = new BingDeviceMetricInfo();
        info.setCampaignData(false);
        info.setSiteType(siteType);
        info.setChannel(channel);
        info.setLanguageType(languageType);
        info.setCategoryId(categoryId);

        info.setDeviceMetric(metric);
        return info;
    }


    private static String getCampaignKey(long accountId, long campaignId) {
        return accountId + "\t" + campaignId;
    }

    private static String getSummaryKey(SiteType siteType, BingChannel channel, LanguageType languageType, int cid) {
        return (channel == null ? DEFAULT_ID : channel.getChannelId()) + "\t" +
                siteType.getSiteCode() + "\t" +
                (languageType == null ? DEFAULT_ID : languageType.getLanguageId()) + "\t" + cid;
    }


    // initialize
    private void init(String inputFilePath) throws IOException {
        // for one account
        Map<String, BingDeviceMetric> tmpCampaignKeyMetricMap = new HashMap<String, BingDeviceMetric>();
        BufferedReader br = FileHelper.readFile(inputFilePath);
        String line;
        while ((line = br.readLine()) != null) {
            try {
                BingCampaignPlatformIntervalReport report = BingCampaignPlatformIntervalReport.parseFromLine(line);
                BingCampaignPlatformPerformanceAttribute attribute = (BingCampaignPlatformPerformanceAttribute) report.getAttribute();
                // summary
                SiteType siteType = BingCampaignNameHelper.getSiteType(attribute.getCampaignName(), "");
                BingChannel channel = BingCampaignNameHelper.getBingChannel(attribute.getCampaignName());
                LanguageType languageType = BingCampaignNameHelper.getLanguageTypeFromCampaignName(attribute.getCampaignName(), "");
                int cid = BingCampaignNameHelper.getCategoryIdFromCampaignName(attribute.getCampaignName());
                CPTree cpTree = (siteType == SiteType.mini ? miniCpTree : litbCpTree);
                List<Integer> pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
                if (!pcidList.contains(BiddingConf.ROOT_CID)) {
                    pcidList.add(BiddingConf.ROOT_CID);
                }

                for (BingChannel bingChannel : Arrays.asList(channel, null)) {
                    for (LanguageType lang : Arrays.asList(languageType, null)) {
                        for (int pcid : pcidList) {
                            String key = getSummaryKey(siteType, bingChannel, lang, pcid);
                            BingDeviceMetric metric = summaryKeyMetricMap.get(key);
                            if (metric == null) {
                                metric = new BingDeviceMetric();
                                summaryKeyMetricMap.put(key, metric);
                            }
                            metric.mergeData(BingDevice.valueOf(attribute.getDevice().toUpperCase()), report.getMetric());
                        }
                    }
                }

                // campaign
                String campaignKey = getCampaignKey(attribute.getAccountId(), attribute.getCampaignId());
                BingDeviceMetric campaignMetric = tmpCampaignKeyMetricMap.get(campaignKey);
                if (campaignMetric == null) {
                    campaignMetric = new BingDeviceMetric();
                    tmpCampaignKeyMetricMap.put(campaignKey, campaignMetric);
                }
                campaignMetric.mergeData(BingDevice.valueOf(attribute.getDevice().toUpperCase()), report.getMetric());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        br.close();

        // filter campaign map
        for (Map.Entry<String, BingDeviceMetric> entry : tmpCampaignKeyMetricMap.entrySet()) {
            BingMetric metric = entry.getValue().getAllDeviceMetric();
//            if(metric.getClicks() >= MIN_CLICK && metric.getAllConversions() >= MIN_CONVERSION)
            if (metric.getClicks() >= MIN_CLICK)
                campaignKeyMetricMap.put(entry.getKey(), entry.getValue());
        }
    }


    // filter summary map
    private void filter() {
        List<String> keyToDelList = new ArrayList<String>();
        for (Map.Entry<String, BingDeviceMetric> entry : summaryKeyMetricMap.entrySet()) {
            String key = entry.getKey();

            BingMetric pcMetric = entry.getValue().getPcMetric();
            BingMetric mMetric = entry.getValue().getSmartPhone();
            if (pcMetric.getClicks() < MIN_CLICK || mMetric.getClicks() < MIN_CLICK)
                keyToDelList.add(key);
//			AdwordsMetric metric = entry.getValue().getAllDeviceMetric();
//
//			if(metric.getClicks() < MIN_CLICK || metric.getConversions() < MIN_CONVERSION)
//				keyToDelList.add(key);
        }
        for (String keyToDel : keyToDelList)
            summaryKeyMetricMap.remove(keyToDel);
    }

}
