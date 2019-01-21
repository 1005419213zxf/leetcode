package com.litb.bid.component.adw.bi;

import com.litb.basic.cptree.CPTree;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;
import com.litb.bid.util.CpTreeFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaCrDivideBiCrProvider implements PlaCrDivideBiCrProviderInterface {
	
	private Map<String, Double> keyValueMap = new HashMap<String, Double>();
	
	// constructor
	public PlaCrDivideBiCrProvider(){
		keyValueMap.put("litb	-1", 0.7454066074252764);
		keyValueMap.put("litb	1180", 0.859030734060059);
		keyValueMap.put("litb	2619", 0.7390960414401767);
		keyValueMap.put("litb	3349", 0.939490757979823);
		keyValueMap.put("litb	35795", 0.5190984927320136);
		keyValueMap.put("litb	42061", 0.2886178861788618);
		keyValueMap.put("litb	71", 0.7748759189658413);
		keyValueMap.put("litb	75", 0.7654074936111496);
		keyValueMap.put("litb	76", 0.62948834507894);
		keyValueMap.put("mini	-1", 0.628625481163119);
		keyValueMap.put("mini	2624", 0.6721519898082108);
		keyValueMap.put("mini	3017", 0.6825843316699854);
		keyValueMap.put("mini	3021", 0.5276248634284453);
		keyValueMap.put("mini	3026", 0.5469860764223018);
		keyValueMap.put("mini	4676", 0.6897301919515781);
		keyValueMap.put("mini	4685", 0.4773361922509721);
		keyValueMap.put("mini	4861", 0.6373047865657657);
		keyValueMap.put("mini	5029", 0.49205963874775915);
		keyValueMap.put("mini	8017", 0.691319075933855);
	}

	@Override
	public Double getPlaCrDivideBiCrData(SiteType siteType, int categoryId) {
		try {
			CPTree cpTree = CpTreeFactory.getCategoryCpTree(siteType);
			List<Integer> pcidList = cpTree.getCategoryAncesterCategoryIncludingSelf(categoryId);
			pcidList.add(-1);
			for(int pcid : pcidList){
				Double value = keyValueMap.get(siteType + "\t" + pcid);
				if(value != null)
					return value;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) throws IOException {
		String inputFilePath = "E:\\Download_tmp\\bi_cr_pla_cr_ratio_90_min_conv_2";
		BufferedReader br = FileHelper.readFile(inputFilePath);
		String line;
		while((line = br.readLine()) != null){
			String[] strArr = line.split("\t");
			System.out.println("keyValueMap.put(\"" + strArr[0] + "\t" + strArr[1] + "\", " + strArr[4] + ");");
		}
		br.close();
		
		PlaCrDivideBiCrProviderInterface interface1 = new PlaCrDivideBiCrProvider();
		System.out.println(interface1.getPlaCrDivideBiCrData(SiteType.litb, 5330));
	}
}
