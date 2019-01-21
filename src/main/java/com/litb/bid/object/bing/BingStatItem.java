package com.litb.bid.object.bing;


import com.litb.bid.object.LogMetric;
import com.litb.bing.lib.operation.report.BingMetric;
public class BingStatItem {
    public static final String SEPARATOR = "\t\t";

    private int interval = -1;
    private BingMetric reportMetric = new BingMetric();
    private LogMetric logMetric = new LogMetric();

    public BingStatItem(int interval) {
        this.interval = interval;
    }

    public BingStatItem() {
        super();
    }

    @Override
    public String toString() {
        return interval + SEPARATOR + reportMetric + SEPARATOR + logMetric;
    }

    public static BingStatItem parse(String line) {
        String[] vals = line.split(SEPARATOR);
        BingStatItem bingStatItem = new BingStatItem();
        bingStatItem.setInterval(Integer.parseInt(vals[0]));
        bingStatItem.setReportMetric(BingMetric.parse(vals[1]));
        bingStatItem.setLogMetric(LogMetric.parse(vals[2]));
        return bingStatItem;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public BingMetric getReportMetric() {
        return reportMetric;
    }

    public void setReportMetric(BingMetric reportMetric) {
        this.reportMetric = reportMetric;
    }

    public LogMetric getLogMetric() {
        return logMetric;
    }

    public void setLogMetric(LogMetric logMetric) {
        this.logMetric = logMetric;
    }

    public static void main(String[] args) {
        String line = "1\t\t0\t0\t0.0\t0.0\t0.0\t-1.0\t0.0\t\t0\t0\t0\t0\t0\t0\t-";
        BingStatItem bingStatItem = BingStatItem.parse(line);
        System.out.println(line);
        System.out.println(bingStatItem);
    }
}
