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
package net.bioclipse.spectrum.action;

import net.bioclipse.core.util.LogUtils;
import net.bioclipse.spectrum.editor.SpectrumEditor;
import net.bioclipse.spectrum.wizards.NewSpectrumWizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.eclipse.ui.internal.WorkbenchPlugin;

public class CheatSheetAction extends Action implements ICheatSheetAction {

  private static final Logger logger = Logger.getLogger(CheatSheetAction.class);
  
	public void run(String[] params, ICheatSheetManager manager) {
		if(params[0].equals("opencompare")){
		    IWorkbenchWindow dw = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		    IWorkbenchPage page = dw.getActivePage();
        try {
            page.showView("net.bioclipse.spectrum.views.SpectrumCompareView");
        } catch ( PartInitException e ) {
            LogUtils.handleException( e, logger);
        }
    } else if(params[0].equals("newspectrum")){
        NewSpectrumWizard predwiz=new NewSpectrumWizard();
        WizardDialog wd=new WizardDialog(new Shell(),predwiz);
        wd.open();
    } else if(params[0].equals("saveas")){
        if(WorkbenchPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor() instanceof SpectrumEditor){
         ((SpectrumEditor)WorkbenchPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor()).doSaveAs();   
        }
    }
	}

}
