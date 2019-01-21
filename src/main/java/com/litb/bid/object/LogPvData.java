package com.litb.bid.object;

import com.litb.adw.lib.util.AdwordsTrackingParser;
import com.litb.adw.lib.util.AdwordsTrackingParser.AdwordsTrackingInfo;
import com.litb.basic.log.obj.HTTPMethod;
import com.litb.basic.log.obj.LogInfo;
import com.litb.basic.util.DateHelper;
import com.litb.basic.util.LitbUrlParser;

import java.util.Date;

public class LogPvData {
	private AdwordsTrackingInfo trackingInfo;

	private HTTPMethod httpMethod;
	private short responseCode = -1;
	private boolean isMobile = false;
	private Date visitTime;
	
	private int pid = -1;
	private boolean isProductVisit = false;;
	private boolean isAddToCart = false;
	private boolean isInternalSearch = false;

	// public methods
	
	public static LogPvData parseFromLog(LogInfo logInfo){
		try {
			LogPvData data = new LogPvData();
			
			String visitPage = logInfo.getVisitPage();
			if(!LitbUrlParser.isFromLitbGroupWebSite(logInfo.getRefererPage()))
				data.trackingInfo = AdwordsTrackingParser.getTrankingInfoFromLandingUrl(visitPage);
			
			data.httpMethod = logInfo.getHttpMethod();
			data.responseCode = logInfo.getResponseCode();
			data.isMobile = logInfo.isFromMobileDevice();
			data.visitTime = logInfo.getVisitTime();
			
			int pid = LitbUrlParser.getProductID(visitPage);
			if(pid > 0){
				data.isProductVisit = true;
				data.pid = pid;
			}
			
			pid = LitbUrlParser.getAddToCartPid(visitPage);
			if(pid >= 0){
				data.isAddToCart = true;
				data.pid = pid;
			}
			
			data.isInternalSearch = LitbUrlParser.isInternalSearch(visitPage);
			
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}

	@Override
	public String toString() {
		return (trackingInfo == null ? "-" : trackingInfo.toString()) + "\t" + 

				httpMethod.getId() + "\t" + 
				responseCode + "\t" + 
				isMobile + "\t" + 
				(int) (visitTime.getTime() / 1000) + "\t" +
				
				pid + "\t" + 
				isProductVisit + "\t" + 
				isAddToCart + "\t" + 
				isInternalSearch;
	}

	public static LogPvData parse(String line) {
		try {
			LogPvData pvData = new LogPvData();
			if(!line.startsWith("-"))
				pvData.trackingInfo = AdwordsTrackingInfo.parse(line);
			
			// reversely parse
			String[] strArray = line.split("\t");
			int index = strArray.length - 1;
			pvData.isInternalSearch = Boolean.parseBoolean(strArray[index--]);
			pvData.isAddToCart = Boolean.parseBoolean(strArray[index--]);
			pvData.isProductVisit = Boolean.parseBoolean(strArray[index--]);
			pvData.pid = Integer.parseInt(strArray[index--]);
			
			pvData.visitTime = DateHelper.getDateFromSeconds(Integer.parseInt(strArray[index--]));
			pvData.isMobile = Boolean.parseBoolean(strArray[index--]);
			pvData.responseCode = Short.parseShort(strArray[index--]);
			pvData.httpMethod = HTTPMethod.getHttpMethod(Integer.parseInt(strArray[index--]));
			
			return pvData;
		} catch (Exception e) {
			System.err.println("Can't be parsed as a PvData: " + line);
			e.printStackTrace();
			return null;
		}
	}


	// Getters and Setters

	public int getVisitTimeInSecond() {
		return (int) (visitTime.getTime() / 1000);
	}

	public AdwordsTrackingInfo getTrackingInfo() {
		return trackingInfo;
	}

	public void setTrackingInfo(AdwordsTrackingInfo trackingInfo) {
		this.trackingInfo = trackingInfo;
	}

	public short getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(short responseCode) {
		this.responseCode = responseCode;
	}

	public HTTPMethod getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(HTTPMethod httpMethod) {
		this.httpMethod = httpMethod;
	}

	public Date getVisitTime() {
		return visitTime;
	}

	public void setVisitTime(Date visitTime) {
		this.visitTime = visitTime;
	}

	public boolean isProductVisit() {
		return isProductVisit;
	}

	public void setProductVisit(boolean isProductVisit) {
		this.isProductVisit = isProductVisit;
	}

	public boolean isMobile() {
		return isMobile;
	}

	public void setMobile(boolean isMobile) {
		this.isMobile = isMobile;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public boolean isAddToCart() {
		return isAddToCart;
	}

	public void setAddToCart(boolean isAddToCart) {
		this.isAddToCart = isAddToCart;
	}

	public boolean isInternalSearch() {
		return isInternalSearch;
	}

	public void setInternalSearch(boolean isInternalSearch) {
		this.isInternalSearch = isInternalSearch;
	}

	public static void main(String[] args) {

		LogPvData pvData = new LogPvData();
		pvData.trackingInfo = null;

		pvData.httpMethod = HTTPMethod.GET;
		pvData.visitTime = new Date();

		System.out.println(pvData.toString());
		System.out.println(LogPvData.parse("-	-1	1	1384312914	0	0	-	0").toString());
	}
}
