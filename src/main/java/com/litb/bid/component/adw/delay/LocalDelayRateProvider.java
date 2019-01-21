package com.litb.bid.component.adw.delay;

import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.adw.lib.enums.LitbAdChannel;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

public class LocalDelayRateProvider extends DelayRateProviderBase {

	public LocalDelayRateProvider(SiteType siteType, String localInputDir) throws SQLException, IOException {
		super(siteType);
		System.err.println("initializing " + this.getClass().getName() + "...");

		// scan each file and sum
		for(String inputFilePath : FileHelper.getFilePathsInOneDir(localInputDir)){
			BufferedReader br = new BufferedReader(new FileReader(new File(inputFilePath)));
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
		String localFileDir = null;
		try {
			localFileDir = args[0];
		} catch (Exception e) {
			System.err.println("Usage : <litb local file path> <mini local file path>");
			System.exit(1);
		}
		try {
			LocalDelayRateProvider litbProvider = new LocalDelayRateProvider(SiteType.litb, localFileDir);
			LocalDelayRateProvider miniProvider = new LocalDelayRateProvider(SiteType.mini, localFileDir);
			
			System.out.println("Usage : <site type> <channel> <language type> <cid> <daysInterval> <getOffsetDays> <predictOffsetDays>");
			Scanner in = new Scanner(System.in);
			while (in.hasNext()) {
				String line = in.nextLine();
				String[] arr = line.split(" ");
				int idx = 0;
				try {
					SiteType siteType = SiteType.valueOf(arr[idx++]);
					LocalDelayRateProvider provider = siteType == SiteType.litb ? litbProvider : miniProvider;
					
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
