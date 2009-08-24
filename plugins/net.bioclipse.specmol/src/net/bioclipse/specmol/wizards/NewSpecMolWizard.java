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
import java.util.List;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.chemoinformatics.wizards.AddMoleculeWizardPage;
import net.bioclipse.chemoinformatics.wizards.WizardHelper;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.jobs.BioclipseUIJob;
import net.bioclipse.specmol.domain.JumboSpecmol;
import net.bioclipse.spectrum.domain.JumboSpectrum;
import net.bioclipse.spectrum.editor.SpectrumEditor;
import nu.xom.Attribute;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.base.CMLType;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLConditionList;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLPeakList;
import org.xmlcml.cml.element.CMLSpectrum;

import spok.utils.PeakPicker;

public class NewSpecMolWizard extends Wizard implements INewWizard {

	private AddMoleculeWizardPage addMoleculePage;
	private AddSpectrumWizardPage addSpectrumPage;
	private WizardNewFileCreationPage selectFilePage;
	private final Logger logger = Logger.getLogger(NewSpecMolWizard.class);
	private IFile newFile;
	
	public NewSpecMolWizard() {
		super();
		setWindowTitle("Create a new Assigned Spectrum File");
		setNeedsProgressMonitor(true);
	}
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}
	
	public void addPages()  
	{  
    ISelection sel=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
    if(sel instanceof IStructuredSelection){
		    selectFilePage = new WizardNewFileCreationPage("newfile",(IStructuredSelection) sel);
		    selectFilePage.setFileName(WizardHelper.findUnusedFileName((IStructuredSelection)sel, "unnamed", ".cml"));
    }else{
        selectFilePage = new WizardNewFileCreationPage("newfile",StructuredSelection.EMPTY);
    }        
		selectFilePage.setTitle("Choose name and location for new assigned spectrum");
		selectFilePage.setDescription("Extension will be .cml if none is given");
		addMoleculePage = new AddMoleculeWizardPage("Add a molecule","Add a Molecule to your new SpecMol Resource","Add empty molecule", false);
		addSpectrumPage = new AddSpectrumWizardPage("Add spectra to your new assigned spectrum file");
		this.addPage(selectFilePage);
		this.addPage(addMoleculePage);
		this.addPage(addSpectrumPage);

	}
	
	@Override
	public boolean performFinish() {
	    try{
	      newFile = selectFilePage.createNewFile();
        if(newFile.getName().indexOf( "."+SpectrumEditor.CML_TYPE) == -1)
            newFile = ResourcesPlugin.getWorkspace().getRoot().getFile( new Path(newFile.getFullPath().toOSString()+"."+SpectrumEditor.CML_TYPE ));
    		IResource molResource = null;
    		if(!addMoleculePage.isCheckboxChecked())
    		    molResource = addMoleculePage.getSelectedRes().get( 0 );
    		final ArrayList<IFile> spectra = addSpectrumPage.getSpectra();
    		CMLMolecule cmlMolecule = null;
    		if(molResource!=null){
            Activator.getDefault().getJavaCDKManager().loadMolecule( (IFile)molResource,
                 new BioclipseUIJob<ICDKMolecule>() {

                @Override
                public void runInUI() {
                    String cmlstring;
                    try {
                        cmlstring = getReturnValue().toCML();
                      CMLBuilder builder = new CMLBuilder();
                      CMLElement cmlElement = null;
                      cmlElement = (CMLElement)builder.parseString(cmlstring);
                      cmlElement.detach();
                      CMLMolecule cmlMolecule = (CMLMolecule) cmlElement;
                      createFile(cmlMolecule,spectra);
                    } catch ( Exception e ) {
                        LogUtils.handleException( e, logger, net.bioclipse.specmol.Activator.PLUGIN_ID );
                    }
                    
                }
            });
            return true;    		    
    		}else{
    			cmlMolecule = new CMLMolecule();
    			return createFile(cmlMolecule,spectra);
    		}
      }catch(Exception ex){
          LogUtils.handleException( ex, logger, net.bioclipse.specmol.Activator.PLUGIN_ID );
          return false;
      }
	}
	    
  private boolean createFile(CMLMolecule cmlMolecule, ArrayList<IFile> spectra) throws IOException, BioclipseException, CoreException{       
    CMLCml cml = new CMLCml();
    cml.addAttribute( new Attribute("convention","nmrshiftdb") );
    cml.addNamespaceDeclaration( "nmrshiftdb","http://nmrshiftdb.sourceforge.net/nmrshiftdb-convention.html");
		if (cmlMolecule != null) {
			cml.appendChild(cmlMolecule);
		}
		else {
			return false;
		}
		if (spectra.size() == 0) {
			cml.appendChild(new CMLSpectrum());
		}
		else {
			for (int i=0; i<spectra.size(); i++) {
				IFile file = (IFile) spectra.get(i);
				JumboSpectrum spectrum = net.bioclipse.spectrum.Activator
				     .getDefault().getJavaSpectrumManager().loadSpectrum(file);
				CMLSpectrum cmlSpec = spectrum.getJumboObject();
						if (cmlSpec.getPeakListElements().size() == 0 && cmlSpec.getSpectrumDataElements().size() != 0) {
							MessageDialog.openInformation(new Shell(), "No peaks - Do Peak Picking?", "The selected spectrum " + cmlSpec.getId() + "contains just continuous data, a peak picking will be performed");
							PeakPicker picker = new PeakPicker(cmlSpec.getSpectrumDataElements().get(0));
							CMLPeakList peaks = picker.getPeakArray();
							cmlSpec.removeChild(cmlSpec.getSpectrumDataElements().get(0));
							if(cmlSpec.getType().equals("NMR")){
								boolean convert=MessageDialog.openQuestion(new Shell(), "Convert to ppm", "Do you want to convert the peaks to ppm?");
								if(convert){
									List<CMLElement> l=((CMLConditionList)cmlSpec.getConditionListElements().get(0)).getChildCMLElements();
									for(int k=0;k<l.size();k++){
										if(l.get(k).getAttribute("id")!=null && l.get(k).getAttribute("id").getValue().equals("dotOBSERVEFREQUENCY")){
											double freq=Double.parseDouble(l.get(k).getValue());
											for(int m=0;m<peaks.getPeakElements().size();m++){
												peaks.getPeakElements().get(m).setXValue(peaks.getPeakElements().get(m).getXValue()/freq);
											}
											break;
										}										
									}
								}
							}
							cmlSpec.addPeakList(peaks);
						}
						cmlSpec.detach();
						cml.appendChild(cmlSpec);
			}
		}
		net.bioclipse.specmol.Activator.getDefault().getJavaSpecmolManager().saveSpecmol(new JumboSpecmol(cml), newFile);
		net.bioclipse.ui.business.Activator.getDefault().getUIManager().open(newFile);
		return true;
	}
}
