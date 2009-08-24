/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package net.bioclipse.spectrum.editor;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JSplitPane;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.jfree.chart.JFreeChart;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLSpectrum;

import spok.guicomponents.SpectrumChartFactory;
import spok.guicomponents.SpokChartPanel;

public class ChartPage extends EditorPart {

	private CMLSpectrum spectrumItem;
	private SpokChartPanel peakChartPanel;

	private SpokChartPanel continuousChartPanel;
	
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
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite contChartcomposite = new Composite(parent, SWT.EMBEDDED);
		FillLayout layout = new FillLayout(SWT.VERTICAL);
		contChartcomposite.setLayout(layout);
		Frame fileTableFrame = SWT_AWT.new_Frame(contChartcomposite);
		JFreeChart  chart = SpectrumChartFactory.createContinousChart(spectrumItem,this);
		chart.setTitle(spectrumItem.getTitle());
		continuousChartPanel = new SpokChartPanel(chart, "continuous", spectrumItem,this);
		//matches is for the multi view - we leave it empty here
		List<CMLElement> matches = new ArrayList<CMLElement>();
		JFreeChart chartpeak = SpectrumChartFactory.createPeakChart(spectrumItem, matches,this);
		peakChartPanel = new SpokChartPanel(chartpeak, "peak", spectrumItem,this);
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,continuousChartPanel, peakChartPanel);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(300);
		fileTableFrame.add(splitPane);
	}

	@Override
	public void setFocus() {
	}

	public void setSpectrumItem(CMLSpectrum spectrumItem){
		if(spectrumItem!=null){
			this.spectrumItem=spectrumItem;
			if(continuousChartPanel!=null && peakChartPanel!=null){
				continuousChartPanel.setSpectrum(spectrumItem);
				peakChartPanel.setSpectrum(spectrumItem);
			}
		}
	}
	
	public void update(){
		continuousChartPanel.update();
		peakChartPanel.update();
	}

	public SpokChartPanel getContinuousChartPanel() {
		return continuousChartPanel;
	}

	public SpokChartPanel getPeakChartPanel() {
		return peakChartPanel;
	}
}
