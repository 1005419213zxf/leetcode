package com.litb.bid;


import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.enums.Currency;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;
import com.litb.bid.util.ComponentFactory;
import com.litb.bid.util.CpTreeFactory;
import com.litb.bid.util.JsonMapper;
import com.litb.bing.lib.enums.BingChannel;

import java.io.BufferedReader;
import java.sql.SQLException;
import java.util.*;

public class BiddingConf {
    // bidding
    public static int ROI_BIDDING_CLICK_THRESHOLD = 200;
    public static int CATEGORY_BIDDING_CLICK_THRESHOLD = 200;
    public static int PPV_BIDDING_UV_THRESHOLD = 10;
    public static int AOS_CONVERSION_THRESHOLD = 3;

    private static double MAX_BID = 2.00;
    private static double MIN_BID = 0.01;

    public static final double PUNISH_RATIO = 0.7;
    public static final double RECENT_DATA_WEIGHT = 2.0;

    // modifier
    public static double MIN_MOBILE_BID_MODIFIER = 0.1; // minimum: 0.1
    public static double MAX_MOBILE_BID_MODIFIER = 0.7;    // maximum: 4.0
    public static int MIN_MOBILE_MODIFIER_COMPUTE_CONVERSIONS = -1;

    // components
    public static final int DELAY_RATE_STAT_INTERVAL = 30;

    // others
    public static double MIN_PC_CR_DIVIDE_MOBILE_CR = 0.5;
    public static double MAX_PC_CR_DIVIDE_MOBILE_CR = 5.0;

    // new bid
    public static int EXPECT_CONVERSION = 20;//TODO bing biding
    public static int MIN_CLICK_THRESHOLD = 500;
    public static int MAX_CLICK_THRESHOLD = 5000;
    public static int EXPECT_CONVERSION_LOW = 5;
    public static int CR_WINDOW_MIN_CONVERSION = 3 + 1;
    public static int MIN_CLICK_THRESHOLD_LOW = 10;
    public static int MAX_CLICK_THRESHOLD_LOW = 5000;
    public static double MAX_CR = 0.08;

    //todo
    //----------
    public static final int INTERVAL_365_DAYS = 365;
    public static final int INTERVAL_3650_DAYS = 3650;
    public static final int MAX_INTERVAL_DAYS = INTERVAL_3650_DAYS;
    public static final int ROOT_CID = -1;
    public static final int[] STAT_INTERVALS = new int[]{1, 7, 14, 28, 90, 180, 365, 365 * 2, 3650};

    public static final int getIntervalIndex(int interval) {
        for (int i = 0; i < STAT_INTERVALS.length; i++) {
            if (STAT_INTERVALS[i] == interval)
                return i;
        }
        return -1;
    }
    //-----------------

    // exclude account
    public static List<Long> EXCLUDE_ACCOUNT_IDS = Arrays.asList(
            4963536716L,    // 3rd seller affords cost
            1709916982L,    // 0.01 bid keywords
            1927861451L,    // Doogee cell phone requires top place
            //DIS not autobidding
            1574819880L,
            3260096913L,
            6818786823L,
            5923384346L,
            4024531221L,
            5116866368L,
            8618301004L,
            3834656875L,
            4266460321L,
            6381804154L,
            6687733939L,
            1492071330L,
            2572662888L,
            8553474895L,
            7683098059L,
            2006988894L,
            6558287945L,
            5856899132L,
            2105234337L,
            5070183754L
    );

//	public static double getMaxBid(double exchangeRate){
//		return BiddingConf.MAX_BID * exchangeRate;
//	}

    public static double getMaxBid(SiteType siteType, AdwordsChannel channel, int cid, double exchangeRate) throws SQLException {
        double maxBid = BiddingConf.MAX_BID;
        CPTree cpTree = CpTreeFactory.getCategoryCpTree(siteType, false);
        List<Integer> cidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
        if (cidList.contains(77392)) {
            if (channel == AdwordsChannel.pla) {
                maxBid = maxBid + 0.5;
            }
        }
        return maxBid * exchangeRate;
    }

    //for bing bidding
    public static double getMaxBid(SiteType siteType, BingChannel channel, int cid, double exchangeRate) throws SQLException {
        double maxBid = BiddingConf.MAX_BID;
        CPTree cpTree = CpTreeFactory.getCategoryCpTree(siteType, false);
        List<Integer> cidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
        if (cidList.contains(77392)) {
            if (channel == BingChannel.bing_pla) {
                maxBid = maxBid + 0.5;
            }
        }
        return maxBid * exchangeRate;
    }

