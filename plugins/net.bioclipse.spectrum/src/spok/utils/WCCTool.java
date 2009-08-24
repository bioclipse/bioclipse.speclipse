/* Copyright (C) 2006  Ron Wehrens <r.wehrens@science.ru.nl>
 *
 * This software is published and distributed under artistic license.
 * The intent of this license is to state the conditions under which this Package
 * may be copied, such that the Copyright Holder maintains some semblance
 * of artistic control over the development of the package, while giving the
 * users of the package the right to use and distribute the Package in a
 * more-or-less customary fashion, plus the right to make reasonable modifications.
 *
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTIBILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * The complete text of the license can be found in a file called LICENSE
 * accompanying this package.
 */
package spok.utils;


public class WCCTool {

	public static double wcc(double[] positions1, double[] intensities1,
			double[] positions2, double[] intensities2,
			double width) {
//		 WCC is de wcccor op X,Y gedeeld door de wortel uit het
//		 product van de waccor op X en Y...
		return wcccor(positions1, intensities1, positions2, intensities2, width)/
		  Math.sqrt(waccor(positions1, intensities1, width)*waccor(positions2, intensities2, width));
	}

	public static double waccor(double[] positions, double[] intensities, double width) {
		int n = positions.length;
		int i, j;
		double sum=0.0, dif;

		for (i=0; i<(n-1); i++) {
			for (j=(i+1); j<n; j++) {
				dif = Math.abs(positions[j] - positions[i]); /* check distance */
				if (dif < width) { /* close */
					sum += intensities[i] * intensities[j] * (1.0 - (dif/width));
				}
			}
		}

		sum = 2.0*sum;
		for (i = 0; i < n; i++) {
			sum += intensities[i]*intensities[i];
		}

		return(sum);
	}

	public static double wcccor(double[] positions1, double[] intensities1,
			double[] positions2, double[] intensities2,
			double width) {
		int n1 = positions1.length;
		int n2 = positions2.length;
		int i, j;
		double sum=0.0, dif;

		for (i=0; i<n1; i++) {
			for (j=0; j<n2; j++) {
				dif = Math.abs(positions2[j] - positions1[i]); /* check distance */
				if (dif < width) /* close */
					sum += intensities1[i] * intensities2[j] * (1.0 - (dif/width));
			}
		}

		return(sum);
	}

}

