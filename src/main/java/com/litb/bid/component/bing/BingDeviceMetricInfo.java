package com.litb.bid.component.bing;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.bid.component.adw.AdwordsDeviceMetricInfo;
import com.litb.bid.object.bing.BingDeviceMetric;
import com.litb.bid.util.JsonMapper;
import com.litb.bing.lib.enums.BingChannel;

import java.io.IOException;

public class BingDeviceMetricInfo {
    private boolean isCampaignData;

    private long accountId;
    private long campaignId;

    private SiteType siteType;
    private BingChannel channel;
    private LanguageType languageType;
    private int categoryId;

    private BingDeviceMetric deviceMetric;

    // public methods

    @Override
    public String toString() {
        try {
            return JsonMapper.toJsonString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static AdwordsDeviceMetricInfo parse(String line) throws IOException {
        return (AdwordsDeviceMetricInfo) JsonMapper.parseJsonString(line, AdwordsDeviceMetricInfo.class);
    }

    //Getter and Setter


    public boolean isCampaignData() {
        return isCampaignData;
    }

    public void setCampaignData(boolean campaignData) {
        isCampaignData = campaignData;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(long campaignId) {
        this.campaignId = campaignId;
    }

    public SiteType getSiteType() {
        return siteType;
    }

    public void setSiteType(SiteType siteType) {
        this.siteType = siteType;
    }

    public BingChannel getChannel() {
        return channel;
    }

    public void setChannel(BingChannel channel) {
        this.channel = channel;
    }

    public LanguageType getLanguageType() {
        return languageType;
    }

    public void setLanguageType(LanguageType languageType) {
        this.languageType = languageType;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public BingDeviceMetric getDeviceMetric() {
        return deviceMetric;
    }

    public void setDeviceMetric(BingDeviceMetric deviceMetric) {
        this.deviceMetric = deviceMetric;
    }
}
