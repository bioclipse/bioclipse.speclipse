/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.specmol.editor;

import java.awt.Color;
import java.awt.event.MouseListener;
import java.io.ByteArrayInputStream;
import java.text.DecimalFormat;
import java.util.List;

import net.bioclipse.cdk.jchempaint.widgets.JChemPaintEditorWidget;
import net.bioclipse.specmol.listeners.AssignmentPageFocusListener;
import net.bioclipse.specmol.listeners.PeakChartCompositeMouseListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.renderer.RendererModel.ExternalHighlightColor;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLSpectrum;

/**
 * The EditorPage containing the Assignment Editor
 * 
 * @author hel, Stefan Kuhn
 *
 */
public class AssignmentPage extends EditorPart {

	private CMLCml cmlcml;
	private boolean isDirty = false;
	public ControlListener cl;
	Table table ;
	java.awt.Frame jcpFrame;
	IAtom atomclosest=null;
	int selectedRow=-1;
	int changedRow=-1;
	private SpecMolDrawingComposite child1;
	protected boolean valid;
	DecimalFormat df = new DecimalFormat("#.00");
	private SpecMolTableComposite child2;
	private SpecMolPeackChartComposite peakChartcomposite;
	private AssignmentController assignmentController;
	private Color highlightColor;
	private boolean assigmentMode = false;
	protected boolean isFocused;
	private boolean hasFocus;
	private PeakChartCompositeMouseListener peakChartCompositeMouseListener;
	private int currentspectrumnumber=0;
	public void setCurrentspectrumnumber(int currentspectrumnumber) {
		this.currentspectrumnumber = currentspectrumnumber;
		child2.updateTable(this.getCurrentSpectrum(),null);
		peakChartcomposite.updateSpectrum(this.getCurrentSpectrum());
	}

	private Label peakDetails;
	SashForm form;
	
