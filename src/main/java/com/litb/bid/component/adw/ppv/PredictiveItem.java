package com.litb.bid.component.adw.ppv;

import com.litb.bid.Conf;
import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.basic.enums.LanguageType;

import java.util.StringTokenizer;

public class PredictiveItem {
	private AdwordsChannel channel = null;
	private LanguageType languageType = null;
	private Boolean isFromMobileDevice = false;	// platform
	private String cpTag;
	
	private double predictiveCr = .0;
	
	private int[] orderedUvNumber = new int[Conf.PPV_DIMENSION];
	private int[] uvNumber = new int[Conf.PPV_DIMENSION];
	private double[] cr = new double[Conf.PPV_DIMENSION];
	
	// constructors
	public PredictiveItem(AdwordsChannel channel, LanguageType languageType, Boolean isFromMobileDevice, String cpTag) {
		this.channel = channel;
		this.languageType = languageType;
		this.isFromMobileDevice = isFromMobileDevice;
		this.cpTag = cpTag;
	}

	// public methods
	public String toString() {
		return toString(false);
	}
	
	public static PredictiveItem parse(String line) {
		StringTokenizer st = new StringTokenizer(line, "\t");
		String ss = st.nextToken();
		AdwordsChannel channel = null;
		try {
			channel = AdwordsChannel.valueOf(ss);
		} catch (Exception e) {;}
		ss = st.nextToken();
		LanguageType languageType = null;
		try {
			languageType = LanguageType.valueOf(ss);
		} catch (Exception e) {;}
		ss = st.nextToken();
		Boolean isFromMobileDevice = null;
		try {
			isFromMobileDevice = Boolean.valueOf(ss);
		} catch (Exception e) {;}
		String cpTag = st.nextToken();
		PredictiveItem item = new PredictiveItem(channel, languageType, isFromMobileDevice, cpTag);
		for(int i=0; i<Conf.PPV_DIMENSION; i++) {
			String line2 = st.nextToken();
			StringTokenizer st2 = new StringTokenizer(line2, ";");
			int uniqueProductVisitNumber = Integer.parseInt(st2.nextToken());
			int orderedUvNumber = Integer.parseInt(st2.nextToken());
			int uvNumber = Integer.parseInt(st2.nextToken());
			double cr = Double.parseDouble(st2.nextToken());
			item.orderedUvNumber[uniqueProductVisitNumber] = orderedUvNumber;
			item.uvNumber[uniqueProductVisitNumber] = uvNumber;
			item.cr[uniqueProductVisitNumber] = cr;
		}
		if(st.hasMoreTokens()) {
			item.predictiveCr = Double.parseDouble(st.nextToken());
		}
		return item;
	}
	
	public void merge(int uniqueProductVisitNumber, boolean ordered) {
		int index = uniqueProductVisitNumber >= Conf.PPV_DIMENSION ? Conf.PPV_DIMENSION - 1 : uniqueProductVisitNumber;
		uvNumber[index] ++;
		if (ordered) {
			orderedUvNumber[index] ++;
		}
	}
	
	public void merge(int uniqueProductVisitNumber, int aOrderedUvNumber, int aUvNumber) {
		int index = uniqueProductVisitNumber >= Conf.PPV_DIMENSION ? Conf.PPV_DIMENSION - 1 : uniqueProductVisitNumber;
		orderedUvNumber[index] += aOrderedUvNumber;
		uvNumber[index] += aUvNumber;
	}
	
	public void merger(PredictiveItem item) {
		for (int i=0; i<Conf.PPV_DIMENSION; i++) {
			this.orderedUvNumber[i] += item.orderedUvNumber[i];
			this.uvNumber[i] += item.uvNumber[i];
		}
	}
	
	public void summary() {
		for (int i=0; i<Conf.PPV_DIMENSION; i++) {
			cr[i] = (uvNumber[i] != 0) ? (double)orderedUvNumber[i] / (double)uvNumber[i]
									   : .0;
		}
	}
	
	public String toString(boolean includePredictedCr) {
		String ans = channel + "\t" + languageType + "\t" + (isFromMobileDevice == null ? "-1" : (isFromMobileDevice ? "1" : "0")) + "\t" + cpTag;
		for (int i=0; i<Conf.PPV_DIMENSION; i++) {
			ans += "\t" + i + ";" + orderedUvNumber[i] + ";" + uvNumber[i] + ";" + cr[i];
		}
		if (includePredictedCr) {
			ans += "\t" + predictiveCr;
		}
		return ans;
	}
	
	// Getters and Setters
	public int[] getOrderedUvNumber() {
		return orderedUvNumber;
	}

	public void setOrderedUvNumber(int[] orderedUvNumber) {
		this.orderedUvNumber = orderedUvNumber;
	}

	public int[] getUvNumber() {
		return uvNumber;
	}

	public void setUvNumber(int[] uvNumber) {
		this.uvNumber = uvNumber;
	}

	public double[] getCr() {
		return cr;
	}

	public void setCr(double[] cr) {
		this.cr = cr;
	}
	
	public AdwordsChannel getChannel() {
		return channel;
	}

	public void setChannel(AdwordsChannel channel) {
		this.channel = channel;
	}

	public LanguageType getLanguageType() {
		return languageType;
	}

	public void setLanguageType(LanguageType languageType) {
		this.languageType = languageType;
	}

	public Boolean isFromMobileDevice() {
		return isFromMobileDevice;
	}

	public void setFromMobileDevice(Boolean isFromMobileDevice) {
		this.isFromMobileDevice = isFromMobileDevice;
	}

	public double getPredictiveCr() {
		return predictiveCr;
	}

	public String getCpTag() {
		return cpTag;
	}

	public void setCpTag(String cpTag) {
		this.cpTag = cpTag;
	}

	public void setPredictiveCr(double predictiveCr) {
		this.predictiveCr = predictiveCr;
	}
	
}
