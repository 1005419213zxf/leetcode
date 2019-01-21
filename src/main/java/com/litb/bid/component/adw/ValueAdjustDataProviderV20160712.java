package com.litb.bid.component.adw;

import com.litb.bid.util.CpTreeFactory;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.db.DBHelper;
import com.litb.basic.db.DBPool;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ValueAdjustDataProviderV20160712 implements ValueAdjustDataProviderInterface {
	private static final boolean DEBUG = false;
	
//	private static final double MAX_ADJ_RATIO = 5;
	private static final double MAX_ADJ_RATIO = 2;
	private static final double MIN_TOTAL_ADJ_RATIO = 0.01;
	private static final int SUM_CID = -1;
	private static final double MIN_ONLINE_PAY_RATIO_BASE = 10000;
	private static final double MIN_ONLINE_CONFIRM_DIVIDE_RECEIVED_BASE = 10000;
	private static final double MIN_OFFLINE_CONFIRM_RATE_BASE = 10000;
	private static final double MIN_VALUE_ADJ_BASE = 10000;
	
	private Map<String, Double> keyOnlinePayRatioMap = new HashMap<String, Double>();
	private Map<String, Double> keyOnlineConfirmDivideReceivedMap = new HashMap<String, Double>();
	private Map<String, Double> keyOfflineConfirmRateMap = new HashMap<String, Double>();
	private Map<String, Double> keyValueAdjMap = new HashMap<String, Double>();
	 
	private SiteType targetSiteType;
	private CPTree cpTree;

	// constructor
	public ValueAdjustDataProviderV20160712(SiteType siteType) throws SQLException {
		System.out.println("initializing ValueAdjustDataProvider...");
		this.targetSiteType = siteType;
		this.cpTree = CpTreeFactory.getCategoryCpTree(siteType);
		DBHelper dbHelper = DBPool.getCnBiMasterDbHelperWithMarketingRole();
		
		init(dbHelper, keyOnlinePayRatioMap, 				"online_rate", 		"total_sales", 		MIN_ONLINE_PAY_RATIO_BASE);
		init(dbHelper, keyOnlineConfirmDivideReceivedMap, 	"confirm_receive", 	"receive_sales", 	MIN_ONLINE_CONFIRM_DIVIDE_RECEIVED_BASE);
		init(dbHelper, keyOfflineConfirmRateMap, 			"confirm_rate", 	"offline_sales", 	MIN_OFFLINE_CONFIRM_RATE_BASE);
		init(dbHelper, keyValueAdjMap, 						"gp_rate", 			"gp_ga", 			MIN_VALUE_ADJ_BASE);
		dbHelper.close();
	}
	
	// public methods
	public ValueAdjustRateInfo getValueAdjustRate(LanguageType languageType, int cid) {
		List<Integer> pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
		pcidList.add(SUM_CID);
		
		ValueAdjustRateInfo onlinePayRatioInfo = getData(keyOnlinePayRatioMap, languageType, cid, pcidList, "onlinePayRatioInfo");
		if(onlinePayRatioInfo == null)
			return null;
		ValueAdjustRateInfo onlineConfirmDivideReceivedInfo = getData(keyOnlineConfirmDivideReceivedMap, languageType, cid, pcidList, "onlineConfirmDivideReceivedInfo");
		if(onlineConfirmDivideReceivedInfo == null)
			return null;
		ValueAdjustRateInfo offlineConfirmRateInfo = getData(keyOfflineConfirmRateMap, languageType, cid, pcidList, "offlineConfirmRateInfo");
		if(offlineConfirmRateInfo == null)
			return null;
		ValueAdjustRateInfo valueAdjInfo = getData(keyValueAdjMap, languageType, cid, pcidList, "valueAdjInfo");
		if(valueAdjInfo == null)
			return null;
		
		double onlinePayRatio = onlinePayRatioInfo.getTotalAdjRatio();
		double onlineConfirmDivideReceived = onlineConfirmDivideReceivedInfo.getTotalAdjRatio();
		double offlineConfirmRate = offlineConfirmRateInfo.getTotalAdjRatio();
		double valueAdj = valueAdjInfo.getTotalAdjRatio();
		
		double finalRate = ( onlinePayRatio * onlineConfirmDivideReceived + (1 - onlinePayRatio) * offlineConfirmRate ) * valueAdj;
		if(finalRate > MAX_ADJ_RATIO)
			finalRate = MAX_ADJ_RATIO;
		else if(finalRate < MIN_TOTAL_ADJ_RATIO)
			finalRate = MIN_TOTAL_ADJ_RATIO;
		
		valueAdjInfo.setTotalAdjRatio(finalRate);
		return valueAdjInfo;
	}
	
	private ValueAdjustRateInfo getData(Map<String, Double> map, LanguageType languageType, int cid, List<Integer> pcidList, String alert){
		for(LanguageType lt : Arrays.asList(languageType, null)){
			for(int pcid : pcidList){
				String key = getKey(lt, pcid);
				Double data = map.get(key);
				if(data != null){
					ValueAdjustRateInfo info = new ValueAdjustRateInfo();
					info.setSiteType(targetSiteType);
					info.setCid(pcid);
					info.setLangType(lt);
					info.setTotalAdjRatio(data);
					if(DEBUG){
						System.out.println(alert);
						System.out.println(info.toString());
					}
					return info;
				}
			}
		}
		return null;
	}
	
	// private methods
	private static String getKey(LanguageType languageType, int cid){
		return (languageType == null ? "-1" : languageType.getLanguageId()) + "\t" + (cid <= 0 ? SUM_CID : cid);
	}
	
	private void init(DBHelper dbHelper, Map<String, Double> map, String columName1, String colunmName2, double threshold) throws SQLException{
		System.out.println("initializing column " + columName1 + " " + colunmName2 + " ...");
		// table 1 
		String sql = "select merchant_id, languages_id, categoryid, " + columName1 + ", " + colunmName2 + " "
				+ "from dw_bidding_fix_mlc where merchant_id = " + targetSiteType.getSiteCode() + " "
				+ "and " + colunmName2 + " >= " + threshold;
		System.out.println(sql);
		ResultSet resultSet = dbHelper.executeQuery(sql);
		while(resultSet.next()){
			LanguageType languageType = null;
			int languageId = resultSet.getInt(2);
			if(languageId > 0)
				languageType = LanguageType.getLanguageType(languageId);
			int cid = resultSet.getInt(3);
			if(cid == 0)
				cid = SUM_CID;
			double data = resultSet.getDouble(4);
			
			String key = getKey(languageType, cid);
			map.put(key, data);
		}
		System.out.println("finish, size: " + map.size());
	}
	
	// main for test
	public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
		ValueAdjustDataProviderV20160712 litbProvider = new ValueAdjustDataProviderV20160712(SiteType.litb);
		BufferedWriter bw = FileHelper.writeFile("/mnt/adwords/auto_bidding/tmp/old_adj");
		CPTree cpTree = CpTreeFactory.getCategoryCpTree(SiteType.litb, false);
		{
			for(int cid : cpTree.getAllCategories()){
				for(LanguageType languageType : LanguageType.values()){
				 	ValueAdjustRateInfo info = litbProvider.getValueAdjustRate(languageType, cid);
				 	bw.append(info.getLangType() + "\t" + info.getCid() + "\t" + info.getTotalAdjRatio() + "\n");
				}
			}
		}
		bw.close();
		ValueAdjustDataProviderV20160712 miniProvider = new ValueAdjustDataProviderV20160712(SiteType.mini);
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
				
				ValueAdjustDataProviderV20160712 provider = (siteType == SiteType.mini ? miniProvider : litbProvider);
				ValueAdjustRateInfo info = provider.getValueAdjustRate(languageType, cid);
				System.out.println("result");
				System.out.println(info.toString());
			}
			catch(Exception exception){
				System.out.println("input : [Site Type] [language Type] [category Id]");
			}
		}
		in.close();
		System.exit(0);
	}
}
