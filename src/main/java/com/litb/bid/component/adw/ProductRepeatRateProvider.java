package com.litb.bid.component.adw;

import com.litb.basic.db.DBHelper;
import com.litb.basic.db.DBPool;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ProductRepeatRateProvider implements ProductRepeatRateProviderInterface {
	private SiteType siteType;
	private static final double MAX_REPEAT_RATE = 3.0;
	private static final double MIN_REPEAT_RATE = 0.0;
	private static final int MIN_FIRST_ORDER_QTY = 10;
	
	private Map<Integer, Double> keyRateMap = new HashMap<Integer, Double>();
	
	// constructor
	public ProductRepeatRateProvider(SiteType siteType) throws SQLException {
		this.siteType = siteType;
		init();
	}
	
	// public methods
	@Override
	public ProductRepeatRateInfo getRepeatRate(int pid) {
		Double rate = keyRateMap.get(pid);
		if(rate != null){
			if(rate > MAX_REPEAT_RATE)
				rate = MAX_REPEAT_RATE;
			else if(rate < MIN_REPEAT_RATE)
				rate = MIN_REPEAT_RATE;
			return new ProductRepeatRateInfo(siteType, pid, rate);
		}
		else {
			return null;
		}
	}
	
	// private methods
	
	private void init() throws SQLException{
		System.out.println("initializing RepeatRateProvider...");
		DBHelper dbHelper = DBPool.getCnBiMasterDbHelperWithMarketingRole();
		
		String sql = "select lsin, repeat_rate_b from dw_lsin_sales_repeat_rate "
				+ "where first_cust_count >= " + MIN_FIRST_ORDER_QTY + " and merchant_id = " + siteType.getSiteCode();
		System.out.println(sql);

		ResultSet resultSet = dbHelper.executeQuery(sql);
		while(resultSet.next()){
			int pid = Integer.parseInt(resultSet.getString(1).substring(1));
			double rate = resultSet.getDouble(2);
			keyRateMap.put(pid, rate);
		}
		System.out.println("data size: " + keyRateMap.size());
		dbHelper.close();
	}

	// main for test
	public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
		SiteType siteType = SiteType.mini;
		String outputFilePath = "E:\\Download_tmp\\product_repeat_rate_" + siteType;
		DBHelper dbHelper = DBPool.getCnBiMasterDbHelperWithMarketingRole();
		
		String sql = "select lsin, repeat_rate_b from dw_lsin_sales_repeat_rate "
				+ "where first_cust_count >= " + MIN_FIRST_ORDER_QTY + " and merchant_id = " + siteType.getSiteCode();
		System.out.println(sql);

		ResultSet resultSet = dbHelper.executeQuery(sql);
		BufferedWriter bw = FileHelper.writeFile(outputFilePath);
		while(resultSet.next()){
			int pid = Integer.parseInt(resultSet.getString(1).substring(1));
			double rate = resultSet.getDouble(2);
			bw.append(pid + "\t" + rate + "\n");
		}
		bw.close();
		dbHelper.close();
//		ProductRepeatRateProvider litbProvider = new ProductRepeatRateProvider(SiteType.litb);
//		ProductRepeatRateProvider miniProvider = new ProductRepeatRateProvider(SiteType.mini);
//		Scanner in = new Scanner(System.in);
//		System.out.println("input : [Site Type] [product Id]");
//		while(in.hasNext()) {
//			try {
//				String line = in.nextLine();
//				if(line.equals("exit") || line.equals("quit"))
//					break;
//				
//				String[] strArr = line.split(" ");
//				SiteType siteType = SiteType.valueOf(strArr[0]);
//				int pid = Integer.parseInt(strArr[1]);
//				
//				ProductRepeatRateProvider provider = (siteType == SiteType.mini ? miniProvider : litbProvider);
//				ProductRepeatRateInfo repeatRateInfo = provider.getRepeatRate(pid);
//				System.out.println(repeatRateInfo.toString());
//			}
//			catch(Exception exception){
//				exception.printStackTrace();
//				System.out.println("input : [Site Type] [product Id]");
//			}
//		}
//		in.close();
//		System.exit(0);
	}

	
}
