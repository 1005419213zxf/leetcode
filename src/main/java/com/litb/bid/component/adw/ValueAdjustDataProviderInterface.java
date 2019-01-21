package com.litb.bid.component.adw;

import com.litb.basic.enums.LanguageType;

public interface ValueAdjustDataProviderInterface {

	public ValueAdjustRateInfo getValueAdjustRate(LanguageType languageType, int cid);
}
