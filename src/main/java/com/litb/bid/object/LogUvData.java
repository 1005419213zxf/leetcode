package com.litb.bid.object;

import com.litb.adw.lib.util.AdwordsTrackingParser.AdwordsTrackingInfo;

public class LogUvData{
	private AdwordsTrackingInfo trackingInfo;
	
	private String cookie;
	private int startTimeInSecond;
	private boolean isFromMobileDevice;
	
	private int pv;
	private int productVisitNum;
	private int uniqueProductVisitNum;
	private int shoppingCartProductNum;
	private int uniqueShoppingCartProductNum;
	private int internalSearchNum;
	
	private int entranceNum;
	
	// constructors
	
	public LogUvData() {};
	public LogUvData(String cookie, int startTimeInSecond, boolean isFromMobileDevice){
		this.cookie = cookie;
		this.startTimeInSecond = startTimeInSecond;
		this.isFromMobileDevice = isFromMobileDevice;
	}
	
	// public methods
	
	public void increasePv() { pv++; }
	public void increaseProductVisitNum() { productVisitNum++; }
	public void increaseUniqueProductVisitNum() { uniqueProductVisitNum++; }
	public void increaseShoppingCartProductNum() { shoppingCartProductNum++; }
	public void increaseUniqueShoppingCartProductNum() { uniqueShoppingCartProductNum++; }
	public void increaseInternalSearchNum() { internalSearchNum++; }
	public void increaseEntranceNum() { entranceNum++; }
	
	@Override
	public String toString(){
		return trackingInfo.toString() + "\t" + 
				cookie + "\t" + 
				startTimeInSecond + "\t" + 
				isFromMobileDevice + "\t" + 
				
				pv + "\t" + 
				productVisitNum + "\t" + 
				uniqueProductVisitNum + "\t" + 
				shoppingCartProductNum + "\t" + 
				uniqueShoppingCartProductNum + "\t" + 
				internalSearchNum + "\t" + 
				entranceNum;
	}
	
	public static LogUvData parse(String line){
		try {
			LogUvData data = new LogUvData();
			// forwardly parse
			data.trackingInfo = AdwordsTrackingInfo.parse(line);
			
			// reversely parse
			String[] strArr = line.split("\t");
			int index = strArr.length - 1;
			
			data.entranceNum = Integer.parseInt(strArr[index--]);
			data.internalSearchNum = Integer.parseInt(strArr[index--]);
			data.uniqueShoppingCartProductNum = Integer.parseInt(strArr[index--]);
			data.shoppingCartProductNum = Integer.parseInt(strArr[index--]);
			data.uniqueProductVisitNum = Integer.parseInt(strArr[index--]);
			data.productVisitNum = Integer.parseInt(strArr[index--]);
			data.pv = Integer.parseInt(strArr[index--]);
			
			data.isFromMobileDevice = Boolean.parseBoolean(strArr[index--]);
			data.startTimeInSecond = Integer.parseInt(strArr[index--]);
			data.cookie = strArr[index--];
			
			return data;
		} catch (Exception e) {
			System.err.println("Can't be parsed as a LogUvData: " + line);
			e.printStackTrace();
			return null;
		}
	}

	// Getters and Setters
	
	public AdwordsTrackingInfo getTrackingInfo() {
		return trackingInfo;
	}
	public void setTrackingInfo(AdwordsTrackingInfo trackingInfo) {
		this.trackingInfo = trackingInfo;
	}
	public String getCookie() {
		return cookie;
	}
	public void setCookie(String cookie) {
		this.cookie = cookie;
	}
	public int getStartTimeInSecond() {
		return startTimeInSecond;
	}
	public void setStartTimeInSecond(int startTimeInSecond) {
		this.startTimeInSecond = startTimeInSecond;
	}
	public boolean isFromMobileDevice() {
		return isFromMobileDevice;
	}
	public void setFromMobileDevice(boolean isFromMobileDevice) {
		this.isFromMobileDevice = isFromMobileDevice;
	}
	public int getPv() {
		return pv;
	}
	public void setPv(int pv) {
		this.pv = pv;
	}
	public int getProductVisitNum() {
		return productVisitNum;
	}
	public void setProductVisitNum(int productVisitNum) {
		this.productVisitNum = productVisitNum;
	}
	public int getUniqueProductVisitNum() {
		return uniqueProductVisitNum;
	}
	public void setUniqueProductVisitNum(int uniqueProductVisitNum) {
		this.uniqueProductVisitNum = uniqueProductVisitNum;
	}
	public int getShoppingCartProductNum() {
		return shoppingCartProductNum;
	}
	public void setShoppingCartProductNum(int shoppingCartProductNum) {
		this.shoppingCartProductNum = shoppingCartProductNum;
	}
	public int getUniqueShoppingCartProductNum() {
		return uniqueShoppingCartProductNum;
	}
	public void setUniqueShoppingCartProductNum(int uniqueShoppingCartProductNum) {
		this.uniqueShoppingCartProductNum = uniqueShoppingCartProductNum;
	}
	public int getInternalSearchNum() {
		return internalSearchNum;
	}
	public void setInternalSearchNum(int internalSearchNum) {
		this.internalSearchNum = internalSearchNum;
	}
	public int getEntranceNum() {
		return entranceNum;
	}
	public void setEntranceNum(int entranceNum) {
		this.entranceNum = entranceNum;
	}

	// main for test
	public static void main(String[] args) {
		
	}
}
