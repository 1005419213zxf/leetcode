package com.litb.bid.component.adw;

import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.databind.JsonNode;
import com.litb.bid.util.ComponentFactory;
import com.litb.bid.util.CpTreeFactory;
import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.db.DBHelper;
import com.litb.basic.db.DBPool;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;
import com.litb.basic.util.DateHelper;
import net.sf.json.JSONException;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TargetCpcContainer {
  private static final double MIN_TARGET_CPC = 0.01;
  private static final double MAX_TARGET_CPC = 2.00;
  
  private Map<String, Double> categoryTargetCpcMap = new HashMap<String, Double>();
  
  public void resolve(String targetCpcConfigString, Date endDate) throws IOException, JSONException, SQLException{
    if(targetCpcConfigString.startsWith("file:")){
      String targetCpcFilePath = targetCpcConfigString.substring(5).trim();
      Pattern systemPlaceHolderPattern = Pattern.compile("\\$\\{([^${}]+)\\}");
      Matcher m = systemPlaceHolderPattern.matcher(targetCpcFilePath);
      String resolvedPath = targetCpcFilePath;
      while(m.find()){
        String placeHolder = m.group(1);
        resolvedPath = resolvedPath.replaceAll("\\$\\{" + placeHolder + "\\}", System.getProperty(placeHolder));
      }
      String configStr = FileHelper.getFileContent(resolvedPath);
//      JSONObject config = new JSONObject(configStr);
      JsonNode config = Jackson.jsonNodeOf(configStr);
//      Iterator<?> it = config.sortedKeys();
      Iterator<?> it = config.fieldNames();
      while(it.hasNext()){
        String key = it.next().toString();
        SiteType siteType = SiteType.valueOf(key);
//        JSONObject siteConfig = config.getJSONObject(key);
        JsonNode siteConfig = config.get(key);
//        Iterator<?> siteIt = siteConfig.sortedKeys();
        Iterator<?> siteIt = siteConfig.fieldNames();
        while(siteIt.hasNext()){
          String channelKey = siteIt.next().toString();
          AdwordsChannel channel = AdwordsChannel.valueOf(channelKey);
//          JSONObject channelConfig = siteConfig.getJSONObject(channelKey);
          JsonNode channelConfig = siteConfig.get(channelKey);
//          Iterator<?> channelIt = channelConfig.sortedKeys();
          Iterator<?> channelIt = channelConfig.fieldNames();
          while(siteIt.hasNext()){
            String cidKey = channelIt.next().toString();
//            double targetCpc = channelConfig.getDouble(cidKey);
            double targetCpc = channelConfig.get(cidKey).asDouble();
            if(targetCpc != -1 && (targetCpc < MIN_TARGET_CPC || targetCpc > MAX_TARGET_CPC))
                throw new Error("Abnormal target Cpc: " + targetCpc);
            categoryTargetCpcMap.put(siteType + "_" + channel + "_" + cidKey, targetCpc);
          }
        }
      }
    }
    if(targetCpcConfigString.startsWith("db:")){
      String dbConfigString = targetCpcConfigString.substring(3).trim();
      Pattern systemPlaceHolderPattern = Pattern.compile("\\%\\{([^%{}]+)\\}");
      Matcher m = systemPlaceHolderPattern.matcher(dbConfigString);
      while(m.find()){
        String placeHolder = m.group(1);
        if(placeHolder.equalsIgnoreCase("END_DATA"))
          dbConfigString = dbConfigString.replaceAll("\\%\\{" + placeHolder + "\\}", DateHelper.getShortDateString(endDate));
      }
      System.out.println("date: " + DateHelper.getShortDateString(endDate));

      String sql = dbConfigString;
      System.out.println(sql);

      DBHelper dbHelper = DBPool.getCnBiMasterDbHelperWithMarketingRole();
      ResultSet resultSet = dbHelper.executeQuery(sql);
      while(resultSet.next()){
          AdwordsChannel channel = AdwordsChannel.getAdwordsChannel(resultSet.getInt("channel"));
          SiteType siteType = SiteType.getSiteType(resultSet.getInt("merchant_id"));
          int cid = resultSet.getInt("categories_id");
          if(cid <= 0)
              cid = -1;
          double targetCpc = resultSet.getDouble("target_cpc");
          if(targetCpc != -1 && (targetCpc < MIN_TARGET_CPC || targetCpc > MAX_TARGET_CPC))
              throw new Error("Abnormal target Cpc: " + targetCpc);
          
          categoryTargetCpcMap.put(siteType + "\t" + channel + "\t" + cid, targetCpc);
      }
      dbHelper.close();
    }
  }
  
  public double getTargetCpc(SiteType siteType, int categoryId, AdwordsChannel channel){
    try {
      CPTree cpTree = CpTreeFactory.getCategoryCpTree(siteType, false);
      List<Integer> cidList = cpTree.getCategoryAncesterCategoryIncludingSelf(categoryId);
      for(int cid : cidList){
        String key = siteType + "\t" + channel + "\t" + cid;
        Double targetCpc = categoryTargetCpcMap.get(key);
        if(targetCpc != null)
          return targetCpc;
      }
    } catch (Exception e) {
      System.err.println("cptree db err");
    }
    return -1.0;
  }
  
  public static void main(String[] args) throws IOException, JSONException, SQLException {
    TargetCpcContainer targetCpcContainer = ComponentFactory.getTargetCpcContainer();
    targetCpcContainer.resolve("db:select stat_date,merchant_id,channel_id,categories_id,target_cpc from ods_ad_target_cpc where stat_date in (select max(stat_date) from ods_ad_target_cpc)", DateHelper.getShortDate("2017-05-03"));
    System.out.println(targetCpcContainer.getTargetCpc(SiteType.litb, 4456, AdwordsChannel.search));
  }
}
