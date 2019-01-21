package com.litb.bid.component.adw;

import com.litb.bid.Conf;
import com.litb.bid.util.CpTreeFactory;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.enums.SiteType;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class PcCrDivideMobileCrProvider {
	private static Map<String, Double> siteCidRatioMap = new HashMap<String, Double>();
	static{
		siteCidRatioMap.put("litb\t-1",  2.393544475);
		siteCidRatioMap.put("litb\t1180",  3.243972048);
		siteCidRatioMap.put("litb\t2619",  2.015126299);
		siteCidRatioMap.put("litb\t3349",  2.302735285);
		siteCidRatioMap.put("litb\t35795",  1.301167772);
		siteCidRatioMap.put("litb\t71",  2.437812374);
		siteCidRatioMap.put("litb\t75",  2.630679256);
		siteCidRatioMap.put("litb\t76",  2.336844117);

		siteCidRatioMap.put("mini\t-1",  2.266962092);
		siteCidRatioMap.put("mini\t2624",  2.507261281);
		siteCidRatioMap.put("mini\t3017",  2.460616556);
		siteCidRatioMap.put("mini\t3021",  2.023954618);
		siteCidRatioMap.put("mini\t3026",  2.373656483);
		siteCidRatioMap.put("mini\t4676",  1.952680213);
		siteCidRatioMap.put("mini\t4685",  3.212236695);
		siteCidRatioMap.put("mini\t4861",  2.935299277);
		siteCidRatioMap.put("mini\t5029",  2.168894664);
		siteCidRatioMap.put("mini\t8017",  2.349441036);
	}
	
	public double getPcCrAgainstMobileCrRatio(SiteType siteType, int cid){
		try {
			CPTree cpTree = CpTreeFactory.getCategoryCpTree(siteType);
			List<Integer> cidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
			cidList.add(Conf.ROOT_CID);
			for(int categoryId : cidList){
				if(siteCidRatioMap.containsKey(siteType+"\t"+categoryId))
					return siteCidRatioMap.get(siteType+"\t"+categoryId);
			}
			return 1.0;
		} catch (SQLException e) {
			e.printStackTrace();
			return 1.0;
		}
	}
	
	public static void main(String[] args) {
		PcCrDivideMobileCrProvider provider = new PcCrDivideMobileCrProvider();
		
		Scanner in = new Scanner(System.in);
		System.out.println("input : [Site Type] [cid]");
		while(in.hasNext()) {
			String line = in.nextLine();
			if(line.equals("exit") || line.equals("quit"))
				break;
			try {
				String[] arr = line.split(" ");
				SiteType siteType = SiteType.valueOf(arr[0]);
				int cid = Integer.parseInt(arr[1]);
				
				System.out.println("result: " + provider.getPcCrAgainstMobileCrRatio(siteType, cid));
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("input : [Site Type] [cid]");
			}
		}
	}
}
