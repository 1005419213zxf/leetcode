package com.litb.bid.component.adw;

import com.litb.bid.Conf;
import com.litb.bid.object.adw.AdwordsDeviceMetric;
import com.litb.bid.util.CpTreeFactory;
import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.adw.lib.enums.AdwordsDevice;
import com.litb.adw.lib.exception.AdwordsApiException;
import com.litb.adw.lib.exception.AdwordsRemoteException;
import com.litb.adw.lib.exception.AdwordsValidationException;
import com.litb.adw.lib.operation.report.AdwordsAttribute;
import com.litb.adw.lib.operation.report.AdwordsMetric;
import com.litb.adw.lib.operation.report.AdwordsReport;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.basic.util.DateHelper;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;

public class AdwordsMetricProviderBase implements AdwordsMetricProviderInterface {
	private static final int DEFAULT_ID = -1;
	private static final int MIN_CLICK = 2000;


	protected CPTree litbCpTree;
	protected CPTree miniCpTree;

	protected Map<String, AdwordsDeviceMetric> campaignKeyMetricMap = new HashMap<String, AdwordsDeviceMetric>();
	protected Map<String, AdwordsDeviceMetric> summaryKeyMetricMap = new HashMap<String, AdwordsDeviceMetric>();

	// constructor
	public AdwordsMetricProviderBase(Date endDate, int interval) throws SQLException, IOException{
		System.out.println("initializing " + this.getClass().getSimpleName() + ", end date: " + DateHelper.getShortDateString(endDate) + " " + interval + "...");
		this.litbCpTree = CpTreeFactory.getCategoryCpTree(SiteType.litb);
		this.miniCpTree = CpTreeFactory.getCategoryCpTree(SiteType.mini);

	}

	// public methods

	@Override
	public AdwordsDeviceMetricInfo getDeviceMetric(long accountId, long campaignId, 
			SiteType siteType, AdwordsChannel channel, LanguageType languageType, int categoryId,
			int minAllDeviceConversions, int minPcConverisons, int minMobileConversions) {
		// try to find campaign data
		if(accountId > 0 && campaignId > 0){
			AdwordsDeviceMetric metric = campaignKeyMetricMap.get(getCampaignKey(accountId, campaignId));
			if(metric != null){
				boolean isOk = true;
				if(minPcConverisons > 0 && metric.getPcMetric().getConversions() < minPcConverisons)
					isOk = false;
				else if(minMobileConversions > 0 && metric.getMobileMetric().getConversions() < minMobileConversions)
					isOk = false;
				else if(minAllDeviceConversions > 0 && metric.getAllDeviceMetric().getConversions() < minAllDeviceConversions)
					isOk = false;
				if(isOk){
					AdwordsDeviceMetricInfo info = new AdwordsDeviceMetricInfo();
					info.setCampaignData(true);
					info.setAccountId(accountId);
					info.setCampaignId(campaignId);
					info.setDeviceMetric(metric);
					return info;
				}
			}
		}
		// try to find summary data
		CPTree cpTree = (siteType == SiteType.mini ? miniCpTree : litbCpTree);
		List<Integer> pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(categoryId);
		if(!pcidList.contains(Conf.ROOT_CID))
			pcidList.add(Conf.ROOT_CID);

		for(AdwordsChannel ch : Arrays.asList(channel, null)){
			for(LanguageType lang : Arrays.asList(languageType, null)){
				for(int pcid : pcidList){
					String key = getSummaryKey(siteType, ch, lang, pcid);
					AdwordsDeviceMetric metric = summaryKeyMetricMap.get(key);
					if(metric != null){
						boolean isOk = true;
						if(minPcConverisons > 0 && metric.getPcMetric().getConversions() < minPcConverisons)
							isOk = false;
						else if(minMobileConversions > 0 && metric.getMobileMetric().getConversions() < minMobileConversions)
							isOk = false;
						else if(minAllDeviceConversions > 0 && metric.getAllDeviceMetric().getConversions() < minAllDeviceConversions)
							isOk = false;
						if(isOk){
							AdwordsDeviceMetricInfo info = new AdwordsDeviceMetricInfo();
							info.setCampaignData(false);
							info.setSiteType(siteType);
							info.setChannel(ch);
							info.setLanguageType(lang);
							info.setCategoryId(pcid);

							info.setDeviceMetric(metric);
							return info;
						}
					}
				}
			}
		}
		return null;
	}


