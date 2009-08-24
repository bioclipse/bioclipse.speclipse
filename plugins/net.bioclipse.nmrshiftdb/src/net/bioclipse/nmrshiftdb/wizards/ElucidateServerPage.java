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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ElucidateServerPage  extends ServerWizardPage {

	Button[] radios;
	
	public ElucidateServerPage() {
	}


	public ElucidateServerPage(String nucleus) {
		
		super(nucleus);
	}
	
	protected void addAdditionalControl (Composite container)
	{
		Label label=new Label(container, SWT.NULL);
		label.setText("\n\rPlease choose the type of search you would like to use:");
		final String[] options = new String[] { "Complete", "Subspectrum" };
		radios = new Button[options.length];
		
		    for (int i = 0; i < options.length; i++) {
		      radios[i] = new Button(container, SWT.RADIO);
		      if(i==0)
		    	  radios[i].setSelection(true);
		      radios[i].setText(options[i]);
		    }
	}
	
	public String selectedOption() //signifies type of search
	{
		if(radios[0].getSelection())
			return "whole";
		else
			return "sub";
	}
}