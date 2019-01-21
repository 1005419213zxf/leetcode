package com.litb.bid.component.adw;

import com.litb.adw.lib.enums.AdwordsCountry;
import com.litb.adw.lib.enums.LitbAdChannel;

public interface AddToCart2OrderConversionProviderInterface {
	public double getAddToCart2OrderConversionRate(LitbAdChannel channel, AdwordsCountry adwordsCountry, int categoryId); 
}
