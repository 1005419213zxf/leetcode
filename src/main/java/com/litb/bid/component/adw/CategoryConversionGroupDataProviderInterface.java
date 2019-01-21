package com.litb.bid.component.adw;

import com.litb.adw.lib.enums.FeedCountry;
import com.litb.basic.enums.LanguageType;
import com.litb.bid.object.adw.DeviceStatItem;

public interface CategoryConversionGroupDataProviderInterface {
	public DeviceStatItem[] getPlaCategoryConversionGroupData(FeedCountry country, int cid, int avgConvDays);
	public DeviceStatItem[] getSearchCategoryConversionGroupData(LanguageType languageType, int cid, int avgConvDays);
}
