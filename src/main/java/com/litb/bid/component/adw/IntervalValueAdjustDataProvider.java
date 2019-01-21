package com.litb.bid.component.adw;

import com.litb.bid.Conf;
import com.litb.bid.util.CpTreeFactory;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.db.DBHelper;
import com.litb.basic.db.DBPool;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;
import com.litb.basic.util.DateHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class IntervalValueAdjustDataProvider{
	private static final double MAX_ADJ_RATIO = 2;
	private static final double MIN_TOTAL_ADJ_RATIO = 0.01;
	private static final int SUM_CID = -1;
	private static final double MIN_DAILY_RECEIVE_SALES = 1000;
	private static final String DATA_DIR = "/mnt/adwords/auto_bidding/value_adjust_data/";
	private Map<String, GMGAData> keyGMGADataArrMap = new HashMap<String, GMGAData>();
	 
	private SiteType siteType;
	private CPTree cpTree;

	// constructor
	public IntervalValueAdjustDataProvider(SiteType siteType) throws SQLException {
		System.out.println("initializing ValueAdjustDataProvider...");
		this.siteType = siteType;
		this.cpTree = CpTreeFactory.getCategoryCpTree(siteType);
		try {
			initFromLocal();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void initFromLocal() throws IOException {
	    String inputFilePath = getGMGADataFilePath(siteType);
	    System.out.println("reading: " + inputFilePath);
	    if (FileHelper.isFileExist(inputFilePath)) {
	      BufferedReader br = FileHelper.readFile(inputFilePath);
	      String line;
	      while ((line=br.readLine()) != null) {
	        String [] arr = line.split("\t");
	        String key = arr[0];
	        GMGAData data = new GMGAData();
	        data.receiveSales = Double.valueOf(arr[1]);
	        data.gmGA = Double.valueOf(arr[2]);
	        data.gmFix = Double.valueOf(arr[3]);
	        keyGMGADataArrMap.put(key, data);
	      }
	      br.close();
	    } else {
	      System.out.println("[WARNING]data file not Exist: " + inputFilePath);
	    }
	    System.out.println("init done, size: " + keyGMGADataArrMap.size());
	  }
	
	// public methods
	public IntervalValueAdjustRateInfo getValueAdjustRate(LanguageType languageType, int categoryId, int interval) {
		List<Integer> cidList = cpTree.getCategoryAncesterCategoryIncludingSelf(categoryId);
		cidList.add(SUM_CID);
		
		for(int cid : cidList){
			for(LanguageType lang : new LanguageType[] { languageType, null}){
				for(int useInterval : Conf.STAT_INTERVALS){
					if(useInterval < interval)
						continue;
					String key = getKey(lang, cid, useInterval);
					GMGAData data = keyGMGADataArrMap.get(key);
					if(data != null && data.receiveSales >= (MIN_DAILY_RECEIVE_SALES * useInterval * Math.pow(0.8, Conf.getIntervalIndex(useInterval)))){
						double finalRate = data.gmGA == 0 ? 0.0 : (data.gmFix / data.gmGA);
						if(finalRate > MAX_ADJ_RATIO)
							finalRate = MAX_ADJ_RATIO;
						else if(finalRate < MIN_TOTAL_ADJ_RATIO)
							finalRate = MIN_TOTAL_ADJ_RATIO;
						IntervalValueAdjustRateInfo valueAdjustRateInfo = new IntervalValueAdjustRateInfo();
						valueAdjustRateInfo.setCid(cid);
						valueAdjustRateInfo.setLangType(lang);
						valueAdjustRateInfo.setSiteType(siteType);
						valueAdjustRateInfo.setInterval(useInterval);
						valueAdjustRateInfo.setReceiveSales(data.receiveSales);
						valueAdjustRateInfo.setGmGA(data.gmGA);
						valueAdjustRateInfo.setGmFix(data.gmFix);
						valueAdjustRateInfo.setTotalAdjRatio(finalRate);
						
						return valueAdjustRateInfo;
					}
				}
			}
		}
		return null;
	}
	
	// private methods
	private static String getKey(LanguageType languageType, int cid, int interval){
		return (languageType == null ? "-1" : languageType.getLanguageId()) + "_" + (cid <= 0 ? SUM_CID : cid) + "_" + interval;
	}
	private static String getKey(int languageId, int cid, int interval){
		return languageId + "_" + (cid <= 0 ? SUM_CID : cid) + "_" + interval;
	}
	
	private static String getGMGADataFilePath(SiteType siteType){
		return DATA_DIR + "data_" + siteType;
	}
	
	public static void loadBiData(SiteType siteType, Date endDate) throws SQLException, IOException {
		Map<String, GMGAData> keyGMGADataArrMap = new HashMap<String, GMGAData>();
	    DBHelper dbHelper = DBPool.getCnBiMasterDbHelperWithMarketingRole();
	    Date startDate = DateHelper.addDays(1-180, endDate);
	    String sql = "select stat_date, categories_id, languages_id, receive_sales, gm_ga, gm_fix from rd_bidding_value_fix "
	        + "where merchant_id = " + siteType.getSiteCode() + " "
	        + "and stat_date>=to_date('" + DateHelper.getShortDateString(startDate) + "','yyyy-MM-dd')";
	    System.out.println("sql: " + sql);
	    ResultSet resultSet = dbHelper.executeQuery(sql);
	    int count = 0;
	    while (resultSet.next()) {
	      if (count++ % 10000 == 0)
	        System.out.println("count = " + count);
	      Date statDate = resultSet.getDate(1);
	      int categoryId = resultSet.getInt(2);
	      int languageId = resultSet.getInt(3);
	      double receiveSales = resultSet.getDouble(4);
	      double gmGA = resultSet.getDouble(5);
	      double gmFix = resultSet.getDouble(6);
	      
	      if(categoryId <= 0)
	    	  categoryId = -1;
	      if(languageId <= 0)
	    	  languageId = -1;
	      int offset = DateHelper.getDeltaDays(statDate, endDate);
	      
	      for(int interval : Conf.STAT_INTERVALS){
	    	  if(offset < interval){
	    		  String key = getKey(languageId, categoryId, interval);
		    	  GMGAData data = keyGMGADataArrMap.get(key);
		    	  if(data == null){
		    		  data = new GMGAData();
		    		  keyGMGADataArrMap.put(key, data);
		    	  }
		    	  data.receiveSales += receiveSales;
		    	  data.gmGA += gmGA;
		    	  data.gmFix += gmFix;
	    	  }
	      }
	    }
	    resultSet.close();
	    dbHelper.close();

	    // normalize, and remove abnormal data
	    List<String> keyToBeRemoved = new ArrayList<String>();
	    for (String key : keyGMGADataArrMap.keySet()) {
	    	GMGAData data = keyGMGADataArrMap.get(key);
	    	double finalSales = data.receiveSales;
		    if(finalSales <= 0)
		    	keyToBeRemoved.add(key);
	    }
	    for (String removedKey : keyToBeRemoved) {
	    	keyGMGADataArrMap.remove(removedKey);
	    }

	    // output to local
	    System.out.println("finish, size: " + keyGMGADataArrMap.size());
	    String outputFilePath = getGMGADataFilePath(siteType);
	    System.out.println("output to local: " + outputFilePath);
	    BufferedWriter bw = FileHelper.writeFile(outputFilePath);
	    List<String> keyList = new ArrayList<String>(keyGMGADataArrMap.keySet());
	    Collections.sort(keyList);
	    for (String key : keyList) {
	    	GMGAData data = keyGMGADataArrMap.get(key);
	      String line = key + "\t" + data.receiveSales + "\t" + data.gmGA + "\t" + data.gmFix;
	      bw.append(line + "\n");
	    }
	    bw.close();
	  }
	
	public static class GMGAData{
		private double receiveSales;
		private double gmGA;
		private double gmFix;
	}
	
	// main for test
	public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
		Date endDate = DateHelper.getShortDate("2018-04-26");
		if(args[0].equalsIgnoreCase("load")){
			IntervalValueAdjustDataProvider.loadBiData(SiteType.litb, endDate);
			IntervalValueAdjustDataProvider.loadBiData(SiteType.mini, endDate);
		}
		IntervalValueAdjustDataProvider litbProvider = new IntervalValueAdjustDataProvider(SiteType.litb);
//		ValueAdjustDataProviderV20160712 oldLitbProvider = new ValueAdjustDataProviderV20160712(SiteType.litb);
//		BufferedWriter bw = FileHelper.writeFile("/mnt/adwords/auto_bidding/tmp/new_adj");
//		CPTree cpTree = CpTreeFactory.getCategoryCpTree(SiteType.litb, false);
//		for(int interval : Conf.STAT_INTERVALS){
//			for(int cid : cpTree.getAllCategories()){
//				for(LanguageType languageType : LanguageType.values()){
//				 	IntervalValueAdjustRateInfo info = litbProvider.getValueAdjustRate(languageType, cid, interval);
//				 	ValueAdjustRateInfo oldInfo = oldLitbProvider.getValueAdjustRate(languageType, cid);
//				 	bw.append(languageType + "\t" + cid + "\t" + interval + "\t" + info.getTotalAdjRatio() + "\t" + oldInfo.getTotalAdjRatio() + "\t" + (info.getTotalAdjRatio() - oldInfo.getTotalAdjRatio() ) + "\n");
//				}
//			}
//		}
//		bw.close();
		
		IntervalValueAdjustDataProvider miniProvider = new IntervalValueAdjustDataProvider(SiteType.mini);
		Scanner in = new Scanner(System.in);
		System.out.println("input : [Site Type] [language Type] [category Id] [interval]");
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
				int interval = Integer.parseInt(strArr[3]);
				
				IntervalValueAdjustDataProvider provider = (siteType == SiteType.mini ? miniProvider : litbProvider);
				IntervalValueAdjustRateInfo info = provider.getValueAdjustRate(languageType, cid, interval);
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
