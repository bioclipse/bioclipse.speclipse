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

import net.bioclipse.specmol.Activator;
import net.bioclipse.specmol.domain.JumboSpecmol;
import net.bioclipse.spectrum.editor.MetadataUtils;
import nu.xom.Elements;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMetadataList;
import org.xmlcml.cml.element.CMLSpectrum;

public class CloneWizard extends Wizard {
	
	ClonePage clonePage=null;
	private IViewPart view=null;
	private IFile biores=null;
	
	/**
	 * Constructor for JCPWizard.
	 */
	public CloneWizard(IViewPart view) {
		super();
		setWindowTitle("Clone Assigend Spectrum");
		setNeedsProgressMonitor(true);
		this.view=view;
	}

	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		ISelection sel=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		if (sel instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) sel).getFirstElement();
			if (element instanceof IFile) {
				biores=(IFile)element;
				String name=biores.getName();
				clonePage = new ClonePage(name);
				addPage(clonePage);
			}
		}
	}
	
	
	@Override
	public boolean performFinish() {
		try {
			IContainer parent=biores.getParent();
			//remove the ids
			CMLCml cmlcml = Activator.getDefault().getJavaSpecmolManager().loadSpecmol(biores).getJumboObject();
			Elements spectra = cmlcml.getChildCMLElements("spectrum");
			for(int h=0;h<spectra.size();h++){
				Elements metadatalists = ((CMLSpectrum)spectra.get(h)).getChildElements();
				for(int l=0;l<metadatalists.size();l++){
					if(metadatalists.get(l) instanceof CMLMetadataList){
						List<CMLMetadata> mds=MetadataUtils.getMetadataDescendantsByName(((CMLMetadataList)metadatalists.get(l)).getMetadataDescendants(),"nmr:nmrshiftdbid");
						for(int i=0;i<mds.size();i++){
							((CMLMetadataList)metadatalists.get(l)).removeChild(mds.get(i));
						}
					}
				}
			}
			//save the new resource
			Activator.getDefault().getJavaSpecmolManager().saveSpecmol(new JumboSpecmol(cmlcml), parent.getFile(new Path(clonePage.text.getText())));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return true;
	}
	
	public IViewPart getView() {
		return view;
	}
}
