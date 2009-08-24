/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package net.bioclipse.spectrum.wizards;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;

class PeakContentProvider implements IStructuredContentProvider {

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	public void dispose() {
	}

	// Return the tasks as an array of Objects
	public Object[] getElements(Object parent) {
		CMLPeakList cpl = (CMLPeakList) parent;
		CMLPeak[] array = new CMLPeak[cpl.getDescendants("peak", null, true)
				.size()];
		for (int i = 0; i < array.length; i++) {
			array[i] = (CMLPeak) cpl.getDescendants("peak", null, true).get(i);
		}
		return array;
	}
}