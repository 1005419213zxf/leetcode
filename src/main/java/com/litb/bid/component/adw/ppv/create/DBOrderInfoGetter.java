package com.litb.bid.component.adw.ppv.create;

import com.litb.basic.db.DBHelper;
import com.litb.basic.db.DBPool;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;
import com.litb.basic.util.DateHelper;
import com.litb.basic.util.TextHelper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class DBOrderInfoGetter {
	public static String getLocalFilePath(Date date){
		return getLocalFileDir(date) + DateHelper.getShortDateString(date);
	}
	private static String getLocalFileDir(Date date){
		return "/home/adplat/wangshihao/ppv/order/" + 
				DateHelper.getDateString(date, "yyyy") + "/" + DateHelper.getDateString(date, "MM") + "/";
	}
	
	private static Date beginDate;
	private static Date endDate;
	
	public static void main(String[] args) throws IOException, SQLException {
		try {
			beginDate = DateHelper.getShortDate(args[0]);
			endDate = DateHelper.getShortDate(args[1]);
		} catch (Exception e) {
			System.err.println("Usage: <begin date> <end date>");
			System.exit(1);
		}
		
		DBHelper dbHelper = DBPool.getCnPcSlaveL1DbHelper();
		
		for(Date date = beginDate; !date.after(endDate); date = DateHelper.addDays(1, date)){
			String outputDir = getLocalFileDir(date);
			System.out.println("output dir: " + outputDir);
			String outputFilePath = getLocalFilePath(date);
			System.out.println("output: " + outputFilePath);
			
			if(!FileHelper.isFileExist(outputDir))
				FileHelper.createDirectory(outputDir);
			BufferedWriter bw = FileHelper.writeFile(outputFilePath);
			
			String sql = "select orders_id, merchant_id, languages_id, orders_user_cookie, "
					+ "date_purchased, order_payment_confirm_time, orders_referral, order_total, currency_code from v3_orders "
					+ "where date_purchased >= '" + DateHelper.getCNDateString(date, "yyyy-MM-dd HH:mm:ss") + "' "
					+ "and date_purchased < '" + DateHelper.getCNDateString(DateHelper.addDays(1, date), "yyyy-MM-dd HH-mm-ss") + "' "
					+ "and order_payment_confirm_time > '1900-01-01' "
					+ "and order_type_id<>2 "
					+ "and orders_user_cookie is not null";
			
			
			ResultSet result = dbHelper.executeQuery(sql);
			int totCnt = 0;
			while(result.next()){
				totCnt ++;
				int orderId = result.getInt(1);
				int merchantId = result.getInt(2);
				int languageId = result.getInt(3);
				String cookie = null;
				try {
					cookie = TextHelper.base64ToOriginalCookie(result.getString(4));
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				Date datePurchased = result.getTimestamp(5);
				Date dateConfirmed = result.getTimestamp(6);
				String referral = result.getString(7).replaceAll("\t", "");
				String platform = "www";
				if(referral.toLowerCase().contains("VelaOrderChannel:mobile_web".toLowerCase()))
					platform = "m";
				double orderTotal = result.getDouble(8);
				String currency = result.getString(9);
				
				SiteType siteType = null;
				try {
					siteType = SiteType.getSiteType(merchantId);
				} catch (Exception e) {
					System.err.println("Cannot deal with merchant id: " + merchantId);
					continue;
				}
				
				DBOrderItem item = new DBOrderItem();
				item.setOrderId(orderId);
				item.setSiteType(siteType);
				item.setLanguageType(LanguageType.getLanguageType(languageId));
				item.setCookie(cookie);
				item.setDatePurchased(datePurchased);
				item.setDateConfirmed(dateConfirmed);
				item.setPlatform(platform);
				item.setOrderTotal(orderTotal);
				item.setCurrency(currency);
				
				String output = item.toString();
				bw.append(output + "\n");
			}
			System.out.println("total count : " + totCnt);
			bw.close();
			
		}
		
		dbHelper.close();
		
		System.out.println("Done");
	}
}
