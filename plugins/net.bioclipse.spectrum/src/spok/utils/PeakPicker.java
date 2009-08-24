/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package spok.utils;

import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;
import org.xmlcml.cml.element.CMLSpectrumData;

/**
 * @author Tobias Helmus
 * @created 19. Dezember 2005
 * 
 * Second derivative based peak picking taken from jumbo4.6 euclid code written
 * by Peter Murray-Rust and adapted
 * 
 */
public class PeakPicker {

	double lowerPeakLimit = 5;

	private double[] xValArray;

	private double[] yValArray;

	private String xAxisLabel;

	private String yAxisLabel;

	public PeakPicker(CMLSpectrumData dataList) {
		this.xValArray = dataList.getXaxisElements().get(0).getArrayElements()
				.get(0).getDoubles();
		this.yValArray = dataList.getYaxisElements().get(0).getArrayElements()
				.get(0).getDoubles();
		xAxisLabel = dataList.getXaxisElements().get(0).getArrayElements().get(
				0).getUnits();
		yAxisLabel = dataList.getYaxisElements().get(0).getArrayElements().get(
				0).getUnits();
	}

	public CMLPeakList getPeakArray() {
		int windo = 7;
		double[] broad = applyFilter(getFilter(windo, GAUSSIAN), this.yValArray);
		double[] deriv2 = applyFilter(getFilter(windo,
				GAUSSIAN_SECOND_DERIVATIVE), broad);
		double[] peakTemp = deriv2;

//		peakTemp = trimSpectrumBelow(peakTemp, 0.0);
		peakTemp = multiplyBy(-1.0, peakTemp);
		CMLPeakList peakList = makeBars(peakTemp, windo);
		// fitPeaks();
		return peakList;

	}

	/**
	 * apply filter. convolute array with another array. This is 1-D image
	 * processing. If <TT>filter</TT> has <= 1 element, return <TT>this</TT>
	 * unchanged. <TT>filter</TT> should have an odd number of elements. The
	 * filter can be created with a IntArray constructor filter is moved along
	 * stepwise
	 * </P>
	 * 
	 * @param filter
	 *            to apply normally smaller than this
	 * @param yvals
	 * @return filtered array
	 */
	public double[] applyFilter(double[] filter, double[] yvals) {
		int nelem = yvals.length;
		int filterNelem = filter.length;
		if (nelem == 0 || filter == null || filterNelem <= 1) {
			return yvals;
		}
		int nfilter = filter.length;
		int midfilter = (nfilter - 1) / 2;
		double[] temp = new double[nelem];
		double wt = 0;
		double sum = 0;
		for (int j = 0; j < midfilter; j++) {
			// get weight
			wt = 0.0;
			sum = 0.0;
			int l = 0;
			for (int k = midfilter - j; k < nfilter; k++) {
				wt += Math.abs(filter[k]);
				sum += filter[k] * yvals[l++];
			}
			temp[j] = sum / wt;
		}
		wt = absSumAllElements(filter);
		for (int j = midfilter; j < nelem - midfilter; j++) {
			sum = 0.0;
			int l = j - midfilter;
			for (int k = 0; k < nfilter; k++) {
				sum += filter[k] * yvals[l++];
			}
			temp[j] = sum / wt;
		}
		for (int j = nelem - midfilter; j < nelem; j++) {
			// get weight
			wt = 0.0;
			sum = 0.0;
			int l = j - midfilter;
			for (int k = 0; k < midfilter + nelem - j; k++) {
				wt += Math.abs(filter[k]);
				sum += filter[k] * yvals[l++];
			}
			temp[j] = sum / wt;
		}
		return temp;
	}

	public final static String GAUSSIAN = "Gaussian";

	public final static String GAUSSIAN_FIRST_DERIVATIVE = "Gaussian First Derivative";

	public final static String GAUSSIAN_SECOND_DERIVATIVE = "Gaussian Second Derivative";

