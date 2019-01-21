package com.litb.bid.component.adw;

import com.litb.bid.util.CpTreeFactory;
import com.litb.adw.lib.enums.AdwordsCountry;
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

public class RepeatRateProvider implements RepeatRateProviderInterface {
	private static final double MAX_REPEAT_RATE = 3.0;
	private static final double MIN_REPEAT_RATE = 0.0;
	private static final int SUM_CID = -1;
	
	private SiteType siteType;
	private CPTree cpTree;
	
	private HashMap<String, RepeatRateInfo> keyRateMap = new HashMap<String, RepeatRateInfo>();
	
	// constructor
	public RepeatRateProvider(SiteType siteType) throws SQLException {
		this.siteType = siteType;
		this.cpTree = CpTreeFactory.getCategoryCpTree(siteType);
		init();
	}
	
	// public methods
	public RepeatRateInfo getRepeatRate(LanguageType languageType, int cid) {
		List<Integer> pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
		pcidList.add(SUM_CID);
		for(int pcid : pcidList){
			String key = getKey(siteType, languageType, pcid);
			RepeatRateInfo info = keyRateMap.get(key);
			if(info != null)
				return info;
			
			key = getKey(siteType, null, pcid);
			info = keyRateMap.get(key);
			if(info != null)
				return info;
		}
		return null;
	}
	
	// private methods
	private static String getKey(SiteType siteType, LanguageType languageType, int cid){
		return siteType.getSiteCode() + "\t" + (languageType == null ? "-1" : languageType.getLanguageId()) + "\t" + (cid <= 0 ? SUM_CID : cid);
	}
	
	private void init() throws SQLException{
		System.out.println("initializing RepeatRateProvider...");
		DBHelper dbHelper = DBPool.getCnBiMasterDbHelper();
		
		String sql = "select merchant_id, language_id, cms_catid, sales_first, rate from BISD.RD_REPEATBUY_RATE_WLP "
				+ "where merchant_id = " + siteType.getSiteCode();
		System.out.println(sql);

		Map<String, Stat> sumKeyStatMap = new HashMap<String, Stat>();
		ResultSet resultSet = dbHelper.executeQuery(sql);
		while(resultSet.next()){
			SiteType siteType = SiteType.getSiteType(resultSet.getInt(1));
			int languageId = resultSet.getInt(2);
			if(languageId <= 0)
				languageId =1;
			LanguageType languageType = LanguageType.getLanguageType(languageId);
			int cid = resultSet.getInt(3);
			double saleFirst = resultSet.getDouble(4);
			double repeatRate = resultSet.getDouble(5);
			if(repeatRate > MAX_REPEAT_RATE)
				repeatRate = MAX_REPEAT_RATE;
			if(repeatRate < MIN_REPEAT_RATE)
				repeatRate = MIN_REPEAT_RATE;
			
			RepeatRateInfo info = new RepeatRateInfo(siteType, languageType, null, cid, repeatRate);
			String key = getKey(siteType, languageType, cid);
			keyRateMap.put(key, info);
			
			// sum
			List<Integer> pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
			pcidList.add(SUM_CID);
			for(int pcid : pcidList){
				String sumKey = getKey(siteType, null, pcid);
				Stat stat = sumKeyStatMap.get(sumKey);
				if(stat == null){
					stat = new Stat();
					stat.siteType = siteType;
					stat.languageType = null;
					stat.cid = pcid;
					sumKeyStatMap.put(sumKey, stat);
				}
				stat.saleFirst += saleFirst;
				stat.saleRepeat += saleFirst * repeatRate;
			}
		}
		System.out.println("original data size: " + keyRateMap.size());
		
		// sum up output
		for(Stat stat : sumKeyStatMap.values()){
			if(stat.saleFirst <= 0)
				continue;
			double repeatRate = stat.saleRepeat / stat.saleFirst;
			String key = getKey(stat.siteType, stat.languageType, stat.cid);
			keyRateMap.put(key, new RepeatRateInfo(siteType, stat.languageType, null, stat.cid, repeatRate));
		}
		System.out.println("finish, size: " + keyRateMap.size());
	}

	// private class
	private static class Stat{
		private SiteType siteType;
		private LanguageType languageType;
		private int cid;
		
		private double saleFirst;
		private double saleRepeat;
	}
	
	// main for test
	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		RepeatRateProvider provider = new RepeatRateProvider(SiteType.litb);
		System.out.println(provider.getRepeatRate(LanguageType.en, 1180));
//		provider = new RepeatRateProvider(SiteType.mini);
//		System.out.println(provider.getRepeatRate(null, -1));
	}

	@Override
	public RepeatRateInfo getRepeatRate(AdwordsCountry adwordsCountry, int categoryId) {
		// TODO Auto-generated method stub
		return null;
	}
}
