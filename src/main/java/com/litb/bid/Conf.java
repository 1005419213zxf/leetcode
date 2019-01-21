package com.litb.bid;

import com.litb.basic.db.DBHelper;
import com.litb.basic.db.DBPool;
import com.litb.basic.enums.SiteType;

import java.sql.SQLException;
import java.text.DecimalFormat;

public class Conf {
	public static DecimalFormat BID_DECIMAL_FORMAT = new DecimalFormat("#0.00");
	public static DecimalFormat RATE_DECIMAL_FORMAT = new DecimalFormat("#0.0000");
	// interval
	public static final int[] LOG_INTERVALS = new int[] {90, 28, 14, 7, 1};
	public static final int[] STAT_INTERVALS = new int[] {1, 7, 14, 28, 90, 180, 365, 365 * 2, 3650};
//	public static final int[] CAMPAIGN_STAT_INTERVALS = new int[] {1, 7, 28, 180};
//	public static final int[] CAMPAIGN_DEVICE_STAT_INTERVALS = new int[] {1, 7, 14, 28, 90, 180, 365};
	public static final int[] CATEGORY_SUM_STAT_INTERVALS = new int[] {90};
	
	public static final int[] REPORT_CRITERIA_INTERVALS = new int[] {1, 7, 14, 28, 90, 180, 365, 365 * 2, 3650};
	public static final int[] REPORT_CAMPAIGN_INTERVALS = new int[] {1, 7, 14, 90};
	public static final int CAMPAIGN_SUMMARY_INTEVAL = 14;
	
	public static final int INTERVAL_1_DAYS = 1;
	public static final int INTERVAL_7_DAYS = 7;
	public static final int INTERVAL_14_DAYS = 14;
	public static final int INTERVAL_28_DAYS = 28;
	public static final int INTERVAL_90_DAYS = 90;
	public static final int INTERVAL_180_DAYS = 180;
	public static final int INTERVAL_365_DAYS = 365;
	public static final int INTERVAL_2YEAR_DAYS = 365 * 2;
	public static final int INTERVAL_3650_DAYS = 3650;
	public static final int MAX_INTERVAL_DAYS = INTERVAL_3650_DAYS;
	
	public static final int ROOT_CID = -1;
	public static final int NO_RESULT_ID = -1;
	
	public static final int getIntervalIndex(int interval){
		for(int i = 0; i < STAT_INTERVALS.length; i++){
			if(STAT_INTERVALS[i] == interval)
				return i;
		}
		return -1;
	}
	
	// PPV
	public static final int PPV_DIMENSION = 26;
	
	// database
	public static DBHelper getMcDbHelper(SiteType siteType) throws SQLException{
		try {
			System.out.println("try to getUsMcMainSlaveDbHelper " + siteType);
			return DBPool.getUsMcMainSlaveDbHelper(siteType);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("cannot getUsMcMainSlaveDbHelper! " + siteType);
		}
		try {
			System.out.println("try to getCnMcSlaveL2DbHelper " + siteType);
			return DBPool.getCnMcSlaveL2DbHelper(siteType);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("cannot getCnMcSlaveL2DbHelper! " + siteType);
		}
		try {
			System.out.println("try to getCnMcMasterDbHelper " + siteType);
			return DBPool.getCnMcMasterDbHelper(siteType);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("cannot getCnMcMasterDbHelper! " + siteType);
		}
		
		throw new Error("cannot get MC DB helper!");
	}
	
	// util
	
	public static void main(String[] args) {
		System.out.println(getIntervalIndex(1));
		System.out.println(getIntervalIndex(7));
		System.out.println(getIntervalIndex(14));
		System.out.println(getIntervalIndex(28));
		System.out.println(getIntervalIndex(90));
		System.out.println(getIntervalIndex(180));
		System.out.println(getIntervalIndex(365));
		System.out.println(getIntervalIndex(3650));
		System.out.println(getIntervalIndex(37));
	}
	
}
