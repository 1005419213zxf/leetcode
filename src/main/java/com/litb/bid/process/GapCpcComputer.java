package com.litb.bid.process;


import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.adw.lib.enums.AdwordsDevice;
import com.litb.adw.lib.enums.AdwordsReportType;
import com.litb.adw.lib.operation.report.AdwordsMetric;
import com.litb.adw.lib.operation.report.AudienceAttribute;
import com.litb.adw.lib.operation.report.KeywordAttribute;
import com.litb.adw.lib.operation.report.ShoppingAttribute;
import com.litb.basic.io.FileHelper;
import com.litb.basic.util.DateHelper;
import com.litb.bid.DirDef;
import com.litb.bid.object.adwreport.AudienceIntervalReport;
import com.litb.bid.object.adwreport.KeywordIntervalReport;
import com.litb.bid.object.adwreport.ShoppingIntervalReport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class GapCpcComputer implements Runnable{

	private AdwordsChannel channel;
	private Date endDate;
	private int interval; 
	
	public static String getOutputFilePath(AdwordsChannel channel ,Date endDate){
		return DirDef.getDailyDir(endDate) + "gap_" + channel;
	}
	
	public GapCpcComputer(AdwordsChannel channel, Date endDate, int interval) {
		super();
		this.channel = channel;
		this.endDate = endDate;
		this.interval = interval;
	}
	
	private Map<String, BidData> getPcCpcAndBid() throws IOException{
		Map<String, BidData> map = new HashMap<String, BidData>();
		AdwordsReportType reportType = null;
		if(channel == AdwordsChannel.pla)
			reportType = AdwordsReportType.shopping_performance;
		if(channel == AdwordsChannel.search)
			reportType = AdwordsReportType.keyword_performance;
		if(channel == AdwordsChannel.display)
			reportType = AdwordsReportType.audience_performance;
		if(reportType == null)
			return map;
		Map<Integer, Set<String>> pidCriterionSetMap = new HashMap<Integer, Set<String>>();
		
		for(int i = 0; i < interval; i++){
			Date targetDate = DateHelper.addDays(-i, endDate);
			String inputDir = DirDef.getFormattedReportDir(targetDate, reportType, 1, null);
			System.out.println("input: " + inputDir);
			for(String inputFilePath : FileHelper.getFilePathsInOneDir(inputDir)){
				BufferedReader br = FileHelper.readFile(inputFilePath);
				String line;
				while((line = br.readLine()) != null){
					if(channel == AdwordsChannel.pla){
						ShoppingIntervalReport report = ShoppingIntervalReport.parseFromLine(line);
						AdwordsMetric metric = report.getMetric();
						if(metric.getCpc() > 0){
							ShoppingAttribute attribute = (ShoppingAttribute)report.getAttribute();
							String key = attribute.getAccountId() + "\t" + attribute.getCampaignId() + "\t" + attribute.getAdgroupId() + "\t" + attribute.getCriterionId();
							if(map.containsKey(key))
								continue;
							int pid = attribute.getProductId();
							Set<String> set = pidCriterionSetMap.get(pid);
							if(set == null){
								set = new HashSet<String>();
								pidCriterionSetMap.put(pid, set);
							}
							set.add(key);
							BidData bidData = new BidData();
							bidData.setTargetDate(targetDate);
							bidData.setClicks(metric.getClicks());
							bidData.setCost(metric.getCost());
							bidData.setMaxCpc(attribute.getCpcBid());
							map.put(key, bidData);
						}
					}
					if(channel == AdwordsChannel.search){
						KeywordIntervalReport report = KeywordIntervalReport.parseFromLine(line);
						AdwordsMetric metric = report.getMetric();
						if(metric.getCpc() > 0){
							KeywordAttribute attribute = (KeywordAttribute)report.getAttribute();
							String key = attribute.getAccountId() + "\t" + attribute.getCampaignId() + "\t" + attribute.getAdgroupId() + "\t" + attribute.getKeywordId();
							if(map.containsKey(key))
								continue;
							BidData bidData = new BidData();
							bidData.setTargetDate(targetDate);
							bidData.setClicks(metric.getClicks());
							bidData.setCost(metric.getCost());
							bidData.setMaxCpc(attribute.getCpcBid());
							map.put(key, bidData);
						}
					}
					if(channel == AdwordsChannel.display){
						AudienceIntervalReport report = AudienceIntervalReport.parseFromLine(line);
						AdwordsMetric metric = report.getMetric();
						if(metric.getCpc() > 0){
							AudienceAttribute attribute = (AudienceAttribute)report.getAttribute();
							String key = attribute.getAccountId() + "\t" + attribute.getCampaignId() + "\t" + attribute.getAdgroupId() + "\t" + attribute.getCriterionId();
							if(map.containsKey(key))
								continue;
							BidData bidData = new BidData();
							bidData.setTargetDate(targetDate);
							bidData.setClicks(metric.getClicks());
							bidData.setCost(metric.getCost());
							bidData.setMaxCpc(attribute.getCpcBid());
							map.put(key, bidData);
						}
					}
				}
				br.close();
			}
			
			inputDir = DirDef.getFormattedReportDir(targetDate, reportType, 1, AdwordsDevice.mobile);
			System.out.println("input: " + inputDir);
			for(String inputFilePath : FileHelper.getFilePathsInOneDir(inputDir)){
				BufferedReader br = FileHelper.readFile(inputFilePath);
				String line;
				while((line = br.readLine()) != null){
					if(channel == AdwordsChannel.pla){
						ShoppingIntervalReport report = ShoppingIntervalReport.parseFromLine(line);
						AdwordsMetric metric = report.getMetric();
						if(metric.getCpc() > 0){
							ShoppingAttribute attribute = (ShoppingAttribute)report.getAttribute();
							String key = attribute.getAccountId() + "\t" + attribute.getCampaignId() + "\t" + attribute.getAdgroupId() + "\t" + attribute.getCriterionId();
							if(!map.containsKey(key))
								continue;
							BidData bidData = map.get(key);
							if(!DateHelper.getShortDateString(targetDate).equals(DateHelper.getShortDateString(bidData.getTargetDate())))
								continue;
							bidData.setClicks(bidData.getClicks() - metric.getClicks());
							bidData.setCost(bidData.getCost() - metric.getCost());
							if(bidData.getClicks() <= 0)
								map.remove(key);
						}
					}
					if(channel == AdwordsChannel.search){
						KeywordIntervalReport report = KeywordIntervalReport.parseFromLine(line);
						AdwordsMetric metric = report.getMetric();
						if(metric.getCpc() > 0){
							KeywordAttribute attribute = (KeywordAttribute)report.getAttribute();
							String key = attribute.getAccountId() + "\t" + attribute.getCampaignId() + "\t" + attribute.getAdgroupId() + "\t" + attribute.getKeywordId();
							if(!map.containsKey(key))
								continue;
							BidData bidData = map.get(key);
							if(!DateHelper.getShortDateString(targetDate).equals(DateHelper.getShortDateString(bidData.getTargetDate())))
								continue;
							bidData.setClicks(bidData.getClicks() - metric.getClicks());
							bidData.setCost(bidData.getCost() - metric.getCost());
							if(bidData.getClicks() <= 0)
								map.remove(key);
						}
					}
					if(channel == AdwordsChannel.display){
						AudienceIntervalReport report = AudienceIntervalReport.parseFromLine(line);
						AdwordsMetric metric = report.getMetric();
						if(metric.getCpc() > 0){
							AudienceAttribute attribute = (AudienceAttribute)report.getAttribute();
							String key = attribute.getAccountId() + "\t" + attribute.getCampaignId() + "\t" + attribute.getAdgroupId() + "\t" + attribute.getCriterionId();
							if(!map.containsKey(key))
								continue;
							BidData bidData = map.get(key);
							if(!DateHelper.getShortDateString(targetDate).equals(DateHelper.getShortDateString(bidData.getTargetDate())))
								continue;
							bidData.setClicks(bidData.getClicks() - metric.getClicks());
							bidData.setCost(bidData.getCost() - metric.getCost());
							if(bidData.getClicks() <= 0)
								map.remove(key);
						}
					}
				}
				br.close();
			}
		}
		
		for(Entry<Integer, Set<String>> entry : pidCriterionSetMap.entrySet()){
			int pid = entry.getKey();
			if(pid <= 0)
				continue;
			Set<String> criterionSet = entry.getValue();
			long maxClicks = 0;
			BidData selectBidData = null;
			for(String criterionKey : criterionSet){
				BidData bidData = map.get(criterionKey);
				if(bidData == null)
					continue;
				long clicks = bidData.clicks;
				if(clicks > maxClicks){
					selectBidData = bidData;
					maxClicks = clicks;
				}
			}
			if(selectBidData == null)
				continue;
			for(String criterionKey : criterionSet){
				BidData bidData = map.get(criterionKey);
				if(bidData == null)
					continue;
				map.put(criterionKey, selectBidData);
			}
		}
		
		return map;
	}

	private static class BidData{
		private Date targetDate;
		private double cost;
		private long clicks;
		private double maxCpc;
		public double getCost() {
			return cost;
		}
		public void setCost(double cost) {
			this.cost = cost;
		}
		public long getClicks() {
			return clicks;
		}
		public void setClicks(long clicks) {
			this.clicks = clicks;
		}
		public double getMaxCpc() {
			return maxCpc;
		}
		public void setMaxCpc(double maxCpc) {
			this.maxCpc = maxCpc;
		}
		public Date getTargetDate() {
			return targetDate;
		}
		public void setTargetDate(Date targetDate) {
			this.targetDate = targetDate;
		}
	}
	
	@Override
	public void run() {
		try {
			String outputFilePath = getOutputFilePath(channel, endDate);
			System.out.println("output: " + outputFilePath);
			BufferedWriter bw = FileHelper.writeFile(outputFilePath);
			
			Map<String, BidData> map = getPcCpcAndBid();
			for(Entry<String, BidData> entry : map.entrySet()){
				BidData bidData = entry.getValue();
				double maxCpc = bidData.getMaxCpc();
				if(bidData.getClicks() <= 0)
					continue;
				double cpc = bidData.getCost() / bidData.getClicks();
				double gap = maxCpc - cpc;
				if(gap < 0)
					continue;
				bw.append(entry.getKey() + "\t" + gap + "\t" + DateHelper.getShortDateString(bidData.getTargetDate()) + "\t" + 
					maxCpc + "\t" + cpc + "\t" + bidData.getClicks() +  "\n");
			}
			bw.close();
			
//			Set<String> keySet = new HashSet<String>();
//			AdwordsReportType reportType = null;
//			if(channel == AdwordsChannel.pla)
//				reportType = AdwordsReportType.shopping_performance;
//			if(channel == AdwordsChannel.search)
//				reportType = AdwordsReportType.keyword_performance;
//			if(reportType == null)
//				return;
//			
//			for(int i = 0; i < interval; i++){
//				Date targetDate = DateHelper.addDays(-i, endDate);
//				String inputDir = DirDef.getFormattedReportDir(targetDate, reportType, 1, null);
//				System.out.println("input: " + inputDir);
//				for(String inputFilePath : FileHelper.getFilePathsInOneDir(inputDir)){
//					BufferedReader br = FileHelper.readFile(inputFilePath);
//					String line;
//					while((line = br.readLine()) != null){
//						if(channel == AdwordsChannel.pla){
//							ShoppingIntervalReport report = ShoppingIntervalReport.parseFromLine(line);
//							AdwordsMetric metric = report.getMetric();
//							if(metric.getCpc() > 0){
//								ShoppingAttribute attribute = (ShoppingAttribute)report.getAttribute();
//								String key = attribute.getAccountId() + "\t" + attribute.getCampaignId() + "\t" + attribute.getAdgroupId() + "\t" + attribute.getCriterionId();
//								if(keySet.contains(key))
//									continue;
//								double gap = attribute.getCpcBid() - metric.getCpc();
//								if(gap < 0)
//									continue;
//								bw.append(key + "\t" + gap + "\t" + DateHelper.getShortDateString(targetDate) + "\t" + 
//									attribute.getCpcBid() + "\t" + metric.getCpc() + "\t" + metric.getClicks() +  "\n");
//								keySet.add(key);
//							}
//						}
//						if(channel == AdwordsChannel.search){
//							KeywordIntervalReport report = KeywordIntervalReport.parseFromLine(line);
//							AdwordsMetric metric = report.getMetric();
//							if(metric.getCpc() > 0){
//								KeywordAttribute attribute = (KeywordAttribute)report.getAttribute();
//								String key = attribute.getAccountId() + "\t" + attribute.getCampaignId() + "\t" + attribute.getAdgroupId() + "\t" + attribute.getKeywordId();
//								if(keySet.contains(key))
//									continue;
//								double gap = attribute.getCpcBid() - metric.getCpc();
//								if(gap < 0)
//									continue;
//								bw.append(key + "\t" + gap + "\t" + DateHelper.getShortDateString(targetDate) + "\t" + 
//									attribute.getCpcBid() + "\t" + metric.getCpc() + "\t" + metric.getClicks() +  "\n");
//								keySet.add(key);
//							}
//						}
//						if(reportType == null)
//							continue;
//						
//					}
//					br.close();
//				}
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		AdwordsChannel channel = null;
		int interval = 0;
		Date endDate = new Date();
		try {
			channel = AdwordsChannel.valueOf(args[0]);
			interval = Integer.parseInt(args[1]);
			if(args.length > 2)
				endDate = DateHelper.getShortDate(args[2]);
			else{
				endDate = DateHelper.getShortDate(DateHelper.getShortDateString(new Date()));
				endDate = DateHelper.addDays(-1, endDate);
			}
		} catch (Exception e) {
			System.err.println("usage:  <channel> <interval> <end date(option)>");
			System.exit(1);
		}
		System.out.println(channel + "\t" + interval + "\t" + DateHelper.getShortDateString(endDate));
		
		GapCpcComputer computer = new GapCpcComputer(channel, endDate, interval);
		computer.run();

		System.out.println("Done.");
	}
}
