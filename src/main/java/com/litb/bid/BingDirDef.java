package com.litb.bid;

import com.litb.basic.enums.SiteType;
import com.litb.basic.log.util.LogPathProvider;
import com.litb.basic.util.DateHelper;
import com.litb.bing.lib.enums.BingChannel;
import com.litb.bing.lib.enums.BingDevice;
import com.litb.bing.lib.enums.BingReportType;

import java.util.Date;
import java.util.List;

public class BingDirDef {
    private static final String BASE_DIR = "/mnt/adwords/auto_bidding/";
    public static final String S3_BASE_DIR = "s3://dm-adwords-bidding/";
    private static final String EMR_JAR_NAME = "dm_bing_bidding.jar";
    public static final String EMR_JAR_PATH = S3_BASE_DIR + "binary/" + EMR_JAR_NAME;

    /*
     *  base directory
     */
    public static String getDailyDir(Date endDate) {
        return BASE_DIR + "daily_data/" + DateHelper.getDateString(endDate, "yyyy-MM") + "/" + DateHelper.getShortDateString(endDate) + "/";
    }

    public static String getDailyDir(SiteType siteType, Date endDate) {
        return getDailyDir(endDate) + siteType + "/";
    }

    public static String getS3DailyDir(Date endDate) {
        return S3_BASE_DIR + "daily_data/" + DateHelper.getDateString(endDate, "yyyy-MM") + "/" + DateHelper.getShortDateString(endDate) + "/";
    }

    public static String getS3DailyDir(SiteType siteType, Date endDate) {
        return getS3DailyDir(endDate) + siteType + "/";
    }

    public static String getLocalBiddableObjectDataDir(SiteType siteType, BingChannel channel, Date endDate) {
        return getDailyDir(endDate) + siteType + "/" + "bing_biddable_object_" + channel + "/";
    }

    public static String getLocalBiddableObjectDataDir(SiteType siteType, BingChannel channel, Date endDate, BingReportType reportType) {
        return getDailyDir(endDate) + siteType + "/" + "bing_biddable_object_" + channel + "_" + reportType + "/";
    }

    public static String getLocalBiddingResultPath(SiteType siteType, BingChannel channel, Date endDate) {
        return getDailyDir(endDate) + siteType + "/" + "bing_bidding_result/" + "bing_bidding_result_" + channel;
    }

    public static String getLocalBiddingResultReportPath(SiteType siteType, BingChannel channel, Date endDate) {
        return getDailyDir(endDate) + siteType + "/" + "bing_bidding_result/" + "bing_bidding_result_report_" + channel;
    }

    public static String getLocalBiddingResultStatisticPath(SiteType siteType, BingChannel channel, Date endDate) {
        return getDailyDir(endDate) + siteType + "/" + "bing_bidding_result/" + "bing_bidding_result_statistic" + channel;
    }

    /*
     * report
     */
    // original reports
    public static String getOriginalReportDir(Date endDate, BingReportType bingReportType, int interval, BingDevice device) {
        Date beginDate = DateHelper.addDays(1 - interval, endDate);
        return getDailyDir(endDate) + "original_reports_" + bingReportType + (device == null ? "" : "_" + device.toString().toLowerCase()) + "/" +
                "interval_" + interval + "_" + DateHelper.getShortDateString(beginDate) + "_to_" + DateHelper.getShortDateString(endDate) + "/";
    }

    public static String getOriginalReportFilePath(Date endDate, BingReportType bingReportType, int interval, BingDevice device, long accountId) {
        return getOriginalReportDir(endDate, bingReportType, interval, device) + accountId + ".csv";
    }

    //formatted reports
    public static String getFormattedReportDir(Date endDate, BingReportType bingReportType, int interval, BingDevice device) {
        Date beginDate = DateHelper.addDays(1 - interval, endDate);
        return getDailyDir(endDate) + "formatted_reports_" + bingReportType + (device == null ? "" : "_" + device.toString().toLowerCase()) + "/" +
                "interval_" + interval + "_" + DateHelper.getShortDateString(beginDate) + "_to_" + DateHelper.getShortDateString(endDate) + "/";
    }

    public static String getFormattedReportFilePath(Date endDate, BingReportType bingReportType, int interval, BingDevice device, long accountId) {
        return getFormattedReportDir(endDate, bingReportType, interval, device) + accountId + ".gz";
    }

    //S3 reports
    public static String getS3ReportDir(Date endDate, BingReportType bingReportType, int interval, BingDevice device) {
        Date beginDate = DateHelper.addDays(1 - interval, endDate);
        return S3_BASE_DIR + "daily_reports/" + DateHelper.getDateString(endDate, "yyyy-MM") + "/" +
                DateHelper.getShortDateString(endDate) + "/" + bingReportType + (device == null ? "" : "_" + device.toString().toLowerCase()) + "/" +
                "interval_" + interval + "_" + DateHelper.getShortDateString(beginDate) + "_to_" + DateHelper.getShortDateString(endDate) + "/";
    }

    public static String getS3ReportFilePath(Date endDate, BingReportType bingReportType, int interval, BingDevice device, long accountId) {
        return getS3ReportDir(endDate, bingReportType, interval, device) + accountId + ".gz";
    }

