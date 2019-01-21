package com.litb.bid.component.bing;


import com.litb.basic.cptree.CPTree;
import com.litb.basic.db.DBHelper;
import com.litb.basic.db.DBPool;
import com.litb.basic.enums.Country;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;
import com.litb.basic.util.DateHelper;
import com.litb.bid.util.ComponentFactory;
import com.litb.bid.util.CpTreeFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class BingRepeatRateWithWeekProvider implements BingRepeatRateProviderInterface {
    private static final double MAX_REPEAT_RATE = 3.0;
    private static final double MIN_REPEAT_RATE = 0.0;
    private static final int SUM_CID = -1;
    private static final String DATA_DIR = "/mnt/adwords/auto_bidding/bing_delay/";

    private SiteType siteType;
    private CPTree cpTree;
    private Calendar calendar;

    private HashMap<String, Double> keyRateMap = new HashMap<String, Double>();
    private HashMap<String, Double> keyCountryRateMap = new HashMap<String, Double>();

    // constructor
    public BingRepeatRateWithWeekProvider(SiteType siteType) throws SQLException, IOException {
        this.siteType = siteType;
        this.cpTree = CpTreeFactory.getCategoryCpTree(siteType);
        initFromLocal();
        Date endDate = ComponentFactory.getEndDate();
        calendar = Calendar.getInstance(DateHelper.US_TIMEZONE, Locale.US);
        calendar.setTime(endDate);
    }

    // public methods
    public BingRepeatRateInfo getRepeatRate(LanguageType languageType, int cid) {
        List<Integer> pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
        if(!pcidList.contains(SUM_CID))
            pcidList.add(SUM_CID);
        int week = getWeek();
        for(LanguageType lang : new LanguageType[]{languageType, LanguageType.all}){
            for(int pcid : pcidList){
                String key = getKey(siteType, lang, pcid, week);
                Double repeatRate = keyRateMap.get(key);
                if(repeatRate != null){
                    BingRepeatRateInfo info = new BingRepeatRateInfo(siteType, lang, null, pcid, repeatRate);
                    return info;
                }
            }
        }
        return null;
    }

    public BingRepeatRateInfo getRepeatRate(Country Country, int cid) {
        List<Integer> pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
        if(!pcidList.contains(SUM_CID))
            pcidList.add(SUM_CID);
        int week = getWeek();
        for(Country country : new Country[]{Country, Country.ALL}){
            for(int pcid : pcidList){
                String key = getCountryKey(siteType, country, pcid, week);
                Double repeatRate = keyCountryRateMap.get(key);
                if(repeatRate != null){
                    BingRepeatRateInfo info = new BingRepeatRateInfo(siteType, null, country, pcid, repeatRate);
                    return info;
                }
            }
        }
        return null;
    }

    // private methods
    private static String getKey(SiteType siteType, LanguageType languageType, int cid, int week){
        return siteType.getSiteCode() + "_" + (languageType == null ? "-1" : languageType.getLanguageId()) + "_" + (cid <= 0 ? SUM_CID : cid) + "_" + week;
    }

    private static String getCountryKey(SiteType siteType, Country country, int cid, int week){
        return siteType.getSiteCode() + "_" + country + "_" + (cid <= 0 ? SUM_CID : cid) + "_" + week;
    }

    private int getWeek() {
        int week = calendar.get(Calendar.WEEK_OF_YEAR);
        return week;
    }

    private static String getLanguageRepeatRateFilePath(SiteType siteType) {
        return DATA_DIR + "lang_repeat_rate" + siteType;
    }

    private static String getCountryRepeatRateFilePath(SiteType siteType) {
        return DATA_DIR + "country_repeat_rate" + siteType;
    }

    public void initFromLocal() throws IOException{
        String inputFilePath = getLanguageRepeatRateFilePath(siteType);
        System.out.println("reading: " + inputFilePath);
        if (FileHelper.isFileExist(inputFilePath)) {
            BufferedReader br = FileHelper.readFile(inputFilePath);
            String line;
            while ((line=br.readLine()) != null) {
                String [] arr = line.split("\t");
                String key = arr[0];
                double repeatRate = Double.valueOf(arr[1]);
                keyRateMap.put(key, repeatRate);
            }
            br.close();
        } else {
            System.out.println("[WARNING]lang repeat rate file not Exist: " + inputFilePath);
        }

        inputFilePath = getCountryRepeatRateFilePath(siteType);
        System.out.println("reading: " + inputFilePath);
        if (FileHelper.isFileExist(inputFilePath)) {
            BufferedReader br = FileHelper.readFile(inputFilePath);
            String line;
            while ((line=br.readLine()) != null) {
                String [] arr = line.split("\t");
                String key = arr[0];
                double repeatRate = Double.valueOf(arr[1]);
                keyCountryRateMap.put(key, repeatRate);
            }
            br.close();
        } else {
            System.out.println("[WARNING]country repeat rate file not Exist: " + inputFilePath);
        }

    }

    public static void loadBiData(SiteType siteType) throws SQLException, IOException{
        //language
        System.out.println("initializing RepeatRateProvider...");
        DBHelper dbHelper = DBPool.getCnBiMasterDbHelperWithMarketingRole();
        HashMap<String, Double> keyRateMap = new HashMap<String, Double>();
        HashMap<String, Double> keyCountryRateMap = new HashMap<String, Double>();

        String sql = "select cid, weeks, languages_id, rate30 from DW_REPEAT_RATE_WEEK where customer_count>=" + 100 + " and merchant_id1 = " + siteType.getSiteCode();
        System.out.println(sql);

        ResultSet resultSet = dbHelper.executeQuery(sql);
        while(resultSet.next()){
            int cid = resultSet.getInt(1);
            int week = resultSet.getInt(2);
            int languageId = resultSet.getInt(3);
            LanguageType languageType = LanguageType.getLanguageType(languageId);
            double repeatRate = resultSet.getDouble(4);
            if(repeatRate > MAX_REPEAT_RATE)
                repeatRate = MAX_REPEAT_RATE;
            if(repeatRate < MIN_REPEAT_RATE)
                repeatRate = MIN_REPEAT_RATE;

            String key = getKey(siteType, languageType, cid, week);
            keyRateMap.put(key, repeatRate);

        }
        System.out.println("finish, size: " + keyRateMap.size());

        sql = "select cid, weeks, iso_code, rate30 from DW_REPEAT_RATE_WEEK_CC where customer_count>=" + 100 + " and merchant_id1 = " + siteType.getSiteCode();
        System.out.println(sql);
        resultSet = dbHelper.executeQuery(sql);
        while(resultSet.next()){
            try {
                int cid = resultSet.getInt(1);
                int week = resultSet.getInt(2);
                String countryCode = resultSet.getString(3);
                Country country = Country.valueOf(countryCode);
                double repeatRate = resultSet.getDouble(4);
                if(repeatRate > MAX_REPEAT_RATE)
                    repeatRate = MAX_REPEAT_RATE;
                if(repeatRate < MIN_REPEAT_RATE)
                    repeatRate = MIN_REPEAT_RATE;

                String key = getCountryKey(siteType, country, cid, week);
                keyCountryRateMap.put(key, repeatRate);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        dbHelper.close();
        System.out.println("finish, size: " + keyCountryRateMap.size());

        // output to local
        String outputFilePath = getLanguageRepeatRateFilePath(siteType);
        System.out.println("output to local: " + outputFilePath);
        BufferedWriter bw = FileHelper.writeFile(outputFilePath);
        List<String> keyList = new ArrayList<String>(keyRateMap.keySet());
        Collections.sort(keyList);
        for (String key : keyList) {
            String line = key + "\t" + keyRateMap.get(key);
            bw.append(line + "\n");
        }
        bw.close();

        outputFilePath = getCountryRepeatRateFilePath(siteType);
        System.out.println("output to local: " + outputFilePath);
        bw = FileHelper.writeFile(outputFilePath);
        keyList = new ArrayList<String>(keyCountryRateMap.keySet());
        Collections.sort(keyList);
        for (String key : keyList) {
            String line = key + "\t" + keyCountryRateMap.get(key);
            bw.append(line + "\n");
        }
        bw.close();
    }
//
//	private void init() throws SQLException{
//		System.out.println("initializing RepeatRateProvider...");
//		DBHelper dbHelper = DBPool.getCnBiMasterDbHelperWithMarketingRole();
//
//		String sql = "select merchant_id1, cid, weeks, languages_id, rate30 from DW_REPEAT_RATE_WEEK where customer_count>=" + 100 + " and merchant_id1 = " + siteType.getSiteCode();
//		System.out.println(sql);
//
//		ResultSet resultSet = dbHelper.executeQuery(sql);
//		while(resultSet.next()){
//			SiteType siteType = SiteType.getSiteType(resultSet.getInt(1));
//			int cid = resultSet.getInt(2);
//			int week = resultSet.getInt(3);
//			int languageId = resultSet.getInt(4);
//			LanguageType languageType = LanguageType.getLanguageType(languageId);
//			double repeatRate = resultSet.getDouble(5);
//			if(repeatRate > MAX_REPEAT_RATE)
//				repeatRate = MAX_REPEAT_RATE;
//			if(repeatRate < MIN_REPEAT_RATE)
//				repeatRate = MIN_REPEAT_RATE;
//
//			BingRepeatRateInfo info = new BingRepeatRateInfo(siteType, languageType, null, cid, repeatRate);
//			String key = getKey(siteType, languageType, cid, week);
//			keyRateMap.put(key, info);
//
//		}
//		dbHelper.close();
//
//		System.out.println("finish, size: " + keyRateMap.size());
//	}
//
//	private void initCountrys() throws SQLException{
//		DBHelper dbHelper = DBPool.getCnBiMasterDbHelperWithMarketingRole();
//
//		String sql = "select merchant_id1, cid, weeks, iso_code, rate30 from DW_REPEAT_RATE_WEEK_CC where customer_count>=" + 100 + " and merchant_id1 = " + siteType.getSiteCode();
//		System.out.println(sql);
//
//		ResultSet resultSet = dbHelper.executeQuery(sql);
//		while(resultSet.next()){
//			try {
//				SiteType siteType = SiteType.getSiteType(resultSet.getInt(1));
//				int cid = resultSet.getInt(2);
//				int week = resultSet.getInt(3);
//				String countryCode = resultSet.getString(4);
//				Country country = Country.valueOf(countryCode);
//				double repeatRate = resultSet.getDouble(5);
//				if(repeatRate > MAX_REPEAT_RATE)
//					repeatRate = MAX_REPEAT_RATE;
//				if(repeatRate < MIN_REPEAT_RATE)
//					repeatRate = MIN_REPEAT_RATE;
//
//				BingRepeatRateInfo info = new BingRepeatRateInfo(siteType, null, country, cid, repeatRate);
//				String key = getCountryKey(siteType, country, cid, week);
//				keyCountryRateMap.put(key, info);
//			} catch (Exception e) {
//				// TODO: handle exception
//			}
//		}
//		dbHelper.close();
//
//		System.out.println("finish, size: " + keyCountryRateMap.size());
//	}

    // main
    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
        if (args.length >= 1 && args[0].equals("load")) {
            BingRepeatRateWithWeekProvider.loadBiData(SiteType.litb);
            BingRepeatRateWithWeekProvider.loadBiData(SiteType.mini);
        } else {
            System.out.println("Use parameter 'load' to load data from BI.");
        }
    }
}
