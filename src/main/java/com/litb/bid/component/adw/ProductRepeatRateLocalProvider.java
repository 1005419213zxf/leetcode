package com.litb.bid.component.adw;

import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ProductRepeatRateLocalProvider implements ProductRepeatRateProviderInterface {
	private SiteType siteType;
	private static final double MAX_REPEAT_RATE = 3.0;
	private static final double MIN_REPEAT_RATE = 0.0;
//	private static final int MIN_FIRST_ORDER_QTY = 10;
	
	private Map<Integer, Double> keyRateMap = new HashMap<Integer, Double>();
	
	// constructor
	public ProductRepeatRateLocalProvider(SiteType siteType) throws IOException {
		this.siteType = siteType;
		init();
	}
	
	// input
	private static String getInputFilePath(SiteType siteType){
		return "/mnt/adwords/auto_bidding/components/product_repeat/product_repeat_rate_" + siteType;
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
	
	private void init() throws IOException {
		String inputFilePath = getInputFilePath(siteType);
		System.out.println("initializing RepeatRateProvider from " + inputFilePath + " ...");
		BufferedReader br = FileHelper.readFile(inputFilePath);
		String line;
		while((line = br.readLine()) != null){
			try {
				String[] strArr = line.split("\t");
				int pid = Integer.parseInt(strArr[0]);
				double rate = Double.parseDouble(strArr[1]);
				keyRateMap.put(pid, rate);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		br.close();
		System.out.println("finish, size: " + keyRateMap.size());
	}

	// main for test
	public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
//		SiteType siteType = SiteType.mini;
//		String outputFilePath = "E:\\Download_tmp\\product_repeat_rate_" + siteType;
//		DBHelper dbHelper = DBPool.getCnBiMasterDbHelperWithMarketingRole();
//		
//		String sql = "select lsin, repeat_rate_b from dw_lsin_sales_repeat_rate "
//				+ "where first_cust_count >= " + MIN_FIRST_ORDER_QTY + " and merchant_id = " + siteType.getSiteCode();
//		System.out.println(sql);
//
//		ResultSet resultSet = dbHelper.executeQuery(sql);
//		BufferedWriter bw = FileHelper.writeFile(outputFilePath);
//		while(resultSet.next()){
//			int pid = Integer.parseInt(resultSet.getString(1).substring(1));
//			double rate = resultSet.getDouble(2);
//			bw.append(pid + "\t" + rate + "\n");
//		}
//		bw.close();
//		dbHelper.close();
		ProductRepeatRateLocalProvider litbProvider = new ProductRepeatRateLocalProvider(SiteType.litb);
		ProductRepeatRateLocalProvider miniProvider = new ProductRepeatRateLocalProvider(SiteType.mini);
		Scanner in = new Scanner(System.in);
		System.out.println("input : [Site Type] [product Id]");
		while(in.hasNext()) {
			try {
				String line = in.nextLine();
				if(line.equals("exit") || line.equals("quit"))
					break;
				
				String[] strArr = line.split(" ");
				SiteType siteType = SiteType.valueOf(strArr[0]);
				int pid = Integer.parseInt(strArr[1]);
				
				ProductRepeatRateLocalProvider provider = (siteType == SiteType.mini ? miniProvider : litbProvider);
				ProductRepeatRateInfo repeatRateInfo = provider.getRepeatRate(pid);
				System.out.println(repeatRateInfo.toString());
			}
			catch(Exception exception){
				exception.printStackTrace();
				System.out.println("input : [Site Type] [product Id]");
			}
		}
		in.close();
		System.exit(0);
	}

	
}
