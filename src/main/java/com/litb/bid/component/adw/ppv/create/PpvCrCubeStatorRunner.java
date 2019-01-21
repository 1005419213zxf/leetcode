package com.litb.bid.component.adw.ppv.create;

import com.litb.basic.enums.SiteType;
import com.litb.basic.util.DateHelper;
import com.litb.dm.aws.emr.JobFlowPool;

import java.util.Date;

public class PpvCrCubeStatorRunner {
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
		
		JobFlowPool pool = new JobFlowPool(1, "ab ppv cr cube test");
		pool.addStep(new PpvCrCubeStator(siteType, endDate, interval).getStep());
		boolean isSuccess = pool.waitForCompletion();
		System.out.println("res: " + isSuccess);
		pool.terminateJobFlows();
		
		System.out.println("Done.");
	}
}
