package com.litb.bid.component.adw;

import com.litb.bid.Conf;
import com.litb.bid.util.CpTreeFactory;
import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.db.DBHelper;
import com.litb.basic.db.DBPool;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;
import com.litb.basic.util.DateHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ArpuTrendHistoryBiProvider implements ArpuTrendHistoryBiProviderInterface {
	private static final int CLICKS_THRESHOLD = 2000;
//	private SiteType siteType;
//	private AdwordsChannel channel;
	private CPTree cpTree;
	private static final String DATA_DIR = "/mnt/adwords/auto_bidding/arpu_trend/";
	private Map<String, ArpuTrendBi> categoryIntervalStatArpuMap = new HashMap<String, ArpuTrendBi>();
	private Map<String, ArpuTrendBi> tomorrawCategoryArpuMap = new HashMap<String, ArpuTrendBi>();

	public ArpuTrendHistoryBiProvider(SiteType siteType, AdwordsChannel channel, Date endDate) throws SQLException, IOException {
		super();
//		this.siteType = siteType;
//		this.channel = channel;
		cpTree = CpTreeFactory.getCategoryCpTree(siteType, false);
		String inputFile = getMarketCostArpuFilePath(siteType);
		BufferedReader br = FileHelper.readFile(inputFile);
		String line = null;
		
		Calendar calendar = Calendar.getInstance(DateHelper.US_TIMEZONE, Locale.US);
	    calendar.setTime(endDate);
	    int year = calendar.get(Calendar.YEAR);
	    Date tomorraw = DateHelper.addDays(1, endDate);
	    String tomorrawString = DateHelper.getShortDateString(tomorraw).replaceAll("\\d{4}-", "").replace("-", "");
		while((line=br.readLine())!=null){
			String[] vals = line.split("\t");
			String statDate = vals[0];
			AdwordsChannel adwordsChannel = AdwordsChannel.valueOf(vals[1]);
			if(adwordsChannel != channel)
				continue;
			int categoryId = Integer.valueOf(vals[2]);
			double clicks = Double.valueOf(vals[3]);
			double arpu = Double.valueOf(vals[4]);
			double avgArpu28Days = Double.valueOf(vals[5]);
//			double deltaArpu = Math.abs(arpu - avgArpu28Days);
//			double rate = deltaArpu / avgArpu28Days;
//			if(rate >= 0.5)
//				arpu = avgArpu28Days;
			arpu = avgArpu28Days;
			if(clicks <= 0)
				continue;
			
			if(statDate.equalsIgnoreCase(tomorrawString)){
				ArpuTrendBi arpuTrendBi = tomorrawCategoryArpuMap.get(categoryId+"");
				if(arpuTrendBi == null){
					arpuTrendBi = new ArpuTrendBi();
					tomorrawCategoryArpuMap.put(categoryId+"", arpuTrendBi);
				}
				arpuTrendBi.arpu = (arpuTrendBi.arpu * arpuTrendBi.clicks + arpu * clicks) / (arpuTrendBi.clicks + clicks);
				arpuTrendBi.clicks = arpuTrendBi.clicks + clicks;
			}
			
			Date date = DateHelper.getDate(year+statDate, "yyyyMMdd");
			int offset = DateHelper.getDeltaDays(date, endDate);
			if(offset < 0){
				date = DateHelper.getDate((year-1)+statDate, "yyyyMMdd");
				offset = DateHelper.getDeltaDays(date, endDate);
			}
			for(int interval : Conf.STAT_INTERVALS){
				if(interval >= Conf.MAX_INTERVAL_DAYS)
					continue;
				if(offset < interval){
					ArpuTrendBi arpuTrendBi = categoryIntervalStatArpuMap.get(categoryId + "\t" + interval);
					if(arpuTrendBi == null){
						arpuTrendBi = new ArpuTrendBi();
						categoryIntervalStatArpuMap.put(categoryId + "\t" + interval, arpuTrendBi);
					}
					arpuTrendBi.arpu = (arpuTrendBi.arpu * arpuTrendBi.clicks + arpu * clicks) / (arpuTrendBi.clicks + clicks);
					arpuTrendBi.clicks = arpuTrendBi.clicks + clicks;
				}
			}
		}
		br.close();
	}
	
	public static void loadData(SiteType siteType) throws SQLException, IOException{
		String outputFile = getMarketCostArpuFilePath(siteType);
		
		String sql = "select stat_date, channel_id, categories_id, clicks, arpu, avg_arpu from dw_market_cost_arpu t where t.merchant_id="
				+ siteType.getSiteCode()
				+ " order by t.stat_date desc";
		BufferedWriter bw = FileHelper.writeFile(outputFile);
		DBHelper dbHelper = DBPool.getCnBiMasterDbHelper();
		ResultSet resultSet = dbHelper.executeQuery(sql);
		while(resultSet.next()){
			String statDate = resultSet.getString(1);
			AdwordsChannel channel = AdwordsChannel.getAdwordsChannel(resultSet.getInt(2));
			int categoryId = resultSet.getInt(3);
			if(categoryId <= 0)
				categoryId = -1;
			double clicks = resultSet.getDouble(4);
			double arpu = resultSet.getDouble(5);
			double avgArpu28Days = resultSet.getDouble(6);
			if(clicks <= 0)
				continue;
			bw.append(statDate + "\t" + channel + "\t" + categoryId + "\t" + clicks + "\t" + arpu + "\t" + avgArpu28Days + "\n");
		}
		bw.close();
		resultSet.close();
		dbHelper.close();
	}
	
	private static String getMarketCostArpuFilePath(SiteType siteType) {
		return DATA_DIR + "yearly_market_cost_arpu_" + siteType;
	}

	private static class ArpuTrendBi{
		double clicks;
		double arpu;
	}

	@Override
	public double getIntervalCategoryArpu(int cid, int interval){
		if(interval >= Conf.MAX_INTERVAL_DAYS)
			interval = Conf.INTERVAL_365_DAYS;
		List<Integer> cidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
		if(!cidList.contains(-1))
			cidList.add(-1);
		if(cidList.contains(1181) || cidList.contains(1773)){
			for(int categoryId : cidList){
				ArpuTrendBi arpuTrendBi = categoryIntervalStatArpuMap.get(categoryId + "\t" + interval);
				if(arpuTrendBi != null && arpuTrendBi.clicks >= CLICKS_THRESHOLD)
					return arpuTrendBi.arpu;
			}
		}
		return -1;
	}

	@Override
	public double getTomorrawCategoryArpu(int cid){
		List<Integer> cidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
		if(!cidList.contains(-1))
			cidList.add(-1);
		if(cidList.contains(1181) || cidList.contains(1773)){
			for(int categoryId : cidList){
				ArpuTrendBi arpuTrendBi = tomorrawCategoryArpuMap.get(categoryId+"");
				if(arpuTrendBi != null && arpuTrendBi.clicks >= CLICKS_THRESHOLD)
					return arpuTrendBi.arpu;
			}
		}
		return -1;
	}
	
	public static void main(String[] args) throws SQLException, IOException {
//		Calendar calendar = Calendar.getInstance(DateHelper.US_TIMEZONE, Locale.US);
//	    calendar.setTime(DateHelper.getShortDate("2017-12-18"));
//	    int year = calendar.get(Calendar.YEAR);
//	    System.out.println(year);
		if(args[0].equalsIgnoreCase("load")){
			ArpuTrendHistoryBiProvider.loadData(SiteType.litb);
			ArpuTrendHistoryBiProvider.loadData(SiteType.mini);
			return;
		}
	    
	    ArpuTrendHistoryBiProvider provider = new ArpuTrendHistoryBiProvider(SiteType.litb, AdwordsChannel.search, DateHelper.getShortDate("2017-12-17"));
	    System.out.println(provider.getIntervalCategoryArpu(Integer.valueOf(args[0]), Integer.valueOf(args[1])));
	    System.out.println(provider.getTomorrawCategoryArpu(Integer.valueOf(args[0])));
	}
}
