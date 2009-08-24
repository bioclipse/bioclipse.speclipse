/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package net.bioclipse.spectrum.editor;

import java.text.DecimalFormat;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.xmlcml.cml.element.CMLPeak;

public class PeakLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	private PeakTableViewer peakTableViewer;

	public PeakLabelProvider(PeakTableViewer ptv) {
		peakTableViewer = ptv;
	}

	public String getColumnText(Object element, int columnIndex) {
		String result = "";
		CMLPeak peak = (CMLPeak) element;
		DecimalFormat df = new DecimalFormat("#.00");
		switch (columnIndex) {
		case 0:
			result = df.format(peak.getXValue());
			// result=new String(peak.getXValue()+"");
			break;
		case 1:
			if(Double.isNaN(peak.getYValue()))
				result=new String("0");
			else
				result = df.format(peak.getYValue());
			// result = new String(peak.getYValue()+"");
			break;
		case 2: // addiditonal columns
			if (peak.getAttribute(peakTableViewer.getCmlPeakFields()[0]) != null)
				result = peak.getAttribute(
						peakTableViewer.getCmlPeakFields()[0]).getValue();
			break;
		case 3: // Additional_FIelds COLUMN
			if (peak.getAttribute(peakTableViewer.getCmlPeakFields()[1]) != null)
				result = peak.getAttribute(
						peakTableViewer.getCmlPeakFields()[1]).getValue();
			break;
		case 4: // Additional_FIelds COLUMN
			if (peak.getAttribute(peakTableViewer.getCmlPeakFields()[2]) != null)
				result = peak.getAttribute(
						peakTableViewer.getCmlPeakFields()[2]).getValue();
			break;
		case 5: // Additional_FIelds COLUMN
			if (peak.getAttribute(peakTableViewer.getCmlPeakFields()[3]) != null)
				result = peak.getAttribute(
						peakTableViewer.getCmlPeakFields()[3]).getValue();
			break;
		default:
			break;
		}
		return result;
	}

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

}
