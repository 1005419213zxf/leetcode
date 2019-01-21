package com.litb.bid.component.adw;

import com.litb.bid.DirDef;
import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.adw.lib.enums.AdwordsDevice;
import com.litb.adw.lib.enums.AdwordsReportType;
import com.litb.adw.lib.exception.AdwordsApiException;
import com.litb.adw.lib.exception.AdwordsRemoteException;
import com.litb.adw.lib.exception.AdwordsValidationException;
import com.litb.adw.lib.operation.report.ShoppingAttribute;
import com.litb.adw.lib.util.AdwordsCampaignNameHelper;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;
import com.litb.basic.util.DateHelper;
import com.litb.bid.object.adwreport.ShoppingIntervalReport;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.Scanner;

public class PlaAdwordsMetricProvider extends AdwordsMetricProviderBase implements AdwordsMetricProviderInterface {
	// constructor
	public PlaAdwordsMetricProvider(Date endDate, int interval) throws SQLException, IOException{
		super(endDate, interval);
		System.out.println("initializing " + this.getClass().getSimpleName() + ", end date: " + DateHelper.getShortDateString(endDate) + " " + interval + "...");

		// initialize

		init(endDate, interval, null);
		init(endDate, interval, AdwordsDevice.mobile);		
		System.out.println("after init size(campaign): " + campaignKeyMetricMap.size());
		System.out.println("after init size(summary): " + summaryKeyMetricMap.size());


		// filter
		filter();
		System.out.println("after filter size(campaign): " + campaignKeyMetricMap.size());
		System.out.println("after filter size(summary): " + summaryKeyMetricMap.size());
	}

	// initialize
	private void init(Date endDate, int interval, AdwordsDevice aDevice) throws IOException{
		String InputDir = DirDef.getFormattedReportDir(endDate, AdwordsReportType.shopping_performance, interval
				, aDevice);
		for(String inputFilePath : FileHelper.getFilePathsInOneDir(InputDir)){		
			// for one account
			BufferedReader br = FileHelper.readFile(inputFilePath);
			String line;			
			while((line = br.readLine()) != null){
				ShoppingIntervalReport report = ShoppingIntervalReport.parseFromLine(line);
				ShoppingAttribute attribute = (ShoppingAttribute)report.getAttribute();
				AdwordsChannel thisChannel = AdwordsCampaignNameHelper.getAdwordsChannel(attribute.getCampaignName());
			    if(!thisChannel.equals(AdwordsChannel.pla)){
			    	continue;
			    }
				SiteType siteType = AdwordsCampaignNameHelper.getSiteType(attribute.getCampaignName());
				LanguageType languageType = AdwordsCampaignNameHelper.getLanguageTypeFromCampaignName(attribute.getCampaignName());
				CPTree cpTree = (siteType == SiteType.mini ? miniCpTree : litbCpTree);
				int cid = attribute.getCategoryId(cpTree);
				Long accountId = attribute.getAccountId();
				Long campaignId = attribute.getCampaignId();
				mergeDataToMap(report, attribute, aDevice,thisChannel,siteType,languageType, cid, accountId, campaignId);
			}
			br.close();
		}
	}

	// main for test
	public static void main(String[] args) throws IOException, ParseException, AdwordsValidationException, AdwordsApiException, AdwordsRemoteException {
		int interval = -1;
		Date endDate = null;
		try {
			interval = Integer.parseInt(args[0]);
			if(args.length > 1)
				endDate = DateHelper.getShortDate(args[1]);
			else {
				endDate = DateHelper.getShortDate(DateHelper.getShortDateString(new Date()));
				endDate = DateHelper.addDays(-1, endDate);
			}
		} catch (Exception e) {
			System.err.println("Usage : <interval> <end date(optional)>");
			System.exit(1);
		}
		try {
			PlaAdwordsMetricProvider provider = new PlaAdwordsMetricProvider(endDate, interval);

			System.out.println("Usage : <accountId> <campaignId> <site type> <channel> <language type> <cid> <minAllConversion> <minPcConversion> <minMobileConversion>");
			Scanner in = new Scanner(System.in);
			while (in.hasNext()) {
				String line = in.nextLine();
				String[] arr = line.split(" ");
				int idx = 0;
				try {
					long accountId = Long.parseLong(arr[idx++]);
					long campaignId = Long.parseLong(arr[idx++]);
					// site
					SiteType siteType = SiteType.valueOf(arr[idx++]);

					// channel
					String channelStr = arr[idx++];
					AdwordsChannel channel = null;
					if(channelStr.equals("-1") || channelStr.equals("null"))
						channel = null;
					else
						channel = AdwordsChannel.valueOf(channelStr);

					// language
					String langStr = arr[idx++];
					LanguageType languageType = null;
					if(langStr.equals("-1") || langStr.equals("null"))
						languageType = null;
					else
						languageType = LanguageType.valueOf(langStr);

					// category
					int categoryId = Integer.parseInt(arr[idx++]);

					// threshold
					int minAllConversion = Integer.parseInt(arr[idx++]);
					int minPcConversion = Integer.parseInt(arr[idx++]);
					int minMobileConversion = Integer.parseInt(arr[idx++]);

					AdwordsDeviceMetricInfo info = provider.getDeviceMetric(accountId, campaignId, siteType, channel, languageType, categoryId, 
							minAllConversion, minPcConversion, minMobileConversion);

					System.out.println(info.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
