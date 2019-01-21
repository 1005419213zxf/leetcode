package com.litb.bid.component.adw.ppv.create;

import com.litb.basic.cptree.CPTree;
import com.litb.basic.cptree.CPTree.ProductStatus;
import com.litb.basic.db.DBHelper;
import com.litb.basic.db.DBPool;
import com.litb.basic.enums.SiteType;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/* this class is a test class used to test cp nodes and their ancestors.
 * you can delete it at any time after date 2016-01-13
 */
public class TestCPNodesAndTheirAncestors {
	private static void find() throws SQLException {
		DBHelper dbHelper = DBPool.getUsMcMainSlaveDbHelper(SiteType.litb);
		boolean onlyCategory = false;
		boolean onlyValidCategory = false;
		boolean considerSoftLink = false;
		boolean considerSoftCopy = false;
		ProductStatus productStatus = ProductStatus.onSale;
		CPTree litbCpTree = new CPTree(dbHelper, onlyCategory, onlyValidCategory, considerSoftLink, considerSoftCopy, productStatus);
		dbHelper = DBPool.getUsMcMainSlaveDbHelper(SiteType.mini);
		CPTree miniCpTree = new CPTree(dbHelper, onlyCategory, onlyValidCategory, considerSoftLink, considerSoftCopy, productStatus);
		
		for (CPTree cpTree : new CPTree[] { litbCpTree, miniCpTree }) {
			Set<Integer> pSet = cpTree.getAllProducts();
			for (int pid : pSet) {
				System.out.print("pid : " + pid);
				List<Integer> list = cpTree.getProductAllAncesterCategoriesExcludingSoftcopy(pid);
				for (int pcid : list) {
					System.out.print("\t" + pcid);
				}
				System.out.println("");
			}
			List<Integer> cList = cpTree.getAllCategories();
			for (int cid : cList) {
				System.out.print("cid : " + cid);
				List<Integer> list = cpTree.getCategoryAncesterCategoryIncludingSelf(cid);
				for (int pcid : list) {
					System.out.print("\t" + pcid);
				}
				System.out.println("");
			}
		}
	}
	
	public static void main(String[] args) {
		try {
			find();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
