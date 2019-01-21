package com.litb.bid.component.adw.tmp;


import com.litb.bid.component.adw.RepeatRateInfo;
import com.litb.bid.component.adw.ValueAdjustRateInfo;
import com.litb.bid.component.adw.delay.DelayRateInfo;

public class LifetimeData {
	private double data;
	
	private DelayRateInfo delayRateInfo;
    private RepeatRateInfo repeatRateInfo;
	private double conversionManyDivideOneRatio = 1;
	private double conversionManyDivideOneThresholdRatio = 1;
	private double conversionManyDivideOneAdjustRate = 1;
	private double shoppingCartRate = 1;

	private double valueRecoveryRate = 1;
	private ValueAdjustRateInfo valueAdjustRateInfo;
	private double taxRate = 1;
	private double receiveRate = 1;
	private double exchangeRate = 1;
	
	private double offlinePaymentRate = 1;
	private double redirectPaymentRate = 1;
	
	// public method
	@Override
	public String toString() {
		return data + "\t" + (delayRateInfo == null ? "-" : delayRateInfo.toString()) + "\t" +
                (repeatRateInfo == null ? "-" : repeatRateInfo.toString()) + "\t" +
                conversionManyDivideOneRatio + "\t" +
				conversionManyDivideOneThresholdRatio + "\t" + conversionManyDivideOneAdjustRate + "\t" +
                shoppingCartRate + "\t" + valueRecoveryRate + "\t" +
                (valueAdjustRateInfo == null ? "-" : valueAdjustRateInfo.toString()) + "\t" +
                taxRate + "\t" + receiveRate + "\t" + exchangeRate + "\t" +
                offlinePaymentRate + "\t" + redirectPaymentRate;
	}
	
	public String toReadableString(){
		return "[val] " + data + " " + 
				"[delay] " + (delayRateInfo == null ? "-" : delayRateInfo.toString()) + " " +
				"[repeat] " +  (repeatRateInfo == null ? "-" : repeatRateInfo.toString()) + " " +
				"[many/one] " + conversionManyDivideOneAdjustRate + " " + 
				"[shopcart] " + shoppingCartRate + " " + 
				"[valueRec] " + valueRecoveryRate + " " + 
				"[valueAdj] " + (valueAdjustRateInfo == null ? "-" : valueAdjustRateInfo.toString()) + " " +
				"[tax] " + taxRate + " " + 
				"[many/one2] " + conversionManyDivideOneRatio + " " + 
				"[many/one thr] " + conversionManyDivideOneThresholdRatio + " " + 
				"[receive]" + receiveRate + " " +
				"[offline payment rate]" + offlinePaymentRate + " " +
				"[redirect payment rate]" + redirectPaymentRate ;
	}


    public static LifetimeData parse(String line) {
		LifetimeData lifetimeData = new LifetimeData();
		String[] strArray = line.split("\t");
		int index = 0;
		
		lifetimeData.setData(Double.parseDouble(strArray[index++]));
//        String tmpString = strArray[index++];
//        lifetimeData.setDelayRateInfo((tmpString.equals("-") ? null : DelayRate.parse(tmpString)));
//        tmpString = strArray[index++];
//        lifetimeData.setRepeatRateInfo((tmpString.equals("-") ? null : RepeatRateInfo.parse(tmpString)));
		lifetimeData.setConversionManyDivideOneRatio(Double.parseDouble(strArray[index++]));
		lifetimeData.setConversionManyDivideOneThresholdRatio(Double.parseDouble(strArray[index++]));
		lifetimeData.setConversionManyDivideOneAdjustRate(Double.parseDouble(strArray[index++]));
		lifetimeData.setShoppingCartRate(Double.parseDouble(strArray[index++]));
		lifetimeData.setValueRecoveryRate(Double.parseDouble(strArray[index++]));
//        tmpString = strArray[index++];
//		lifetimeData.setValueAdjustRateInfo((tmpString.equals("-") ? null : ValueAdjustRateInfo.parse(tmpString)));
		lifetimeData.setTaxRate(Double.parseDouble(strArray[index++]));
		lifetimeData.setReceiveRate(Double.parseDouble(strArray[index++]));
		lifetimeData.setExchangeRate(Double.parseDouble(strArray[index++]));
		try {
			lifetimeData.setOfflinePaymentRate(Double.parseDouble(strArray[index++]));
			lifetimeData.setRedirectPaymentRate(Double.parseDouble(strArray[index++]));
		} catch (Exception e) {
			lifetimeData.setOfflinePaymentRate(1.0);
			lifetimeData.setRedirectPaymentRate(1.0);
		}
		
		return lifetimeData;
	}
	
