/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package net.bioclipse.spectrum.editor;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLConditionList;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMetadataList;
import org.xmlcml.cml.element.CMLScalar;
import org.xmlcml.cml.element.CMLSubstance;
import org.xmlcml.cml.element.CMLSubstanceList;

public class JCampLabelProvider implements ITableLabelProvider {

	public JCampLabelProvider() {
	}

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof CMLMetadataList) {
			if (columnIndex == 0) {
				String title = "Metadata List";
				return title;
			} else {
				return null;
			}
		} 
		else if (element instanceof CMLConditionList) {
			if (columnIndex == 0) {
				String title = "Condition List";
				return title;
			} else {
				return null;
			}
		}
		else if (element instanceof CMLSubstanceList) {
			if (columnIndex == 0) {
				String title = "Substance List";
				return title;
			} else {
				return null;
			}
		}
		else if (element instanceof CMLMetadata) {
			if (columnIndex == 0) {
				CMLMetadata metadata = (CMLMetadata) element;
				if (metadata.getTitle() != null
						&& metadata.getTitle().length() > 0) {
					return (metadata.getTitle());
				} else if (metadata.getName() != null) {
					String name = metadata.getName();
					int a = name.indexOf(":");
					return name.substring(a + 1, name.length());
				}
				//if no name or title set to unknown
				else {
					return "unknown";
				}
			}
			else if (columnIndex == 1) {
				return ((CMLMetadata) element).getContent();
			} else {
				return null;
			}
		} 
		else if (element instanceof CMLScalar) {
			if (columnIndex == 0) {
				CMLScalar scalar = (CMLScalar) element;
				if (scalar.getTitle() != null
						&& scalar.getTitle().length() > 0) {
					return scalar.getTitle();
				} else {
					String name = scalar.getId();
					if (name == null) {
						name = scalar.getDictRef();
					}
					int a = name.indexOf(":");
					return name.substring(a + 1, name.length());
				}
			}
			else if (columnIndex == 1) {
				if (element instanceof CMLScalar) {
					return ((CMLScalar)element).getValue();
				}
				else {
					return null;
				}
			} 
			else {
				return null;
			}
		}
		else if (element instanceof CMLSubstance) {
			if (columnIndex == 0) {
				CMLSubstance substance = (CMLSubstance) element;
				if (substance.getTitle() != null
						&& substance.getTitle().length() > 0) {
					return substance.getTitle();
				} else {
					String name = substance.getId();
					if (name == null) {
						name = substance.getDictRef();
					}
					int a = name.indexOf(":");
					return name.substring(a + 1, name.length());
				}
			}
			else if (columnIndex == 1) {
				if (element instanceof CMLSubstance) {
					return ((CMLSubstance)element).getValue();
				}
				else {
					return null;
				}
			} 
			else {
				return null;
			}
		}
		else {
			System.out.println("should not happen: " + ((CMLElement)element).toXML());
			return null;
		}
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}
}
