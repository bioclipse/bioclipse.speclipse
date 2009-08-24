/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package net.bioclipse.spectrum.wizards;

import java.util.Arrays;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableItem;
import org.xmlcml.cml.element.CMLPeak;

class PeakCellModifier implements ICellModifier {

	private String[] columnNames;

	private TableViewer tableViewer;

	public PeakCellModifier(String[] columnNames, TableViewer tableViewer) {
		this.columnNames = columnNames;
		this.tableViewer = tableViewer;
	}

	public boolean canModify(Object element, String property) {
		return true;
	}

	public Object getValue(Object element, String property) {
		int columnIndex = Arrays.asList(columnNames).indexOf(property);
		//
		Object result = null;
		CMLPeak peak = (CMLPeak) element;

		switch (columnIndex) {
		case 0: // X_AXIS
			result = new String("" + peak.getXValue());
			break;
		case 1: // Y_AXIS COLUMN
			result = new String("" + peak.getYValue());
			break;
		default:
			result = "";
		}
		return result;
	}

	public void modify(Object element, String property, Object value) {
		int columnIndex = Arrays.asList(columnNames).indexOf(property);

		TableItem item = (TableItem) element;
		CMLPeak peak = (CMLPeak) item.getData();

		switch (columnIndex) {
		case 0: // X_AXIS
			peak.setXValue(new Double((String) value).doubleValue());
			break;
		case 1: // Y_AXIS COLUMN
			peak.setYValue(new Double((String) value).doubleValue());
			break;
		default:
		}
		tableViewer.refresh();
	}
}