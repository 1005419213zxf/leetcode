package com.litb.bid.object.bing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.litb.bid.Conf;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.bing.lib.enums.BingChannel;
import com.litb.bing.lib.enums.BingStatus;
import com.litb.bing.lib.operation.report.BingAttribute;
import com.litb.bing.lib.operation.report.BingKeywordAttribute;
import com.litb.bing.lib.util.BingCampaignNameHelper;

public class BingKeywordPeriodData extends BingBiddableObject {
    public final static String SEPARATOR = "_&&_";

    @Override
    public BingAttribute getAttribute() {
        return attribute;
    }


    @JsonIgnore
    @Override
    public SiteType getSiteType() {
        BingKeywordAttribute attribute = (BingKeywordAttribute) getAttribute();
        String campaignName = attribute.getCampaignName();
        String url = attribute.getFinalUrl();
        SiteType siteType = BingCampaignNameHelper.getSiteType(campaignName, url);
        return siteType;
    }

    @JsonIgnore
    @Override
    public BingChannel getChannel() {
        BingKeywordAttribute attribute = (BingKeywordAttribute) getAttribute();
        String campaignName = attribute.getCampaignName();
        BingChannel bingChannel = BingCampaignNameHelper.getBingChannel(campaignName);
        return bingChannel;

    }

    @JsonIgnore
    @Override
    public LanguageType getLanguageType() {
        String campaignName = ((BingKeywordAttribute) attribute).getCampaignName();
        String finalUrl = ((BingKeywordAttribute) attribute).getFinalUrl();
        LanguageType languageType = BingCampaignNameHelper.getLanguageTypeFromCampaignName(campaignName, "");
        return languageType;
    }

    @JsonIgnore
    @Override
    public double getMaxCpc() {
        double currentMaxCpc = ((BingKeywordAttribute) attribute).getCurrentMaxCpc();
        return currentMaxCpc;
    }


    @JsonIgnore
    @Override
    public long getAccountId() {
        return ((BingKeywordAttribute) attribute).getAccountId();
    }

    @JsonIgnore
    @Override
    public long getCampaignId() {
        return ((BingKeywordAttribute) attribute).getCampaignId();
    }

    @JsonIgnore
    @Override
    public String getCampaignName() {
        return ((BingKeywordAttribute) attribute).getCampaignName();
    }

    @JsonIgnore
    @Override
    public BingStatus getCampaignStatus() {
        return ((BingKeywordAttribute) attribute).getCampaignStatus();
    }

    @JsonIgnore
    @Override
    public long getAdGroupId() {
        return ((BingKeywordAttribute) attribute).getAdgroupId();
    }

    @JsonIgnore
    @Override
    public String getAdGroupName() {
        return ((BingKeywordAttribute) attribute).getAdgroupName();
    }

    @JsonIgnore
    @Override
    public BingStatus getAdGroupStatus() {
        return ((BingKeywordAttribute) attribute).getAdgroupStatus();
    }

    @JsonIgnore
    @Override
    public long getCriterionId() {
        return ((BingKeywordAttribute) attribute).getKeywordId();
    }

    @JsonIgnore
    @Override
    public String getCriterionText() {
        return ((BingKeywordAttribute) attribute).getKeywordText();
    }

    @JsonIgnore
    @Override
    public BingStatus getCriterionStatus() {
        return ((BingKeywordAttribute) attribute).getKeywordStatus();
    }

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

    public static BingKeywordPeriodData parse(String line) {
        BingKeywordPeriodData bingKeywordPeriodData = new BingKeywordPeriodData();
        BingKeywordAttribute attribute = new BingKeywordAttribute();
        String[] stringArr = line.split(SEPARATOR);
        String attributeString = stringArr[0];
        attribute.fillAttributeDataForwardly(attributeString.split(BingAttribute.SEPARATOR));
        bingKeywordPeriodData.setAttribute(attribute);
        BingDeviceStatItem[] statItems = new BingDeviceStatItem[Conf.STAT_INTERVALS.length];
        for (int i = 0; i < statItems.length; i++) {
            statItems[i] = BingDeviceStatItem.parse(stringArr[i + 1]);
            if (statItems[i] == null) {
                statItems[i] = new BingDeviceStatItem(Conf.STAT_INTERVALS[i]);
            }
        }
        bingKeywordPeriodData.setIntervalStatItems(statItems);
        return bingKeywordPeriodData;
    }

