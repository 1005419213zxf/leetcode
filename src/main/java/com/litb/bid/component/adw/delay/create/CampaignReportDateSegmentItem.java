package com.litb.bid.component.adw.delay.create;

import com.litb.basic.util.DateHelper;

import java.util.Date;
import java.util.StringTokenizer;

public class CampaignReportDateSegmentItem {
	private long accountId;
	private long campaignId;
	private String campaignName;
	private String campaignStatusString;
	private long impression;
	private long click;
	private long conversionOnePerClick;
	private long conversionManyPerClick;
	private double convValue;
	private double cost;
	private Date statDate;
	private Date getDate;
	
	public static CampaignReportDateSegmentItem parse(String line) throws Exception {
		StringTokenizer st = new StringTokenizer(line, "\t");
		if(st.countTokens() != 12) throw new Exception();
		CampaignReportDateSegmentItem item = new CampaignReportDateSegmentItem();
		item.accountId = Long.parseLong(st.nextToken());
		item.campaignId = Long.parseLong(st.nextToken());
		item.campaignName = st.nextToken();
		item.campaignStatusString = st.nextToken();
		item.impression = Long.parseLong(st.nextToken());
		item.click = Long.parseLong(st.nextToken());
		item.conversionOnePerClick = Long.parseLong(st.nextToken());
		item.conversionManyPerClick = Long.parseLong(st.nextToken());
		item.convValue = Double.parseDouble(st.nextToken());
		item.cost = Double.parseDouble(st.nextToken());
		item.statDate = DateHelper.getShortDate(st.nextToken());
		item.getDate = DateHelper.getShortDate(st.nextToken());
		
		return item;
	}

	
	
	// get and set
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

	public String getCampaignName() {
		return campaignName;
	}

	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}

	public String getCampaignStatusString() {
		return campaignStatusString;
	}

	public void setCampaignStatusString(String campaignStatusString) {
		this.campaignStatusString = campaignStatusString;
	}

	public long getImpression() {
		return impression;
	}

	public void setImpression(long impression) {
		this.impression = impression;
	}

	public long getClick() {
		return click;
	}

	public void setClick(long click) {
		this.click = click;
	}

	public long getConversionOnePerClick() {
		return conversionOnePerClick;
	}

	public void setConversionOnePerClick(long conversionOnePerClick) {
		this.conversionOnePerClick = conversionOnePerClick;
	}

	public long getConversionManyPerClick() {
		return conversionManyPerClick;
	}

	public void setConversionManyPerClick(long conversionManyPerClick) {
		this.conversionManyPerClick = conversionManyPerClick;
	}

	public double getConvValue() {
		return convValue;
	}

	public void setConvValue(double convValue) {
		this.convValue = convValue;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public Date getStatDate() {
		return statDate;
	}

	public void setStatDate(Date statDate) {
		this.statDate = statDate;
	}

	public Date getGetDate() {
		return getDate;
	}

	public void setGetDate(Date getDate) {
		this.getDate = getDate;
	}
	
}
