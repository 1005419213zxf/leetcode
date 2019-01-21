package com.litb.bid.object.adw;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.adw.lib.enums.AdwordsStatus;
import com.litb.adw.lib.enums.LitbAdChannel;
import com.litb.adw.lib.operation.report.AdwordsAttribute;
import com.litb.adw.lib.operation.report.KeywordAttribute;
import com.litb.adw.lib.util.AdwordsCampaignNameHelper;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.bid.Conf;

public class KeywordPeriodData extends BiddableObject {
	
	public final static String SEPARATOR = "_&&_";

	@Override
	public AdwordsAttribute getAttribute() {
		return attribute;
	}
	
	public void setAttribute(KeywordAttribute attribute) {
		this.attribute = attribute;
	}

	@JsonIgnore
	@Override
	public SiteType getSiteType() {
		String campaignName = ((KeywordAttribute)attribute).getCampaignName();
		SiteType siteType = AdwordsCampaignNameHelper.getSiteType(campaignName);
		return siteType;
	}

	@JsonIgnore
	@Override
	public AdwordsChannel getChannel() {
		String campaignName = ((KeywordAttribute)attribute).getCampaignName();
		LitbAdChannel litbAdChannel = AdwordsCampaignNameHelper.getLitbAdChannel(campaignName);
		switch (litbAdChannel) {
		case adwords_search:
			return AdwordsChannel.search;
		case adwords_display:
			return AdwordsChannel.display;
		case adwords_pla:
			return AdwordsChannel.pla;
		case adwords_rmkt:
			return AdwordsChannel.remarketing;
		default:
			return null;
		}
	}

	@Override
	@JsonIgnore
	public LanguageType getLanguageType() {
		String campaignName = ((KeywordAttribute)attribute).getCampaignName();
		LanguageType languageType = AdwordsCampaignNameHelper.getLanguageTypeFromCampaignName(campaignName);
		return languageType;
	}
	
	@Override
	public String toString() {
		String res = "" + attribute;
		if (intervalStatItems == null)
			intervalStatItems = new DeviceStatItem[Conf.STAT_INTERVALS.length];
		for (int i = 0; i < intervalStatItems.length; i ++) {
			res += SEPARATOR + intervalStatItems[i];
		}
		return res;
	}
	
	public static KeywordPeriodData parse(String line) {
		KeywordPeriodData data = new KeywordPeriodData();
		KeywordAttribute attribute = new KeywordAttribute();
		String[] ss = line.split(SEPARATOR);
		String s = ss[0];
		String[] ss1 = s.split(AdwordsAttribute.SEPARATOR);
		attribute.fillAttributeDataForwardly(ss1);
		data.setAttribute(attribute);
		DeviceStatItem[] statItems = new DeviceStatItem[Conf.STAT_INTERVALS.length];
		for (int i = 0; i < statItems.length; i ++) {
			statItems[i] = DeviceStatItem.parse(ss[i+1]);
			if (statItems[i] == null) {
				statItems[i] = new DeviceStatItem(Conf.STAT_INTERVALS[i]);
			}
		}
		data.intervalStatItems = statItems;
		return data;
	}
	
	@JsonIgnore
	@Override
	public long getAccountId() {
		return ((KeywordAttribute)attribute).getAccountId();
	}

	@JsonIgnore
	@Override
	public long getCampaignId() {
		return ((KeywordAttribute)attribute).getCampaignId();
	}

	@JsonIgnore
	@Override
	public String getCampaignName() {
		return ((KeywordAttribute)attribute).getCampaignName();
	}

	@JsonIgnore
	@Override
	public long getAdGroupId() {
		return ((KeywordAttribute)attribute).getAdgroupId();
	}

	@JsonIgnore
	@Override
	public String getAdGroupName() {
		return ((KeywordAttribute)attribute).getAdgroupName();
	}

	@JsonIgnore
	@Override
	public long getCriterionId() {
		return ((KeywordAttribute)attribute).getKeywordId();
	}

	@JsonIgnore
	@Override
	public String getCriterionText() {
		return ((KeywordAttribute)attribute).getKeywordText();
	}
	
	@JsonIgnore
	@Override
	public double getMaxCpc() {
		KeywordAttribute keywordAttribute = (KeywordAttribute)attribute;
		return keywordAttribute.getCpcBid();
	}
	
	public static void main(String[] args) {
//		KeywordAttribute attribute = new KeywordAttribute();
//		attribute.setAccountId(12345);
//		attribute.setAdgroupId(234567);
//		attribute.setCampaignId(3456);
//		attribute.setCpcBid(12.3);
//		KeywordPeriodData data = new KeywordPeriodData();
//		data.setAttribute(attribute);
//		StatItem[] statItems = new StatItem[Conf.STAT_INTERVALS.length];
//		for (int i = 0; i < statItems.length; i ++) {
//			statItems[i] = new StatItem(Conf.STAT_INTERVALS[i]);
//		}
//		statItems[0].getLogMetric().setUv(999);
//		data.setStatItems(statItems);
//		System.out.println(data.toString());
//		System.out.println(KeywordPeriodData.parse(data.toString()).toString());
	}

	@JsonIgnore
	@Override
	public AdwordsStatus getCampaignStatus() {
		return ((KeywordAttribute)attribute).getCampaignStatus();
	}

	@JsonIgnore
	@Override
	public AdwordsStatus getAdGroupStatus() {
		return ((KeywordAttribute)attribute).getAdgroupStatus();
	}

	@JsonIgnore
	@Override
	public AdwordsStatus getCriterionStatus() {
		return ((KeywordAttribute)attribute).getKeywordStatus();
	}
}
