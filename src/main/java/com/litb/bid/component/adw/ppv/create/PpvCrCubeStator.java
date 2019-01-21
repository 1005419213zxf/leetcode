package com.litb.bid.component.adw.ppv.create;

import com.amazonaws.services.elasticmapreduce.model.ActionOnFailure;
import com.amazonaws.services.elasticmapreduce.model.HadoopJarStepConfig;
import com.amazonaws.services.elasticmapreduce.model.StepConfig;
import com.litb.adw.lib.enums.AdwordsChannel;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;
import com.litb.basic.util.DateHelper;
import com.litb.bid.component.adw.ppv.PredictiveItem;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

public class PpvCrCubeStator extends Configured implements Tool {

	private SiteType siteType;
	private Date beginDate;
	private Date endDate;
	private int interval;
	
	// CONSTRUCTOR
	public PpvCrCubeStator(SiteType siteType, Date endDate, int interval) {
		this.siteType = siteType;
		this.endDate = endDate;
		this.interval = interval;
		this.beginDate = DateHelper.addDays(1-interval, endDate);
	}
	
	public static String getOutputFileDir(SiteType siteType, Date beginDate, Date endDate) {
		return "s3://litb.adwords.test/ppv_affair/ppv_cr_cube_info/" + siteType + "/" +
			   DateHelper.getShortDateString(beginDate) + "_to_" + 
			   DateHelper.getShortDateString(endDate) + "/";
	}
	
	@Override
	public int run(String[] arg0) throws Exception {
		Configuration conf = getConf();
		
		Job job = new Job(conf, MRDef.getJobName(getClass().getSimpleName(), siteType, beginDate, endDate));
		job.setJarByClass(PpvCrCubeStator.class);
		job.setMapperClass(PpvCrCubeStator.TaskMapper.class);
		job.setReducerClass(PpvCrCubeStator.TaskReducer.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(NullWritable.class);
		
		
		String inputFilePath = PpvUvMarker.getOutputFileDir(siteType, beginDate, endDate);
		int isFileExist = MRHelper.checkAndAddInputPath(job, inputFilePath);
		if (isFileExist <= 0) {
			System.err.println("marked uv data from " + DateHelper.getShortDateString(beginDate) + " to " 
									+ DateHelper.getShortDateString(endDate) + " lost");
			return 0;
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
		HadoopJarStepConfig hadoopJarStep = new HadoopJarStepConfig(PpvUvMarker.PPV_TMP_JAR_PATH);	
		
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
			System.err.println("Usage: <site type> <end date> <interval>");
			System.exit(1);
		}
		
		int exitCode = ToolRunner.run(new PpvCrCubeStator(siteType, endDate, interval), args);
		System.exit(exitCode);
	}	
	
	
	/*
	 * Mapper
	 */
	private static class TaskMapper extends Mapper<LongWritable, Text, Text, Text> {
		
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String line = value.toString();
			int index = line.lastIndexOf("\t");
			String uvDataString = line.substring(0, index);
			boolean ordered = line.substring(index+1).equals("1") ? true : false;
			LogUvData uvData = LogUvData.parse(uvDataString);
			// KEY
			AdwordsChannel channel = uvData.getTrackingInfo().getChannel();
			LanguageType languageType = uvData.getTrackingInfo().getLanguageType();
			boolean isFromMobileDevice = uvData.isFromMobileDevice();
			String cpTag = uvData.getTrackingInfo().getCpTag();
			
			String outKey = channel + "\t" + languageType + "\t" + isFromMobileDevice + "\t" + cpTag;
			// VALUE
			int uniqueProductVisitNumber = uvData.getUniqueProductVisitNum();
			String outValue = uniqueProductVisitNumber + "\t" + ordered;
			context.write(new Text(outKey), new Text(outValue));
		}
	}
	
	/*
	 * Reducer
	 */
	private static class TaskReducer extends Reducer<Text, Text, Text, NullWritable>{
		@Override
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			String line = key.toString();
			StringTokenizer st = new StringTokenizer(line, "\t");
			AdwordsChannel channel = AdwordsChannel.valueOf(st.nextToken());
			LanguageType languageType = LanguageType.valueOf(st.nextToken());
			boolean isFromMobileDevice = Boolean.valueOf(st.nextToken());
			String cpTag = st.nextToken();
			
			PredictiveItem item = new PredictiveItem(channel, languageType, isFromMobileDevice, cpTag);
			
			for (Text value : values) {
				StringTokenizer st2 = new StringTokenizer(value.toString(), "\t");
				int uniqueProductVisitNumber = Integer.parseInt(st2.nextToken());
				boolean ordered = Boolean.parseBoolean(st2.nextToken());
				item.merge(uniqueProductVisitNumber, ordered);
			}
			
			boolean includePredictedCr = false;
			context.write(new Text(item.toString(includePredictedCr)), NullWritable.get());
		}
	}
}
