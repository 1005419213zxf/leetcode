package com.litb.bid.object.bingreport;

import com.litb.bing.lib.operation.report.BingShoppingReport;

public class BingShoppingIntervalReport extends BingShoppingReport {
    public static final String PREFIX = "brsi_";
    private int interval;
    private boolean onlySmartPhone;

    public BingShoppingIntervalReport(int interval, boolean onlySmartPhone) {
        this.interval = interval;
        this.onlySmartPhone = onlySmartPhone;
    }

    public static BingShoppingIntervalReport parseFromReport(int interval, boolean onlyMobile, String[] vals) {
        BingShoppingIntervalReport report = new BingShoppingIntervalReport(interval, onlyMobile);
        report.fillDataFromReport(vals);
        return report;
    }

    public static BingShoppingIntervalReport parseFromLine(String line) {
        int index = line.indexOf("\t");
        String[] strArr = line.substring(0, index).split("_");
        int interval = Integer.parseInt(strArr[1]);
        boolean onlyMobile = false;
        if (strArr.length > 2)
            onlyMobile = strArr[2].equals("m");
        BingShoppingIntervalReport report = new BingShoppingIntervalReport(interval, onlyMobile);
        report.fillDataFromFormattedLine(line.substring(index + 1));
        return report;
    }

    @Override
    public String toString() {
        return PREFIX + interval + (onlySmartPhone ? "_m" : "_all") + "\t" + super.toString();
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public boolean isOnlySmartPhone() {
        return onlySmartPhone;
    }

    public void setOnlySmartPhone(boolean onlySmartPhone) {
        this.onlySmartPhone = onlySmartPhone;
    }
}
