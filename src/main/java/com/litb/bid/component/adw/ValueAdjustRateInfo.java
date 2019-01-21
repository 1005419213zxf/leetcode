package com.litb.bid.component.adw;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.bid.util.JsonMapper;

import java.io.IOException;

/**
 * The value adjustment related data, including refund, offline payment etc.<br/>
 * 
 * <b>Value adjust ratio</b>: [0, infinite) adjustment ratio considering refund, order process fee... but offline payment.<br/>
 * <b>Offline adjust ratio</b>: [1, infinite) (online + offline) / online sales.<br/>
 * <b>Total adjust ratio</b>: [0, infinite) adjustment ratio considering refund, order process fee ... and offline payment.<br/>
 * 
 * @author Rui Zhang
 */
public class ValueAdjustRateInfo {
	private SiteType siteType;
    private LanguageType langType;
    private int cid = -1;
    
    private double valueAdjRatio = 1;
    private double offlineAdjRatio = 1;
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

    public static ValueAdjustRateInfo parse(String line) throws IOException {
        return (ValueAdjustRateInfo)JsonMapper.parseJsonString(line, ValueAdjustRateInfo.class);
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

	public double getValueAdjRatio() {
		return valueAdjRatio;
	}

	public void setValueAdjRatio(double valueAdjRatio) {
		this.valueAdjRatio = valueAdjRatio;
	}

	public double getOfflineAdjRatio() {
		return offlineAdjRatio;
	}

	public void setOfflineAdjRatio(double offlineAdjRatio) {
		this.offlineAdjRatio = offlineAdjRatio;
	}

	public double getTotalAdjRatio() {
		return totalAdjRatio;
	}

	public void setTotalAdjRatio(double totalAdjRatio) {
		this.totalAdjRatio = totalAdjRatio;
	}
 
	// main for test
	public static void main(String[] args) throws IOException {
		ValueAdjustRateInfo info = new ValueAdjustRateInfo();
		String line = info.toString();
		System.out.println(line);
		
		info = ValueAdjustRateInfo.parse(line);
		System.out.println(info.toString());
	}
}

