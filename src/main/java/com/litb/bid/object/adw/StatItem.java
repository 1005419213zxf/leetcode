package com.litb.bid.object.adw;

import com.litb.adw.lib.operation.report.AdwordsMetric;
import com.litb.bid.object.LogMetric;

public class StatItem {
	
	public static final String SEPARATOR = "\t\t";
	
	private int interval = -1;
	private AdwordsMetric reportMetric = new AdwordsMetric();
	private LogMetric logMetric = new LogMetric();
	
	// constructor
	public StatItem(int interval) {
		this.interval = interval;
	}
	
	public StatItem() {
		super();
	}

	// public methods
	@Override
	public String toString() {
		return interval + SEPARATOR + reportMetric + SEPARATOR + logMetric;
	}
	
	public static StatItem parse(String line) {
		try {
			String[] ss = line.split(SEPARATOR);
			int interval = Integer.parseInt(ss[0]);
			AdwordsMetric adwordsMetric = AdwordsMetric.parse(ss[1]);
			LogMetric logMetric = LogMetric.parse(ss[2]);
			StatItem item = new StatItem(interval);
			item.setReportMetric(adwordsMetric);
			item.setLogMetric(logMetric);
			return item;
		} catch (Exception e) {
			return null;
		}
	}
	
	// Getters and Setters
	public AdwordsMetric getReportMetric() {
		return reportMetric;
	}
	public void setReportMetric(AdwordsMetric reportMetric) {
		this.reportMetric = reportMetric;
	}
	public LogMetric getLogMetric() {
		return logMetric;
	}
	public void setLogMetric(LogMetric logMetric) {
		this.logMetric = logMetric;
	}
	
	public int getInterval() {
		return interval;
	}
	public void setInterval(int interval) {
		this.interval = interval;
	}
}
