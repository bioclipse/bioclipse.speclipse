/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.specmol.editor;

import net.bioclipse.specmol.actions.AssignAction;
import net.bioclipse.specmol.actions.AssignBibtexAction;
import net.bioclipse.specmol.actions.SwitchModeAction;
import net.bioclipse.specmol.actions.ViewBibtexAction;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;

public class SpecMolEditorContributor extends
		MultiPageEditorActionBarContributor {
	
	private SwitchModeAction switchModeAction;
	private AssignAction assignAction;
	
	public SpecMolEditorContributor() {
		super();
		assignAction = new AssignAction(this);
		switchModeAction = new SwitchModeAction(this);
	}
	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		toolBarManager.add(new Separator());
		toolBarManager.add(assignAction);
		toolBarManager.add(switchModeAction);
		super.contributeToToolBar(toolBarManager);
	}


	private IEditorPart activeEditorPart;

	@Override
	public void setActivePage(IEditorPart activeEditor) {
		// TODO Auto-generated method stub

	}
	
	public void contributeToMenu(IMenuManager manager) {
		super.contributeToMenu(manager);
		MenuManager specmolMenu = new MenuManager("SpecMol","net.bioclipse.specmol.menu");
		manager.insertAfter("additions", specmolMenu);
		specmolMenu.add(new ViewBibtexAction(this));
		specmolMenu.add(new AssignBibtexAction(this));
	}
	
	public IEditorPart getActiveEditorPart() {
		return activeEditorPart;
	}
	
	public void setActiveEditor(IEditorPart part) {
		if (!(activeEditorPart == part)) {
			this.activeEditorPart = part;
		}
	}
	
	
	public SwitchModeAction getSwitchModeAction() {
		return switchModeAction;
	}	
}

