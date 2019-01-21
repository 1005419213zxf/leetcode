package com.litb.bid.component.bing;

import com.litb.basic.enums.LanguageType;
import com.litb.basic.enums.SiteType;

import java.util.HashMap;

public class BingRedirectSuccRateProvider implements BingRedirectSuccRateProviderInterface {
    private static final double DEFAULT_RATE = 0.95;

    private HashMap<String, Double> keyRateMap = new HashMap<String, Double>();

    public BingRedirectSuccRateProvider() {

        int merchantId = 1;
        keyRateMap.put(merchantId + "\t" + 1, 0.953808074);
        keyRateMap.put(merchantId + "\t" + 2, 0.900603527);
        keyRateMap.put(merchantId + "\t" + 3, 1.00);
        keyRateMap.put(merchantId + "\t" + 4, 1.00);
        keyRateMap.put(merchantId + "\t" + 5, 0.967616774);
        keyRateMap.put(merchantId + "\t" + 6, 0.723904202);
        keyRateMap.put(merchantId + "\t" + 7, 1.00);
        keyRateMap.put(merchantId + "\t" + 8, 0.965286236);
        keyRateMap.put(merchantId + "\t" + 9, 0.967965368);
        keyRateMap.put(merchantId + "\t" + 12, 1.00);
        keyRateMap.put(merchantId + "\t" + 13, 0.99161888);
        keyRateMap.put(merchantId + "\t" + 14, 0.971413585);
        keyRateMap.put(merchantId + "\t" + 15, 1.00);
        keyRateMap.put(merchantId + "\t" + 17, 0.971917808);
        keyRateMap.put(merchantId + "\t" + 18, 0.942873969);
        keyRateMap.put(merchantId + "\t" + 19, 1.00);
        keyRateMap.put(merchantId + "\t" + 20, 1.00);
        keyRateMap.put(merchantId + "\t" + 21, 0.915841584);
        keyRateMap.put(merchantId + "\t" + 22, 1.00);
        keyRateMap.put(merchantId + "\t" + 23, 1.00);
        keyRateMap.put(merchantId + "\t" + 24, 1.00);
        keyRateMap.put(merchantId + "\t" + 25, 0.915418502);
        keyRateMap.put(merchantId + "\t" + 26, 1.00);
        keyRateMap.put(merchantId + "\t" + 27, 1.00);
        keyRateMap.put(merchantId + "\t" + 28, 0.855072464);
        keyRateMap.put(merchantId + "\t" + 29, 1.00);
        keyRateMap.put(merchantId + "\t" + 30, 1.00);

        merchantId = 7;
        keyRateMap.put(merchantId + "\t" + 1, 0.942471516);
        keyRateMap.put(merchantId + "\t" + 2, 0.891082153);
        keyRateMap.put(merchantId + "\t" + 3, 0.936317613);
        keyRateMap.put(merchantId + "\t" + 4, 0.775545852);
        keyRateMap.put(merchantId + "\t" + 5, 0.91103941);
        keyRateMap.put(merchantId + "\t" + 6, 0.544857768);
        keyRateMap.put(merchantId + "\t" + 7, 0.986622074);
        keyRateMap.put(merchantId + "\t" + 8, 0.992221262);
        keyRateMap.put(merchantId + "\t" + 9, 0.941637395);
        keyRateMap.put(merchantId + "\t" + 11, 1.00);
        keyRateMap.put(merchantId + "\t" + 12, 0.956147893);
        keyRateMap.put(merchantId + "\t" + 13, 0.966246625);
        keyRateMap.put(merchantId + "\t" + 14, 0.966422621);
        keyRateMap.put(merchantId + "\t" + 15, 1.00);
        keyRateMap.put(merchantId + "\t" + 17, 0.930406147);
        keyRateMap.put(merchantId + "\t" + 18, 0.880070547);
        keyRateMap.put(merchantId + "\t" + 19, 1.00);
        keyRateMap.put(merchantId + "\t" + 20, 0.934306569);
        keyRateMap.put(merchantId + "\t" + 21, 0.917721519);
        keyRateMap.put(merchantId + "\t" + 22, 0.810631229);
        keyRateMap.put(merchantId + "\t" + 23, 0.9);
        keyRateMap.put(merchantId + "\t" + 24, 0.971246006);
        keyRateMap.put(merchantId + "\t" + 25, 0.813186813);
        keyRateMap.put(merchantId + "\t" + 26, 1.00);
        keyRateMap.put(merchantId + "\t" + 27, 0.813559322);
        keyRateMap.put(merchantId + "\t" + 28, 0.952941176);
        keyRateMap.put(merchantId + "\t" + 29, 1.00);
        keyRateMap.put(merchantId + "\t" + 30, 0.945520036);


//		int merchantId = 1;
//		keyRateMap.put(merchantId + "\t" + 1, 0.7161);
//		keyRateMap.put(merchantId + "\t" + 2, 0.6964);
//		keyRateMap.put(merchantId + "\t" + 3, 0.5482);
//		keyRateMap.put(merchantId + "\t" + 4, 0.2852);
//		keyRateMap.put(merchantId + "\t" + 5, 0.5207);
//		keyRateMap.put(merchantId + "\t" + 6, 0.5146);
//		keyRateMap.put(merchantId + "\t" + 7, 0.9077);
//		keyRateMap.put(merchantId + "\t" + 12, 0.7434);
//		keyRateMap.put(merchantId + "\t" + 15, 0.8646);
//		keyRateMap.put(merchantId + "\t" + 18, 0.7091);
//		keyRateMap.put(merchantId + "\t" + 19, 0.8095);
//		keyRateMap.put(merchantId + "\t" + 20, 0.6517);
//		keyRateMap.put(merchantId + "\t" + 21, 0.7059);
//		keyRateMap.put(merchantId + "\t" + 23, 0.7500);
//		keyRateMap.put(merchantId + "\t" + 26, 0.5000);
//
//		merchantId = 7;
//		keyRateMap.put(merchantId + "\t" + 1, 0.6623);
//		keyRateMap.put(merchantId + "\t" + 2, 0.7310);
//		keyRateMap.put(merchantId + "\t" + 3, 0.4988);
//		keyRateMap.put(merchantId + "\t" + 4, 0.3426);
//		keyRateMap.put(merchantId + "\t" + 5, 0.6210);
//		keyRateMap.put(merchantId + "\t" + 6, 0.5277);
//		keyRateMap.put(merchantId + "\t" + 7, 0.7734);
//		keyRateMap.put(merchantId + "\t" + 12, 0.7996);
//		keyRateMap.put(merchantId + "\t" + 15, 0.8923);
//		keyRateMap.put(merchantId + "\t" + 19, 0.8052);
//		keyRateMap.put(merchantId + "\t" + 20, 0.7383);
//		keyRateMap.put(merchantId + "\t" + 26, 0.2692);
//	}
    }

