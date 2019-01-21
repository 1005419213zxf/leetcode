package com.litb.bid.component.adw;

import com.litb.bid.DirDef;
import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;
import com.litb.basic.util.DateHelper;

import java.io.BufferedReader;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class BiYesterdayCpcProvider {
	
	private Map<String, Double> keyCpcMap = new HashMap<String, Double>();
	
	// constructor
	public BiYesterdayCpcProvider(Date endDate) throws SQLException{
		System.out.println("init " + BiYesterdayCpcProvider.class.getSimpleName() + "...");
		String yestodayCpcFilePath = DirDef.getDailyDir(endDate) + "bi_yestoday_cpc";
		try {
			BufferedReader br = FileHelper.readFile(yestodayCpcFilePath);
			String line = null;
			while((line=br.readLine())!=null){
				String[] vals = line.split("\t");
				SiteType siteType = SiteType.getSiteType(Integer.valueOf(vals[0]));
				AdwordsChannel channel = AdwordsChannel.getAdwordsChannel(Integer.valueOf(vals[1]));
				int cid = Integer.valueOf(vals[2]);
				String key = siteType.getSiteCode() + "\t" + channel.getChannelId() + "\t" + cid;
				double cpc = Double.valueOf(vals[3]);
				keyCpcMap.put(key, cpc);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("finish, size: " + keyCpcMap.size());
//		DBHelper dbHelper = DBPool.getCnBiMasterDbHelperWithMarketingRole();
//		String sql = "select merchant_id, channel_id, categories_id, sum(market_cost)/sum(clicks) "
//				+ "from RD_MARKET_COST_PERF t "
//				+ "where platform = 'PC' "
//				+ "and categories_id >= 0 "
//				+ "and channel_id in (127, 8, 81) "
//				+ "and stat_date >= to_date('" + DateHelper.getShortDateString(endDate) + "','yyyy-mm-dd') "
//				+ "and stat_date <= to_date('" + DateHelper.getShortDateString(endDate) + "','yyyy-mm-dd') "
//				+ "group by merchant_id, channel_id, categories_id "
//				+ "having sum(clicks) > 5 "
//				+ "order by merchant_id, channel_id, categories_id ";
//		System.out.println(sql);
//		
//		ResultSet resultSet = dbHelper.executeQuery(sql);
//		while(resultSet.next()){
//			SiteType siteType = SiteType.getSiteType(resultSet.getInt(1));
//			AdwordsChannel channel = AdwordsChannel.getAdwordsChannel(resultSet.getInt(2));
//			int cid = resultSet.getInt(3);
//			if(cid < 0)
//				continue;
//			if(cid == 0)
//				cid = -1;
//			
//			String key = siteType.getSiteCode() + "\t" + channel.getChannelId() + "\t" + cid;
//			double cpc = resultSet.getDouble(4);
//			
//			keyCpcMap.put(key, cpc);
//		}
//		
//		dbHelper.close();
	}
	
	// public methods
	public Double getCpc(SiteType siteType, AdwordsChannel channel, int cid){
		return keyCpcMap.get(siteType.getSiteCode() + "\t" + channel.getChannelId() + "\t" + cid);
	}
	
	// main for test
	public static void main(String[] args) throws SQLException {
		Date endDate = null;
		try {
			if(args.length > 0)
				endDate = DateHelper.getShortDate(args[0]);
			else {
				endDate = DateHelper.getShortDate(DateHelper.getShortDateString(new Date()));
				endDate = DateHelper.addDays(-1, endDate);
			}
		} catch (Exception e) {
			System.err.println("Usage: <end date (optional)>");
			System.exit(1);
		}
		
		BiYesterdayCpcProvider provider = new BiYesterdayCpcProvider(endDate);
		
		Scanner in = new Scanner(System.in);
		System.out.println("input : [Site Type] [channel] [category Id]");
		while(in.hasNext()) {
			try {
				String line = in.nextLine();
				if(line.equals("exit") || line.equals("quit"))
					break;
				
				String[] strArr = line.split(" ");
				SiteType siteType = SiteType.valueOf(strArr[0]);
				AdwordsChannel channel = AdwordsChannel.valueOf(strArr[1]);
				int cid = Integer.parseInt(strArr[2]);
				System.err.println("res:" + provider.getCpc(siteType, channel, cid));
			}
			catch(Exception exception){
				exception.printStackTrace();
				System.out.println("input : [Site Type] [channel] [category Id]");
			}
		}
		in.close();
		System.exit(0);
	}
}
