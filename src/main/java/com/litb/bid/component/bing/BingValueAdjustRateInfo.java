package com.litb.bid.component.bing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.bid.util.JsonMapper;

import java.io.IOException;

public class BingValueAdjustRateInfo {

    private SiteType siteType;
    private LanguageType langType;
    private int cid = -1;

    private double valueAdjRatio = 1;
    private double offlineAdjRatio = 1;
    private double totalAdjRatio = 1;

    // public methods
    @Override
    public String toString() {
        try {
            return JsonMapper.toJsonString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return  null;
        }
    }

    public static BingValueAdjustRateInfo parse(String line) throws IOException {
        return (BingValueAdjustRateInfo)JsonMapper.parseJsonString(line, BingValueAdjustRateInfo.class);
    }

    // Getters and Setters
    public SiteType getSiteType() {
        return siteType;
    }

    public void setSiteType(SiteType siteType) {
        this.siteType = siteType;
    }

    public LanguageType getLangType() {
        return langType;
    }

    public void setLangType(LanguageType languageType) {
        this.langType = languageType;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public double getValueAdjRatio() {
        return valueAdjRatio;
    }

    public void setValueAdjRatio(double valueAdjRatio) {
        this.valueAdjRatio = valueAdjRatio;
    }

    public double getOfflineAdjRatio() {
        return offlineAdjRatio;
    }

    public void setOfflineAdjRatio(double offlineAdjRatio) {
        this.offlineAdjRatio = offlineAdjRatio;
    }

    public double getTotalAdjRatio() {
        return totalAdjRatio;
    }

    public void setTotalAdjRatio(double totalAdjRatio) {
        this.totalAdjRatio = totalAdjRatio;
    }
        //
    // main for test
    public static void main(String[] args) throws IOException {
        BingValueAdjustRateInfo info = new BingValueAdjustRateInfo();
        String line = info.toString();
        System.out.println(line);

        info = BingValueAdjustRateInfo.parse(line);
        System.out.println(info.toString());
    }
}
