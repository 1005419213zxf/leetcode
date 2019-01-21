package com.litb.bid.component.bing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.litb.basic.enums.Country;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.bid.util.JsonMapper;

import java.io.IOException;

public class BingRepeatRateInfo {
    private SiteType siteType;
    private LanguageType languageType;
    private Country adwordsCountry;
    private int cid = -1;

    private double repeatRate = 1;

    // constructor
    public BingRepeatRateInfo(SiteType siteType, LanguageType languageType, Country adwordsCountry, int cid, double repeatRate) {
        super();
        this.siteType = siteType;
        this.languageType = languageType;
        this.adwordsCountry = adwordsCountry;
        this.cid = cid;
        this.repeatRate = repeatRate;
    }

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

    public static BingRepeatRateInfo parse(String line) throws IOException {
        return (BingRepeatRateInfo) JsonMapper.parseJsonString(line, BingRepeatRateInfo.class);
    }

    // Getters and Setters
    public SiteType getSiteType() {
        return siteType;
    }

    public void setSiteType(SiteType siteType) {
        this.siteType = siteType;
    }

    public LanguageType getLanguageType() {
        return languageType;
    }

    public void setLanguageType(LanguageType languageType) {
        this.languageType = languageType;
    }

    public Country getAdwordsCountry() {
        return adwordsCountry;
    }

    public void setAdwordsCountry(Country adwordsCountry) {
        this.adwordsCountry = adwordsCountry;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public double getRepeatRate() {
        return repeatRate;
    }

    public void setRepeatRate(double repeatRate) {
        this.repeatRate = repeatRate;
    }
}
