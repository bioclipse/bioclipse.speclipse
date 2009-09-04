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

import java.util.List;

import net.bioclipse.core.domain.ISpecmol;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.jobs.BioclipseUIJob;
import net.bioclipse.nmrshiftdb.util.NmrshiftdbUtils;
import net.bioclipse.specmol.Activator;
import net.bioclipse.specmol.domain.IJumboSpecmol;
import net.bioclipse.spectrum.editor.SpectrumEditor;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.xmlcml.cml.element.CMLMolecule;

public class NewFromNmrshiftdbWizard extends Wizard implements INewWizard{
	
	
	private static final Logger logger = Logger.getLogger(NewFromNmrshiftdbWizard.class);
	private NewFromNmrshiftdbWizardPage newFromNmrshiftdbWizardPage;
	protected String searchtext;
	protected String searchmode;
	protected String searchfield;
	private ServerWizardPage serverPage;
	
	public NewFromNmrshiftdbWizard(){
		setWindowTitle("Query NMRShiftDB");
	}
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}
	
	public NewFromNmrshiftdbWizardPage getNewMolPage() {
		return newFromNmrshiftdbWizardPage;
	}
	
	public void addPages()  
	{  
		// create and add first page
		serverPage=new ServerWizardPage("dummy");
		addPage(serverPage);
		newFromNmrshiftdbWizardPage=new NewFromNmrshiftdbWizardPage();
		addPage(newFromNmrshiftdbWizardPage);
	}
	
	
	
	public boolean performFinish() {
		try {
			net.bioclipse.nmrshiftdb.Activator.getDefault().getJavaNmrshiftdbManager().generalSearch(NmrshiftdbUtils.replaceSpaces(searchtext), NmrshiftdbUtils.replaceSpaces(newFromNmrshiftdbWizardPage.getTypemap().get(searchmode)), NmrshiftdbUtils.replaceSpaces(newFromNmrshiftdbWizardPage.getFieldmap().get(searchfield)), serverPage.getSelectedServer(), new BioclipseUIJob<List<ISpecmol>>() {
	            @Override
	            public void runInUI() {
	    			List<ISpecmol> result = getReturnValue();
	    			if(result.size()>0){
		    			//We save the results in a virtual folder
		    			try{
		    	    		IFolder folder=NmrshiftdbUtils.createVirtualFolder();
		    		    	for(int i=0;i<result.size();i++){
		    		    		net.bioclipse.specmol.Activator.getDefault().getJavaSpecmolManager().saveSpecmol((IJumboSpecmol)result.get(i),folder.getFile(((CMLMolecule) ((IJumboSpecmol)result.get(i)).getJumboObject().getChildCMLElement("molecule",0)).getId() + "." + SpectrumEditor.CML_TYPE));
		    		    	}
		        			MessageBox mb = new MessageBox(new Shell(),  SWT.ICON_INFORMATION | SWT.OK);
		        			mb.setMessage("Your result have been saved to "+folder.getFullPath().toOSString());
		        			mb.setText("NMRShiftDB search results saved");
		        			mb.open();
		    			} catch (Exception e) {
		    				LogUtils.handleException(e, logger, Activator.PLUGIN_ID);
		    			}
	    			}else{
	        			MessageBox mb = new MessageBox(new Shell(),  SWT.ICON_INFORMATION | SWT.OK);
	        			mb.setMessage("Querying NMRShiftDB for "+searchtext+"/"+searchmode+"/"+searchfield+" found no entries!");
	        			mb.setText("Empty result");
	        			mb.open();
	    			}
	            }
			});
		} catch (Exception e) {
			LogUtils.handleException(e, logger, Activator.PLUGIN_ID);
		}
		return true;
	}
	
	
	public boolean canFinish(){
		if(!newFromNmrshiftdbWizardPage.getSearchstring().equals(""))
			return true;
		else
			return false;
	}

}
