package com.litb.bid.component.adw;

import com.litb.adw.lib.enums.AdwordsCountry;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.db.DBHelper;
import com.litb.basic.db.DBPool;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.bid.util.CpTreeFactory;;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class RepeatRateProviderV20160712 implements RepeatRateProviderInterface {
	private static final double MAX_REPEAT_RATE = 3.0;
	private static final double MIN_REPEAT_RATE = 0.0;
	private static final int SUM_CID = -1;
	private static final double MIN_SALES = 20000;
	
	private SiteType siteType;
	private CPTree cpTree;
	
	private HashMap<String, RepeatRateInfo> keyRateMap = new HashMap<String, RepeatRateInfo>();
	private HashMap<String, RepeatRateInfo> keyCountryRateMap = new HashMap<String, RepeatRateInfo>();
	
	// constructor
	public RepeatRateProviderV20160712(SiteType siteType) throws SQLException {
		this.siteType = siteType;
		this.cpTree = CpTreeFactory.getCategoryCpTree(siteType);
		init();
		initCountrys();
	}
	
	// public methods
	public RepeatRateInfo getRepeatRate(LanguageType languageType, int cid) {
		List<Integer> pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
		if(!pcidList.contains(SUM_CID))
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
	
	public RepeatRateInfo getRepeatRate(AdwordsCountry adwordsCountry, int cid) {
		List<Integer> pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
		if(!pcidList.contains(SUM_CID))
			pcidList.add(SUM_CID);
		for(AdwordsCountry country : new AdwordsCountry[]{adwordsCountry, AdwordsCountry.ALL}){
			for(int pcid : pcidList){
				String key = getCountryKey(siteType, country, pcid);
				RepeatRateInfo info = keyCountryRateMap.get(key);
				if(info != null)
					return info;
			}
		}
		return null;
	}
	
	// private methods
	private static String getKey(SiteType siteType, LanguageType languageType, int cid){
		return siteType.getSiteCode() + "\t" + (languageType == null ? "-1" : languageType.getLanguageId()) + "\t" + (cid <= 0 ? SUM_CID : cid);
	}
	
	private static String getCountryKey(SiteType siteType, AdwordsCountry country, int cid){
		return siteType.getSiteCode() + "\t" + country + "\t" + (cid <= 0 ? SUM_CID : cid);
	}
	
	private void init() throws SQLException{
		System.out.println("initializing RepeatRateProvider...");
		DBHelper dbHelper = DBPool.getCnBiMasterDbHelperWithMarketingRole();
		
		String sql = "select merchant_id1, cid, languages_id, t1, rate30 from DW_REPEAT_RATE_MC where t1>=" + MIN_SALES + " and merchant_id1 = " + siteType.getSiteCode();
		System.out.println(sql);

		ResultSet resultSet = dbHelper.executeQuery(sql);
		while(resultSet.next()){
			SiteType siteType = SiteType.getSiteType(resultSet.getInt(1));
			int cid = resultSet.getInt(2);
			int languageId = resultSet.getInt(3);
			LanguageType languageType = null;
			if(languageId > 0)
				languageType = LanguageType.getLanguageType(languageId);
//			double saleFirst = resultSet.getDouble(4);
			double repeatRate = resultSet.getDouble(5);
			if(repeatRate > MAX_REPEAT_RATE)
				repeatRate = MAX_REPEAT_RATE;
			if(repeatRate < MIN_REPEAT_RATE)
				repeatRate = MIN_REPEAT_RATE;
			
			RepeatRateInfo info = new RepeatRateInfo(siteType, languageType, null, cid, repeatRate);
			String key = getKey(siteType, languageType, cid);
			keyRateMap.put(key, info);
			
		}
		
		// total
		sql = "select sum(t3)/(sum(t1)+sum(t2)) from DW_REPEAT_RATE_MC where merchant_id1 = " + siteType.getSiteCode() 
				+ " and cid_level=1 and languages_id = 0";
		resultSet = dbHelper.executeQuery(sql);
		if(resultSet.next()){
			String key = getKey(siteType, null, SUM_CID);
			RepeatRateInfo info = new RepeatRateInfo(siteType, null, null, SUM_CID, resultSet.getDouble(1));
			keyRateMap.put(key, info);
		}
		
		dbHelper.close();
		
		System.out.println("finish, size: " + keyRateMap.size());
	}
	
	private void initCountrys() throws SQLException{
		DBHelper dbHelper = DBPool.getCnBiMasterDbHelperWithMarketingRole();
		
		String sql = "select merchant_id1, cid, iso_code, t1, rate from DW_REPEAT_RATE_COUNTRY where t1>=" + MIN_SALES + " and merchant_id1 = " + siteType.getSiteCode();
		System.out.println(sql);

		ResultSet resultSet = dbHelper.executeQuery(sql);
		while(resultSet.next()){
			try {
				SiteType siteType = SiteType.getSiteType(resultSet.getInt(1));
				int cid = resultSet.getInt(2);
				String countryCode = resultSet.getString(3);
				AdwordsCountry country = AdwordsCountry.valueOf(countryCode);
				double repeatRate = resultSet.getDouble(5);
				if(repeatRate > MAX_REPEAT_RATE)
					repeatRate = MAX_REPEAT_RATE;
				if(repeatRate < MIN_REPEAT_RATE)
					repeatRate = MIN_REPEAT_RATE;
				
				RepeatRateInfo info = new RepeatRateInfo(siteType, null, country, cid, repeatRate);
				String key = getCountryKey(siteType, country, cid);
				keyCountryRateMap.put(key, info);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		dbHelper.close();
		
		System.out.println("finish, size: " + keyCountryRateMap.size());
	}
	
	// main for test
	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		RepeatRateProviderV20160712 litbProvider = new RepeatRateProviderV20160712(SiteType.litb);
		RepeatRateProviderV20160712 miniProvider = new RepeatRateProviderV20160712(SiteType.mini);
		Scanner in = new Scanner(System.in);
		System.out.println("input : [Site Type] [language Type] [category Id]");
		while(in.hasNext()) {
			try {
				String line = in.nextLine();
				if(line.equals("exit") || line.equals("quit"))
					break;
				
				String[] strArr = line.split(" ");
				SiteType siteType = SiteType.valueOf(strArr[0]);
				LanguageType languageType = null;
				if(!strArr[1].equals("null"))
					languageType = LanguageType.valueOf(strArr[1]);
				int cid = Integer.parseInt(strArr[2]);
				
				RepeatRateProviderV20160712 provider = (siteType == SiteType.mini ? miniProvider : litbProvider);
				RepeatRateInfo repeatRateInfo = provider.getRepeatRate(languageType, cid);
				System.out.println(repeatRateInfo.toString());
			}
			catch(Exception exception){
				exception.printStackTrace();
				System.out.println("input : [Site Type] [language Type] [category Id]");
			}
		}
		
		System.out.println("input : [Site Type] [AdwordsCountry] [category Id]");
		while(in.hasNext()) {
			try {
				String line = in.nextLine();
				if(line.equals("exit") || line.equals("quit"))
					break;
				
				String[] strArr = line.split(" ");
				SiteType siteType = SiteType.valueOf(strArr[0]);
				AdwordsCountry adwordsCountry = null;
				if(!strArr[1].equals("null"))
					adwordsCountry = AdwordsCountry.valueOf(strArr[1]);
				int cid = Integer.parseInt(strArr[2]);
				
				RepeatRateProviderV20160712 provider = (siteType == SiteType.mini ? miniProvider : litbProvider);
				RepeatRateInfo repeatRateInfo = provider.getRepeatRate(adwordsCountry, cid);
				System.out.println(repeatRateInfo.toString());
			}
			catch(Exception exception){
				exception.printStackTrace();
				System.out.println("input : [Site Type] [language Type] [category Id]");
			}
		}
		in.close();
		System.exit(0);
//		System.out.println(provider.getRepeatRate(LanguageType.en, 1180));
//		provider = new RepeatRateProvider(SiteType.mini);
//		System.out.println(provider.getRepeatRate(null, -1));
	}
}
