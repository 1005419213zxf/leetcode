package com.litb.bid.object.bing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.litb.bid.object.LogMetric;
import com.litb.bing.lib.operation.report.BingMetric;

public class BingDeviceStatItem {

    private static final String SEPERATOR = "_@@_";
    private BingStatItem smartPhoneStatItem;
    private BingStatItem allStatItem;

    public BingDeviceStatItem() {
        super();
    }

    public BingDeviceStatItem(int interval) {
        this.smartPhoneStatItem = new BingStatItem(interval);
        this.allStatItem = new BingStatItem(interval);
    }

    public void mergeData(BingDeviceStatItem otherOne) {
        this.allStatItem.getReportMetric().mergeData(otherOne.getAllStatItem().getReportMetric());
        this.allStatItem.getLogMetric().mergeData(otherOne.getAllStatItem().getLogMetric());

        this.smartPhoneStatItem.getReportMetric().mergeData(otherOne.getSmartPhoneStatItem().getReportMetric());
        this.smartPhoneStatItem.getLogMetric().mergeData(otherOne.getSmartPhoneStatItem().getLogMetric());
    }

    @Override
    public String toString() {
        return allStatItem.toString() + SEPERATOR + smartPhoneStatItem.toString();
    }

    public static BingDeviceStatItem parse(String line) {
        try {
            BingDeviceStatItem deviceStatItem = new BingDeviceStatItem();
            String[] stringArr = line.split(SEPERATOR);
            deviceStatItem.allStatItem = BingStatItem.parse(stringArr[0]);
            deviceStatItem.smartPhoneStatItem = BingStatItem.parse(stringArr[1]);
            return deviceStatItem;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(line);
            return null;
        }
    }

    @JsonIgnore
    public int getInterval() {
        return allStatItem.getInterval();
    }

    @JsonIgnore
    public BingStatItem getPcDeviceStatItem() {
        BingStatItem pcStatItem = new BingStatItem(getInterval());
        BingMetric allAdwordsMetric = allStatItem.getReportMetric();
        pcStatItem.getReportMetric().mergeData(allAdwordsMetric);

        BingMetric mAdwordsMetric = smartPhoneStatItem.getReportMetric();
        pcStatItem.getReportMetric().mergeData(mAdwordsMetric, -1);

        BingMetric pcBingMetric = pcStatItem.getReportMetric();
        if (pcBingMetric.getClicks() < 0 || pcBingMetric.getConversions() < 0 || pcBingMetric.getSpend() < 0) {
            pcStatItem.setReportMetric(allAdwordsMetric);
        }

        LogMetric allLogMetric = allStatItem.getLogMetric();
        pcStatItem.getLogMetric().mergeData(allLogMetric);
        return pcStatItem;
    }

    //getter and setter
    public BingStatItem getSmartPhoneStatItem() {
        return smartPhoneStatItem;
    }

    public void setSmartPhoneStatItem(BingStatItem smartPhoneStatItem) {
        this.smartPhoneStatItem = smartPhoneStatItem;
    }

    public BingStatItem getAllStatItem() {
        return allStatItem;
    }

    public void setAllStatItem(BingStatItem allStatItem) {
        this.allStatItem = allStatItem;
    }

    public static void main(String[] args) {
        String line = "1\t\t12\t0\t0.0\t0.0\t0.0\t-1.0\t0.0\t\t0\t0\t0\t0\t0\t0\t-_@@_1\t\t0\t0\t0.0\t0.0\t0.0\t-1.0\t0.0\t\t0\t0\t0\t0\t0\t0\t-";
        BingDeviceStatItem bingDeviceStatItem = parse(line);
        BingMetric reportMetric = bingDeviceStatItem.allStatItem.getReportMetric();
        bingDeviceStatItem.allStatItem.getReportMetric().mergeData(reportMetric);
        System.out.println(bingDeviceStatItem.allStatItem.getReportMetric());
    }
}
