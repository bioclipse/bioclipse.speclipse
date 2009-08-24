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

import org.eclipse.jface.action.Action;

public class SwitchModeAction extends Action {



	private SpecMolEditorContributor contributor;

	public SwitchModeAction(SpecMolEditorContributor contributor) {
		super("Switch Assignment mode on/off", Action.AS_CHECK_BOX);
		this.contributor = contributor;
	}
	
	@Override
	public void setChecked(boolean checked) {
		super.setChecked(checked);
		((SpecMolEditor)this.contributor.getActiveEditorPart()).getSpecmoleditorpage().setAssigmentMode(checked);
	}
}
