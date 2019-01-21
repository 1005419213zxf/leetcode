package com.litb.bid.component.adw.delay;

import com.litb.bid.util.CpTreeFactory;
import com.litb.adw.lib.enums.LitbAdChannel;
import com.litb.basic.cptree.CPTree;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.basic.io.FileHelper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class TestDelayRate {
  
  public static void main(String[] args) throws NumberFormatException, IOException, SQLException {
    DelayRateProviderInterface newLitbDelayRateProvider = new DelayRateProviderBi(SiteType.litb);
    DelayRateProviderInterface newMiniDelayRateProvider = new DelayRateProviderBi(SiteType.mini);
    DelayRateProviderInterface oldLitbDelayRateProvider = new DelayRateProviderV160712(SiteType.litb);
    DelayRateProviderInterface oldMiniDelayRateProvider = new DelayRateProviderV160712(SiteType.mini);
    
    int predictInterval = 30;
    String outputFilePath = "/mnt/adwords/auto_bidding/delay/delay_rate_comparison.csv";
    BufferedWriter bw = FileHelper.writeFile(outputFilePath);
    bw.append("channel,site,language,cid,interval,new_value,old_value\n");
    for (LitbAdChannel channel : Arrays.asList(LitbAdChannel.adwords_search, LitbAdChannel.adwords_pla)) {
      for (SiteType siteType : Arrays.asList(SiteType.litb, SiteType.mini)) {
        DelayRateProviderInterface newDelayRateProvider = (siteType == SiteType.litb ? newLitbDelayRateProvider : newMiniDelayRateProvider);
        DelayRateProviderInterface oldDelayRateProvider = (siteType == SiteType.litb ? oldLitbDelayRateProvider : oldMiniDelayRateProvider);
        CPTree cpTree = CpTreeFactory.getCategoryCpTree(siteType);
        List<Integer> cidList = cpTree.getAllCategories();
        for (LanguageType languageType : LanguageType.values()) {
           for (int cid : cidList) {
             for (int delayInterval : Arrays.asList(1, 7, 14, 28)) {
               DelayRateInfo newDelayRate = newDelayRateProvider.getDelayRate(channel, siteType, languageType, cid, delayInterval, 1, predictInterval);
               DelayRateInfo oldDelayRate = oldDelayRateProvider.getDelayRate(channel, siteType, languageType, cid, delayInterval, 1, predictInterval);
               System.out.println(channel + "," + siteType + "," + languageType + "," + cid + "," + delayInterval + "," + newDelayRate.getDelayRate() + "," + oldDelayRate.getDelayRate());
               bw.write(channel + "," + siteType + "," + languageType + "," + cid + "," + delayInterval + "," + newDelayRate.getDelayRate() + "," + oldDelayRate.getDelayRate() + "\n");
             }
           }
        }
      }
    }
  }

}
