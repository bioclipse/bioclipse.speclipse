/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.specmol.wizards;

import net.bioclipse.specmol.editor.AssignmentPage;
import net.bioclipse.specmol.editor.SpecMolEditor;
import net.bioclipse.specmol.editor.UneditableTextEditor;
import nu.xom.Elements;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.PlatformUI;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLSpectrum;

public class ViewDeleteBibtexWizard extends Wizard {
	private ViewDeleteWizardPage viewDeletePage;
	AssignmentPage assignmentpage=null;
	SpecMolEditor specmoleditor=null;
	CMLCml cml=null;
	CMLMolecule mol=null;
	CMLSpectrum spec=null;
	
	/**
	 * Constructor for JCPWizard.
	 */
	public ViewDeleteBibtexWizard(CMLCml cml, CMLMolecule mol, CMLSpectrum spec, SpecMolEditor specmoleditor) {
		super();
		setWindowTitle("Remove Literature References");
		setNeedsProgressMonitor(true);
		this.assignmentpage=specmoleditor.getSpecmoleditorpage();
		this.specmoleditor=specmoleditor;
		this.cml=cml;
		this.mol=mol;
		this.spec=spec;
	}
	
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		viewDeletePage = new ViewDeleteWizardPage(cml, mol, spec);
		addPage(viewDeletePage);
	}
	
	@Override
	public boolean performFinish() {
		try{
			if(viewDeletePage.getList1()!=null){
				Elements cmlbis=cml.getChildElements("entry","http://bibtexml.sf.net/");
				for(int i=0;i<viewDeletePage.getList1().getItemCount();i++){
					if(viewDeletePage.getList1().isSelected(i)){
						cml.removeChild(cmlbis.get(i));
					}
				}
			}
			if(viewDeletePage.getList2()!=null){
				Elements cmlbis=mol.getChildElements("entry","http://bibtexml.sf.net/");
				for(int i=0;i<viewDeletePage.getList1().getItemCount();i++){
					if(viewDeletePage.getList2().isSelected(i)){
						mol.removeChild(cmlbis.get(i));
					}
				}
			}
			if(viewDeletePage.getList3()!=null){
				Elements cmlbis=spec.getChildElements("entry","http://bibtexml.sf.net/");
				for(int i=0;i<viewDeletePage.getList3().getItemCount();i++){
					if(viewDeletePage.getList3().isSelected(i)){
						spec.removeChild(cmlbis.get(i));
					}
				}
			}
	        //This is nasty code, but I did not find a better possibility to refresh the xml editor
	        specmoleditor.removePage(1);
	        UneditableTextEditor textEditor = new UneditableTextEditor();
	        int index=specmoleditor.addPage(textEditor,specmoleditor.getEditorInput());
	        specmoleditor.setPageText(index,"Source");
	        specmoleditor.setTextEditor(textEditor);
	        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(textEditor);
	        specmoleditor.getSpecmoleditorpage().setDirty(true);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return true;
	}

}