	// Getters and Setters
	
	public double getData() {
		return data;
	}
	public void setData(double data) {
		this.data = data;
	}

    public DelayRateInfo getDelayRateInfo() {
        return delayRateInfo;
    }

    public void setDelayRateInfo(DelayRateInfo delayRateInfo) {
        this.delayRateInfo = delayRateInfo;
    }
    public RepeatRateInfo getRepeatRateInfo() {
        return repeatRateInfo;
    }

    public void setRepeatRateInfo(RepeatRateInfo repeatRateInfo) {
        this.repeatRateInfo = repeatRateInfo;
    }

    public ValueAdjustRateInfo getValueAdjustRateInfo() {
        return valueAdjustRateInfo;
    }

    public void setValueAdjustRateInfo(ValueAdjustRateInfo valueAdjustRateInfo) {
        this.valueAdjustRateInfo = valueAdjustRateInfo;
    }

    public double getTaxRate() {
		return taxRate;
	}
	public void setTaxRate(double taxRate) {
		this.taxRate = taxRate;
	}
	public double getConversionManyDivideOneRatio() {
		return conversionManyDivideOneRatio;
	}
	public void setConversionManyDivideOneRatio(double conversionManyDivideOneRatio) {
		this.conversionManyDivideOneRatio = conversionManyDivideOneRatio;
	}
	public double getConversionManyDivideOneThresholdRatio() {
		return conversionManyDivideOneThresholdRatio;
	}
	public void setConversionManyDivideOneThresholdRatio(
			double conversionManyDivideOneThresholdRatio) {
		this.conversionManyDivideOneThresholdRatio = conversionManyDivideOneThresholdRatio;
	}
	public double getConversionManyDivideOneAdjustRate() {
		return conversionManyDivideOneAdjustRate;
	}
	public void setConversionManyDivideOneAdjustRate(
			double conversionManyDivideOneAdjustRate) {
		this.conversionManyDivideOneAdjustRate = conversionManyDivideOneAdjustRate;
	}
	public double getShoppingCartRate() {
		return shoppingCartRate;
	}
	public void setShoppingCartRate(double shoppingCartRate) {
		this.shoppingCartRate = shoppingCartRate;
	}

	public double getValueRecoveryRate() {
		return valueRecoveryRate;
	}

	public void setValueRecoveryRate(double valueRecoveryRate) {
		this.valueRecoveryRate = valueRecoveryRate;
	}

	public double getReceiveRate() {
		return receiveRate;
	}

	public void setReceiveRate(double receiveRate) {
		this.receiveRate = receiveRate;
	}

	public double getExchangeRate() {
		return exchangeRate;
	}

	public void setExchangeRate(double exchangeRate) {
		this.exchangeRate = exchangeRate;
	}

	public double getOfflinePaymentRate() {
		return offlinePaymentRate;
	}

	public void setOfflinePaymentRate(double offlinePaymentRate) {
		this.offlinePaymentRate = offlinePaymentRate;
	}

	public double getRedirectPaymentRate() {
		return redirectPaymentRate;
	}

	public void setRedirectPaymentRate(double redirectPaymentRate) {
		this.redirectPaymentRate = redirectPaymentRate;
	}
	

}
