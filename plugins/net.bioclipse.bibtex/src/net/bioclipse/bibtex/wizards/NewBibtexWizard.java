/*******************************************************************************
 * Copyright (c) 2008-2009  Stefan Kuhn <stefan.kuhn@ebi.ac.uk
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.bibtex.wizards;


import java.io.ByteArrayInputStream;
import java.io.InputStream;

import net.bioclipse.chemoinformatics.wizards.WizardHelper;
import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * This is a new wizard for creating bibtex files.
 */

public class NewBibtexWizard extends Wizard implements INewWizard {
	private NewBibtexFileWizardPage filePage;

	private static final Logger logger = Logger.getLogger(NewBibtexWizard.class);
	
	/**
	 * Constructor for NewBibtexWizard.
	 */
	public NewBibtexWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
	    ISelection sel=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
	    if(sel instanceof IStructuredSelection){
	        filePage = new NewBibtexFileWizardPage((IStructuredSelection) sel);
	        filePage.setFileName(WizardHelper.findUnusedFileName((IStructuredSelection)sel, "unnamed", ".bib"));
	    }else{
	        filePage = new NewBibtexFileWizardPage(StructuredSelection.EMPTY);
	    }        
	    addPage(filePage);
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * will create an operation and run it using wizard as execution context.
	 */
	public boolean performFinish() {

		//Create the new SpectrumResource as a child of the folder
		IFile newRes=ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(filePage.getCompleteFileName() ));

		try{
			if(filePage.getYesButton().getSelection()){
				InputStream ins = this.getClass().getClassLoader().getResourceAsStream("/net/bioclipse/bibtex/resources/template.bib");
				newRes.create(ins, false, new NullProgressMonitor());
			}else{
				newRes.create(new ByteArrayInputStream(new String("").getBytes()), false, new NullProgressMonitor());
			}
		}
		catch(Exception ex){
			LogUtils.handleException( ex, logger, net.bioclipse.bibtex.Activator.PLUGIN_ID);
		}
		return true;
	}
	   
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}
}