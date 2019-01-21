package com.litb.bid.util;

import com.litb.bid.Conf;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.cptree.CPTree.ProductStatus;
import com.litb.basic.db.DBHelper;
import com.litb.basic.enums.SiteType;
import com.litb.dm.aws.s3.AS3FileHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Offer CpTree instance.
 * @author Rui Zhang
 */
public class CpTreeFactory {
	private static List<CpTreeInstance> litbInstanceList = new ArrayList<CpTreeInstance>();
	private static List<CpTreeInstance> miniInstanceList = new ArrayList<CpTreeInstance>();
	private static List<CpTreeInstance> ezbuymyInstanceList = new ArrayList<CpTreeInstance>();
	private static List<CpTreeInstance> ezbuythstanceList = new ArrayList<CpTreeInstance>();
	private static List<CpTreeInstance> ezbuysgstanceList = new ArrayList<CpTreeInstance>();
	private static final String AS3_LITB_CPTREE_FILE = "s3://litb.auto.bidding/adwords/db/cptree/cptree_litb.gz";
	private static final String AS3_MINI_CPTREE_FILE = "s3://litb.auto.bidding/adwords/db/cptree/cptree_mini.gz";
	private static final String AS3_EZBUYMY_CPTREE_FILE = "s3://litb.auto.bidding/adwords/db/cptree/cptree_ezbuymy.gz";
	private static final String AS3_EZBUYTH_CPTREE_FILE = "s3://litb.auto.bidding/adwords/db/cptree/cptree_ezbuyth.gz";
	private static final String AS3_EZBUYSG_CPTREE_FILE = "s3://litb.auto.bidding/adwords/db/cptree/cptree_ezbuysg.gz";
	
	// public methods
	public static synchronized CPTree getCategoryCpTree(SiteType siteType) throws SQLException{
//		return getCpTree(siteType, true, true, true, false, ProductStatus.onSale);
		if(siteType == SiteType.ezbuymy || siteType == SiteType.ezbuyth || siteType == SiteType.ezbuysg)
			try {
				return getCategoryCpTreeFromAS3(siteType);
			} catch (Exception e) {
				e.printStackTrace();
			}
		return getCpTree(siteType, true, true, true, false, ProductStatus.onSale);
	}
	public static synchronized CPTree getProductCpTree(SiteType siteType) throws SQLException{
		if(siteType == SiteType.ezbuymy || siteType == SiteType.ezbuyth || siteType == SiteType.ezbuysg)
			try {
				return getProductCpTreeFromAS3(siteType);
			} catch (IOException e) {
				e.printStackTrace();
			}
		return getCpTree(siteType, false, true, true, true, ProductStatus.onSale);
	}
	
