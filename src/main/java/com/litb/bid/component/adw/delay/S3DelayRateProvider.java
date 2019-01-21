package com.litb.bid.component.adw.delay;

import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.adw.lib.enums.LitbAdChannel;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.basic.util.DateHelper;
import com.litb.bid.BiddingConf;
import com.litb.bid.component.adw.delay.create.DelayInfoMerger;
import com.litb.dm.aws.s3.AS3FileHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class S3DelayRateProvider extends DelayRateProviderBase{
	private static final String S3_BUCKET = "s3://litb.adwords.test/";

	public S3DelayRateProvider(SiteType siteType) throws SQLException, IOException {
		super(siteType);
		System.err.println("initializing " + this.getClass().getName() + "...");
		
		// find target AS3 input directory
		Date nowDate = new Date();
		Date statDate = null;
		for(Date date=DateHelper.addDays(-31, nowDate); date.after(DateHelper.getShortDate("2015-08-01")); date=DateHelper.addDays(-1, date)) {
			String inputPath = DelayInfoMerger.getAs3OutputDir(date, BiddingConf.DELAY_RATE_STAT_INTERVAL);
			if(AS3FileHelper.isFileExist(inputPath)) {
				statDate = date;
				break;
			}
		}
		if(statDate == null) {
			throw new NullPointerException("can not find the result from delay rate merger since 2015-08-01");
		} else {
			System.out.println("get the latest delay rate merger result on date " + DateHelper.getShortDateString(statDate));
		}
		
		// scan each file and sum
		String s3Dir = DelayInfoMerger.getAs3OutputDir(statDate, BiddingConf.DELAY_RATE_STAT_INTERVAL);
		System.out.println("input dir: " + s3Dir);
		List<String> inputS3FilePathList = AS3FileHelper.getObjectList(s3Dir);
		for(String inputS3FilePath : inputS3FilePathList){
			BufferedReader br = AS3FileHelper.readFile(S3_BUCKET + inputS3FilePath);
			initMap(br);
			br.close();
		}
		System.out.println("key size(before refine): " + keyConvValueArrMap.size());
		
		// refine
		refindData();
		System.out.println("finish, size(after refine): " + keyConvValueArrMap.size());
	}
	
	// main for test
	public static void main(String[] args) {
		try {
			S3DelayRateProvider litbProvider = new S3DelayRateProvider(SiteType.litb);
			S3DelayRateProvider miniProvider = new S3DelayRateProvider(SiteType.mini);
			
			System.out.println("Usage : <site type> <channel> <language type> <cid> <daysInterval> <getOffsetDays> <predictOffsetDays>");
			Scanner in = new Scanner(System.in);
			while (in.hasNext()) {
				String line = in.nextLine();
				String[] arr = line.split(" ");
				int idx = 0;
				try {
					SiteType siteType = SiteType.valueOf(arr[idx++]);
					S3DelayRateProvider provider = siteType == SiteType.litb ? litbProvider : miniProvider;
					AdwordsChannel adwordsChannel = AdwordsChannel.valueOf(arr[idx++]);
					LitbAdChannel channel = null;
					if (adwordsChannel == AdwordsChannel.search) 
						channel = LitbAdChannel.adwords_search;
					else if (adwordsChannel == AdwordsChannel.pla)
						channel = LitbAdChannel.adwords_pla;
					LanguageType languageType = null;
					try { languageType = LanguageType.valueOf(arr[idx]); } catch (Exception e) {} finally { idx ++; }
					int categoryId = Integer.parseInt(arr[idx++]);
					int daysInterval = Integer.parseInt(arr[idx++]);
					int getOffsetDays = Integer.parseInt(arr[idx++]);
					int predictOffsetDays = Integer.parseInt(arr[idx++]);
					
					DelayRateInfo info = provider.getDelayRate(channel, siteType, languageType, categoryId, daysInterval, getOffsetDays, predictOffsetDays);
					System.out.println(info.getDelayRate());
				} catch (Exception e) {
					System.err.println("Usage : <site type> <channel> <language type> <cid> <daysInterval> <getOffsetDays> <predictOffsetDays>");
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
