package com.litb.bid;

import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.adw.lib.enums.AdwordsDevice;
import com.litb.adw.lib.enums.AdwordsReportType;
import com.litb.basic.enums.SiteType;
import com.litb.basic.util.DateHelper;

import java.util.Date;

public class DirDef {
	private static final String BASE_DIR = "/mnt/adwords/auto_bidding/";
	public static final String S3_BASE_DIR = "s3://dm-adwords-bidding/";
	private static final String EMR_JAR_NAME = "dm_bidding.jar";
	public static final String EMR_JAR_PATH = S3_BASE_DIR + "binary/" + EMR_JAR_NAME;
	
	/*
	 *  base directory
	 */
	public static String getDailyDir(Date endDate){
		return BASE_DIR + "daily_data/" + DateHelper.getDateString(endDate, "yyyy-MM") + "/" + DateHelper.getShortDateString(endDate) + "/";
	}
	private static String getS3DailyDir(Date endDate){
		return S3_BASE_DIR + "daily_data/" + DateHelper.getDateString(endDate, "yyyy-MM") + "/" + DateHelper.getShortDateString(endDate) + "/";
	}
	private static String getS3DailyDir(Date endDate, SiteType siteType){
		return getS3DailyDir(endDate) + siteType + "/";
	}
	
	public static String getLocalBiddableObjectDataDir(SiteType siteType, AdwordsChannel channel, Date endDate){
		return getDailyDir(endDate) + siteType + "/" + "biddable_object_" + channel + "/";
	}
	
	public static String getLocalBiddableObjectDataDir(SiteType siteType, AdwordsChannel channel, Date endDate, AdwordsReportType reportType){
		return getDailyDir(endDate) + siteType + "/" + "biddable_object_" + channel + "_" + reportType + "/";
	}
	
	public static String getLocalBiddingResultPath(SiteType siteType, AdwordsChannel channel, Date endDate){
		return getDailyDir(endDate) + siteType + "/" + "bidding_result/" + "bidding_result_" + channel;
	}
	
	public static String getLocalBiddingResultReportPath(SiteType siteType, AdwordsChannel channel, Date endDate){
		return getDailyDir(endDate) + siteType + "/" + "bidding_result/" + "bidding_result_report_" + channel;
	}
	
	public static String getLocalBiddingResultStatisticPath(SiteType siteType, AdwordsChannel channel, Date endDate){
		return getDailyDir(endDate) + siteType + "/" + "bidding_result/" + "bidding_result_statistic" + channel;
	}
	
	public static String getLocalBiddableObjectProductAggregationFilePath(SiteType siteType, AdwordsChannel channel, Date endDate){
		return getDailyDir(endDate) + siteType + "/" + "biddable_object_aggregation_" + channel + "/product_aggregation_data";
	}
	
	public static String getLocalBiddableObjectCategoryAggregationFilePath(SiteType siteType, AdwordsChannel channel, Date endDate){
		return getDailyDir(endDate) + siteType + "/" + "biddable_object_aggregation_" + channel + "/category_aggregation_data";
	}
	
	
	//group
	public static String getLocalBiddableObjectProductGroupFileDir(SiteType siteType, AdwordsChannel channel, Date endDate){
		return getDailyDir(endDate) + siteType + "/biddable_object_group_" + channel + "/biddable_object_with_product_group_data/";
	}
	
	public static String getLocalBiddableObjectCategoryGroupFilePath(SiteType siteType, AdwordsChannel channel, Date endDate){
		return getDailyDir(endDate) + siteType + "/biddable_object_group_" + channel + "/category_group_data";
	}
	
	public static String getLocalBiddableObjectCategoryAddToCartFilePath(SiteType siteType, AdwordsChannel channel, Date endDate){
		return getDailyDir(endDate) + siteType + "/" + "biddable_object_group_" + channel + "/category_addtocart_data";
	}
	
	public static String getLocalBiddableObjectCategoryConversionGroupFilePath(SiteType siteType, AdwordsChannel channel, Date endDate){
		return getDailyDir(endDate) + siteType + "/biddable_object_group_" + channel + "/category_conversion_group_data";
	}
	
