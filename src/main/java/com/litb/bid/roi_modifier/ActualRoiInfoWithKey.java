package com.litb.bid.roi_modifier;

public class ActualRoiInfoWithKey{
	private String key;
	private long clicks;
	private double conversions;
	private double cost;
	private double lifetimeValue;
	private double roi;
	public double getRoi() {
		return roi;
	}

	public void setRoi(double roi) {
		this.roi = roi;
	}

	public static ActualRoiInfoWithKey parse(String line){
		String[] vals = line.split("\t");
		ActualRoiInfoWithKey actualRoiInfoWithKey = new ActualRoiInfoWithKey();
		actualRoiInfoWithKey.key = vals[0];
		actualRoiInfoWithKey.clicks = Long.valueOf(vals[1]);
		actualRoiInfoWithKey.conversions = Double.valueOf(vals[2]);
		actualRoiInfoWithKey.cost = Double.valueOf(vals[3]);
		actualRoiInfoWithKey.lifetimeValue = Double.valueOf(vals[4]);
		actualRoiInfoWithKey.roi = Double.valueOf(vals[5]);
		return actualRoiInfoWithKey;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public long getClicks() {
		return clicks;
	}
	public void setClicks(long clicks) {
		this.clicks = clicks;
	}
	public double getConversions() {
		return conversions;
	}
	public void setConversions(double conversions) {
		this.conversions = conversions;
	}
	public double getCost() {
		return cost;
	}
	public void setCost(double cost) {
		this.cost = cost;
	}
	public double getLifetimeValue() {
		return lifetimeValue;
	}
	public void setLifetimeValue(double lifetimeValue) {
		this.lifetimeValue = lifetimeValue;
	}
	
	@Override
	public String toString() {
		return key + "\t" + clicks + "\t" + conversions + "\t" + cost + "\t" + lifetimeValue + "\t" + roi;
	}
}
