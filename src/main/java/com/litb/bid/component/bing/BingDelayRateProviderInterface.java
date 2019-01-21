package com.litb.bid.component.bing;


import com.litb.bid.component.bing.delay.BingDelayRateInfo;
import com.litb.bid.object.bing.BingLitbAdChannel;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;

public interface BingDelayRateProviderInterface {

    /**
     *
     * @param channel
     * @param siteType
     * @param language
     * @param categoryId
     * @param daysInterval	: [1, infinite), the given number of days. For [startDate, endDate], daysInterval = endDate - startDate + 1
     * @param getOffsetDays	: [1, infinite), the duration from endDate and view date (when you get the data)
     * @param predictOffsetDays : [1, infinite), more than getOffsetDays
     * @return
     */
    public BingDelayRateInfo getDelayRate(BingLitbAdChannel channel, SiteType siteType, LanguageType language, int categoryId, int daysInterval, int getOffsetDays, int predictOffsetDays);

}
