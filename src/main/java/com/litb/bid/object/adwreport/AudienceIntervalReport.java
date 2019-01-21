package com.litb.bid.object.adwreport;

import com.litb.adw.lib.operation.report.AudienceReport;

public class AudienceIntervalReport extends AudienceReport {
	public static final String PREFIX = "rai_";	// keyword -- "rki_",campaign -- "rci_", so audience -- "rai_"
	private int interval;
	private boolean onlyMobile;

	// constructor
	public AudienceIntervalReport(int interval, boolean onlyMobile) {
		this.interval = interval;
		this.onlyMobile = onlyMobile;
	}
	
	// overrider
	public static AudienceIntervalReport parseFromReport(int interval, boolean onlyMobile, String[] vals){
		AudienceIntervalReport report = new AudienceIntervalReport(interval, onlyMobile);
		report.fillDataFromReport(vals);
		return report;
	}
	
	public static AudienceIntervalReport parseFromLine(String line){
		int index = line.indexOf("\t");
		String[] strArr = line.substring(0, index).split("_");
		int interval = Integer.parseInt(strArr[1]);
		boolean onlyMobile = false;
		if(strArr.length > 2)
			onlyMobile = strArr[2].equals("m");
		AudienceIntervalReport report = new AudienceIntervalReport(interval, onlyMobile);
		report.fillDataFromFormattedLine(line.substring(index + 1));
		return report;
	}
	
	// public methods
	@Override
	public String toString(){
		return PREFIX + interval + (onlyMobile ? "_m" : "_all") + "\t" + super.toString();
	}
	
	// Getters and Setters
	public int getInterval() {
		return interval;
	}
	
	public boolean isOnlyMobile() {
		return onlyMobile;
	}

	public void setOnlyMobile(boolean onlyMobile) {
		this.onlyMobile = onlyMobile;
	}

	// main for test
	public static void main(String[] args) {
//		AudienceIntervalReport report = new AudienceIntervalReport(7);
//		String line = report.toString();
//		System.out.println(line);
//		report = AudienceIntervalReport.parseFromLine(line);
//		System.out.println(report.toString());
		
	}
}
