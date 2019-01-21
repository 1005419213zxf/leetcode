package com.litb.bid.util;


import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.adw.lib.exception.AdwordsApiException;
import com.litb.adw.lib.exception.AdwordsRemoteException;
import com.litb.adw.lib.exception.AdwordsValidationException;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.basic.util.DateHelper;
import com.litb.bid.Conf;
import com.litb.bid.component.adw.*;
import com.litb.bid.component.adw.delay.*;
import com.litb.bid.component.adw.ppv.CrPredictorInterface;
import com.litb.bid.component.adw.ppv.S3CrPredictor;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ComponentFactory {
	// end date
	private static Date endDate;
	static{
		endDate = DateHelper.getShortDate(DateHelper.getShortDateString(new Date()));
		endDate = DateHelper.addDays(-1, endDate);
		System.out.println("component factory end date: " + DateHelper.getShortDateString(endDate));
	}
	public static void setEndDate(Date endDate){
		ComponentFactory.endDate = endDate;
		System.out.println("component factory end date: " + DateHelper.getShortDateString(endDate));
	}
	public static Date getEndDate(){
		return endDate;
	}
	// instances
	private static AccountExchangeRateProvider accountExchangeRateProvider;
	private static PlaProductPriceProvider plaProductPriceProvider;
	private static Map<SiteType, CurrencyValueFormatterProvider> siteTypeCurrencyValueFormatterProviderMap = new HashMap<SiteType, CurrencyValueFormatterProvider>();
	private static AdwordsMetricProviderInterface adwordsMetricProviderInterface;
	private static Map<String, ArpuTrendHistoryBiProviderInterface> keyArpuTrendHistoryBiProviderInterfaceMap = new HashMap<String, ArpuTrendHistoryBiProviderInterface>();
	private static AvgCpcAgainstMaxCpcRatioProviderInterface avgCpcAgainstMaxCpcRatioProviderInterface;
	private static Map<String, CategoryStatItemsProvider> keyCategoryStatItemsProviderMap = new HashMap<String, CategoryStatItemsProvider>();
	private static Map<String, ProductStatItemsProvider> keyProductStatItemsProviderMap = new HashMap<String, ProductStatItemsProvider>();
	private static PcCrDivideMobileCrProvider pcCrDivideMobileCrProvider;
	private static RedirectSuccRateProviderInterface redirectSuccRateProviderInterface;
	private static Map<SiteType, RepeatRateProviderInterface> keyRepeatRateProviderInterfaceMap = new HashMap<SiteType, RepeatRateProviderInterface>();
	private static Map<SiteType, DisplayRepeatRateProvider> keyDisplayRepeatRateProviderMap = new HashMap<SiteType, DisplayRepeatRateProvider>();
	private static Map<String, BiddingParametersProvider> keyTargetRoiProviderMap = new HashMap<String, BiddingParametersProvider>();
	private static Map<SiteType, ValueAdjustDataProviderInterface> keyValueAdjustDataProviderInterfaceMap = new HashMap<SiteType, ValueAdjustDataProviderInterface>();
	private static Map<SiteType, IntervalValueAdjustDataProvider> keyIntervalValueAdjustDataProviderMap = new HashMap<SiteType, IntervalValueAdjustDataProvider>();
	private static Map<SiteType, DelayRateProviderInterface> keyDelayRateProviderInterfaceMap = new HashMap<SiteType, DelayRateProviderInterface>();
	private static Map<SiteType, DisplayDelayRateProviderBi> keyDisplayDelayRateProviderMap = new HashMap<SiteType, DisplayDelayRateProviderBi>();
	private static Map<SiteType, AccountDelayRateProviderBi> keyAccountDelayRateProviderMap = new HashMap<SiteType, AccountDelayRateProviderBi>();
	private static Map<SiteType, AddToCart2OrderConversionProviderInterface> keyAddToCart2OrderConversionProviderMap = new HashMap<SiteType, AddToCart2OrderConversionProviderInterface>();
	private static Map<SiteType, CrPredictorInterface> keyCrPredictorInterfaceMap = new HashMap<SiteType, CrPredictorInterface>();
	private static DevicesMetricProvider devicesMetricProvider;
	private static NewItemIdsProvider newItemIdsProvider;
	private static TargetCpcContainer targetCpcContainer;
	private static Map<SiteType, DelayRateProviderInterface> keyFacebookAppInstallDelayRateProviderInterfaceMap = new HashMap<SiteType, DelayRateProviderInterface>();
	private static Map<SiteType, FacebookDelayRateProviderBi> keyFacebookDelayRateProviderInterfaceMap = new HashMap<SiteType, FacebookDelayRateProviderBi>();

	private static Map<String, BiYesterdayCpcProvider> biYesterdayCpcProviderMap = new HashMap<String, BiYesterdayCpcProvider>();
	private static Map<String, AvgCpcAgainstMaxCpcRatioHistoryProvider> avgCpcAgainstMaxCpcRatioHistoryProviderMap = new HashMap<String, AvgCpcAgainstMaxCpcRatioHistoryProvider>();

	// add on 20160601
	private static Map<SiteType, PlaCategoryDataProviderInterface> keyPlaCategoryDataProviderInterfaceMap = new HashMap<SiteType, PlaCategoryDataProviderInterface>();

	private static Map<String, CampaignDataProvider> keyCampaignDataProviderInterface = new HashMap<String, CampaignDataProvider>();

	// added on 20160608
	private static Map<SiteType, SearchCategoryDataProviderInterface> keySearchCategoryDataProviderInterface = new HashMap<SiteType, SearchCategoryDataProviderInterface>();

	private static Map<SiteType, DisplayCategoryDataProviderInterface> keyDisplayCategoryDataProviderInterface = new HashMap<SiteType, DisplayCategoryDataProviderInterface>();


	private static Map<SiteType, CategoryConversionGroupDataProviderInterface> keyCategoryConversionGroupDataProviderInterface = new HashMap<SiteType, CategoryConversionGroupDataProviderInterface>();

	private static Map<String, YesterdayCostChangeProvider> yesterdayCostChangeProviderProviderMap = new HashMap<String, YesterdayCostChangeProvider>();
	
	// public methods
	public static AccountExchangeRateProvider getAccountExchangeRateProvider() throws AdwordsValidationException, AdwordsApiException, AdwordsRemoteException, SQLException{
		synchronized (AccountExchangeRateProvider.class) {
			if(accountExchangeRateProvider != null)
				return accountExchangeRateProvider;
			else {
				accountExchangeRateProvider = new AccountExchangeRateProvider(endDate);
				return accountExchangeRateProvider;
			}
		}
	}
	
	public static PlaProductPriceProvider getPlaProductPriceProvider() throws AdwordsValidationException, AdwordsApiException, AdwordsRemoteException, SQLException{
		synchronized (PlaProductPriceProvider.class) {
			if(plaProductPriceProvider != null)
				return plaProductPriceProvider;
			else {
				plaProductPriceProvider = new PlaProductPriceProvider();
				return plaProductPriceProvider;
			}
		}
	}

	public static AdwordsMetricProviderInterface getAdwordsMetricProvider() throws SQLException, IOException{
		synchronized (AdwordsMetricProvider.class) {
			if(adwordsMetricProviderInterface != null)
				return adwordsMetricProviderInterface;
			else {
				adwordsMetricProviderInterface = new AdwordsMetricProvider(endDate, Conf.CAMPAIGN_SUMMARY_INTEVAL);
				return adwordsMetricProviderInterface;
			}
		}
	}

	public static AdwordsMetricProviderInterface getAdwordsMetricProvider(int interval) throws SQLException, IOException{
		synchronized (AdwordsMetricProvider.class) {
			if(adwordsMetricProviderInterface != null)
				return adwordsMetricProviderInterface;
			else {
				adwordsMetricProviderInterface = new AdwordsMetricProvider(endDate, interval);
				return adwordsMetricProviderInterface;
			}
		}
	}

	public static AvgCpcAgainstMaxCpcRatioProviderInterface getAvgCpcAgainstMaxCpcRatioProviderInterface(){
		synchronized (AvgCpcAgainstMaxCpcRatioProvider.class) {
			if(avgCpcAgainstMaxCpcRatioProviderInterface != null)
				return avgCpcAgainstMaxCpcRatioProviderInterface;
			else {
				avgCpcAgainstMaxCpcRatioProviderInterface = new AvgCpcAgainstMaxCpcRatioProvider();
				return avgCpcAgainstMaxCpcRatioProviderInterface;
			}
		}
	}

	public static CategoryStatItemsProvider getCategoryStatItemsProvider(SiteType siteType, AdwordsChannel channel) throws SQLException{
		String key = getKey(siteType, channel);
		CategoryStatItemsProvider provider = keyCategoryStatItemsProviderMap.get(key);
		if(provider != null)
			return provider;
		synchronized (keyCategoryStatItemsProviderMap) {
			provider = keyCategoryStatItemsProviderMap.get(key);
			if(provider != null)
				return provider;
			else {
				provider = new CategoryStatItemsProvider(siteType, channel, endDate);
				keyCategoryStatItemsProviderMap.put(key, provider);
				return provider;
			}
		}
	}

	public static ProductStatItemsProvider getProductStatItemsProvider(SiteType siteType, AdwordsChannel channel) throws SQLException{
		String key = getKey(siteType, channel);
		ProductStatItemsProvider provider = keyProductStatItemsProviderMap.get(key);
		synchronized (keyProductStatItemsProviderMap) {
			provider = keyProductStatItemsProviderMap.get(key);
			if(provider != null)
				return provider;
			else {
				provider = new ProductStatItemsProvider(siteType, channel, endDate);
				keyProductStatItemsProviderMap.put(key, provider);
				return provider;
			}
		}
	}

	public static PcCrDivideMobileCrProvider getPcCrDivideMobileCrProvider(){
		synchronized (PcCrDivideMobileCrProvider.class) {
			if(pcCrDivideMobileCrProvider != null)
				return pcCrDivideMobileCrProvider;
			else {
				pcCrDivideMobileCrProvider = new PcCrDivideMobileCrProvider();
				return pcCrDivideMobileCrProvider;
			}
		}
	}

	public static RedirectSuccRateProviderInterface getRedirectSuccRateProvider(){
		synchronized (RedirectSuccRateProvider.class) {
			if(redirectSuccRateProviderInterface != null)
				return redirectSuccRateProviderInterface;
			else {
				redirectSuccRateProviderInterface = new RedirectSuccRateProvider();
				return redirectSuccRateProviderInterface;
			}
		}
	}

	public static RepeatRateProviderInterface getRepeatRateProvider(SiteType siteType) throws SQLException, IOException{
		RepeatRateProviderInterface repeatRateProviderInterface = keyRepeatRateProviderInterfaceMap.get(siteType);
		if(repeatRateProviderInterface != null)
			return repeatRateProviderInterface;
		synchronized (keyRepeatRateProviderInterfaceMap) {
			repeatRateProviderInterface = keyRepeatRateProviderInterfaceMap.get(siteType);
			if(repeatRateProviderInterface != null)
				return repeatRateProviderInterface;
			else {
//				repeatRateProviderInterface = new RepeatRateProviderV20160712(siteType);	// 20160712
				repeatRateProviderInterface = new RepeatRateWithWeekProvider(siteType);
				keyRepeatRateProviderInterfaceMap.put(siteType, repeatRateProviderInterface);
				return repeatRateProviderInterface;
			}
		}
	}
	
	public static CurrencyValueFormatterProvider getCurrencyValueFormatterProvider(SiteType siteType) throws SQLException{
		CurrencyValueFormatterProvider currencyValueFormatterProvider = siteTypeCurrencyValueFormatterProviderMap.get(siteType);
		if(currencyValueFormatterProvider != null)
			return currencyValueFormatterProvider;
		synchronized (siteTypeCurrencyValueFormatterProviderMap) {
			currencyValueFormatterProvider = siteTypeCurrencyValueFormatterProviderMap.get(siteType);
			if(currencyValueFormatterProvider != null)
				return currencyValueFormatterProvider;
			else {
				currencyValueFormatterProvider = new CurrencyValueFormatterProvider(siteType);
				siteTypeCurrencyValueFormatterProviderMap.put(siteType, currencyValueFormatterProvider);
				return currencyValueFormatterProvider;
			}
		}
	}
	
	public static DisplayRepeatRateProvider getDisplayRepeatRateProvider(SiteType siteType) throws SQLException{
		DisplayRepeatRateProvider repeatRateProviderInterface = keyDisplayRepeatRateProviderMap.get(siteType);
		if(repeatRateProviderInterface != null)
			return repeatRateProviderInterface;
		synchronized (keyDisplayRepeatRateProviderMap) {
			repeatRateProviderInterface = keyDisplayRepeatRateProviderMap.get(siteType);
			if(repeatRateProviderInterface != null)
				return repeatRateProviderInterface;
			else {
				repeatRateProviderInterface = new DisplayRepeatRateProvider(siteType);
				keyDisplayRepeatRateProviderMap.put(siteType, repeatRateProviderInterface);
				return repeatRateProviderInterface;
			}
		}
	}

	public static BiddingParametersProvider getBiddingParametersProvider(SiteType siteType, AdwordsChannel channel) throws IOException, SQLException{
		String key = getKey(siteType, channel);
		BiddingParametersProvider targetRoiProvider = keyTargetRoiProviderMap.get(key);
		if(targetRoiProvider != null)
			return targetRoiProvider;
		synchronized (keyTargetRoiProviderMap) {
			targetRoiProvider = keyTargetRoiProviderMap.get(key);
			if(targetRoiProvider != null)
				return targetRoiProvider;
			else {
				targetRoiProvider = new BiddingParametersProvider(siteType, channel);
				keyTargetRoiProviderMap.put(key, targetRoiProvider);
				return targetRoiProvider;
			}
		}
	}

	public static ValueAdjustDataProviderInterface getValueAdjustDataProvider(SiteType siteType) throws SQLException{
		ValueAdjustDataProviderInterface valueAdjustDataProviderInterface = keyValueAdjustDataProviderInterfaceMap.get(siteType);
		if(valueAdjustDataProviderInterface != null)
			return valueAdjustDataProviderInterface;
		synchronized (keyValueAdjustDataProviderInterfaceMap) {
			valueAdjustDataProviderInterface = keyValueAdjustDataProviderInterfaceMap.get(siteType);
			if(valueAdjustDataProviderInterface != null)
				return valueAdjustDataProviderInterface;
			else {
				valueAdjustDataProviderInterface = new ValueAdjustDataProviderV20160712(siteType);	// 20160712
				keyValueAdjustDataProviderInterfaceMap.put(siteType, valueAdjustDataProviderInterface);
				return valueAdjustDataProviderInterface;
			}
		}
	}
	public static IntervalValueAdjustDataProvider getIntervalValueAdjustDataProvider(SiteType siteType) throws SQLException{
		IntervalValueAdjustDataProvider valueAdjustDataProviderInterface = keyIntervalValueAdjustDataProviderMap.get(siteType);
		if(valueAdjustDataProviderInterface != null)
			return valueAdjustDataProviderInterface;
		synchronized (keyIntervalValueAdjustDataProviderMap) {
			valueAdjustDataProviderInterface = keyIntervalValueAdjustDataProviderMap.get(siteType);
			if(valueAdjustDataProviderInterface != null)
				return valueAdjustDataProviderInterface;
			else {
				valueAdjustDataProviderInterface = new IntervalValueAdjustDataProvider(siteType);
				keyIntervalValueAdjustDataProviderMap.put(siteType, valueAdjustDataProviderInterface);
				return valueAdjustDataProviderInterface;
			}
		}
	}
	
	public static ArpuTrendHistoryBiProviderInterface getArpuTrendHistoryBiProvider(SiteType siteType, AdwordsChannel channel) throws SQLException, IOException{
		String key = getKey(siteType, channel);
		ArpuTrendHistoryBiProviderInterface arpuTrendHistoryBiProviderInterface = keyArpuTrendHistoryBiProviderInterfaceMap.get(key);
		if(arpuTrendHistoryBiProviderInterface != null)
			return arpuTrendHistoryBiProviderInterface;
		synchronized (keyArpuTrendHistoryBiProviderInterfaceMap) {
			arpuTrendHistoryBiProviderInterface = keyArpuTrendHistoryBiProviderInterfaceMap.get(key);
			if(arpuTrendHistoryBiProviderInterface != null)
				return arpuTrendHistoryBiProviderInterface;
			else {
				arpuTrendHistoryBiProviderInterface = new ArpuTrendHistoryBiProvider(siteType, channel, endDate);
				keyArpuTrendHistoryBiProviderInterfaceMap.put(key, arpuTrendHistoryBiProviderInterface);
				return arpuTrendHistoryBiProviderInterface;
			}
		}
	}

	public static DelayRateProviderInterface getDelayRateProvider(SiteType siteType) throws NumberFormatException, IOException, SQLException{
		DelayRateProviderInterface delayRateProviderInterface = keyDelayRateProviderInterfaceMap.get(siteType);
		if(delayRateProviderInterface != null)
			return delayRateProviderInterface;
		synchronized (keyDelayRateProviderInterfaceMap) {
			delayRateProviderInterface = keyDelayRateProviderInterfaceMap.get(siteType);
			if(delayRateProviderInterface != null)
				return delayRateProviderInterface;
			else {
//				delayRateProviderInterface = new DelayRateProviderV160712(siteType);	// 20160712
			  /**
			   * 使用BI计算的delay rate
			   * 修改者：赵宏宏
			   * 需求来源：时斌
			   */
				delayRateProviderInterface = new DelayRateProviderBi(siteType);    // 20160712
				keyDelayRateProviderInterfaceMap.put(siteType, delayRateProviderInterface);
				return delayRateProviderInterface;
			}
		}
	}
	
	public static FacebookDelayRateProviderBi getFacebookDelayRateProvider(SiteType siteType) throws NumberFormatException, IOException, SQLException{
		FacebookDelayRateProviderBi delayRateProviderInterface = keyFacebookDelayRateProviderInterfaceMap.get(siteType);
		if(delayRateProviderInterface != null)
			return delayRateProviderInterface;
		synchronized (keyFacebookDelayRateProviderInterfaceMap) {
			delayRateProviderInterface = keyFacebookDelayRateProviderInterfaceMap.get(siteType);
			if(delayRateProviderInterface != null)
				return delayRateProviderInterface;
			else {
				delayRateProviderInterface = new FacebookDelayRateProviderBi(siteType);
				keyFacebookDelayRateProviderInterfaceMap.put(siteType, delayRateProviderInterface);
				return delayRateProviderInterface;
			}
		}
	}
	public static DisplayDelayRateProviderBi getDisplayDelayRateProvider(SiteType siteType) throws NumberFormatException, IOException, SQLException{
		DisplayDelayRateProviderBi delayRateProvider = keyDisplayDelayRateProviderMap.get(siteType);
		if(delayRateProvider != null)
			return delayRateProvider;
		synchronized (keyDisplayDelayRateProviderMap) {
			delayRateProvider = keyDisplayDelayRateProviderMap.get(siteType);
			if(delayRateProvider != null)
				return delayRateProvider;
			else {
				delayRateProvider = new DisplayDelayRateProviderBi(siteType); 
				keyDisplayDelayRateProviderMap.put(siteType, delayRateProvider);
				return delayRateProvider;
			}
		}
	}
	
	public static DelayRateProviderInterface getAppInstallDelayRateProvider(SiteType siteType) throws NumberFormatException, IOException, SQLException{
		DelayRateProviderInterface delayRateProviderInterface = keyFacebookAppInstallDelayRateProviderInterfaceMap.get(siteType);
		if(delayRateProviderInterface != null)
			return delayRateProviderInterface;
		synchronized (keyFacebookAppInstallDelayRateProviderInterfaceMap) {
			delayRateProviderInterface = keyFacebookAppInstallDelayRateProviderInterfaceMap.get(siteType);
			if(delayRateProviderInterface != null)
				return delayRateProviderInterface;
			else {
				delayRateProviderInterface = new AppInstallDelayRateProviderBi(siteType);
				keyFacebookAppInstallDelayRateProviderInterfaceMap.put(siteType, delayRateProviderInterface);
				return delayRateProviderInterface;
			}
		}
	}
	public static AccountDelayRateProviderBi getAccountDelayRateProvider(SiteType siteType) throws NumberFormatException, IOException, SQLException{
		AccountDelayRateProviderBi delayRateProvider = keyAccountDelayRateProviderMap.get(siteType);
		if(delayRateProvider != null)
			return delayRateProvider;
		synchronized (keyAccountDelayRateProviderMap) {
			delayRateProvider = keyAccountDelayRateProviderMap.get(siteType);
			if(delayRateProvider != null)
				return delayRateProvider;
			else {
				delayRateProvider = new AccountDelayRateProviderBi(siteType); 
				keyAccountDelayRateProviderMap.put(siteType, delayRateProvider);
				return delayRateProvider;
			}
		}
	}
	
	public static AddToCart2OrderConversionProviderInterface getAddToCart2OrderConversionProvider(SiteType siteType) throws NumberFormatException, IOException, SQLException{
		AddToCart2OrderConversionProviderInterface addToCart2OrderConversionProvider = keyAddToCart2OrderConversionProviderMap.get(siteType);
		if(addToCart2OrderConversionProvider != null)
			return addToCart2OrderConversionProvider;
		synchronized (keyAddToCart2OrderConversionProviderMap) {
			addToCart2OrderConversionProvider = keyAddToCart2OrderConversionProviderMap.get(siteType);
			if(addToCart2OrderConversionProvider != null)
				return addToCart2OrderConversionProvider;
			else {
				addToCart2OrderConversionProvider = new AddToCart2OrderConversionProvider(siteType); 
				keyAddToCart2OrderConversionProviderMap.put(siteType, addToCart2OrderConversionProvider);
				return addToCart2OrderConversionProvider;
			}
		}
	}

	public static CrPredictorInterface getCrPredictor(SiteType siteType) throws IOException, SQLException{
		CrPredictorInterface crPredictorInterface = keyCrPredictorInterfaceMap.get(siteType);
		if(crPredictorInterface != null)
			return crPredictorInterface;
		synchronized (keyCrPredictorInterfaceMap) {
			crPredictorInterface = keyCrPredictorInterfaceMap.get(siteType);
			if(crPredictorInterface != null)
				return crPredictorInterface;
			else {
				crPredictorInterface = new S3CrPredictor(siteType);
				keyCrPredictorInterfaceMap.put(siteType, crPredictorInterface);
				return crPredictorInterface;
			}
		}
	}

	public static DevicesMetricProvider getDevicesMetricProvider(){
		synchronized (DevicesMetricProvider.class) {
			if(devicesMetricProvider != null)
				return devicesMetricProvider;
			else {
				devicesMetricProvider = new DevicesMetricProvider(endDate);
				return devicesMetricProvider;
			}
		}
	}

	public static NewItemIdsProvider getNewItemIdsProvider(SiteType siteType) throws IOException{
		synchronized (NewItemIdsProvider.class) {
			if(newItemIdsProvider != null)
				return newItemIdsProvider;
			else {
				newItemIdsProvider = new NewItemIdsProvider(siteType);
				return newItemIdsProvider;
			}
		}
	}

	public static PlaCategoryDataProviderInterface getPlaCategoryDataProviderInterface(SiteType siteType) throws IOException, SQLException{
		PlaCategoryDataProviderInterface plaCategoryDataProviderInterface = keyPlaCategoryDataProviderInterfaceMap.get(siteType);
		if(plaCategoryDataProviderInterface != null)
			return plaCategoryDataProviderInterface;
		synchronized (keyPlaCategoryDataProviderInterfaceMap) {
			plaCategoryDataProviderInterface = keyPlaCategoryDataProviderInterfaceMap.get(siteType);
			if(plaCategoryDataProviderInterface != null)
				return plaCategoryDataProviderInterface;
			else {
				plaCategoryDataProviderInterface = new PlaCategoryDataProvider(siteType, endDate);
				keyPlaCategoryDataProviderInterfaceMap.put(siteType, plaCategoryDataProviderInterface);
				return plaCategoryDataProviderInterface;
			}
		}
	}

	public static SearchCategoryDataProviderInterface getSearchCategoryDataProviderInterface(SiteType siteType) throws IOException, SQLException {
		SearchCategoryDataProviderInterface searchCategoryDataProviderInterface = keySearchCategoryDataProviderInterface.get(siteType);
		if (searchCategoryDataProviderInterface != null)
			return searchCategoryDataProviderInterface;
		synchronized (keySearchCategoryDataProviderInterface) {
			searchCategoryDataProviderInterface = keySearchCategoryDataProviderInterface.get(siteType);
			if (searchCategoryDataProviderInterface != null)
				return searchCategoryDataProviderInterface;
			else {
				searchCategoryDataProviderInterface = new SearchCategoryDataProvider(siteType, endDate);
				keySearchCategoryDataProviderInterface.put(siteType, searchCategoryDataProviderInterface);
				return searchCategoryDataProviderInterface;
			}
		}
	}
	
	public static CampaignDataProvider getCampaignDataProviderInterface(SiteType siteType, AdwordsChannel channel) throws IOException, SQLException {
		CampaignDataProvider campaignDataProvider = keyCampaignDataProviderInterface.get(siteType + "\t" + channel);
		if (campaignDataProvider != null)
			return campaignDataProvider;
		synchronized (keyCampaignDataProviderInterface) {
			campaignDataProvider = keyCampaignDataProviderInterface.get(siteType + "\t" + channel);
			if (campaignDataProvider != null)
				return campaignDataProvider;
			else {
				campaignDataProvider = new CampaignDataProvider(siteType, channel, endDate);
				keyCampaignDataProviderInterface.put(siteType + "\t" + channel, campaignDataProvider);
				return campaignDataProvider;
			}
		}
	}
	
	public static DisplayCategoryDataProviderInterface getDisplayCategoryDataProviderInterface(SiteType siteType) throws IOException, SQLException {
		DisplayCategoryDataProviderInterface displayCategoryDataProviderInterface = keyDisplayCategoryDataProviderInterface.get(siteType);
		if (displayCategoryDataProviderInterface != null)
			return displayCategoryDataProviderInterface;
		synchronized (keyDisplayCategoryDataProviderInterface) {
			displayCategoryDataProviderInterface = keyDisplayCategoryDataProviderInterface.get(siteType);
			if (displayCategoryDataProviderInterface != null)
				return displayCategoryDataProviderInterface;
			else {
				displayCategoryDataProviderInterface = new DisplayCategoryDataProvider(siteType, endDate);
				keyDisplayCategoryDataProviderInterface.put(siteType, displayCategoryDataProviderInterface);
				return displayCategoryDataProviderInterface;
			}
		}
	}

	// added on 201608024
	public static CategoryConversionGroupDataProviderInterface getCategoryConversionGroupDataProviderInterface(SiteType siteType) throws IOException, SQLException {
		synchronized (keyCategoryConversionGroupDataProviderInterface) {
			CategoryConversionGroupDataProviderInterface categoryConversionGroupDataProviderInterface = keyCategoryConversionGroupDataProviderInterface.get(siteType);
			if (categoryConversionGroupDataProviderInterface != null)
				return categoryConversionGroupDataProviderInterface;
			else {
				categoryConversionGroupDataProviderInterface = new CategoryConversionGroupDataProvider(siteType, endDate);
				keyCategoryConversionGroupDataProviderInterface.put(siteType, categoryConversionGroupDataProviderInterface);
				return categoryConversionGroupDataProviderInterface;
			}
		}
	}

	public static BiYesterdayCpcProvider getBiYesterdayCpcProvider(Date endDate) throws SQLException {
		String key = DateHelper.getShortDateString(endDate);
		BiYesterdayCpcProvider biYesterdayCpcProvider = biYesterdayCpcProviderMap.get(key);
		if(biYesterdayCpcProvider != null)
			return biYesterdayCpcProvider;
		synchronized (biYesterdayCpcProviderMap) {
			biYesterdayCpcProvider = biYesterdayCpcProviderMap.get(key);
			if(biYesterdayCpcProvider != null)
				return biYesterdayCpcProvider;
			else{
				biYesterdayCpcProvider = new BiYesterdayCpcProvider(endDate);
				biYesterdayCpcProviderMap.put(key, biYesterdayCpcProvider);
				return biYesterdayCpcProvider;
			}
		}
	}
	
	public static YesterdayCostChangeProvider getYesterdayCostChangeProvider() throws SQLException {
		String key = DateHelper.getShortDateString(endDate);
		YesterdayCostChangeProvider yesterdayCostChangeProvider = yesterdayCostChangeProviderProviderMap.get(key);
		if(yesterdayCostChangeProvider != null)
			return yesterdayCostChangeProvider;
		synchronized (yesterdayCostChangeProviderProviderMap) {
			yesterdayCostChangeProvider = yesterdayCostChangeProviderProviderMap.get(key);
			if(yesterdayCostChangeProvider != null)
				return yesterdayCostChangeProvider;
			else{
				yesterdayCostChangeProvider = new YesterdayCostChangeProvider(endDate);
				yesterdayCostChangeProviderProviderMap.put(key, yesterdayCostChangeProvider);
				return yesterdayCostChangeProvider;
			}
		}
	}
	
	public static AvgCpcAgainstMaxCpcRatioHistoryProvider getAvgCpcAgainstMaxCpcRatioHistoryProvider(Date endDate, AdwordsChannel channel) {
		String key = DateHelper.getShortDateString(endDate) + "\t" + channel;
		AvgCpcAgainstMaxCpcRatioHistoryProvider avgCpcAgainstMaxCpcRatioHistoryProvider = avgCpcAgainstMaxCpcRatioHistoryProviderMap.get(key);
		if(avgCpcAgainstMaxCpcRatioHistoryProvider != null)
			return avgCpcAgainstMaxCpcRatioHistoryProvider;
		synchronized (avgCpcAgainstMaxCpcRatioHistoryProviderMap) {
			avgCpcAgainstMaxCpcRatioHistoryProvider = avgCpcAgainstMaxCpcRatioHistoryProviderMap.get(key);
			if(avgCpcAgainstMaxCpcRatioHistoryProvider != null)
				return avgCpcAgainstMaxCpcRatioHistoryProvider;
			else{
				avgCpcAgainstMaxCpcRatioHistoryProvider = new AvgCpcAgainstMaxCpcRatioHistoryProvider(endDate, channel);
				avgCpcAgainstMaxCpcRatioHistoryProviderMap.put(key, avgCpcAgainstMaxCpcRatioHistoryProvider);
				return avgCpcAgainstMaxCpcRatioHistoryProvider;
			}
		}
	}
	
	public static TargetCpcContainer getTargetCpcContainer(){
	  synchronized (TargetCpcContainer.class) {
        if(targetCpcContainer != null)
            return targetCpcContainer;
        else {
          targetCpcContainer = new TargetCpcContainer();
          return targetCpcContainer;
        }
    }
	}
	// private methods
	private static String getKey(SiteType siteType, AdwordsChannel channel){
		return siteType.getSiteCode() + "\t" + channel.getChannelId();
	}

	// main for test
	public static void main(String[] args) {
		ComponentFactory.setEndDate(DateHelper.getShortDate("2016-04-01"));
		System.out.println(ComponentFactory.getRedirectSuccRateProvider().getRediretSuccRate(SiteType.litb, LanguageType.en));
	}
}
