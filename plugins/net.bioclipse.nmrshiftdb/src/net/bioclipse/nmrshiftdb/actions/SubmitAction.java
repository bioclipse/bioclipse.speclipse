package net.bioclipse.nmrshiftdb.actions;

import net.bioclipse.nmrshiftdb.wizards.SubmitWizard;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;


public class SubmitAction implements IViewActionDelegate {

	private IViewPart view=null;
	
	
	/**
	 * Constructor for Action1.
	 */
	public SubmitAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		
		SubmitWizard predwiz=new SubmitWizard(view);
		WizardDialog wd=new WizardDialog(new Shell(),predwiz);
		wd.open();
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void init(IViewPart view) {
		this.view = view;
	}
}
