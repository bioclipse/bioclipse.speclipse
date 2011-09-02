/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.specmol.editor;

import javax.vecmath.Point2d;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.openscience.cdk.controller.ControllerModuleAdapter;
import org.openscience.cdk.controller.IChemModelRelay;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * An implementation of a cdk controller module, which offers only one fuction: If an
 * atom is clicked, it is added to the externalselection.
 * 
 * @author shk3
 *
 */
public class SelectByClickModule extends ControllerModuleAdapter implements KeyListener {
	
	AssignmentController assignmentController;
	SpecMolDrawingComposite specMolDrawingComposite;
	private boolean ctrl;

	public SelectByClickModule(IChemModelRelay chemModelRelay, AssignmentController assignmentController, SpecMolDrawingComposite specMolDrawingComposite) {
		super(chemModelRelay);
		this.assignmentController=assignmentController;
		this.specMolDrawingComposite=specMolDrawingComposite;
	}

	public String getDrawModeString() {
		return "Select Atom by Click";
	}


	public void mouseClickedDown(Point2d worldCoord) {
		IAtom atom = chemModelRelay.getClosestAtom(worldCoord);
		if (atom != null) {
			IAtomContainer ac= atom.getBuilder().newInstance(IAtomContainer.class);
			if(ctrl)
				ac=chemModelRelay.getRenderer().getRenderer2DModel().getExternalSelectedPart();
			ac.addAtom(atom);
			chemModelRelay.getRenderer().getRenderer2DModel().setExternalSelectedPart(ac);
			assignmentController.setSelection(ac, specMolDrawingComposite);
		}
		chemModelRelay.updateView();
	}

	public void setChemModelRelay(IChemModelRelay relay) {
		this.chemModelRelay = relay;
	}

	public void keyPressed(KeyEvent e) {
		//TODO the statemask is always zero - why?
		//if(e.stateMask==SWT.CTRL)
			ctrl=true;
	}

	public void keyReleased(KeyEvent e) {
		//if(e.stateMask==SWT.CTRL)
			ctrl=false;
	}
}
