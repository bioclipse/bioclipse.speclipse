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
package net.bioclipse.nmrshiftdb.actions;

import net.bioclipse.nmrshiftdb.wizards.AssignPredictWizard;
import net.bioclipse.specmol.editor.SpecMolEditor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLSpectrum;

public class CheckAssignmentAction implements IEditorActionDelegate
{

	

	private IEditorPart editor;




	public void run(IAction action) {
		try{
		if (this.editor != null) {
			if (this.editor instanceof SpecMolEditor) {
				SpecMolEditor specMolEditor = (SpecMolEditor) editor;
				CMLMolecule molecule = specMolEditor.getSpecmoleditorpage().getCurrentMolecule();
				CMLSpectrum spectrum = specMolEditor.getSpecmoleditorpage().getCurrentSpectrum();
				AssignPredictWizard predwiz=new AssignPredictWizard(molecule, spectrum, specMolEditor.getSpecmoleditorpage(), editor,true);
				WizardDialog wd=new WizardDialog(new Shell(),predwiz);
				wd.open();
			}
		}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}


	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}




	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.editor = targetEditor;
		
	}
}