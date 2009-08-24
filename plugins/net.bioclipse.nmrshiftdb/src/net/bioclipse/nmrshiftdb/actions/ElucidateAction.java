package net.bioclipse.nmrshiftdb.actions;

import net.bioclipse.nmrshiftdb.wizards.ElucidateWizard;
import net.bioclipse.spectrum.domain.JumboSpectrum;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.xmlcml.cml.element.CMLSpectrum;

import spok.utils.SpectrumUtils;


public class ElucidateAction implements IViewActionDelegate {

	private IViewPart view=null;
	
	
	/**
	 * Constructor for Action1.
	 */
	public ElucidateAction() {
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
		ISelection sel=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		if (sel.isEmpty()==false){
		   if (sel instanceof IStructuredSelection) {
		       IStructuredSelection ssel = (IStructuredSelection) sel;
		       try {
		    	   IFile cdkres = (IFile)ssel.getFirstElement();
		    	   JumboSpectrum ac=net.bioclipse.spectrum.Activator
		    	       .getDefault().getJavaSpectrumManager()
		    	       .loadSpectrum(cdkres);
					CMLSpectrum cmlspectrum= ac.getJumboObject();
					if(cmlspectrum.getPeakListElements().size()==0){
						MessageBox mb = new MessageBox(new Shell(), SWT.OK);
			            mb.setText("No Peaks");
			            mb.setMessage("This spectrum does not contain any peaks. Searches need a peak spectrum! Hint: The bc_spectrum plugin offers a peak picking.");
			            mb.open();
						return;
					}
					if(cmlspectrum.getTypeAttribute()==null || !SpectrumUtils.getNormalizedSpectrumType(cmlspectrum.getType()).equals("NMR")){
						MessageBox mb = new MessageBox(new Shell(), SWT.YES|SWT.NO);
			            mb.setText("Wrong type?");
			            mb.setMessage("This spectrum has no type or is not of type NMR.We can still try a search, but the result might be invalid. Shall we continue?");
			            int result=mb.open();
			            if(result==SWT.NO)
			            	return;
					}
					ElucidateWizard predwiz=new ElucidateWizard(cmlspectrum);
					WizardDialog wd=new WizardDialog(new Shell(),predwiz);
					wd.open();
		       } catch (Exception e) {
					throw new RuntimeException(e.getMessage());
		       }
		    }
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void init(IViewPart view) {
		this.view = view;
	}

	public IViewPart getView() {
		return view;
	}
}
