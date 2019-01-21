package com.litb.bid.object.bingreport;

import com.litb.basic.io.CsvReader;
import com.litb.basic.io.FileHelper;
import com.litb.bing.lib.operation.report.BingCampaignPlatformPerformanceReport;

import java.io.BufferedReader;
import java.io.IOException;

public class BingCampaignPlatformIntervalReport extends BingCampaignPlatformPerformanceReport {
    public static final String PREFIX = "brcpi_";
    private int interval;

    public BingCampaignPlatformIntervalReport(int interval) {
        this.interval = interval;
    }

    public static BingCampaignPlatformIntervalReport parseFromReport(int interval, String[] vals) {
        BingCampaignPlatformIntervalReport report = new BingCampaignPlatformIntervalReport(interval);
        report.fillDataFromReport(vals);
        return report;
    }

    public static BingCampaignPlatformIntervalReport parseFromLine(String line) {
        int index = line.indexOf("\t");
        int interval = Integer.parseInt(line.substring(PREFIX.length(), index));
        BingCampaignPlatformIntervalReport report = new BingCampaignPlatformIntervalReport(interval);
        report.fillDataFromFormattedLine(line.substring(index + 1));
        return report;
    }

    public int getInterval() {
        return interval;
    }

    @Override
    public String toString() {
        return PREFIX + interval + "\t" + super.toString();
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public static void main(String[] args)  {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = FileHelper.readFile("D:\\test\\a.csv");
            CsvReader csvReader = new CsvReader(bufferedReader, ',');
            while (csvReader.readRecord()) {
                String[] vals = csvReader.getValues();
                BingCampaignPlatformIntervalReport bingCampaignPlatformIntervalReport = BingCampaignPlatformIntervalReport.parseFromReport(1, vals);
                System.out.println(bingCampaignPlatformIntervalReport.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