    public static String getS3IntermediateReportDir(Date endDate, BingReportType bingReportType) {
        return S3_BASE_DIR + "daily_reports/" + DateHelper.getDateString(endDate, "yyyy-MM") + "/" +
                DateHelper.getShortDateString(endDate) + "/" + bingReportType + "_intermediate/";
    }

    /*
     * log
     */
    // daily log process result
    public static String getS3LogDailyDataDir(SiteType siteType, Date endDate) {
        return S3_BASE_DIR + "daily_log_process/" + siteType + "/"
                + DateHelper.getDateString(endDate, "yyyy-MM") + "/"
                + DateHelper.getShortDateString(endDate) + "/";
    }

    //local daily log process dir
    public static String getLocalLogDailyDataDir(SiteType siteType, Date endDate) {
        return BASE_DIR + "daily_log_process/" + siteType + "/"
                + DateHelper.getDateString(endDate, "yyyy-MM") + "/"
                + DateHelper.getShortDateString(endDate) + "/";
    }

    //interval log process result
    public static String getS3LogIntervalDataDir(SiteType siteType, Date endDate) {
        return getS3DailyDir(siteType, endDate) + "log_interval/";
    }

    public static String getS3LogIntervalDataDir(SiteType siteType, Date beginDate, Date endDate) {
        return getS3DailyDir(siteType, endDate) + "log_interval/"
                + DateHelper.getShortDateString(beginDate) + "_to_"
                + DateHelper.getShortDateString(endDate) + "/";
    }

    //local interval log process result
    public static String getLocalLogIntervalDataDir(SiteType siteType, Date endDate) {
        return getDailyDir(siteType, endDate) + "log_interval/";
    }

    public static String getLocalLogIntervalDataDir(SiteType siteType, Date beginDate, Date endDate) {
        return getDailyDir(siteType, endDate) + "log_interval/"
                + DateHelper.getShortDateString(beginDate) + "_to_"
                + DateHelper.getShortDateString(endDate) + "/";
    }

    //biddable object result
    public static String getS3BiddableObjectDataDir(SiteType siteType, BingChannel channel, Date endDate) {
        return getS3DailyDir(siteType, endDate) + "bing_biddable_object_" + channel + "/";
    }

//    public static String getS3BiddableObjectDataDir(SiteType siteType, BingChannel channel, BingReportType bingReportType, Date endDate) {
//        return getS3DailyDir(siteType, endDate) + "bing_biddable_object_" + channel + "_" + bingReportType + "/";
//    }

    //group
    public static String getS3BiddableObjectProductGroupFileDir(SiteType siteType, BingChannel channel, Date endDate) {
        return getS3DailyDir(siteType, endDate) + "bing_biddable_object_group_" + channel + "/bing_biddable_object_with_product_group_data/";
    }

    public static String getLocalBiddableObjectCampaignGroupFilePath(SiteType siteType, BingChannel channel, Date endDate) {
        return getDailyDir(endDate) + siteType + "/bing_biddable_object_group_" + channel + "/bing_campaign_group_data";
    }

    public static String getLocalBiddableObjectCategoryGroupFilePath(SiteType siteType, BingChannel channel, Date endDate) {
        return getDailyDir(endDate) + siteType + "/bing_biddable_object_group_" + channel + "/bing_category_group_data";
    }

    public static String getLocalBiddableObjectCategoryAddToCartFilePath(SiteType siteType, BingChannel channel, Date endDate) {
        return getDailyDir(endDate) + siteType + "/" + "bing_biddable_object_group_" + channel + "/category_addtocart_data";
    }

    public static void main(String[] args) {
        long accountId = 1142377L;
        BingReportType bingKeywordsReport = BingReportType.bing_keyword_performance;
        Date endDate = new Date();
        endDate = DateHelper.getShortDate("2019-01-15");

        String originalReportDir = getOriginalReportDir(endDate, bingKeywordsReport, 7, null);
        System.out.println(originalReportDir);

        String originalReportFilePath = getOriginalReportFilePath(endDate, bingKeywordsReport, 7, null, accountId);
        System.out.println(originalReportFilePath);

        String formattedReportFilePath = getFormattedReportFilePath(endDate, bingKeywordsReport, 7, null, accountId);
        System.out.println(formattedReportFilePath);

        String as3FilePath = getS3ReportFilePath(endDate, BingReportType.bing_shopping, 7, BingDevice.SMARTPHONE, accountId);
        System.out.println(as3FilePath);
        System.out.println("+++++++biddable object s3 path++++++");
        System.out.println(getS3BiddableObjectDataDir(SiteType.litb, BingChannel.bing_search, endDate));
        System.out.println(getLocalBiddableObjectDataDir(SiteType.litb, BingChannel.bing_search, endDate));
        System.out.println(getS3BiddableObjectDataDir(SiteType.litb, BingChannel.bing_search, endDate));
        System.out.println(getS3ReportDir(endDate, BingReportType.bing_keyword_performance, 3650, null));
        System.out.println(getS3LogDailyDataDir(SiteType.litb, endDate));

        System.out.println(getLocalBiddableObjectCampaignGroupFilePath(SiteType.litb, BingChannel.bing_search, endDate));

        System.out.println("____________________________________________________________");
        List<String> s3LogFilePathList = LogPathProvider.getS3LogFilePathList(SiteType.litb, false, endDate);
        for (String logPath : s3LogFilePathList) {
            System.out.println(logPath);
        }
    }
}

