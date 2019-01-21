package com.litb.bid.component.adw.delay;

import com.litb.bid.util.ComponentFactory;
import com.litb.bid.util.CpTreeFactory;
import com.litb.adw.lib.enums.LitbAdChannel;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.db.DBHelper;
import com.litb.basic.db.DBPool;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DisplayDelayRateProviderBi{
  private static final double MAX_DELAY_RATE = 10.0;
  private static final int DELAY_INTERVAL = 90;

  private static final String DATA_DIR = "/mnt/adwords/auto_bidding/delay_display/";
  
  private SiteType siteType;
  private CPTree cpTree;
  private Map<String, double[]> keyOrderRateArrayMap = new ConcurrentHashMap<String, double[]>();
  private Map<String, DelayRateInfo> delayRateInfoCacheMap = new ConcurrentHashMap<String, DelayRateInfo>();

  // constructor
  public DisplayDelayRateProviderBi(SiteType siteType) throws SQLException, IOException  {
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
        double [] rateArray = new double[DELAY_INTERVAL];
        String[] rateArrayStr = arr[1].split(",");
        for (int index = 0; index < DELAY_INTERVAL; index++) {
        	rateArray[index] = Double.parseDouble(rateArrayStr[index]);
        }
        keyOrderRateArrayMap.put(key, rateArray);
      }
      br.close();
    } else {
      System.out.println("[WARNING]Sales data file not Exist: " + salesFilePath);
    }
    System.out.println("init done, size: " + keyOrderRateArrayMap.size());
  }

  public static void loadBiData(SiteType siteType) throws SQLException, IOException {
    Map<String, double[]> keyOrderRateArrayMap = new HashMap<String, double[]>();
    DBHelper dbHelper = DBPool.getCnBiMasterDbHelperWithMarketingRole();
    String sql = "select merchant_id1, cid, rnk, diff_days, order_total, rate_inc from dw_cate_ltv_90days "
        + "where merchant_id1 = " + siteType.getSiteCode() + " "
        + "and diff_days < " + DELAY_INTERVAL;
    System.out.println("sql: " + sql);
    ResultSet resultSet = dbHelper.executeQuery(sql);
    int count = 0;
    while (resultSet.next()) {
      if (count++ % 100000 == 0)
        System.out.println("count = " + count);
//      int merchantId = resultSet.getInt(1);
      int categoryId = resultSet.getInt(2);
      int rank = resultSet.getInt(3);
      int diffDays = resultSet.getInt(4);
      double rateInc = resultSet.getDouble(6);
      if(rateInc <=0 || rateInc > 1)
    	  continue;

      List<Integer> pcidList = new ArrayList<Integer>();
      pcidList.add(categoryId == 0 ? -1 : categoryId);
      if(rank >= 4)
    	  rank = 4;

      for(int pcid : pcidList){
          String key = getKey(pcid,rank);
          // accumulate sales
          double [] orderRateArray = keyOrderRateArrayMap.get(key);
          if (orderRateArray == null) {
        	  orderRateArray = new double[DELAY_INTERVAL];
        	  keyOrderRateArrayMap.put(key, orderRateArray);
          }
          orderRateArray[diffDays] = rateInc + orderRateArray[diffDays];
      }
    }
    resultSet.close();
    dbHelper.close();

    // normalize, and remove abnormal data
    List<String> keyToBeRemoved = new ArrayList<String>();
    for (String key : keyOrderRateArrayMap.keySet()) {
      double [] orderRateArray = keyOrderRateArrayMap.get(key);
      double finalRate = orderRateArray[DELAY_INTERVAL - 1];
      if (finalRate > 0) {
        for (int index = 0; index < orderRateArray.length; index++) {
        	orderRateArray[index] = orderRateArray[index] / finalRate;
        }
      } else {
        keyToBeRemoved.add(key);
      }
    }
    for (String removedKey : keyToBeRemoved) {
    	keyOrderRateArrayMap.remove(removedKey);
    }

    // output to local
    System.out.println("finish, size: " + keyOrderRateArrayMap.size());
    String outputFilePath = getSalesArrayFilePath(siteType);
    System.out.println("output to local: " + outputFilePath);
    BufferedWriter bw = FileHelper.writeFile(outputFilePath);
    List<String> keyList = new ArrayList<String>(keyOrderRateArrayMap.keySet());
    Collections.sort(keyList);
    for (String key : keyList) {
      double [] rateArray = keyOrderRateArrayMap.get(key);
      String line = key + "\t" + getSalesArrayString(rateArray);
      bw.append(line + "\n");
    }
    bw.close();
  }

  // static methods
  private static String getSalesArrayString(double [] rateArray) {
    String string = "";
    for (double rate : rateArray) {
      string += (rate + ",");
    }
    return string.substring(0, string.length()-1);
  }
  private static String getSalesArrayFilePath(SiteType siteType) {
    return DATA_DIR + "rate_array_" + siteType;
  }
  private static String getKey(int categoryId, int rank) {
    return categoryId + "_" + rank;
  }

  // public methods
  public DelayRateInfo getDelayRate(SiteType siteType, LanguageType languageType, int categoryId, int rank,
      int daysInterval, int getOffsetDays, int predictOffsetDays) throws NumberFormatException, IOException, SQLException {
	  if(rank == 0){
		  return  ComponentFactory.getDelayRateProvider(siteType).getDelayRate(LitbAdChannel.adwords_display, siteType,
					languageType, categoryId, daysInterval, getOffsetDays, predictOffsetDays);
	  }
    List<Integer> pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(categoryId);
    if (!pcidList.contains(-1)) {
      pcidList.add(-1);
    }

    for (int parentCid : pcidList) {
        double sumSalesEnd = 0;
        double sumSalesN = 0;
        String topKey = siteType + "\t" + parentCid + "\t" + rank + "\t" + daysInterval + "\t" + getOffsetDays + "\t" + predictOffsetDays;
        DelayRateInfo delayRateInfo = delayRateInfoCacheMap.get(topKey);
        if(delayRateInfo != null)
          return delayRateInfo;
        String key = getKey(parentCid, rank);
        for (int backwardDays = 0; backwardDays < daysInterval; backwardDays++) {
          double [] orderRateArray = keyOrderRateArrayMap.get(key);
          if (orderRateArray != null) {
            sumSalesEnd += orderRateArray[predictOffsetDays - 1];
            int index = Math.min(backwardDays + getOffsetDays - 1, predictOffsetDays - 1);
            sumSalesN += orderRateArray[index];
          }
        }
        // 如果订单数足够，返回
        if(sumSalesN > 0){
          double averageDelayRate = (sumSalesN == 0 ? 0 : sumSalesEnd / sumSalesN);
          if(averageDelayRate < 1) {
            averageDelayRate = 1.0;
          } else if(averageDelayRate > MAX_DELAY_RATE) {
            averageDelayRate = MAX_DELAY_RATE;
          }
          
          DelayRateInfo info = new DelayRateInfo();
          info.setSiteType(siteType);
          info.setChannel(LitbAdChannel.adwords_display);
          info.setLanguageType(languageType);
          info.setCid(categoryId);
          info.setDelayRate(averageDelayRate);
          delayRateInfoCacheMap.put(topKey, info);
          return info;
        }
    }
    return null;
  }

  public static void main(String[] args) throws Exception {
    if (args.length >= 1 && args[0].equals("load")) {
      DisplayDelayRateProviderBi.loadBiData(SiteType.litb);
      DisplayDelayRateProviderBi.loadBiData(SiteType.mini);
    } else {
      System.out.println("Use parameter 'load' to load data from BI.");
    }
    System.out.println("load done.");
    DisplayDelayRateProviderBi litbProvider = new DisplayDelayRateProviderBi(SiteType.litb);
    DisplayDelayRateProviderBi miniProvider = new DisplayDelayRateProviderBi(SiteType.mini);
	Scanner in = new Scanner(System.in);
	System.out.println("input : [Site Type] [language Type] [category Id] [rank] [days Interval] [predict days(30/90)]");
	while(in.hasNext()) {
		try {
			String line = in.nextLine();
			if(line.equals("exit") || line.equals("quit"))
				break;
			if(line.equals("test")) {
				System.out.println("test begin...");
				for(String key : litbProvider.keyOrderRateArrayMap.keySet()) {
					double[] value = litbProvider.keyOrderRateArrayMap.get(key);
					for(double d : value) System.out.print(" " + d);
					System.err.println("");
				}
				for(String key : miniProvider.keyOrderRateArrayMap.keySet()) {
					double[] value = miniProvider.keyOrderRateArrayMap.get(key);
					for(double d : value) System.out.print(" " + d);
					System.err.println("");
				}
				System.out.println("test end.");
			}
			if(line.substring(0, 3).equals("all")) {
				StringTokenizer stringTokenizer = new StringTokenizer(line, " ");
				int leftCount = 1000000;
				if(stringTokenizer.countTokens() == 2) {
					stringTokenizer.nextToken();
					leftCount = Integer.parseInt(stringTokenizer.nextToken());
				}
				System.out.println("all begin...");
				for(DisplayDelayRateProviderBi provider : new DisplayDelayRateProviderBi[] {litbProvider, miniProvider}) {
					{
						for(LanguageType languageType : LanguageType.class.getEnumConstants()) {
							for(int categoryId : provider.cpTree.getAllCategories()) {
								for(int getOffsetDays=1; getOffsetDays<=30; getOffsetDays++) {
									SiteType siteType = provider.equals(litbProvider) ? SiteType.litb : SiteType.mini;
									DelayRateInfo info = provider.getDelayRate(siteType, languageType, categoryId, 1, 3, getOffsetDays, 90);
									if(info == null) 
										System.out.print(getOffsetDays + ":" + "null" + " ");
									else
										System.out.print(getOffsetDays + ":" + info.getDelayRate() + " ");
									if(getOffsetDays == 30) {
										System.out.println(info.getSiteType() + " " + info.getChannel() + " "
												   + info.getLanguageType() + " " + info.getCid());
									}
								}
								System.out.println("");
								leftCount --;
								if(leftCount <= 0) {
									break;
								}
							}
							if(leftCount <= 0) {
								break;
							}
						}
						if(leftCount <= 0) {
							break;
						}
					}
					if(leftCount <= 0) {
						break;
					}
				}
				System.out.println("all end.");
				continue;
			}
			StringTokenizer st = new StringTokenizer(line, " ");
			if(st.countTokens() != 6) {
				System.err.println("usage : [Site Type] [language Type] [category Id] [rank] [days Interval] [predict days30/90]");
				continue;
			}
			SiteType siteType = null;
			LanguageType languageType = null;
			int categoryId = -1;
			int rank = -1;
			
			int daysInterval = 0;
			int predictDays = 0;
			
			String s = null;
			s = st.nextToken();
			siteType = (s.equals("-1")) ? null : SiteType.valueOf(s);
			s = st.nextToken();
			languageType = (s.equals("-1")) ? null : LanguageType.valueOf(s);
			s = st.nextToken();
			categoryId = Integer.valueOf(s);
			s = st.nextToken();
			rank = Integer.valueOf(s);
			s = st.nextToken();
			daysInterval = Integer.valueOf(s);
			if(daysInterval < 1) {
				System.err.println("days Interval should >= 1 !!!");
				continue;
			}
			s = st.nextToken();
			predictDays = Integer.valueOf(s);
            if(predictDays < 1) {
                System.err.println("predictDays should >= 1 !!!");
                continue;
            }
			
			DelayRateInfo info = null;
			for(int getOffsetDays=1; getOffsetDays<=90; getOffsetDays++) {
				DisplayDelayRateProviderBi provider = (siteType == SiteType.litb) ? litbProvider : miniProvider;
				info = provider.getDelayRate(siteType, languageType, categoryId, rank, daysInterval, getOffsetDays, predictDays);
				if(info != null) {
					System.out.println(getOffsetDays + ":" + info.getDelayRate());
					if(getOffsetDays == 30) {
						System.out.println(info.getSiteType() + " " + info.getChannel() + " "
										   + info.getLanguageType() + " " + info.getCid());
					}
				} else {
					System.out.print(getOffsetDays + ":" + null + " ");
					if(getOffsetDays == 30)
						System.out.println("");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("usage : [Site Type] [channel] [language Type] [category Id] [days Interval]");
		}
	}
	in.close();
    System.out.println("Done");
    System.exit(0);
  }
}
