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

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.domain.ISpectrum;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.jobs.BioclipseUIJob;
import net.bioclipse.nmrshiftdb.Activator;
import net.bioclipse.nmrshiftdb.util.NmrshiftdbUtils;
import net.bioclipse.specmol.domain.JumboSpecmol;
import net.bioclipse.spectrum.domain.JumboSpectrum;

import org.apache.axis.client.Call;
import org.apache.axis.client.async.AsyncCall;
import org.apache.axis.client.async.IAsyncResult;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.openscience.cdk.io.formats.CMLFormat;
import org.openscience.cdk.io.formats.IChemFormat;
import org.xmlcml.cml.element.CMLSpectrum;


/**
 * This wizard performs a spectrum search on NMRShiftDB for a spectrum and stores the result in a virtual folder
 */

public class ElucidateWizard extends Wizard{
	private ElucidateServerPage serverPage;
	CMLSpectrum cmlspectrum =null;
	String VIRTUAL_FOLDER="NMRShiftDB spectrum search results";
	IAsyncResult ar;
	Call     call;
	AsyncCall acall;
	SOAPBodyElement[] input;
	Shell shell;
	Boolean flag;
	private static final Logger logger = Logger.getLogger(ElucidateWizard.class);

	/**
	 * Constructor for JCPWizard.
	 */
	public ElucidateWizard(CMLSpectrum cmlspectrum) {
		super();
		setWindowTitle("Search NMRShiftDB by Spectrum");
		setNeedsProgressMonitor(true);
		this.cmlspectrum=cmlspectrum;
	}
	
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		serverPage=new ElucidateServerPage();
		addPage(serverPage);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	
	public boolean performFinish() {
		try{
			ISpectrum spectrum = new JumboSpectrum(cmlspectrum);
            net.bioclipse.nmrshiftdb.Activator.getDefault().getJavaNmrshiftdbManager().searchBySpectrum(spectrum, serverPage.selectedOption().equals("sub"), serverPage.getSelectedServer(), new BioclipseUIJob<List<ICDKMolecule>>() {
                @Override
                public void runInUI() {
        			List<ICDKMolecule> result = getReturnValue();
        			//We save the results in a virtual folder
        			try{
	        			IFolder virtualfolder=NmrshiftdbUtils.createVirtualFolder();
	        			for(int i=0;i<result.size();i++){
	        		        CDKMolecule cmlMol = (CDKMolecule) result.get(i);
	        		        String similarity = (String)cmlMol.getAtomContainer().getProperty("similarity");
	        		        ICDKManager manager = 
	        		            net.bioclipse.cdk.business.Activator
	        		                    .getDefault().getJavaCDKManager();
	        		        String filename = (similarity.length() == 7 ? "0"
	        	                + similarity : (similarity.length() == 6 ? "00"
	        	                + similarity : similarity))
	        	                + " similarity" + i + ".cml";
	        				manager.saveMolecule(cmlMol, 
	        				                     virtualfolder.getFile(filename), 
	        				                     (IChemFormat)CMLFormat.getInstance());
	        			}
	        			MessageBox mb = new MessageBox(new Shell(),  SWT.ICON_INFORMATION | SWT.OK);
	        			mb.setMessage("Your result have been saved to "+virtualfolder.getFullPath().toOSString());
	        			mb.setText("NMRShiftDB search results saved");
	        			mb.open();
        			}catch(Exception ex){
        				LogUtils.handleException(ex, logger, Activator.ID);
        			}
                }
                
            });
		}catch(Exception ex){
			LogUtils.handleException(ex, logger, Activator.ID);
		}
		return true;
	}
}