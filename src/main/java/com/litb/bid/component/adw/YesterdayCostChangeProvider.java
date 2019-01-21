package com.litb.bid.component.adw;

import com.litb.bid.DirDef;
import com.litb.basic.io.FileHelper;
import com.litb.basic.util.DateHelper;
import com.litb.bid.object.adw.BiCost;

import java.io.BufferedReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class YesterdayCostChangeProvider {
	private Map<String, BiCost> keyYestodayAndBeforeCostMap = new HashMap<String, BiCost>();
	private Date endDate;

	public YesterdayCostChangeProvider(Date endDate) {
		this.endDate = endDate;
		init();
	}
	
	private void init(){
		System.out.println("init " + YesterdayCostChangeProvider.class.getSimpleName() + "...");
		String YestodayAndBeforeCostFilePath = DirDef.getDailyDir(endDate) + "bi_yestoday_and_before_cost";
		try {
			BufferedReader br = FileHelper.readFile(YestodayAndBeforeCostFilePath);
			String line = null;
			while((line=br.readLine())!=null){
				String[] vals = line.split("\t");
				long accountId = Long.valueOf(vals[0]);
				long campaignId = Long.valueOf(vals[1]);
				long adgroupId = Long.valueOf(vals[2]);
				long criterionId = Long.valueOf(vals[3]);
				double yesCost = Double.valueOf(vals[4]);
				double beforeYesCost = Double.valueOf(vals[5]);
				
				BiCost biCost = new BiCost();
				biCost.yestodayCost = yesCost;
				biCost.dayBeforeYestodayCost = beforeYesCost;
				String key = accountId + "\t" + campaignId + "\t" + adgroupId + "\t" + criterionId;
				keyYestodayAndBeforeCostMap.put(key, biCost);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("finish, size: " + keyYestodayAndBeforeCostMap.size());
	}
	
	public BiCost getYesAndBeforeYesCost(String key){
		if(keyYestodayAndBeforeCostMap.containsKey(key))
			return keyYestodayAndBeforeCostMap.get(key);
		return null;
	}
	
	public static void main(String[] args) {
		YesterdayCostChangeProvider yesterdayCostChangeProvider = new YesterdayCostChangeProvider(DateHelper.getShortDate("2018-02-26"));
		BiCost biCost = yesterdayCostChangeProvider.getYesAndBeforeYesCost(2268590490L + "\t" + 785646238L + "\t" + 41418893192L + "\t" + 53003294455L);
		System.out.println(biCost.yestodayCost + "\t" + biCost.dayBeforeYestodayCost);
	}
}
