package com.litb.bid.component.adw;

import com.litb.bid.DirDef;
import com.litb.bid.BiddingConf;
import com.litb.bid.Conf;
import com.litb.bid.object.adw.DeviceStatItem;
import com.litb.bid.object.adw.Aggregation;
import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.adw.lib.operation.report.AdwordsMetric;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;
import com.litb.basic.util.DateHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CampaignDataProvider{
	private static final int INTERVAL = Conf.MAX_INTERVAL_DAYS;
	private Map<String, Aggregation> keyMap = new HashMap<String, Aggregation>();
	
	public CampaignDataProvider(SiteType siteType, AdwordsChannel channel, Date endDate) throws IOException, SQLException {
		String campaignPath = DirDef.getLocalBiddableObjectCampaignGroupFilePath(siteType, channel, endDate);
		System.out.println("init CamaignDataProvider...");
		BufferedReader br = FileHelper.readFile(campaignPath);
		String line = null;
		while((line=br.readLine())!=null){
			Aggregation aggregation = Aggregation.parse(line);
			if(!keyMap.containsKey(aggregation.getKey()))
				keyMap.put(aggregation.getKey(), aggregation);
		}
		br.close();
		System.out.println("init CampaignDataProvider...done. " + keyMap.size());
	}
	
	public DeviceStatItem[] getCampaignData(long accountId, long campaignId) {
		Aggregation aggregation = keyMap.get(accountId + "\t" + campaignId);
		if(aggregation == null)
			return null;
		DeviceStatItem deviceStatItem = aggregation.getStatItems()[Conf.getIntervalIndex(INTERVAL)];
		AdwordsMetric metric = deviceStatItem.getAllDeviceStatItem().getReportMetric();
		if(metric.getConversions() >= BiddingConf.EXPECT_CONVERSION && metric.getCr() <= BiddingConf.MAX_CR){
			return aggregation.getStatItems();
		}
		return null;
	}

	public static void main(String[] args) throws IOException, SQLException {
		long accoutId = -1L;
		long campaignId = -1L;
		Date endDate = null;
		try {
			accoutId = Long.valueOf(args[0]);
			campaignId = Long.valueOf(args[1]);
			endDate = DateHelper.getShortDate(args[2]);
		} catch (Exception e) {
			System.out.println("usage: accountid campaignid enddate");
			System.exit(1);
		}
		CampaignDataProvider searchCategoryDataProvider = new CampaignDataProvider(SiteType.litb, AdwordsChannel.search, endDate);
		DeviceStatItem[] deviceStatItems = searchCategoryDataProvider.getCampaignData(accoutId, campaignId);
		Aggregation aggregation = new Aggregation();
		aggregation.setStatItems(deviceStatItems);
		System.out.println(aggregation);
	}

}
