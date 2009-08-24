/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package net.bioclipse.spectrum.editor;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.xml.sax.SAXException;
import org.xmlcml.cml.element.CMLSpectrum;

public class PeakTablePage extends EditorPart{

	CMLSpectrum spectrumItem;
	private boolean isDirty=false;
	static int PEAK_TABLE=1;
	PeakTableViewer viewer;
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		// this is never used, since saving is done via text editor		
	}

	@Override
	public void doSaveAs() {
		// this is never used, since saving is done via text editor	
		
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		this.setSite(site);
		this.setInput(input);
	}

	@Override
	public boolean isDirty() {
		return isDirty;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// this is never used, since saving is done via text editor	
		return true;
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new PeakTableViewer(parent,spectrumItem,this);
		viewer.spectrumItem=spectrumItem;
	}
	
	public void setSpectrumItem(CMLSpectrum spectrumItem) throws ParserConfigurationException, SAXException, IOException{
		if(spectrumItem!=null){
			this.spectrumItem=spectrumItem;
			if(viewer!=null)
				viewer.setSpectrumItem(spectrumItem);
			this.update();
		}
	}

	@Override
	public void setFocus() {
	}
	
    public void setDirty(boolean bool) {
        this.isDirty = bool;
        firePropertyChange(PROP_DIRTY);
    }
    
    public void update() throws ParserConfigurationException, SAXException, IOException {
    	if(viewer!=null)
    		viewer.configureFromXMLFile();
    }

}
