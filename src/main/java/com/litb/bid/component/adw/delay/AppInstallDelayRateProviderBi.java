package com.litb.bid.component.adw.delay;

import com.litb.adw.lib.enums.LitbAdChannel;
import com.litb.basic.db.DBHelper;
import com.litb.basic.db.DBPool;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AppInstallDelayRateProviderBi implements DelayRateProviderInterface {
  private static final double MAX_DELAY_RATE = 10.0;

  private Map<String, Double> keyDelayRateMap = new HashMap<String, Double>();

  // constructor
  public AppInstallDelayRateProviderBi(SiteType siteType) throws SQLException, IOException  {
    init();
  }

  private void init() throws IOException, SQLException {
	    DBHelper dbHelper = DBPool.getCnBiMasterDbHelperWithMarketingRole();
	    String sql = "select tt.channel_id,tt.diff_days,tt.convs_value from rd_app_install_delay_r tt where tt.channel_id in (144,455,459)";
	    System.out.println("sql: " + sql);
	    ResultSet resultSet = dbHelper.executeQuery(sql);
	    int count = 0;
	    while (resultSet.next()) {
	      if (count++ % 100000 == 0)
	        System.out.println("count = " + count);
	      int channelId = resultSet.getInt(1);
	      LitbAdChannel channel = null;
	      try {
	        channel = LitbAdChannel.getLitbAdChannel(channelId);
	      } catch (Exception e) {
	    	  e.printStackTrace();
	    	  continue;
	      }
	      int diffDays = resultSet.getInt(2);
	      Double convValue = resultSet.getDouble(3);

	      keyDelayRateMap.put(channel+"_"+diffDays, convValue);
	    }
	    resultSet.close();
	    dbHelper.close();

	    System.out.println("init done, size: " + keyDelayRateMap.size());
  }

  // public methods
  @Override
  public DelayRateInfo getDelayRate(LitbAdChannel channel, SiteType siteType, LanguageType languageType, int categoryId,
      int daysInterval, int getOffsetDays, int predictOffsetDays) {
	  double sumConvValue = 0;
	  double sumConvValueEnd = 0;
	  for (int backwardDays = 0; backwardDays < daysInterval; backwardDays++) {
          int diffDays = backwardDays + getOffsetDays - 1;
          String key = channel + "_" + diffDays;
          Double convValue = keyDelayRateMap.get(key);
          if (convValue != null) {
        	  sumConvValue += convValue;
        	  key = channel + "_" + (predictOffsetDays - 1);
        	  Double ConvValueEnd = keyDelayRateMap.get(key);
        	  sumConvValueEnd += ConvValueEnd;
          }
        }
        // 如果订单数足够，返回
        if (sumConvValue > 0) {
          double averageDelayRate = (sumConvValue == 0 ? 0 : sumConvValueEnd / sumConvValue);
          if(averageDelayRate < 1) {
            averageDelayRate = 1.0;
          } else if(averageDelayRate > MAX_DELAY_RATE) {
            averageDelayRate = MAX_DELAY_RATE;
          }
          
          DelayRateInfo info = new DelayRateInfo();
          info.setSiteType(siteType);
          info.setChannel(channel);
          info.setLanguageType(languageType);
          info.setCid(categoryId);
          info.setDelayRate(averageDelayRate);
          return info;
        }
        return null;
  }

  public static void main(String[] args) throws Exception {
    AppInstallDelayRateProviderBi delayRateProviderBi = new AppInstallDelayRateProviderBi(SiteType.litb);
    System.out.println(delayRateProviderBi.getDelayRate(LitbAdChannel.facebook_app_install, SiteType.litb, LanguageType.en, 71, 1, 7, 28));
  }
}
