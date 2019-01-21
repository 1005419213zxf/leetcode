package com.litb.bid.component.adw;

import com.litb.bid.DirDef;
import com.litb.bid.object.adw.DeviceStatItem;
import com.litb.bid.object.adw.Aggregation;
import com.litb.bid.util.CpTreeFactory;
import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryStatItemsProvider {
	private SiteType siteType;
	private AdwordsChannel channel;
	private Date endDate;
	private CPTree cpTree;
	private Map<Integer, DeviceStatItem[]> cidStatItemsMap = new HashMap<Integer, DeviceStatItem[]>();
	
	// constructor
	public CategoryStatItemsProvider(SiteType siteType, AdwordsChannel channel, Date endDate) throws SQLException {
		this.siteType = siteType;
		this.channel = channel;
		this.endDate = endDate;
		this.cpTree = CpTreeFactory.getCategoryCpTree(siteType);
		try {
			init();
		} catch (IOException e) {
			System.err.println("init CategoryStatItemsProvider failed.");
		}
	}
	
	private void init() throws IOException{
		String input = DirDef.getLocalBiddableObjectCategoryAggregationFilePath(siteType, channel, endDate);
		BufferedReader br = FileHelper.readFile(input);
		String line = null;
		System.out.println("init CategoryStatItemsProvider...");
		while((line=br.readLine())!=null){
			Aggregation aggregation = Aggregation.parse(line);
			if(aggregation == null)
				continue;
			int productId = Integer.valueOf(aggregation.getKey());
			cidStatItemsMap.put(productId, aggregation.getStatItems());
		}
		br.close();
		System.out.println("CategoryStatItemsProvider size: " + cidStatItemsMap.size());
 	}
	
	// public methods
	public DeviceStatItem[] getStatItems(int categoryId){
		List<Integer> cidList = cpTree.getCategoryAncesterCategoryIncludingSelf(categoryId);
		cidList.add(-1);
		for(int cid : cidList)
			if(cidStatItemsMap.containsKey(cid))
				return cidStatItemsMap.get(cid);
		return null;
	}
	
	// main for test
	public static void main(String[] args) {
		
	}
}
