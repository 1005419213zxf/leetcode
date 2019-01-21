package com.litb.bid.component.adw.ppv.create;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.litb.basic.util.DateHelper;
import com.litb.dm.aws.s3.AS3FileHelper;

import java.io.File;
import java.util.Date;

public class DBOrderInfoUploader {
	private static Date beginDate;
	private static Date endDate;
	
	public static String getOutputPath(Date date) {
		return "s3://litb.auto.bidding/adwords/db/order/" + DateHelper.getDateString(date, "yyyy") + "/" 
				+ DateHelper.getDateString(date, "MM") + "/" + DateHelper.getShortDateString(date);
	}
	
	public static void main(String[] args) throws AmazonServiceException, AmazonClientException, InterruptedException {
		try {
			beginDate = DateHelper.getShortDate(args[0]);
			endDate = DateHelper.getShortDate(args[1]);
		} catch (Exception e) {
			System.err.println("usage: <begin date> <end date>");
			System.exit(1);
		}
		
		for (Date date=beginDate; !date.after(endDate); date=DateHelper.addDays(1, date)) {
			System.out.println("scanning to date " + DateHelper.getShortDateString(date) + ".");
			String localFile = DBOrderInfoGetter.getLocalFilePath(date);
			String key = getOutputPath(date);
			if (new File(localFile).exists() == true && AS3FileHelper.isFileExist(key) == false) {
				System.out.println("begin to deal with date " + DateHelper.getShortDateString(date) + " ... ...");
				AS3FileHelper.copyLocalFileToS3(localFile, key);
				System.out.println(DateHelper.getShortDateString(date) + " uploaded successfully.");
			}
		}
	}
}
