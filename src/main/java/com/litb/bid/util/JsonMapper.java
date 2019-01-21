package com.litb.bid.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.IOException;

/**
 * Transfer object to JSon string, or transfer JSon string to object. Realized by Jackson lib which is thread-safe.
 * @author Rui Zhang
 */
public class JsonMapper {
	private static ObjectMapper mapper = new ObjectMapper();
	private static ObjectMapper mapper2 = new ObjectMapper();
	static{
		mapper2.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
	}
	
	// public methods
	public static String toJsonString(Object object) throws JsonProcessingException {
		return mapper.writeValueAsString(object);
	}
	
	public static String toJsonStringWithPrivateFields(Object object) throws JsonProcessingException {
		return mapper2.writeValueAsString(object);
	}
	
	
	public static Object parseJsonString(String str, Class<?> class1) throws IOException{
		return mapper.readValue(str, class1);
	}
	
	// main for test
	public static void main(String[] args) throws IOException {
//		DelayRateInfo delayRate = new DelayRateInfo();
//		String str = JsonMapper.toJsonString(delayRate);
//		System.out.println(str);
//
//		delayRate = (DelayRateInfo)JsonMapper.parseJsonString(str, DelayRateInfo.class);
//
//		System.out.println(JsonMapper.toJsonString(delayRate));
//
//		String s = null;
//		System.out.println(JsonMapper.toJsonString(s));
//
//		System.out.println(JsonMapper.parseJsonString("null", String.class));
	}
}
