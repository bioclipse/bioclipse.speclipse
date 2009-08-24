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
import net.bioclipse.specmol.wizards.ViewDeleteBibtexWizard;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

public class ViewBibtexAction extends Action {
	
	
	private SpecMolEditorContributor contributor;

	public ViewBibtexAction(SpecMolEditorContributor contributor) {
		super("Show bibtex entries");
		this.contributor = contributor;
	}

	@Override
	public void run() {
		if (this.contributor.getActiveEditorPart() != null) {
			if (this.contributor.getActiveEditorPart() instanceof SpecMolEditor) {
				SpecMolEditor specMolEditor = (SpecMolEditor) contributor.getActiveEditorPart();
				ViewDeleteBibtexWizard predwiz=new ViewDeleteBibtexWizard(specMolEditor.getSpecmoleditorpage().getCurrentCml(),specMolEditor.getSpecmoleditorpage().getCurrentMolecule(), specMolEditor.getSpecmoleditorpage().getCurrentSpectrum(),specMolEditor);
				WizardDialog wd=new WizardDialog(new Shell(),predwiz);
				wd.open();
			}
		}
	}

}
