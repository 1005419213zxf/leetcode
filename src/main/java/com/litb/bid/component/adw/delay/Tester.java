package com.litb.bid.component.adw.delay;

import com.litb.adw.lib.enums.LitbAdChannel;
import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;

import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * 
 * @author zhangrui1
 *
 */
public class Tester {
	private static final boolean TEST_S3 = true;
	private static final String FILE_PATH = "D:\\book.txt";
	private static final int INTERVAL_DAYS = 1;
	private static final int PREDICT_OFFSET_DAYS = 30;
	
	public static void main(String[] args) throws Exception {
		System.out.println("start litb ...");
		DelayRateProviderInterface litbProvider = TEST_S3 ? new S3DelayRateProvider(SiteType.litb) : new LocalDelayRateProvider(SiteType.litb, FILE_PATH);
		System.out.println("start mini ...");
		DelayRateProviderInterface miniProvider = TEST_S3 ? new S3DelayRateProvider(SiteType.mini) : new LocalDelayRateProvider(SiteType.mini, FILE_PATH);
		
		Scanner in = new Scanner(System.in);
		System.out.println("input : [Site Type] [channel] [language Type] [category Id] [days Interval]");
		while(in.hasNext()) {
			String line = in.nextLine();
			if(line.equals("exit") || line.equals("quit"))
				break;
			
			StringTokenizer st = new StringTokenizer(line, " ");
			if(st.countTokens() != 5) {
				System.err.println("usage : [Site Type] [channel] [language Type] [category Id] [days Interval]");
				continue;
			}
			SiteType siteType = null;
			LitbAdChannel channel = null;
			LanguageType languageType = null;
			int categoryId = -1;
			int daysInterval = 0;
			
			String s = null;
			s = st.nextToken();
			siteType = (s.equals("-1")) ? null : SiteType.valueOf(s);
			s = st.nextToken();
			channel = (s.equals("-1")) ? null : LitbAdChannel.valueOf(s);
			s = st.nextToken();
			languageType = (s.equals("-1")) ? null : LanguageType.valueOf(s);
			s = st.nextToken();
			categoryId = Integer.valueOf(s);
			s = st.nextToken();
			daysInterval = Integer.valueOf(s);
			if(daysInterval < 1) {
				System.err.println("days Interval should >= 1 !!!");
				continue;
			}
			
			DelayRateInfo info = null;
			for(int getOffsetDays=1; getOffsetDays<=30; getOffsetDays++) {
				DelayRateProviderInterface provider = (siteType == SiteType.litb) ? litbProvider : miniProvider;
				info = provider.getDelayRate(channel, siteType, languageType, categoryId, INTERVAL_DAYS, getOffsetDays, PREDICT_OFFSET_DAYS);
				if(info != null) {
					System.out.println(info.getDelayRate());
					if(getOffsetDays == 30) {
						System.out.println(info.getSiteType() + " " + info.getChannel() + " "
										   + info.getLanguageType() + " " + info.getCid());
					}
				} else {
					System.out.print(getOffsetDays + ":" + null + " ");
					if(getOffsetDays == 30)
						System.out.println("");
				}
			}
		}
	}
}
