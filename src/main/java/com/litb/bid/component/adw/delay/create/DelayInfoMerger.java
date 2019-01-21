package com.litb.bid.component.adw.delay.create;

import com.amazonaws.services.elasticmapreduce.model.ActionOnFailure;
import com.amazonaws.services.elasticmapreduce.model.HadoopJarStepConfig;
import com.amazonaws.services.elasticmapreduce.model.StepConfig;
import com.litb.basic.util.DateHelper;
import com.litb.dm.aws.emr.MRHelper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DelayInfoMerger extends Configured implements Tool {
	
	// output
	public static String getAs3OutputDir(Date endDate, int interval){
		return  OUTPUT_DIR + DateHelper.getDateString(endDate, "yyyy/MM") + "/" + DateHelper.getShortDateString(endDate) + "_" + interval + "/";
	}
	
	private static final String OUTPUT_DIR = "s3://litb.adwords.test/delayRateInfo_history/";
	private static final String JAR_PATH = "s3://litb.adwords.test/DeLayRate/test.jar";
	
	private Date statDate;
	private int interval;
	
	public DelayInfoMerger(Date statDate, int interval) {
		this.statDate = statDate;
		this.interval = interval;
	}
	
//	public static String getOutputPath(Date date) {
//		return OUTPUT_DIR + DelayInfoCalculator.getDateSuffix(date);
//	}
	
	public int run(String[] args) throws Exception {
		Configuration conf = getConf();
		conf.set("statDate", DateHelper.getShortDateString(statDate));
		Job job = new Job(conf, "job name");
		job.setJarByClass(DelayInfoMerger.class);
		job.setMapperClass(DelayInfoMergerMR.TaskMapper.class);
		job.setReducerClass(DelayInfoMergerMR.TaskReducer.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		Date date = statDate;
		int totalCount = 0;
		for(int i=0;i<interval;i++) {
			String inputPath = DelayInfoCalculator.getOutputDir(date);
			totalCount += MRHelper.checkAndAddInputPath(job, inputPath);
			date = DateHelper.addDays(-1, date);
		}
		if(totalCount < interval * 0.9) {
			System.err.println("delay info merger error: input Files less than 90% (" + totalCount + ")");
			return 1;
		}
		String outputPath = getAs3OutputDir(statDate, interval);
		MRHelper.deleteAndAddOutputPath(job, outputPath);
		
		job.waitForCompletion(true);
		
		return 0;
	}
	
	public StepConfig getStep() {
		HadoopJarStepConfig hadoopJarStep = new HadoopJarStepConfig(JAR_PATH);	
		List<String> jarArgs = new ArrayList<String>();
		String mainClass = this.getClass().getName();
		
		String stepName = "delay info merger ";
		jarArgs.add(DateHelper.getShortDateString(statDate));
		jarArgs.add(interval + "");
		
		hadoopJarStep.setMainClass(mainClass);
		hadoopJarStep.setArgs(jarArgs);
		StepConfig step = new StepConfig().withName(stepName)
										  .withActionOnFailure(ActionOnFailure.TERMINATE_JOB_FLOW)
										  .withHadoopJarStep(hadoopJarStep);	
		return step;
	}
	
	// main
	
	public static void main(String[] args) throws Exception {
		Date statDate = DateHelper.getShortDate(args[0]);
		int interval = Integer.parseInt(args[1]);
		int exitCode = ToolRunner.run(new DelayInfoMerger(statDate, interval), args);
		System.exit(exitCode);
	}
}
