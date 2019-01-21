package com.litb.bid.component.adw;

import com.litb.bid.DirDef;
import com.litb.bid.BiddingConf;
import com.litb.bid.Conf;
import com.litb.bid.object.adw.DeviceStatItem;
import com.litb.bid.object.adw.Aggregation;
import com.litb.bid.util.CpTreeFactory;
import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.adw.lib.enums.FeedCountry;
import com.litb.adw.lib.operation.report.AdwordsMetric;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;
import com.litb.basic.util.DateHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class PlaCategoryDataProvider implements PlaCategoryDataProviderInterface{
	private static boolean debug = false;
	
	private static final int INTERVAL = Conf.MAX_INTERVAL_DAYS;
	private Map<String, Aggregation> keyMap = new HashMap<String, Aggregation>();
	private CPTree cpTree;
	
	public PlaCategoryDataProvider(SiteType siteType, Date endDate) throws IOException, SQLException {
		String categoryPath = DirDef.getLocalBiddableObjectCategoryGroupFilePath(siteType, AdwordsChannel.pla, endDate);
		cpTree = CpTreeFactory.getCategoryCpTree(siteType, false);
		System.out.println("init PlaCategoryDataProvider...");
		BufferedReader br = FileHelper.readFile(categoryPath);
		String line = null;
		while((line=br.readLine())!=null){
			Aggregation aggregation = Aggregation.parse(line);
			if(!keyMap.containsKey(aggregation.getKey()))
				keyMap.put(aggregation.getKey(), aggregation);
		}
		br.close();
		System.out.println("init PlaCategoryDataProvider...done. " + keyMap.size());
	}

	@Override
	public DeviceStatItem[] getCategoryData(FeedCountry country, int cid) {
		List<Integer> cidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
		if(!cidList.contains(Conf.ROOT_CID))
          cidList.add(Conf.ROOT_CID);
		
		for(int categoryId : cidList){
			Aggregation aggregation = keyMap.get(categoryId + "\t" + country);
			if(aggregation == null)
				continue;
			DeviceStatItem deviceStatItem = aggregation.getStatItems()[Conf.getIntervalIndex(INTERVAL)];
			AdwordsMetric metric = deviceStatItem.getAllDeviceStatItem().getReportMetric();
			if(metric.getConversions() >= BiddingConf.EXPECT_CONVERSION && metric.getCr() <= BiddingConf.MAX_CR){
				if(debug)
					System.out.println("target cid: " + categoryId);
				return aggregation.getStatItems();
			}
		}
		return null;
	}
	
	@Override
    public DeviceStatItem[] getCategoryDataTest(FeedCountry country, int cid) {
        List<Integer> cidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
        if(!cidList.contains(Conf.ROOT_CID))
          cidList.add(Conf.ROOT_CID);
        
        for(int categoryId : cidList){
            for(FeedCountry feedCountry : new FeedCountry[]{country, null}){
              String key = categoryId + "\t" + (feedCountry != null ? feedCountry : "-1");
              Aggregation aggregation = keyMap.get(key);
              if(aggregation == null)
                  continue;
              DeviceStatItem deviceStatItem = aggregation.getStatItems()[Conf.getIntervalIndex(INTERVAL)];
              AdwordsMetric metric = deviceStatItem.getPcDeviceStatItem().getReportMetric();
              if(metric.getConversions() >= BiddingConf.EXPECT_CONVERSION && metric.getCr() <= BiddingConf.MAX_CR && metric.getClicks() >= 10000){
                  if(debug)
                      System.out.println("target cid: " + categoryId);
                  return aggregation.getStatItems();
              }
            }
        }
        return null;
    }
	
	public static void main(String[] args) throws IOException, SQLException {
		Date endDate = null;
		try {
			endDate = DateHelper.getShortDate(args[0]);
		} catch (Exception e) {
			System.err.println("Usage: <end date>");
			System.exit(1);
		}
		PlaCategoryDataProvider.debug = true;
		PlaCategoryDataProvider litbProvider = new PlaCategoryDataProvider(SiteType.litb, endDate);
		PlaCategoryDataProvider miniProvider = new PlaCategoryDataProvider(SiteType.mini, endDate);
		
		Scanner in = new Scanner(System.in);
		System.out.println("input : [Site Type] [country code] [category Id]");
		while(in.hasNext()) {
			try {
				String line = in.nextLine();
				if(line.equals("exit") || line.equals("quit"))
					break;
				
				String[] strArr = line.split(" ");
				SiteType siteType = SiteType.valueOf(strArr[0]);
				FeedCountry country = null;
				if(!strArr[1].equals("null"))
					country = FeedCountry.valueOf(strArr[1]);
				int cid = Integer.parseInt(strArr[2]);
				
				PlaCategoryDataProvider provider = (siteType == SiteType.mini ? miniProvider : litbProvider);
				DeviceStatItem[] resArr = provider.getCategoryData(country, cid);
				System.out.println("result");
				if(resArr == null)
					System.out.println("null");
				else {
					for(DeviceStatItem item : resArr)
						System.out.println(item.toString());
				}
			}
			catch(Exception exception){
				System.out.println("input : [Site Type] [country code] [category Id]");
			}
		}
		in.close();
		System.exit(0);
	}
}
