package com.litb.bid.component.bing.delay;


import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.bid.object.bing.BingLitbAdChannel;

public class BingDelayRateInfo {
    private BingLitbAdChannel channel;
    private SiteType siteType;
    private LanguageType languageType;
    private int cid = -1;

    private double delayRate = 1;


    // public methods
    // public methods
    @Override
    public String toString() {
        if (channel != null && languageType != null && siteType != null)
            return channel.toString() + "__" + languageType.toString() + "__" + siteType.toString() + "__" + cid + "__" + delayRate;
        else
            return String.valueOf(delayRate);
    }

    public static BingDelayRateInfo parse(String line) {
        BingDelayRateInfo delayRateInfo = new BingDelayRateInfo();
        String[] strArr = line.split("__");

        BingLitbAdChannel channel = null;
        LanguageType languageType = null;
        SiteType siteType = null;
        int cid = -1;
        double delayRate = -1;

        if (strArr.length > 1) {
            channel = BingLitbAdChannel.valueOf(strArr[0]);
            languageType = LanguageType.valueOf(strArr[1]);
            siteType = SiteType.valueOf(strArr[2]);
            cid = Integer.parseInt(strArr[3]);
            delayRate = Double.parseDouble(strArr[4]);
        }
        else {
            delayRate = Double.parseDouble(strArr[0]);
        }

        delayRateInfo.channel = channel;
        delayRateInfo.languageType = languageType;
        delayRateInfo.siteType = siteType;
        delayRateInfo.cid = cid;
        delayRateInfo.delayRate = delayRate;

        return delayRateInfo;
    }


    // Getters and Setters
    public BingLitbAdChannel getChannel() {
        return channel;
    }

    public void setChannel(BingLitbAdChannel channel2) {
        this.channel = channel2;
    }

    public LanguageType getLanguageType() {
        return languageType;
    }

    public void setLanguageType(LanguageType languageType) {
        this.languageType = languageType;
    }

    public SiteType getSiteType() {
        return siteType;
    }

    public void setSiteType(SiteType siteType) {
        this.siteType = siteType;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public double getDelayRate() {
        return delayRate;
    }

    public void setDelayRate(double delayRate) {
        this.delayRate = delayRate;
    }
}