	public static synchronized CPTree getCategoryCpTree(SiteType siteType, boolean considerSoftLink) throws SQLException{
		if(siteType == SiteType.ezbuymy || siteType == SiteType.ezbuyth || siteType == SiteType.ezbuysg)
			try {
				return getCategoryCpTreeFromAS3(siteType);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return getCpTree(siteType, true, true, considerSoftLink, false, ProductStatus.onSale);
	}
	public static synchronized CPTree getProductCpTree(SiteType siteType,boolean considerSoftLink) throws SQLException{
		if(siteType == SiteType.ezbuymy || siteType == SiteType.ezbuyth || siteType == SiteType.ezbuysg)
			try {
				return getProductCpTreeFromAS3(siteType);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return getCpTree(siteType, false, true, considerSoftLink, true, ProductStatus.onSale);
	}
	
	public static synchronized CPTree getCategoryCpTreeFromAS3(SiteType siteType) throws SQLException, IOException{
//		return getCpTree(siteType, true, true, true, false, ProductStatus.onSale);
		return getCpTreeFromAS3(siteType, true, true, true, false, ProductStatus.onSale);
	}
	public static synchronized CPTree getProductCpTreeFromAS3(SiteType siteType) throws SQLException, IOException{
		return getCpTreeFromAS3(siteType, false, true, true, true, ProductStatus.onSale);
	}
	
	// private methods
	private static synchronized CPTree getCpTree(SiteType siteType, boolean onlyCategory, boolean onlyValidCategory, boolean considerSoftLink, boolean considerSoftcopy, ProductStatus productStatus) throws SQLException{
		// try to reuse
		List<CpTreeInstance> instanceList = (siteType == SiteType.mini ? miniInstanceList : litbInstanceList);
		if(siteType == SiteType.ezbuymy)
			instanceList = ezbuymyInstanceList;
		if(siteType == SiteType.ezbuyth)
			instanceList = ezbuythstanceList;
		if(siteType == SiteType.ezbuysg)
			instanceList = ezbuysgstanceList;
		for(CpTreeInstance instance : instanceList){
			if(instance.match(onlyCategory, onlyValidCategory, considerSoftLink, considerSoftcopy, productStatus))
				return instance.cpTree;
		}
		
		// need build new one
		DBHelper dbHelper = Conf.getMcDbHelper(siteType);
		CPTree cpTree = new CPTree(dbHelper, onlyCategory, onlyValidCategory, considerSoftLink, considerSoftcopy, productStatus);
		CpTreeInstance instance = new CpTreeInstance();
		instance.onlyCategory = onlyCategory;
		instance.onlyValidCategory = onlyValidCategory;
		instance.considerSoftcopy = considerSoftcopy;
		instance.considerSoftLink = considerSoftLink;
		instance.productStatus = productStatus;
		instance.cpTree = cpTree;
		
		instanceList.add(instance);
		
		return instance.cpTree;
	}
	
	private static synchronized CPTree getCpTreeFromAS3(SiteType siteType, boolean onlyCategory, boolean onlyValidCategory, boolean considerSoftLink, boolean considerSoftcopy, ProductStatus productStatus) throws SQLException, IOException{
		// try to reuse
		List<CpTreeInstance> instanceList = (siteType == SiteType.mini ? miniInstanceList : litbInstanceList);
		if(siteType == SiteType.ezbuymy)
			instanceList = ezbuymyInstanceList;
		if(siteType == SiteType.ezbuyth)
			instanceList = ezbuythstanceList;
		if(siteType == SiteType.ezbuysg)
			instanceList = ezbuysgstanceList;
		for(CpTreeInstance instance : instanceList){
			if(instance.match(onlyCategory, onlyValidCategory, considerSoftLink, considerSoftcopy, productStatus))
				return instance.cpTree;
		}
		
		// need build new one
		String filePath = siteType == SiteType.litb ? AS3_LITB_CPTREE_FILE : AS3_MINI_CPTREE_FILE;
		if(siteType == SiteType.ezbuymy)
			filePath = AS3_EZBUYMY_CPTREE_FILE;
		if(siteType == SiteType.ezbuyth)
			filePath = AS3_EZBUYTH_CPTREE_FILE;
		if(siteType == SiteType.ezbuysg)
			filePath = AS3_EZBUYSG_CPTREE_FILE;
		BufferedReader br = AS3FileHelper.readFile(filePath);
		CPTree cpTree =  new CPTree(br, siteType, onlyCategory, onlyValidCategory, considerSoftLink, considerSoftcopy, productStatus);
		br.close();
		CpTreeInstance instance = new CpTreeInstance();
		instance.onlyCategory = onlyCategory;
		instance.onlyValidCategory = onlyValidCategory;
		instance.considerSoftcopy = considerSoftcopy;
		instance.considerSoftLink = considerSoftLink;
		instance.productStatus = productStatus;
		instance.cpTree = cpTree;
		
		instanceList.add(instance);
		
		return instance.cpTree;
	}
	
	// internal class
	private static class CpTreeInstance{
		private boolean onlyCategory;
		private boolean onlyValidCategory;
		private boolean considerSoftLink;
		private boolean considerSoftcopy;
		private ProductStatus productStatus;
		
		private CPTree cpTree;
		
		private boolean match(boolean onlyCategory, boolean onlyValidCategory, boolean considerSoftLink, boolean considerSoftcopy, ProductStatus productStatus){
			if(this.onlyCategory != onlyCategory && onlyCategory == false)
				return false;
			if(this.onlyValidCategory != onlyValidCategory && onlyValidCategory == false)
				return false;
			if(this.considerSoftcopy != considerSoftcopy)
				return false;
			if(this.considerSoftLink != considerSoftLink)
				return false;
			if(this.productStatus != ProductStatus.any && this.productStatus != productStatus)
				return false;
			return true;
		}
	}
	
	// main for test
	public static void main(String[] args) throws SQLException, IOException {
		CPTree cpTree = CpTreeFactory.getCategoryCpTree(SiteType.litb);
		System.out.println(cpTree.getCategoryAllChildCategoriesIncludeSelf(1181));
		System.exit(0);
		
		CpTreeInstance instance = new CpTreeInstance();
		instance.onlyCategory = true;
		instance.onlyValidCategory = true;
		instance.considerSoftcopy = true;
		instance.considerSoftLink = true;
		instance.productStatus = ProductStatus.onSale;
		
		boolean onlyCategory = true;
		boolean onlyValidCategory = true;
		boolean considerSoftcopy = true;
		boolean considerSoftLink = true;
		ProductStatus productStatus = ProductStatus.onSale;
		
		System.out.println(instance.match(onlyCategory, onlyValidCategory, considerSoftLink, considerSoftcopy, productStatus));
		
		
	}
}
