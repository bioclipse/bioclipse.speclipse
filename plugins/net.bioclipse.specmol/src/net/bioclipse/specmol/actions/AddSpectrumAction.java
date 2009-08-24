/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package net.bioclipse.specmol.actions;

import net.bioclipse.specmol.wizards.AddSpectrumWizard;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

public class AddSpectrumAction extends AbstractHandler {
    public Object execute( ExecutionEvent event ) throws ExecutionException {
        AddSpectrumWizard predwiz=new AddSpectrumWizard();
        WizardDialog wd=new WizardDialog(new Shell(),predwiz);
        wd.open();
        return null;
    }
}
