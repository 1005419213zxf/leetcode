package com.litb.bid.object.adwreport;

import com.litb.adw.lib.operation.report.CampaignPlatformReport;

public class CampaignPlatformIntervalReport extends CampaignPlatformReport {
	public static final String PREFIX = "rcpi_";
	private int interval;

	// constructor
	private CampaignPlatformIntervalReport(int interval) {
		this.interval = interval;
	}
	
	// overrider
	public static CampaignPlatformIntervalReport parseFromReport(int interval, String[] vals){
		CampaignPlatformIntervalReport report = new CampaignPlatformIntervalReport(interval);
		report.fillDataFromReport(vals);
		return report;
	}
	
	public static CampaignPlatformIntervalReport parseFromLine(String line){
		int index = line.indexOf("\t");
		int interval = Integer.parseInt(line.substring(PREFIX.length(), index));
		CampaignPlatformIntervalReport report = new CampaignPlatformIntervalReport(interval);
		report.fillDataFromFormattedLine(line.substring(index + 1));
		return report;
	}
	
	@Override
	public String toString(){
		return PREFIX + interval + "\t" + super.toString();
	}
	
	// Getters and Setters
	public int getInterval() {
		return interval;
	}
	
	// main for test
	public static void main(String[] args) {
		CampaignPlatformIntervalReport report = new CampaignPlatformIntervalReport(7);
		String line = report.toString();
		System.out.println(line);
		report = CampaignPlatformIntervalReport.parseFromLine(line);
		System.out.println(report.toString());
		
		System.out.println("-------");
		line = "rcpi_2	2615402740	361110851	[ES][SRC][LITB][c1180][W&E]<ES> <shopping> <item_target>	active	SHOPPING	Mobile devices with full browsers	5000	699196211__&&__610422	11393	7	9.0	977.0	1908.94	0.0	0	0.0	0	0.0	10.0	1028.0";
		System.out.println(line);
		report = CampaignPlatformIntervalReport.parseFromLine(line);
		System.out.println(report.toString());
		
	}
}
