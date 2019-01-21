package com.litb.bid.component.adw.ppv.create;

import com.litb.basic.enums.SiteType;
import com.litb.basic.util.DateHelper;
import com.litb.dm.aws.emr.JobFlowPool;

import java.util.Date;

/*
 * this class is used to mark whether a LogUvData has order,
 * if a uv has successful order(s) in the next 30 days, the output will be uv.toString() + "\t1",
 * else the output will be uv.toString() + "\t0".
 * the order informantion is from S3, which is uploaded by another program.
 * 
 * this program is run after class "LogDailyProcessorEunner" succeeded in generate the pre data.
 */
public class PpvUvMarkerRunner {
	private static SiteType siteType;
	private static Date endDate;
	private static int interval;
	
	public static void main(String[] args) throws InterruptedException {
		try {
			siteType = SiteType.valueOf(args[0]);
			endDate = DateHelper.getShortDate(args[1]);
			interval = Integer.parseInt(args[2]);
		} catch (Exception e) {
			System.err.println("Usage: <site type> <end date> <interval>");
			System.exit(1);
		}
		
		JobFlowPool pool = new JobFlowPool(1, "ab ppv uv mark test");
		pool.addStep(new PpvUvMarker(siteType, endDate, interval).getStep());
		boolean isSuccess = pool.waitForCompletion();
		System.out.println("res: " + isSuccess);
		pool.terminateJobFlows();
		
		System.out.println("Done.");
	}
}
