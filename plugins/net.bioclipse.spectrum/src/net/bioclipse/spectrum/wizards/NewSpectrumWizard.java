/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package net.bioclipse.spectrum.wizards;

import java.io.File;
import java.util.Iterator;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.spectrum.business.ISpectrumManager;
import net.bioclipse.spectrum.domain.JumboSpectrum;
import net.bioclipse.ui.business.Activator;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.xmlcml.cml.base.CMLElements;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;
import org.xmlcml.cml.element.CMLSpectrum;

import spok.GenerateId;

public class NewSpectrumWizard extends Wizard implements INewWizard {
	private NewSpectrumFileWizardPage filePage;

	private NewSpectrumDetailWizardPage specPage;
	
	private static final Logger logger = Logger.getLogger(NewSpectrumWizard.class);

	/**
	 * Constructor for JCPWizard.
	 */
	public NewSpectrumWizard() {
		super();
		setWindowTitle("Create a New Spectrum");
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the pages to the wizard.
	 */

	public void addPages() {
    ISelection selection=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
    if (selection != null && selection instanceof IStructuredSelection) {
        Object object = ((IStructuredSelection)selection).getFirstElement();
        if(object !=null && object instanceof IContainer){
            filePage = new NewSpectrumFileWizardPage("New Spectrum File Wizard","This wizard creates a new spectrum file", (IContainer)object);
        }else if(object!=null && object instanceof IResource){
            filePage = new NewSpectrumFileWizardPage("New Spectrum File Wizard","This wizard creates a new spectrum file", ((IResource)object).getParent());
        }else{
            filePage = new NewSpectrumFileWizardPage("New Spectrum File Wizard","This wizard creates a new spectrum file", null);
        }
    }else{
        filePage = new NewSpectrumFileWizardPage("New Spectrum File Wizard","This wizard creates a new spectrum file", null);
    }
		specPage = new NewSpectrumDetailWizardPage();
		addPage(filePage);
		addPage(specPage);
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * will create an operation and run it using wizard as execution context.
	 */
	public boolean performFinish() {

		//Get folder to install in from wizard page
		IResource parentFolder = filePage.getSelectedFolder();

		CMLSpectrum spec= createNewCMLSpectrum();
		try {
			createNewSpectrum(filePage.getFileName(), filePage.getExtension(),parentFolder, spec);
		} catch (Exception e) {
		    LogUtils.handleException( e, logger, net.bioclipse.spectrum.Activator.PLUGIN_ID);
		}
		return true;
	}

	public static void createNewSpectrum(String filename, String extension, IResource parentFolder, CMLSpectrum spec) throws BioclipseException, CoreException{
		ISpectrumManager spectrumManager = net.bioclipse.spectrum.Activator
		    .getDefault().getJavaSpectrumManager();
       	spectrumManager.saveSpectrum(new JumboSpectrum(spec), ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(parentFolder.getFullPath().toOSString()+File.separator+filename+ ( filename.indexOf( "."+extension ) == -1 ? "."+extension : "" ))), extension);
       	Activator.getDefault().getUIManager().open(ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(parentFolder.getFullPath().toOSString()+File.separator+filename+ ( filename.indexOf( "."+extension ) == -1 ? "."+extension : "" ))));
	}
	
	private CMLSpectrum createNewCMLSpectrum() {
		String type = specPage.getSpectrumType();
		CMLSpectrum spectrum = new CMLSpectrum();
		spectrum.setType(type);
		CMLPeakList peakList = specPage.getPeakList();
		CMLElements<CMLPeak> peaks = peakList.getPeakElements();
		Iterator<CMLPeak> it = peaks.iterator();
		while (it.hasNext()) {
			CMLPeak peak = it.next();
			if (peak.getXValue() == 0) {
				peakList.removeChild(peak);
			}
		}
		spectrum.addPeakList(peakList);
		String spectrumID = GenerateId.generateId();
		spectrum.setId(spectrumID);
		return spectrum;

	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}
}