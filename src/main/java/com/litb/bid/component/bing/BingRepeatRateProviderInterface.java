package com.litb.bid.component.bing;

import com.litb.basic.enums.Country;
import com.litb.basic.enums.LanguageType;

public interface BingRepeatRateProviderInterface {
    public BingRepeatRateInfo getRepeatRate(LanguageType languageType, int categoryId);
    public BingRepeatRateInfo getRepeatRate(Country adwordsCountry, int categoryId);

}
