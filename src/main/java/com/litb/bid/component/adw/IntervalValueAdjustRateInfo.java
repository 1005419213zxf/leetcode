package com.litb.bid.component.adw;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.bid.util.JsonMapper;

import java.io.IOException;

public class IntervalValueAdjustRateInfo {
	private SiteType siteType;
    private LanguageType langType;
    private int cid = -1;
    private int interval;
    
    private double receiveSales;
    private double gmGA;
    private double gmFix;
    private double totalAdjRatio = 1;

    // public methods
	@Override
	public String toString() {
		try {
			return JsonMapper.toJsonString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return  null;
		}
	}

    public static IntervalValueAdjustRateInfo parse(String line) throws IOException {
        return (IntervalValueAdjustRateInfo)JsonMapper.parseJsonString(line, IntervalValueAdjustRateInfo.class);
	}

    // Getters and Setters
	public SiteType getSiteType() {
		return siteType;
	}

	public void setSiteType(SiteType siteType) {
		this.siteType = siteType;
	}

	public LanguageType getLangType() {
		return langType;
	}

	public void setLangType(LanguageType languageType) {
		this.langType = languageType;
	}

	public int getCid() {
		return cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}
	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public double getReceiveSales() {
		return receiveSales;
	}

	public void setReceiveSales(double receiveSales) {
		this.receiveSales = receiveSales;
	}

	public double getGmGA() {
		return gmGA;
	}

	public void setGmGA(double gmGA) {
		this.gmGA = gmGA;
	}

	public double getGmFix() {
		return gmFix;
	}

	public void setGmFix(double gmFix) {
		this.gmFix = gmFix;
	}

	public double getTotalAdjRatio() {
		return totalAdjRatio;
	}

	public void setTotalAdjRatio(double totalAdjRatio) {
		this.totalAdjRatio = totalAdjRatio;
	}
 
	// main for test
	public static void main(String[] args) throws IOException {
		IntervalValueAdjustRateInfo info = new IntervalValueAdjustRateInfo();
		String line = info.toString();
		System.out.println(line);
		
		info = IntervalValueAdjustRateInfo.parse(line);
		System.out.println(info.toString());
	}
}

