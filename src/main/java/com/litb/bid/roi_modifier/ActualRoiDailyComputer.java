package com.litb.bid.roi_modifier;


import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.adw.lib.enums.LitbAdChannel;
import com.litb.adw.lib.exception.AdwordsApiException;
import com.litb.adw.lib.exception.AdwordsRemoteException;
import com.litb.adw.lib.exception.AdwordsValidationException;
import com.litb.adw.lib.operation.report.AdwordsMetric;
import com.litb.adw.lib.util.AdwordsCampaignNameHelper;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;
import com.litb.basic.util.DateHelper;
import com.litb.bid.DirDef;

import com.litb.bid.component.adw.RedirectSuccRateInfo;
import com.litb.bid.component.adw.RepeatRateInfo;
import com.litb.bid.component.adw.ValueAdjustRateInfo;
import com.litb.bid.component.adw.delay.DelayRateInfo;
import com.litb.bid.object.adw.AudiencePeriodData;
import com.litb.bid.object.adw.BiddableObject;
import com.litb.bid.object.adw.KeywordPeriodData;
import com.litb.bid.object.adw.ShoppingPeriodData;
import com.litb.bid.util.ComponentFactory;
import com.litb.dm.aws.s3.AS3FileHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ActualRoiDailyComputer {
	private static final int INTERVAL = 1;
	
	private SiteType siteType;
	@SuppressWarnings("unused")
	private Date endDate;
	private AdwordsChannel channel;
	
	public ActualRoiDailyComputer(SiteType siteType, Date endDate, AdwordsChannel channel) throws IOException, NumberFormatException, SQLException, AdwordsValidationException, AdwordsApiException, AdwordsRemoteException {
		System.out.println(siteType + " " + DateHelper.getShortDateString(endDate) + " " + channel);
		this.siteType = siteType;
		this.endDate = endDate;
		this.channel = channel;
		ComponentFactory.setEndDate(endDate);
		
		String inputFilePath = DirDef.getLocalBiddableObjectDataDir(siteType, channel, endDate) + "part-r-00000";
		if(!FileHelper.isFileExist(inputFilePath)){
			String s3BiddableObjectFilePath = DirDef.getS3BiddableObjectDataDir(siteType, channel, endDate) + "part-r-00000";
			if(!AS3FileHelper.isFileExist(s3BiddableObjectFilePath)){
				System.err.println("s3 biddable object file not exist.");
				return;
			}
			FileHelper.createDirectory(DirDef.getLocalBiddableObjectDataDir(siteType, channel, endDate));
			System.out.println("s3 source: " + s3BiddableObjectFilePath);
			System.out.println("local target: " + inputFilePath);
			System.out.println("copy s3 biddable objects to local...");
			AS3FileHelper.copyS3FileToLocal(s3BiddableObjectFilePath, inputFilePath);
		}
	
		Map<String, ActualRoiInfoWithKey> keyMap = processOneFile(inputFilePath);
		
		BufferedWriter bw = FileHelper.writeFile(DirDef.getActRoiForTargetRoiModifierFilePath(siteType,endDate, channel));
		for(ActualRoiInfoWithKey targetRoiModifierInfoWithMetric : keyMap.values()){
			targetRoiModifierInfoWithMetric.setRoi(targetRoiModifierInfoWithMetric.getLifetimeValue()/targetRoiModifierInfoWithMetric.getCost());
			bw.append(targetRoiModifierInfoWithMetric.toString() + "\n");
		}
		bw.close();
		
		System.out.println("Done."); 
	}

	public static void main(String[] args) throws SQLException, NumberFormatException, IOException, AdwordsValidationException, AdwordsApiException, AdwordsRemoteException {
//		SiteType siteType = null;
//		Date endDate = null;
//		AdwordsChannel channel = null;
//		try {
//			siteType = SiteType.valueOf(args[0]);
//			channel = AdwordsChannel.valueOf(args[1]);
//			endDate = DateHelper.getShortDate(args[2]);
//		} catch (Exception e) {
//			System.err.println("Usage: <site> <channel> <end date>");
//			System.exit(1);
//		}
//		ActualRoiDailyComputer actualRoiDailyComputer = new ActualRoiDailyComputer(siteType, endDate, channel);
	}
	
	private Map<String, ActualRoiInfoWithKey> processOneFile(String inputFilePath) throws IOException, NumberFormatException, SQLException, AdwordsValidationException, AdwordsApiException, AdwordsRemoteException{
		System.out.println("process: " + inputFilePath);
		Map<String, ActualRoiInfoWithKey> keyInfoMap = new HashMap<String, ActualRoiInfoWithKey>();
		BufferedReader br = FileHelper.readFile(inputFilePath);
		String line = null;
		int count = 0;
		Class<? extends BiddableObject> biddableObjectClz = null;
		if(channel == AdwordsChannel.pla)
			biddableObjectClz = ShoppingPeriodData.class;
		if(channel == AdwordsChannel.search)
			biddableObjectClz = KeywordPeriodData.class;
		if(channel == AdwordsChannel.display)
			biddableObjectClz = AudiencePeriodData.class;
		if(biddableObjectClz == null)
			return keyInfoMap;
		while((line = br.readLine())!=null){
			if(count++ % 100000 == 0)
				System.out.println("process: " + count);
			// parse
			BiddableObject biddableObject = BiddableObject.parse(line, biddableObjectClz);
			if(biddableObject == null)
				continue;
			String campaignName = biddableObject.getCampaignName();
			
			// filter
			if(AdwordsCampaignNameHelper.isBrandCampaign(campaignName))
				continue;
			if(AdwordsCampaignNameHelper.isMobileCampaign(campaignName))
				continue;
			// lifetime value
			int cid = biddableObject.getCategoryId();
			LanguageType languageType = biddableObject.getLanguageType();
			if(languageType == null)
				languageType = LanguageType.en;
			long accountId = biddableObject.getAccountId();
			
			RedirectSuccRateInfo redirectSuccRateInfo = ComponentFactory.getRedirectSuccRateProvider().getRediretSuccRate(siteType, languageType);
			RepeatRateInfo repeatRateInfo = ComponentFactory.getRepeatRateProvider(siteType).getRepeatRate(languageType, cid);
			ValueAdjustRateInfo valueAdjustRateInfo = ComponentFactory.getValueAdjustDataProvider(siteType).getValueAdjustRate(languageType, cid);
			LitbAdChannel litbAdChannel = LitbAdChannel.getLitbAdChannel(channel.getChannelId());
			DelayRateInfo delayRateInfo = ComponentFactory.getDelayRateProvider(siteType).getDelayRate(litbAdChannel, siteType,
					languageType, cid, INTERVAL, 1, 30);
			// compute
			double redirectSuccRate = redirectSuccRateInfo.getRedirectSuccRate();
			double repeatRate = (repeatRateInfo == null) ? 0 : repeatRateInfo.getRepeatRate();
			double valueAdjustRate = (valueAdjustRateInfo == null) ? 1.0 : valueAdjustRateInfo.getTotalAdjRatio();
			double delayRate = (delayRateInfo == null) ? 1.0 : delayRateInfo.getDelayRate();
			double exchangeAdjRate = ComponentFactory.getAccountExchangeRateProvider().getExchangeRate(accountId);
			
			AdwordsMetric metric = biddableObject.getFirstIntervalStatItem().getPcDeviceStatItem().getReportMetric();
			if(metric.getClicks() <=0 )
				continue;
			double ltv = metric.getConversionValue();
			ltv /= 3.3;
			ltv *= delayRate;
			ltv *= (1 + repeatRate);
			ltv *= valueAdjustRate;
			ltv /= redirectSuccRate;
			
			double cost = metric.getCost();
			cost *= exchangeAdjRate;
			
			// summary
			for(String key : KeysProvider.getDuplicatedKeys(biddableObject)){
				ActualRoiInfoWithKey roiInfo = keyInfoMap.get(key);
				if(roiInfo == null){
					roiInfo = new ActualRoiInfoWithKey();
					roiInfo.setKey(key);
					keyInfoMap.put(key, roiInfo);
				}
				roiInfo.setClicks(roiInfo.getClicks() + metric.getClicks());
				roiInfo.setConversions(roiInfo.getConversions() + metric.getConversions());
				roiInfo.setCost(roiInfo.getCost() + cost);
				roiInfo.setLifetimeValue(roiInfo.getLifetimeValue() + ltv);
			}
			
		}
		br.close();
		
		return keyInfoMap;
	}
}
