package com.litb.bid.component.adw.ppv.create;

import com.amazonaws.services.elasticmapreduce.model.ActionOnFailure;
import com.amazonaws.services.elasticmapreduce.model.HadoopJarStepConfig;
import com.amazonaws.services.elasticmapreduce.model.StepConfig;
import com.litb.bid.DirDef;
import com.litb.basic.enums.SiteType;
import com.litb.basic.util.DateHelper;
import com.litb.bid.object.LogUvData;
import com.litb.dm.aws.emr.MRDef;
import com.litb.dm.aws.emr.MRHelper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.util.*;

public class PpvUvMarker extends Configured implements Tool {
	
	public static final String PPV_TMP_JAR_PATH = "s3://litb.adwords.test/ppv_affair/test.jar";
	
//	private static final int UV_INTERVAL_TIME_IN_SECOND = 24 * 60 * 60;
	
	private SiteType siteType;
	private Date beginDate;
	private Date endDate;
	private int interval;
	
	// CONSTRUCTOR
	public PpvUvMarker(SiteType siteType, Date endDate, int interval) {
		this.siteType = siteType;
		this.endDate = endDate;
		this.interval = interval;
		this.beginDate = DateHelper.addDays(1-interval, endDate);
	}
	
	public static String getOutputFileDir(SiteType siteType, Date beginDate, Date endDate) {
		return "s3://litb.adwords.test/ppv_affair/ppv_uv_with_order_info/" + siteType + "/" +
			   DateHelper.getShortDateString(beginDate) + "_to_" + 
			   DateHelper.getShortDateString(endDate) + "/";
	}
	
