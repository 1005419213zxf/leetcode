package com.litb.bid.component.adw;

import com.litb.bid.DirDef;
import com.litb.bid.Conf;
import com.litb.bid.object.adw.BiddableObject;


import com.litb.bid.object.adw.ShoppingPeriodData;
import com.litb.bid.roi_modifier.KeysProvider;
import com.litb.bid.roi_modifier.TargetRoiModifierInfo;
import com.litb.bid.util.CpTreeFactory;
import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public class BiddingParametersProvider {
	private BiddingParameters biddingParameters;
	
	// constructor
	public BiddingParametersProvider(SiteType siteType, AdwordsChannel channel) throws IOException, SQLException {
		biddingParameters = new BiddingParameters();
		//target roi
		String configPath = DirDef.getTargetRoiPath(siteType, channel);
		System.out.println("loading old target ROI file: " + configPath);
		BufferedReader br = FileHelper.readFile(configPath);
		String line = null;
		while((line=br.readLine()) != null){
			String[] vals = line.split("\t");
			biddingParameters.getCategoryIdTargetRoiMap().put(Integer.valueOf(vals[0]), Double.valueOf(vals[1]));
		}
		br.close();
		
		//load target roi modifiers
		configPath = DirDef.getTargetRoiModifierPath(siteType, channel);
		System.out.println("loading new target ROI file: " + configPath);
		if(FileHelper.isFileExist(configPath)){
			br = FileHelper.readFile(configPath);
			line = null;
			while((line=br.readLine()) != null){
				TargetRoiModifierInfo info = TargetRoiModifierInfo.parse(line);
				biddingParameters.getKeyTargetRoiModifierMap().put(info.getKey(), info);
			}
			br.close();
		}
		else
			System.out.println("cannot load " + configPath);
		
		//others
		loadBiddingParameters(siteType, channel, biddingParameters);
		
		biddingParameters.setChannel(channel);
		biddingParameters.setSiteType(siteType);
	}

	// public methods
	public BiddingParameters getBiddingParameters(){
		return this.biddingParameters;
	}
	
	// configuration loading
	private void loadBiddingParameters(SiteType siteType, AdwordsChannel channel, BiddingParameters biddingParameters) throws IOException{
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("./config/" + siteType + "/custom_parameters.properties"));
		} catch (IOException e) {
			System.err.println("properties file not exists.");
			throw new IOException("properties file not exists.");
		}
		
		biddingParameters.crSlidingRatio = loadBiddingParameter(properties, channel + ".crSlidingRatio", Double.class);
		if(biddingParameters.crSlidingRatio < 0)
			biddingParameters.crSlidingRatio = 0;
		else if(biddingParameters.crSlidingRatio > 1)
			biddingParameters.crSlidingRatio = 1;
		biddingParameters.maxDecreaseRate = loadBiddingParameter(properties, channel + ".maxDecreaseRate", Double.class);
		biddingParameters.maxIncreaseRate = loadBiddingParameter(properties, channel + ".maxIncreaseRate", Double.class);
	}
		
		@SuppressWarnings("unchecked")
		private <T> T loadBiddingParameter(Properties properties, String key, Class<T> clz) throws IOException{
			String property = properties.getProperty(key);
			if(property == null)
				throw new IOException("bidding parameter <" + key + "> required.");
			try {
				return (T) clz.getDeclaredMethod("valueOf", String.class).invoke(null, property);
			} catch (Exception e) {
				throw new IOException("bidding parameter <" + key + "> value error.");
			}
		}
	
	public static class BiddingParameters{
		private SiteType siteType;
		private AdwordsChannel channel;
		private double crSlidingRatio;
		private double maxIncreaseRate;
		private double maxDecreaseRate;
		private Map<Integer, Double> categoryIdTargetRoiMap = new HashMap<Integer, Double>();
		private Map<String, TargetRoiModifierInfo> keyTargetRoiModifierMap = new HashMap<String, TargetRoiModifierInfo>();
		public double getCrSlidingRatio() {
			return crSlidingRatio;
		}
		public void setCrSlidingRatio(double crSlidingRatio) {
			this.crSlidingRatio = crSlidingRatio;
		}
		public double getMaxIncreaseRate() {
			return maxIncreaseRate;
		}
		public void setMaxIncreaseRate(double maxIncreaseRate) {
			this.maxIncreaseRate = maxIncreaseRate;
		}
		public double getMaxDecreaseRate() {
			return maxDecreaseRate;
		}
		public void setMaxDecreaseRate(double maxDecreaseRate) {
			this.maxDecreaseRate = maxDecreaseRate;
		}
		public Map<Integer, Double> getCategoryIdTargetRoiMap() {
			return categoryIdTargetRoiMap;
		}
		public void setCategoryIdTargetRoiMap(Map<Integer, Double> categoryIdTargetRoiMap) {
			this.categoryIdTargetRoiMap = categoryIdTargetRoiMap;
		}
		public double getTargetRoi(int cid) throws IOException, SQLException{
			List<Integer> cidList = CpTreeFactory.getCategoryCpTree(siteType).getCategoryAncesterCategoryIncludingSelf(cid);
			cidList.add(Conf.ROOT_CID);
			for(int id : cidList){
				if(categoryIdTargetRoiMap.containsKey(id))
					return categoryIdTargetRoiMap.get(id);
			}
		
			throw new IOException("no target roi config for: <site>" +siteType +", <cid>"+cid );
		}
		public double getTargetRoi(BiddableObject biddableObject) throws IOException, SQLException{
			List<String> keySet = KeysProvider.getDuplicatedKeys(biddableObject);
			Map<String, Double> dimensionModifierMap = new HashMap<String, Double>();
			for(String key : keySet){
				String[] vals = key.split(":");
				String dimension = vals[0];
				TargetRoiModifierInfo targetRoiModifierInfo = keyTargetRoiModifierMap.get(key);
				if(targetRoiModifierInfo == null)
					continue;
				Double modifier = targetRoiModifierInfo.getModifier();
				if(!dimensionModifierMap.containsKey(dimension))
					dimensionModifierMap.put(dimension, modifier);
			}
			//debug
			double sum = 0.0;
			for(Entry<String, Double> entry : dimensionModifierMap.entrySet()){
				System.out.println(entry.getKey() + "\t" + entry.getValue());
				sum += entry.getValue();
			}
			if(dimensionModifierMap.size() == 0)
				throw new IOException("no target roi config for: <site>" +siteType);
			return sum/dimensionModifierMap.size();
//			System.out.println("sum:"+sum);
//			System.out.println("avg:"+sum/dimensionModifierMap.size());
		
//			throw new IOException("no target roi config for: <site>" +siteType);
		}
		public SiteType getSiteType() {
			return siteType;
		}
		public void setSiteType(SiteType siteType) {
			this.siteType = siteType;
		}
		public AdwordsChannel getChannel() {
			return channel;
		}
		public void setChannel(AdwordsChannel channel) {
			this.channel = channel;
		}
		public Map<String, TargetRoiModifierInfo> getKeyTargetRoiModifierMap() {
			return keyTargetRoiModifierMap;
		}
		public void setKeyTargetRoiModifierMap(
				Map<String, TargetRoiModifierInfo> keyTargetRoiModifierMap) {
			this.keyTargetRoiModifierMap = keyTargetRoiModifierMap;
		}
	}
	// main for test
	public static void main(String[] args) throws IOException, SQLException {
		BiddingParametersProvider biddingParametersProvider = new BiddingParametersProvider(SiteType.litb, AdwordsChannel.pla);
		BiddableObject biddableObject = BiddableObject.parse("1156612465	332346796	[EN][SRC][P&E][c199][Camera] <AU> <shopping>	active	15590806756	Battery Grips #1	active	82978473196	product_type_l1==phones & electronics&+product_type_l2==camera, photo & accessories&+product_type_l3==battery grips&+id==116830_0_au_en	active	PRODUCT_PARTITION	false	0.23	0	0	0_&&_1		0	0	0	0.0	0.0	0.0	-1.0	0	0.0	0	0.0	0.0	0.0		0	0	0	0	0	0	-_@@_1		0	0	0	0.0	0.0	0.0	-1.0	0	0.0	0	0.0	0.0	0.0		0	0	0	0	0	0	-_&&_7		0	0	0	0.0	0.0	0.0	-1.0	0	0.0	0	0.0	0.0	0.0		0	0	0	0	0	0	-_@@_7		0	0	0	0.0	0.0	0.0	-1.0	0	0.0	0	0.0	0.0	0.0		0	0	0	0	0	0	-_&&_14		0	0	0	0.0	0.0	0.0	-1.0	0	0.0	0	0.0	0.0	0.0		0	0	0	0	0	0	-_@@_14		0	0	0	0.0	0.0	0.0	-1.0	0	0.0	0	0.0	0.0	0.0		0	0	0	0	0	0	-_&&_28		0	0	0	0.0	0.0	0.0	-1.0	0	0.0	0	0.0	0.0	0.0		0	0	0	0	0	0	-_@@_28		0	0	0	0.0	0.0	0.0	-1.0	0	0.0	0	0.0	0.0	0.0		0	0	0	0	0	0	-_&&_90		0	0	0	0.0	0.0	0.0	-1.0	0	0.0	0	0.0	0.0	0.0		0	0	0	0	0	0	-_@@_90		0	0	0	0.0	0.0	0.0	-1.0	0	0.0	0	0.0	0.0	0.0		0	0	0	0	0	0	-_&&_180		0	0	0	0.0	0.0	0.0	-1.0	0	0.0	0	0.0	0.0	0.0		0	0	0	0	0	0	-_@@_180		0	0	0	0.0	0.0	0.0	-1.0	0	0.0	0	0.0	0.0	0.0		0	0	0	0	0	0	-_&&_365		0	0	0	0.0	0.0	0.0	-1.0	0	0.0	0	0.0	0.0	0.0		0	0	0	0	0	0	-_@@_365		0	0	0	0.0	0.0	0.0	-1.0	0	0.0	0	0.0	0.0	0.0		0	0	0	0	0	0	-_&&_3650		48	2	0	0.0	0.0	0.26	0.0	0	0.0	0	0.0	0.0	0.0		0	0	0	0	0	0	-_@@_3650		0	0	0	0.0	0.0	0.0	-1.0	0	0.0	0	0.0	0.0	0.0		0	0	0	0	0	0	-", ShoppingPeriodData.class);
		System.out.println(biddingParametersProvider.getBiddingParameters().getTargetRoi(biddableObject));
	}
}
