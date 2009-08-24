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

import java.util.HashMap;

import net.bioclipse.nmrshiftdb.util.Bc_nmrshiftdbConstants;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewFromNmrshiftdbWizardPage extends WizardPage {

	private Text txtName;
	
	private Combo searchfields;
	private Combo searchtype;
	private HashMap<String,String> typemap=new HashMap<String,String>();
	private HashMap<String,String> fieldmap=new HashMap<String,String>();
	
	public NewFromNmrshiftdbWizardPage() {
		super("Query NMRShiftDB");
		setTitle("Query NMRShiftDB");
		setDescription("Give a search term and choose search type!");
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 5;
		layout.verticalSpacing = 9;
		container.setLayout(layout);


		
		
		final Label lblSmiles = new Label(container, SWT.NONE);
		lblSmiles.setBounds(25, 40, 210, 25);
		lblSmiles.setText("Search expression:");
		
		txtName=new Text(container, SWT.BORDER);
		txtName.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					checkForCompletion();
				}
			});
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.widthHint=200;
		txtName.setLayoutData(gridData);
		
		
		typemap.put("Exact",Bc_nmrshiftdbConstants.EXACT);
		typemap.put("Fragment",Bc_nmrshiftdbConstants.FRAGMENT);
		typemap.put("Regular Expression",Bc_nmrshiftdbConstants.REGEXP);
		typemap.put("Fuzzy",Bc_nmrshiftdbConstants.FUZZY);
		searchtype = new Combo (container, SWT.READ_ONLY);
		searchtype.setItems ((String[])typemap.keySet().toArray(new String[0]));
		searchtype.setText("Exact");

		fieldmap.put("Chemical Name (with Pubchem name resultion)", Bc_nmrshiftdbConstants.CHEMNAMEPUBCHEM);
		fieldmap.put("Chemical Name", Bc_nmrshiftdbConstants.CHEMNAME);
		fieldmap.put("Literature/Author", Bc_nmrshiftdbConstants.LITERATURE_AUTHOR);
		fieldmap.put("CAS number", Bc_nmrshiftdbConstants.CASNUMBER);
		fieldmap.put("Chemical Formula", Bc_nmrshiftdbConstants.FORMULA);
		fieldmap.put("Chemical Formula (with other elements)", Bc_nmrshiftdbConstants.FORMULA_WITH_OTHER);
		fieldmap.put("Literature/Title", Bc_nmrshiftdbConstants.LITERATURE_TITLE);
		fieldmap.put("Comment", Bc_nmrshiftdbConstants.COMMENT);
		fieldmap.put("Canonical Name", Bc_nmrshiftdbConstants.CANNAME);
		fieldmap.put("Molecule Hyperlink Description", Bc_nmrshiftdbConstants.MOLLINK);
		fieldmap.put("Spectrum Hyperlink Description", Bc_nmrshiftdbConstants.SPECLINK);
		fieldmap.put("Molecule Keyword", Bc_nmrshiftdbConstants.MOLKEY);
		fieldmap.put("Spectrum Keyword", Bc_nmrshiftdbConstants.SPECKEY);
		fieldmap.put("Multiplicities", Bc_nmrshiftdbConstants.MULTIPLICITY);
		fieldmap.put("Potential C13-Multiplicities", Bc_nmrshiftdbConstants.POTMULTIPLICITY);
		fieldmap.put("Spectrum NMRShiftDB-Number", Bc_nmrshiftdbConstants.SPECTRUM_NR);
		fieldmap.put("Molecule NMRShiftDB-Number", Bc_nmrshiftdbConstants.MOLECULE_NR);
		fieldmap.put("HOSE code", Bc_nmrshiftdbConstants.HOSECODE);
		fieldmap.put("double bond equivalents/smallest set of smallest rings", Bc_nmrshiftdbConstants.DBE_RINGS);
		fieldmap.put("Molecular weight (format: from-to)", Bc_nmrshiftdbConstants.WEIGHT);
		fieldmap.put("Substance Comment", Bc_nmrshiftdbConstants.COMMENT);
			
		searchfields = new Combo (container, SWT.READ_ONLY);
		searchfields.setItems ((String[])fieldmap.keySet().toArray(new String[0]));
		searchfields.setText("Chemical Name (with Pubchem name resultion)");
		
		setControl(container);
		checkForCompletion();

	}
	
	public static Object createObject(String className)	throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		return Class.forName(className).newInstance();	
	}

	/**
	 * If page not complete, set error messages
	 */
	protected void checkForCompletion() {
		
		if (txtName.getText() == null || txtName.getText().compareTo("") == 0){
			this.setErrorMessage("You need to enter a search term!");
		} else {
			setErrorMessage(null);
			((NewFromNmrshiftdbWizard)getWizard()).searchtext = txtName.getText();
			((NewFromNmrshiftdbWizard)getWizard()).searchfield=searchfields.getText();
			((NewFromNmrshiftdbWizard)getWizard()).searchmode=searchtype.getText();
			this.setPageComplete(true);
		}
		getWizard().getContainer().updateButtons();
	}

	public String getSearchstring() {
		return txtName.getText();
	}
	
	public String getSearchtype(){
		return searchtype.getText();
	}
	
	public String getSearchfield(){
		return searchfields.getText();
	}

	public HashMap<String, String> getTypemap() {
		return typemap;
	}

	public HashMap<String, String> getFieldmap() {
		return fieldmap;
	}
	
}
