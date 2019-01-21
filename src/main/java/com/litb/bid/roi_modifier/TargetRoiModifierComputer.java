package com.litb.bid.roi_modifier;


import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.adw.lib.exception.AdwordsApiException;
import com.litb.adw.lib.exception.AdwordsRemoteException;
import com.litb.adw.lib.exception.AdwordsValidationException;
import com.litb.basic.db.DBHelper;
import com.litb.basic.db.DBPool;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;
import com.litb.basic.util.DateHelper;
import com.litb.bid.DirDef;
import com.litb.bid.component.adw.BiddingParametersProvider;
import com.litb.bid.util.ComponentFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class TargetRoiModifierComputer {
	private static SiteType siteType;
	private static Date endDate;
	private static int duration;
	private static AdwordsChannel channel;
	private static int minConversions;
	
	public static void main(String[] args) throws SQLException, NumberFormatException, IOException, AdwordsValidationException, AdwordsApiException, AdwordsRemoteException {
		try {
			siteType = SiteType.valueOf(args[0]);
			channel = AdwordsChannel.valueOf(args[1]);
			endDate = DateHelper.getShortDate(args[2]);
			duration = Integer.parseInt(args[3]);
			minConversions = Integer.parseInt(args[4]);
		} catch (Exception e) {
			System.err.println("Usage: <site> <channel> <end date> <duration> <minConversions>");
			System.exit(1);
		}
		System.out.println(siteType + " " + DateHelper.getShortDateString(endDate) + " " + duration + " " + channel);
		BiddingParametersProvider.BiddingParameters biddingParameters = ComponentFactory.getBiddingParametersProvider(siteType, channel).getBiddingParameters();
		ComponentFactory.setEndDate(endDate);
		
		Map<String, ActualRoiInfoWithKey> keyInfoMap = new HashMap<String, ActualRoiInfoWithKey>();
		for(int i = 0; i < duration; i++){
			Date targetDate = DateHelper.addDays(-i, endDate);
			String inputFilePath = DirDef.getActRoiForTargetRoiModifierFilePath(siteType,targetDate, channel);
			String line = null;
			BufferedReader br = FileHelper.readFile(inputFilePath);
			while((line=br.readLine())!=null){
				ActualRoiInfoWithKey actualRoiInfoWithKey = ActualRoiInfoWithKey.parse(line);
				String key = actualRoiInfoWithKey.getKey();
				ActualRoiInfoWithKey infoWithMetric = keyInfoMap.get(key);
				if(infoWithMetric == null){
					keyInfoMap.put(key, actualRoiInfoWithKey);
				}else {
					double actRoi = (infoWithMetric.getRoi() * infoWithMetric.getClicks() + actualRoiInfoWithKey.getRoi() * actualRoiInfoWithKey.getClicks())/(infoWithMetric.getClicks() + actualRoiInfoWithKey.getClicks());
					infoWithMetric.setRoi(actRoi);
					infoWithMetric.setClicks(infoWithMetric.getClicks() + actualRoiInfoWithKey.getClicks());
					infoWithMetric.setConversions(infoWithMetric.getConversions() + actualRoiInfoWithKey.getConversions());
					infoWithMetric.setCost(infoWithMetric.getCost() + actualRoiInfoWithKey.getCost());
					infoWithMetric.setLifetimeValue(infoWithMetric.getLifetimeValue() + actualRoiInfoWithKey.getLifetimeValue());
				}
			}
			br.close();
		}
		
		// filter
		System.out.println("filtering....");
		System.out.println("before filtering size: " + keyInfoMap.size());
		List<String> keysToDelList = new ArrayList<String>();
		for(Map.Entry<String, ActualRoiInfoWithKey> entry : keyInfoMap.entrySet()){
			//filter
			ActualRoiInfoWithKey entryInfo = entry.getValue();
			double cr = entryInfo.getConversions() / entryInfo.getClicks();
			if(cr > 0.05 ){
				keysToDelList.add(entry.getKey());
			}
			if(entry.getValue().getConversions() < minConversions)
				keysToDelList.add(entry.getKey());
		}
		for(String key : keysToDelList)
			keyInfoMap.remove(key);
		System.out.println("finish, size: " + keyInfoMap.size());
		
		DBHelper dbHelper = DBPool.getCnBiMasterDbHelperWithMarketingRole();
		ResultSet resultSet = dbHelper.executeQuery("select target_roi from ods_ad_target_expect where merchant_id=" + siteType.getSiteCode()
				+ " and channel_id=" + channel.getChannelId() + " order by stat_date DESC");
		if(!resultSet.first())
			throw new IOException("no exp roi for " + siteType + ":" + channel + " in ods_ad_target_expect");
		double expRoi = resultSet.getDouble(1);
		
		// get new target ROI
		String outputFilePath = "./config/" + siteType + "/" + channel + "_target_roi_modifier.config";
		System.out.println("output: " + outputFilePath);
		BufferedWriter bw = FileHelper.writeFile(outputFilePath);
		List<String> sortedKeys = new ArrayList<String>(keyInfoMap.keySet());
		Collections.sort(sortedKeys);
		for(String key : sortedKeys){
			ActualRoiInfoWithKey roiInfo = keyInfoMap.get(key);
			double actRoi = roiInfo.getRoi();
//			TargetRoiModifierInfo modifierInfo = biddingParameters.getKeyTargetRoiModifierMap().get(key);
//			double expRoi = modifierInfo.getExpectRoi();
//			double oldModifier = modifierInfo.getModifier();
			int categoryId = Integer.valueOf(key.split(":")[1]);
			double oldTargetRoi = biddingParameters.getTargetRoi(categoryId);
			double oldModifier = oldTargetRoi - expRoi;
			double modifier = oldModifier + expRoi - actRoi;
			String output = key + "\t" + modifier + "\t" + expRoi + "\t" + actRoi;
			bw.append(output + "\n");
		}
		bw.close();
		
		System.out.println("Done."); 
	}
}
