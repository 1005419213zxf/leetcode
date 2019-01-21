package com.litb.bid.component.bing;

import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;

public interface BingRedirectSuccRateProviderInterface {
    public BingRedirectSuccRateInfo getRediretSuccRate(SiteType siteType, LanguageType languageType);
}
