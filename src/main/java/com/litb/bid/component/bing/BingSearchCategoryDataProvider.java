package com.litb.bid.component.bing;

import com.litb.basic.cptree.CPTree;
import com.litb.basic.enums.Country;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;
import com.litb.bid.BiddingConf;
import com.litb.bid.BingDirDef;
import com.litb.bid.object.bing.BingAggregation;
import com.litb.bid.object.bing.BingDeviceStatItem;
import com.litb.bid.util.CpTreeFactory;
import com.litb.bing.lib.enums.BingChannel;
import com.litb.bing.lib.operation.report.BingMetric;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BingSearchCategoryDataProvider implements BingSearchCategoryDataProviderInterface {

    private static final int INTERVAL = BiddingConf.MAX_INTERVAL_DAYS;
    private Map<String, BingAggregation> keyMap = new HashMap<String, BingAggregation>();
    private CPTree cpTree;

    public BingSearchCategoryDataProvider(SiteType siteType, Date endDate) throws SQLException, IOException {
        String categoryPath = BingDirDef.getLocalBiddableObjectCategoryGroupFilePath(siteType, BingChannel.bing_search, endDate);
        cpTree = CpTreeFactory.getCategoryCpTree(siteType);
        System.out.println("init SearchCategoryDataProvider...");
        BufferedReader br = FileHelper.readFile(categoryPath);
        String line = null;
        while((line=br.readLine())!=null){
            BingAggregation aggregation = BingAggregation.parse(line);
           // System.out.println("put:"+aggregation.getKey());
            if(!keyMap.containsKey(aggregation.getKey()))
                keyMap.put(aggregation.getKey(), aggregation);
        }
        br.close();
        System.out.println("init SearchCategoryDataProvider...done. " + keyMap.size());
    }
//
//    @Override
//    public BingDeviceStatItem[] getCategoryData(int cid, LanguageType languageType) {
//        List<Integer> cidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
//        for(int categoryId : cidList){
//            BingAggregation aggregation = keyMap.get(categoryId + "\t" + languageType);
//            if(aggregation == null)
//                continue;
//            BingDeviceStatItem deviceStatItem = aggregation.getStatItems()[BiddingConf.getIntervalIndex(INTERVAL)];
//            BingMetric metric = deviceStatItem.getAllStatItem().getReportMetric();
//            if(metric.getConversions() >= BiddingConf.EXPECT_CONVERSION && metric.getCr() <= BiddingConf.MAX_CR){
//                return aggregation.getStatItems();
//            }
//        }
//        return null;
//    }
//
//    @Override
//    public BingDeviceStatItem[] getCategoryData(int cid, Country country) {
//
//        List<Integer> cidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
//        for(int categoryId : cidList){
//            BingAggregation aggregation = keyMap.get(categoryId + "\t" + country);
//            if(aggregation == null)
//                continue;
//            BingDeviceStatItem deviceStatItem = aggregation.getStatItems()[BiddingConf.getIntervalIndex(INTERVAL)];
//
//            BingMetric metric = deviceStatItem.getAllStatItem().getReportMetric();
//            if(metric.getConversions() >= BiddingConf.EXPECT_CONVERSION && metric.getCr() <= BiddingConf.MAX_CR){
//                return aggregation.getStatItems();
//            }
//        }
//        return null;
//    }
    @Override
    public BingDeviceStatItem[] getCategoryData(int cid, Country country, LanguageType languageType) {
        List<Integer> cidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
        String countryStr="-1";
        String languageTypeStr="-1";
        if(country!=null){
            countryStr=country.name();
        }else{
            if(languageType!=null){
                languageTypeStr=languageType.name();
            }
        }
        for(int categoryId : cidList){
           // System.out.println("get:"+categoryId + "\t" + countryStr+"\t"+languageTypeStr);
            BingAggregation aggregation = keyMap.get(categoryId + "\t" + countryStr+"\t"+languageTypeStr);

            if(aggregation == null)
                continue;
            //System.out.println("search->  get true:"+categoryId + "\t" + countryStr+"\t"+languageTypeStr);
            BingDeviceStatItem deviceStatItem = aggregation.getStatItems()[BiddingConf.getIntervalIndex(INTERVAL)];

            BingMetric metric = deviceStatItem.getAllStatItem().getReportMetric();
            if(metric.getConversions() >= BiddingConf.EXPECT_CONVERSION && metric.getCr() <= BiddingConf.MAX_CR){
                return aggregation.getStatItems();
            }
        }
        return null;
    }

    @Override
    public BingDeviceStatItem[] getCategoryDataTest(int cid, LanguageType languageType) {
        List<Integer> cidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
        for(int categoryId : cidList){
            BingAggregation aggregation = keyMap.get(categoryId + "\t" + languageType);
            if(aggregation == null)
                continue;
            BingDeviceStatItem deviceStatItem = aggregation.getStatItems()[BiddingConf.getIntervalIndex(BiddingConf.INTERVAL_365_DAYS)];
            BingMetric metric = deviceStatItem.getPcDeviceStatItem().getReportMetric();
            if(metric.getConversions() >= BiddingConf.EXPECT_CONVERSION && metric.getCr() <= BiddingConf.MAX_CR && metric.getClicks() >= 10000){
                return aggregation.getStatItems();
            }
        }
        return null;
    }
}
