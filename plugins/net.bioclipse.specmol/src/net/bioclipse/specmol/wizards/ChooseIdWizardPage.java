/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.specmol.wizards;

import java.util.HashMap;
import java.util.Iterator;

import net.bioclipse.bibtex.Activator;
import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;



public class ChooseIdWizardPage extends WizardPage{
	List list1;
	HashMap<Integer, BibtexEntry> map=new HashMap<Integer, BibtexEntry>();
	Button specbutton=null;
	Button molbutton=null;
	Button cmlbutton=null;
	Composite container=null;

	protected ChooseIdWizardPage() {
		super("Choose an entry");
		setTitle("Assgin Bibtex entries wizard");
		setDescription("This wizard lets you choose an entry in a bibtex file");
		this.setPageComplete(false);
	}
	
	public void initUi(){
		BibtexDatabase db;
		try {
			db = Activator.getDefault().getJavaBibtexManager().loadBibliodata(((AssignBibtexWizard)this.getWizard()).getBibtexPage().getSelectedRes()).getJabrefDatabase();
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		}
		
		Iterator it=db.getEntries().iterator();
		int i=0;
		while(it.hasNext()){
			BibtexEntry entry=(BibtexEntry)it.next();
			if(!map.containsValue(entry)){
				list1.add(entry.getCiteKey());
				map.put(new Integer(i), entry);
				i++;
			}
		}
		for(int k=0;k<10;k++){
			if(list1.getItem(0).equals(" ") || list1.getItem(0).equals("                               "))
				list1.remove(0);
		}
		list1.addSelectionListener(
			   new SelectionAdapter()
				   {
				     public void widgetSelected(SelectionEvent e)
				     {
				    	 ChooseIdWizardPage.this.setPageComplete(true);
				    	 ChooseIdWizardPage.this.setErrorMessage(null);
				    	 getWizard().getContainer().updateButtons();
				     }
				   });

		list1.redraw();		
	}

	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
		layout.verticalSpacing = 9;
		list1 = new List(container, SWT.SCROLL_PAGE | SWT.SCROLL_LINE | SWT.V_SCROLL | SWT.H_SCROLL);
		//This is needed to give a certain size to the list - bad, but i found no other way
		list1.add("                               ");
		list1.add(" ");
		list1.add(" ");
		list1.add(" ");
		list1.add(" ");
		list1.add(" ");
		list1.add(" ");
		list1.add(" ");
		list1.add(" ");
		list1.add(" ");
		list1.setSize(100,200);
		Label labeltemplate = new Label(container, SWT.NULL);
	    labeltemplate.setText("Do you want to assign this entry to ");
	    specbutton = new Button(container, SWT.RADIO);
	    specbutton.setText("the current spectrum or ");
	    molbutton = new Button(container, SWT.RADIO);
	    molbutton.setText("the current molecule or");
	    cmlbutton = new Button(container, SWT.RADIO);
	    cmlbutton.setText("the whole cml file?");
	    specbutton.setSelection(true);		
		setControl(container);
	}
	
	public Button getMolbutton() {
		return molbutton;
	}

	public Button getSpecbutton() {
		return specbutton;
	}
	


}
