package com.litb.bid.component.bing.delay;

import com.litb.bid.component.bing.BingDelayRateProviderInterface;
import com.litb.bid.object.bing.BingLitbAdChannel;
import com.litb.bid.util.BingComponentFactory;
import com.litb.bid.util.CpTreeFactory;
import com.litb.adw.lib.enums.LitbAdChannel;
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
import java.util.concurrent.ConcurrentHashMap;

public class BingDelayRateProviderBi implements BingDelayRateProviderInterface {


    private static final double MAX_DELAY_RATE = 10.0;
    private static final int DELAY_INTERVAL = 90;
    private static final int MIN_ORDER_NUM = 50;
    private static final String DATA_DIR = "/mnt/adwords/auto_bidding/bing_delay/";

    private SiteType siteType;
    private CPTree cpTree;
    private Map<String, double[]> keySalesArrayMap = new HashMap<String, double[]>();
    private Map<String, long[]> keyOrderNumArrayMap = new HashMap<String, long[]>();
    private Calendar calendar;
    private Map<Integer, Integer> backforwordDayToWeekMapCache = new ConcurrentHashMap<Integer, Integer>();
    private Map<String, BingDelayRateInfo> delayRateInfoCacheMap = new ConcurrentHashMap<String, BingDelayRateInfo>();

    // constructor
    public BingDelayRateProviderBi(SiteType siteType) throws SQLException, IOException {
        this.siteType = siteType;
        this.cpTree = CpTreeFactory.getCategoryCpTree(siteType);
        initFromLocal();
        Date endDate = BingComponentFactory.getEndDate();
        calendar = Calendar.getInstance(DateHelper.US_TIMEZONE, Locale.US);
        calendar.setTime(endDate);
    }

