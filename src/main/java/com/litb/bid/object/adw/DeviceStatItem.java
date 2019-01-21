package com.litb.bid.object.adw;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.litb.adw.lib.operation.report.AdwordsMetric;
import com.litb.bid.object.LogMetric;

public class DeviceStatItem {
	private static final String SEPERATOR = "_@@_";
	
	private StatItem mobileStatItem;
	private StatItem allDeviceStatItem;
	
	// constructor
	private DeviceStatItem() {}
	public DeviceStatItem(int interval){
		mobileStatItem = new StatItem(interval);
		allDeviceStatItem = new StatItem(interval);
	}
	
	// public methods
	public void mergeData(DeviceStatItem intervalStatItem){
		// merge all device data
		this.allDeviceStatItem.getReportMetric().mergeData(intervalStatItem.allDeviceStatItem.getReportMetric());
		this.allDeviceStatItem.getLogMetric().mergeData(intervalStatItem.allDeviceStatItem.getLogMetric());
		// merge mobile device data
		this.mobileStatItem.getReportMetric().mergeData(intervalStatItem.mobileStatItem.getReportMetric());
		this.mobileStatItem.getLogMetric().mergeData(intervalStatItem.mobileStatItem.getLogMetric());
	}
	
	@Override
	public String toString(){
		return allDeviceStatItem.toString() + SEPERATOR + mobileStatItem.toString();
	}
	
	public static DeviceStatItem parse(String line){
		try {
			DeviceStatItem item = new DeviceStatItem();
			String[] strArr = line.split(SEPERATOR);
			item.allDeviceStatItem = StatItem.parse(strArr[0]);
			item.mobileStatItem = StatItem.parse(strArr[1]);
			return item;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(line);
			return null;
		}
	}
	
	// Getters and Setters
	
	@JsonIgnore
	public int getInterval(){
		return allDeviceStatItem.getInterval();
	}
	
	public StatItem getMobileStatItem() {
		return mobileStatItem;
	}
	public void setMobileStatItem(StatItem mobileStatItem) {
		this.mobileStatItem = mobileStatItem;
	}
	public StatItem getAllDeviceStatItem() {
		return allDeviceStatItem;
	}
	public void setAllStatItem(StatItem allStatItem) {
		this.allDeviceStatItem = allStatItem;
	}
	
	public StatItem getPcDeviceStatItem() {
		StatItem pcStatItem = new StatItem(getInterval());
		AdwordsMetric allAdwordsMetric = allDeviceStatItem.getReportMetric();
		pcStatItem.getReportMetric().mergeData(allAdwordsMetric);
		AdwordsMetric mAdwordsMetric = mobileStatItem.getReportMetric();
		pcStatItem.getReportMetric().mergeData(mAdwordsMetric, -1);
		AdwordsMetric pcAdwordsMetric = pcStatItem.getReportMetric();
		if(pcAdwordsMetric.getClicks() < 0 || pcAdwordsMetric.getConversions() < 0 || pcAdwordsMetric.getCost() < 0)
			pcStatItem.setReportMetric(allAdwordsMetric);
		
		LogMetric allLogMetric = allDeviceStatItem.getLogMetric();
		pcStatItem.getLogMetric().mergeData(allLogMetric);
		return pcStatItem;
	}
	
	// main for test
	public static void main(String[] args) {
		DeviceStatItem intervalStatItem = new DeviceStatItem();
		intervalStatItem.allDeviceStatItem = new StatItem(7);
		intervalStatItem.allDeviceStatItem.getReportMetric().setClicks(10);
		intervalStatItem.mobileStatItem = new StatItem(7);
		intervalStatItem.mobileStatItem.getReportMetric().setClicks(12);
		
		String line = intervalStatItem.toString();
		System.out.println(line);
		intervalStatItem = DeviceStatItem.parse(line);
		System.out.println(intervalStatItem.toString());
		
		System.out.println(intervalStatItem.getPcDeviceStatItem().toString());
	}
}
