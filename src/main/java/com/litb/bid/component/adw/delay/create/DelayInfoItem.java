package com.litb.bid.component.adw.delay.create;

import com.litb.adw.lib.enums.LitbAdChannel;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;

import java.util.StringTokenizer;

public class DelayInfoItem {
	private LitbAdChannel channel = null;;
    private SiteType siteType = null;
    private LanguageType languageType = null;
    private int categoryId = 0;
    private double[] convValueArray = null;
    private long[] conversionsArray = null;
    
    public DelayInfoItem() {
    	convValueArray = new double[30];
    	conversionsArray = new long[30];
    }
    
    public static DelayInfoItem parse(String line) throws Exception {
    	StringTokenizer st = new StringTokenizer(line, "\t");
    	if(st.countTokens() != 6) throw new Exception("DelayInfoItem parse error!");
    	DelayInfoItem item = new DelayInfoItem();
    	LitbAdChannel channel = LitbAdChannel.valueOf(st.nextToken());
    	SiteType siteType = SiteType.valueOf(st.nextToken());
    	String langString = st.nextToken();
    	if(langString.toLowerCase().equals("jp"))
    		langString = "ja";
    	LanguageType languageType = LanguageType.valueOf(langString);
    	int categoryId = Integer.parseInt(st.nextToken());
    	double[] convValueArray = parseConvValueArrayString(st.nextToken());
    	long[] conversionsArray = parseConversionsArrayString(st.nextToken());
    	item.setChannel(channel);
    	item.setSiteType(siteType);
    	item.setLanguageType(languageType);
    	item.setCategoryId(categoryId);
    	item.setConvValueArray(convValueArray);
    	item.setConversionsArray(conversionsArray);
    	return item;
    }

    public void mergeValue(int index, double convValue, long conversions) {
    	convValueArray[index] += convValue;
    	conversionsArray[index] += conversions;
    }
    
    public void mergeValues(double[] convValueArray, long[] conversionsArray) {
    	for(int i=0;i<30;i++) {
    		this.convValueArray[i] += convValueArray[i];
    		this.conversionsArray[i] += conversionsArray[i];
    	}
    } 
    
    public String getConvValueArrayString() {
    	String s = "" + convValueArray[0];
    	for(int i=1;i<30;i++) s += "_" + convValueArray[i];
    	return s;
    }
    
    public String getConversionsArrayString() {
    	String s = "" + conversionsArray[0];
    	for(int i=1;i<30;i++) s += "_" + conversionsArray[i];
    	return s;
    }
    
    public static double[] parseConvValueArrayString(String line){
    	double[] convValueArray = new double[30];
    	StringTokenizer st = new StringTokenizer(line, "_");
    	if(st.countTokens() != 30) 
    		throw new IllegalArgumentException("convValue array size != 30");
    	for(int i=0;i<30;i++) {
    		convValueArray[i] = Double.parseDouble(st.nextToken());
    	}
    	return convValueArray;
    }
    
    public static long[] parseConversionsArrayString(String line) {
    	long[] conversionsArray = new long[30];
    	StringTokenizer st = new StringTokenizer(line, "_");
    	if(st.countTokens() != 30) 
    		throw new IllegalArgumentException("conversions array size != 30");
    	for(int i=0;i<30;i++) {
    		conversionsArray[i] = Long.parseLong(st.nextToken());
    	}
    	return conversionsArray;
    }
    
    public void addConvValue(int index, double convValue) {
    	convValueArray[index] += convValue;
    }
    public void addConversions(int index, long conversions) {
    	conversionsArray[index] += conversions;
    }
    
    // get and set
	public LitbAdChannel getChannel() {
		return channel;
	}

	public void setChannel(LitbAdChannel channel) {
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

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public double[] getConvValueArray() {
		return convValueArray;
	}

	public void setConvValueArray(double[] convValueArray) {
		this.convValueArray = convValueArray;
	}

	public long[] getConversionsArray() {
		return conversionsArray;
	}

	public void setConversionsArray(long[] conversionsArray) {
		this.conversionsArray = conversionsArray;
	}
}
