package com.litb.bid.object.adw;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.adw.lib.enums.AdwordsStatus;
import com.litb.adw.lib.operation.report.*;
import com.litb.adw.lib.util.AdwordsCampaignNameHelper;
import com.litb.adw.lib.util.AdwordsCategoryIdHelper;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.bid.Conf;
import com.litb.bid.util.CpTreeFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;


public abstract class BiddableObject {
	
	protected AdwordsAttribute attribute;
	protected DeviceStatItem[] intervalStatItems = new DeviceStatItem[Conf.STAT_INTERVALS.length];
	
	public abstract AdwordsAttribute getAttribute();
	public abstract SiteType getSiteType();
	public abstract AdwordsChannel getChannel();
	public abstract LanguageType getLanguageType();
	public abstract double getMaxCpc();
	
	public abstract long getAccountId();
	
	public abstract long getCampaignId();
	public abstract String getCampaignName();
	public abstract AdwordsStatus getCampaignStatus();
	
	public abstract long getAdGroupId();
	public abstract String getAdGroupName();
	public abstract AdwordsStatus getAdGroupStatus();
	
	public abstract long getCriterionId();
	public abstract String getCriterionText();
	public abstract AdwordsStatus getCriterionStatus();
	
	public static BiddableObject parse(String line, Class<? extends BiddableObject> clz){
		if(clz == KeywordPeriodData.class)
			return KeywordPeriodData.parse(line);
		if(clz == ShoppingPeriodData.class)
			return ShoppingPeriodData.parse(line);
		if(clz == AudiencePeriodData.class)
			return AudiencePeriodData.parse(line);
		return null;
	}
	@JsonIgnore
	public StatItem[] getAllStatItems(){
		StatItem[] statItems = new StatItem[intervalStatItems.length];
		for(int i = 0; i < intervalStatItems.length; i++)
			statItems[i] = intervalStatItems[i].getAllDeviceStatItem();
		return statItems;
	}
	public DeviceStatItem[] getIntervalStatItems() {
		return intervalStatItems;
	}
	public void setIntervalStatItems(DeviceStatItem[] IntervalStatItems) {
		this.intervalStatItems = IntervalStatItems;
	}
	public void setAttribute(AdwordsAttribute attribute) {
		this.attribute = attribute;
	}
	@JsonIgnore
	public DeviceStatItem getFirstIntervalStatItem(){
		return intervalStatItems[0];
	}
	@JsonIgnore
	public DeviceStatItem getLastIntervalStatItem(){
		return intervalStatItems[intervalStatItems.length - 1];
	}
	
	// utility
	@JsonIgnore
	public int getCategoryId(){
		try {
			if(attribute instanceof ShoppingAttribute){
				SiteType siteType = getSiteType();
				CPTree cpTree = CpTreeFactory.getProductCpTree(siteType,false);
//				ShoppingAttribute shoppingAttribute = (ShoppingAttribute)attribute;
//				return shoppingAttribute.getCategoryId(cpTree);
				int cid = AdwordsCategoryIdHelper.getCategoryId(cpTree, getCriterionText(), siteType);
				if(cid == -1){
					cid = AdwordsCampaignNameHelper.getCategoryIdFromCampaignName(getCampaignName());
				}
//				return AdwordsCategoryIdHelper.getCategoryId(cpTree, getCriterionText(), siteType);
				return cid;
			}
			else if(attribute instanceof KeywordAttribute){
				KeywordAttribute keywordAttribute = (KeywordAttribute)attribute;
				int cid = keywordAttribute.getCategoryId();
				SiteType siteType = getSiteType();
				CPTree cpTree = CpTreeFactory.getCategoryCpTree(siteType, false);
				if(cpTree.getCategoryTopCategory(cid) < 0)
					cid = AdwordsCampaignNameHelper.getCategoryIdFromCampaignName(getCampaignName());
				return cid;
			} if(attribute instanceof AudienceAttribute){
				AudienceAttribute audienceAttribute = (AudienceAttribute)attribute;
				return audienceAttribute.getCategoryId();
			}
			else 
				throw new IllegalArgumentException("cannot get category id from attribute " + attribute.getClass().getSimpleName());
		} catch (Exception e) {
			e.printStackTrace();
			return AdwordsCampaignNameHelper.getCategoryIdFromCampaignName(getCampaignName());
		}
	}
	
