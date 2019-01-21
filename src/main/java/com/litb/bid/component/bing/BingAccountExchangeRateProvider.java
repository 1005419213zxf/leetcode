package com.litb.bid.component.bing;


import com.litb.basic.db.DBHelper;
import com.litb.basic.db.DBPool;
import com.litb.basic.enums.Currency;
import com.litb.basic.util.DateHelper;
import com.litb.bing.lib.obj.BingAccountManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BingAccountExchangeRateProvider {
    private BingAccountManager bingAccountManager;
    private Map<Currency, Double> currencyExchangeRateMap = new HashMap<Currency, Double>();



    public BingAccountExchangeRateProvider(Date endDate) throws SQLException {
        System.out.println("init account exchangeRate provider...");
        bingAccountManager = new BingAccountManager();
      //  Map<Long, BingAccountBasicInfo> bingAccountBasicInfoMap = bingAccountManager.getBingAccountBasicInfoMap();

        DBHelper dbHelper = DBPool.getCnBiMasterDbHelperWithMarketingRole();
        String sql = "select code,value from ods_360_curr "
                + "where start_date<=to_date('" + DateHelper.getShortDateString(endDate) + "','yyyy-mm-dd') "
                + "and end_date>to_date('" + DateHelper.getShortDateString(endDate) + "','yyyy-mm-dd')";
        ResultSet resultSet = dbHelper.executeQuery(sql);
        while (resultSet.next()) {
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




    /**
     * Multiply to account conversion value to make bid and conversion value in same currency.
     * @param accountId
     * @return
     */
    public double getExchangeRate(long accountId){

        Currency currency = bingAccountManager.getAccountCurrency(accountId);
        if(currencyExchangeRateMap.containsKey(currency))
            return currencyExchangeRateMap.get(currency);
        throw new RuntimeException("not found exchange rate for currency:" + currency);
    }

    public Currency getCurrency(long accountId){
        Currency currency = bingAccountManager.getAccountCurrency(accountId);
        return currency;
    }
}
