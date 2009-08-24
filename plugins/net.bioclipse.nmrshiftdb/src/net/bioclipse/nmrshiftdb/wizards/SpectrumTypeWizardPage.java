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

import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.bioclipse.nmrshiftdb.util.Bc_nmrshiftdbConstants;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.utils.Options;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The spectrumt type wizard page allows choosing a spectrum type for a prediction.
 */

public class SpectrumTypeWizardPage extends WizardPage {
	private String selectedFormat=null;
	private Combo combo=null;
	String nucleus=null;
	boolean assignment=false;
	DisplayWizardPage dwPage;
	Button usecalculated;
  private boolean usecalculatedbool;
	
	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public SpectrumTypeWizardPage() {
		super("SpectrumTypeWizardPage");
		setTitle("Predict from NMRShiftDB wizard");
		setDescription("This wizard predicts a spectrum from NMRShiftDB");
		dwPage=null;
		this.setPageComplete(false);
	}

	public SpectrumTypeWizardPage(String nucleus) {
		this();
		this.nucleus=nucleus;
		assignment=true;
	}
	
	public void initUi() throws Exception{
	    Options opts = new Options(new String[0]);
	    opts.setDefaultURL(((PredictWizard)this.getWizard()).getServerPage().getSelectedServer()+"/services/NMRShiftDB");
	    Service  service = new Service();
	    Call     call    = (Call) service.createCall();
	    call.setOperationName("getSpectrumTypes");
	    call.setTargetEndpointAddress( new URL(opts.getURL()) );
	    DocumentBuilder builder;
	    builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    SOAPBodyElement[] input = new SOAPBodyElement[1];
	    Document doc = builder.newDocument();
	    Element cdataElem;
	    cdataElem = doc.createElementNS(opts.getURL(), "getSpectrumTypes");
	    input[0] = new SOAPBodyElement(cdataElem);
        
	    Vector          elems = (Vector) call.invoke( input );
	    SOAPBodyElement elem  = null ;
	    Element         e     = null ;
	    elem = (SOAPBodyElement) elems.get(0);
	    e    = elem.getAsDOM();
	    String spectrumTypes=e.getFirstChild().getTextContent();
	    
	    StringTokenizer st=new StringTokenizer(spectrumTypes);

	    combo.removeAll();
	    combo.add("   ");
		while(st.hasMoreTokens()){
			combo.add(st.nextToken());
		}
		combo.setText(combo.getItem(0));
		dwPage = (DisplayWizardPage)(this.getWizard()).getNextPage(this);
	}

	
	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		try{
			Composite container = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			container.setLayout(layout);
			layout.numColumns = 1;
			layout.verticalSpacing = 9;
	
			Label label=new Label(container, SWT.NULL);
			label.setText("Choose a spectrum type you want to predict");
			combo = new Combo(container,SWT.DROP_DOWN);
			combo.add("     ");
			
			combo.addSelectionListener(
			 new SelectionAdapter()
			 {
			   public void widgetSelected(SelectionEvent e)
			   {
				   try{
					   	selectedFormat=combo.getText();
					   	if(!combo.getText().equals("   ") && (assignment || dwPage!=null))
					   	{
					   		if(assignment || dwPage.initUi())
					   			SpectrumTypeWizardPage.this.setPageComplete(true);
					   	}
					 }catch(Exception ex){
						ex.printStackTrace();
					 }
			   }
			 }
			);
			
			Label labelCalc=new Label(container, SWT.NULL);
      labelCalc.setText("Should calculated spectra also be used?");
      usecalculated = new Button(container, SWT.CHECK);
      usecalculated.addSelectionListener(
         new SelectionAdapter()
         {
           public void widgetSelected(SelectionEvent e)
           {
             try{
                usecalculatedbool = usecalculated.getSelection();
                if(!combo.getText().equals("   ") && (assignment || dwPage!=null))
                {
                  if(assignment || dwPage.initUi())
                    SpectrumTypeWizardPage.this.setPageComplete(true);
                }
             }catch(Exception ex){
              ex.printStackTrace();
             }
           }
         }
        );
			setControl(container);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}


	public String getSelectedFormat() {
		return selectedFormat;
	}

    public String getCalculated() {
        if(usecalculatedbool)
            return "true";
        else
            return "false";
    }
}