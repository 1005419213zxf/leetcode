package com.litb.bid.component.adw;

import com.litb.adw.lib.enums.FeedCountry;
import com.litb.adw.lib.operation.adgroup.criterion.AdwordsProductPartitionToBeUpdated;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;
import com.litb.basic.util.DateHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class NewItemIdsProvider {
	private Set<String> newSkuSet = new HashSet<String>();
	
	private static String getInputPath(Date endDate, SiteType siteType, FeedCountry country){
		return "/mnt/adwords/shopping_feed/" + siteType + "/" + DateHelper.getDateString(endDate, "yyyy") + "/" +  
				DateHelper.getDateString(endDate, "MM") + "/" + DateHelper.getShortDateString(endDate) + "/" + "adgroup_to_be_added/"
				+ country + "_offer_uploaded";
	}

	public NewItemIdsProvider(SiteType siteType) throws IOException {
		System.err.println("initializing NewItemIdsProvider...");
		Date newSkuDate = DateHelper.getShortDate("2016-03-24");
		if(siteType == SiteType.mini)
			newSkuDate = DateHelper.getShortDate("2016-03-25");
		for(FeedCountry country : FeedCountry.values()){
			System.out.println("processing " + country);
			for(boolean isSupply : new Boolean[]{true,false}){
				// each country
				String inputFilePath = getInputPath(newSkuDate, siteType, country);
				if(isSupply)
					inputFilePath += "_2";
				if(FileHelper.isFileExist(inputFilePath))
					System.out.println("input: " + inputFilePath);
				else{
					System.out.println("skip: " + inputFilePath);
					continue;
				}
				
				BufferedReader br = FileHelper.readFile(inputFilePath);
				String line = null;
				while((line = br.readLine()) != null){
					try {
						AdwordsProductPartitionToBeUpdated offer = AdwordsProductPartitionToBeUpdated.parse(line);
						newSkuSet.add(offer.getAccountId() + "\t" + offer.getCampaignId() + "\t" + offer.getAdgroupId() + "\t" + offer.getCriterionId());
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("line: " + line);
					}
				}
				br.close();
			}
		}
		System.out.println("finish, set: " + newSkuSet.size());
	}
	
	public boolean isNewItemId(String key){
		return newSkuSet.contains(key);
	}
	
}
