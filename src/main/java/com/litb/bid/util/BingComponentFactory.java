package com.litb.bid.util;

import com.litb.adw.lib.exception.AdwordsApiException;
import com.litb.adw.lib.exception.AdwordsRemoteException;
import com.litb.adw.lib.exception.AdwordsValidationException;
import com.litb.basic.enums.SiteType;
import com.litb.basic.util.DateHelper;
import com.litb.bid.Conf;
import com.litb.bid.component.bing.*;
import com.litb.bid.component.bing.delay.BingAccountDelayRateProviderBi;
import com.litb.bid.component.bing.delay.BingDelayRateProviderBi;
import com.litb.bing.lib.enums.BingChannel;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BingComponentFactory {
    private static Date endDate;

    static {
        //getShortDateString  yyyy-MM-dd,默认当天的前一天
        endDate = DateHelper.getShortDate(DateHelper.getShortDateString(new Date()));
        endDate = DateHelper.addDays(-1, endDate);
        System.out.println("component factory end date: " + DateHelper.getShortDateString(endDate));
    }

    public static void setEndDate(Date endDate) {
        BingComponentFactory.endDate = endDate;
        System.out.println("component factory end date: " + DateHelper.getShortDateString(endDate));
    }

    public static Date getEndDate() {
        return endDate;
    }

    private static BingAccountExchangeRateProvider accountExchangeRateProvider;

    private static BingMetricProviderInterface bingMetricProviderInterface;
    private static BingAvgCpcAgainstMaxCpcRatioProviderInterface avgCpcAgainstMaxCpcRatioProviderInterface;
    private static BingRedirectSuccRateProviderInterface redirectSuccRateProviderInterface;


    private static Map<SiteType, BingSearchCategoryDataProviderInterface> keySearchCategoryDataProviderInterface = new HashMap<SiteType, BingSearchCategoryDataProviderInterface>();
    private static Map<String, BingCampaignDataProviderInterface> keyBingCampaignDataProviderInterface = new HashMap<String, BingCampaignDataProviderInterface>();


    private static Map<String, BingAvgCpcAgainstMaxCpcRatioHistoryProvider> avgCpcAgainstMaxCpcRatioHistoryProviderMap = new HashMap<String, BingAvgCpcAgainstMaxCpcRatioHistoryProvider>();
    private static Map<SiteType, BingDelayRateProviderInterface> keyDelayRateProviderInterfaceMap = new HashMap<SiteType, BingDelayRateProviderInterface>();

    private static Map<SiteType, BingPlaCategoryDataProviderInterface> keyPlaCategoryDataProviderInterfaceMap = new HashMap<SiteType, BingPlaCategoryDataProviderInterface>();

    private static Map<SiteType, BingRepeatRateProviderInterface> keyRepeatRateProviderInterfaceMap = new HashMap<SiteType, BingRepeatRateProviderInterface>();

    // private static Map<String, BingCampaignDataProvider> keyCampaignDataProviderInterface = new HashMap<String, BingCampaignDataProvider>();
    private static Map<SiteType, BingAccountDelayRateProviderBi> keyAccountDelayRateProviderMap = new HashMap<SiteType, BingAccountDelayRateProviderBi>();


    // public methods
    public static BingAccountExchangeRateProvider getAccountExchangeRateProvider() throws AdwordsValidationException, AdwordsApiException, AdwordsRemoteException, SQLException {
        synchronized (BingAccountExchangeRateProvider.class) {
            if (accountExchangeRateProvider != null)
                return accountExchangeRateProvider;
            else {
                accountExchangeRateProvider = new BingAccountExchangeRateProvider(endDate);
                return accountExchangeRateProvider;
            }
        }
    }

    public static BingMetricProviderInterface getBingMetricProvider() throws SQLException, IOException {
        synchronized (BingMetricProviderInterface.class) {
            if (bingMetricProviderInterface != null)
                return bingMetricProviderInterface;
            else {
                bingMetricProviderInterface = new BingMetricProvider(endDate, Conf.CAMPAIGN_SUMMARY_INTEVAL);
                return bingMetricProviderInterface;
            }
        }
    }

    public static BingMetricProviderInterface getBingMetricProvider(int interval) throws SQLException, IOException {
        synchronized (BingMetricProviderInterface.class) {
            if (bingMetricProviderInterface != null)
                return bingMetricProviderInterface;
            else {
                bingMetricProviderInterface = new BingMetricProvider(endDate, interval);
                return bingMetricProviderInterface;
            }
        }
    }


    public static BingAvgCpcAgainstMaxCpcRatioProviderInterface getAvgCpcAgainstMaxCpcRatioProviderInterface() {
        synchronized (BingAvgCpcAgainstMaxCpcRatioProviderInterface.class) {
            if (avgCpcAgainstMaxCpcRatioProviderInterface != null)
                return avgCpcAgainstMaxCpcRatioProviderInterface;
            else {
                avgCpcAgainstMaxCpcRatioProviderInterface = new BingAvgCpcAgainstMaxCpcRatioProvider();
                return avgCpcAgainstMaxCpcRatioProviderInterface;
            }
        }
    }

    //
    private static Map<SiteType, BingValueAdjustDataProviderInterface> keyValueAdjustDataProviderInterfaceMap = new HashMap<SiteType, BingValueAdjustDataProviderInterface>();

    public static BingValueAdjustDataProviderInterface getValueAdjustDataProvider(SiteType siteType) throws SQLException {
        BingValueAdjustDataProviderInterface valueAdjustDataProviderInterface = keyValueAdjustDataProviderInterfaceMap.get(siteType);
        if (valueAdjustDataProviderInterface != null)
            return valueAdjustDataProviderInterface;
        synchronized (keyValueAdjustDataProviderInterfaceMap) {
            valueAdjustDataProviderInterface = keyValueAdjustDataProviderInterfaceMap.get(siteType);
            if (valueAdjustDataProviderInterface != null)
                return valueAdjustDataProviderInterface;
            else {
                valueAdjustDataProviderInterface = new BingValueAdjustDataProvider(siteType);    // 20160712
                keyValueAdjustDataProviderInterfaceMap.put(siteType, valueAdjustDataProviderInterface);
                return valueAdjustDataProviderInterface;
            }
        }
    }


    public static BingSearchCategoryDataProviderInterface getSearchCategoryDataProviderInterface(SiteType siteType) throws IOException, SQLException {
        BingSearchCategoryDataProviderInterface searchCategoryDataProviderInterface = keySearchCategoryDataProviderInterface.get(siteType);
        if (searchCategoryDataProviderInterface != null)
            return searchCategoryDataProviderInterface;
        synchronized (keySearchCategoryDataProviderInterface) {
            searchCategoryDataProviderInterface = keySearchCategoryDataProviderInterface.get(siteType);
            if (searchCategoryDataProviderInterface != null)
                return searchCategoryDataProviderInterface;
            else {
                searchCategoryDataProviderInterface = new BingSearchCategoryDataProvider(siteType, endDate);
                keySearchCategoryDataProviderInterface.put(siteType, searchCategoryDataProviderInterface);
                return searchCategoryDataProviderInterface;
            }
        }
    }

    public static BingCampaignDataProviderInterface getCampaignDataProviderInterface(SiteType siteType, BingChannel channel, Date endDate) throws IOException, SQLException {
        String key = getKey(siteType, channel);
        BingCampaignDataProviderInterface searchCampaignDataProviderInterface = keyBingCampaignDataProviderInterface.get(key);
        if (searchCampaignDataProviderInterface != null)
            return searchCampaignDataProviderInterface;
        synchronized (keyBingCampaignDataProviderInterface) {
            searchCampaignDataProviderInterface = keyBingCampaignDataProviderInterface.get(key);
            if (searchCampaignDataProviderInterface != null)
                return searchCampaignDataProviderInterface;
            else {
                searchCampaignDataProviderInterface = new BingCampaignDataProvider(siteType, channel, endDate);
                keyBingCampaignDataProviderInterface.put(key, searchCampaignDataProviderInterface);
                return searchCampaignDataProviderInterface;
            }
        }
    }


    public static BingAvgCpcAgainstMaxCpcRatioHistoryProvider getAvgCpcAgainstMaxCpcRatioHistoryProvider(Date endDate, BingChannel channel) {
        String key = DateHelper.getShortDateString(endDate) + "\t" + channel;
        BingAvgCpcAgainstMaxCpcRatioHistoryProvider avgCpcAgainstMaxCpcRatioHistoryProvider = avgCpcAgainstMaxCpcRatioHistoryProviderMap.get(key);
        if (avgCpcAgainstMaxCpcRatioHistoryProvider != null)
            return avgCpcAgainstMaxCpcRatioHistoryProvider;
        synchronized (avgCpcAgainstMaxCpcRatioHistoryProviderMap) {
            avgCpcAgainstMaxCpcRatioHistoryProvider = avgCpcAgainstMaxCpcRatioHistoryProviderMap.get(key);
            if (avgCpcAgainstMaxCpcRatioHistoryProvider != null)
                return avgCpcAgainstMaxCpcRatioHistoryProvider;
            else {
                avgCpcAgainstMaxCpcRatioHistoryProvider = new BingAvgCpcAgainstMaxCpcRatioHistoryProvider(endDate, channel);
                avgCpcAgainstMaxCpcRatioHistoryProviderMap.put(key, avgCpcAgainstMaxCpcRatioHistoryProvider);
                return avgCpcAgainstMaxCpcRatioHistoryProvider;
            }
        }
    }


    public static BingPlaCategoryDataProviderInterface getPlaCategoryDataProviderInterface(SiteType siteType) throws IOException, SQLException {
        BingPlaCategoryDataProviderInterface plaCategoryDataProviderInterface = keyPlaCategoryDataProviderInterfaceMap.get(siteType);
        if (plaCategoryDataProviderInterface != null)
            return plaCategoryDataProviderInterface;
        synchronized (keyPlaCategoryDataProviderInterfaceMap) {
            plaCategoryDataProviderInterface = keyPlaCategoryDataProviderInterfaceMap.get(siteType);
            if (plaCategoryDataProviderInterface != null)
                return plaCategoryDataProviderInterface;
            else {
                plaCategoryDataProviderInterface = new BingPlaCategoryDataProvider(siteType, endDate);
                keyPlaCategoryDataProviderInterfaceMap.put(siteType, plaCategoryDataProviderInterface);
                return plaCategoryDataProviderInterface;
            }
        }
    }


    public static BingRedirectSuccRateProviderInterface getRedirectSuccRateProvider() {
        synchronized (BingRedirectSuccRateProviderInterface.class) {
            if (redirectSuccRateProviderInterface != null)
                return redirectSuccRateProviderInterface;
            else {
                redirectSuccRateProviderInterface = new BingRedirectSuccRateProvider();
                return redirectSuccRateProviderInterface;
            }
        }
    }

    public static BingRepeatRateProviderInterface getRepeatRateProvider(SiteType siteType) throws SQLException, IOException {
        BingRepeatRateProviderInterface repeatRateProviderInterface = keyRepeatRateProviderInterfaceMap.get(siteType);
        if (repeatRateProviderInterface != null)
            return repeatRateProviderInterface;
        synchronized (keyRepeatRateProviderInterfaceMap) {
            repeatRateProviderInterface = keyRepeatRateProviderInterfaceMap.get(siteType);
            if (repeatRateProviderInterface != null)
                return repeatRateProviderInterface;
            else {
//				repeatRateProviderInterface = new RepeatRateProviderV20160712(siteType);	// 20160712
                repeatRateProviderInterface = new BingRepeatRateWithWeekProvider(siteType);
                keyRepeatRateProviderInterfaceMap.put(siteType, repeatRateProviderInterface);
                return repeatRateProviderInterface;
            }
        }
    }


    public static BingAccountDelayRateProviderBi getAccountDelayRateProvider(SiteType siteType) throws NumberFormatException, IOException, SQLException {
        BingAccountDelayRateProviderBi delayRateProvider = keyAccountDelayRateProviderMap.get(siteType);
        if (delayRateProvider != null)
            return delayRateProvider;
        synchronized (keyAccountDelayRateProviderMap) {
            delayRateProvider = keyAccountDelayRateProviderMap.get(siteType);
            if (delayRateProvider != null)
                return delayRateProvider;
            else {
                delayRateProvider = new BingAccountDelayRateProviderBi(siteType);
                keyAccountDelayRateProviderMap.put(siteType, delayRateProvider);
                return delayRateProvider;
            }
        }
    }

    //
    private static String getKey(SiteType siteType, BingChannel channel) {
        return siteType.getSiteCode() + "\t" + channel.getChannelId();
    }


    public static BingDelayRateProviderInterface getDelayRateProvider(SiteType siteType) throws NumberFormatException, IOException, SQLException {
        BingDelayRateProviderInterface delayRateProviderInterface = keyDelayRateProviderInterfaceMap.get(siteType);
        if (delayRateProviderInterface != null)
            return delayRateProviderInterface;
        synchronized (keyDelayRateProviderInterfaceMap) {
            delayRateProviderInterface = keyDelayRateProviderInterfaceMap.get(siteType);
            if (delayRateProviderInterface != null)
                return delayRateProviderInterface;
            else {
//				delayRateProviderInterface = new DelayRateProviderV160712(siteType);	// 20160712
                //暂定以下引用
                /**
                 * 使用BI计算的delay rate
                 * 修改者：赵宏宏
                 * 需求来源：时斌
                 */
                delayRateProviderInterface = new BingDelayRateProviderBi(siteType);    // 20160712
                keyDelayRateProviderInterfaceMap.put(siteType, delayRateProviderInterface);
                return delayRateProviderInterface;
            }
        }
    }


}
