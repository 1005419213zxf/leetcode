package com.litb.bid.object.adw;

import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.adw.lib.enums.AdwordsStatus;
import com.litb.adw.lib.enums.LitbAdChannel;
import com.litb.adw.lib.operation.report.AdwordsAttribute;
import com.litb.adw.lib.operation.report.AudienceAttribute;
import com.litb.adw.lib.util.AdwordsCampaignNameHelper;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.bid.Conf;

public class AudiencePeriodData extends BiddableObject {

	public final static String SEPARATOR = "_&&_";

	@Override
	public AdwordsAttribute getAttribute() {
		return attribute;
	}
	
	public void setAttribute(AudienceAttribute attribute) {
		this.attribute = attribute;
	}

	@Override
	public SiteType getSiteType() {
		String campaignName = ((AudienceAttribute)attribute).getCampaignName();
		SiteType siteType = AdwordsCampaignNameHelper.getSiteType(campaignName);
		return siteType;
	}

	@Override
	public AdwordsChannel getChannel() {
		String campaignName = ((AudienceAttribute)attribute).getCampaignName();
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
	public LanguageType getLanguageType() {
		String campaignName = ((AudienceAttribute)attribute).getCampaignName();
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
	
	public static AudiencePeriodData parse(String line) {
		AudiencePeriodData data = new AudiencePeriodData();
		AudienceAttribute attribute = new AudienceAttribute();
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
	
	@Override
	public long getAccountId() {
		return ((AudienceAttribute)attribute).getAccountId();
	}

	@Override
	public long getCampaignId() {
		return ((AudienceAttribute)attribute).getCampaignId();
	}

	@Override
	public String getCampaignName() {
		return ((AudienceAttribute)attribute).getCampaignName();
	}

	@Override
	public long getAdGroupId() {
		return ((AudienceAttribute)attribute).getAdgroupId();
	}

	@Override
	public String getAdGroupName() {
		return ((AudienceAttribute)attribute).getAdgroupName();
	}

	@Override
	public long getCriterionId() {
		return ((AudienceAttribute)attribute).getCriterionId();
	}

	@Override
	public String getCriterionText() {
		return ((AudienceAttribute)attribute).getAudienceText();
	}
	
	@Override
	public double getMaxCpc() {
		AudienceAttribute AudienceAttribute = (AudienceAttribute)attribute;
		return AudienceAttribute.getCpcBid();
	}
	
	public static void main(String[] args) {
//		AudienceAttribute attribute = new AudienceAttribute();
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
//		data.intervalStatItems = statItems;
//		System.out.println(data.toString());
//		System.out.println(KeywordPeriodData.parse(data.toString()).toString());
	}

	@Override
	public AdwordsStatus getCampaignStatus() {
		return ((AudienceAttribute)attribute).getCampaignStatus();
	}

	@Override
	public AdwordsStatus getAdGroupStatus() {
		return ((AudienceAttribute)attribute).getAdgroupStatus();
	}

	@Override
	public AdwordsStatus getCriterionStatus() {
		return ((AudienceAttribute)attribute).getAudienceStatus();
	}
}