	// private methods
	// key definition
	private static String getCampaignKey(long accountId, long campaignId){
		return accountId + "\t" + campaignId;
	}
	private static String getSummaryKey(SiteType siteType, AdwordsChannel channel, LanguageType languageType, int cid){
		return (channel == null ? DEFAULT_ID : channel.getChannelId()) + "\t" + 
				siteType.getSiteCode() + "\t" + 
				(languageType == null ? DEFAULT_ID : languageType.getLanguageId()) + "\t" + cid;
	}

	// protected methods
	protected void mergeDataToMap(AdwordsReport report, AdwordsAttribute attribute, AdwordsDevice aDevice
			, AdwordsChannel channel, SiteType siteType, LanguageType languageType, int cid, Long accountId, Long campaignId) throws IOException{
		// summary   
		CPTree cpTree = (siteType == SiteType.mini ? miniCpTree : litbCpTree);
		List<Integer> pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
		if(!pcidList.contains(Conf.ROOT_CID))
			pcidList.add(Conf.ROOT_CID);
		// category
		for(AdwordsChannel ch : Arrays.asList(channel, null)){
			for(LanguageType lang : Arrays.asList(languageType, null)){
				for(int pcid : pcidList){
					String key = getSummaryKey(siteType, ch, lang, pcid);
					AdwordsDeviceMetric metric = summaryKeyMetricMap.get(key);
					if(metric == null){
						metric = new AdwordsDeviceMetric();
						summaryKeyMetricMap.put(key, metric);
					}
					if(channel.equals(AdwordsChannel.display)){
						metric.mergeData(aDevice, report.getMetric());
					}
					else if(aDevice == null){
						metric.mergeData(AdwordsDevice.desktop, report.getMetric());
					}
					else{
						metric.mergeMobileAndSubstractPcData(report.getMetric());
					}
				}
			}
		}

		// campaign
		String campaignKey = getCampaignKey(accountId, campaignId);
		AdwordsDeviceMetric campaignMetric = campaignKeyMetricMap.get(campaignKey);
		if(campaignMetric == null){
			campaignMetric = new AdwordsDeviceMetric();
			campaignKeyMetricMap.put(campaignKey, campaignMetric);
		}
		if(channel.equals(AdwordsChannel.display)){
			campaignMetric.mergeData(aDevice, report.getMetric());
		}
		else if(aDevice == null){
			campaignMetric.mergeData(AdwordsDevice.desktop, report.getMetric());
		}
		else{
			campaignMetric.mergeMobileAndSubstractPcData(report.getMetric());
		}
	}

	protected void filter(){
		// filter campaign map
		List<String> keyToDelList = new ArrayList<String>();
		for(Entry<String, AdwordsDeviceMetric> entry : campaignKeyMetricMap.entrySet()){
			String key = entry.getKey();
			AdwordsMetric pcMetric = entry.getValue().getPcMetric();
			AdwordsMetric mMetric = entry.getValue().getMobileMetric();
			if(pcMetric.getClicks() < MIN_CLICK || mMetric.getClicks() < MIN_CLICK)
				keyToDelList.add(key);
		}
		for(String keToDel : keyToDelList){
			campaignKeyMetricMap.remove(keToDel);
		}		

		// filter summary map		
		keyToDelList = new ArrayList<String>();
		for(Entry<String, AdwordsDeviceMetric> entry : summaryKeyMetricMap.entrySet()){
			String key = entry.getKey();
			AdwordsMetric pcMetric = entry.getValue().getPcMetric();
			AdwordsMetric mMetric = entry.getValue().getMobileMetric();
			if(pcMetric.getClicks() < MIN_CLICK || mMetric.getClicks() < MIN_CLICK)
				keyToDelList.add(key);
		}
		for(String keyToDel : keyToDelList)
			summaryKeyMetricMap.remove(keyToDel);
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
			AdwordsMetricProviderBase provider = new AdwordsMetricProviderBase(endDate, interval);

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