	/**
	 * Constructor
	 */
	public AssignmentPage(CMLCml cmlcml) {
		super();
		this.cmlcml=cmlcml;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException{
		//the editorinput is never used, we work on cmlcml
		super.setSite(site);
		super.setInput(input);
	}

	//@Override
	public void createPartControl444(Composite parent) {
	    //  create widget
		JChemPaintEditorWidget widget=new JChemPaintEditorWidget(parent,SWT.NONE);
		IAtomContainer atomContainer=null;

		IChemModel model = null;
		try {
			List<CMLElement> moleculeList = this.getCmlcml().getDescendants("molecule", null, true);
			String moleculestring=moleculeList.get(0).toXML();
	        CMLReader reader = new CMLReader(new ByteArrayInputStream(moleculestring.getBytes()));
	        IChemFile chemfile = (IChemFile)reader.read(new org.openscience.cdk.ChemFile());
			model=chemfile.getChemSequence(0).getChemModel(0);
		} catch (CDKException e) {
			throw new RuntimeException(e);
		}

		if(model!=null)
		    atomContainer=model.getMoleculeSet().getAtomContainer(0);
		
		
		MenuManager menuMgr = new MenuManager();
	  menuMgr.add( new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
	  getSite().registerContextMenu( "net.bioclipse.cdk.ui.editors.jchempaint.menu",
	                                 menuMgr, widget);
	    
	  //Control control = lViewer.getControl();
	  Menu menu = menuMgr.createContextMenu(widget);
	  widget.setMenu(menu);    


		// setup hub 
		getSite().setSelectionProvider( widget );
		widget.setAtomContainer( atomContainer );
	}	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
			assignmentController = new AssignmentController(this);
			SashForm formVert = new SashForm(parent,SWT.VERTICAL);
			formVert.setLayout(new FillLayout());
			form = new SashForm(formVert,SWT.HORIZONTAL);
			form.setLayout(new FillLayout());
			AssignmentPageFocusListener focusListener = new AssignmentPageFocusListener(this);
			
			child1 = new SpecMolDrawingComposite(form, SWT.PUSH,this);
			assignmentController.addSpecMolListener(child1);
			child1.addFocusListener(focusListener);
			highlightColor = child1.getDrawingPanel().
			    getRenderer2DModel().getParameter(
			    	ExternalHighlightColor.class
			    ).getValue();
			
			child2 = new SpecMolTableComposite(form,SWT.NONE, this);
			assignmentController.addSpecMolListener(child2);
			form.setWeights(new int[] {70,30});
			
			// add a status bar, in between the JCP/PeakView and the JChart
			peakDetails = new Label(formVert,SWT.NONE);
			peakDetails.setText("  ");
			
			peakChartCompositeMouseListener = new PeakChartCompositeMouseListener(this);
			peakChartcomposite = new SpecMolPeackChartComposite(formVert, SWT.EMBEDDED, this);
			assignmentController.addSpecMolListener(peakChartcomposite);
			peakChartcomposite.addFocusListener(focusListener);
			
			formVert.setWeights(new int[] {67,3,30});
			updateSpectrum(null);
	}
	
	/**
	 * propagates a update spectrum to the spectrum visualising components (peak table, and peak chart)
	 */
	public void updateSpectrum(CMLSpectrum predictedSpectrum){
		CMLSpectrum spectrum = this.getCurrentSpectrum();
		child2.updateTable(spectrum,predictedSpectrum);
		peakChartcomposite.updateSpectrum(spectrum);
	}
	

	/**
	 * propagates a change in the dirty state of the editor
	 */
	private void fireSetDirtyChanged() {
		Runnable r= new Runnable() {
			public void run() {
				firePropertyChange(PROP_DIRTY);
			}
		};
		Display fDisplay = getSite().getShell().getDisplay();
		fDisplay.asyncExec(r);
		
	}

	@Override
	public void doSaveAs() {
		// not needed, done in SpecMolEditor	
	}


	@Override
	public void setFocus() {
		this.hasFocus = true;
		if (child1 != null) {
			this.child1.setFocus();
		}
	}

	@Override
	public boolean isDirty() {
		return this.isDirty ;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * set the dirty flag of the editor
	 * 
	 * @param boolean isDirty
	 */
	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
		fireSetDirtyChanged();
	}

	/**
	 * retrieve the current molecule
	 * 
	 * @return CMLMolecule molecule
	 */
	public CMLMolecule getCurrentMolecule() {
		return (CMLMolecule)cmlcml.getChildCMLElement("molecule", 0);
	}
	
	
	/**
	 * retrieve the current molecule
	 * 
	 * @return CMLMolecule molecule
	 */
	public CMLCml getCurrentCml() {
		return cmlcml;
	}
	

	/**
	 * retrieve the current spectrum
	 * 
	 * @return CMLSpectrum spectrum
	 */
	public CMLSpectrum getCurrentSpectrum() {
		return (CMLSpectrum)cmlcml.getDescendants("spectrum",null,true).get(currentspectrumnumber);
	}

	/* (non-Javadoc)
	 * @see net.bioclipse.plugins.bc_jchempaint.editors.IJCPEditorPart#getJcpComposite()
	 */
	public SpecMolDrawingComposite getJcpComposite() {
		return child1;
	}


	/**
	 * retrieve the assignment controller for this class
	 * 
	 * @return AssignmentController assignmentController
	 */
	public AssignmentController getAssignmentController() {
		return assignmentController;
	}

	/**
	 * retrieve the color used for highlighting
	 * 
	 * @return Color highlightColor
	 */
	public Color getHighlightColor() {
		return highlightColor;
	}

	/**
	 * get the assignmentMode flag
	 * 
	 * @return boolean assigmentMode
	 */
	public boolean isAssigmentMode() {
		return assigmentMode;
	}

	/**
	 * set the assigmentMode flag
	 * 
	 * @param boolean assigmentMode
	 */
	public void setAssigmentMode(boolean assigmentMode) {
		if (assigmentMode) {
			unselectInAllComposites();
		}
		this.assigmentMode = assigmentMode;
	}

	/**
	 * Sets the content of the peak details text field
	 * 
	 * @param details The text to set
	 */
	public void setPeakDetails(final String details) {
		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				if (details != null && details.length()>0 && peakDetails != null) {
					peakDetails.setText(details);
				}
			}
			
		});
		
	}
	
	/**
	 * propagate a deselection to all components
	 * 
	 */
	private void unselectInAllComposites() {
		this.peakChartcomposite.unselect();
		this.child1.unselect();
		this.child2.unselect();
	}

	/**
	 * set the hasFocus flag to false
	 * 
	 */
	public void lostFocus() {
		this.hasFocus = false;		
	}

	/**
	 * get the hasFocus flag
	 * 
	 * @return boolean hasFocus
	 */
	public boolean getFocus() {
		return this.hasFocus;
	}

	/**
	 * get the PeakChartComnpositeMousListener
	 * 
	 * @return PeakChartComnpositeMousListener peakChartCompositeMouseListener
	 */
	public MouseListener getPeakChartCompositeMouseListener() {
		return this.peakChartCompositeMouseListener;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		//not needed, done in SpecMolEditor		
	}


	/**
	 * get the SpecMolPeackChartComposite
	 * 
	 * @return  SpecMolPeackChartComposite peakChartcomposite
	 */
	public SpecMolPeackChartComposite getPeakChartcomposite() {
		return peakChartcomposite;
	}

	/**
	 * @return The current model the AssignmentPage works on
	 */
	public CMLCml getCmlcml() {
		return cmlcml;
	}
}
