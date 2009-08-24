/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package net.bioclipse.spectrum.editor;

import net.bioclipse.spectrum.action.contribution.AddMetadataAction;
import net.bioclipse.spectrum.action.contribution.IntegrateAction;
import net.bioclipse.spectrum.action.contribution.PeakPickingAction;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.EditorActionBarContributor;

public class SpectrumEditorContributor extends
EditorActionBarContributor {

	private IEditorPart editor;
	PeakPickingAction pickPeaksAction;
	IntegrateAction integrateAction;

	@Override
	public void setActiveEditor(IEditorPart targetEditor) {
		this.editor = targetEditor;
		super.setActiveEditor(targetEditor);
		pickPeaksAction.setActiveEditor((SpectrumEditor)targetEditor);
		integrateAction.setActiveEditor((SpectrumEditor)targetEditor);
	}

	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		toolBarManager.add(new Separator());
		AddMetadataAction addMetadataAction = new AddMetadataAction(this);
		toolBarManager.add(addMetadataAction);
		pickPeaksAction=new PeakPickingAction();
		toolBarManager.add(pickPeaksAction);
		integrateAction = new IntegrateAction();
		toolBarManager.add(integrateAction);
	}

	public IEditorPart getEditor() {
		return editor;
	}

}
