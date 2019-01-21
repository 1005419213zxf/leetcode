package com.litb.bid.component.adw;

import com.litb.bid.DirDef;
import com.litb.bid.Conf;
import com.litb.bid.object.adw.AdwordsDeviceMetric;
import com.litb.bid.object.adwreport.CampaignPlatformIntervalReport;
import com.litb.bid.util.CpTreeFactory;
import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.adw.lib.enums.AdwordsDevice;
import com.litb.adw.lib.enums.AdwordsReportType;
import com.litb.adw.lib.exception.AdwordsApiException;
import com.litb.adw.lib.exception.AdwordsRemoteException;
import com.litb.adw.lib.exception.AdwordsValidationException;
import com.litb.adw.lib.operation.report.AdwordsMetric;
import com.litb.adw.lib.operation.report.CampaignPlatformAttribute;
import com.litb.adw.lib.util.AdwordsCampaignNameHelper;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;
import com.litb.basic.util.DateHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

public class AdwordsMetricProvider implements AdwordsMetricProviderInterface {
	private static final int DEFAULT_ID = -1;
	private static final int MIN_CLICK = 2000;
	private static final int MIN_CONVERSION = 1;
	
	private static final AdwordsDevice TARGET_DEVICE = null;
	
	private CPTree litbCpTree;
	private CPTree miniCpTree;
	
	private Map<String, AdwordsDeviceMetric> campaignKeyMetricMap = new HashMap<String, AdwordsDeviceMetric>();
	private Map<String, AdwordsDeviceMetric> summaryKeyMetricMap = new HashMap<String, AdwordsDeviceMetric>();
	
	
//	private Map<Integer, Map<String, AdwordsMetricInfo>> intervalKeyMetricMap = new HashMap<Integer, Map<String, AdwordsMetricInfo>>();
	
	// constructor
	public AdwordsMetricProvider(Date endDate, int interval) throws SQLException, IOException{
		System.out.println("initializing " + this.getClass().getSimpleName() + ", end date: " + DateHelper.getShortDateString(endDate) + " " + interval + "...");
		this.litbCpTree = CpTreeFactory.getCategoryCpTree(SiteType.litb);
		this.miniCpTree = CpTreeFactory.getCategoryCpTree(SiteType.mini);
		
		// initialize
		String inputDir = DirDef.getFormattedReportDir(endDate, AdwordsReportType.campaign_device_performance, interval, TARGET_DEVICE);
		System.out.println("input dir: " + inputDir);
		for(String inputFilePath : FileHelper.getFilePathsInOneDir(inputDir))
			init(inputFilePath);
		System.out.println("after init size(campaign): " + campaignKeyMetricMap.size());
		System.out.println("after init size(summary): " + summaryKeyMetricMap.size());
		// filter
		filter();
		System.out.println("after filter size(campaign): " + campaignKeyMetricMap.size());
		System.out.println("after filter size(summary): " + summaryKeyMetricMap.size());
	}
	


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
			for(int pcid : pcidList){
				for(LanguageType lang : Arrays.asList(languageType, null)){
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
	
	public AdwordsDeviceMetricInfo getDeviceMetricExact(SiteType siteType, AdwordsChannel channel, LanguageType languageType, int categoryId){
		AdwordsDeviceMetric metric = summaryKeyMetricMap.get(getSummaryKey(siteType, channel, languageType, categoryId));
		if(metric == null)
			return null;
		
		AdwordsDeviceMetricInfo info = new AdwordsDeviceMetricInfo();
		info.setCampaignData(false);
		info.setSiteType(siteType);
		info.setChannel(channel);
		info.setLanguageType(languageType);
		info.setCategoryId(categoryId);
		
		info.setDeviceMetric(metric);
		return info;
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
	
	// initialize
	private void init(String inputFilePath) throws IOException{
		// for one account
		Map<String, AdwordsDeviceMetric> tmpCampaignKeyMetricMap = new HashMap<String, AdwordsDeviceMetric>();
		BufferedReader br = FileHelper.readFile(inputFilePath);
		String line;
		while((line = br.readLine()) != null){
			try {
				CampaignPlatformIntervalReport report = CampaignPlatformIntervalReport.parseFromLine(line);
				CampaignPlatformAttribute attribute = (CampaignPlatformAttribute)report.getAttribute();
				
				// summary
				SiteType siteType = AdwordsCampaignNameHelper.getSiteType(attribute.getCampaignName());
				AdwordsChannel channel = AdwordsCampaignNameHelper.getAdwordsChannel(attribute.getCampaignName());
				LanguageType languageType = AdwordsCampaignNameHelper.getLanguageTypeFromCampaignName(attribute.getCampaignName());
				int cid = AdwordsCampaignNameHelper.getCategoryIdFromCampaignName(attribute.getCampaignName());
				CPTree cpTree = (siteType == SiteType.mini ? miniCpTree : litbCpTree);
				List<Integer> pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
				if(!pcidList.contains(Conf.ROOT_CID))
					pcidList.add(Conf.ROOT_CID);
				
				for(AdwordsChannel ch : Arrays.asList(channel, null)){
					for(LanguageType lang : Arrays.asList(languageType, null)){
						for(int pcid : pcidList){
							String key = getSummaryKey(siteType, ch, lang, pcid);
							AdwordsDeviceMetric metric = summaryKeyMetricMap.get(key);
							if(metric == null){
								metric = new AdwordsDeviceMetric();
								summaryKeyMetricMap.put(key, metric);
							}
							metric.mergeData(attribute.getDevice(), report.getMetric());
						}
					}
				}
				
				// campaign
				String campaignKey = getCampaignKey(attribute.getAccountId(), attribute.getCampaignId());
				AdwordsDeviceMetric campaignMetric = tmpCampaignKeyMetricMap.get(campaignKey);
				if(campaignMetric == null){
					campaignMetric = new AdwordsDeviceMetric();
					tmpCampaignKeyMetricMap.put(campaignKey, campaignMetric);
				}
				campaignMetric.mergeData(attribute.getDevice(), report.getMetric());
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		br.close();
		
		// filter campaign map
		for(Map.Entry<String, AdwordsDeviceMetric> entry : tmpCampaignKeyMetricMap.entrySet()){
			AdwordsMetric metric = entry.getValue().getAllDeviceMetric();
			if(metric.getClicks() >= MIN_CLICK && metric.getAllConversions() >= MIN_CONVERSION)
				campaignKeyMetricMap.put(entry.getKey(), entry.getValue());
		}
	}
	
	// filter summary map
	private void filter(){
		List<String> keyToDelList = new ArrayList<String>();
		for(Map.Entry<String, AdwordsDeviceMetric> entry : summaryKeyMetricMap.entrySet()){
			String key = entry.getKey();
			
			AdwordsMetric pcMetric = entry.getValue().getPcMetric();
			AdwordsMetric mMetric = entry.getValue().getMobileMetric();
			if(pcMetric.getClicks() < MIN_CLICK || mMetric.getClicks() < MIN_CLICK)
				keyToDelList.add(key);
//			AdwordsMetric metric = entry.getValue().getAllDeviceMetric();
//			
//			if(metric.getClicks() < MIN_CLICK || metric.getConversions() < MIN_CONVERSION)
//				keyToDelList.add(key);
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
			AdwordsMetricProvider provider = new AdwordsMetricProvider(endDate, interval);
			
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
