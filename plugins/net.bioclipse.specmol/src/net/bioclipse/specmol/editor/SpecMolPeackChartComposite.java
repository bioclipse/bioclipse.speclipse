/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.specmol.editor;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.plot.XYPlot;
import org.xmlcml.cml.element.AbstractPeak;
import org.xmlcml.cml.element.AbstractPeakStructure;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakStructure;
import org.xmlcml.cml.element.CMLSpectrum;

import spok.guicomponents.SpectrumChartFactory;
import spok.guicomponents.SpokChartPanel;
import net.bioclipse.specmol.listeners.SpecMolListener;

/**
 * @author hel
 *
 */
public class SpecMolPeackChartComposite extends Composite implements SpecMolListener {

	private SpokChartPanel chartPanel;
	private AssignmentPage page;
	private JFreeChart chart;

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param style
	 * @param page
	 */
	public SpecMolPeackChartComposite(Composite parent, int style, AssignmentPage page) {
		super(parent, style);
		this.page = page;
		init(page);
	}

	/**
	 * initializes the composite by creating and adding a peak chart
	 * @param AssignmentPage page 
	 */
	private void init(AssignmentPage page) {
		GridLayout layout = new GridLayout();
		this.setLayout(layout);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		this.setLayoutData(layoutData);
		Frame fileTableFrame = SWT_AWT.new_Frame(this);
		fileTableFrame.setLayout(new BorderLayout());
		//create the chart and add it to the SWT_AWT frame
		chart = SpectrumChartFactory.createPeakChart(null,null, null);
		chart.setTitle("empty chart");
		chartPanel = new SpokChartPanel(chart, "peak", null,null);
		fileTableFrame.add(chartPanel, BorderLayout.CENTER);
		chartPanel.addMouseListener(page.getPeakChartCompositeMouseListener());		
	}

	/**
	 * updates the charts underlying spectrum and repaints the chart with the new content
	 * @param CMLSpectrum spectrum
	 */
	public void updateSpectrum(CMLSpectrum spectrum) {
		chartPanel.setSpectrum(spectrum);
	}

	
	/* (non-Javadoc)
	 * @see bc_specmol.listener.SpecMolListener#selectionChanged(bc_specmol.editors.AssignmentController)
	 * get peakList form controller and highlight contained peaks in the peak chart by using a XYLineAnnotation
	 */
	public void selectionChanged(AssignmentController controller) {
		ArrayList<CMLPeak> peaks = controller.getSelectedPeaks();
		XYPlot plot = chartPanel.getChart().getXYPlot();
		//first remove all existing old Annotations
		List oldAnnos = plot.getAnnotations();
		Iterator it = oldAnnos.iterator();
		while (it.hasNext()) {
			plot.removeAnnotation((XYAnnotation) it.next());
		}
		
		//iterate over selected peaks and highlight them via adding a XYLineAnnotation
		if (peaks != null && peaks.size() > 0) {
			for (int i=0; i<peaks.size(); i++) {
				CMLPeak peak = peaks.get(i);
				double xval = peak.getXValue();
				double yval = peak.getYValue();
				//if peak has no y-value assigned set y value to upperBound of the chart
				if (yval == 0 || Double.isNaN(yval)) {
					yval = plot.getRangeAxis().getUpperBound();
				}
				//create the XYKLineAnnotation and add it to the plot
				XYLineAnnotation lineAnno = new XYLineAnnotation(xval, 0, xval, yval, new BasicStroke(1f), page.getHighlightColor());
				plot.addAnnotation(lineAnno);
			}
		}
		// give peak details
		if (peaks.size() == 1) {
			AbstractPeak peak = (AbstractPeak)peaks.get(0);
			String details = "";
			if (peak.getPeakMultiplicity() != null && peak.getPeakMultiplicity().length() > 0) {
				details = "multiplicity: " + peak.getPeakMultiplicity();
			}
			if (peak.getPeakStructureElements().size() > 0) {
				details += " J-couplings: ";
				Iterator<CMLPeakStructure> iter = peak.getPeakStructureElements().iterator();
				while (iter.hasNext()) {
					String aps = ((AbstractPeakStructure)iter.next()).getCMLValue();
					if ( aps != null && aps.length() > 0)
					details += ((AbstractPeakStructure)iter.next()).getCMLValue() + " "; 
				}
			}
			page.setPeakDetails(details);
		}
	}

	/**
	 * remove all annotations from the chart
	 */
	public void unselect() {
		XYPlot plot = chartPanel.getChart().getXYPlot();
		List oldAnnos = plot.getAnnotations();
		Iterator it = oldAnnos.iterator();
		while (it.hasNext()) {
			plot.removeAnnotation((XYAnnotation) it.next());
		}		
	}

	/**
	 * Method for retrieving the chartPanel containing the chart
	 * @return SpocChartPanel chartPanel
	 */
	public SpokChartPanel getChartPanel() {
		return chartPanel;
	}

}
