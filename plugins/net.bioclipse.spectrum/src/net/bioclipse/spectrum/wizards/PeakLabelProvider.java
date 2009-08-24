/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package net.bioclipse.spectrum.wizards;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.xmlcml.cml.element.CMLPeak;

class PeakLabelProvider extends LabelProvider implements ITableLabelProvider {

	public PeakLabelProvider() {
	}

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		String result = "";
		CMLPeak peak = (CMLPeak) element;
		switch (columnIndex) {
		case 0:
			result = new String(peak.getXValue() + "");
			break;
		case 1:
			result = new String(peak.getYValue() + "");
			break;
		default:
			break;
		}
		return result;
	}

}