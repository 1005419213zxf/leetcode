package com.litb.bid.component.adw;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.litb.bid.DirDef;
import com.litb.bid.Conf;
import com.litb.bid.object.adwreport.CampaignPlatformIntervalReport;
import com.litb.bid.util.CpTreeFactory;
import com.litb.adw.lib.enums.AdwordsDevice;
import com.litb.adw.lib.enums.AdwordsReportType;
import com.litb.adw.lib.exception.AdwordsApiException;
import com.litb.adw.lib.exception.AdwordsRemoteException;
import com.litb.adw.lib.exception.AdwordsValidationException;
import com.litb.adw.lib.obj.AdwordsAccount;
import com.litb.adw.lib.obj.AdwordsAccountManager;
import com.litb.adw.lib.operation.report.AdwordsMetric;
import com.litb.adw.lib.operation.report.CampaignPlatformAttribute;
import com.litb.adw.lib.util.AdwordsCampaignNameHelper;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;
import com.litb.basic.util.DateHelper;
import com.litb.bid.util.JsonMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DevicesMetricProvider {
	private AdwordsAccountManager accountManager;
	private Date endDate;
	private Map<String, DevicesMetric[]> campaignDevicesMetricMap = new HashMap<String, DevicesMetric[]>();
	private Map<String, DevicesMetric[]> sitetypeCidDevicesMetricMap = new HashMap<String, DevicesMetric[]>();
	
	public DevicesMetricProvider(Date endDate) {
		System.out.println("init device adjust rates provider...");
		this.endDate = endDate;
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void init() throws IOException, AdwordsValidationException, AdwordsApiException, AdwordsRemoteException{
		accountManager = new AdwordsAccountManager();
		
		for(int i = 0; i < Conf.REPORT_CAMPAIGN_INTERVALS.length; i++){
			int interval = Conf.REPORT_CAMPAIGN_INTERVALS[i];
			for(AdwordsAccount account : accountManager.getAllAccounts()){
//				if(!account.getName().toLowerCase().contains("shopping"))
//					continue;
				long accountId = account.getId();
				String formattedFilePath = DirDef.getFormattedReportFilePath(endDate, AdwordsReportType.campaign_device_performance, interval, null, accountId);
				if(!FileHelper.isFileExist(formattedFilePath))
					continue;
				BufferedReader br = FileHelper.readFile(formattedFilePath);
				String line = null;
				while((line=br.readLine())!=null){
					CampaignPlatformIntervalReport campaignReport = CampaignPlatformIntervalReport.parseFromLine(line);
					AdwordsMetric adwordsMetric = campaignReport.getMetric();
					CampaignPlatformAttribute attribute = (CampaignPlatformAttribute)campaignReport.getAttribute();
					String key = accountId + "\t" + attribute.getCampaignId();
					DevicesMetric[] devicesMetrics = campaignDevicesMetricMap.get(key);
					if(devicesMetrics == null){
						devicesMetrics = new DevicesMetric[Conf.REPORT_CAMPAIGN_INTERVALS.length];
						for(int j = 0; j < Conf.REPORT_CAMPAIGN_INTERVALS.length; j++)
							devicesMetrics[j] = new DevicesMetric();
						campaignDevicesMetricMap.put(key, devicesMetrics);
					}
					devicesMetrics[i].setInterval(interval);
					if(attribute.getDevice() == AdwordsDevice.desktop){
						AdwordsMetric metrics = devicesMetrics[i].getPcAdwordsMetrics();
						metrics.mergeData(adwordsMetric);
					}
					if(attribute.getDevice() == AdwordsDevice.mobile){
						AdwordsMetric metrics = devicesMetrics[i].getMobileMetrics();
						metrics.mergeData(adwordsMetric);;
					}
					
					String campaignName = attribute.getCampaignName();
					SiteType siteType = AdwordsCampaignNameHelper.getSiteType(campaignName);
					int cid = AdwordsCampaignNameHelper.getCategoryIdFromCampaignName(campaignName);
					DevicesMetric[] cidDevicesMetrics = sitetypeCidDevicesMetricMap.get(siteType+"\t"+cid);
					if(cidDevicesMetrics == null){
						cidDevicesMetrics = new DevicesMetric[Conf.REPORT_CAMPAIGN_INTERVALS.length];
						for(int j = 0; j < Conf.REPORT_CAMPAIGN_INTERVALS.length; j++)
							cidDevicesMetrics[j] = new DevicesMetric();
						sitetypeCidDevicesMetricMap.put(siteType+"\t"+cid, cidDevicesMetrics);
					}
					cidDevicesMetrics[i].setInterval(interval);
					if(attribute.getDevice() == AdwordsDevice.desktop){
						AdwordsMetric metrics = cidDevicesMetrics[i].getPcAdwordsMetrics();
						metrics.mergeData(adwordsMetric);;
					}
					if(attribute.getDevice() == AdwordsDevice.mobile){
						AdwordsMetric metrics = cidDevicesMetrics[i].getMobileMetrics();
						metrics.mergeData(adwordsMetric);;
					}
				}
				br.close();
			}
		}
		
//		System.out.println("output:");
//		System.out.println(JsonMapper.toJsonString(campaignDevicesMetricMap));
//		System.out.println(JsonMapper.toJsonString(sitetypeCidDevicesMetricMap));
	}
	
	public DevicesMetric[] getDevicesMetrics(SiteType siteType, int cid){
		try {
			CPTree cpTree = CpTreeFactory.getCategoryCpTree(siteType);
			List<Integer> cidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
			cidList.add(Conf.ROOT_CID);
			for(int id : cidList){
				if(sitetypeCidDevicesMetricMap.containsKey(siteType+"\t"+id))
					return sitetypeCidDevicesMetricMap.get(siteType+"\t"+id);
			}
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public DevicesMetric[] getDevicesMetrics(long accountId, long campaignId){
		if(campaignDevicesMetricMap.containsKey(accountId+"\t"+campaignId))
			return campaignDevicesMetricMap.get(accountId+"\t"+campaignId);
		return null;
	}
	
	public static class DevicesMetric{
		private int interval;
		private AdwordsMetric pcAdwordsMetrics = new AdwordsMetric();
		private AdwordsMetric mobileMetrics = new AdwordsMetric();
		public AdwordsMetric getPcAdwordsMetrics() {
			return pcAdwordsMetrics;
		}
		public void setPcAdwordsMetrics(AdwordsMetric pcAdwordsMetrics) {
			this.pcAdwordsMetrics = pcAdwordsMetrics;
		}
		public AdwordsMetric getMobileMetrics() {
			return mobileMetrics;
		}
		public void setMobileMetrics(AdwordsMetric mobileMetrics) {
			this.mobileMetrics = mobileMetrics;
		}
		public int getInterval() {
			return interval;
		}
		public void setInterval(int interval) {
			this.interval = interval;
		}
		@Override
		public String toString() {
			try {
				return JsonMapper.toJsonString(this);
			} catch (JsonProcessingException e) {
				return null;
			}
		}
	}
	
	public static void main(String[] args) throws JsonProcessingException {
		Date endDate = DateHelper.getShortDate(args[0]);
		DevicesMetricProvider deviceAdjustRateProvider = new DevicesMetricProvider(endDate);
		System.out.println(JsonMapper.toJsonString(deviceAdjustRateProvider.getDevicesMetrics(2014606789L, 261612014L)));
	}
}
