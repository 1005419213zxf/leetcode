package com.litb.bid.component.adw.delay.create;

import com.litb.adw.lib.enums.LitbAdChannel;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.StringTokenizer;

public class DelayInfoMergerMR {
	
	public static class TaskMapper extends Mapper<LongWritable, Text, Text, Text> {
		
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String line = value.toString();
			DelayInfoItem item;
			try {
				item = DelayInfoItem.parse(line);
			} catch (Exception e) {
				return;
			}
			String _key = "" + item.getChannel() + "\t" + item.getSiteType() + "\t" + item.getLanguageType() + "\t" + item.getCategoryId();
			String _value = "" + item.getConvValueArrayString() + "\t" + item.getConversionsArrayString();
			context.write(new Text(_key), new Text(_value));
		}
	}
	
	public static class TaskReducer extends Reducer<Text, Text, Text, Text> {

		@Override
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			DelayInfoItem item = new DelayInfoItem();
			StringTokenizer st = new StringTokenizer(key.toString(), "\t");
			if(st.countTokens() != 4) return;
			LitbAdChannel channel = null;
			SiteType siteType = null;
			LanguageType languageType = null;
			int categoryId = 0;
			try {
				channel = LitbAdChannel.valueOf(st.nextToken());
		    	siteType = SiteType.valueOf(st.nextToken());
		    	languageType = LanguageType.valueOf(st.nextToken());
		    	categoryId = Integer.parseInt(st.nextToken());
			} catch (Exception e) {
				return;
			}
			item.setChannel(channel);
	    	item.setSiteType(siteType);
	    	item.setLanguageType(languageType);
	    	item.setCategoryId(categoryId);
	    	
	    	double[] convValueArray = null;
	    	long[] conversionsArray = null;
			for(Text value : values) {
				st = new StringTokenizer(value.toString(), "\t");
				if(st.countTokens() != 2) continue;
				try {
					convValueArray = DelayInfoItem.parseConvValueArrayString(st.nextToken());
					conversionsArray = DelayInfoItem.parseConversionsArrayString(st.nextToken());
				} catch (Exception e) {
					continue;
				}
				item.mergeValues(convValueArray, conversionsArray);
			}
			
			String _key = "" + item.getChannel() + "\t" + item.getSiteType() + "\t" + item.getLanguageType() + "\t" + item.getCategoryId();
			String _value = "" + item.getConvValueArrayString() + "\t" + item.getConversionsArrayString();
			context.write(new Text(_key), new Text(_value));
		}
	}
}
