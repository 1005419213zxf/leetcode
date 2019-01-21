package com.litb.bid.component.adw;

import com.litb.adw.lib.enums.AdwordsCountry;
import com.litb.basic.enums.LanguageType;

public interface RepeatRateProviderInterface {

	public RepeatRateInfo getRepeatRate(LanguageType languageType, int categoryId); 
	public RepeatRateInfo getRepeatRate(AdwordsCountry adwordsCountry, int categoryId); 
}
