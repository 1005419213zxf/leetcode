package com.litb.bid.component.adw;

import com.litb.bid.util.CpTreeFactory;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.db.DBHelper;
import com.litb.basic.db.DBPool;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValueAdjustDataProvider implements ValueAdjustDataProviderInterface {
	private static final double MAX_ADJ_RATIO = 5;
	private static final double MIN_TOTAL_ADJ_RATIO = 0.01;
	private static final int SUM_CID = -1;
	
	private SiteType targetSiteType;
	private CPTree cpTree;

	private Map<String, ValueAdjustRateInfo> keyRatioMap = new HashMap<String, ValueAdjustRateInfo>();
	
	// constructor
	public ValueAdjustDataProvider(SiteType siteType) throws SQLException {
		System.out.println("initializing ValueAdjustDataProvider...");
		this.targetSiteType = siteType;
		this.cpTree = CpTreeFactory.getCategoryCpTree(siteType);
		
		init();
	}
	
	// public methods
	public ValueAdjustRateInfo getValueAdjustRate(LanguageType languageType, int cid) {
		List<Integer> pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
		pcidList.add(SUM_CID);
		for(int pcid : pcidList){
			String key = getKey(languageType, pcid);
			ValueAdjustRateInfo info = keyRatioMap.get(key);
			if(info != null)
				return info;
		}
		for(int pcid : pcidList){
			String key = getKey(null, pcid);
			ValueAdjustRateInfo info = keyRatioMap.get(key);
			if(info != null)
				return info;
		}
		return null;
	}
	
	// private methods
	private static String getKey(LanguageType languageType, int cid){
		return (languageType == null ? "-1" : languageType.getLanguageId()) + "\t" + (cid <= 0 ? SUM_CID : cid);
	}
	
	private void init() throws SQLException{
		System.out.println("initializing ValueAdjustDataProvider...");
		DBHelper dbHelper = DBPool.getCnBiMasterDbHelper();
		Map<String, Stat> sumKeyStatMap = new HashMap<String, Stat>();
		
		// table 1 
		String sql = "select merchant_id, languages_id, cms_catid1, profit, profit_fix, off_total_sales, off_online_sales "
				+ "from BISD.RD_ADWORDS_FIX_RATE1 t where merchant_id = " + targetSiteType.getSiteCode();
		System.out.println(sql);
		init(dbHelper, sql, sumKeyStatMap);
		// table 2
		sql = "select merchant_id, languages_id, cms_catid2, profit, profit_fix, off_succ_total_sales, off_succ_online_sales "
				+ "from BISD.RD_ADWORDS_FIX_RATE2 t where merchant_id = " + targetSiteType.getSiteCode();
		System.out.println(sql);
		init(dbHelper, sql, sumKeyStatMap);
		dbHelper.close();
		System.out.println("original data size: " + keyRatioMap.size());
		
		// sum data
		for(Stat stat : sumKeyStatMap.values()){
			double[] adjRatioArr = getAdjRate(stat.profitAdwords, stat.profitActual, stat.salesTotal, stat.salesOnline);
			
			// save to map
			ValueAdjustRateInfo info = new ValueAdjustRateInfo();
			info.setSiteType(targetSiteType);
			info.setLangType(stat.languageType);
			info.setCid(stat.cid);
			info.setValueAdjRatio(adjRatioArr[0]);
			info.setOfflineAdjRatio(adjRatioArr[1]);
			info.setTotalAdjRatio(adjRatioArr[2]);
			
			String sumKey = getKey(stat.languageType, stat.cid);
			keyRatioMap.put(sumKey, info);
		}
		System.out.println("finish, size: " + keyRatioMap.size());
	}
	
	private void init(DBHelper dbHelper, String sql, Map<String, Stat> sumKeyStatMap) throws SQLException{
		ResultSet resultSet = dbHelper.executeQuery(sql);
		while(resultSet.next()){
			SiteType siteType = SiteType.getSiteType(resultSet.getInt(1));
			LanguageType languageType = LanguageType.getLanguageType(resultSet.getInt(2));
			int cid = resultSet.getInt(3);
			double profitAdwords = resultSet.getDouble(4);
			double profitActual = resultSet.getDouble(5);
			double salesTotal = resultSet.getDouble(6);
			double salesOnline = resultSet.getDouble(7);
			
			double[] adjRatioArr = getAdjRate(profitAdwords, profitActual, salesTotal, salesOnline);
			
			// save to map
			ValueAdjustRateInfo info = new ValueAdjustRateInfo();
			info.setSiteType(siteType);
			info.setLangType(languageType);
			info.setCid(cid);
			info.setValueAdjRatio(adjRatioArr[0]);
			info.setOfflineAdjRatio(adjRatioArr[1]);
			info.setTotalAdjRatio(adjRatioArr[2]);
			
			String key = getKey(languageType, cid);
			keyRatioMap.put(key, info);
			
			// sum
			List<Integer> pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
			pcidList.add(SUM_CID);
			for(int pcid : pcidList){
				String sumKey = getKey(null, pcid);
				Stat stat = sumKeyStatMap.get(sumKey);
				if(stat == null){
					stat = new Stat();
					stat.languageType = null;
					stat.cid = cid;
					sumKeyStatMap.put(sumKey, stat);
				}
				stat.profitAdwords += profitAdwords;
				stat.profitActual += profitActual;
				stat.salesTotal += salesTotal;
				stat.salesOnline += salesOnline;
			}
		}
	}
	
	private static double[] getAdjRate(double profitAdwords, double profitActual, double salesTotal, double salesOnline){
		double valueAdjRate = (profitAdwords == 0 ? 1 : (profitActual / profitAdwords));
		if(valueAdjRate > MAX_ADJ_RATIO)
			valueAdjRate = MAX_ADJ_RATIO;
		else if(valueAdjRate < MIN_TOTAL_ADJ_RATIO)
			valueAdjRate = MIN_TOTAL_ADJ_RATIO;
		
		double offlineAdjRate = (salesOnline == 0 ? 1 : (salesTotal / salesOnline));
		if(offlineAdjRate > MAX_ADJ_RATIO)
			offlineAdjRate = MAX_ADJ_RATIO;
		else if(offlineAdjRate < 1)
			offlineAdjRate = 1;
		
		double totalAdjRate = valueAdjRate * offlineAdjRate;
		if(totalAdjRate > MAX_ADJ_RATIO)
			totalAdjRate = MAX_ADJ_RATIO;
		else if(totalAdjRate < MIN_TOTAL_ADJ_RATIO)
			totalAdjRate = MIN_TOTAL_ADJ_RATIO;
		
		return new double[] {valueAdjRate, offlineAdjRate, totalAdjRate};
	}
	
	// private class
	private static class Stat{
		private LanguageType languageType;
		private int cid;
		
		private double profitAdwords;
		private double profitActual;
		private double salesTotal;
		private double salesOnline;
	}
	
	// main for test
	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		ValueAdjustDataProvider provider = new ValueAdjustDataProvider(SiteType.litb);
		System.out.println(provider.getValueAdjustRate(LanguageType.en, -1));
		System.out.println(provider.getValueAdjustRate(LanguageType.de, 0));
	}
}
