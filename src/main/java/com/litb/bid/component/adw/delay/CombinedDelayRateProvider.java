package com.litb.bid.component.adw.delay;

import com.litb.adw.lib.enums.LitbAdChannel;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;

import java.io.IOException;
import java.sql.SQLException;

public class CombinedDelayRateProvider implements DelayRateProviderInterface {
  private SiteType siteType;

  private DelayRateProviderInterface newDelayRateProvider;
  private DelayRateProviderInterface oldDelayRateProvider;
  
  public CombinedDelayRateProvider(SiteType siteType) throws SQLException, IOException {
    this.siteType = siteType;
    this.newDelayRateProvider = new DelayRateProviderBi(siteType);
    this.oldDelayRateProvider = new DelayRateProviderV160712(siteType);
  }

  @Override
  public DelayRateInfo getDelayRate(LitbAdChannel channel, SiteType siteType,
      LanguageType language, int categoryId, int daysInterval, int getOffsetDays,
      int predictOffsetDays) {
    DelayRateInfo info = newDelayRateProvider.getDelayRate(channel, siteType, language, categoryId, daysInterval, getOffsetDays, predictOffsetDays);
    if (info != null) {
      return info;
    }
    return oldDelayRateProvider.getDelayRate(channel, siteType, language, categoryId, daysInterval, getOffsetDays, predictOffsetDays);
  }

  public SiteType getSiteType() {
    return siteType;
  }
  public void setSiteType(SiteType siteType) {
    this.siteType = siteType;
  }

}
