package com.litb.bid.component.adw.delay.create;

import com.litb.adw.lib.util.AdwordsCampaignNameHelper;
import com.litb.basic.util.DateHelper;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

public class DelayInfoCalculatorMR {
	
	public static class TaskMapper extends Mapper<LongWritable, Text, Text, Text> {
		
		private Date statDate = null;
		
		@Override
		public void setup(Context context) {
			String statDateString = context.getConfiguration().get("statDate");
			statDate = DateHelper.getShortDate(statDateString);
		}
		
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String line = value.toString();
			CampaignReportDateSegmentItem item = null;
			try {
				item = CampaignReportDateSegmentItem.parse(line);
			} catch (Exception e) {
				return;
			}
			if(statDate == null || statDate.equals(item.getStatDate()) == false) return;	
			String campaignKey = "" + item.getAccountId() + "\t" + item.getCampaignId();
			int daysInterval = DateHelper.getDeltaDays(item.getStatDate(), item.getGetDate());
			String _value = "" + item.getCampaignName() + "\t" + daysInterval + "\t" + item.getConvValue() + "\t" + item.getConversionManyPerClick();
			context.write(new Text(campaignKey), new Text(_value));
		}
	}
	
	public static class TaskReducer extends Reducer<Text, Text, Text, Text> {

		@Override
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			DelayInfoItem item = new DelayInfoItem();
			HashMap<String, Integer> tmpMap = new HashMap<String, Integer>();
			for(Text value : values) {
				StringTokenizer st = new StringTokenizer(value.toString(), "\t");
				if(st.countTokens() != 4) continue;
				String campaignName = null;
				int daysInterval = 0;
				double convValue = .0;
				long conversions = 0;
				try {
					campaignName = st.nextToken();
					daysInterval = Integer.parseInt(st.nextToken());
					convValue = Double.parseDouble(st.nextToken());
					conversions = Long.parseLong(st.nextToken());
				} catch (Exception e) {
					continue;
				}
				if(!tmpMap.containsKey(campaignName)) {
					tmpMap.put(campaignName, 1);
				} else {
					tmpMap.put(campaignName, tmpMap.get(campaignName)+1);
				}
				
				if(daysInterval >= 1 && daysInterval <= 30) {
					item.addConvValue(daysInterval-1, convValue);
					item.addConversions(daysInterval-1, conversions);
				}
			}
			
			String finalCampaignName = null;
			int appearTimes = -1;
			for(String cname : tmpMap.keySet()) {
				int tmpValue = tmpMap.get(cname);
				if(tmpValue > appearTimes) {
					appearTimes = tmpValue;
					finalCampaignName = cname;
				}
			}
			if(finalCampaignName == null) 
				return;
			
			item.setChannel(AdwordsCampaignNameHelper.getLitbAdChannel(finalCampaignName));
			item.setSiteType(AdwordsCampaignNameHelper.getSiteType(finalCampaignName));
			item.setLanguageType(AdwordsCampaignNameHelper.getLanguageTypeFromCampaignName(finalCampaignName));
			item.setCategoryId(AdwordsCampaignNameHelper.getCategoryIdFromCampaignName(finalCampaignName));
			
			if(item.getCategoryId() < 0)
				return;
			if(item.getLanguageType() == null) 
				return;
			
			// USED TO GET convValue ARRAY
			double[] convValueArray = item.getConvValueArray();
			boolean flag = false;
			for(int i=0;i<30;i++)
				if(convValueArray[i] > .0) {
					flag = true;
					break;
				}
			if(flag == false) return;
			int cnt = 0;
			for(int i=1;i<30;i++) {
				if(convValueArray[i] < convValueArray[i-1]) {
					convValueArray[i] = convValueArray[i-1];
					cnt ++;
					if(cnt >= 3) break;
				}
			}
			if(cnt >= 3)
				return;
			
			// USED TO GET conversions ARRAY
			long[] conversionsArray = item.getConversionsArray();
			flag = false;
			for(int i=0;i<30;i++) {
				if(conversionsArray[i] > .0) {
					flag = true;
					break;
				}
			}
			if(flag == false) return;
			cnt = 0;
			for(int i=1;i<30;i++) {
				if(conversionsArray[i] < conversionsArray[i-1]) {
					conversionsArray[i] = conversionsArray[i-1];
					cnt ++;
					if(cnt >= 3) break;
				}
			}
			if(cnt >= 3)
				return;
			
			String _key = "" + item.getChannel() + "\t" + item.getSiteType() + "\t" + item.getLanguageType() + "\t" + item.getCategoryId();
			String _value = "" + item.getConvValueArrayString() + "\t" + item.getConversionsArrayString();
			context.write(new Text(_key), new Text(_value));
		}
	}
}
