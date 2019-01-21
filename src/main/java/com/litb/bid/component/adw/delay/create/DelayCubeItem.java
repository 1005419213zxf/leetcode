package com.litb.bid.component.adw.delay.create;

import com.litb.adw.lib.enums.AdwordsDelayChannel;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;

import java.util.Arrays;

/*
 * this class is used to parse delay rate data from as3
 * key == channel + siteType + languageType + categoryId
 * after the key followed 30 values which instead of the continues 30 days' conversion.
 * this class used to be a inner class of AdwordsDelayCubeStatorMR class of the old version,
 * I change it to a independency class here.
 */
public class DelayCubeItem{
	private AdwordsDelayChannel channel;
	private SiteType siteType;
	private LanguageType languageType;
	private int cid;
	
	private static final int DELAY_DIMENSION = 30;
	
	private int[] conversionArray = new int[DELAY_DIMENSION];
	
	public DelayCubeItem() {
		Arrays.fill(conversionArray, 0);
	}
	
	public static DelayCubeItem parse(String line){
		DelayCubeItem item = new DelayCubeItem();
		String[] strArr = line.split("\t");
		int index = 0;
		
		String str = strArr[index++];
		item.channel = (str.equals("-1") ? null : AdwordsDelayChannel.valueOf(str));
		str = strArr[index++];
		item.siteType = (str.equals("-1") ? null : SiteType.valueOf(str));
		str = strArr[index++];
		if(str.equals("jp")) item.languageType = LanguageType.ja;
		else item.languageType = (str.equals("-1") ? null : LanguageType.valueOf(str));
		str = strArr[index++];
		item.cid = Integer.parseInt(str);
		
		for(int i = 0; i < DELAY_DIMENSION; i++)
			item.conversionArray[i] = Integer.parseInt(strArr[index++]);
		
		return item;
	}
	
	@Override
	public String toString(){
		String output = (channel == null ? -1 : channel.toString()) + "\t" + 
						(siteType == null ? -1 : siteType.toString()) + "\t" + 
						(languageType == null ? -1 : languageType.toString()) + "\t" + 
						cid;
		for(int conversion : conversionArray)
			output += "\t" + conversion;
		return output;
	}

	public AdwordsDelayChannel getChannel() {
		return channel;
	}

	public void setChannel(AdwordsDelayChannel channel) {
		this.channel = channel;
	}

	public SiteType getSiteType() {
		return siteType;
	}

	public void setSiteType(SiteType siteType) {
		this.siteType = siteType;
	}

	public LanguageType getLanguageType() {
		return languageType;
	}

	public void setLanguageType(LanguageType languageType) {
		this.languageType = languageType;
	}

	public int getCid() {
		return cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}

	public int[] getConversionArray() {
		return conversionArray;
	}

	public void setConversionArray(int[] conversionArray) {
		this.conversionArray = conversionArray;
	}
}
