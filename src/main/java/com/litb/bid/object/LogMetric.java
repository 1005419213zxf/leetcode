package com.litb.bid.object;

import java.util.*;
import java.util.Map.Entry;

public class LogMetric {
	private int pv;
	private int uv;
	private int bouncedUv;
	private int productPv;
	private int addedToCartUv;
	private int internalSearchUv;
	
	private Map<Integer, Integer> ppvUvMap = new HashMap<Integer, Integer>();
	
	// public methods
	public void mergeData(LogMetric m){
		pv += m.getPv();
		uv += m.getUv();
		bouncedUv += m.getBouncedUv();
		productPv += m.getProductPv();
		addedToCartUv += m.getAddedToCartUv();
		internalSearchUv += m.getInternalSearchUv();
		
		for(Entry<Integer, Integer> entry : m.ppvUvMap.entrySet()){
			Integer curUv = ppvUvMap.get(entry.getKey());
			if(curUv == null)
				ppvUvMap.put(entry.getKey(), entry.getValue());
			else
				ppvUvMap.put(entry.getKey(), curUv + entry.getValue());
		}
	}

	@Override
	public String toString(){
		// sort
		List<Entry<Integer, Integer>> entryList = new ArrayList<Entry<Integer,Integer>>(ppvUvMap.entrySet());
		Collections.sort(entryList, new Comparator<Entry<Integer, Integer>>() {
			@Override
			public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {
				return o1.getKey() - o2.getKey();
			}
		});

		// output
		String uvStr = "";
		for(Entry<Integer, Integer> entry : entryList)
			uvStr += "_" + entry.getKey() + ":" + entry.getValue();
		if(uvStr.length() > 0)
			uvStr = uvStr.substring(1);
		else
			uvStr = "-";
		
		return pv + "\t" + uv + "\t" + bouncedUv + "\t" + productPv + "\t" + addedToCartUv + "\t" + internalSearchUv + "\t" + uvStr; 
	}
	
	public static LogMetric parse(String line){
		LogMetric metric = new LogMetric();
		String[] strArr = line.split("\t");
		int index = 0;
		
		metric.pv = Integer.parseInt(strArr[index++]);
		metric.uv = Integer.parseInt(strArr[index++]);
		metric.bouncedUv = Integer.parseInt(strArr[index++]);
		metric.productPv = Integer.parseInt(strArr[index++]);
		metric.addedToCartUv = Integer.parseInt(strArr[index++]);
		metric.internalSearchUv = Integer.parseInt(strArr[index++]);
		
		String uvString = strArr[index++];
		if(!uvString.equals("-")){
			String[] uvStrArr = uvString.split("_");
			for(String uvStr : uvStrArr){
				String[] arr = uvStr.split(":");
				metric.ppvUvMap.put(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]));
			}
		}
		
		return metric;
	}
	
	// Getters and Setters
	
	public int getPv() {
		return pv;
	}

	public void setPv(int pv) {
		this.pv = pv;
	}

	public int getUv() {
		return uv;
	}

	public void setUv(int uv) {
		this.uv = uv;
	}

	public int getBouncedUv() {
		return bouncedUv;
	}

	public void setBouncedUv(int bouncedUv) {
		this.bouncedUv = bouncedUv;
	}

	public int getProductPv() {
		return productPv;
	}

	public void setProductPv(int productPv) {
		this.productPv = productPv;
	}

	public int getAddedToCartUv() {
		return addedToCartUv;
	}

	public void setAddedToCartUv(int addedToCartUv) {
		this.addedToCartUv = addedToCartUv;
	}

	public int getInternalSearchUv() {
		return internalSearchUv;
	}

	public void setInternalSearchUv(int internalSearchUv) {
		this.internalSearchUv = internalSearchUv;
	}

	public Map<Integer, Integer> getPpvUvMap() {
		return ppvUvMap;
	}

	public void setPpvUvMap(Map<Integer, Integer> ppvUvMap) {
		this.ppvUvMap = ppvUvMap;
	}

	// main for test
	public static void main(String[] args) {
		LogMetric metric = new LogMetric();
		metric.pv = 16;
		metric.uv = 8;
		metric.bouncedUv = 2;
		metric.internalSearchUv = 3;
		metric.productPv = 5;
		
		metric.ppvUvMap.put(0, 3);
		metric.ppvUvMap.put(3, 1);
		metric.ppvUvMap.put(15, 5);
		
		String line = metric.toString();
		System.out.println(line);
		
		metric = parse(line);
		System.out.println(metric.toString());
	}
	
}
