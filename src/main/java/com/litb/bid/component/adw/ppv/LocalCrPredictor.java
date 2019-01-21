package com.litb.bid.component.adw.ppv;


import com.litb.bid.Conf;
import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class LocalCrPredictor extends CrPredictorBase {
	
	public LocalCrPredictor(SiteType siteType, String inputLocalDir) throws SQLException, IOException {
		super(siteType);
		
		for(String inputFilePath : FileHelper.getFilePathsInOneDir(inputLocalDir)){
			BufferedReader br = new BufferedReader(new FileReader(new File(inputFilePath)));
			initMap(br);
			br.close();
		}
		summaryAndFitting();

		System.out.println("after init map : " + keyItemMap.size());
	}
	
	// main for test
	public static void main(String[] args) throws SQLException, IOException {
		String localFileDir1 = null;
		String localFileDir2 = null;
		try {
			localFileDir1 = args[0];
			localFileDir2 = args[1];
		} catch (Exception e) {
			System.err.println("Usage : <litb local file dir> <mini local file dir>");
			System.exit(1);
		}
		LocalCrPredictor litbPredictor = new LocalCrPredictor(SiteType.litb, localFileDir1);
		LocalCrPredictor miniPredictor = new LocalCrPredictor(SiteType.mini, localFileDir2);
		
		System.out.println("Usage : <siteType> <channel> <language> <isMobile(1|0|-1)> <cid>");
		Scanner in = new Scanner(System.in);
		while (in.hasNext()) {
			String line = in.nextLine();
			String[] arr = line.split(" ");
			int idx = 0;
			try {
				SiteType siteType = SiteType.valueOf(arr[idx++]);
				LocalCrPredictor predictor = siteType == SiteType.litb ? litbPredictor : miniPredictor;
				AdwordsChannel channel = AdwordsChannel.valueOf(arr[idx++]);
				LanguageType languageType = LanguageType.valueOf(arr[idx++]);
				Boolean isFromMobileDevice = null;
				String flag = arr[idx++];
				if (flag.equals("1")) 
					isFromMobileDevice = true;
				else if (flag.equals("0")) 
					isFromMobileDevice = false;
				int categoryId = Integer.parseInt(arr[idx++]);
				ArrayList<Integer> uvList = new ArrayList<Integer>();
				for (int i = 0; i < Conf.PPV_DIMENSION; i ++) 
					uvList.add(100);
				PredictiveCrInfo info = predictor.getPredictiveCr(channel, languageType, isFromMobileDevice, categoryId, uvList);
				System.out.println(info.toString());
				System.out.println("predictive cr : " + info.getPredictiveCr());
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
