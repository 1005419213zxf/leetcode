package com.litb.bid.component.bing;


import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.bing.lib.enums.BingChannel;

public interface BingMetricProviderInterface {
    public BingDeviceMetricInfo getDeviceMetric(long accountId, long campaignId,
                                                SiteType siteType, BingChannel channel, LanguageType languageType, int categoryId,
                                                int minAllDeviceConversions, int minPcConverisons, int minMobileConversions);
}