    @Override
    public BingRedirectSuccRateInfo getRediretSuccRate(SiteType siteType, LanguageType languageType) {
//		String key = siteType.getSiteCode() + "\t" + languageType.getLanguageId();
//		Double rate = keyRateMap.get(key);
//		if (rate != null){
//			if(rate < 0.5)
//				rate = 0.5;
//			if(rate > 1)
//				rate = 1.0;
//			return new RedirectSuccRateInfo(siteType, languageType, -1, rate);
//		}
//		else
//			return new RedirectSuccRateInfo(siteType, null, -1, DEFAULT_RATE);
        return new BingRedirectSuccRateInfo(siteType, null, -1, 1.0);
    }

    // TEST
    public static void main(String[] args) {
//        RedirectSuccRateProvider provider = new RedirectSuccRateProvider();
//        System.out.println(provider.getRediretSuccRate(SiteType.litb, LanguageType.en));
//        System.out.println(provider.getRediretSuccRate(SiteType.litb, LanguageType.fr));
//        System.out.println(provider.getRediretSuccRate(SiteType.litb, LanguageType.ar));
//        System.out.println(provider.getRediretSuccRate(SiteType.litb, LanguageType.cn));
//
//
//        System.out.println(provider.getRediretSuccRate(SiteType.mini, LanguageType.fr));

    }
}
