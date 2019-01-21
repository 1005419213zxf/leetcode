package com.litb.bid.object.bing;

import com.litb.bid.Conf;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.bing.lib.enums.BingChannel;
import com.litb.bing.lib.enums.BingStatus;
import com.litb.bing.lib.operation.report.BingAttribute;
import com.litb.bing.lib.operation.report.BingAudienceAttribute;
import com.litb.bing.lib.util.BingCampaignNameHelper;

public class BingAudiencePeriodData extends BingBiddableObject {
    public static final String SEPARATOR = "_&&_";

    @Override
    public BingAttribute getAttribute() {
        return attribute;
    }

    @Override
    public SiteType getSiteType() {
        String campaignName = ((BingAudienceAttribute) attribute).getCampaignName();
        //TODO
        String url = "";
        SiteType siteType = BingCampaignNameHelper.getSiteType(campaignName, url);
        return siteType;
    }

    @Override
    public BingChannel getChannel() {

        String campaignName = ((BingAudienceAttribute) attribute).getCampaignName();
        BingChannel bingChannel = BingCampaignNameHelper.getBingChannel(campaignName);
        return bingChannel;
    }

    @Override
    public LanguageType getLanguageType() {
        //TODO
        return null;
    }

    @Override
    public double getMaxCpc() {
        //TODO 无cpc
        return 0;
    }

    @Override
    public long getAccountId() {
        return ((BingAudienceAttribute) attribute).getAccountId();
    }

    @Override
    public long getCampaignId() {
        return ((BingAudienceAttribute) attribute).getCampaignId();
    }

    @Override
    public String getCampaignName() {
        return ((BingAudienceAttribute) attribute).getCampaignName();
    }

    @Override
    public BingStatus getCampaignStatus() {
        return ((BingAudienceAttribute) attribute).getCampaignStatus();
    }

    @Override
    public long getAdGroupId() {
        return ((BingAudienceAttribute) attribute).getAdgroupId();
    }

    @Override
    public String getAdGroupName() {
        return ((BingAudienceAttribute) attribute).getAdgroupName();
    }

    @Override
    public BingStatus getAdGroupStatus() {
        return ((BingAudienceAttribute) attribute).getAdgroupStatus();
    }

    @Override
    public long getCriterionId() {
        return ((BingAudienceAttribute) attribute).getCriterionId();
    }

    @Override
    public String getCriterionText() {
        return ((BingAudienceAttribute) attribute).getAudienceText();
    }

    @Override
    public BingStatus getCriterionStatus() {
        return ((BingAudienceAttribute) attribute).getAudienceStatus();
    }

    @Override
    public String toString() {
        String res = attribute.toString();
        if (intervalStatItems == null) {
            intervalStatItems = new BingDeviceStatItem[Conf.STAT_INTERVALS.length];
        }
        for (int i = 0; i < intervalStatItems.length; i++) {
            res += SEPARATOR + intervalStatItems[i];
        }
        return res;
    }

    public static BingAudiencePeriodData parse(String line) {
        BingAudiencePeriodData bingAudiencePeriodData = new BingAudiencePeriodData();
        BingAudienceAttribute attribute = new BingAudienceAttribute();
        String[] stringArr = line.split(SEPARATOR);
        String attributeString = stringArr[0];
        attribute.fillAttributeDataForwardly(attributeString.split(BingAttribute.SEPARATOR));
        bingAudiencePeriodData.setAttribute(attribute);
        BingDeviceStatItem[] statItems = new BingDeviceStatItem[Conf.STAT_INTERVALS.length];
        for (int i = 0; i < statItems.length; i++) {
            statItems[i] = BingDeviceStatItem.parse(stringArr[i + 1]);
            if (statItems[i] == null) {
                statItems[i] = new BingDeviceStatItem(Conf.STAT_INTERVALS[i]);
            }
        }
        bingAudiencePeriodData.setIntervalStatItems(statItems);
        return bingAudiencePeriodData;
    }


    public static void main(String[] args) {
        BingAudiencePeriodData bingAudiencePeriodData = new BingAudiencePeriodData();
        bingAudiencePeriodData.setAttribute(new BingAudienceAttribute());
        BingDeviceStatItem[] statItems = new BingDeviceStatItem[3];
        statItems[0] = new BingDeviceStatItem(1);
        bingAudiencePeriodData.setIntervalStatItems(statItems);
        System.out.println(bingAudiencePeriodData);
    }
}
