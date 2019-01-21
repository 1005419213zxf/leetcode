package com.litb.bid.component.adw.delay;

import com.litb.bid.util.CpTreeFactory;
import com.litb.adw.lib.enums.LitbAdChannel;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.db.DBHelper;
import com.litb.basic.db.DBPool;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class FacebookDelayRateProviderBi{
  private static final double MAX_DELAY_RATE = 5.0;
  private static final int DELAY_INTERVAL = 28;
  private static final int MIN_ORDER_NUM = 20;

  private static final String DATA_DIR = "/mnt/adwords/auto_bidding/delay/";
  private SiteType siteType;
  private CPTree cpTree;
  private Map<String, double[]> keySalesArrayMap = new HashMap<String, double[]>();
  private Map<String, long[]> keyOrderNumArrayMap = new HashMap<String, long[]>();

  // constructor
  public FacebookDelayRateProviderBi(SiteType siteType) throws SQLException, IOException  {
    this.siteType = siteType;
    this.cpTree = CpTreeFactory.getCategoryCpTree(siteType);
    initFromLocal();
  }
  
  private void initFromLocal() throws IOException {
	    String salesFilePath = getSalesArrayFilePath(siteType);
	    System.out.println("reading: " + salesFilePath);
	    if (FileHelper.isFileExist(salesFilePath)) {
	      BufferedReader br = FileHelper.readFile(salesFilePath);
	      String line;
	      while ((line=br.readLine()) != null) {
	        String [] arr = line.split("\t");
	        String key = arr[0];
	        double [] salesArray = new double[DELAY_INTERVAL];
	        String[] salesArrayStr = arr[1].split(",");
	        for (int index = 0; index < DELAY_INTERVAL; index++) {
	          salesArray[index] = Double.parseDouble(salesArrayStr[index]);
	        }
	        keySalesArrayMap.put(key, salesArray);
	      }
	      br.close();
	    } else {
	      System.out.println("[WARNING]Sales data file not Exist: " + salesFilePath);
	    }
	    
	    String orderNumFilePath = getOrderNumArrayFilePath(siteType);
	    System.out.println("reading: " + orderNumFilePath);
	    if (FileHelper.isFileExist(orderNumFilePath)) {
	      BufferedReader br = FileHelper.readFile(orderNumFilePath);
	      String line;
	      while ((line=br.readLine()) != null) {
	        String [] arr = line.split("\t");
	        String key = arr[0];
	        long [] orderNumArray = new long[DELAY_INTERVAL];
	        String [] orderNumArrayStr = arr[1].split(",");
	        for (int index = 0; index < DELAY_INTERVAL; index++) {
	          orderNumArray[index] = Long.parseLong(orderNumArrayStr[index]);
	        }
	        keyOrderNumArrayMap.put(key, orderNumArray);
	      }
	      br.close();
	    } else {
	      System.out.println("[WARNING]Order number file not exist: " + orderNumFilePath);
	    }
	    System.out.println("init done, size: " + keySalesArrayMap.size());
	  }

  public static void loadBiData(SiteType siteType) throws SQLException, IOException {
    Map<String, double[]> keySalesArrayMap = new HashMap<String, double[]>();
    Map<String, long[]> keyOrderNumArrayMap = new HashMap<String, long[]>();
    DBHelper dbHelper = DBPool.getCnBiMasterDbHelperWithMarketingRole();
    String sql = "select campaign_id, category_id, diff_day, acc_orders_num, acc_sales from dw_account_mktc_delay_rate "
        + "where merchant_id = " + siteType.getSiteCode() + " "
        + "and channel_id= " + LitbAdChannel.facebook.getChannelId() + " "
        + "and diff_day <= " + DELAY_INTERVAL;
    System.out.println("sql: " + sql);
    ResultSet resultSet = dbHelper.executeQuery(sql);
    int count = 0;
    while (resultSet.next()) {
      if (count++ % 100000 == 0)
        System.out.println("count = " + count);
      long campaignId = resultSet.getLong(1);
      int categoryId = resultSet.getInt(2);
      int diffDays = resultSet.getInt(3);
      long orderNum = resultSet.getLong(4);
      double sales = resultSet.getDouble(5);

      String key = getKey(campaignId, categoryId);
      // accumulate sales
      double [] salesArray = keySalesArrayMap.get(key);
      if (salesArray == null) {
        salesArray = new double[DELAY_INTERVAL];
        keySalesArrayMap.put(key, salesArray);
      }
      double oldSales = salesArray[diffDays];
      salesArray[diffDays] = oldSales + sales;
      // accumulate order number
      long [] orderNumArray = keyOrderNumArrayMap.get(key);
      if (orderNumArray == null) {
        orderNumArray = new long[DELAY_INTERVAL];
        keyOrderNumArrayMap.put(key, orderNumArray);
      }
      long oldOrderNum = orderNumArray[diffDays];
      orderNumArray[diffDays] = oldOrderNum + orderNum;
    }
    resultSet.close();
    dbHelper.close();

    // normalize, and remove abnormal data
    List<String> keyToBeRemoved = new ArrayList<String>();
    for (String key : keySalesArrayMap.keySet()) {
      double [] salesArray = keySalesArrayMap.get(key);
      double finalSales = salesArray[DELAY_INTERVAL - 1];
      if (finalSales > 0) {
        for (int index = 0; index < salesArray.length; index++) {
          salesArray[index] = salesArray[index] / finalSales;
        }
      } else {
        keyToBeRemoved.add(key);
      }
    }
    for (String removedKey : keyToBeRemoved) {
      keySalesArrayMap.remove(removedKey);
      keyOrderNumArrayMap.remove(removedKey);
    }
    
 // output to local
    System.out.println("finish, size: " + keySalesArrayMap.size());
    String outputFilePath = getSalesArrayFilePath(siteType);
    System.out.println("output to local: " + outputFilePath);
    BufferedWriter bw = FileHelper.writeFile(outputFilePath);
    List<String> keyList = new ArrayList<String>(keySalesArrayMap.keySet());
    Collections.sort(keyList);
    for (String key : keyList) {
      double [] salesArray = keySalesArrayMap.get(key);
      String line = key + "\t" + getSalesArrayString(salesArray);
      bw.append(line + "\n");
    }
    bw.close();

    String outputFilePath2 = getOrderNumArrayFilePath(siteType);
    System.out.println("output to local: " + outputFilePath2);
    BufferedWriter bw2 = FileHelper.writeFile(outputFilePath2);
    Collections.sort(keyList);
    for (String key : keyList) {
      long [] orderNumArray = keyOrderNumArrayMap.get(key);
      String line = key + "\t" + getOrderNumArrayString(orderNumArray);
      bw2.append(line + "\n");
    }
    bw2.close();
  }
  
//static methods
 private static String getSalesArrayString(double [] salesArray) {
   String string = "";
   for (double sales : salesArray) {
     string += (sales + ",");
   }
   return string.substring(0, string.length()-1);
 }
 private static String getOrderNumArrayString(long [] orderNumArray) {
   String string = "";
   for (long orderNum : orderNumArray) {
     string += (orderNum + ",");
   }
   return string.substring(0, string.length()-1);
 }
 private static String getSalesArrayFilePath(SiteType siteType) {
   return DATA_DIR + "facebook_sales_array_" + siteType;
 }
 private static String getOrderNumArrayFilePath(SiteType siteType) {
   return DATA_DIR + "facebook_order_num_array_" + siteType;
 }

  // static methods
  private static String getKey(long campaignId, int categoryId) {
    return campaignId + "_" + categoryId;
  }
  // public methods
  public DelayRateInfo getDelayRate(SiteType siteType, long campaignId, int categoryId,
      int daysInterval, int getOffsetDays, int predictOffsetDays) {
	  if(categoryId > 0)
		  campaignId = -1L;
    List<Integer> pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(categoryId);
    if (!pcidList.contains(0)) {
        pcidList.add(0);
      }
    if (!pcidList.contains(-1)) {
      pcidList.add(-1);
    }
    List<Long> campaignIdList = new ArrayList<Long>();
    campaignIdList.add(campaignId);
    if (!campaignIdList.contains(0L)) {
    	campaignIdList.add(0L);
      }
    if (!campaignIdList.contains(-1L)) {
    	campaignIdList.add(-1L);
      }

    for(long useCampaignId : campaignIdList){
    	for (int parentCid : pcidList) {
            double sumSalesEnd = 0;
            double sumSalesN = 0;
            long sumOrderEnd = 0;
            long sumOrderN = 0;
            long sumOrderCount = 0;
            for (int backwardDays = 0; backwardDays < daysInterval; backwardDays++) {
              String key = getKey(useCampaignId, parentCid);
              long [] orderNumArray = keyOrderNumArrayMap.get(key);
              if (orderNumArray != null) {
                sumOrderCount += orderNumArray[getOffsetDays-1];
                double [] salesArray = keySalesArrayMap.get(key);
                sumSalesEnd += salesArray[predictOffsetDays - 1];
                sumOrderEnd += orderNumArray[predictOffsetDays - 1];
                int index = Math.min(backwardDays + getOffsetDays - 1, predictOffsetDays - 1);
                sumSalesN += salesArray[index];
                sumOrderN += orderNumArray[index];
              }
            }
            // 如果订单数足够，返回
            if (sumOrderCount >= MIN_ORDER_NUM) {
              double averageDelayRate = (sumSalesN == 0 ? 0 : sumSalesEnd / sumSalesN);
              if(averageDelayRate < 1) {
                averageDelayRate = 1.0;
              } else if(averageDelayRate >= MAX_DELAY_RATE) {
	        	  averageDelayRate = (sumOrderN == 0 ? 0 : sumOrderEnd / sumOrderN);
	              if(averageDelayRate < 1) {
	                averageDelayRate = 1.0;
	              } else if(averageDelayRate > MAX_DELAY_RATE) {
	                averageDelayRate = MAX_DELAY_RATE;
	              }
              }
              
              DelayRateInfo info = new DelayRateInfo();
              info.setSiteType(siteType);
              info.setChannel(LitbAdChannel.facebook);
              info.setLanguageType(null);
              info.setCid(categoryId);
              info.setDelayRate(averageDelayRate);
              return info;
            }
        }
    }
    return null;
  }

  public static void main(String[] args) throws Exception {
	if (args.length >= 1 && args[0].equals("load")) {
	  FacebookDelayRateProviderBi.loadBiData(SiteType.litb);
	  FacebookDelayRateProviderBi.loadBiData(SiteType.mini);
    } else {
      System.out.println("Use parameter 'load' to load data from BI.");
    }
    FacebookDelayRateProviderBi delayRateProviderBi = new FacebookDelayRateProviderBi(SiteType.litb);
    System.out.println(delayRateProviderBi.getDelayRate(SiteType.litb, -1L, 1180, 1, 1, 28));
    System.out.println("Done");
    System.exit(0);
  }
}
