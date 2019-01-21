package com.litb.bid.object.adwreport;

import com.litb.adw.lib.operation.report.OriginalShoppingReport;

public class ShoppingOriginalIntervalReport extends OriginalShoppingReport {
	public static final String PREFIX = "rsoi_";
	private int interval;
	private boolean onlyMobile;

	// constructor
	private ShoppingOriginalIntervalReport(int interval, boolean onlyMobile) {
		this.interval = interval;
		this.onlyMobile = onlyMobile;
	}
	
	public ShoppingOriginalIntervalReport() {
		super();
		// TODO Auto-generated constructor stub
	}

	// overrider
	public static ShoppingOriginalIntervalReport parseFromReport(int interval, boolean onlyMobile, String[] vals){
		ShoppingOriginalIntervalReport report = new ShoppingOriginalIntervalReport(interval, onlyMobile);
		report.fillDataFromReport(vals);
		return report;
	}
	
	public static ShoppingOriginalIntervalReport parseFromLine(String line){
		int index = line.indexOf("\t");
		String[] strArr = line.substring(0, index).split("_");
		int interval = Integer.parseInt(strArr[1]);
		boolean onlyMobile = false;
		if(strArr.length > 2)
			onlyMobile = strArr[2].equals("m");
		ShoppingOriginalIntervalReport report = new ShoppingOriginalIntervalReport(interval, onlyMobile);
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

	public void setInterval(int interval) {
		this.interval = interval;
	}
	
	// main for test
	public static void main(String[] args) {
		ShoppingOriginalIntervalReport report = new ShoppingOriginalIntervalReport(7, false);
		String line = report.toString();
		System.out.println(line);
		report = ShoppingOriginalIntervalReport.parseFromLine(line);
		System.out.println(report.toString());
		
		line = "rsoi_7	7948279828	211812674	[ES][SRC][MINI][c2624][ELG] <ES> <shopping> (miniinthebox)	active	1385233_0_es_es	--__&&__12	1	0	0.0	0.0	0.15	-1.0	0	0.0	0	0.0	0.0	0.0";
		System.out.println(line);
		report = ShoppingOriginalIntervalReport.parseFromLine(line);
		System.out.println(report.toString());
		
	}
}
