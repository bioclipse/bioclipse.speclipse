/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.specmol.actions;

import net.bioclipse.specmol.editor.SpecMolEditor;
import net.bioclipse.specmol.editor.SpecMolEditorContributor;
import net.bioclipse.specmol.wizards.AssignBibtexWizard;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLSpectrum;

public class AssignBibtexAction extends Action
{

	private SpecMolEditorContributor contributor;

	public AssignBibtexAction(SpecMolEditorContributor contributor) {
		super("Add bibtex entries");
		this.contributor = contributor;
	}
	

	@Override
	public void run() {
		if (this.contributor.getActiveEditorPart() != null) {
			if (this.contributor.getActiveEditorPart() instanceof SpecMolEditor) {
				SpecMolEditor specMolEditor = (SpecMolEditor) contributor.getActiveEditorPart();
				CMLMolecule molecule = specMolEditor.getSpecmoleditorpage().getCurrentMolecule();
				CMLSpectrum spectrum = specMolEditor.getSpecmoleditorpage().getCurrentSpectrum();
				AssignBibtexWizard predwiz=new AssignBibtexWizard(molecule, spectrum,specMolEditor);
				WizardDialog wd=new WizardDialog(new Shell(),predwiz);
				wd.open();
			}
		}
	}
}