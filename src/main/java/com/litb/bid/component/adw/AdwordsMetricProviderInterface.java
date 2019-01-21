package com.litb.bid.component.adw;

import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;

public interface AdwordsMetricProviderInterface {
	
//	public AdwordsMetricInfo getCampaignMetric(long accountId, long campaignId);
//	public AdwordsMetricInfo getCategoryMetric(AdwordsChannel channel, SiteType siteType, LanguageType languageType, int categoryId, int interval);
	public AdwordsDeviceMetricInfo getDeviceMetric(long accountId, long campaignId,
                                                   SiteType siteType, AdwordsChannel channel, LanguageType languageType, int categoryId,
                                                   int minAllDeviceConversions, int minPcConverisons, int minMobileConversions);
}
