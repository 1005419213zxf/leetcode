package com.litb.bid.component.adw;


import com.litb.bid.DirDef;
import com.litb.bid.object.adw.Aggregation;
import com.litb.bid.object.adw.DeviceStatItem;
import com.litb.bid.util.CpTreeFactory;
import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.adw.lib.enums.FeedCountry;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;
import com.litb.basic.util.DateHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class CategoryConversionGroupDataProvider implements CategoryConversionGroupDataProviderInterface {
	private Map<String, Aggregation> keyMap = new HashMap<String, Aggregation>();
	private CPTree cpTree;

	public CategoryConversionGroupDataProvider(SiteType siteType, Date endDate) throws IOException, SQLException {
		System.out.println("init CategoryConversionGroupDataProvider...");
		for (AdwordsChannel channel : Arrays.asList(AdwordsChannel.search, AdwordsChannel.pla)) {
			String categoryConversionDataPath = DirDef.getLocalBiddableObjectCategoryConversionGroupFilePath(siteType, channel, endDate);
			if (!FileHelper.isFileExist(categoryConversionDataPath))
				continue;
			BufferedReader br = FileHelper.readFile(categoryConversionDataPath);
			String line = null;
			while((line=br.readLine())!=null){
				Aggregation aggregation = Aggregation.parse(line);
				if(!keyMap.containsKey(aggregation.getKey()))
					keyMap.put(aggregation.getKey(), aggregation);
			}
			br.close();
		}
		this.cpTree = CpTreeFactory.getCategoryCpTree(siteType);
		System.out.println("init CategoryConversionGroupDataProvider...done. " + keyMap.size());
	}

	@Override
	public DeviceStatItem[] getPlaCategoryConversionGroupData(FeedCountry country, int cid, int avgConvDays) {
		List<Integer> pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
		pcidList.add(-1);
		Aggregation aggregation = null;
		for (int pcid : pcidList) {
			aggregation = keyMap.get(avgConvDays + "\t" + pcid + "\t" + country);
			if(aggregation != null)
				return aggregation.getStatItems();
		}
		return null;
	}

	@Override
	public DeviceStatItem[] getSearchCategoryConversionGroupData(LanguageType languageType, int cid, int avgConvDays) {
		List<Integer> pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
		pcidList.add(-1);
		Aggregation aggregation = null;
		for (int pcid : pcidList) {
			aggregation = keyMap.get(avgConvDays + "\t" + pcid + "\t" + languageType);
			if(aggregation != null)
				return aggregation.getStatItems();
		}
		return null;
	}	

	public static void main(String[] args) throws IOException, SQLException {
		Date endDate = null;
		try {
			endDate = DateHelper.getShortDate(args[0]);
		} catch (Exception e) {
			System.err.println("Usage: <end date>");
			System.exit(1);
		}
		CategoryConversionGroupDataProvider litbProvider = new CategoryConversionGroupDataProvider(SiteType.litb, endDate);
		CategoryConversionGroupDataProvider miniProvider = new CategoryConversionGroupDataProvider(SiteType.mini, endDate);

		Scanner in = new Scanner(System.in);
		System.out.println("input : [site] [country / language] [category_id] [avgConvDays]");
		while(in.hasNext()) {
			try {
				String line = in.nextLine();
				if(line.equals("exit") || line.equals("quit"))
					break;

				String[] strArr = line.split(" ");
				SiteType siteType = SiteType.valueOf(strArr[0]);
				FeedCountry country = null;
				LanguageType languageType = null;
				try {
					languageType = LanguageType.valueOf(strArr[1]);
				} catch (Exception e) {}
				try {
					country = FeedCountry.valueOf(strArr[1]);
				} catch (Exception e) {}

				int cid = Integer.parseInt(strArr[2]);
				int avgConvDays = Integer.parseInt(strArr[3]);

				CategoryConversionGroupDataProvider provider = (siteType == SiteType.mini ? miniProvider : litbProvider);
				DeviceStatItem[] resArr = null;
				if (languageType != null) {
					resArr = provider.getSearchCategoryConversionGroupData(languageType, cid, avgConvDays);
				}
				if (country != null) {
					resArr = provider.getPlaCategoryConversionGroupData(country, cid, avgConvDays);
				}

				System.out.println("result");
				if(resArr == null)
					System.out.println("null");
				else {
					for(DeviceStatItem item : resArr)
						System.out.println(item.toString());
				}
			}catch(Exception exception){
				exception.printStackTrace();
			}
			System.out.println("input : [site] [country / language] [category_id] [avgConvDays]");
		}
		in.close();
		System.exit(0);
	}

}
