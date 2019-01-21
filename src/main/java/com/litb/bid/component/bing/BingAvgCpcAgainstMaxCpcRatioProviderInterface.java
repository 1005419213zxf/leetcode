package com.litb.bid.component.bing;


import com.litb.bid.object.bing.BingBiddableObject;
import com.litb.bid.object.GapData;

public interface BingAvgCpcAgainstMaxCpcRatioProviderInterface {
    public double getAvgCpcAgaistMaxCpcRatio(BingBiddableObject biddableObject);
    public GapData getMaxCpcAndAvgCpcGap(BingBiddableObject biddableObject, double newBid);

}
