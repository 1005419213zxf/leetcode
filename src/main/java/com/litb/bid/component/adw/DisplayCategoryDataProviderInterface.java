package com.litb.bid.component.adw;

import com.litb.bid.object.adw.DeviceStatItem;
import com.litb.adw.lib.enums.AdwordsCountry;
import com.litb.basic.enums.LanguageType;

public interface DisplayCategoryDataProviderInterface {
	public DeviceStatItem[] getCategoryData(int cid, LanguageType languageType);
	public DeviceStatItem[] getCategoryData(int cid, AdwordsCountry country);
}
