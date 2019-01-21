package com.litb.bid.component.adw.ppv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.bid.util.JsonMapper;

import java.io.IOException;

public class PredictiveCrInfo {
	private AdwordsChannel channel; 
	private SiteType siteType;
	private LanguageType languageType;
	private int categoryId;
	private Boolean isMobile;
	
	private double predictiveCr;
	
	// public methods
	public String toString() {
		try {
			return JsonMapper.toJsonString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static PredictiveCrInfo parse(String line) throws IOException {
		return (PredictiveCrInfo)JsonMapper.parseJsonString(line, PredictiveCrInfo.class);
	}
	
	// Getters and Setters
	public AdwordsChannel getChannel() {
		return channel;
	}

	public void setChannel(AdwordsChannel channel) {
		this.channel = channel;
	}

	public LanguageType getLanguageType() {
		return languageType;
	}

	public void setLanguageType(LanguageType languageType) {
		this.languageType = languageType;
	}

	public double getPredictiveCr() {
		return predictiveCr;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public void setPredictiveCr(double predictiveCr) {
		this.predictiveCr = predictiveCr;
	}

	public SiteType getSiteType() {
		return siteType;
	}

	public void setSiteType(SiteType siteType) {
		this.siteType = siteType;
	}

	public Boolean getIsMobile() {
		return isMobile;
	}

	public void setIsMobile(Boolean isMobile) {
		this.isMobile = isMobile;
	}

	// main for test
	public static void main(String[] args) {
		
	}
}