	/**
	 * creates a filter based on Gaussian and derivatives. Scaled so that
	 * approximately 2.5 sigma is included (that is value at edge is ca 0.01 of
	 * centre
	 * 
	 * @param halfWidth
	 * @param function
	 */
	public static double[] getFilter(int halfWidth, String function) {
		if (!function.equals(GAUSSIAN)
				&& !function.equals(GAUSSIAN_FIRST_DERIVATIVE)
				&& !function.equals(GAUSSIAN_SECOND_DERIVATIVE))
			return null;
		if (halfWidth < 1)
			halfWidth = 1;
		double xar[] = new double[2 * halfWidth + 1];
		double limit = 7.0; // ymin ca 0.01

		double sum = 0;
		double x = 0.0;
		double y = 1.0;
		// double dHalf = Math.sqrt(0.693);
		double dHalf = limit * 0.693 * 0.693 / (double) halfWidth;
		for (int i = 0; i <= halfWidth; i++) {
			if (function.equals(GAUSSIAN))
				y = Math.exp(-x * x);
			if (function.equals(GAUSSIAN_FIRST_DERIVATIVE))
				y = -2 * x * Math.exp(-x * x);
			if (function.equals(GAUSSIAN_SECOND_DERIVATIVE))
				y = (4 * (x * x) - 2.0) * Math.exp(-x * x);
			xar[halfWidth + i] = (function.equals(GAUSSIAN_FIRST_DERIVATIVE)) ? -y
					: y;
			xar[halfWidth - i] = y;
			sum += (i == 0) ? y : 2 * y;
			x += dHalf;
		}
		// normalise for Gaussian (only = the others are meaningless)
		if (function.equals(GAUSSIAN)) {
			for (int i = 0; i < 2 * halfWidth + 1; i++) {
				xar[i] /= sum;
			}
		}
		return xar;
	}

	public CMLPeakList makeBars(double[] arr, int windo) {
		int npoints = arr.length;
		double[] arry = new double[npoints];
		// 100 because percent
		double arrMax = ArrayUtils.getMaxValue(arr);
		double arrMin = ArrayUtils.getMinValue(arr);
		double arrRange = arrMax - arrMin;
		double yRange = arrRange / 100;
		double yMin = arrMin;
		for (int i = 0; i < npoints; i++) {
			arry[i] = arr[i];
		}
		CMLPeakList peaks = new CMLPeakList();
		// remove baseline ripples
		double yCut = 0.01 * arrMax;
		for (int i = 0; i < npoints; i++) {
			arry[i] = (arry[i] < yCut) ? 0.0 : arry[i];
		}
		// and end effects
		for (int i = 0; i < windo; i++) {
			arry[i] = 0.0;
			arry[npoints - 1 - i] = 0.0;
		}
		for (int i = 1; i < npoints - 1; i++) {
			if (arry[i] > arry[i - 1] && arry[i] > arry[i + 1]) {
				// int peak = i;
				// (plotStyle.getXDirection() < 0) ? npoints - 1 - i : i;
				double peakSize = Math.abs(arry[i] - yMin);
				if (peakSize < lowerPeakLimit * yRange)
					continue;
				CMLPeak peak = new CMLPeak();
				peak.setXValue(this.xValArray[i]);
				peak.setYValue(this.yValArray[i]);
				if (this.xAxisLabel != null) {
					peak.setXUnits(this.xAxisLabel);
				}
				if (this.yAxisLabel != null) {
					peak.setYUnits(this.yAxisLabel);
				}
				// peak.setYValue(arry[i]);
				peaks.addPeak(peak);
			}
		}
		return peaks;
	}

	/**
	 * sum of all absolute element values.
	 * 
	 * @param filter
	 * @return sigma(abs(this(i)))
	 */
	public double absSumAllElements(double[] array) {
		int nelem = array.length;
		double sum = 0.0;
		for (int i = 0; i < nelem; i++) {
			sum += Math.abs(array[i]);
		}
		return sum;
	}

	// removes all spectrum except that below
	double[] trimSpectrumBelow(double[] arr, double limit) {
		int npoints = arr.length;
		double[] arrx = arr;
		double[] arr1 = new double[npoints];
		for (int i = 0; i < npoints; i++) {
			arr1[i] = Math.min(arrx[i], limit);
		}
		return arr1;
	}

	/**
	 * array multiplication by a scalar. creates new array; does NOT modify
	 * 'this'
	 * 
	 * @param f
	 *            multiplier
	 * @param peakTemp
	 * @return the new array
	 */
	public double[] multiplyBy(double f, double[] peakTemp) {
		for (int i = 0; i < peakTemp.length; i++) {
			peakTemp[i] *= f;
		}
		return peakTemp;
	}
}
