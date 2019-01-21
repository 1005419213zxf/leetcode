package com.litb.bid.component.adw.ppv;


import com.litb.bid.Conf;
import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.basic.util.DateHelper;
import com.litb.bid.component.adw.ppv.create.PpvCrCubeStator;
import com.litb.dm.aws.s3.AS3FileHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class S3CrPredictor extends CrPredictorBase {
	protected static final Date FIRST_DATE = DateHelper.getShortDate("2015-01-01");
	
	// constructor
	public S3CrPredictor(SiteType siteType) throws IOException, SQLException {
		super(siteType);
		
		Date nowDate = new Date();
		boolean existFile = false;
		String inputFilePath = null;
		for(Date date=DateHelper.addDays(-31, nowDate); !FIRST_DATE.after(date); date=DateHelper.addDays(-1, date)) {
			Date endDate = date;
			Date beginDate = DateHelper.addDays(1-INTERVAL, endDate);
			String tmpInputPath = PpvCrCubeStator.getOutputFileDir(siteType, beginDate, endDate);
			if(AS3FileHelper.isFileExist(tmpInputPath)) {
				existFile = true;
				inputFilePath = tmpInputPath;
				System.out.println(tmpInputPath);
				break;
			}
		}
		if (existFile == false) {
			System.out.println("cannot found any input data from ever history data");
			System.exit(1);
		} 
		else {
			List<String> files = AS3FileHelper.getObjectList(inputFilePath);
			System.out.println(siteType + ": FILE_SIZE : " + files.size());
			for(String key : files) {
				System.out.println("NOW SCAN TO : " + key);
				BufferedReader br = AS3FileHelper.readFile(TMP_BUCKET_NAME, key);
				initMap(br);
				br.close();
			}
			summaryAndFitting();
		}
	}
	
	// main for test
	public static void main(String[] args) throws SQLException, IOException {	
		S3CrPredictor litbPredictor = new S3CrPredictor(SiteType.litb);
		S3CrPredictor miniPredictor = new S3CrPredictor(SiteType.mini);
		System.out.println("Usage : <siteType> <channel> <language> <isMobile(1|0|-1)> <cid>");
		Scanner in = new Scanner(System.in);
		while (in.hasNext()) {
			String line = in.nextLine();
			String[] arr = line.split(" ");
			int idx = 0;
			try {
				SiteType siteType = SiteType.valueOf(arr[idx++]);
				S3CrPredictor predictor = siteType == SiteType.litb ? litbPredictor : miniPredictor;
				AdwordsChannel channel = AdwordsChannel.valueOf(arr[idx++]);
				LanguageType languageType = LanguageType.valueOf(arr[idx++]);
				Boolean isFromMobileDevice = null;
				String flag = arr[idx++];
				if (flag.equals("1")) isFromMobileDevice = true;
				else if (flag.equals("0")) isFromMobileDevice = false;
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
		
		
//		PpvCrS3Provider litbProvider = new PpvCrS3Provider(SiteType.litb);
//		PpvCrS3Provider miniProvider = new PpvCrS3Provider(SiteType.mini);
//		
//		System.out.println("usage: <site type--litb or mini> <channel> <languageType> <isFromMobileDevice 1 or 0> <cpTag>");
//		
//		Scanner in = new Scanner(System.in);
//		
//		while (in.hasNext()) {
//			String line = in.nextLine();
//			if(line.length() >= 3 && line.substring(0, 3).equals("all")) {
//				try {
//					StringTokenizer st = new StringTokenizer(line);
//					st.nextToken();
//					int cnt = Integer.parseInt(st.nextToken());
//					
//					List<Integer> uvList = new ArrayList<Integer>();
//					for (int i=0; i<26; i++) {
//						uvList.add(100);
//					}
//						
//					for (PpvCrS3Provider provider : new PpvCrS3Provider[] { litbProvider, miniProvider }) {
//						HashMap<String, PredictiveCRInfo> map = provider.keyItemMap;
//						for (String key : map.keySet()) {
//							PredictiveCRInfo item = map.get(key);
//							double[] cr = item.getCr();
//							System.out.print(key);
//							for (int i=0; i<26; i++) 
//								System.out.print(" " + cr[i]);
//							System.out.println("");
//							cnt --;
//							if (cnt <= 0) 
//								throw new Exception();
//						}
//					}
//				} catch (Exception e) {
//					continue;
//				}
//			} else if (line.length() >= 6 && line.substring(0, 6).equals("mapKey")) {
//				StringTokenizer st = new StringTokenizer(line);
//				if(st.countTokens() != 2) {
//					System.err.println("mapKey format error");
//					continue;
//				} else {
//					st.nextToken();
//					int cnt = Integer.parseInt(st.nextToken());
//					HashMap<String, PredictiveCRInfo> map = litbProvider.keyItemMap;
//					for (String theKey : map.keySet()) {
//						System.out.println("KEY : " + theKey);
//						cnt --;
//						if (cnt <= 0) 
//							break;
//					}
//				}
//			} else {
//				StringTokenizer st = new StringTokenizer(line);
//				try {
//					if(st.countTokens() != 5)
//						throw new Exception("token count != 5, is " + st.countTokens());
//					SiteType siteType = SiteType.valueOf(st.nextToken());
//					if(siteType == null || siteType != SiteType.litb && siteType != SiteType.mini)
//						throw new Exception("siteType != litb or mini");
//					String channelString = st.nextToken();
//					AdwordsChannel channel = null;
//					if (channelString.equals("null") || channelString.equals("-1")) {
//						;
//					} else {
//						try {
//							channel = AdwordsChannel.valueOf(channelString);
//						} catch (IllegalArgumentException e) {
//							throw new IllegalArgumentException("channel illegal");
//						}
//					}
//					String languageTypeString = st.nextToken();
//					LanguageType languageType = null;
//					if (languageTypeString.equals("null") || languageTypeString.equals("-1")) {
//						;
//					} else {
//						try {
//							languageType = LanguageType.valueOf(languageTypeString);
//						} catch (IllegalArgumentException e) {
//							throw new IllegalArgumentException("languageType illegal");
//						}
//					}
//					String isFromMobileDeviceString = st.nextToken();
//					Boolean isFromMobileDevice = null;
//					if (isFromMobileDeviceString.equals("-1") || isFromMobileDeviceString.equals("null"))
//						;
//					else if (isFromMobileDeviceString.equals("1"))
//						isFromMobileDevice = true;
//					else if (isFromMobileDeviceString.equals("0")) 
//						isFromMobileDevice = false;
//					else
//						throw new Exception("isFromMobile string not 1 or 0");
//					String cpTag = st.nextToken();
//					PpvCrS3Provider provider = (siteType == SiteType.litb) ? litbProvider : miniProvider;
//					List<Integer> uvList = new ArrayList<Integer>();
//					for (int i=0; i<26; i++)
//						uvList.add(100);
//					PredictiveCRInfo item = provider.getPredictedCrResult(channel, languageType, isFromMobileDevice, cpTag, uvList);
//					
//					// debug
//					System.out.println("[debug]");
//					System.out.println("channel : " + channel);
//					System.out.println("language : " + languageType);
//					System.out.println("isMobile : " + isFromMobileDevice);
//					System.out.println("cpTag : " + cpTag);
//					
//					// TEST
//					if (item != null) {
//						System.out.println(siteType + ":\t" + item.toString(true));
//					}
//					else 
//						System.out.println(siteType + ": null");
//					
//				} catch (Exception e) {
//					System.out.println("usage: <site type--litb or mini> <channel> <languageType> <isFromMobileDevice 1 or 0> <cpTag>");
//					e.printStackTrace();
//				}
//			}
//		}
	}
}
