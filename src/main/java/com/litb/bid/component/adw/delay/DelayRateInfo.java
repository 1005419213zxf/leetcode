package com.litb.bid.component.adw.delay;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.litb.adw.lib.enums.LitbAdChannel;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;

import java.io.IOException;

/**
 * Delay rate information, the delay rate value is in [1, infinite)
 * 
 * @author zhangrui1
 *
 */
public class DelayRateInfo {
    private LitbAdChannel channel;
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

     public static DelayRateInfo parse(String line) {
    	 DelayRateInfo delayRateInfo = new DelayRateInfo();
    	 String[] strArr = line.split("__");

    	 LitbAdChannel channel = null;
    	 LanguageType languageType = null;
    	 SiteType siteType = null;
    	 int cid = -1;
    	 double delayRate = -1;

    	 if (strArr.length > 1) {
    		 channel = LitbAdChannel.valueOf(strArr[0]);
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
    public LitbAdChannel getChannel() {
        return channel;
    }

    public void setChannel(LitbAdChannel channel2) {
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
    
    // main
    public static void main(String[] args) throws IOException {
		DelayRateInfo rate = new DelayRateInfo();
		rate.channel = LitbAdChannel.adwords_search;
		rate.siteType = SiteType.litb;
		rate.languageType = null;
		rate.cid = 1181;
		rate.delayRate = 1.5;
		
		ObjectMapper mapper = new ObjectMapper();
		String str = mapper.writeValueAsString(rate);
		System.out.println(str);
		
		rate = mapper.readValue(str, DelayRateInfo.class);
		
		System.out.println(rate.getChannel());
		System.out.println(rate.getSiteType());
		System.out.println(rate.getLanguageType());
		System.out.println(rate.getCid());
		System.out.println(rate.getDelayRate());
		
		str = mapper.writeValueAsString(rate);
		System.out.println(str);
		
		System.out.println("Done.");
	}
}

