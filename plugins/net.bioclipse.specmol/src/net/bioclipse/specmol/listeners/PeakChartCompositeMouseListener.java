/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.specmol.listeners;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;

import net.bioclipse.specmol.editor.AssignmentPage;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.xmlcml.cml.element.CMLPeak;

import spok.guicomponents.SpokChartPanel;

public class PeakChartCompositeMouseListener implements java.awt.event.MouseListener {

	private AssignmentPage page;

	public PeakChartCompositeMouseListener(AssignmentPage page) {
		this.page = page;
	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(java.awt.event.MouseEvent arg0) {
		if (!page.getFocus()) {
			page.setFocus();
		}
		
		// translate screen coordinates to java2D and further on to chart coordinates
		SpokChartPanel chartPanel = page.getPeakChartcomposite().getChartPanel();
		Point2D p = chartPanel.translateScreenToJava2D(arg0.getPoint());
		XYPlot plot = (XYPlot) chartPanel.getChart().getPlot();
        ChartRenderingInfo info = chartPanel.getChartRenderingInfo();
        Rectangle2D dataArea = info.getPlotInfo().getDataArea();
        double xx = plot.getDomainAxis().java2DToValue(
            p.getX(), dataArea, plot.getDomainAxisEdge()
        );
        // iterate over the dataset values and find the value nearest to the one clicken onto
        XYDataset dataSet = plot.getDataset();
        double nearestValue = xx;
        double diff = 1000000;
        for (int j=0; j<dataSet.getSeriesCount(); j++) {
	        for (int i=0; i < dataSet.getItemCount(j); i++) {
	        	double value = dataSet.getXValue(j, i);
	        	double newDiff = Math.abs(xx-value);
	        	if (diff == 1000000) {
	        		diff = Math.abs(xx-value);
	        		nearestValue = value;
	        	}
	        	if (newDiff < diff) {
	        		nearestValue = value;
	        		diff = newDiff;
	        	}
	        }
        }
        // find the peak related to the determined xvalue
        ArrayList<CMLPeak> peaks = new ArrayList<CMLPeak>();
        Iterator<CMLPeak> peakIt = page.getCurrentSpectrum().getPeakListElements().get(0).getPeakElements().iterator();
        while (peakIt.hasNext()) {
        	CMLPeak peak = peakIt.next();
        	if (peak.getXValue() == nearestValue) {
        		peaks.add(peak);
        		break;
        	}
        }
        // set the selection to this peak...
        // pass null as SpecMolListener, so that the SpecMolChartComposite itself gets notified as well
        page.getAssignmentController().setSelection(peaks, null);
	}

	public void mouseEntered(java.awt.event.MouseEvent arg0) {
		// do nothing
	}

	public void mouseExited(java.awt.event.MouseEvent arg0) {
		// do nothing
	}

	public void mousePressed(java.awt.event.MouseEvent arg0) {
		// do nothing
	}

	public void mouseReleased(java.awt.event.MouseEvent arg0) {
		// do nothing
	}

}
