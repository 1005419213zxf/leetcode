package com.litb.bid.component.adw.bi;

import com.litb.basic.db.DBHelper;
import com.litb.basic.db.DBPool;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Import {
	private static final SiteType SITE_TYPE = SiteType.litb;
	private static final String TAREGT_DATE_STRING = "2016-03-28";
	
	public static void main(String[] args) throws IOException, SQLException {
		// scan
		String inputFilePath = DailyStat.getOutputFilePath(SITE_TYPE, TAREGT_DATE_STRING);
		System.out.println("input: " + inputFilePath);
		BufferedReader br = FileHelper.readFile(inputFilePath);
		String line;
		List<String> sqlList = new ArrayList<String>(); 
		while((line = br.readLine()) != null){
			sqlList.add(line);
		}
		br.close();
		
		System.out.println("size: " + sqlList.size());
		
		// import
		DBHelper dbHelper = DBPool.getCnBiMasterDbHelperWithMarketingRole();
		dbHelper.executeUpdates(sqlList);
		dbHelper.close();
		
		System.out.println("Done.");
	}
}
