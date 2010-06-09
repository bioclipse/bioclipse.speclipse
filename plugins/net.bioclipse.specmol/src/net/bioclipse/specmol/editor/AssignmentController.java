/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.specmol.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.bioclipse.specmol.listeners.SpecMolListener;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.xmlcml.cml.element.CMLPeak;


/**
 * Handles the selected atom/bond and peak elements and passes changes of these sets around
 * 
 * @author hel
 *
 */
public class AssignmentController {
	ArrayList<CMLPeak> peakList;
	IAtomContainer ac;
	ArrayList<SpecMolListener> listeners = new ArrayList<SpecMolListener>();
	private AssignmentPage page;
	private SpecMolListener notifyingListener;
	
	public AssignmentController(AssignmentPage page) {
		this.page = page;
		
	}
	
	/**
	 * Sets the selected peaks
	 * 
	 * @param peakList - the selected peaks
	 * @param listener - the listener notifying this class
	 */
	public void setSelection(ArrayList<CMLPeak> peakList, SpecMolListener listener) {
		this.peakList = peakList;
		this.notifyingListener = listener;
		if (!page.isAssigmentMode()) {
			findAcForPeakList(peakList);
		}
		fireChange();
	}
	
	/**
	 * Sets the selected atoms/bonds
	 * 
	 * @param ac - the selected AtomContainer
	 * @param listener - the listener notifying this class
	 */
	public void setSelection(IAtomContainer ac, SpecMolListener listener) {
		setSelectedSubstructure(ac);
		this.notifyingListener = listener;
		if (!page.isAssigmentMode()) {
			findPeakListForAc(ac);
		}
		fireChange();
	}
	
	/**
	 * Retrieving the peaks referencing to the selected atoms/bonds
	 * 
	 * @param ac2 - the selected AtomContainer
	 */
	private void findPeakListForAc(IAtomContainer ac2) {
		List<CMLPeak> peaks = page.getCurrentSpectrum().getPeakListElements().getList().get(0).getPeakElements().getList();
		if (peakList == null) {
			peakList = new ArrayList<CMLPeak>();
		}
		else {
			peakList.clear();
		}
		for (int i=0; i<ac2.getAtomCount(); i++) {
			Iterator<CMLPeak> it = peaks.iterator();
			IAtom atom = ac2.getAtom(i);
			while (it.hasNext()) {
				CMLPeak peak = it.next();
				String[] atomRefs = peak.getAtomRefs();
				if (atomRefs != null) {
					for (int j=0; j<atomRefs.length; j++) {
						if (atomRefs[j].equals(atom.getID())) {
							peakList.add(peak);
						}
					}
				}
			}
		}	
	}

	/**
	 * Retrieving the atoms referenced by the selected peaks
	 * 
	 * @param peakList2 - the selected peaks
	 */
	private void findAcForPeakList(ArrayList<CMLPeak> peakList2) {
		if (ac == null) {
			setSelectedSubstructure(
				DefaultChemObjectBuilder.getInstance().newInstance(
					IAtomContainer.class
				)
			);
		}
		else {
			this.getSelectedSubstructure().removeAllElements();
		}
		for (int j=0; j<peakList2.size(); j++) {
			String[] atomrefs = peakList.get(j).getAtomRefs();
			IAtomContainer allac=page.getJcpComposite().getAtomContainer();
			if (!page.isAssigmentMode()) {
				page.getJcpComposite().getDrawingPanel().getRenderer2DModel()
					.setExternalSelectedPart(allac.getBuilder().newInstance(
						IAtomContainer.class
					)
				);
			}
			if (atomrefs != null) {
				for(int i=0;i<atomrefs.length;i++){
					try {
						addAtomToSelectedSubstructure((AtomContainerManipulator.getAtomById(allac,atomrefs[i])));
					} catch (CDKException e) {
						e.printStackTrace();
					}
				}
			}
		}		
	}

	/**
	 * Add a SpecMolListener to this Controller class
	 * 
	 * @param listener - the SpecMolListener
	 */
	public void addSpecMolListener(SpecMolListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Propagate a change to all registered SpecMolListeners except to the notifying class
	 */
	private void fireChange() {
		if (!page.isAssigmentMode()) {
			for (int i=0; i<listeners.size(); i++) {
				if (listeners.get(i) != this.notifyingListener) {
					listeners.get(i).selectionChanged(this);
				}
			}
		}
	}

	/**
	 * Returns the selected AtomContainer
	 * 
	 * @return - AtomContainer
	 */
	public IAtomContainer getSelectedSubstructure() {
		return ac;
	}

	/**
	 * Returns the selected peaks
	 * 
	 * @return ArrayList<CMLPeak>
	 */
	public ArrayList<CMLPeak> getSelectedPeaks() {
		return peakList;
	}

	/**
	 * Set the selected AtomContainer to ac
	 * 
	 * @param ac - the AtomContainer
	 */
	public void setSelectedSubstructure(IAtomContainer ac) {
		this.ac = ac;
	}
	
	/**
	 * Add an atom to the selected AtomContainer
	 * 
	 * @param atom - the Atom to be added
	 */
	public void addAtomToSelectedSubstructure(IAtom atom) {
		getSelectedSubstructure().addAtom(atom);
	}
}
