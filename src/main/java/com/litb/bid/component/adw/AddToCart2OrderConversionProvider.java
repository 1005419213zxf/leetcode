package com.litb.bid.component.adw;

import com.litb.bid.util.CpTreeFactory;
import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.adw.lib.enums.AdwordsCountry;
import com.litb.adw.lib.enums.LitbAdChannel;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.db.DBHelper;
import com.litb.basic.db.DBPool;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class AddToCart2OrderConversionProvider implements AddToCart2OrderConversionProviderInterface {
	private static final double MAX_CONVERSION_RATE = 1.0;
	private static final double MIN_CONVERSION_RATE = 0.0;
	private static final int SUM_CID = -1;
	private static final int MIN_ADD_CART_COUNT = 400;
	private static final String DATA_DIR = "/mnt/adwords/auto_bidding/delay/";
	
	private SiteType siteType;
	private CPTree cpTree;
	
	private HashMap<String, AddToCartConversionData> keyRateMap = new HashMap<String, AddToCartConversionData>();
	
	private static String getOutputFilePath(SiteType siteType){
		return DATA_DIR + "country_cid_addtocart_conversion_" + siteType;
	}
	
	// constructor
	public AddToCart2OrderConversionProvider(SiteType siteType) throws SQLException, IOException {
		this.siteType = siteType;
		this.cpTree = CpTreeFactory.getCategoryCpTree(siteType);
		initFromLocal();
	}
	
	// public methods
	public void initFromLocal() throws IOException{
	    String inputFilePath = getOutputFilePath(siteType);
	    System.out.println("reading: " + inputFilePath);
	    if (FileHelper.isFileExist(inputFilePath)) {
	      BufferedReader br = FileHelper.readFile(inputFilePath);
	      String line;
	      while ((line=br.readLine()) != null) {
	        String [] arr = line.split("\t");
	        String key = arr[0];
	        double addToCart = Double.valueOf(arr[1]);
	        double order = Double.valueOf(arr[2]);
	        AddToCartConversionData data = new AddToCartConversionData();
	        data.addToCarts = addToCart;
	        data.order = order;
	        keyRateMap.put(key, data);
	      }
	      br.close();
	    } else {
	      System.out.println("[WARNING]addtocart conversion file not Exist: " + inputFilePath);
	    }
	}
	
	@Override
	public double getAddToCart2OrderConversionRate(LitbAdChannel channel, AdwordsCountry country,
			int categoryId) {
		int topCid = cpTree.getCategoryTopCategory(categoryId);
		List<Integer> cidList = new ArrayList<Integer>();
		cidList.add(topCid);
		if(topCid != -1)
			cidList.add(-1);
		for(int cid : cidList){
			String key = getKey(channel, country, cid);
			AddToCartConversionData data = keyRateMap.get(key);
			if(data != null){
				double conversionRate = data.order/data.addToCarts;
				if(conversionRate > MAX_CONVERSION_RATE)
					conversionRate = MAX_CONVERSION_RATE;
				if(conversionRate < MIN_CONVERSION_RATE)
					conversionRate = MIN_CONVERSION_RATE;
				return conversionRate;
			}
		}
		return 0.0;
	}
	
	// private methods
	private static String getKey(LitbAdChannel channel, AdwordsCountry adwordsCountry,
			int cid){
		return channel.getChannelId() + "_" + adwordsCountry + "_" + (cid <= 0 ? SUM_CID : cid);
	}

	public static void loadBiData(SiteType sType) throws SQLException, IOException{
		System.out.println("initializing AddToCart2OrderConversionProvider...");
		DBHelper dbHelper = DBPool.getCnBiMasterDbHelperWithMarketingRole();
		
		String sql = "select merchant_id, channel_id, cms_catid1, country_code, add_count, order_count from DW_WA_SHOP_ORDER_CONVERSION";
		System.out.println(sql);
		HashMap<String, AddToCartConversionData> keyRateMap = new HashMap<String, AddToCartConversionData>();

		ResultSet resultSet = dbHelper.executeQuery(sql);
		while(resultSet.next()){
			try {
				SiteType siteType = SiteType.getSiteType(resultSet.getInt(1));
				if(sType != siteType)
					continue;
				LitbAdChannel channel = LitbAdChannel.getLitbAdChannel(resultSet.getInt(2));
				int cid = resultSet.getInt(3);
				if(cid <= 0)
					cid = -1;
				AdwordsCountry country = AdwordsCountry.valueOf(resultSet.getString(4).toUpperCase().replace("GB", "UK"));
				long addToCart = resultSet.getLong(5);
				long order = resultSet.getLong(6);
				
				List<Integer> cidList = new ArrayList<Integer>();
				cidList.add(cid);
				if(cid != -1)
					cidList.add(-1);
				
				for(int categoryId : cidList){
					String key = getKey(channel, country, categoryId);
					AddToCartConversionData data = keyRateMap.get(key);
					if(data == null){
						data = new AddToCartConversionData();
						keyRateMap.put(key, data);
					}
					data.addToCarts += addToCart;
					data.order += order;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		dbHelper.close();
		
		List<String> deleteKeyList = new ArrayList<String>();
		for(Entry<String, AddToCartConversionData> entry : keyRateMap.entrySet()){
			if(entry.getValue().addToCarts < MIN_ADD_CART_COUNT)
				deleteKeyList.add(entry.getKey());
		}
		for(String deleteKey : deleteKeyList)
			keyRateMap.remove(deleteKey);
		
		System.out.println("finish, size: " + keyRateMap.size());
		
		String outputFilePath = getOutputFilePath(sType);
		BufferedWriter bw = FileHelper.writeFile(outputFilePath);
		for(Entry<String, AddToCartConversionData> entry : keyRateMap.entrySet()){
			bw.append(entry.getKey() + "\t" + entry.getValue() + "\n");
		}
		bw.close();
	}
	
	public static class AddToCartConversionData{
		private double addToCarts;
		private double order;
		@Override
		public String toString() {
			return addToCarts + "\t" + order;
		}
	}
	
	// main for test
	public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
		if (args.length >= 1 && args[0].equals("load")) {
			AddToCart2OrderConversionProvider.loadBiData(SiteType.litb);
			AddToCart2OrderConversionProvider.loadBiData(SiteType.mini);
	    }
		
		SiteType siteType = null;
		AdwordsCountry country = null;
		AdwordsChannel channel = null;
		int cid = -1;
		if(args.length >= 4){
			siteType = SiteType.valueOf(args[0]);
			country = AdwordsCountry.valueOf(args[1]);
			channel = AdwordsChannel.valueOf(args[2]);
			cid = Integer.valueOf(args[3]);
		}
		LitbAdChannel litbAdChannel = LitbAdChannel.getLitbAdChannel(channel.getChannelId());
		AddToCart2OrderConversionProvider provider = new AddToCart2OrderConversionProvider(siteType);
		System.out.println(provider.getAddToCart2OrderConversionRate(litbAdChannel, country, cid));
		System.exit(0);
	}
}

