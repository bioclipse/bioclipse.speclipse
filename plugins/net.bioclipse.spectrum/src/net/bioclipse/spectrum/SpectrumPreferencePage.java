/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stefan Kuhn
 *     
 ******************************************************************************/
package net.bioclipse.spectrum;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jcamp.parser.JCAMPReader;

public class SpectrumPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {


	Combo comboDropDown=null;
	
	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		composite.setLayout(layout);
		Label checkLabel = new Label(composite, SWT.NULL);
		checkLabel.setText("JCAMP-DX reading mode");
	    comboDropDown = new Combo(composite, SWT.DROP_DOWN | SWT.BORDER);
	    comboDropDown.add(JCAMPReader.STRICT);
	    comboDropDown.add(JCAMPReader.RELAXED);
	    comboDropDown.select(
	    		Activator.getDefault().getModePreference().equals(JCAMPReader.STRICT) ? 0 : 1);
		return composite;
	}

	public void init(IWorkbench workbench) {
	}

	private void saveValues(){
		net.bioclipse.spectrum.Activator.getDefault().getPluginPreferences().setValue(Activator.MODE_PREFERENCE,comboDropDown.getSelectionIndex()==0 ? JCAMPReader.STRICT : JCAMPReader.RELAXED);
	}
	
	@Override
	protected void performApply() {
		saveValues();
		super.performApply();
	}

	@Override
	public boolean performOk() {
		saveValues();
		return super.performOk();
	}
	
	@Override
    protected void performDefaults() {
		comboDropDown.select(0);
    }	
}
