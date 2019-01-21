package com.litb.bid.component.adw.ppv;

import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.basic.enums.LanguageType;

import java.util.List;
import java.util.Map;

public interface CrPredictorInterface {

	/**
	 * Get predicted conversion rate from PPV-UV distribution data.
	 * @param channel
	 * @param languageType
	 * @param isFromMobileDevice
	 * @param uvList
	 * @return
	 */
	PredictiveCrInfo getPredictiveCr(AdwordsChannel channel, LanguageType languageType, Boolean isFromMobileDevice,
                                     int categoryId, List<Integer> uvList);

	/**
	 * Get predicted conversion rate from PPV-UV distribution data.
	 * @param channel
	 * @param languageType
	 * @param isFromMobileDevice
	 * @param ppvUvMap
	 * @return
	 */
	PredictiveCrInfo getPredictiveCr(AdwordsChannel channel, LanguageType languageType, Boolean isFromMobileDevice,
                                     int categoryId, Map<Integer, Integer> ppvUvMap);
}
