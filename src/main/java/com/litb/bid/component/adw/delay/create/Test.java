package com.litb.bid.component.adw.delay.create;

public class Test {
	public static void main(String[] args) throws Exception {
		String line = "1037685855	344798061	[ES][SRC][MINI][c4861][APP]<ES> <shopping> <item_target> <rlsa> (miniinthebox)	enabled	4	1	1	1	5.0	0.18	2016-04-24	2016-05-09";
		System.out.println(line);
		CampaignReportDateSegmentItem item = CampaignReportDateSegmentItem.parse(line);
		System.out.println(item);
		
		line = "adwords_display	litb	ja	1181	108.0_122.0_131.0_131.0_154.0_154.0_175.0_175.0_175.0_185.0_185.0_185.0_199.0_199.0_199.0_199.0_199.0_199.0_202.0_202.0_202.0_202.0_202.0_202.0_202.0_202.0_202.0_202.0_202.0_202.0	1_3_4_4_5_5_6_6_6_7_7_7_8_8_8_8_8_8_9_9_9_9_9_9_9_9_9_9_9_9";
		System.out.println(line);
		DelayInfoItem infoItem = DelayInfoItem.parse(line);
		System.out.println(infoItem.toString());
	}
}