	public static String getS3BiddableObjectProductGroupFileDir(SiteType siteType, AdwordsChannel channel, Date endDate){
		return getS3DailyDir(endDate, siteType) + "biddable_object_group_" + channel + "/biddable_object_with_product_group_data/";
	}
	
	public static String getLocalBiddableObjectCampaignGroupFilePath(SiteType siteType, AdwordsChannel channel, Date endDate){
		return getDailyDir(endDate) + siteType + "/biddable_object_group_" + channel + "/campaign_group_data";
	}
	/*
	 * log 
	 */
	// daily log process result
	public static String getS3LogDailyDataDir(SiteType siteType, Date endDate){
		return S3_BASE_DIR + "daily_log_process/" + siteType + "/" + DateHelper.getDateString(endDate, "yyyy-MM") + "/" + 
				DateHelper.getShortDateString(endDate) + "/";
	}
	// interval log result
	public static String getS3LogIntervalDataDir(SiteType siteType, Date beginDate, Date endDate){
		return getS3DailyDir(endDate, siteType) + "log_interval/" + DateHelper.getShortDateString(beginDate) + "_to_" + DateHelper.getShortDateString(endDate) + "/";
	}
	// biddable object result
	public static String getS3BiddableObjectDataDir(SiteType siteType, AdwordsChannel channel, Date endDate){
		return getS3DailyDir(endDate, siteType) + "biddable_object_" + channel + "/";
	}
	
	public static String getS3BiddableObjectDataDir(SiteType siteType, AdwordsChannel channel, Date endDate, AdwordsReportType reportType){
		return getS3DailyDir(endDate, siteType) + "biddable_object_" + channel + "_" + reportType + "/";
	}
	
	/*
	 * report
	 */
	// original reports
	public static String getOriginalReportDir(Date endDate, AdwordsReportType reportType, int interval, AdwordsDevice device){
		Date beginDate = DateHelper.addDays(1 - interval, endDate);
		return  getDailyDir(endDate) + "original_reports_" + reportType + (device == null ? "" : "_" + device) + "/" + 
				"interval_" + interval + "_" + DateHelper.getShortDateString(beginDate) + "_to_" + DateHelper.getShortDateString(endDate) + "/";
	}
	public static String getOriginalReportFilePath(Date endDate, AdwordsReportType reportType, int interval, AdwordsDevice device, long accountId){
		return getOriginalReportDir(endDate, reportType, interval, device) + accountId + ".csv.gz";
	}
	// formatted reports
	public static String getFormattedReportDir(Date endDate, AdwordsReportType reportType, int interval, AdwordsDevice device){
		Date beginDate = DateHelper.addDays(1 - interval, endDate);
		return  getDailyDir(endDate) + "formatted_reports_" + reportType + (device == null ? "" : "_" + device) + "/" + 
				"interval_" + interval + "_" + DateHelper.getShortDateString(beginDate) + "_to_" + DateHelper.getShortDateString(endDate) + "/";
	}
	public static String getFormattedReportFilePath(Date endDate, AdwordsReportType reportType, int interval, AdwordsDevice device, long accountId){
		return getFormattedReportDir(endDate, reportType, interval, device) + accountId + ".gz";
	}
	// S3 reports
	public static String getS3ReportDir(Date endDate, AdwordsReportType reportType, int interval, AdwordsDevice device){
		Date beginDate = DateHelper.addDays(1 - interval, endDate);
		return  S3_BASE_DIR + "daily_reports/" + DateHelper.getDateString(endDate, "yyyy-MM") + "/" + 
				DateHelper.getShortDateString(endDate) + "/" + reportType + (device == null ? "" : "_" + device) + "/" + 
				"interval_" + interval + "_" + DateHelper.getShortDateString(beginDate) + "_to_" + DateHelper.getShortDateString(endDate) + "/";
	}
	public static String getS3IntermediateReportDir(Date endDate, AdwordsReportType reportType){
		return  S3_BASE_DIR + "daily_reports/" + DateHelper.getDateString(endDate, "yyyy-MM") + "/" + 
				DateHelper.getShortDateString(endDate) + "/" + reportType + "_intermediate/";
	}
	public static String getS3ReportFilePath(Date endDate, AdwordsReportType reportType, int interval, AdwordsDevice device, long accountId){
		return getS3ReportDir(endDate, reportType, interval, device) + accountId + ".gz";
	}
	
