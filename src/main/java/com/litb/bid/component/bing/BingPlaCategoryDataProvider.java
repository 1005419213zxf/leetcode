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

public class BingPlaCategoryDataProvider implements BingPlaCategoryDataProviderInterface {

    private static boolean debug = false;

    private static final int INTERVAL = BiddingConf.MAX_INTERVAL_DAYS;
    private Map<String, BingAggregation> keyMap = new HashMap<String, BingAggregation>();
    private CPTree cpTree;

    public BingPlaCategoryDataProvider(SiteType siteType, Date endDate) throws IOException, SQLException {
        String categoryPath = BingDirDef.getLocalBiddableObjectCategoryGroupFilePath(siteType, BingChannel.bing_pla, endDate);
        cpTree = CpTreeFactory.getCategoryCpTree(siteType, false);
        System.out.println("init PlaCategoryDataProvider...");
        BufferedReader br = FileHelper.readFile(categoryPath);
        String line = null;
        while((line=br.readLine())!=null){
            BingAggregation aggregation = BingAggregation.parse(line);
           // System.out.println("put:"+aggregation.getKey());
            if(!keyMap.containsKey(aggregation.getKey()))
                keyMap.put(aggregation.getKey(), aggregation);
        }
        br.close();
        System.out.println("init PlaCategoryDataProvider...done. " + keyMap.size());
    }
    @Override
    public BingDeviceStatItem[] getCategoryData(int cid , Country country, LanguageType languageType) {
        List<Integer> cidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
        if(!cidList.contains(BiddingConf.ROOT_CID))
            cidList.add(BiddingConf.ROOT_CID);
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
            BingDeviceStatItem deviceStatItem = aggregation.getStatItems()[BiddingConf.getIntervalIndex(INTERVAL)];
            BingMetric metric = deviceStatItem.getAllStatItem().getReportMetric();
            if(metric.getConversions() >= BiddingConf.EXPECT_CONVERSION && metric.getCr() <= BiddingConf.MAX_CR){
                if(debug)
                    System.out.println("target cid: " + categoryId);
                return aggregation.getStatItems();
            }
        }
        return null;
    }

//    @Override
//    public BingDeviceStatItem[] getCategoryDataTest( int cid ,Country country, LanguageType languageType) {
//        List<Integer> cidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
//        if(!cidList.contains(BiddingConf.ROOT_CID))
//            cidList.add(BiddingConf.ROOT_CID);
//
//        for(int categoryId : cidList){
//            for(Country feedCountry : new Country[]{country, null}){
//                String key = categoryId + "\t" + (feedCountry != null ? feedCountry : "-1");
//                BingAggregation aggregation = keyMap.get(key);
//                if(aggregation == null)
//                    continue;
//                BingDeviceStatItem deviceStatItem = aggregation.getStatItems()[BiddingConf.getIntervalIndex(INTERVAL)];
//                BingMetric metric = deviceStatItem.getPcDeviceStatItem().getReportMetric();
//                if(metric.getConversions() >= BiddingConf.EXPECT_CONVERSION && metric.getCr() <= BiddingConf.MAX_CR && metric.getClicks() >= 10000){
//                    if(debug)
//                        System.out.println("target cid: " + categoryId);
//                    return aggregation.getStatItems();
//                }
//            }
//        }
//        return null;
//    }


}
