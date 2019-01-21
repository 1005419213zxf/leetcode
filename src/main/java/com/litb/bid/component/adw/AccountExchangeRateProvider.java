package com.litb.bid.component.adw;

import com.litb.adw.lib.exception.AdwordsApiException;
import com.litb.adw.lib.exception.AdwordsRemoteException;
import com.litb.adw.lib.exception.AdwordsValidationException;
import com.litb.adw.lib.obj.AdwordsAccountManager;
import com.litb.basic.db.DBHelper;
import com.litb.basic.db.DBPool;
import com.litb.basic.enums.Currency;
import com.litb.basic.util.DateHelper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AccountExchangeRateProvider {
//	private static final SiteType SITE_TYPE = SiteType.litb;
	
	private AdwordsAccountManager adwordsAccountManager;
	private Map<Currency, Double> currencyExchangeRateMap = new HashMap<Currency, Double>();
//	private double usdAgainstEuroExchangeRate;
	
	// constructor
	public AccountExchangeRateProvider(Date endDate) throws AdwordsValidationException, AdwordsApiException, AdwordsRemoteException, SQLException {
		System.out.println("init account exchangeRate provider...");
		adwordsAccountManager = new AdwordsAccountManager();
//		DBHelper dbHelper = DBPool.getUsMcMainSlaveDbHelper(SITE_TYPE);
//		String sql = "SELECT CODE,decimal_places,VALUE FROM mc_currencies WHERE CODE = 'EUR'";
//		ResultSet resultSet = dbHelper.executeQuery(sql);
//		if(resultSet.next()){
//			usdAgainstEuroExchangeRate = resultSet.getDouble(3);
//		}
//		dbHelper.close();
		DBHelper dbHelper = DBPool.getCnBiMasterDbHelperWithMarketingRole();
		String sql = "select code,value from ods_360_curr "
//				+ "where code='EUR' "
				+ "where start_date<=to_date('"+DateHelper.getShortDateString(endDate)+"','yyyy-mm-dd') "
				+ "and end_date>to_date('"+DateHelper.getShortDateString(endDate)+"','yyyy-mm-dd')";
		ResultSet resultSet = dbHelper.executeQuery(sql);
		while(resultSet.next()){
			try {
				Currency currency = Currency.valueOf(resultSet.getString(1).toLowerCase());
				double exchangeRate = resultSet.getDouble(2);
				currencyExchangeRateMap.put(currency, exchangeRate);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		dbHelper.close();
	}

	// public methods
	/**
	 * Multiply to account conversion value to make bid and conversion value in same currency.
	 * @param accountId
	 * @return
	 */
	public double getExchangeRate(long accountId){
		Currency currency = adwordsAccountManager.getAccountCurrency(accountId);
//		if(currency == Currency.eur)
//			return usdAgainstEuroExchangeRate;
//		for(Entry<Currency, Double> entry : currencyExchangeRateMap.entrySet())
//			System.out.println(entry.getKey() + "\t" + entry.getValue());
		if(currencyExchangeRateMap.containsKey(currency))
			return currencyExchangeRateMap.get(currency);
		throw new RuntimeException("not found exchange rate for currency:" + currency);
	}
	
	public Currency getCurrency(long accountId){
		Currency currency = adwordsAccountManager.getAccountCurrency(accountId);
		return currency;
	}
	
	public static void main(String[] args) throws AdwordsValidationException, AdwordsApiException, AdwordsRemoteException, SQLException {
		AccountExchangeRateProvider provider = new AccountExchangeRateProvider(new Date());
		System.out.println(provider.getExchangeRate(Long.valueOf(args[0])));
		System.out.println(provider.getCurrency(Long.valueOf(args[0])));
	}
}
