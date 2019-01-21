package com.litb.bid.component.adw.ppv;

import com.litb.bid.Conf;

/*
 * this class is used for Polynomial fitting.
 * but now we use f(x) =a * x to fitting, so 
 * if you use fitting, please use the f(x)=a*x method.
 */
class FittingTools {
	
	public static double[] getFittingCrArray(double[] cr) {
		double[] filteredCr = new double[cr.length];
		double a = .0, sum = .0;
		for (int i=0; i<cr.length; i++) {
			a += (double)i * cr[i];
			sum += (double)i * (double)i;
		}
		a /= sum;
		for (int i=0; i< filteredCr.length; i++) {
			filteredCr[i] = (double)i * a;
			if (filteredCr[i] > 1.0)
				filteredCr[i] = 1.0;
		}
		return filteredCr;
	}
	
	public static double[] getAdjustedCrArray(PredictiveItem item) {
		//for (int i=0; i<Conf.PPV_DIMENSION; i++)
		double[] filteredCr = new double[Conf.PPV_DIMENSION];
		try {
			int orderSum = 0;
			for (int i=0; i<Conf.PPV_DIMENSION; i++)
				orderSum += item.getOrderedUvNumber()[i];
			int uvSum = 0;
			for (int i=0; i<Conf.PPV_DIMENSION; i++)
				uvSum += item.getUvNumber()[i];
			double avgCr = (double)orderSum / (double)uvSum;
			for (int i=0; i<Conf.PPV_DIMENSION; i++){
				double ratio = item.getUvNumber()[i] / (double)uvSum;
				filteredCr[i] = ratio * item.getCr()[i]
						+ (1-ratio) * avgCr;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return filteredCr;
	}

//	@Deprecated
//	public static double[] getFilteredCr(double[] cr, int dimension) {
//		final WeightedObservedPoints obs = new WeightedObservedPoints();
//		for (int i=0; i<cr.length; i++) {
//			obs.add((double)i, cr[i]);
//		}
//		final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(dimension);
//		final double[] coeff = fitter.fit(obs.toList());
//		double[] filteredCr = new double[cr.length];
//		for (int i=0; i<cr.length; i++) {
//			for (int j=0; j<=dimension; j++) {
//				filteredCr[i] += coeff[j] * Math.pow(i, j);
//			}
//		}
//		return filteredCr;
//	}
	
	
	// TEST
	public static void main(String[] args) {
		double[] cr = new double[26];
		for (int i=0; i<26; i++) {
			cr[i] = Math.random();
		}
		double[] predictedCr = getFittingCrArray(cr);
		System.out.println("cr");
		for (double c : cr)
			System.out.print("\t" + c);
		System.out.println("");
		System.out.println("predicted cr");
		for (double c : predictedCr)
			System.out.print("\t" + c);
		System.out.println("");
	}
}
