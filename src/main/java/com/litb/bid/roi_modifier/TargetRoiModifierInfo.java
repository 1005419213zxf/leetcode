package com.litb.bid.roi_modifier;

public class TargetRoiModifierInfo {
	private String key;
	private double modifier;
	private double expectRoi;
	private double actualRoi;
	public static TargetRoiModifierInfo parse(String line){
		String[] vals = line.split("\t");
		TargetRoiModifierInfo info = new TargetRoiModifierInfo();
		info.key = vals[0];
		info.modifier = Double.valueOf(vals[1]);
		info.expectRoi = Double.valueOf(vals[2]);
		info.actualRoi = Double.valueOf(vals[3]);
		return info;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public double getModifier() {
		return modifier;
	}
	public void setModifier(double modifier) {
		this.modifier = modifier;
	}
	public double getExpectRoi() {
		return expectRoi;
	}
	public void setExpectRoi(double expectRoi) {
		this.expectRoi = expectRoi;
	}
	public double getActualRoi() {
		return actualRoi;
	}
	public void setActualRoi(double actualRoi) {
		this.actualRoi = actualRoi;
	}
	@Override
	public String toString() {
		return key + "\t" + modifier + "\t" + expectRoi + "\t" + actualRoi;
	}
}
