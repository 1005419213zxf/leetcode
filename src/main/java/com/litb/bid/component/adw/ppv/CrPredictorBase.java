package com.litb.bid.component.adw.ppv;

import com.litb.bid.Conf;
import com.litb.bid.util.CpTreeFactory;
import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

public class CrPredictorBase implements CrPredictorInterface{
	
	protected static final int INTERVAL = 30;
	
	protected static final String TMP_BUCKET_NAME = "litb.adwords.test";
	
	protected SiteType siteType;
	protected CPTree cpTree;
	protected HashMap<String, PredictiveItem> keyItemMap = new HashMap<String, PredictiveItem>();// KEY = channel + language + platform + cid

	// constructors
	public CrPredictorBase(SiteType siteType) throws SQLException {
		this.siteType = siteType;
		this.cpTree = CpTreeFactory.getProductCpTree(siteType);
	}
	
	// private static methods
	private static List<Integer> ppvUvMapToUvList(Map<Integer, Integer> ppvUvMap){
		Integer[] uvArray = new Integer[Conf.PPV_DIMENSION];
		int i = 0;
		while(i<Conf.PPV_DIMENSION)
			uvArray[i++] = 0;
		for(Entry<Integer, Integer> entry : ppvUvMap.entrySet())
			uvArray[entry.getKey()] = entry.getValue();
		List<Integer> uvList = Arrays.asList(uvArray);
		return uvList;
	}

	// public methods
	@Override
	public PredictiveCrInfo getPredictiveCr(AdwordsChannel channel, LanguageType languageType, Boolean isFromMobileDevice,
			int categoryId, Map<Integer, Integer> ppvUvMap) {
		List<Integer> uvList = ppvUvMapToUvList(ppvUvMap);
		return getPredictiveCr(channel, languageType, isFromMobileDevice, categoryId, uvList);
	}
	
	@Override
	public PredictiveCrInfo getPredictiveCr(AdwordsChannel channel, LanguageType languageType, Boolean isFromMobileDevice,
			int categoryId, List<Integer> uvList) {
		
		PredictiveItem item = getItemPreMethod(channel, languageType, isFromMobileDevice, categoryId);
		if (item == null)
			return null;
		
		// calculate
		double[] crArr = item.getCr();
		double predictedCr = .0;
		int sum = 0;
		for (int i = 0; i < Conf.PPV_DIMENSION ; i++) {
			sum += uvList.get(i);
			predictedCr += (double)uvList.get(i) * crArr[i];
		}
		predictedCr /= (double)sum;
		item.setPredictiveCr(predictedCr);
		
		// output
		PredictiveCrInfo info = new PredictiveCrInfo();
		info.setSiteType(siteType);
		info.setChannel(item.getChannel());
		info.setLanguageType(item.getLanguageType());
		info.setCategoryId(categoryId);
		info.setIsMobile(item.isFromMobileDevice());
		info.setPredictiveCr(predictedCr);
		
		return info;
	}
	