    public static void main(String[] args) {
//        BingKeywordsReport bingKeywordsReport = new BingKeywordsReport();
//        BingKeywordPeriodData periodData = new BingKeywordPeriodData();
//        periodData.setAttribute(bingKeywordsReport.getAttribute());
//
//        BingDeviceStatItem[] statItems = new BingDeviceStatItem[3];
//
//        BingDeviceStatItem deviceStatItem = new BingDeviceStatItem();
//        deviceStatItem.setSmartPhoneStatItem(new BingStatItem(1));
//        deviceStatItem.setAllStatItem(new BingStatItem(1));
//        statItems[0] = deviceStatItem;
//
//        statItems[1] = new BingDeviceStatItem(1);
//
//        periodData.setIntervalStatItems(statItems);
//        System.out.println(periodData.getAttribute());
//        System.out.println(periodData);


        String line = "1142377\t20926501\t[EN][BingBrand][PLF][c0]lightinthebox-MUL\tACTIVE\t269709106\tlightinthebox brand name\tACTIVE\t6460885393\tlight in the box\tACTIVE\texact_match\t\t\t10\t4.44_&&_1\t\t88\t23\t7.0\t196.0\t14.2\t1.0\t0.0\t\t0\t0\t0\t0\t0\t0\t-_@@_1\t\t19\t5\t3.0\t0.0\t2.0\t1.0\t0.0\t\t0\t0\t0\t0\t0\t0\t-_&&_7\t\t630\t131\t16.0\t1015.0\t63.27\t1.0\t0.0\t\t0\t0\t0\t0\t0\t0\t-_@@_7\t\t109\t15\t4.0\t0.0\t4.15\t1.0\t0.0\t\t0\t0\t0\t0\t0\t0\t-_&&_14\t\t1268\t247\t32.0\t1287.0\t124.64\t1.0\t0.0\t\t0\t0\t0\t0\t0\t0\t-_@@_14\t\t244\t41\t6.0\t0.0\t12.82\t1.0\t0.0\t\t0\t0\t0\t0\t0\t0\t-_&&_28\t\t2068\t407\t45.0\t2024.0\t227.98\t1.01\t0.0\t\t0\t0\t0\t0\t0\t0\t-_@@_28\t\t390\t65\t8.0\t0.0\t23.77\t1.01\t0.0\t\t0\t0\t0\t0\t0\t0\t-_&&_90\t\t7320\t1436\t132.0\t10315.0\t714.31\t1.01\t0.0\t\t0\t0\t0\t0\t0\t0\t-_@@_90\t\t1011\t155\t15.0\t347.0\t51.55\t1.01\t0.0\t\t0\t0\t0\t0\t0\t0\t-_&&_180\t\t9932\t2267\t194.0\t17788.0\t1037.08\t1.01\t0.0\t\t0\t0\t0\t0\t0\t0\t-_@@_180\t\t1483\t273\t19.0\t497.0\t97.33\t1.01\t0.0\t\t0\t0\t0\t0\t0\t0\t-_&&_365\t\t12425\t3665\t318.0\t29294.12\t1618.92\t1.01\t0.0\t\t0\t0\t0\t0\t0\t0\t-_@@_365\t\t1863\t432\t27.0\t497.0\t132.22\t1.01\t0.0\t\t0\t0\t0\t0\t0\t0\t-_&&_730\t\t15003\t5255\t487.0\t46745.69\t2757.94\t1.01\t0.0\t\t0\t0\t0\t0\t0\t0\t-_@@_730\t\t1992\t487\t27.0\t497.0\t149.98\t1.01\t0.0\t\t0\t0\t0\t0\t0\t0\t-_&&_3650\t\t41601\t21949\t1418.0\t137669.67\t9132.92\t1.01\t0.0\t\t0\t0\t0\t0\t0\t0\t-_@@_3650\t\t1994\t487\t27.0\t497.0\t149.98\t1.01\t0.0\t\t0\t0\t0\t0\t0\t0\t-";
        BingKeywordPeriodData data = parse(line);
        BingKeywordPeriodData data1 = data;
        BingKeywordPeriodData data2 = data;

        System.out.println(data.attribute);
    }
}
