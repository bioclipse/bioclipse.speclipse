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

import java.io.File;
import java.io.IOException;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.jobs.BioclipseUIJob;
import net.bioclipse.specmol.domain.JumboSpecmol;
import net.bioclipse.spectrum.editor.SpectrumEditor;
import net.bioclipse.spectrum.wizards.NewSpectrumWizard;
import nu.xom.Element;
import nu.xom.Elements;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.base.CMLUtil;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLSpectrum;

import spok.utils.SpectrumUtils;

/**
 * This is a wizard allowing prediction of a spectrum fron nmrshiftdb
 * and saving it.
 */

public class PredictWizard extends Wizard{
	protected SpectrumTypeWizardPage typePage;
	protected ServerWizardPage serverPage;
	private DisplayWizardPage displayPage;
	private IFile cdkres;
	private IAtomContainer ac;
	private IMolecule provmol;

	/**
	 * Constructor for JCPWizard.
	 */
	public PredictWizard() {
		super();
		setWindowTitle("Predict NMR Spectrum");
		setNeedsProgressMonitor(true);
		ISelection sel=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		if (sel != null && sel.isEmpty()==false){
		   if (sel instanceof IStructuredSelection) {
		       IStructuredSelection ssel = (IStructuredSelection) sel;
		       try {
		    	   cdkres =(IFile)ssel.getFirstElement();
             Activator.getDefault().getJavaCDKManager().loadMolecule( cdkres,
                  new BioclipseUIJob<ICDKMolecule>() {

                 @Override
                 public void runInUI() {
                     provmol=getReturnValue();                     
                     try {
                        ac=((ICDKMolecule)Activator.getDefault().getJavaCDKManager().perceiveAromaticity( provmol )).getAtomContainer();
                    } catch ( BioclipseException e ) {
                        MessageBox mb = new MessageBox( new Shell(), SWT.OK | SWT.ICON_WARNING );
                        mb.setText( "Error detecting aromaticity" );
                        mb.setMessage( "We could not detect aromaticity. We still do a predition, but this could be wrong." );
                        mb.open();
                    }
                 }
             });
		       } catch (Exception e) {
		           throw new RuntimeException(e.getMessage());
		       }
		    }
		}
	}
	
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		serverPage = new ServerWizardPage();
		addPage(serverPage);
		typePage = new SpectrumTypeWizardPage();
		addPage(typePage);
		displayPage=new DisplayWizardPage();
		addPage(displayPage);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		try{
			//Get filename from wizard page
			String filename = displayPage.getFileName();
			//Get folder to install in from wizard page
			IResource parentFolder = displayPage.getSelectedFolder();
			if(displayPage.getCmlbutton().getSelection()){
				//Create the new SpectrumResource as a child of the folder
				CMLBuilder builder = new CMLBuilder(false);
				CMLSpectrum cmlElement = null;
				try{
					cmlElement = (CMLSpectrum) builder.parseString(displayPage.getSpectrumstring());
				}catch (ClassCastException ex) {
					Element element = (Element) builder.parseString(displayPage.getSpectrumstring());
					SpectrumUtils.namespaceThemAll(element.getChildElements());
					element.setNamespaceURI(CMLUtil.CML_NS);
					cmlElement = (CMLSpectrum) builder.parseString(element.toXML());
				}
				NewSpectrumWizard.createNewSpectrum(filename, SpectrumEditor.CML_TYPE ,parentFolder, cmlElement);
			}else{
				//Create the new SpecMolResource as a child of the folder
				filename = displayPage.getFileName() + "." + SpectrumEditor.CML_TYPE;
				CMLCml cml=new CMLCml();				
				cml.appendChild(provmol.toCML());
				displayPage.getSpectrum().detach();
				cml.appendChild(displayPage.getSpectrum());
				net.bioclipse.specmol.Activator.getDefault().getJavaSpecmolManager().saveSpecmol(new JumboSpecmol(cml), ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(parentFolder.getFullPath().toOSString()+File.separator+filename)));
			}
			return true;
		}catch(Exception ex){
			ex.printStackTrace();
			MessageBox messageBox=new MessageBox(getShell(), SWT.ICON_WARNING);
			messageBox.setMessage("We could not create the file");
			messageBox.setText("Resource creation impossible");
			messageBox.open();
		}
		return false;
	}
	
	public ServerWizardPage getServerPage() {
		return serverPage;
	}

	public SpectrumTypeWizardPage getTypePage() {
		return typePage;
	}

	public DisplayWizardPage getDisplayPage() {
		return displayPage;
	}

	public void setDisplayPage(DisplayWizardPage displayPage) {
		this.displayPage = displayPage;
	}

	public IAtomContainer getAc() {
		return ac;
	}
}