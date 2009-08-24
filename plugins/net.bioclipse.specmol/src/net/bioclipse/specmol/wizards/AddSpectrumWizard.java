/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.specmol.wizards;


import java.io.IOException;
import java.util.ArrayList;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.specmol.Activator;
import net.bioclipse.specmol.domain.IJumboSpecmol;
import net.bioclipse.spectrum.domain.JumboSpectrum;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLPeakList;
import org.xmlcml.cml.element.CMLSpectrum;

import spok.utils.PeakPicker;

public class AddSpectrumWizard extends Wizard implements INewWizard {

	private AddSpectrumWizardPage addSpectrumPage;
	private static final Logger logger = Logger.getLogger(AddSpectrumWizard.class);
	
	public AddSpectrumWizard() {
		super();
		setWindowTitle("Add Spectrum to Assigend Spectrum File");
		setNeedsProgressMonitor(true);
	}
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}
	
	public void addPages()  
	{  
		addSpectrumPage = new AddSpectrumWizardPage("This wizard lets you add spectra to an existing assigned spectrum file");
		this.addPage(addSpectrumPage);

	}
	
	@Override
	public boolean performFinish() {
		try {
			ISelection sel=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
			if (sel instanceof IStructuredSelection) {
				Object element = ((IStructuredSelection) sel)
				.getFirstElement();
				if (element instanceof IFile) {
				IJumboSpecmol specmol;
					specmol = Activator.getDefault().getJavaSpecmolManager().loadSpecmol((IFile)element);
				CMLCml cmlcml= specmol.getJumboObject();
				ArrayList<IFile> spectra = addSpectrumPage.getSpectra();
				for (int i=0; i<spectra.size(); i++) {
					IFile file = (IFile) spectra.get(i);
					JumboSpectrum spectrum = net.bioclipse.spectrum.Activator
					    .getDefault().getJavaSpectrumManager()
					    .loadSpectrum(file);
					CMLSpectrum cmlSpec = spectrum.getJumboObject();
					if (cmlSpec.getPeakListElements().size() == 0 && cmlSpec.getSpectrumDataElements().size() != 0) {
						MessageDialog.openInformation(this.getShell(), "No peaks - Do Peak Picking?", "The selected spectrum " + cmlSpec.getId() + "contains just coninuous data, a peak picking will be performed");
						PeakPicker picker = new PeakPicker(cmlSpec.getSpectrumDataElements().get(0));
						CMLPeakList peaks = picker.getPeakArray();
						cmlSpec.removeChild(cmlSpec.getSpectrumDataElements().get(0));
						cmlSpec.addPeakList(peaks);
					}
					cmlSpec.detach();
					cmlcml.appendChild(cmlSpec);
				}
				Activator.getDefault().getJavaSpecmolManager().saveSpecmol(specmol, (IFile)element);			
				return true;
			}else{
				return false;
			}
			}
		} catch (IOException e) {
			LogUtils.handleException(e,logger);
		} catch (BioclipseException e) {
			LogUtils.handleException(e,logger);
		} catch (CoreException e) {
			LogUtils.handleException(e,logger);
		}
		return false;
	}
}
