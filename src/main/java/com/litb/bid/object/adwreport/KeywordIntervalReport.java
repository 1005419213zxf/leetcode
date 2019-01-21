package com.litb.bid.object.adwreport;

import com.litb.adw.lib.operation.report.KeywordReport;

public class KeywordIntervalReport extends KeywordReport {
	public static final String PREFIX = "rki_";
	private int interval;
	private boolean onlyMobile;
	
	// constructor
	private KeywordIntervalReport(int interval, boolean onlyMobile) {
		this.interval = interval;
		this.onlyMobile = onlyMobile;
	}
	
	// overrider
	public static KeywordIntervalReport parseFromReport(int interval, boolean onlyMobile, String[] vals){
		KeywordIntervalReport report = new KeywordIntervalReport(interval, onlyMobile);
		report.fillDataFromReport(vals);
		return report;
	}
	
	public static KeywordIntervalReport parseFromLine(String line){
		int index = line.indexOf("\t");
		String[] strArr = line.substring(0, index).split("_");
		int interval = Integer.parseInt(strArr[1]);
		boolean onlyMobile = false;
		if(strArr.length > 2)
			onlyMobile = strArr[2].equals("m");
		KeywordIntervalReport report = new KeywordIntervalReport(interval, onlyMobile);
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
		KeywordIntervalReport report = new KeywordIntervalReport(7, false);
		String line = report.toString();
		System.out.println(line);
		report = KeywordIntervalReport.parseFromLine(line);
		System.out.println(report.toString());
		
		line = "rki_7	0	0	-	-	0	-	-	0	-	-	-	0	-	0	0	0__&&__0	0	0	0.0	0.0	0.0	-1.0	0	0.0	0	0.0	0.0	0.0";
		System.out.println(line);
		report = KeywordIntervalReport.parseFromLine(line);
		System.out.println(report.toString());
		
	}
}
