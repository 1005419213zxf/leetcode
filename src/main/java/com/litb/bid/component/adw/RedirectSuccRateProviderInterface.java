package com.litb.bid.component.adw;

import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;

public interface RedirectSuccRateProviderInterface {
	
	public RedirectSuccRateInfo getRediretSuccRate(SiteType siteType, LanguageType languageType);
}