    public static double getMinBid(SiteType siteType, Currency currency) throws SQLException {
        return ComponentFactory.getCurrencyValueFormatterProvider(siteType).getMinCurrencyValue(currency);
    }

    public static double getTrustModifier(String key, int interval) {
        if (key.contains("self")) {
            if (interval < 28)
                return 1;
            if (interval > 365)
                return 0.85;
            switch (interval) {
                case 28:
                    return 1;
                case 90:
                    return 0.95;
                case 180:
                    return 0.9;
                case 365:
                    return 0.85;
                default:
                    break;
            }
        }
        if (key.contains("lsin")) {
            if (interval < 28)
                return 0.9;
            if (interval > 365)
                return 0.75;
            switch (interval) {
                case 28:
                    return 0.9;
                case 90:
                    return 0.85;
                case 180:
                    return 0.8;
                case 365:
                    return 0.75;
                default:
                    break;
            }
        }
        if (key.contains("campaign")) {
            if (interval < 28)
                return 0.85;
            if (interval > 365)
                return 0.7;
            switch (interval) {
                case 28:
                    return 0.85;
                case 90:
                    return 0.8;
                case 180:
                    return 0.75;
                case 365:
                    return 0.7;
                default:
                    break;
            }
        }
        if (key.contains("cate")) {
            if (interval < 28)
                return 0.8;
            if (interval > 365)
                return 0.65;
            switch (interval) {
                case 28:
                    return 0.8;
                case 90:
                    return 0.75;
                case 180:
                    return 0.7;
                case 365:
                    return 0.65;
                default:
                    break;
            }
        }
        return 1;
    }

    private static String[] addToCartTestKeywordKeys = {
            "8128317248_956332709_51575896314_385202863628",
            "2224351558_957157857_49628948541_406964221863",
            "4140899686_939039509_53675371624_341792560252",
            "3648687164_956337329_54410912131_383437564720",
            "4720400368_956294294_51332651954_294930716533",
            "8128317248_956289794_48159063733_297076636271",
            "5570321816_144064908_9247597308_13593466372",
            "3648687164_957150234_47970974785_298291700793",
            "9863634480_223538508_10253234988_24641441630",
            "7284787399_762558450_40632952299_11076810087"
    };
    public static Set<String> addToCartTestKeywordKeySet = new HashSet<String>(Arrays.asList(addToCartTestKeywordKeys));

    ////////////////////////////////////////////////////////////////
    //因为convertvalue问题，重新下载报告的日期
    public static String[] bugDateStrings = {"2016-08-16", "2016-08-17", "2016-12-17", "2016-12-18", "2016-12-19",
            "2016-12-20", "2016-12-21", "2016-12-22", "2016-12-23", "2016-12-24", "2016-12-25", "2016-12-26", "2016-12-27",
            "2016-12-28", "2016-12-29", "2016-12-30", "2016-12-31", "2017-03-11", "2017-03-12", "2017-03-13", "2017-03-14",
            "2017-03-15", "2017-03-16", "2017-03-17", "2017-03-18", "2017-03-19", "2017-03-20", "2018-09-05"};
    ////////////////////////////////////////////////////////////////

    public static Map<Integer, List<Integer>> categoryExceptionOffsetMap = new HashMap<Integer, List<Integer>>();
    public static int maxCategoryExceptionOffset = 0;

    static {
        try {
            String file = "/mnt/adwords/auto_bidding/abtest/category_offset_exception";
            BufferedReader br = FileHelper.readFile(file);
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] vals = line.split("\t");
                Integer offset = Integer.valueOf(vals[0]);
                String[] cidStrs = vals[1].split(",");
                List<Integer> cidList = new ArrayList<Integer>();
                for (String cidStr : cidStrs)
                    cidList.add(Integer.valueOf(cidStr));
                if (offset > maxCategoryExceptionOffset)
                    maxCategoryExceptionOffset = offset;
                categoryExceptionOffsetMap.put(offset, cidList);
            }
            br.close();
//        	categoryExceptionOffsetMap.put(3, Arrays.asList(1180));
            System.out.println("categoryExceptionOffsetMap: " + JsonMapper.toJsonString(categoryExceptionOffsetMap));
            System.out.println("maxCategoryExceptionOffset: " + maxCategoryExceptionOffset);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}
