package com.litb.bid.component.adw;

import com.litb.bid.util.ComponentFactory;
import com.litb.bid.util.CpTreeFactory;
import com.litb.adw.lib.enums.AdwordsCountry;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.db.DBHelper;
import com.litb.basic.db.DBPool;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class DisplayRepeatRateProvider{
	private static final double MAX_REPEAT_RATE = 3.0;
	private static final double MIN_REPEAT_RATE = 0.0;
	private static final int SUM_CID = -1;
	private static final double MIN_SALES = 20000;
	
	private SiteType siteType;
	private CPTree cpTree;
	
	private HashMap<String, RepeatRateInfo> keyRateMap = new HashMap<String, RepeatRateInfo>();
	
	// constructor
	public DisplayRepeatRateProvider(SiteType siteType) throws SQLException {
		this.siteType = siteType;
		this.cpTree = CpTreeFactory.getCategoryCpTree(siteType);
		init();
	}
	
	// public methods
	public RepeatRateInfo getRepeatRate(LanguageType languageType, AdwordsCountry adwordsCountry, int cid, int rank) throws SQLException, IOException {
		if(rank == 0){
			RepeatRateInfo repeatRateInfo = ComponentFactory.getRepeatRateProvider(siteType).getRepeatRate(adwordsCountry, cid);
			if(repeatRateInfo == null)
				repeatRateInfo = ComponentFactory.getRepeatRateProvider(siteType).getRepeatRate(languageType, cid);
			return repeatRateInfo;
		}
		List<Integer> pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
		if(!pcidList.contains(SUM_CID))
			pcidList.add(SUM_CID);
		for(int pcid : pcidList){
			String key = getKey(siteType, languageType, pcid, rank);
			RepeatRateInfo info = keyRateMap.get(key);
			if(info != null)
				return info;
			
			key = getKey(siteType, null, pcid, rank);
			info = keyRateMap.get(key);
			if(info != null)
				return info;
		}
		return null;
	}
	
	// private methods
	private static String getKey(SiteType siteType, LanguageType languageType, int cid, int rank){
		return siteType.getSiteCode() + "\t" + (languageType == null ? "-1" : languageType.getLanguageId()) + "\t" + (cid <= 0 ? SUM_CID : cid) + "\t" + rank;
	}
	
	private void init() throws SQLException{
		System.out.println("initializing DisplayRepeatRateProvider...");
		DBHelper dbHelper = DBPool.getCnBiMasterDbHelperWithMarketingRole();
		
		String sql = "select merchant_id1, cid, rnk, order_total, ltv_total_90, ltv_total_over90 from dw_cate_ltv where merchant_id1 = " + siteType.getSiteCode();
		System.out.println(sql);

		HashMap<String, LtvInfo> keyInfoMap = new HashMap<String, LtvInfo>();
		ResultSet resultSet = dbHelper.executeQuery(sql);
		while(resultSet.next()){
			SiteType siteType = SiteType.getSiteType(resultSet.getInt(1));
			int cid = resultSet.getInt(2);
			int rank = resultSet.getInt(3);
			double orderTotal = resultSet.getDouble(4);
			double ltv1 = resultSet.getDouble(5);
			double ltv2 = resultSet.getDouble(6);
			if(rank >= 4)
				rank = 4;
			String key = getKey(siteType, null, cid, rank);
			LtvInfo ltvInfo = keyInfoMap.get(key);
			if(ltvInfo == null){
				ltvInfo = new LtvInfo();
				ltvInfo.cid = cid;
				keyInfoMap.put(key, ltvInfo);
			}
			ltvInfo.orderTotal += orderTotal;
			ltvInfo.ltv1 += ltv1;
			ltvInfo.ltv2 += ltv2;
		}
		dbHelper.close();
		
		for(String key : keyInfoMap.keySet()){
			LtvInfo ltvInfo = keyInfoMap.get(key);
			if(ltvInfo.orderTotal < MIN_SALES)
				continue;
			double t1 = ltvInfo.ltv1 / ltvInfo.orderTotal;
			double t2 = ltvInfo.ltv2 / ltvInfo.orderTotal;
			double repeatRate = t2 / (1 + t1);
			if(repeatRate > MAX_REPEAT_RATE)
				repeatRate = MAX_REPEAT_RATE;
			if(repeatRate < MIN_REPEAT_RATE)
				repeatRate = MIN_REPEAT_RATE;
			
			RepeatRateInfo info = new RepeatRateInfo(siteType, null, null, ltvInfo.cid, repeatRate);
			keyRateMap.put(key, info);
		}
		System.out.println("finish, size: " + keyRateMap.size());
	}
	
	public static class LtvInfo{
		private int cid;
		private double orderTotal;
		private double ltv1;
		private double ltv2;
	}
	
	// main for test
	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		DisplayRepeatRateProvider litbProvider = new DisplayRepeatRateProvider(SiteType.litb);
		DisplayRepeatRateProvider miniProvider = new DisplayRepeatRateProvider(SiteType.mini);
		Scanner in = new Scanner(System.in);
		System.out.println("input : [Site Type] [language Type] [category Id] [rank]");
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
				int rank = Integer.parseInt(strArr[3]);
				
				DisplayRepeatRateProvider provider = (siteType == SiteType.mini ? miniProvider : litbProvider);
				RepeatRateInfo repeatRateInfo = provider.getRepeatRate(languageType, null, cid, rank);
				System.out.println(repeatRateInfo.toString());
			}
			catch(Exception exception){
				exception.printStackTrace();
				System.out.println("input : [Site Type] [language Type] [category Id] [rank]");
			}
		}
		in.close();
		System.exit(0);
//		System.out.println(provider.getRepeatRate(LanguageType.en, 1180));
//		provider = new RepeatRateProvider(SiteType.mini);
//		System.out.println(provider.getRepeatRate(null, -1));
	}
}
