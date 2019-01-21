package com.litb.bid.component.adw.bi;


import com.litb.adw.lib.enums.LitbAdChannel;
import com.litb.basic.db.DBHelper;
import com.litb.basic.db.DBPool;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;
import com.litb.bid.component.adw.delay.DelayRateProvider2;
import com.litb.bid.component.adw.RepeatRateInfo;
import com.litb.bid.component.adw.RepeatRateProvider;
import com.litb.bid.component.adw.ValueAdjustRateInfo;
import com.litb.bid.component.adw.ValueAdjustDataProvider;
import com.litb.bid.component.adw.delay.DelayRateInfo;

import java.io.BufferedWriter;
import java.sql.ResultSet;

public class DailyStat {
	private static final SiteType SITE_TYPE = SiteType.litb;
	private static final String TAREGT_DATE_STRING = "2016-03-28";
	
	static String getOutputFilePath(SiteType siteType, String dateString){
		return "D:\\workplace\\" + dateString + "_" + siteType + ".sql";
	}
	
	public static void main(String[] args) throws Exception {
		// prepare components
		ValueAdjustDataProvider valueAdjustDataProvider = new ValueAdjustDataProvider(SITE_TYPE);
		RepeatRateProvider repeatRateProvider = new RepeatRateProvider(SITE_TYPE);
		DelayRateProvider2 delayRateProvider = new DelayRateProvider2(SITE_TYPE);
//		RedirectSuccRateProvider redirectSuccRateProvider = new RedirectSuccRateProvider();
		
		String outputFilePath = getOutputFilePath(SITE_TYPE, TAREGT_DATE_STRING);
		BufferedWriter bw = FileHelper.writeFile(outputFilePath);
		
		// scan all rows
		DBHelper dbHelper = DBPool.getCnBiMasterDbHelperWithMarketingRole();
		String sql = "select adwords_date, merchant_id, languages_id, categories_id, channel_id, currency_code, "
				+ "cost, revenue "
				+ "from BISD.ODS_AD_MARKET_COST_OC where merchant_id = " + SITE_TYPE.getSiteCode() + " "
				+ "and adwords_date = to_date('" + TAREGT_DATE_STRING + "', 'yyyy-mm-dd') "
				+ "and channel_id in (7, 8, 81, 151, 127)";
		System.out.println(sql);
		ResultSet resultSet = dbHelper.executeQuery(sql);
		double sumCost = 0, sumRevenueOriginal = 0, sumRevenueLtv = 0;
		while(resultSet.next()){
			LanguageType languageType = LanguageType.getLanguageType(resultSet.getInt(3));
			int originalCid = resultSet.getInt(4);
			int cid = originalCid;
			if(cid <= 0)
				cid = -1;
			LitbAdChannel channel = LitbAdChannel.getLitbAdChannel(resultSet.getInt(5));
			String currencyCode = resultSet.getString(6);
			double cost = resultSet.getDouble(7);
			double revenue = resultSet.getDouble(8);
			
			// component
			DelayRateInfo delayRateInfo = delayRateProvider.getDelayRate(channel, SITE_TYPE, languageType, cid, 1, 1, 30);
			double delayRate = (delayRateInfo != null ? delayRateInfo.getDelayRate() : 1);
			RepeatRateInfo repeatRateInfo = repeatRateProvider.getRepeatRate(languageType, cid);
			double repeatRate = (repeatRateInfo != null ? repeatRateInfo.getRepeatRate() : 0.0);
			ValueAdjustRateInfo valueAdjustRateInfo = valueAdjustDataProvider.getValueAdjustRate(languageType, cid);
			double valueAdjRate = (valueAdjustRateInfo != null ? valueAdjustRateInfo.getOfflineAdjRatio() : 1.0);
//			RedirectSuccRateInfo redirectSuccRateInfo = redirectSuccRateProvider.getRediretSuccRate(SITE_TYPE, languageType);
//			double redirectSuccRate = (redirectSuccRateInfo != null ? redirectSuccRateInfo.getRedirectSuccRate() : 1);
			// compute
			double ltRevenue = revenue / 3.3;
			ltRevenue *= delayRate;
			ltRevenue *= (1 + repeatRate);
			ltRevenue *= valueAdjRate;
//			ltRevenue /= redirectSuccRate;
			
			// sum
			sumCost += cost;
			sumRevenueOriginal += revenue;
			sumRevenueLtv += ltRevenue;
			
			// output
			String updateSql = "update BISD.ODS_AD_MARKET_COST_OC set lifetime_value = " + ltRevenue + " "
					+ "where adwords_date = to_date('" + TAREGT_DATE_STRING + "', 'yyyy-mm-dd') "
					+ "and merchant_id=" + SITE_TYPE.getSiteCode() + " "
					+ "and languages_id=" + languageType.getLanguageId() + " "
					+ "and categories_id=" + originalCid + " "
					+ "and channel_id=" + channel.getChannelId() + " "
					+ "and currency_code=" + "'" + currencyCode + "'";
			bw.append(updateSql + "\n");
		}
		bw.close();
		System.out.println("Done.");
		
		double accountRoi = sumRevenueOriginal / sumCost;
		double lifetimeRoi = sumRevenueLtv / sumCost;
		
		System.out.println("total cost: " + sumCost);
		System.out.println("total original revenue: " + sumRevenueOriginal);
		System.out.println("total lifetime revenue: " + sumRevenueLtv);
		System.out.println("account ROI: " + accountRoi);
		System.out.println("lifetime ROI: " + lifetimeRoi);
	}
}
