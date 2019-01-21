package com.litb.bid.object.bingreport;

import com.litb.bing.lib.operation.report.BingAudienceReport;

public class BingAudienceIntervalReport extends BingAudienceReport {
    public static final String PREFIX = "brai_";
    private int interval;
    private boolean onlySmartPhone;

    public BingAudienceIntervalReport(int interval, boolean onlySmartPhone) {
        this.interval = interval;
        this.onlySmartPhone = onlySmartPhone;
    }

    public static BingAudienceIntervalReport parseFromReport(int interval, boolean onlyMobile, String[] vals) {
        BingAudienceIntervalReport bingAudienceIntervalReport = new BingAudienceIntervalReport(interval, onlyMobile);
        bingAudienceIntervalReport.fillDataFromReport(vals);
        return bingAudienceIntervalReport;
    }

    public static BingAudienceIntervalReport parseFromLine(String line) {
        int index = line.indexOf("\t");
        String[] strArr = line.substring(0, index).trim().split("_");
        int interval = Integer.parseInt(strArr[1]);
        boolean onlyMobile = false;
        if (line.length() > 2) {
            onlyMobile = strArr.equals("m");
        }
        BingAudienceIntervalReport report = new BingAudienceIntervalReport(interval, onlyMobile);
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