    private void initFromLocal() throws IOException {
        String salesFilePath = getSalesArrayFilePath(siteType);
        System.out.println("reading: " + salesFilePath);
        if (FileHelper.isFileExist(salesFilePath)) {
            BufferedReader br = FileHelper.readFile(salesFilePath);
            String line;
            while ((line = br.readLine()) != null) {
                String[] arr = line.split("\t");
                String key = arr[0];
                double[] salesArray = new double[DELAY_INTERVAL + 1];
                String[] salesArrayStr = arr[1].split(",");
                for (int index = 0; index < DELAY_INTERVAL + 1; index++) {
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
            while ((line = br.readLine()) != null) {
                String[] arr = line.split("\t");
                String key = arr[0];
                long[] orderNumArray = new long[DELAY_INTERVAL + 1];
                String[] orderNumArrayStr = arr[1].split(",");
                for (int index = 0; index < DELAY_INTERVAL + 1; index++) {
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
//    CPTree cpTree = CpTreeFactory.getCategoryCpTree(siteType);
        Map<String, double[]> keySalesArrayMap = new HashMap<String, double[]>();
        Map<String, long[]> keyOrderNumArrayMap = new HashMap<String, long[]>();
        DBHelper dbHelper = DBPool.getCnBiMasterDbHelperWithMarketingRole();
        String sql = "select channel_id, category_id, lang, week, diff_day, acc_orders_num, acc_sales from dw_mktc_ltv_cohort_1 "
                + "where merchant_id = " + siteType.getSiteCode() + " "
                + "and typ='no_same_cate' "
                + "and diff_day <= " + DELAY_INTERVAL;
        System.out.println("sql: " + sql);
        ResultSet resultSet = dbHelper.executeQuery(sql);
        int count = 0;
        while (resultSet.next()) {
            if (count++ % 100000 == 0)
                System.out.println("count = " + count);
            int channelId = resultSet.getInt(1);
            int categoryId = resultSet.getInt(2);
            String langStr = resultSet.getString(3);
            LanguageType languageType = null;
            LitbAdChannel channel = null;
            try {
                channel = LitbAdChannel.getLitbAdChannel(channelId);
                languageType = LanguageType.valueOf(langStr.toLowerCase());
            } catch (Exception e) {
                continue;
            }
            int week = Integer.parseInt(resultSet.getString(4));
            int diffDays = resultSet.getInt(5);
            long orderNum = resultSet.getLong(6);
            double sales = resultSet.getDouble(7);

//      List<Integer> pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(categoryId);
//      if (!pcidList.contains(-1)) {
//        pcidList.add(-1);
//      }
            List<Integer> pcidList = new ArrayList<Integer>();
            pcidList.add(categoryId == 0 ? -1 : categoryId);

            for (int pcid : pcidList) {
                for (LanguageType lang : new LanguageType[]{languageType, null}) {
                    String key = getKey(channel, lang, pcid, week);
                    // accumulate sales
                    double[] salesArray = keySalesArrayMap.get(key);
                    if (salesArray == null) {
                        salesArray = new double[DELAY_INTERVAL + 1];
                        keySalesArrayMap.put(key, salesArray);
                    }
                    double oldSales = salesArray[diffDays];
                    salesArray[diffDays] = oldSales + sales;
                    // accumulate order number
                    long[] orderNumArray = keyOrderNumArrayMap.get(key);
                    if (orderNumArray == null) {
                        orderNumArray = new long[DELAY_INTERVAL + 1];
                        keyOrderNumArrayMap.put(key, orderNumArray);
                    }
                    long oldOrderNum = orderNumArray[diffDays];
                    orderNumArray[diffDays] = oldOrderNum + orderNum;
                }
            }
        }
        resultSet.close();
        dbHelper.close();

        // normalize, and remove abnormal data
        List<String> keyToBeRemoved = new ArrayList<String>();
        for (String key : keySalesArrayMap.keySet()) {
            double[] salesArray = keySalesArrayMap.get(key);
            double finalSales = salesArray[DELAY_INTERVAL];
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
            double[] salesArray = keySalesArrayMap.get(key);
            String line = key + "\t" + getSalesArrayString(salesArray);
            bw.append(line + "\n");
        }
        bw.close();

        String outputFilePath2 = getOrderNumArrayFilePath(siteType);
        System.out.println("output to local: " + outputFilePath2);
        BufferedWriter bw2 = FileHelper.writeFile(outputFilePath2);
        Collections.sort(keyList);
        for (String key : keyList) {
            long[] orderNumArray = keyOrderNumArrayMap.get(key);
            String line = key + "\t" + getOrderNumArrayString(orderNumArray);
            bw2.append(line + "\n");
        }
        bw2.close();
    }

    // static methods
    private static String getSalesArrayString(double[] salesArray) {
        String string = "";
        for (double sales : salesArray) {
            string += (sales + ",");
        }
        return string.substring(0, string.length() - 1);
    }

    private static String getOrderNumArrayString(long[] orderNumArray) {
        String string = "";
        for (long orderNum : orderNumArray) {
            string += (orderNum + ",");
        }
        return string.substring(0, string.length() - 1);
    }

    private static String getSalesArrayFilePath(SiteType siteType) {
        return DATA_DIR + "sales_array_" + siteType;
    }

    private static String getOrderNumArrayFilePath(SiteType siteType) {
        return DATA_DIR + "order_num_array_" + siteType;
    }

    private static String getKey(LitbAdChannel channel, LanguageType languageType, int categoryId, int week) {
        return channel + "_" + languageType + "_" + categoryId + "_" + week;
    }

//  private int getWeek(int backwardDays) {
//    Date endDate = ComponentFactory.getEndDate();
//    Date beginDate = DateHelper.addDays(-backwardDays, endDate);
//    Calendar calendar = Calendar.getInstance();
//    calendar.setTime(beginDate);
//    return calendar.get(Calendar.WEEK_OF_YEAR);
//    String year = DateHelper.getShortDateString(beginDate).substring(0, 4);
//    Date firstDayOfYear = DateHelper.getShortDate(year + "-01-01");
//    int deltaDays = DateHelper.getDeltaDays(firstDayOfYear, beginDate);
//    return Math.min(52, deltaDays/7 + 1);
//  }

    private int getWeek(int backwardDays) {
        Integer week = backforwordDayToWeekMapCache.get(backwardDays);
        if (week == null) {
            synchronized (calendar) {
                calendar.add(Calendar.DAY_OF_MONTH, -backwardDays);
                week = calendar.get(Calendar.WEEK_OF_YEAR);
                backforwordDayToWeekMapCache.put(backwardDays, week);
                calendar.add(Calendar.DAY_OF_MONTH, backwardDays);
            }
        }
        return week;
    }

    // public methods
    @Override
    public BingDelayRateInfo getDelayRate(BingLitbAdChannel channel, SiteType siteType, LanguageType languageType, int categoryId,
                                          int daysInterval, int getOffsetDays, int predictOffsetDays) {
        List<Integer> pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(categoryId);
        if (!pcidList.contains(-1)) {
            pcidList.add(-1);
        }

        for (int parentCid : pcidList) {
            for (LanguageType lang : new LanguageType[]{languageType, null}) {
                double sumSalesEnd = 0;
                double sumSalesN = 0;
                long sumOrderCount = 0;
                String topKey = channel + "\t" + siteType + "\t" + lang + "\t" + parentCid + "\t" + daysInterval + "\t" + getOffsetDays + "\t" + predictOffsetDays;
                BingDelayRateInfo delayRateInfo = delayRateInfoCacheMap.get(topKey);
                if (delayRateInfo != null)
                    return delayRateInfo;
                String prefixKey = channel + "_" + lang + "_" + parentCid;
                for (int backwardDays = 0; backwardDays < daysInterval; backwardDays++) {
                    int week = getWeek(backwardDays + getOffsetDays - 1);
//          String key = getKey(channel, lang, parentCid, week);
                    String key = prefixKey + "_" + week;
                    long[] orderNumArray = keyOrderNumArrayMap.get(key);
                    if (orderNumArray != null) {
                        sumOrderCount += orderNumArray[getOffsetDays - 1];
                        double[] salesArray = keySalesArrayMap.get(key);
                        sumSalesEnd += salesArray[predictOffsetDays];
                        int index = Math.min(backwardDays + getOffsetDays - 1, predictOffsetDays);
                        sumSalesN += salesArray[index];
                    }
                }
                // 如果订单数足够，返回
                if (sumOrderCount >= MIN_ORDER_NUM) {
                    double averageDelayRate = (sumSalesN == 0 ? 0 : sumSalesEnd / sumSalesN);
                    if (averageDelayRate < 1) {
                        averageDelayRate = 1.0;
                    } else if (averageDelayRate > MAX_DELAY_RATE) {
                        averageDelayRate = MAX_DELAY_RATE;
                    }

                    BingDelayRateInfo info = new BingDelayRateInfo();
                    info.setSiteType(siteType);
                    info.setChannel(channel);
                    info.setLanguageType(languageType);
                    info.setCid(categoryId);
                    info.setDelayRate(averageDelayRate);
                    delayRateInfoCacheMap.put(topKey, info);
                    return info;
                }
            }
        }
        return null;
    }

}
