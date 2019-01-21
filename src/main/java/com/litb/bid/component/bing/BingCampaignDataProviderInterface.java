package com.litb.bid.component.bing;


import com.litb.bid.object.bing.BingDeviceStatItem;

public interface BingCampaignDataProviderInterface {
    public BingDeviceStatItem[] getCampaignData(long accountId, long campaignId);
}
