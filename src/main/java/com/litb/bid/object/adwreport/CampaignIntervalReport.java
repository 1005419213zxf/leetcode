package com.litb.bid.object.adwreport;

import com.litb.adw.lib.operation.report.CampaignReport;

public class CampaignIntervalReport extends CampaignReport {
	public static final String PREFIX = "rci_";
	private int interval;
	private boolean onlyMobile;

	// constructor
	private CampaignIntervalReport(int interval, boolean onlyMobile) {
		this.interval = interval;
		this.onlyMobile = onlyMobile;
	}
	
	// overrider
	public static CampaignIntervalReport parseFromReport(int interval, boolean onlyMobile, String[] vals){
		CampaignIntervalReport report = new CampaignIntervalReport(interval, onlyMobile);
		report.fillDataFromReport(vals);
		return report;
	}
	
	public static CampaignIntervalReport parseFromLine(String line){
		int index = line.indexOf("\t");
		String[] strArr = line.substring(0, index).split("_");
		int interval = Integer.parseInt(strArr[1]);
		boolean onlyMobile = false;
		if(strArr.length > 2)
			onlyMobile = strArr[2].equals("m");
		CampaignIntervalReport report = new CampaignIntervalReport(interval, onlyMobile);
		report.fillDataFromFormattedLine(line.substring(index + 1));
		return report;
	}
	
	@Override
	public String toString(){
		return PREFIX + interval + (onlyMobile ? "_m" : "_all") + "\t" + super.toString();
	}
	
	// Getters and Setters
	public int getInterval() {
		return interval;
	}
	
	// main for test
	public static void main(String[] args) {
		CampaignIntervalReport report = new CampaignIntervalReport(7, true);
		String line = report.toString();
		System.out.println(line);
		report = CampaignIntervalReport.parseFromLine(line);
		System.out.println(report.toString());
		
	}
}
