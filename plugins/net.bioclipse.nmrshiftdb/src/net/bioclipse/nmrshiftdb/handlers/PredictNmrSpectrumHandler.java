package net.bioclipse.nmrshiftdb.handlers;

import net.bioclipse.nmrshiftdb.wizards.PredictWizard;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;


public class PredictNmrSpectrumHandler extends AbstractHandler {

    public Object execute( ExecutionEvent event ) throws ExecutionException {
        PredictWizard predwiz=new PredictWizard();
        WizardDialog wd=new WizardDialog(new Shell(),predwiz);
        wd.open();  
        return null;
    }

}
