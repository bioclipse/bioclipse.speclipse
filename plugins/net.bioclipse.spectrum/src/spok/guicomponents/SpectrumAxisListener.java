/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package spok.guicomponents;

import java.util.ArrayList;

import net.bioclipse.spectrum.editor.ChartPage;
import net.bioclipse.spectrum.views.SpectrumCompareView;

import org.eclipse.ui.IWorkbenchPart;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;

/**
 * A listener for zoom event in the spectrum charts, securing, that the x-axis
 * scaling is syncronised in both charts (peak & continuous) or in several peak charts.
 * 
 * @author Stefan Kuhn
 * @created 4/Aug/08
 */
public class SpectrumAxisListener implements AxisChangeListener {
	private double oldLower = 0, oldUpper = 0;
	private IWorkbenchPart page;
	private boolean externallimits=false;
	private double minx;
	private double maxx;
	private double origmin;
	private double origmax;
	
	
	/**
	 * Constructor for the SpectrumAxisListener object
	 * 
	 * @param page2
	 *            the Workbench page containing the ContinuousSpectrumView and
	 *            the PeakSpectrumView
	 * @param chart
	 *            the JFreeChart the listener is connected to
	 */
	public SpectrumAxisListener(IWorkbenchPart page2, JFreeChart chart) {
		this.page = page2;
	}

	/**
	 * Constructor for the SpectrumAxisListener object
	 * 
	 * @param page2
	 *            the Workbench page containing the ContinuousSpectrumView and
	 *            the PeakSpectrumView
	 * @param chart
	 *            the JFreeChart the listener is connected to
	 * @param	minx
	 * @param	maxx When the zoom is reset, these bounds will be used (else the input determines the bounds)
	 */
	public SpectrumAxisListener(IWorkbenchPart page2, JFreeChart chart, double minx, double maxx) {
		this.page = page2;
		externallimits=true;
		this.minx=minx;
		this.maxx=maxx;
		this.origmax=chart.getXYPlot().getDomainAxis().getUpperBound();
		this.origmin=chart.getXYPlot().getDomainAxis().getLowerBound();
	}
	
	/**
	 * On zooming of one chart the x-Axis of the other one gets syncronized
	 * 
	 * @param event
	 *            the AxisChangeEvent
	 */
	public void axisChanged(AxisChangeEvent event) {
		double upper = ((ValueAxis) event.getAxis()).getUpperBound();
		double lower = ((ValueAxis) event.getAxis()).getLowerBound();
		if(externallimits && upper==origmax && lower==origmin){
			lower=minx;
			upper=maxx;
		}
		if (Math.round(upper) != Math.round(oldUpper)
				|| Math.round(lower) != Math.round(oldLower)) {
			oldUpper = upper;
			oldLower = lower;
			if(page!=null){
				ArrayList<JFreeChart> charts = new ArrayList<JFreeChart>();
				if(page instanceof ChartPage){
					if(((ChartPage)page).getContinuousChartPanel()!=null)
						charts.add(((ChartPage)page).getContinuousChartPanel().getChart());
					if(((ChartPage)page).getPeakChartPanel()!=null)
						charts.add(((ChartPage)page).getPeakChartPanel().getChart());
				}
				if(page instanceof SpectrumCompareView){
					charts.addAll(((SpectrumCompareView)page).getCharts());
				}
				for (int f = 0; f < charts.size(); f++) {
					JFreeChart chart = charts.get(f);
					if (chart != null) {
						chart.getXYPlot().getDomainAxis().setUpperBound(upper);
						chart.getXYPlot().getDomainAxis().setLowerBound(lower);
					}
				}
			}
		}
	}
}
