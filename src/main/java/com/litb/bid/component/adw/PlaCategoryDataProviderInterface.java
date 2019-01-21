package com.litb.bid.component.adw;

import com.litb.bid.object.adw.DeviceStatItem;
import com.litb.adw.lib.enums.FeedCountry;

public interface PlaCategoryDataProviderInterface {
	public DeviceStatItem[] getCategoryData(FeedCountry country, int cid);
	public DeviceStatItem[] getCategoryDataTest(FeedCountry country, int cid);
}
