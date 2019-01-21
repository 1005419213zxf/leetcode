package com.litb.bid.component.adw.delay.create;

import com.amazonaws.services.elasticmapreduce.model.ActionOnFailure;
import com.amazonaws.services.elasticmapreduce.model.HadoopJarStepConfig;
import com.amazonaws.services.elasticmapreduce.model.StepConfig;
import com.litb.basic.util.DateHelper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DelayInfoCalculator extends Configured implements Tool {
	
	private static final String INPUT_DIR = "s3://litb.auto.bidding/adwords/auto_bidding/adwords_report/campaign_performance_date_segment/";
	private static final String OUTPUT_DIR = "s3://litb.adwords.test/delayRateInfo_oneDay/";
	private static final String JAR_PATH = "s3://litb.adwords.test/DeLayRate/test.jar";
	
	public static String getOutputDir(Date date) {
		return OUTPUT_DIR + getDateSuffix(date);
	}
	
	public static String getDateSuffix(Date date) {
		return new SimpleDateFormat("yyyy/MM/yyyy-MM-dd/", Locale.US).format(date);
	}
	
	private Date statDate;
	
	public DelayInfoCalculator(Date statDate) {
		this.statDate = statDate;
	}
	
	public int run(String[] args) throws Exception {
		Configuration conf = getConf();
		conf.set("statDate", DateHelper.getShortDateString(statDate));
		Job job = new Job(conf, "job name");
		job.setJarByClass(DelayInfoCalculator.class);
		job.setMapperClass(DelayInfoCalculatorMR.TaskMapper.class);
		job.setReducerClass(DelayInfoCalculatorMR.TaskReducer.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		Date date = statDate;
		for(int i=1;i<=30;i++) {
			date = DateHelper.addDays(1, date);
			FileInputFormat.addInputPath(job, new Path(INPUT_DIR + getDateSuffix(date)));
		}
		FileOutputFormat.setOutputPath(job, new Path(getOutputDir(statDate)));
		
		job.waitForCompletion(true);
		
		return 0;
	}
	
	public StepConfig getStep() {
		HadoopJarStepConfig hadoopJarStep = new HadoopJarStepConfig(JAR_PATH);	
		List<String> jarArgs = new ArrayList<String>();
		String mainClass = this.getClass().getName();
		
		String stepName = "delay info calculator ";
		jarArgs.add(DateHelper.getShortDateString(statDate));
		
		hadoopJarStep.setMainClass(mainClass);
		hadoopJarStep.setArgs(jarArgs);
		StepConfig step = new StepConfig().withName(stepName)
										  .withActionOnFailure(ActionOnFailure.TERMINATE_JOB_FLOW)
										  .withHadoopJarStep(hadoopJarStep);	
		return step;
	}
	
	// main
	
	public static void main(String[] args) throws Exception {
		int exitCode = ToolRunner.run(new DelayInfoCalculator(DateHelper.getShortDate(args[0])), args);
		System.exit(exitCode);
	}
	
}
