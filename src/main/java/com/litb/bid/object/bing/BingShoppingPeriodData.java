package com.litb.bid.object.bing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.litb.bid.Conf;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.bing.lib.enums.BingChannel;
import com.litb.bing.lib.enums.BingStatus;
import com.litb.bing.lib.operation.report.BingAttribute;
import com.litb.bing.lib.operation.report.BingShoppingAttribute;
import com.litb.bing.lib.util.BingCampaignNameHelper;

public class BingShoppingPeriodData extends BingBiddableObject {
    public static final String SEPARATOR = "_&&_";

    @Override
    public String toString() {
        String res = "" + attribute;
        if (intervalStatItems == null) {
            intervalStatItems = new BingDeviceStatItem[Conf.STAT_INTERVALS.length];
        }
        for (int i = 0; i < intervalStatItems.length; i++) {
            res += SEPARATOR + intervalStatItems[i];
        }
        return res;
    }

    public static BingShoppingPeriodData parse(String line) {
        BingShoppingPeriodData bingShoppingPeriodData = new BingShoppingPeriodData();

        BingShoppingAttribute attribute = new BingShoppingAttribute();
        String[] stringArr = line.split(SEPARATOR);
        String attributeString = stringArr[0];
        attribute.fillAttributeDataForwardly(attributeString.split(BingAttribute.SEPARATOR));
        bingShoppingPeriodData.setAttribute(attribute);

        BingDeviceStatItem[] statItems = new BingDeviceStatItem[Conf.STAT_INTERVALS.length];
        for (int i = 0; i < statItems.length; i++) {
            statItems[i] = BingDeviceStatItem.parse(stringArr[i + 1]);
            if (statItems[i] == null) {
                statItems[i] = new BingDeviceStatItem(Conf.STAT_INTERVALS[i]);
            }
        }
        bingShoppingPeriodData.setIntervalStatItems(statItems);
        return bingShoppingPeriodData;
    }


    @Override
    public BingAttribute getAttribute() {
        return attribute;
    }

    @JsonIgnore
    @Override
    public SiteType getSiteType() {
        BingShoppingAttribute attribute = (BingShoppingAttribute) getAttribute();
        String campaignName = attribute.getCampaignName();
        String url = "";
        SiteType siteType = BingCampaignNameHelper.getSiteType(campaignName, url);
        return siteType;
    }

    @JsonIgnore
    @Override
    public BingChannel getChannel() {
        BingShoppingAttribute attribute = (BingShoppingAttribute) getAttribute();
        String campaignName = attribute.getCampaignName();
        BingChannel bingChannel = BingCampaignNameHelper.getBingChannel(campaignName);
        return bingChannel;
    }

    @JsonIgnore
    @Override
    public LanguageType getLanguageType() {
        String campaignName = ((BingShoppingAttribute) attribute).getCampaignName();
        String finalUrl = "";
        LanguageType languageType = BingCampaignNameHelper.getLanguageTypeFromCampaignName(campaignName, "");
        return languageType;
    }

    @JsonIgnore
    @Override
    public double getMaxCpc() {
        return ((BingShoppingAttribute) attribute).getCurrentMaxCpc();
    }

    @JsonIgnore
    @Override
    public long getAccountId() {
        return ((BingShoppingAttribute) attribute).getAccountId();
    }

    @JsonIgnore
    @Override
    public long getCampaignId() {
        return ((BingShoppingAttribute) attribute).getCampaignId();
    }

    @JsonIgnore
    @Override
    public String getCampaignName() {
        return ((BingShoppingAttribute) attribute).getCampaignName();
    }

    @JsonIgnore
    @Override
    public BingStatus getCampaignStatus() {
        return ((BingShoppingAttribute) attribute).getCampaignStatus();
    }

    @JsonIgnore
    @Override
    public long getAdGroupId() {
        return ((BingShoppingAttribute) attribute).getAdgroupId();
    }

    @JsonIgnore
    @Override
    public String getAdGroupName() {
        return ((BingShoppingAttribute) attribute).getAdgroupName();
    }

    @JsonIgnore
    @Override
    public BingStatus getAdGroupStatus() {
        return ((BingShoppingAttribute) attribute).getAdgroupStatus();
    }

    @JsonIgnore
    @Override
    public long getCriterionId() {
        return ((BingShoppingAttribute) attribute).getAdGroupCriterionId();
    }

    @JsonIgnore
    @Override
    public String getCriterionText() {
        return ((BingShoppingAttribute) attribute).getProductGroup();
    }

    @JsonIgnore
    @Override
    public BingStatus getCriterionStatus() {
        return ((BingShoppingAttribute) attribute).getAdStatus();
    }
}
