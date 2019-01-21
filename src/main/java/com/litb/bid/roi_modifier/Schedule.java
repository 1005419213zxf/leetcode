package com.litb.bid.roi_modifier;

import com.litb.basic.util.DateHelper;

import java.util.Date;

public class Schedule {

	private static Date endDate;
//	private static final SiteType[] TARGET_SITE_TYPES = new SiteType[] { SiteType.litb, SiteType.mini};
//	private static final AdwordsChannel[] TARGET_CHANNELS = new AdwordsChannel[] { AdwordsChannel.pla, AdwordsChannel.search, AdwordsChannel.display };
	
	public static void main(String[] args) throws Exception {
		try {
			if (args.length > 0) {
				endDate = DateHelper.getShortDate(args[0]);
			}
			else{
				endDate = DateHelper.getShortDate(DateHelper.getShortDateString(new Date()));
				endDate = DateHelper.addDays(-1, endDate);
			}
		} catch (Exception e) {
			System.err.println("Usage: <endDate(optional)>");
			System.exit(1);
		}
		
		// aggregate
//		for(SiteType siteType : TARGET_SITE_TYPES){
//			for(AdwordsChannel channel : TARGET_CHANNELS){
//				ActualRoiDailyComputer dailyComputer = new ActualRoiDailyComputer(siteType, endDate, channel);
//			}
//		}
		
		System.out.println("Done.");
	}
}