	// protected methods
	protected void initMap(BufferedReader br) throws NumberFormatException, IOException{
		String line = null;
		while ((line = br.readLine()) != null) {
			try {
				PredictiveItem item = PredictiveItem.parse(line);
				AdwordsChannel channel = item.getChannel();
				LanguageType languageType = item.getLanguageType();
				Boolean isFromMobileDevice = item.isFromMobileDevice();
				String cpTag = item.getCpTag();
				char c = cpTag.charAt(0);
				List<Integer> pcidList = new ArrayList<Integer>();
				if (cpTag.equals("null")) {
					;
				}
				else if (c == 'c') {
					int cid = Integer.parseInt(cpTag.substring(1));
					pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
				} else {
					int pid = Integer.parseInt(cpTag.substring(1));
					pcidList = cpTree.getProductAllAncesterCategoriesExcludingSoftcopy(pid);
				}
				if (!pcidList.contains(-1))
					pcidList.add(-1);
				
				for (Boolean isM : (isFromMobileDevice != null ? new Boolean[] { isFromMobileDevice, null } : new Boolean[] { null }))
					for (AdwordsChannel ch : (channel != null ? new AdwordsChannel[] { channel, null } : new AdwordsChannel[] { null }) ) {
						for (LanguageType lang : (languageType != null ? new LanguageType[] { languageType, null } : new LanguageType[] { null })) {
							for (int cid : pcidList) {
								String key = getKey(ch, lang, isM, cid);
								if(keyItemMap.containsKey(key) == false) {
									PredictiveItem tmpItem = new PredictiveItem(ch, lang, isM, "c"+cid);
									tmpItem.setChannel(ch);
									tmpItem.setLanguageType(lang);
									tmpItem.setFromMobileDevice(isM);
									tmpItem.setCpTag("c" + cid);
									tmpItem.setCr(item.getCr());
									tmpItem.setOrderedUvNumber(item.getOrderedUvNumber());
									tmpItem.setUvNumber(item.getUvNumber());
									keyItemMap.put(key, tmpItem);
								} else {
									PredictiveItem innerItem = keyItemMap.get(key);
									innerItem.merger(item);
								}
								if (isM != null) {
									key = getKey(ch, lang, null, cid);
									if (keyItemMap.containsKey(key) == false) {
										PredictiveItem tmpItem = new PredictiveItem(ch, lang, null, "c" + cid);
										tmpItem.merger(item);
										keyItemMap.put(key, tmpItem);
									} else {
										PredictiveItem tmpItem = keyItemMap.get(key);
										tmpItem.merger(item);
									}
								}
							}
						}
					}
			} catch (Exception e) {
				System.err.println("LINE INFO: " + line);
				e.printStackTrace();
			}
			
		}
		System.out.println(this.siteType + "__--__--__--__" + keyItemMap.size());
	}
	
	protected void summaryAndFitting() {
		// summary & fitting
		for(PredictiveItem item : keyItemMap.values()){
			item.summary();

//			double[] fittingCrArray = FittingTools.getFittingCrArray(item.getCr());
//			item.setCr(fittingCrArray);
//			double[] fittingCrArray = FittingTools.getAdjustedCrArray(item);
//			item.setCr(fittingCrArray);
		}
	}
	
	protected static String getKey(AdwordsChannel channel, LanguageType languageType, Boolean isFromMobileDevice, int cid) {
		return channel + "\t" + languageType + "\t" + isFromMobileDevice + "\tc" + cid;
	}
	
	private boolean meetOrderNumberCondition(PredictiveItem item) {
		// Filter uvNum
		int sumUv = 0;
		for (int uvNum : item.getUvNumber())
			sumUv += uvNum;
		
		// Filter cr zero appear time
		int zeroCrPointNum = 0;
		for (double num : item.getCr()) {
			if (num <= 1e-10)
				zeroCrPointNum ++;
		}
		if (sumUv < 100 || zeroCrPointNum > 10)
			return false;
		else
			return true;
	}
	
	private PredictiveItem getItemPreMethod(AdwordsChannel channel, LanguageType languageType, Boolean isFromMobileDevice, int cid) {
		PredictiveItem item = null;
		
		List<Integer> pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
		pcidList.add(-1);
		
		for (Boolean isM : (isFromMobileDevice != null ? new Boolean[] { isFromMobileDevice, null } : new Boolean[] { null })){
			for (AdwordsChannel ch : (channel != null ? new AdwordsChannel[] { channel, null } : new AdwordsChannel[] { null }) ) {
				for (int pcid : pcidList) {
					for (LanguageType lang : (languageType != null ? new LanguageType[] { languageType, null } : new LanguageType[] { null })) {
						String key = getKey(ch, lang, isM, pcid);
						if (keyItemMap.containsKey(key)) {
//							System.out.println("KEY : " + key);
							item = keyItemMap.get(key);

							if (item != null && meetOrderNumberCondition(item))
								return item;
						}
					}
				}
			}
		}
		return null;
	}

}
