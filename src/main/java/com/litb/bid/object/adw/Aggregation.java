package com.litb.bid.object.adw;

import com.litb.bid.Conf;

public class Aggregation {

    private static final String SEPARATOR = "_AGG_";
    private String key;
    private DeviceStatItem[] statItems;

    // public methods
//		@Override
//		public String toString(){
//			try {
//				return JsonMapper.toJsonString(this);
//			} catch (Exception e) {
//				return null;
//			}
//		}
//		public static Aggregation parse(String line){
//			try {
//				return (Aggregation) JsonMapper.parseJsonString(line, Aggregation.class);
//			} catch (IOException e) {
//				e.printStackTrace();
//				return null;
//			}
//		}
    @Override
    public String toString() {
        String res = "" + key;
        if (statItems == null)
            statItems = new DeviceStatItem[Conf.STAT_INTERVALS.length];
        for (int i = 0; i < statItems.length; i++) {
            res += SEPARATOR + statItems[i];
        }
        return res;
    }

    public static Aggregation parse(String line) {
        Aggregation data = new Aggregation();
        String[] ss = line.split(SEPARATOR);
        String key = ss[0];
        DeviceStatItem[] statItems = new DeviceStatItem[Conf.STAT_INTERVALS.length];
        for (int i = 0; i < statItems.length; i++) {
            statItems[i] = DeviceStatItem.parse(ss[i + 1]);
            if (statItems[i] == null) {
                statItems[i] = new DeviceStatItem(Conf.STAT_INTERVALS[i]);
            }
        }
        data.statItems = statItems;
        data.key = key;
        return data;
    }

    // Getters and Setters
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public DeviceStatItem[] getStatItems() {
        return statItems;
    }

    public void setStatItems(DeviceStatItem[] statItems) {
        this.statItems = statItems;
    }


}
