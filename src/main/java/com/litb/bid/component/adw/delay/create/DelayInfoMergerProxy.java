package com.litb.bid.component.adw.delay.create;

import com.litb.basic.util.DateHelper;
import com.litb.dm.aws.emr.JobFlowPool;

import java.util.Date;

public class DelayInfoMergerProxy {
	public static final String FIRST_STAT_DATE_STRING = "2015-08-01";
	public static final String BUCKET_NAME = "litb.adwords.test";
	
	private static Date endDate;
	private static int interval;
	
	public static void main(String[] args) {
		try {
			endDate = DateHelper.getShortDate(args[0]);
			interval = Integer.parseInt(args[1]);
			if(DateHelper.getDeltaDays(endDate, new Date()) < 31) {
				System.err.println("days between statDate and getDate is less than 30 days.");
				return;
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("Usage: <end date> <interval>");
			System.exit(1);
		}
		System.out.println("end date: " + DateHelper.getShortDateString(endDate));
		System.out.println("interval: " + interval);
		
		JobFlowPool jobFlowPool = null;
		try {
			jobFlowPool = new JobFlowPool(1, "delay info merger job flow pool");
			jobFlowPool.addStep(new DelayInfoMerger(endDate, interval).getStep());
			boolean isSuccess = jobFlowPool.waitForCompletion();
			if (!isSuccess) {
				jobFlowPool.terminateJobFlows();
				System.err.println("failed!");
				System.exit(1);
			}
			else {
				System.out.println("succeed!");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if(jobFlowPool != null)
				jobFlowPool.terminateJobFlows();
		}
	}
}
