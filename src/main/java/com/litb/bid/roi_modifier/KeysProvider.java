package com.litb.bid.roi_modifier;


import com.litb.basic.cptree.CPTree;
import com.litb.bid.Conf;
import com.litb.bid.object.adw.BiddableObject;
import com.litb.bid.util.CpTreeFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KeysProvider {
	public static List<String> getDuplicatedKeys(BiddableObject biddableObject) throws SQLException{
		List<String> keys = new ArrayList<String>();
		//category
		int category = biddableObject.getCategoryId();
		CPTree cpTree = CpTreeFactory.getCategoryCpTree(biddableObject.getSiteType());
		List<Integer> cidList = cpTree.getCategoryAncesterCategoryIncludingSelf(category);
		if(!cidList.contains(Conf.ROOT_CID))
			cidList.add(Conf.ROOT_CID);
		int index = cidList.size() > 4 ? cidList.size() - 4 : 0;
		for(int i = index; i < cidList.size();i++ ){
			keys.add("category:"+cidList.get(i));
		}
		//country
//		FeedCountry country = AdwordsCampaignNameHelper.getFeedCountry(biddableObject.getCampaignName());
//		keys.add("country:"+country);
//		//maxCpc
//		double maxCpc = biddableObject.getMaxCpc();
//		keys.add("maxCpc:"+maxCpc);
		
		return keys;
	}
	
	public static void main(String[] args) {
		List<Integer> list = new ArrayList<Integer>();
		list.add(4);
		list.add(3);
		list.add(2);
		list.add(1);
		list.add(-1);
		int index = list.size() > 4 ? list.size() - 4 : 0;
		for(int i = index; i < list.size();i++ ){
			System.out.println(list.get(i));
		}
	}
}
