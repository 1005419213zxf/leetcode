package com.litb.bid.object.adwreport;

import com.litb.adw.lib.operation.report.ShoppingReport;

public class ShoppingIntervalReport extends ShoppingReport {
	public static final String PREFIX = "rpi_";
	private int interval;
	private boolean onlyMobile;

	// constructor
	private ShoppingIntervalReport(int interval, boolean onlyMobile) {
		this.interval = interval;
		this.onlyMobile = onlyMobile;
	}
	
	// overrider
	public static ShoppingIntervalReport parseFromReport(int interval, boolean onlyMobile, String[] vals){
		ShoppingIntervalReport report = new ShoppingIntervalReport(interval, onlyMobile);
		report.fillDataFromReport(vals);
		return report;
	}
	
	public static ShoppingIntervalReport parseFromLine(String line){
		int index = line.indexOf("\t");
		String[] strArr = line.substring(0, index).split("_");
		int interval = Integer.parseInt(strArr[1]);
		boolean onlyMobile = false;
		if(strArr.length > 2)
			onlyMobile = strArr[2].equals("m");
		ShoppingIntervalReport report = new ShoppingIntervalReport(interval, onlyMobile);
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

	public boolean isOnlyMobile() {
		return onlyMobile;
	}

	public void setOnlyMobile(boolean onlyMobile) {
		this.onlyMobile = onlyMobile;
	}

	// main for test
	public static void main(String[] args) {
		ShoppingIntervalReport report = new ShoppingIntervalReport(7, true);
		String line = report.toString();
		System.out.println(line);
		report = ShoppingIntervalReport.parseFromLine(line);
		System.out.println(report.toString());
		
	}
}
