package com.litb.bid.component.adw;


import com.litb.bid.DirDef;
import com.litb.bid.BiddingConf;
import com.litb.bid.Conf;
import com.litb.bid.object.adw.Aggregation;
import com.litb.bid.object.adw.DeviceStatItem;
import com.litb.bid.util.CpTreeFactory;
import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.adw.lib.enums.AdwordsCountry;
import com.litb.adw.lib.operation.report.AdwordsMetric;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;
import com.litb.basic.util.DateHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplayCategoryDataProvider implements DisplayCategoryDataProviderInterface {
	private static final int INTERVAL = Conf.MAX_INTERVAL_DAYS;
	private Map<String, Aggregation> keyMap = new HashMap<String, Aggregation>();
	private CPTree cpTree;
	
	public DisplayCategoryDataProvider(SiteType siteType, Date endDate) throws IOException, SQLException {
		String categoryPath = DirDef.getLocalBiddableObjectCategoryGroupFilePath(siteType, AdwordsChannel.display, endDate);
		cpTree = CpTreeFactory.getCategoryCpTree(siteType);
		System.out.println("init DisplayCategoryDataProvider...");
		BufferedReader br = FileHelper.readFile(categoryPath);
		String line = null;
		while((line=br.readLine())!=null){
			Aggregation aggregation = Aggregation.parse(line);
			if(!keyMap.containsKey(aggregation.getKey()))
				keyMap.put(aggregation.getKey(), aggregation);
		}
		br.close();
		System.out.println("init DisplayCategoryDataProvider...done. " + keyMap.size());
	}
	
	@Override
	public DeviceStatItem[] getCategoryData(int cid, LanguageType languageType) {
		List<Integer> cidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
		for(int categoryId : cidList){
			Aggregation aggregation = keyMap.get(categoryId + "\t" + languageType);
			if(aggregation == null)
				continue;
			DeviceStatItem deviceStatItem = aggregation.getStatItems()[Conf.getIntervalIndex(INTERVAL)];
			AdwordsMetric metric = deviceStatItem.getAllDeviceStatItem().getReportMetric();
			if(metric.getConversions() >= BiddingConf.EXPECT_CONVERSION && metric.getCr() <= BiddingConf.MAX_CR){
				return aggregation.getStatItems();
			}
		}
		return null;
	}

	@Override
	public DeviceStatItem[] getCategoryData(int cid, AdwordsCountry country) {
		List<Integer> cidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
		for(int categoryId : cidList){
			Aggregation aggregation = keyMap.get(categoryId + "\t" + country);
			if(aggregation == null)
				continue;
			DeviceStatItem deviceStatItem = aggregation.getStatItems()[Conf.getIntervalIndex(INTERVAL)];
			AdwordsMetric metric = deviceStatItem.getAllDeviceStatItem().getReportMetric();
			if(metric.getConversions() >= BiddingConf.EXPECT_CONVERSION && metric.getCr() <= BiddingConf.MAX_CR){
				return aggregation.getStatItems();
			}
		}
		return null;
	}

	public static void main(String[] args) throws IOException, SQLException {
		LanguageType languageType = null;
		int cid = 0;
		Date endDate = null;
		try {
			languageType = LanguageType.valueOf(args[0]);
			cid = Integer.valueOf(args[1]);
			endDate = DateHelper.getShortDate(args[2]);
		} catch (Exception e) {
			System.out.println("usage: <languageType> <cid>");
			System.exit(1);
		}
		DisplayCategoryDataProvider searchCategoryDataProvider = new DisplayCategoryDataProvider(SiteType.litb, endDate);
		DeviceStatItem[] deviceStatItems = searchCategoryDataProvider.getCategoryData(cid, languageType);
		Aggregation aggregation = new Aggregation();
		aggregation.setStatItems(deviceStatItems);
		System.out.println(aggregation);
	}

}
