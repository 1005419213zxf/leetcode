package com.litb.bid.component.adw.ppv.create;

import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.basic.util.DateHelper;

import java.util.Date;

/* this class is a basic item used to describe the order information,
 * it is used in class "PpvUvMarkerRunner" as a basic class.
 */
public class DBOrderItem{
	public static final int SEGMENT_SIZE = 9;
	
	private int orderId;
	private SiteType siteType;
	private LanguageType languageType;
	private String cookie;
	private Date datePurchased;
	private Date dateConfirmed;
	private String platform;
	private double orderTotal;
	private String currency;
	
	// public methods
	
	@Override
	public String toString(){
		return orderId + "\t" + 
				(siteType == null ? "-" : siteType.getSiteCode()) + "\t" + 
				(languageType == null ? "-" : languageType.getLanguageId()) + "\t" + 
				(cookie == null ? "-" : cookie) + "\t" + 
				(datePurchased == null ? "-" : DateHelper.getTimeInSeconds(datePurchased)) + "\t" + 
				(dateConfirmed == null ? "-" : DateHelper.getTimeInSeconds(dateConfirmed)) + "\t" + 
				(platform == null ? "-" : platform) + "\t" + 
				orderTotal + "\t" + 
				(currency == null ? "-" : currency);
		
	}
	
	public static DBOrderItem parse(String line){
		DBOrderItem item = new DBOrderItem();
		String[] strArr = line.split("\t");
		int index = 0;
		String str = strArr[index++];
		item.orderId = Integer.parseInt(str);
		str = strArr[index++];
		item.siteType = (str.equals("-") ? null : SiteType.getSiteType((Integer.parseInt(str))));
		str = strArr[index++];
		item.languageType = (str.equals("-") ? null : LanguageType.getLanguageType(Integer.parseInt(str)));
		str = strArr[index++];
		item.cookie = (str.equals("-") ? null :str);
		str = strArr[index++];
		item.datePurchased = (str.equals("-") ? null : DateHelper.getDateFromSeconds(Integer.parseInt(str)));
		str = strArr[index++];
		item.dateConfirmed = (str.equals("-") ? null : DateHelper.getDateFromSeconds(Integer.parseInt(str)));
		str = strArr[index++];
		item.platform = (str.equals("-") ? null :str);
		str = strArr[index++];
		item.orderTotal = Double.parseDouble(str);
		str = strArr[index++];
		item.currency = (str.equals("-") ? null :str);
		
		return item;
	}

	
	// Getters and Setters
	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}


	public SiteType getSiteType() {
		return siteType;
	}

	public void setSiteType(SiteType siteType) {
		this.siteType = siteType;
	}

	public LanguageType getLanguageType() {
		return languageType;
	}

	public void setLanguageType(LanguageType languageType) {
		this.languageType = languageType;
	}

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public Date getDatePurchased() {
		return datePurchased;
	}

	public void setDatePurchased(Date datePurchased) {
		this.datePurchased = datePurchased;
	}

	public Date getDateConfirmed() {
		return dateConfirmed;
	}

	public void setDateConfirmed(Date dateConfirmed) {
		this.dateConfirmed = dateConfirmed;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public double getOrderTotal() {
		return orderTotal;
	}

	public void setOrderTotal(double orderTotal) {
		this.orderTotal = orderTotal;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}
	
	public static void main(String[] args) {
		DBOrderItem item = new DBOrderItem();
		item.setSiteType(SiteType.litb);
		System.out.println(item.toString());
		
		System.out.println(DBOrderItem.parse("11020386	7	3	000000002D91A951762F4133024C1E0C	1370070006	1370070354	www	33.86	EUR").toString());
	}
}

