/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package spok.utils;

/**
 * Utility class for Arrays
 * 
 * @author Tobias Helmus
 * @created 19. Dezember 2005
 * 
 */
public class ArrayUtils {

	/**
	 * static method for getting the maximum value of an array of doubles
	 * 
	 * @param array
	 *            the array to get the max value of
	 * @return the maximum value as a double
	 */
	public static double getMaxValue(double[] array) {
		double max = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] > max) {
				max = array[i];
			}
		}
		return max;
	}

	/**
	 * static method for getting the minimum value of an array of doubles
	 * 
	 * @param array
	 *            the array to get the min value of
	 * @return the minimum value as a double
	 */
	public static double getMinValue(double[] array) {
		double min = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] < min) {
				min = array[i];
			}
		}
		return min;
	}
}
