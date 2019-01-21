package com.litb.bid.object.bingreport;

import com.litb.bing.lib.operation.report.BingKeywordsReport;

public class BingKeywordIntervalReport extends BingKeywordsReport {
    public static final String PREFIX = "brki_";
    private int interval;
    private boolean onlySmartPhone;

    public BingKeywordIntervalReport(int interval, boolean onlySmartPhone) {
        this.interval = interval;
        this.onlySmartPhone = onlySmartPhone;
    }

    public static BingKeywordIntervalReport parseFromReport(int interval, boolean onlyMobile, String[] vals) {
        BingKeywordIntervalReport bingKeywordIntervalReport = new BingKeywordIntervalReport(interval, onlyMobile);
        bingKeywordIntervalReport.fillDataFromReport(vals);
        return bingKeywordIntervalReport;
    }

    public static BingKeywordIntervalReport parseFromLine(String line) {
        int index = line.indexOf("\t");
        String[] strArr = line.substring(0, index).split("_");
        int interval = Integer.parseInt(strArr[1]);
        boolean onlyMobile = false;
        if (strArr.length > 2) {
            onlyMobile = "m".equals(strArr[2]);
        }
        BingKeywordIntervalReport bingKeywordsReport = new BingKeywordIntervalReport(interval, onlyMobile);
        bingKeywordsReport.fillDataFromFormattedLine(line.substring(index + 1));
        return bingKeywordsReport;
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
