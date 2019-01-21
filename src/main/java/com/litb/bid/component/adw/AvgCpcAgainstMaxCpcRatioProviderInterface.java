package com.litb.bid.component.adw;


import com.litb.bid.object.GapData;
import com.litb.bid.object.adw.BiddableObject;

public interface AvgCpcAgainstMaxCpcRatioProviderInterface {
	public double getAvgCpcAgaistMaxCpcRatio(BiddableObject biddableObject);
	public GapData getMaxCpcAndAvgCpcGap(BiddableObject biddableObject, double newBid);
}