	@JsonIgnore
	public int getOrderNum(){
		try {
			if(attribute instanceof AudienceAttribute){
				AudienceAttribute audienceAttribute = (AudienceAttribute)attribute;
				return audienceAttribute.getOrderNum();
			}
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	@JsonIgnore
	public int getProductId(){
		try {
			if(attribute instanceof ShoppingAttribute){
				ShoppingAttribute shoppingAttribute = (ShoppingAttribute)attribute;
				return shoppingAttribute.getProductId();
			}
			else if(attribute instanceof KeywordAttribute){
				return Conf.ROOT_CID;
			} if(attribute instanceof AudienceAttribute){
				return Conf.ROOT_CID;
			}
			else 
				throw new IllegalArgumentException("cannot get product id from attribute " + attribute.getClass().getSimpleName());
		} catch (Exception e) {
			e.printStackTrace();
			return Conf.ROOT_CID;
		}
	}
	
	/**
	 * 由statInterval=180天内的转化记录计算平均转化时间；
	 * 对于新上线的投放单元，statInterval取不小于投放天数的最接近天数
	 * @return 平均转化天数 
	 * [0,1]->1
	 * (1,7]->7
	 * (7,14]->14
	 * (14,28]->28
	 * (28,90]->90
	 * (90,180]->180
	 * (180,Inf]->3650
	 */
	@JsonIgnore
	public int getAverageConversionDays() {
		// find statInterval
		int statInterval = 180;
		for (int index = 0; index < Conf.STAT_INTERVALS.length-2; index++) {
			AdwordsMetric currentMetric = intervalStatItems[index].getAllDeviceStatItem().getReportMetric();
			AdwordsMetric nextMetric = intervalStatItems[index+1].getAllDeviceStatItem().getReportMetric();
			if (currentMetric.getImpressions() == nextMetric.getImpressions() 
				&& currentMetric.getClicks() == nextMetric.getClicks()) {
				statInterval = Conf.STAT_INTERVALS[index];
				break;
			}
		}
		
		// get average conversion days
		DeviceStatItem deviceStatItem = intervalStatItems[Conf.getIntervalIndex(statInterval)];
		StatItem statItem = deviceStatItem.getAllDeviceStatItem();
		double conversions = statItem.getReportMetric().getConversions();
		if (conversions > 0) {
			int conversionDays = (int)Math.round(statInterval/conversions);
			if (conversionDays == 0 || conversionDays == 1)
				return 1;
			for (int index = 0; index < Conf.STAT_INTERVALS.length-1; index++) {
				if (conversionDays > Conf.STAT_INTERVALS[index] && conversionDays <= Conf.STAT_INTERVALS[index+1])
					return Conf.STAT_INTERVALS[index+1];
			}
		}
		return Conf.MAX_INTERVAL_DAYS;
	}
	
	public static void main(String[] args) throws IOException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
//		AdwordsMetric metric = new AdwordsMetric();
//		System.out.println(metric.toString());
//		String str = JsonMapper.toJsonString(metric);
//		System.out.println(str);
//		
//		metric = (AdwordsMetric)JsonMapper.parseJsonString(str, AdwordsMetric.class);
//		System.out.println(metric.toString());
//		
//		System.out.println(KeywordPeriodData.class.getDeclaredMethod("parse", String.class).invoke(null, "sss"));
//		for (int conversions=0; conversions < 200; conversions++) {
//			System.out.println(conversions + "->" + getAverageConversionDays(conversions));
//		}
	}
}
