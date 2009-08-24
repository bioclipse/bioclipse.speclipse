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
package net.bioclipse.nmrshiftdb;

import net.bioclipse.nmrshiftdb.util.Bc_nmrshiftdbConstants;
import net.bioclipse.nmrshiftdb.wizards.ServerWizardPage;
import net.bioclipse.nmrshiftdb.wizards.SubmitWizard;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class NmrshiftdbPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {


	private Text usernameValue;
	private Text passwordValue;
	private Text serverValue;
	private Button checkServer;
	private Button check;

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		composite.setLayout(layout);
		Label checkLabel = new Label(composite, SWT.NULL);
		checkLabel.setText("Save username/password?");
		check = new Button(composite, SWT.CHECK);
		if (net.bioclipse.nmrshiftdb.Activator.getDefault().getPluginPreferences().getBoolean(SubmitWizard.REMEMBER_NMRSHIFTDB_VALUE)) {
			check.setSelection(true);
		}
		check.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button) e.getSource();
				if (!button.getSelection()) {
					if (usernameValue != null) {
						usernameValue.setEnabled(false);
						passwordValue.setEnabled(false);
					}
				}
				else {
					usernameValue.setEnabled(true);
					passwordValue.setEnabled(true);
				}

			}
		});
		Label setLabel = new Label(composite, SWT.NULL);
		setLabel.setText("Username: ");
		usernameValue = new Text(composite, SWT.BORDER | SWT.SINGLE);
		usernameValue.setText(net.bioclipse.nmrshiftdb.Activator.getDefault().getPluginPreferences().getString(SubmitWizard.REMEMBER_NMRSHIFTDB_USER));
		if (!check.getSelection()) {
			usernameValue.setEnabled(false);
		}
		else {
			usernameValue.setEnabled(true);
		}
		Label passwordLabel = new Label(composite, SWT.NULL);
		passwordLabel.setText("Password: ");
		passwordValue = new Text(composite, SWT.BORDER | SWT.SINGLE);
		passwordValue.setText(net.bioclipse.nmrshiftdb.Activator.getDefault().getPluginPreferences().getString(SubmitWizard.REMEMBER_NMRSHIFTDB_PW));
		if (!check.getSelection()) {
			passwordValue.setEnabled(false);
		}
		else {
			passwordValue.setEnabled(true);
		}
		Label checkServerLabel = new Label(composite, SWT.NULL);
		checkServerLabel.setText("Save servername?");
		checkServer = new Button(composite, SWT.CHECK);
		if (net.bioclipse.nmrshiftdb.Activator.getDefault().getPluginPreferences().getBoolean(ServerWizardPage.SAVE_SERVER)) {
			checkServer.setSelection(true);
		}
		checkServer.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button) e.getSource();
				if (!button.getSelection()) {
					if (serverValue != null) {
						serverValue.setEnabled(false);
					}
				}
				else {
					serverValue.setEnabled(true);
				}

			}
		});
		Label serverLabel = new Label(composite, SWT.NULL);
		serverLabel.setText("Server: ");
		serverValue = new Text(composite, SWT.BORDER | SWT.SINGLE);
		serverValue.setText(net.bioclipse.nmrshiftdb.Activator.getDefault().getPluginPreferences().getString(ServerWizardPage.NMRSHIFTDB_SERVER));
		if (!checkServer.getSelection()) {
			serverValue.setEnabled(false);
		}
		else {
			serverValue.setEnabled(true);
		}
		return composite;
	}

	public void init(IWorkbench workbench) {
	}

	private void saveValues(){
		net.bioclipse.nmrshiftdb.Activator.getDefault().getPluginPreferences().setValue(ServerWizardPage.SAVE_SERVER,checkServer.getSelection());
		net.bioclipse.nmrshiftdb.Activator.getDefault().getPluginPreferences().setValue(ServerWizardPage.NMRSHIFTDB_SERVER,serverValue.getText());
		net.bioclipse.nmrshiftdb.Activator.getDefault().getPluginPreferences().setValue(SubmitWizard.REMEMBER_NMRSHIFTDB_VALUE,check.getSelection());
		net.bioclipse.nmrshiftdb.Activator.getDefault().getPluginPreferences().setValue(SubmitWizard.REMEMBER_NMRSHIFTDB_USER,usernameValue.getText());
		net.bioclipse.nmrshiftdb.Activator.getDefault().getPluginPreferences().setValue(SubmitWizard.REMEMBER_NMRSHIFTDB_PW,passwordValue.getText());
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
		check.setSelection(false);
		checkServer.setSelection(false);
		serverValue.setText(Bc_nmrshiftdbConstants.server);
		serverValue.setEnabled(false);
		usernameValue.setText("");
		usernameValue.setEnabled(false);
		passwordValue.setText("");
		passwordValue.setEnabled(false);
    }	
}
