package com.litb.bid.component.adw;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.litb.basic.enums.SiteType;
import com.litb.bid.util.JsonMapper;

import java.io.IOException;

public class ProductRepeatRateInfo {
    private SiteType siteType;
    private int pid = -1;
    
    private double repeatRate = 1;

    // constructor
    public ProductRepeatRateInfo(SiteType siteType, int pid, double repeatRate) {
		super();
		this.siteType = siteType;
		this.pid = pid;
		this.repeatRate = repeatRate;
	}

	// public methods
	@Override
	public String toString() {
		try {
			return JsonMapper.toJsonString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}

    public static ProductRepeatRateInfo parse(String line) throws IOException {
    	return (ProductRepeatRateInfo) JsonMapper.parseJsonString(line, ProductRepeatRateInfo.class);
	}

    // Getters and Setters
    public SiteType getSiteType() {
        return siteType;
    }

    public void setSiteType(SiteType siteType) {
        this.siteType = siteType;
    }

    public double getRepeatRate() {
        return repeatRate;
    }

    public void setRepeatRate(double repeatRate) {
        this.repeatRate = repeatRate;
    }

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}
}

