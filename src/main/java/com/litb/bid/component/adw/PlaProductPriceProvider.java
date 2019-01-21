package com.litb.bid.component.adw;

import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;
import com.litb.dm.aws.s3.AS3FileHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaProductPriceProvider {
	private Map<String, Double> sitetypePidPriceMap = new HashMap<String, Double>();

	public PlaProductPriceProvider() {
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void init() throws IOException{
		String s3Dir = "s3://litbweblog/hive/tables/dim_prod_perf_lsin/";
		String localPriceFile = "/mnt/adwords/auto_bidding/abtest/data/product_price_info";
		List<String> s3FileList = AS3FileHelper.getObjectList(s3Dir);
		System.out.println(s3FileList);
		for(String key : s3FileList){
			AS3FileHelper.copyS3FileToLocal("s3://litbweblog/"+key, localPriceFile);
		}
		
		if(FileHelper.isFileExist(localPriceFile)){
			BufferedReader br = FileHelper.readFile(localPriceFile);
			String line = null;
			while((line=br.readLine())!=null){
				String[] vals = line.split("\t");
				try {
					if(vals.length < 16)
						continue;
					int pid = Integer.valueOf(vals[1]);
					SiteType siteType = SiteType.getSiteType(Integer.valueOf(vals[2]));
					if(vals[15] == null || vals[15].length() <= 0)
						continue;
					Double price = Double.valueOf(vals[15]);
					if(vals.length == 17 && vals[16] != null && vals[16].length() > 0)
						price = Double.valueOf(vals[16]);
					sitetypePidPriceMap.put(siteType + "\t" + pid, price);
				} catch (Exception e) {
					if(e.getMessage() != null && e.getMessage().toLowerCase().contains("illegal site type code"))
						continue;
					System.out.println(line);
					e.printStackTrace();
				}
			}
			br.close();
		}
	}
	
	public double getProductBidModifier(SiteType siteType, int pid){
		String key = siteType + "\t" + pid;
		double modifier = 1.0;
		if(sitetypePidPriceMap.containsKey(key)){
			double price = sitetypePidPriceMap.get(key);
			if(price <= 4){
				modifier = (2 - Math.pow(0.78/(price+1), 2) - 0.39) / 1.6;
			}
		}
		return modifier;
	}
	
	public static void main(String[] args) {
		PlaProductPriceProvider plaProductPriceProvider = new PlaProductPriceProvider();
		System.out.println(plaProductPriceProvider.getProductBidModifier(SiteType.litb, Integer.valueOf(args[0])));
	}
}
