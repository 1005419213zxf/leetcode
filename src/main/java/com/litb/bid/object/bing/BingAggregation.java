package com.litb.bid.object.bing;


import com.litb.bid.Conf;

public class BingAggregation {
    private static final String SEPARATOR = "_AGG_";
    private String key;
    private BingDeviceStatItem[] statItems;

    @Override
    public String toString() {
        String result = "" + key;
        if (statItems == null) {
            statItems = new BingDeviceStatItem[Conf.STAT_INTERVALS.length];
        }
        for (int i = 0; i < statItems.length; i++) {
            result += SEPARATOR + statItems[i];
        }
        return result;
    }

    public static BingAggregation parse(String line) {
        BingAggregation bingAggregation = new BingAggregation();
        String[] stringArr = line.split(SEPARATOR);
        String key = stringArr[0];
        BingDeviceStatItem[] statItems = new BingDeviceStatItem[Conf.STAT_INTERVALS.length];
        for (int i = 0; i < statItems.length; i++) {
            statItems[i] = BingDeviceStatItem.parse(stringArr[i + 1]);
        }
        bingAggregation.setKey(key);
        bingAggregation.setStatItems(statItems);
        return bingAggregation;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public BingDeviceStatItem[] getStatItems() {
        return statItems;
    }

    public void setStatItems(BingDeviceStatItem[] statItems) {
        this.statItems = statItems;
    }
}
