package com.litb.bid.component.bing;

import com.litb.basic.enums.LanguageType;

public interface BingValueAdjustDataProviderInterface {
    //
    public BingValueAdjustRateInfo getValueAdjustRate(LanguageType languageType, int cid);
}
