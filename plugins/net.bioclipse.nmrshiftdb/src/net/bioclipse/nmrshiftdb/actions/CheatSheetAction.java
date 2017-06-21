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

import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.specmol.wizards.NewSpecMolWizard;
import net.bioclipse.spectrum.wizards.NewSpectrumWizard;
import net.bioclipse.ui.business.Activator;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

public class CheatSheetAction extends Action implements ICheatSheetAction {

  private static final Logger logger = Logger.getLogger(CheatSheetAction.class);

	public void run(String[] params, ICheatSheetManager manager) {
		if(params[0].equals("newstruc")){
	      //Open editor with content (String) as content
        ICDKMolecule mol = new CDKMolecule(
        	SilentChemObjectBuilder.getInstance().newInstance( IAtomContainer.class)
        );
        try {
            Activator.getDefault().getUIManager().open( mol, 
                                "net.bioclipse.cdk.ui.editors.jchempaint.cml" );
        } catch ( Exception e ) {
            LogUtils.handleException( e, logger, net.bioclipse.nmrshiftdb.Activator.ID );
        }
   }else if(params[0].equals("newspec")){
			NewSpectrumWizard predwiz=new NewSpectrumWizard();
			WizardDialog wd=new WizardDialog(new Shell(),predwiz);
			wd.open();			
		}else if(params[0].equals("newspecmol")){
			NewSpecMolWizard predwiz=new NewSpecMolWizard();
			WizardDialog wd=new WizardDialog(new Shell(),predwiz);
			wd.open();			
		}
	}

}
