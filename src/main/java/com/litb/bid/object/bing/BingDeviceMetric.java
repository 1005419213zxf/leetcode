package com.litb.bid.object.bing;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.litb.bid.object.adw.AdwordsDeviceMetric;
import com.litb.bid.util.JsonMapper;
import com.litb.bing.lib.enums.BingDevice;
import com.litb.bing.lib.operation.report.BingMetric;

import java.io.IOException;

public class BingDeviceMetric {

    private BingMetric pcMetric = new BingMetric();
    private BingMetric smartPhone = new BingMetric();
    private BingMetric tabletMetric = new BingMetric();
    private BingMetric unknownMetric = new BingMetric();

    // public methods

    public double getPcCrDivideMobileCr() {
        double pcCr = getNonMobileMetric().getCr();
        double mCr = smartPhone.getCr();
        return (mCr <= 0 ? 0 : (pcCr / mCr));
    }

    public double getPcAosDivideMobileAos() {
        double pcAos = getNonMobileMetric().getAos();
        double mAos = smartPhone.getAos();
        return (mAos <= 0 ? 0 : (pcAos / mAos));
    }

    public void mergeData(BingDevice bingDevice, BingMetric metric) {
        switch (bingDevice) {
            case COMPUTER:
                pcMetric.mergeData(metric);
                break;
            case SMARTPHONE:
                smartPhone.mergeData(metric);
                break;
            case TABLET:
                tabletMetric.mergeData(metric);
                break;
            default:
                unknownMetric.mergeData(metric);
                break;
        }
    }

    public void mergeMobileAndSubstractPcData(BingMetric metric) {
        smartPhone.mergeData(metric);
        pcMetric.subtractData(metric);
    }

    public BingMetric getAllDeviceMetric() {
        BingMetric allMetric = new BingMetric();
        allMetric.mergeData(pcMetric);
        allMetric.mergeData(smartPhone);
        allMetric.mergeData(tabletMetric);
        allMetric.mergeData(unknownMetric);
        return allMetric;
    }

    public BingMetric getNonMobileMetric() {
        BingMetric allMetric = new BingMetric();
        allMetric.mergeData(pcMetric);
        allMetric.mergeData(tabletMetric);
        allMetric.mergeData(unknownMetric);
        return allMetric;
    }

    @Override
    public String toString() {
        try {
            return JsonMapper.toJsonString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static AdwordsDeviceMetric parse(String line) throws IOException {
        return (AdwordsDeviceMetric) JsonMapper.parseJsonString(line, AdwordsDeviceMetric.class);
    }

    public BingMetric getMetric(BingDevice device) {
        switch (device) {
            case COMPUTER:
                return pcMetric;
            case SMARTPHONE:
                return smartPhone;
            case TABLET:
                return tabletMetric;
            case NONSMARTPHONE:
                return unknownMetric;
            default:
                throw new IllegalArgumentException("cannot deal with device: " + device);
        }
    }

    // Getters and Setters
    public BingMetric getSmartPhone() {
        return smartPhone;
    }

    public void setSmartPhone(BingMetric smartPhone) {
        this.smartPhone = smartPhone;
    }

    public BingMetric getPcMetric() {
        return pcMetric;
    }

    public void setPcMetric(BingMetric pcMetric) {
        this.pcMetric = pcMetric;
    }

    public BingMetric getTabletMetric() {
        return tabletMetric;
    }

    public void setTabletMetric(BingMetric tabletMetric) {
        this.tabletMetric = tabletMetric;
    }

    public BingMetric getUnknownMetric() {
        return unknownMetric;
    }

    public void setUnknownMetric(BingMetric unknownMetric) {
        this.unknownMetric = unknownMetric;
    }

    public static void main(String[] args) {
        BingDeviceMetric deviceMetric = new BingDeviceMetric();
        BingMetric aMetric = new BingMetric();
        aMetric.setConversions(100);
        deviceMetric.mergeData(BingDevice.COMPUTER, aMetric);
        System.out.println(deviceMetric.pcMetric.getConversions());
        deviceMetric.mergeMobileAndSubstractPcData(aMetric);
        System.out.println(deviceMetric.pcMetric.getConversions());
        System.out.println(deviceMetric.smartPhone.getConversions());

    }
}
