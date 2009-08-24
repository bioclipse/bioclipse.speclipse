/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package net.bioclipse.spectrum.wizards;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.xmlcml.cml.element.CMLPeakList;

class PeakTableTabListener implements Listener {
	private int myColumn = 0;

	private TableViewer tableViewer;

	private Table table;

	public PeakTableTabListener(int myColumn, TableViewer tableViewer) {
		this.myColumn = myColumn;
		this.tableViewer = tableViewer;
		this.table = tableViewer.getTable();
	}

	public void handleEvent(Event event) {
		if (event.detail == SWT.TRAVERSE_TAB_PREVIOUS
				|| event.detail == SWT.TRAVERSE_ARROW_PREVIOUS) {
			tableViewer.editElement(tableViewer.getElementAt(table
					.getSelectionIndex()), myColumn - 1);
		}
		if (event.detail == SWT.TRAVERSE_TAB_NEXT
				|| event.detail == SWT.TRAVERSE_ARROW_NEXT) {
			if (1 == myColumn
					&& table.getSelectionIndex() < ((CMLPeakList) tableViewer
							.getInput()).getChildCount() - 1)
				tableViewer.editElement(tableViewer.getElementAt(table
						.getSelectionIndex() + 1), 0);
			if (1 > myColumn)
				tableViewer.editElement(tableViewer.getElementAt(table
						.getSelectionIndex()), myColumn + 1);
		}
	}

}