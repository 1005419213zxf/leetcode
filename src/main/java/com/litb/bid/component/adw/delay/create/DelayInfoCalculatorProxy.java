package com.litb.bid.component.adw.delay.create;

import com.litb.basic.util.DateHelper;
import com.litb.dm.aws.emr.JobFlowPool;
import com.litb.dm.aws.s3.AS3FileHelper;

import java.util.Date;

public class DelayInfoCalculatorProxy {
	public static final String FIRST_STAT_DATE_STRING = "2015-08-01";
	public static final String BUCKET_NAME = "litb.adwords.test";
	
	private static int poolSize = 1;
	private static Date firstStatDate;
	
	public static void main(String[] args) {
		try {
			poolSize = Integer.parseInt(args[0]);
			if(args.length > 1)
				firstStatDate = DateHelper.getShortDate(args[1]);
			else
				firstStatDate = DateHelper.getShortDate(FIRST_STAT_DATE_STRING);
		} catch (Exception e) {
			System.err.println("Usage: <pool size>");
			System.exit(1);
		}
		System.out.println("pool size: " + poolSize);
		
		JobFlowPool jobFlowPool = null;
		try {
			jobFlowPool = new JobFlowPool(poolSize, "delay info calculator job flow pool");
			Date nowDate = new Date();
			Date lastStatDate = DateHelper.addDays(-32, nowDate);
			for(Date date = lastStatDate; !date.before(firstStatDate); date = DateHelper.addDays(-1, date)) {
				if(!AS3FileHelper.isFileExist(BUCKET_NAME, "delayRateInfo_oneDay/" + DelayInfoCalculator.getDateSuffix(date))) {
					jobFlowPool.addStep(new DelayInfoCalculator(date).getStep());
					System.out.println("file NOT exits on date " + DateHelper.getShortDateString(date));
				}
				else {
					System.out.println("file exists on date " + DateHelper.getShortDateString(date));
				}
			}
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
