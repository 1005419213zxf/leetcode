package com.litb.bid.component.bing;


import com.litb.basic.enums.Country;
import com.litb.basic.enums.LanguageType;
import com.litb.bid.object.bing.BingDeviceStatItem;

public interface BingPlaCategoryDataProviderInterface {
    public BingDeviceStatItem[] getCategoryData(int cid, Country country, LanguageType languageType);
//    public BingDeviceStatItem[] getCategoryDataTest(Country country, int cid);
}
