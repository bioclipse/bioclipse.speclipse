/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package net.bioclipse.spectrum.editor;

import net.bioclipse.spectrum.editor.PeakTableViewer.PeakContentProvider;
import nu.xom.Attribute;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.xmlcml.cml.element.CMLPeak;

public class PeakCellModifier implements ICellModifier {
	public PeakTableViewer peakTableViewer;

	public PeakCellModifier(PeakTableViewer peakTableViewer) {
		super();
		this.peakTableViewer = peakTableViewer;
	}

	public boolean canModify(Object element, String property) {
		if (property.equals(PeakTableViewer.INVALID_COLUMN))
			return false;
		else
			return true;
	}

	public Object getValue(Object element, String property) {

		// Find the index of the column
		int columnIndex = peakTableViewer.getColumnNames().indexOf(property);
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
		case 2: // addiditonal columns
			if (peak.getAttribute(peakTableViewer.getCmlPeakFields()[0]) != null) {
				result = peak.getAttribute(
						peakTableViewer.getCmlPeakFields()[0]).getValue();
				break;
			}
		case 3: // Additional_FIelds COLUMN
			if (peak.getAttribute(peakTableViewer.getCmlPeakFields()[1]) != null) {
				result = peak.getAttribute(
						peakTableViewer.getCmlPeakFields()[1]).getValue();
				break;
			}
		case 4: // Additional_FIelds COLUMN
			if (peak.getAttribute(peakTableViewer.getCmlPeakFields()[2]) != null) {
				result = peak.getAttribute(
						peakTableViewer.getCmlPeakFields()[2]).getValue();
				break;
			}
		case 5: // Additional_FIelds COLUMN
			if (peak.getAttribute(peakTableViewer.getCmlPeakFields()[3]) != null) {
				result = peak.getAttribute(
						peakTableViewer.getCmlPeakFields()[3]).getValue();
				break;
			}
		default:
			result = "";
		}
		return result;
	}

	public void modify(Object element, String property, Object value) {

		// Find the index of the column
		int columnIndex = peakTableViewer.getColumnNames().indexOf(property);

		TableItem item = (TableItem) element;
		CMLPeak peak = (CMLPeak) item.getData();

		switch (columnIndex) {
		case 0: // X_AXIS
			try{
				peak.setXValue(new Double((String) value).doubleValue());
			}catch(NumberFormatException ex){
				 MessageDialog.openError(new Shell(), "No valid float figure", "Your input could not be parsed as a float figure. Only these are possible here!");
			}
			for(int i=0;i<peakTableViewer.hashesofpeaks.length;i++){
				if(peakTableViewer.hashesofpeaks[i]==peak.hashCode())
					peakTableViewer.spectrumItem.getPeakListElements().get(0).getPeakElements().get(i).setXValue(new Double((String) value).doubleValue());
					break;
			}
			break;
		case 1: // Y_AXIS COLUMN
			try{
				peak.setYValue(new Double((String) value).doubleValue());
			}catch(NumberFormatException ex){
				 MessageDialog.openError(new Shell(), "No valid float figure", "Your input could not be parsed as a float figure. Only these are possible here!");
			}
			for(int i=0;i<peakTableViewer.hashesofpeaks.length;i++){
				if(peakTableViewer.hashesofpeaks[i]==peak.hashCode())
					peakTableViewer.spectrumItem.getPeakListElements().get(0).getPeakElements().get(i).setYValue(new Double((String) value).doubleValue());
					break;
			}
			break;
		case 2: // Additional column
			setAttribute(value, peak, 0);
			break;
		case 3: // Additional column
			setAttribute(value, peak, 1);
			break;
		case 4: // Additional column
			setAttribute(value, peak, 2);
			break;
		case 5: // Additional column
			setAttribute(value, peak, 3);
			break;
		default:
		}
		peakTableViewer.peakTablePage.setDirty(true);
		peakTableViewer.tableViewer.refresh(true);
	}

	private void setAttribute(Object value, CMLPeak peak, int position) {
		String valueString = ((String) value).trim();
		if (value.equals("")){
			if(peak.getAttribute(peakTableViewer.getCmlPeakFields()[position]) != null){
				peak.removeAttribute(peakTableViewer.getCmlPeakFields()[position]);
				for(int i=0;i<peakTableViewer.hashesofpeaks.length;i++){
					if(peakTableViewer.hashesofpeaks[i]==peak.hashCode())
						peakTableViewer.spectrumItem.getPeakListElements().get(0).getPeakElements().get(i).removeAttribute(peakTableViewer.getCmlPeakFields()[position]);
				}
			}
			return;
		}
		if (peak.getAttribute(peakTableViewer.getCmlPeakFields()[position]) == null
				&& !peakTableViewer.getCmlPeakFields()[position].equals("")){
			peak.addAttribute(new Attribute(
					peakTableViewer.getCmlPeakFields()[position], valueString));
			for(int i=0;i<peakTableViewer.hashesofpeaks.length;i++){
				if(peakTableViewer.hashesofpeaks[i]==peak.hashCode())
					peakTableViewer.spectrumItem.getPeakListElements().get(0).getPeakElements().get(i).addAttribute(new Attribute(
							peakTableViewer.getCmlPeakFields()[position], valueString));
					break;
			}
		}
		if (peak.getAttribute(peakTableViewer.getCmlPeakFields()[position]) != null){
			peak.getAttribute(peakTableViewer.getCmlPeakFields()[position])
					.setValue(valueString);
			for(int i=0;i<peakTableViewer.hashesofpeaks.length;i++){
				if(peakTableViewer.hashesofpeaks[i]==peak.hashCode())
					peakTableViewer.spectrumItem.getPeakListElements().get(0).getPeakElements().get(i).getAttribute(peakTableViewer.getCmlPeakFields()[position]).setValue(valueString);
					break;
			}
		}
	}

}
