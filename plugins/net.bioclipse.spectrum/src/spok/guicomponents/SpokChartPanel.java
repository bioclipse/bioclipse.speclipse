/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package spok.guicomponents;

import java.awt.event.MouseEvent;

import net.bioclipse.spectrum.editor.ChartPage;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.xmlcml.cml.element.CMLSpectrum;

/**
 * @author Stefan Kuhn
 * @created 4/Aug/08
 * 
 * A SwingPanel acting as container for JFreeChart charts. Used for showing and
 * placing the spectrum charts as swing components within the views composite
 * 
 */
public class SpokChartPanel extends ChartPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4704886223722312693L;

	private String chartType;

	private CMLSpectrum spectrum;

	private ChartPage page;

	private JFreeChart chart = null;

	/**
	 * @param chart
	 *            the JFreeChart chart to be embedded
	 * @param type
	 *            a string descriminating the spectrum/chart type (peak or
	 *            continuous)
	 * @param spectrum
	 *            the cmlSpectrum the chart should be build and displayed for
	 */
	public SpokChartPanel(JFreeChart chart, String type, CMLSpectrum spectrum, ChartPage page) {
		super(chart);
		this.chartType = type;
		this.spectrum = spectrum;
		this.page = page;

	}

	/**
	 * Updates the UI components of this chart, e. g. after a spectrum change.
	 */
	public void update() {
		chart = null;
		if (spectrum != null) {
			if (chartType.equals("peak")) {
				if (spectrum.getPeakListElements().size() != 0) {
					chart = SpectrumChartFactory
							.createPeakChart(spectrum, null, page);
				}
			} else if (chartType.equals("continuous")) {
				if (spectrum.getSpectrumDataElements() != null) {
					if (spectrum.getSpectrumDataElements().size() != 0) {
						chart = SpectrumChartFactory.createContinousChart(
								spectrum, page);
					}
				}
			}
			if (chart != null) {
				this.setChart(chart);
				this.setMouseZoomable(true, false);
			}
		}
		this.setChart(chart);
		if (chart != null) {
			chart.fireChartChanged();
		}
		this.repaint();
	}

	/**
	 * Sets and resets the spectrum for a chart -> the chart is been rebuild
	 * with this data
	 * 
	 * @param spectrum
	 *            the cmlSpectrum the chart should be build and displayed for
	 */
	public void setSpectrum(CMLSpectrum spectrum) {
		this.spectrum = spectrum;
		this.update();
	}

	@Override
	protected void processMouseEvent(MouseEvent arg0) {
		// for avoiding null pointer errors if someone clicks on empty chart...
		if (this.spectrum != null) {
			super.processMouseEvent(arg0);
		}
	}
}
