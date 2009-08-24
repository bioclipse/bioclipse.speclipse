/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package net.bioclipse.specmol.actions;

import java.util.ArrayList;

import net.bioclipse.specmol.editor.AssignmentPage;
import net.bioclipse.specmol.editor.SpecMolEditor;
import net.bioclipse.specmol.editor.SpecMolEditorContributor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.xmlcml.cml.element.CMLPeak;

public class AssignAction extends Action {


	private SpecMolEditorContributor contributor;

	public AssignAction(SpecMolEditorContributor contributor) {
		super("Do assignment");
		this.contributor = contributor;
	}

	@Override
	public void run() {
		IEditorPart activeEditor = contributor.getActiveEditorPart();
		if (activeEditor instanceof SpecMolEditor) {
			SpecMolEditor specMolEditor = (SpecMolEditor) activeEditor;
			AssignmentPage assignmentPage = specMolEditor.getSpecmoleditorpage();
			if (assignmentPage != null) {
				ArrayList<CMLPeak> peaks = assignmentPage.getAssignmentController().getSelectedPeaks();
				IAtomContainer substructure = assignmentPage.getAssignmentController().getSelectedSubstructure();
				doAssignment(substructure, peaks);
				assignmentPage.updateSpectrum(null);
				assignmentPage.setDirty(true);
			}
		}
	}

	private void doAssignment(IAtomContainer substructure, ArrayList<CMLPeak> peaks) {
		if (contributor.getSwitchModeAction().isChecked()) {
			for (int i=0; i<peaks.size(); i++) {
				CMLPeak peak = peaks.get(i);
				ArrayList<String> atomrefs = new ArrayList<String>();
				for (int j=0; j<substructure.getAtomCount(); j++) {
					IAtom atom = substructure.getAtom(j);
					atomrefs.add(atom.getID());
				}
				String[] atomRefArray = new String[atomrefs.size()];
				for (int k=0; k<atomrefs.size(); k++) {
					atomRefArray[k]= atomrefs.get(k);
				}
				if (atomRefArray.length > 0) {
					peak.setAtomRefs(atomRefArray);
				}
			}
		}
		else {
			MessageDialog.openInformation(contributor.getPage().getActivePart().getSite().getShell() , "Not in assignment mode", "You need to switch to assignment mode to do assignments");
		}
	}
	
}
