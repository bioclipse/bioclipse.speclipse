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
package net.bioclipse.nmrshiftdb.wizards;

import net.bioclipse.core.util.LogUtils;
import net.bioclipse.nmrshiftdb.Activator;
import net.bioclipse.nmrshiftdb.util.Bc_nmrshiftdbConstants;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * The server wizard page allows choosing a server for executing the nmrshiftdb web services.
 */

public class ServerWizardPage extends WizardPage {
	protected String nucleus;
	Combo combo2 = null;
	public static final String NMRSHIFTDB_SERVER="nmrshiftdb_server";
	public static final String SAVE_SERVER="save_server";
	private Button selection;
	private static Logger logger = Logger.getLogger(ServerWizardPage.class);
	
	/**
	 * Constructor for ServerWizardPage.
	 * The next page will ask for a nucleus if using this constructor.
	 * 
	 * @param pageName
	 */
	public ServerWizardPage() {
		super("ServerWizardPage");
		setTitle("Choose an NMRShiftDB server");
		setDescription("Here you can choose an NMRShiftDB server to use");
	}


	/**
	 * Constructor for ServerWizardPage.
	 * There will be no next page.
	 * 
	 * @param pageName
	 */
	public ServerWizardPage(String nucleus) {
		this();
		this.nucleus=nucleus;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
		layout.verticalSpacing = 9;

		Label label=new Label(container, SWT.NULL);
		label.setText("Choose a server to use (internet connection is needed to use this service)");
		combo2=new Combo(container,SWT.DROP_DOWN);
		combo2.add(getNmrshiftdbServerPreference());
		combo2.setText(combo2.getItem(0));
		combo2.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
		    	  if(ServerWizardPage.this.selection!=null){
		    		  if(ServerWizardPage.this.selection.getSelection()){
		    			  ServerWizardPage.setNmrshiftdbServerPreference(combo2.getText());
		    			  ServerWizardPage.setSaveServerPreference(true);
		    		  }else{
		    			  ServerWizardPage.setNmrshiftdbServerPreference(Bc_nmrshiftdbConstants.server);
		    			  ServerWizardPage.setSaveServerPreference(false);
		    		  }
		    	  }
			}
		});
		combo2.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		    	  if(ServerWizardPage.this.selection!=null){
		    		  if(ServerWizardPage.this.selection.getSelection()){
		    			  ServerWizardPage.setNmrshiftdbServerPreference(combo2.getText());
		    			  ServerWizardPage.setSaveServerPreference(true);
		    		  }else{
		    			  ServerWizardPage.setNmrshiftdbServerPreference(Bc_nmrshiftdbConstants.server);
		    			  ServerWizardPage.setSaveServerPreference(false);
		    		  }
		    	  }
		      }
		});
		Label label6=new Label(container,SWT.NULL);
		label6.setText("Check this box if you want to remember the URL entered");
		selection = new Button(container, SWT.CHECK);
		selection.setSelection(getSaveServerPreference());
		selection.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}
			public void widgetSelected(SelectionEvent e) {
		    	  if(ServerWizardPage.this.selection!=null){
		    		  if(ServerWizardPage.this.selection.getSelection()){
		    			  ServerWizardPage.setNmrshiftdbServerPreference(combo2.getText());
		    			  ServerWizardPage.setSaveServerPreference(true);
		    		  }else{
		    			  ServerWizardPage.setNmrshiftdbServerPreference(Bc_nmrshiftdbConstants.server);
		    			  ServerWizardPage.setSaveServerPreference(false);
		    		  }
		    	  }
			}
		});
		addAdditionalControl(container);
		setControl(container);
	}

	protected void addAdditionalControl (Composite container)
	{
		
	}
	public String getSelectedServer() {
		return combo2.getText();
	}
	
	public IWizardPage getNextPage(){
		if(nucleus!=null){
			if(nucleus.equals("dummy")){
				return ((NewFromNmrshiftdbWizard)this.getWizard()).getNewMolPage();
			}else{
				return null;
			}
		}
		SpectrumTypeWizardPage page = ((PredictWizard)this.getWizard()).getTypePage();
		try{
			page.initUi();
		}catch(Exception ex){
			LogUtils.handleException(ex, logger, Activator.ID);
		}
		return page;
	}
	
	public static String getNmrshiftdbServerPreference() {
		if(net.bioclipse.nmrshiftdb.Activator.getDefault().getPluginPreferences().getString(NMRSHIFTDB_SERVER).equals(""))
			return Bc_nmrshiftdbConstants.server;
		else
			return net.bioclipse.nmrshiftdb.Activator.getDefault().getPluginPreferences().getString(NMRSHIFTDB_SERVER);
	}
	
	public static void setNmrshiftdbServerPreference(String value) {
		net.bioclipse.nmrshiftdb.Activator.getDefault().getPluginPreferences().setValue(NMRSHIFTDB_SERVER, value);
	}
	
	public static boolean getSaveServerPreference() {
		return net.bioclipse.nmrshiftdb.Activator.getDefault().getPluginPreferences().getBoolean(SAVE_SERVER);
	}
	
	public static void setSaveServerPreference(boolean value) {
		net.bioclipse.nmrshiftdb.Activator.getDefault().getPluginPreferences().setValue(SAVE_SERVER, value);
	}

}