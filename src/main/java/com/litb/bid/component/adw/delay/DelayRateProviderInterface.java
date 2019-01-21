package com.litb.bid.component.adw.delay;

import com.litb.adw.lib.enums.LitbAdChannel;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;

public interface DelayRateProviderInterface {
	
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
	public DelayRateInfo getDelayRate(LitbAdChannel channel, SiteType siteType, LanguageType language, int categoryId, int daysInterval, int getOffsetDays, int predictOffsetDays);
}
