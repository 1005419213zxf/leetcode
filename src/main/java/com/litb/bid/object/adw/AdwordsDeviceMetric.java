package com.litb.bid.object.adw;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.litb.adw.lib.enums.AdwordsDevice;
import com.litb.adw.lib.operation.report.AdwordsMetric;
import com.litb.bid.util.JsonMapper;

import java.io.IOException;

public class AdwordsDeviceMetric {

	private AdwordsMetric pcMetric = new AdwordsMetric();
	private AdwordsMetric mobileMetric = new AdwordsMetric();
	private AdwordsMetric tabletMetric = new AdwordsMetric();
	private AdwordsMetric unknownMetric = new AdwordsMetric();
	
	// public methods
	
	public double getPcCrDivideMobileCr(){
		double pcCr = getNonMobileMetric().getCr();
		double mCr = mobileMetric.getCr();
		return (mCr <= 0 ? 0 : (pcCr / mCr));
	}
	
	public double getPcAosDivideMobileAos(){
      double pcAos = getNonMobileMetric().getAos();
      double mAos = mobileMetric.getAos();
      return (mAos <= 0 ? 0 : (pcAos / mAos));
  }
	
	public void mergeData(AdwordsDevice adwordsDevice, AdwordsMetric metric){
		switch (adwordsDevice) {
		case desktop:
			pcMetric.mergeData(metric);
			break;
		case mobile:
			mobileMetric.mergeData(metric);
			break;
		case tablet:
			tabletMetric.mergeData(metric);
			break;
		default:
			unknownMetric.mergeData(metric);
			break;
		}
	}
	public void mergeMobileAndSubstractPcData(AdwordsMetric metric) {
		mobileMetric.mergeData(metric);
		pcMetric.subtractData(metric);
	}
	
	public AdwordsMetric getAllDeviceMetric(){
		AdwordsMetric allMetric = new AdwordsMetric();
		allMetric.mergeData(pcMetric);
		allMetric.mergeData(mobileMetric);
		allMetric.mergeData(tabletMetric);
		allMetric.mergeData(unknownMetric);
		return allMetric;
	}

	public AdwordsMetric getNonMobileMetric(){
		AdwordsMetric allMetric = new AdwordsMetric();
		allMetric.mergeData(pcMetric);
		allMetric.mergeData(tabletMetric);
		allMetric.mergeData(unknownMetric);
		return allMetric;
	}
	
	@Override
	public String toString(){
		try {
			return JsonMapper.toJsonString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static AdwordsDeviceMetric parse(String line) throws IOException{
		return (AdwordsDeviceMetric)JsonMapper.parseJsonString(line, AdwordsDeviceMetric.class);
	}
	
	public AdwordsMetric getMetric(AdwordsDevice device){
		switch (device) {
		case desktop:
			return pcMetric;
		case mobile:
			return mobileMetric;
		case tablet:
			return tabletMetric;
		case unknown:
			return unknownMetric;
		default:
			throw new IllegalArgumentException("cannot deal with device: " + device);
		}
	}

	// Getters and Setters
	public AdwordsMetric getMobileMetric() {
		return mobileMetric;
	}
	public void setMobileMetric(AdwordsMetric mobileMetric) {
		this.mobileMetric = mobileMetric;
	}
	public AdwordsMetric getPcMetric() {
		return pcMetric;
	}
	public void setPcMetric(AdwordsMetric pcMetric) {
		this.pcMetric = pcMetric;
	}
	public AdwordsMetric getTabletMetric() {
		return tabletMetric;
	}
	public void setTabletMetric(AdwordsMetric tabletMetric) {
		this.tabletMetric = tabletMetric;
	}
	public AdwordsMetric getUnknownMetric() {
		return unknownMetric;
	}
	public void setUnknownMetric(AdwordsMetric unknownMetric) {
		this.unknownMetric = unknownMetric;
	}

	public static void main(String[] args) {
		AdwordsDeviceMetric deviceMetric = new AdwordsDeviceMetric();
		AdwordsMetric aMetric = new AdwordsMetric();
		aMetric.setAllConversions(100);
		deviceMetric.mergeData(AdwordsDevice.desktop, aMetric);
		System.out.println(deviceMetric.pcMetric.getAllConversions());
		deviceMetric.mergeMobileAndSubstractPcData(aMetric);
		System.out.println(deviceMetric.pcMetric.getAllConversions());
		System.out.println(deviceMetric.mobileMetric.getAllConversions());

	}
	
	
}
