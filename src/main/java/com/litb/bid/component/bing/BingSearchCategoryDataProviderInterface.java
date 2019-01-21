package com.litb.bid.component.bing;

import com.litb.basic.enums.Country;
import com.litb.basic.enums.LanguageType;
import com.litb.bid.object.bing.BingDeviceStatItem;

public interface BingSearchCategoryDataProviderInterface {
     BingDeviceStatItem[] getCategoryData(int cid, Country country, LanguageType languageType);
     BingDeviceStatItem[] getCategoryDataTest(int cid, LanguageType languageType);
}
