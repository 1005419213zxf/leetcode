package com.litb.bid.component.adw;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.bid.object.adw.AdwordsDeviceMetric;
import com.litb.bid.util.JsonMapper;

import java.io.IOException;

public class AdwordsDeviceMetricInfo {
	private boolean isCampaignData;
	
	private long accountId;
	private long campaignId;
	
	private SiteType siteType;
	private AdwordsChannel channel;
	private LanguageType languageType;
	private int categoryId;
	
	private AdwordsDeviceMetric deviceMetric;
	
	// public methods
	
	@Override
	public String toString(){
		try {
			return JsonMapper.toJsonString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static AdwordsDeviceMetricInfo parse(String line) throws IOException{
		return (AdwordsDeviceMetricInfo)JsonMapper.parseJsonString(line, AdwordsDeviceMetricInfo.class);
	}

	// Getters and Setters
	public boolean isCampaignData() {
		return isCampaignData;
	}

	public void setCampaignData(boolean isCampaignData) {
		this.isCampaignData = isCampaignData;
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

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public AdwordsDeviceMetric getDeviceMetric() {
		return deviceMetric;
	}

	public void setDeviceMetric(AdwordsDeviceMetric deviceMetric) {
		this.deviceMetric = deviceMetric;
	}
}