	public static String getS3SecondReportDir(SiteType siteType, AdwordsChannel channel, Date endDate){
		return getS3DailyDir(endDate, siteType) + "interval_report_" + channel + "/";
	}
	
	// mobile bid modifier
	public static String getMobileBidModifierFilePath(Date endDate, AdwordsChannel targetChannel){
		return getDailyDir(endDate) + "modifier/mobile_bid_modifier_" + (targetChannel == null ? "all" : targetChannel);
	}
	
	// DB cid and search cid compare
	public static String getCidAndNameFromDBFilePath(Date endDate){
		return getDailyDir(endDate) + "cid_from_DB";
	}   
	public static String getCidNameWithUrlFilePath(SiteType siteType, Date date) {
		return getDailyDir(date) + "cid_from_DB_url_" + siteType ;
	}
	public static String getS3CidAndNameFromDBFilePath(Date endDate) {
		return S3_BASE_DIR + "daily_reports/" + DateHelper.getDateString(endDate, "yyyy-MM") + "/" + 
		DateHelper.getShortDateString(endDate) + "/cid_from_DB";
	}
	
	public static String getS3DBAndSearchCompareFilePath(SiteType siteType, Date endDate) {
		return S3_BASE_DIR + "daily_reports/" + DateHelper.getDateString(endDate, "yyyy-MM") + "/" + 
		DateHelper.getShortDateString(endDate) + "/DB_and_search_cid_compare/" + siteType + "/";
	}
	public static String getDBAndSearchCompareFilePath(SiteType siteType, Date endDate) {
		return getDailyDir(endDate) + "DB_and_search_cid_compare/" + siteType + "/";
	}
	public static String getNoSearchCidFilePath(SiteType siteType, Date endDate) {
		return getDailyDir(endDate) + "no_search_cid/" + siteType + "_not_in_report_cidName.csv.gz" ;
	}
	public static String getUrlNoSearchCidFilePath(SiteType siteType, Date endDate) {
		return getDailyDir(endDate) + "no_search_cid/" + siteType + "_not_in_report_cidName_withUrl" ;
	}
	
//	public static String getTargetRoiModifierFilePath(SiteType siteType, Date endDate, AdwordsChannel targetChannel){
//		return getDailyDir(endDate) + siteType + "/" + "modifier/target_roi_modifier_" + targetChannel + "/modifier";
//	}
	public static String getActRoiForTargetRoiModifierFilePath(SiteType siteType, Date endDate, AdwordsChannel targetChannel){
		return getDailyDir(endDate) + siteType + "/" + "modifier/target_roi_modifier_" + targetChannel + "/actual_roi_info";
	}
	// boosted ratio
	public static String getBoostedRatioFilePath(Date endDate, AdwordsChannel targetChannel){
		return getDailyDir(endDate) + "boosted_ratio/boosted_" + (targetChannel == null ? "all" : targetChannel);
	}
	
	
	// depended component data path
	public static String getDelayRatePath(Date endDate, int interval){
		return S3_BASE_DIR + "component_data/" + DateHelper.getDateString(endDate, "yyyy-MM") + "/" +
				"interval_" + interval + "_" + DateHelper.getShortDateString(endDate) + "/delay_data";
	}
	
	// ROI
	public static String getTargetRoiPath(SiteType siteType, AdwordsChannel channel){
		return BASE_DIR + "config/" + siteType + "/" + channel + "_target_roi.config";
	}
	public static String getTargetRoiModifierPath(SiteType siteType, AdwordsChannel channel){
		return BASE_DIR + "config/" + siteType + "/" + channel + "_target_roi_modifier.config";
	}

	
	
}
