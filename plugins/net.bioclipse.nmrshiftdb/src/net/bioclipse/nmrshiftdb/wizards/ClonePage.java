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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class ClonePage extends org.eclipse.jface.wizard.WizardPage {

	private String name="";
	public Text text=null;

	public ClonePage(String name){
		super("Clone a submit entry");
		setTitle("Clone a submit entry");
		setDescription("Choose a file name for the new entry");
		this.name=name;
	}
	
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
		text=new Text(container, SWT.NULL);
		text.setText(name+".new");
		GridData gridData3 = new GridData();
		gridData3.widthHint=200;
		text.setLayoutData(gridData3);
		setControl(container);
	}

}
