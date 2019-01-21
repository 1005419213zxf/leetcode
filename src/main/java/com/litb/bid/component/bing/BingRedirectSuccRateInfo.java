package com.litb.bid.component.bing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.bid.util.JsonMapper;

import java.io.IOException;

public class BingRedirectSuccRateInfo {
    // to be continue
    private SiteType siteType;
    private LanguageType languageType;
    private int categoryId;

    private double redirectSuccRate;

    // constructor
    public BingRedirectSuccRateInfo(SiteType siteType, LanguageType languageType, int categoryId, double redirectSuccRate) {
        super();
        this.siteType = siteType;
        this.languageType = languageType;
        this.categoryId = categoryId;
        this.redirectSuccRate = redirectSuccRate;
    }

    // public methods
    @Override
    public String toString(){
            try{
                return JsonMapper.toJsonString(this);
            }catch (JsonProcessingException e){
                e.printStackTrace();
                return null;
            }


    }

    public static BingRedirectSuccRateInfo parse(String line) throws IOException {
        return (BingRedirectSuccRateInfo)JsonMapper.parseJsonString(line, BingRedirectSuccRateInfo.class);
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

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public double getRedirectSuccRate() {
        return redirectSuccRate;
    }

    public void setRedirectSuccRate(double redirectSuccRate) {
        this.redirectSuccRate = redirectSuccRate;
    }
}
