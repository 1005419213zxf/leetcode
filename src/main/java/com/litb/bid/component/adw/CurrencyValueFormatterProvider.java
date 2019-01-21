package com.litb.bid.component.adw;

import com.litb.basic.db.DBHelper;
import com.litb.basic.db.DBPool;
import com.litb.basic.enums.Currency;
import com.litb.basic.enums.SiteType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;

public class CurrencyValueFormatterProvider {
	private HashMap<Currency, String> currencyValueTableMap = new HashMap<Currency, String>();
	private HashMap<Currency, DecimalFormat> currencyDecimalFormatCacheMap = new HashMap<Currency, DecimalFormat>();
	
	// constructor
	public CurrencyValueFormatterProvider(SiteType siteType) throws SQLException{
		System.out.println("init CurrencyValueFormatterProvider...");
		DBHelper dbHelper = DBPool.getUsMcMainSlaveDbHelper(siteType);
	    String sql = "select code,decimal_places,value from mc_currencies";
	    ResultSet resultSet = dbHelper.executeQuery(sql);
	    while (resultSet.next()) {
	      try {
	    	  currencyValueTableMap.put(Currency.valueOf(resultSet.getString(1).toLowerCase()),
	    	          resultSet.getString(2));
		} catch (Exception e) {
			e.printStackTrace();
		}
	    }
	    dbHelper.close();
	}

	// public methods
	public DecimalFormat getBidDecimalFormat(Currency currency){
		if(currencyDecimalFormatCacheMap.containsKey(currency))
			return currencyDecimalFormatCacheMap.get(currency);
		String dcStr = currencyValueTableMap.get(currency);
        int dec = Integer.valueOf(dcStr);
        String format = "##0";
        if (dec > 0)
          format = format + ".";
        for (int i = 0; i < dec; i++)
          format = format + "0";
        DecimalFormat df = new DecimalFormat(format);
        currencyDecimalFormatCacheMap.put(currency, df);
        return df;
	}
	
	public double getMinCurrencyValue(Currency currency){
		String dcStr = currencyValueTableMap.get(currency);
        int dec = Integer.valueOf(dcStr);
        return 1 / Math.pow(10, dec);
	}
	
	public static void main(String[] args) throws SQLException {
		CurrencyValueFormatterProvider provider = new CurrencyValueFormatterProvider(SiteType.litb);
		System.out.println(provider.getMinCurrencyValue(Currency.usd));
		System.out.println(provider.getMinCurrencyValue(Currency.jpy));
		System.out.println(provider.getBidDecimalFormat(Currency.usd).toPattern());
		System.out.println(provider.getBidDecimalFormat(Currency.jpy).toPattern());
	}
}
