package com.litb.bid.process;


import com.litb.basic.io.FileHelper;
import com.litb.basic.util.DateHelper;
import com.litb.bid.BingDirDef;
import com.litb.bid.object.bingreport.BingAudienceIntervalReport;
import com.litb.bid.object.bingreport.BingKeywordIntervalReport;
import com.litb.bid.object.bingreport.BingShoppingIntervalReport;
import com.litb.bing.lib.enums.BingChannel;
import com.litb.bing.lib.enums.BingDevice;
import com.litb.bing.lib.enums.BingReportType;
import com.litb.bing.lib.operation.report.BingAudienceAttribute;
import com.litb.bing.lib.operation.report.BingKeywordAttribute;
import com.litb.bing.lib.operation.report.BingMetric;
import com.litb.bing.lib.operation.report.BingShoppingAttribute;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BingGapCpcComputer implements Runnable {
    private BingChannel channel;
    private Date endDate;
    private int interval;

    public BingGapCpcComputer(BingChannel channel, Date endDate, int interval) {
        super();
        this.channel = channel;
        this.endDate = endDate;
        this.interval = interval;
    }

    public static String getOutputFilePath(BingChannel channel, Date endDate) {
        return BingDirDef.getDailyDir(endDate) + "gap_" + channel;
    }


    private Map<String, BidData> getPcCpcAndBid() throws IOException {
        Map<String, BidData> keyAndBidDataMap = new HashMap<String, BidData>();
        BingReportType reportType = null;
        if (channel == BingChannel.bing_pla) {
            reportType = BingReportType.bing_shopping;
        }
        if (channel == BingChannel.bing_search) {
            reportType = BingReportType.bing_keyword_performance;
        }
        if (channel == BingChannel.bing_display) {
            reportType = BingReportType.bing_audience_performance;
        }
        if (reportType == null) {
            return keyAndBidDataMap;
        }


        for (int i = 0; i < interval; i++) {
            Date targetDate = DateHelper.addDays(-i, endDate);
            String allDeviceReportDir = BingDirDef.getFormattedReportDir(targetDate, reportType, 1, null);
            System.out.println("input: " + allDeviceReportDir);
            for (String inputFilePath : FileHelper.getFilePathsInOneDir(allDeviceReportDir)) {
                BufferedReader br = FileHelper.readFile(inputFilePath);
                String line;
                while ((line = br.readLine()) != null) {
                    if (channel == BingChannel.bing_pla) {
                        BingShoppingIntervalReport report = BingShoppingIntervalReport.parseFromLine(line);
                        BingMetric metric = report.getMetric();
                        if (metric.getCpc() > 0) {

                            BingShoppingAttribute attribute = (BingShoppingAttribute) report.getAttribute();
                            String key = attribute.getAccountId() + "\t" + attribute.getCampaignId() + "\t" + attribute.getAdgroupId() + "\t" + attribute.getAdGroupCriterionId();
                            if (keyAndBidDataMap.containsKey(key)) {
                                continue;
                            }
                            BidData bidData = new BidData();
                            bidData.setTargetDate(targetDate);
                            bidData.setClicks(metric.getClicks());
                            bidData.setCost(metric.getSpend());
                            bidData.setMaxCpc(attribute.getCurrentMaxCpc());
                            keyAndBidDataMap.put(key, bidData);
                        }
                    }
                    if (channel == BingChannel.bing_search) {
                        BingKeywordIntervalReport report = BingKeywordIntervalReport.parseFromLine(line);
                        BingMetric metric = report.getMetric();
                        if (metric.getCpc() > 0) {
                            BingKeywordAttribute attribute = (BingKeywordAttribute) report.getAttribute();
                            String key = attribute.getAccountId() + "\t" + attribute.getCampaignId() + "\t" + attribute.getAdgroupId() + "\t" + attribute.getKeywordId();
                            if (keyAndBidDataMap.containsKey(key)) {
                                continue;
                            }
                            BidData bidData = new BidData();
                            bidData.setTargetDate(targetDate);
                            bidData.setClicks(metric.getClicks());
                            bidData.setCost(metric.getSpend());
                            bidData.setMaxCpc(attribute.getCurrentMaxCpc());
                            keyAndBidDataMap.put(key, bidData);
                        }
                    }
                    if (channel == BingChannel.bing_display) {
                        BingAudienceIntervalReport report = BingAudienceIntervalReport.parseFromLine(line);
                        BingMetric metric = report.getMetric();
                        if (metric.getCpc() > 0) {
                            BingAudienceAttribute attribute = (BingAudienceAttribute) report.getAttribute();
                            String key = attribute.getAccountId() + "\t" + attribute.getCampaignId() + "\t" + attribute.getAdgroupId() + "\t" + attribute.getCriterionId();
                            if (keyAndBidDataMap.containsKey(key)) {
                                continue;
                            }
                            BidData bidData = new BidData();
                            bidData.setTargetDate(targetDate);
                            bidData.setClicks(metric.getClicks());
                            bidData.setCost(metric.getSpend());
                            // todo  need BingAudienceAttribute.cpcbid
                            // bidData.setMaxCpc(attribute.get);
                            keyAndBidDataMap.put(key, bidData);
                        }
                    }
                }
                br.close();
            }

            String smartPhoneReportDir = BingDirDef.getFormattedReportDir(targetDate, reportType, 1, BingDevice.SMARTPHONE);
            System.out.println("input: " + smartPhoneReportDir);
            for (String inputFilePath : FileHelper.getFilePathsInOneDir(smartPhoneReportDir)) {
                BufferedReader br = FileHelper.readFile(inputFilePath);
                String line;
                while ((line = br.readLine()) != null) {
                    if (channel == BingChannel.bing_pla) {
                        BingShoppingIntervalReport report = BingShoppingIntervalReport.parseFromLine(line);
                        BingMetric metric = report.getMetric();
                        if (metric.getCpc() > 0) {
                            BingShoppingAttribute attribute = (BingShoppingAttribute) report.getAttribute();
                            String key = attribute.getAccountId() + "\t" + attribute.getCampaignId() + "\t" + attribute.getAdgroupId() + "\t" + attribute.getAdGroupCriterionId();
                            if (!keyAndBidDataMap.containsKey(key)) {
                                continue;
                            }
                            BidData bidData = keyAndBidDataMap.get(key);
                            if (!DateHelper.getShortDateString(targetDate)
                                    .equals(DateHelper.getShortDateString(bidData.getTargetDate()))) {
                                continue;
                            }
                            bidData.setClicks(bidData.getClicks() - metric.getClicks());
                            bidData.setCost(bidData.getCost() - metric.getSpend());
                            if (bidData.getClicks() <= 0)
                                keyAndBidDataMap.remove(key);
                        }
                    }
                    if (channel == BingChannel.bing_search) {
                        BingKeywordIntervalReport report = BingKeywordIntervalReport.parseFromLine(line);
                        BingMetric metric = report.getMetric();
                        if (metric.getCpc() > 0) {
                            BingKeywordAttribute attribute = (BingKeywordAttribute) report.getAttribute();
                            String key = attribute.getAccountId() + "\t" + attribute.getCampaignId() + "\t" + attribute.getAdgroupId() + "\t" + attribute.getKeywordId();
                            if (!keyAndBidDataMap.containsKey(key))
                                continue;
                            BidData bidData = keyAndBidDataMap.get(key);
                            if (!DateHelper.getShortDateString(targetDate).equals(DateHelper.getShortDateString(bidData.getTargetDate())))
                                continue;
                            bidData.setClicks(bidData.getClicks() - metric.getClicks());
                            bidData.setCost(bidData.getCost() - metric.getSpend());
                            if (bidData.getClicks() <= 0)
                                keyAndBidDataMap.remove(key);
                        }
                    }
                    if (channel == BingChannel.bing_display) {
                        BingAudienceIntervalReport report = BingAudienceIntervalReport.parseFromLine(line);
                        BingMetric metric = report.getMetric();
                        if (metric.getCpc() > 0) {
                            BingAudienceAttribute attribute = (BingAudienceAttribute) report.getAttribute();
                            String key = attribute.getAccountId() + "\t" + attribute.getCampaignId() + "\t" + attribute.getAdgroupId() + "\t" + attribute.getCriterionId();
                            if (!keyAndBidDataMap.containsKey(key))
                                continue;
                            BidData bidData = keyAndBidDataMap.get(key);
                            if (!DateHelper.getShortDateString(targetDate).equals(DateHelper.getShortDateString(bidData.getTargetDate())))
                                continue;
                            bidData.setClicks(bidData.getClicks() - metric.getClicks());
                            bidData.setCost(bidData.getCost() - metric.getSpend());
                            if (bidData.getClicks() <= 0)
                                keyAndBidDataMap.remove(key);
                        }
                    }
                }
                br.close();
            }
        }

        return keyAndBidDataMap;
    }


    private static class BidData {
        private Date targetDate;
        private double cost;
        private long clicks;
        private double maxCpc;

        public double getCost() {
            return cost;
        }

        public void setCost(double cost) {
            this.cost = cost;
        }

        public long getClicks() {
            return clicks;
        }

        public void setClicks(long clicks) {
            this.clicks = clicks;
        }

        public double getMaxCpc() {
            return maxCpc;
        }

        public void setMaxCpc(double maxCpc) {
            this.maxCpc = maxCpc;
        }

        public Date getTargetDate() {
            return targetDate;
        }

        public void setTargetDate(Date targetDate) {
            this.targetDate = targetDate;
        }
    }

    @Override
    public void run() {
        try {
            String outputFilePath = getOutputFilePath(channel, endDate);
            System.out.println("output: " + outputFilePath);
            BufferedWriter bw = FileHelper.writeFile(outputFilePath);
            Map<String, BidData> map = getPcCpcAndBid();
            for (Map.Entry<String, BidData> entry : map.entrySet()) {
                BidData bidData = entry.getValue();
                double maxCpc = bidData.getMaxCpc();
                if (bidData.getClicks() <= 0)
                    continue;
                double cpc = bidData.getCost() / bidData.getClicks();
                double gap = maxCpc - cpc;
                if (gap < 0)
                    continue;
                bw.append(entry.getKey() + "\t" + gap + "\t" + DateHelper.getShortDateString(bidData.getTargetDate()) + "\t" +
                        maxCpc + "\t" + cpc + "\t" + bidData.getClicks() + "\n");
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        BingChannel channel = null;
        int interval = 0;
        Date endDate = new Date();
        try {
            channel = BingChannel.valueOf(args[0]);
            interval = Integer.parseInt(args[1]);
            if (args.length > 2)
                endDate = DateHelper.getShortDate(args[2]);
            else {
                endDate = DateHelper.getShortDate(DateHelper.getShortDateString(new Date()));
                endDate = DateHelper.addDays(-1, endDate);
            }
        } catch (Exception e) {
            System.err.println("usage:  <channel> <interval> <end date(option)>");
            System.exit(1);
        }
        System.out.println(channel + "\t" + interval + "\t" + DateHelper.getShortDateString(endDate));
        BingGapCpcComputer computer = new BingGapCpcComputer(channel, endDate, interval);
        computer.run();

        System.out.println("Done.");
    }

}
