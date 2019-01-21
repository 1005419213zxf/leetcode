package com.litb.bid.component.adw.ppv.create;

import com.litb.basic.enums.SiteType;


public class Test {

	public static void main(String[] args) {
		SiteType siteType = SiteType.valueOf("hello,world");
		System.out.println(siteType);
	} 
	
//	private static final int PAGE_SIZE = 100;
//	
//	// TEST
//	public static void main(String[] args) throws Exception {
//		
//		AdwordsAccountManager accountManager = new AdwordsAccountManager();
//		for (AdwordsAccount account : accountManager.getAllAccounts()) {
//			AdWordsServices adWordsServices = new AdWordsServices();
//			AdWordsSession session = account.getAwSession();
//			runExample(adWordsServices, session);
//		}	
//	}
//	
//	public static void runExample(
//		      AdWordsServices adWordsServices, AdWordsSession session) throws Exception {
//		    // Get the CampaignService.
//		    CampaignServiceInterface campaignService =
//		        adWordsServices.get(session, CampaignServiceInterface.class);
//
//		    int offset = 0;
//
//		    // Create selector.
//		    SelectorBuilder builder = new SelectorBuilder();
//		    Selector selector = builder
//		        .fields(CampaignField.Id, CampaignField.Name)
//		        .orderAscBy(CampaignField.Name)
//		        .offset(offset)
//		        .limit(PAGE_SIZE)
//		        .build();
//
//		    CampaignPage page = null;
//		    do {
//		      // Get all campaigns.
//		      page = campaignService.get(selector);
//
//		      // Display campaigns.
//		      if (page.getEntries() != null) {
//		        for (Campaign campaign : page.getEntries()) {
//		          System.out.printf("Campaign with name '%s' and ID %d was found.%n", campaign.getName(),
//		              campaign.getId());
////		          System.out.println("\tCampaign status : " + campaign.getBudget());
//		        }
//		      } else {
//		        System.out.println("No campaigns were found.");
//		      }
//
//		      offset += PAGE_SIZE;
//		      selector = builder.increaseOffsetBy(PAGE_SIZE).build();
//		    } while (offset < page.getTotalNumEntries());
//		  }
}
