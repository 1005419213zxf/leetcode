package com.litb.bid.component.adw;

import com.litb.bid.DirDef;
import com.litb.bid.object.adw.DeviceStatItem;
import com.litb.bid.object.adw.Aggregation;
import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;
import com.litb.basic.util.DateHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProductCountryStatItemsProvider {
	private static final AdwordsChannel channel = AdwordsChannel.pla;
	
	private SiteType siteType;
	private Date endDate;
	private Map<Integer, DeviceStatItem[]> productIdStatItemsMap = new HashMap<Integer, DeviceStatItem[]>();
	
	// constructor
	public ProductCountryStatItemsProvider(SiteType siteType,
			Date endDate) {
		this.siteType = siteType;
		this.endDate = endDate;
		try {
			init();
		} catch (IOException e) {
			System.err.println("init ProductStatItemsProvider failed.");
		}
	}
	
	private void init() throws IOException{
		String input = DirDef.getLocalBiddableObjectProductAggregationFilePath(siteType, channel, endDate);
		BufferedReader br = FileHelper.readFile(input);
		String line = null;
		System.out.println("init ProductStatItemsProvider...");
		while((line=br.readLine())!=null){
			Aggregation aggregation = Aggregation.parse(line);
			if(aggregation == null)
				continue;
			int productId = Integer.valueOf(aggregation.getKey());
			productIdStatItemsMap.put(productId, aggregation.getStatItems());
		}
		br.close();
		System.out.println("ProductStatItemsProvider size: " + productIdStatItemsMap.size());
 	}
	
	// public methods
	public DeviceStatItem[] getStatItems(int productId){
		if(productId == -1)
			return null;
		return productIdStatItemsMap.get(productId);
	}
	
	// main for test
	public static void main(String[] args) {
		ProductCountryStatItemsProvider categoryStatItemsProvider = new ProductCountryStatItemsProvider(SiteType.litb, DateHelper.getShortDate("2016-05-03"));
		System.out.println(categoryStatItemsProvider.getStatItems(1126755)[0]);
	}
}
