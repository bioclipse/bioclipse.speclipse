/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.specmol.wizards;

import java.util.HashSet;

import net.bioclipse.bibtex.Activator;
import net.bioclipse.specmol.editor.AssignmentPage;
import net.bioclipse.specmol.editor.SpecMolEditor;
import net.bioclipse.specmol.editor.UneditableTextEditor;
import nu.xom.Document;
import nu.xom.Element;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.PlatformUI;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLSpectrum;

public class AssignBibtexWizard  extends Wizard{
	
	private ChooseBibtexWizardPage bibtexPage;
	private ChooseIdWizardPage entryPage;
	private CMLMolecule molecule;
	private CMLSpectrum spectrum;
	AssignmentPage assignmentpage=null;
	SpecMolEditor specmoleditor=null;
	
	/**
	 * Constructor for JCPWizard.
	 */
	public AssignBibtexWizard(CMLMolecule molecule, CMLSpectrum spectrum, SpecMolEditor specmoleditor) {
		super();
		setWindowTitle("Add Literature References");
		setNeedsProgressMonitor(true);
		this.spectrum=spectrum;
		this.molecule=molecule;
		this.assignmentpage=specmoleditor.getSpecmoleditorpage();
		this.specmoleditor=specmoleditor;
	}
	
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		bibtexPage = new ChooseBibtexWizardPage();
		bibtexPage.setPageComplete(false);
		addPage(bibtexPage);
		entryPage = new ChooseIdWizardPage();
		entryPage.setPageComplete(false);
		addPage(entryPage);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		try{
			HashSet<String> hs=new HashSet<String>();
			hs.add(entryPage.map.get(new Integer(entryPage.list1.getSelectionIndex())).getId());
	        Document doc = Activator.getDefault().getJavaBibtexManager().loadBibliodata(bibtexPage.getSelectedRes()).getJabrefDatabaseAsXml();
	        Element child=doc.getRootElement().getChildElements("entry","http://bibtexml.sf.net/").get(0);
	        child.detach();
	        if(entryPage.getMolbutton().getSelection()){
	        	molecule.appendChild(child);
	        }else if(entryPage.getSpecbutton().getSelection()){
	        	spectrum.appendChild(child);
	        }else{
	        	Element parent=spectrum;
	        	while(!parent.getLocalName().equals("cml")){
	        		parent=(Element)parent.getParent();;
	        	}
	        	parent.appendChild(child);
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

	public ChooseBibtexWizardPage getBibtexPage() {
		return bibtexPage;
	}

	public ChooseIdWizardPage getEntryPage() {
		return entryPage;
	}

	public void setEntryPage(ChooseIdWizardPage entryPage) {
		this.entryPage = entryPage;
	}
	
	public boolean canFinish(){
		int selection=entryPage.list1.getSelectionIndex();
		if(selection>-1)
			return true;
		else
			return false;
	}
}
