/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.specmol.editor;

import java.io.ByteArrayInputStream;
import java.util.List;

import net.bioclipse.cdk.jchempaint.widgets.JChemPaintEditorWidget;
import net.bioclipse.specmol.listeners.SpecMolListener;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.io.CMLReader;
import org.xmlcml.cml.base.CMLElement;

/**
 * @author hel, Stefan Kuhn
 *
 */
public class SpecMolDrawingComposite extends Composite implements SpecMolListener{

	private boolean ret;
	private JChemPaintEditorWidget drawingPanel;
	public JChemPaintEditorWidget getDrawingPanel() {
		return drawingPanel;
	}

	private AssignmentPage page;
	private IAtomContainer atomContainer;

	/**
	 * Constructor
	 * 
	 * @param Composite parent
	 * @param int style
	 * @param AssignmentPage page
	 */
	public SpecMolDrawingComposite(Composite parent, int style, AssignmentPage page) {
		super(parent, style);
		this.page = page;
		this.init(page);
	}

	/**
	 * initializes the composite itself (creates drawingPanel and jcpModel..)
	 * 
	 * @param AssignmentPage page
	 */
	private void init(AssignmentPage page) {
		this.setLayout(new FillLayout());	
		
		drawingPanel=new JChemPaintEditorWidget(this,SWT.NONE);
		
		IChemModel model = null;
		try {
			List<CMLElement> moleculeList = page.getCmlcml().getDescendants("molecule", null, true);
			String moleculestring=moleculeList.get(0).toXML();
	        CMLReader reader = new CMLReader(new ByteArrayInputStream(moleculestring.getBytes()));
	        IChemFile chemfile = (IChemFile)reader.read(new org.openscience.cdk.ChemFile());
			model=chemfile.getChemSequence(0).getChemModel(0);
		} catch (CDKException e) {
			throw new RuntimeException(e);
		}
		//TODO all input
		drawingPanel.setAtomContainer(model.getMoleculeSet().getAtomContainer(0) );
		SelectByClickModule selectByClickModule=new SelectByClickModule(drawingPanel.getControllerHub(),page.getAssignmentController(),this);
		drawingPanel.setActiveDrawModule(selectByClickModule);
		this.atomContainer=model.getMoleculeSet().getAtomContainer(0) ;
		drawingPanel.addKeyListener(selectByClickModule);
	}


	public IAtomContainer getAtomContainer() {
		return atomContainer;
	}

	/* (non-Javadoc)
	 * @see bc_specmol.listener.SpecMolListener#selectionChanged(bc_specmol.editors.AssignmentController)
	 */
	public void selectionChanged(AssignmentController controller) {
		IAtomContainer atomsAndBonds = controller.getSelectedSubstructure();
		drawingPanel.getRenderer2DModel().getExternalSelectedPart().add(atomsAndBonds);
		drawingPanel.redraw();
	}

	/**
	 * get the assignment editor page 
	 * 
	 * @return AssignmentPage page
	 */
	public AssignmentPage getPage() {
		return page;
	}

	/**
	 * deselect all selected atoms/bonds
	 */
	public void unselect() {
		drawingPanel.getRenderer2DModel().setExternalSelectedPart(
			DefaultChemObjectBuilder.getInstance().newInstance(
				IAtomContainer.class
			)
		);		
		if (page.getAssignmentController().getSelectedSubstructure() != null) {
			page.getAssignmentController().getSelectedSubstructure().removeAllElements();
		}
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Composite#setFocus()
	 */
	@Override
	public boolean setFocus() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				ret = SpecMolDrawingComposite.super.setFocus();
			}
			
		});
		return ret;
	}
}
