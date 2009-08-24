/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package net.bioclipse.spectrum.editor;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.xmlcml.cml.element.CMLPeak;

/**
 * Sorter for PeakTable toggles the values when clicked on column name. also
 * reverse sort for values.
 * 
 * @authors Stefan Kuhn & Sashikanth.Chitti
 */

public class PeakSorter extends ViewerSorter {

	// Criteria that the instance uses
	public PeakTableViewer peakTableViewer;

	public boolean reverseSort;

	private Object property;

	public PeakSorter(String columnname, PeakTableViewer peakTableViewer,
			boolean reverseSort) {
		super();
		property = columnname;
		this.peakTableViewer = peakTableViewer;
		this.reverseSort = reverseSort;
	}

	/*
	 * (non-Javadoc) Method declared on ViewerSorter.
	 */
	public int compare(Viewer viewer, Object obj1, Object obj2) {

		int comp = 0;
		CMLPeak p1 = (CMLPeak) obj1;
		CMLPeak p2 = (CMLPeak) obj2;

		int columnIndex = peakTableViewer.getColumnNames().indexOf(property);

		// Determine which field to sort on, then sort
		// on that field
		// System.err.println("columnIndex "+columnIndex);
		switch (columnIndex) {
		case 0: // X_AXIS
			if (reverseSort) {
				comp = new Double(-(p1.getXValue())).compareTo(new Double(-(p2
						.getXValue())));
			} else {
				comp = new Double(p1.getXValue()).compareTo(new Double(p2
						.getXValue()));
			}
			break;
		case 1: // Y_AXIS COLUMN
			if (reverseSort) {
				comp = new Double(-(p1.getYValue())).compareTo(new Double(-(p2
						.getYValue())));
			} else {
				comp = new Double(p1.getYValue()).compareTo(new Double(p2
						.getYValue()));
			}
			break;
		case 2: // additional columns

			comp = p1.getAttribute(peakTableViewer.getCmlPeakFields()[0])
					.getValue().compareTo(
							p2.getAttribute(
									peakTableViewer.getCmlPeakFields()[0])
									.getValue());

			break;

		case 3: // Additional_FIelds COLUMN

			comp = p1.getAttribute(peakTableViewer.getCmlPeakFields()[1])
					.getValue().compareTo(
							p2.getAttribute(
									peakTableViewer.getCmlPeakFields()[1])
									.getValue());
			break;
		case 4: // Additional_FIelds COLUMN

			comp = p1.getAttribute(peakTableViewer.getCmlPeakFields()[2])
					.getValue().compareTo(
							p2.getAttribute(
									peakTableViewer.getCmlPeakFields()[2])
									.getValue());
			break;
		case 5: // Additional_FIelds COLUMN
			comp = p1.getAttribute(peakTableViewer.getCmlPeakFields()[3])
					.getValue().compareTo(
							p2.getAttribute(
									peakTableViewer.getCmlPeakFields()[3])
									.getValue());
			break;
		}

		return comp;
	}

}
