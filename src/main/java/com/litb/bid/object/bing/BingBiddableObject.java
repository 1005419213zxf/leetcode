package com.litb.bid.object.bing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.litb.bid.Conf;
import com.litb.bid.util.CpTreeFactory;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.bing.lib.enums.BingChannel;
import com.litb.bing.lib.enums.BingStatus;
import com.litb.bing.lib.operation.report.BingAttribute;
import com.litb.bing.lib.operation.report.BingAudienceAttribute;
import com.litb.bing.lib.operation.report.BingKeywordAttribute;
import com.litb.bing.lib.operation.report.BingShoppingAttribute;
import com.litb.bing.lib.util.BingCampaignNameHelper;
import com.litb.bing.lib.util.BingCategoryIdHelper;

public abstract class BingBiddableObject {
    protected BingAttribute attribute;
    protected BingDeviceStatItem[] intervalStatItems = new BingDeviceStatItem[Conf.STAT_INTERVALS.length];

    public abstract BingAttribute getAttribute();

    public abstract SiteType getSiteType();

    public abstract BingChannel getChannel();

    public abstract LanguageType getLanguageType();

    public abstract double getMaxCpc();

    public abstract long getAccountId();

    public abstract long getCampaignId();

    public abstract String getCampaignName();

    public abstract BingStatus getCampaignStatus();

    public abstract long getAdGroupId();

    public abstract String getAdGroupName();

    public abstract BingStatus getAdGroupStatus();

    public abstract long getCriterionId();

    public abstract String getCriterionText();

    public abstract BingStatus getCriterionStatus();

    public static BingBiddableObject parse(String line, Class<? extends BingBiddableObject> clz) {
        if (clz == BingKeywordPeriodData.class) {
            return BingKeywordPeriodData.parse(line);
        }
        if (clz == BingAudiencePeriodData.class) {
            return BingAudiencePeriodData.parse(line);
        }
        return null;
    }


    @JsonIgnore
    public BingStatItem[] getAllStatItems() {
        BingStatItem[] bingStatItems = new BingStatItem[intervalStatItems.length];
        for (int i = 0; i < intervalStatItems.length; i++) {
            bingStatItems[i] = intervalStatItems[i].getAllStatItem();
        }
        return bingStatItems;
    }

    @JsonIgnore
    public BingDeviceStatItem getFirstIntervalStatItem() {
        return intervalStatItems[0];
    }

    @JsonIgnore
    public BingDeviceStatItem getLastIntervalStatItem() {
        return intervalStatItems[intervalStatItems.length - 1];
    }


    @JsonIgnore
    public int getCategoryId() {
        String campaignName = getCampaignName();
        String finalUrl = "";
        try {
            if (attribute instanceof BingKeywordAttribute) {
                BingKeywordAttribute bingKeywordAttribute = (BingKeywordAttribute) attribute;
                int cid = bingKeywordAttribute.getCategoryId();
                campaignName = bingKeywordAttribute.getCampaignName();
                finalUrl = bingKeywordAttribute.getFinalUrl();
                SiteType siteType = BingCampaignNameHelper.getSiteType(campaignName, finalUrl);
                CPTree cpTree = CpTreeFactory.getCategoryCpTree(siteType, false);
                if (cpTree.getCategoryTopCategory(cid) < 0) {
                    cid = BingCampaignNameHelper.getCategoryIdFromCampaignName(campaignName, finalUrl);
                }
                return cid;
            }
            if (attribute instanceof BingShoppingAttribute) {
                BingShoppingAttribute bingShoppingAttribute = (BingShoppingAttribute) attribute;
                campaignName = bingShoppingAttribute.getCampaignName();
                SiteType siteType = BingCampaignNameHelper.getSiteType(campaignName, finalUrl);
                CPTree cpTree = CpTreeFactory.getCategoryCpTree(siteType, false);
                int cid = BingCategoryIdHelper.getCategoryId(cpTree, getCriterionText(), siteType);
                if (cid < 0) {
                    cid = BingCampaignNameHelper.getCategoryIdFromCampaignName(campaignName, finalUrl);
                }
                return cid;
            }
            if (attribute instanceof BingAudienceAttribute) {
                BingAudienceAttribute bingAudienceAttribute = (BingAudienceAttribute) attribute;
                //TODO BingAudienceAttribute 新增getCatagoryId
//                int cid = bingAudienceAttribute.getCatagoryId();
                return -1;
            }
            throw new IllegalArgumentException("cannot get category id from attribute " + attribute.getClass().getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
            return BingCampaignNameHelper.getCategoryIdFromCampaignName(campaignName, finalUrl);
        }
    }

    //    @JsonIgnore
//    public int getProductId(){
//        try {
//            if(attribute instanceof BingShoppingAttribute){
//                BingShoppingAttribute shoppingAttribute = (BingShoppingAttribute)attribute;
//                //TODO add getProductId
////                return shoppingAttribute.getProductId();
//                return 0;
//            }
//            else if(attribute instanceof BingKeywordAttribute){
//                return Conf.ROOT_CID;
//            } if(attribute instanceof BingAudienceAttribute){
//                return Conf.ROOT_CID;
//            }
//            else
//                throw new IllegalArgumentException("cannot get product id from attribute " + attribute.getClass().getSimpleName());
//        } catch (Exception e) {
//            e.printStackTrace();
//            return Conf.ROOT_CID;
//        }
//    }


    //getter and setter

    public void setAttribute(BingAttribute attribute) {
        this.attribute = attribute;
    }

    public BingDeviceStatItem[] getIntervalStatItems() {
        return intervalStatItems;
    }

    public void setIntervalStatItems(BingDeviceStatItem[] intervalStatItems) {
        this.intervalStatItems = intervalStatItems;
    }
}