	@Override
	public int run(String[] arg0) throws Exception {
		Configuration conf = getConf();
		
		Job job = new Job(conf, MRDef.getJobName(getClass().getSimpleName(), siteType, beginDate, endDate));
		job.setJarByClass(PpvUvMarker.class);
		job.setMapperClass(PpvUvMarker.TaskMapper.class);
		job.setReducerClass(PpvUvMarker.TaskReducer.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(NullWritable.class);
		
		for (Date date=endDate; !beginDate.after(date); date=DateHelper.addDays(-1, date)) {
			String inputFilePath = DirDef.getS3LogDailyDataDir(siteType, date);
			int isFileExist = MRHelper.checkAndAddInputPath(job, inputFilePath);
			if (isFileExist <= 0) {
				System.err.println("uv data on " + DateHelper.getShortDateString(date) + " lost");
				return 0;
			}
		}
		
		Date finalDate = DateHelper.addDays(30, endDate);
		for(Date date=finalDate; !beginDate.after(date); date=DateHelper.addDays(-1, date)) {
			String inputFilePath = DBOrderInfoUploader.getOutputPath(date);
			int isFileExist = MRHelper.checkAndAddInputPath(job, inputFilePath);
			if (isFileExist <= 0) {
				System.err.println("order data on " + DateHelper.getShortDateString(date) + " lost");
				return 0;
			}
		}
		
		String outputFilePath = getOutputFileDir(siteType, beginDate, endDate);
		MRHelper.deleteAndAddOutputPath(job, outputFilePath);
		job.waitForCompletion(true);
		return 0;
	}
	
	/*
	 * for EMR cluster used
	 */
	public StepConfig getStep() {
//		HadoopJarStepConfig hadoopJarStep = new HadoopJarStepConfig(DirDef.EMR_JAR_PATH);	
		// TEMP JAR PATH
		HadoopJarStepConfig hadoopJarStep = new HadoopJarStepConfig(PPV_TMP_JAR_PATH);	
		
		List<String> jarArgs = new ArrayList<String>();
		String mainClass = this.getClass().getName();
		
		String stepName = mainClass + " " + siteType.toString()  + " " + 
						  DateHelper.getShortDateString(beginDate) + " to " + 
						  DateHelper.getShortDateString(endDate);
		jarArgs.add(siteType.toString());
		jarArgs.add(DateHelper.getShortDateString(endDate));
		jarArgs.add("" + interval);
		
		hadoopJarStep.setMainClass(mainClass);
		hadoopJarStep.setArgs(jarArgs);
		StepConfig step = new StepConfig().withName(stepName)
										  .withActionOnFailure(ActionOnFailure.TERMINATE_JOB_FLOW)
										  .withHadoopJarStep(hadoopJarStep);	
		return step;
	}
	
	// main 
	public static void main(String[] args) throws Exception {
		SiteType siteType = null;
		Date endDate = null;
		int interval = 0;
		try {
			siteType = SiteType.valueOf(args[0].toLowerCase());
			endDate = DateHelper.getShortDate(args[1]);
			interval = Integer.parseInt(args[2]);
		} catch (Exception e) {
			System.err.println("Usage: <site type> <end date> <inderval>");
			System.exit(1);
		}
		
		int exitCode = ToolRunner.run(new PpvUvMarker(siteType, endDate, interval), args);
		System.exit(exitCode);
	}
	
	/*
	 * Mapper
	 */
	private static class TaskMapper extends Mapper<LongWritable, Text, Text, Text> {
		
		@Override
		public void map(LongWritable key, Text value, Context context) {
			 String line = value.toString();
	            try {
	                int segmentSize = line.split("\t").length;
	                if(segmentSize == DBOrderItem.SEGMENT_SIZE){
	                    DBOrderItem orderItem = DBOrderItem.parse(line);
	                    String cookie = orderItem.getCookie();
	                    if(cookie != null && cookie.length() > 0)
	                        context.write(new Text(cookie), new Text(line));
	                }
	                else{
	                    LogUvData uv = LogUvData.parse(line);
	                    String cookie = uv.getCookie();
	                    if(cookie != null && cookie.length() > 0)
	                        context.write(new Text(cookie), new Text(line));
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	                System.out.println(line);
	                return;
	            }
		}
	}
	
	/*
	 * Reducer
	 */
	private static class TaskReducer extends Reducer<Text, Text, Text, NullWritable>{
		@Override
		public void reduce(Text key, Iterable<Text> values, Context context) {
			List<DBOrderItem> orderItemList = new ArrayList<DBOrderItem>();

            List<LogUvData> uvList = new ArrayList<LogUvData>();
            List<LogUvData> orderedUvList = new ArrayList<LogUvData>();

            for(Text val : values){
                String line = val.toString();
                int segmentSize = line.split("\t").length;
                if(segmentSize == DBOrderItem.SEGMENT_SIZE){
                    DBOrderItem orderItem = DBOrderItem.parse(line);
                    orderItemList.add(orderItem);
                }
                else{
                    LogUvData uv = LogUvData.parse(line);
                    uvList.add(uv);
                }
            }

            Collections.sort(uvList, new Comparator<LogUvData>(){
                @Override
                public int compare(LogUvData uv1, LogUvData uv2) {
                    return uv1.getStartTimeInSecond() - uv2.getStartTimeInSecond();
                }
            });

            for (DBOrderItem order: orderItemList) {
                Date orderDate = order.getDatePurchased();
                LogUvData orderUv = null;
                for (LogUvData uv: uvList) {
                    if (DateHelper.getDateFromSeconds(uv.getStartTimeInSecond()).before(orderDate)) {
                        orderUv = uv;
                    }
                    else
                        break;
                }
                if (orderUv != null) {
                    Date uvDate = DateHelper.getDateFromSeconds(orderUv.getStartTimeInSecond());
                    if (DateHelper.getDeltaDays(uvDate, orderDate) <= 30 && !orderedUvList.contains(orderUv)) {
                        orderedUvList.add(orderUv);
                    }
                }
            }

            try {
                for (LogUvData uv: uvList) {
                    if (orderedUvList.contains(uv))
                        context.write(new Text(uv.toString() + "\t1"), NullWritable.get());
                    else
                        context.write(new Text(uv.toString() + "\t0"), NullWritable.get());

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
		}
	}
}
