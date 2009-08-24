/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org�epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Stefan Kuhn - initial API and implementation
 *     
 ******************************************************************************/
package net.bioclipse.spectrum.outline;


import net.bioclipse.spectrum.editor.SpectrumEditor;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.xmlcml.cml.element.CMLSpectrum;

/**
 * An Outline Page for the SpectrumEditor
 * @author shk3
 *
 */
public class SpectrumOutlinePage extends ContentOutlinePage 
                            implements ISelectionListener, IAdaptable{

    private final String CONTRIBUTOR_ID
            ="net.bioclipse.spectrum.outline.SpectrumOutlinePage";
    private CMLSpectrum spectrum;
    private SpectrumEditor editor;
    private TreeViewer treeViewer;


    /**
     * Our constructor
     * @param editorInput
     * @param spectrumEditor
     */
    public SpectrumOutlinePage(CMLSpectrum spectrum
            , SpectrumEditor spectrumEditor) {
        super();
        this.spectrum=spectrum;
        this.editor=spectrumEditor;
    }
    
    /**
     * Sets a new input for the outline
     * @param spectrum The new spectrum 
     */
    public void setInput(CMLSpectrum spectrum){
    	treeViewer.setInput(spectrum);
    	treeViewer.expandToLevel(2);
    }


    /**
     * Set up the treeviewer for the outline with a spectrum as input
     */
    public void createControl(Composite parent) {

        super.createControl(parent);

        treeViewer= getTreeViewer();
        treeViewer.setContentProvider(new SpectrumContentProvider());
        treeViewer.setLabelProvider(new SpectrumLabelProvider());
        treeViewer.addSelectionChangedListener(this);

        if (spectrum==null) return;
        
        setInput(spectrum);
        getSite().getPage().addSelectionListener(this);
    }


    /**
     * Update selected items if selected in editor
     */
    public void selectionChanged(IWorkbenchPart selectedPart,
                                 ISelection selection) {
        // Does nothing for now. See selectionChanged in
        // net.bioclipse.jmol.views.outline.JmolContentOutlinePage
        // for implementation inspiration.
    }



    /**
     * This is our ID for the TabbedPropertiesContributor
     */
    public String getContributorId() {
        return CONTRIBUTOR_ID;
    }


    public Object getAdapter(Class adapter) {
        return null;
    }
}
