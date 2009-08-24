/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package net.bioclipse.spectrum.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLConditionList;
import org.xmlcml.cml.element.CMLMetadataList;
import org.xmlcml.cml.element.CMLSubstanceList;

public class JCampContentProvider implements ITreeContentProvider {

	public JCampContentProvider() {
	}

	public void dispose() {

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public Object[] getElements(Object inputElement) {
		ArrayList parentElementsArray = new ArrayList();
		List<CMLElement> mlist = (List<CMLElement>) inputElement;
		Iterator<CMLElement> it = mlist.iterator();
		while (it.hasNext()) {
			CMLElement element = (CMLElement) it.next();
			if (element != null) {
				parentElementsArray.add(element);
			}
		}
		return parentElementsArray.toArray();
	}

	public Object[] getChildren(Object parentElement) {
		ArrayList childElements = new ArrayList();
		CMLElement element = (CMLElement) parentElement;
		return element.getChildCMLElements().toArray();
	}

	public Object getParent(Object element) {
		return ((CMLElement)element).getParent();
	}

	public boolean hasChildren(Object element) {
		if (element instanceof CMLMetadataList
				&& ((CMLMetadataList) element).getMetadataElements().size() != 0) {
			return true;
		} else if (element instanceof CMLConditionList
				&& ((CMLConditionList) element).getScalarElements().size() != 0) {
			return true;
		}else if (element instanceof CMLSubstanceList
				&& ((CMLSubstanceList) element).getSubstanceElements().size() != 0) {
			return true;
		}
		else {
			return false;
		}
	}

}
