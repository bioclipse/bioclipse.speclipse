/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package spok.guicomponents;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

import net.bioclipse.spectrum.editor.ChartPage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.WorkbenchPart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYBarDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLSpectrum;

import spok.utils.SpectrumUtils;

/**
 * Factory class for building spectrum charts
 * 
 * @author Stefan Kuhn
 * @created 4/Aug/08
 */
public class SpectrumChartFactory {



	
	/** The Buttons of the Chart Properties Popup Window */
	// public static JButton buttonSetChanges, buttonApply, buttonCancel;
	static double[] xData;

	static double[] yData;

	static double continuousUpperDomainBound;

	static double continuousLowerDomainBound;

	private static XYSeries SERIEShigh = null;
	private static XYSeries SERIESlow = null;

	private static JFreeChart lastPeakchart = null;
	public final static String LABELTHRESHOLD="labelthreshold";
	
	private static double minx;
	private static double maxx;

	/**
	 * Method for creating a peak chart.
	 * 
	 * @param spectrum  The SpokSpectrum for which the chart should be created
	 * @param matches	These peaks will be highlighted in the chart
	 * @param page		The SpectrumCompareView or ChartPage this chart is in
	 * @return the ChartPanel containing the peak chart
	 */
	public static JFreeChart createPeakChart(CMLSpectrum spectrum, List<CMLElement> matches, WorkbenchPart page) {
		return SpectrumChartFactory.createPeakChart(spectrum,matches,page,false);
	}
	
	/**
	 * Method for creating a peak chart.
	 * 
	 * @param spectrum  The SpokSpectrum for which the chart should be created
	 * @param matches	These peaks will be highlighted in the chart
	 * @param page		The SpectrumCompareView or ChartPage this chart is in
	 * @param minx
	 * @param maxx		The limits to be used for the chart (normally the peaks determine this, you can override this by these values)
	 * @return the ChartPanel containing the peak chart
	 */
	public static JFreeChart createPeakChart(CMLSpectrum spectrum, List<CMLElement> matches, WorkbenchPart page, double minx, double maxx) {
		SpectrumChartFactory.minx=minx;
		SpectrumChartFactory.maxx=maxx;
		return SpectrumChartFactory.createPeakChart(spectrum,matches,page,true);
	}

	/**
	 * Method for creating a peak chart.
	 * 
	 * @param spectrum  The SpokSpectrum for which the chart should be created
	 * @param matches	These peaks will be highlighted in the chart
	 * @param page		The SpectrumCompareView or ChartPage this chart is in
	 * @param externalLimits	Shall external limits be used?
	 * @return the ChartPanel containing the peak chart
	 */
	public static JFreeChart createPeakChart(CMLSpectrum spectrum, List<CMLElement> matches, IWorkbenchPart page, boolean externalLimits) {
		JFreeChart peakChart;
		String xAxisLabel = null;
		String yAxisLabel = null;
		try{
		if (spectrum != null) {
			if (spectrum.getPeakListElements().size() != 0) {
				//not sure how this can happen, but it does and causes npe
				if(spectrum.getPeakListElements().get(0).getPeakElements().get(0)!=null){
					CMLPeak peak = spectrum.getPeakListElements().get(0)
					.getPeakElements().get(0);
					xAxisLabel = peak.getXUnits();
					yAxisLabel = peak.getYUnits();
				}
			} else if ( spectrum.getSpectrumDataElements().size()>0){
				xAxisLabel = spectrum.getSpectrumDataElements().get(0)
						.getXaxisElements().get(0).getArrayElements().get(0)
						.getUnits();
				yAxisLabel = spectrum.getSpectrumDataElements().get(0)
						.getYaxisElements().get(0).getArrayElements().get(0)
						.getUnits();
			}else{
			    MessageBox mb = new MessageBox(page.getSite().getShell(),SWT.ICON_INFORMATION);
			    mb.setText( "No Data" );
			    mb.setMessage( "We found neither peaks nor continuous data in your spectrum. This might be a problem with the file or the parsing. We will still display, but don't be suprised!" );
			    mb.open();
			}
			if(xAxisLabel==null)
				xAxisLabel="";
			if(yAxisLabel==null)
				yAxisLabel="";
			//if label are namespaced, remove namespace
			if (xAxisLabel != null && xAxisLabel.indexOf(":") != -1) {
				xAxisLabel = xAxisLabel.substring(xAxisLabel.indexOf(":")+1);
				yAxisLabel = yAxisLabel.substring(yAxisLabel.indexOf(":")+1);
			}
			String title = getSpectrumTitle(spectrum);
			List<CMLElement> peaks = SpectrumUtils.getPeakElements(spectrum);
			if(peaks!=null){
				if(net.bioclipse.spectrum.Activator.getDefault().getPluginPreferences().isDefault(LABELTHRESHOLD))
					net.bioclipse.spectrum.Activator.getDefault().getPluginPreferences().setValue(LABELTHRESHOLD,-1);
				SERIEShigh = new XYSeries("high Peak");
				SERIESlow = new XYSeries("low Peak");
				boolean allYVal0 = checkIfAllYVal0(peaks);
				Iterator<CMLElement> it = peaks.iterator();
				while (it.hasNext()) {
					CMLPeak peak = (CMLPeak)it.next();
					int tresholdMultiplicator = 0;
					//dependent on the threshold sort peaks into to series - SERIEShigh will have the y-value as label
					if (net.bioclipse.spectrum.Activator.getDefault().getPluginPreferences().getInt(LABELTHRESHOLD) != -1) {
						tresholdMultiplicator = net.bioclipse.spectrum.Activator.getDefault().getPluginPreferences().getInt(LABELTHRESHOLD);
					}
					double threshold = (SpectrumUtils.getHighestY(spectrum)/100)*tresholdMultiplicator;
					if (!allYVal0 && peak.getYValue() > threshold || (Double.isNaN(peak.getYValue()) && net.bioclipse.spectrum.Activator.getDefault().getPluginPreferences().getInt(LABELTHRESHOLD) != -1)) {
						if(Double.isNaN(peak.getYValue()))
							SERIEShigh.add(peak.getXValue(), 1);
						else
							SERIEShigh.add(peak.getXValue(), peak.getYValue());
					}
					else {
						if(Double.isNaN(peak.getYValue()))
							SERIESlow.add(peak.getXValue(), 1);
						else if (allYVal0) {
							SERIEShigh.add(peak.getXValue(), 100);
						}
						else
							SERIESlow.add(peak.getXValue(), peak.getYValue());
					}
					
				}
				
				XYSeriesCollection collection = new XYSeriesCollection(SERIEShigh);
				collection.addSeries(SERIESlow);
				XYBarDataset dataset = new XYBarDataset(collection, 0);
				
				//create the chart
				peakChart = ChartFactory.createXYBarChart(title, xAxisLabel, false,
						yAxisLabel, dataset, PlotOrientation.VERTICAL, false, true,
						false);
				peakChart.setAntiAlias(false);
				XYPlot peakPlot = peakChart.getXYPlot();
				
				//highlight peaks which are in matches
				if (peaks != null && peaks.size() > 0) {
					for (int i=0; i<peaks.size(); i++) {
						if(matches!=null){
							for(int k=0;k<matches.size();k++){
								if(((CMLPeak)peaks.get(i)).getXValue()==((CMLPeak)matches.get(k)).getXValue()){
									CMLPeak peak = (CMLPeak)peaks.get(i);
									double xval = peak.getXValue();
									double yval = peak.getYValue();
									//if peak has no y-value assigned set y value to upperBound of the chart
									if (yval == 0 || Double.isNaN(yval)) {
										yval = peakPlot.getRangeAxis().getUpperBound();
									}
									//create the XYKLineAnnotation and add it to the plot
									XYLineAnnotation lineAnno = new XYLineAnnotation(xval, 0, xval, yval, new BasicStroke(1f), Color.GREEN);
									peakPlot.addAnnotation(lineAnno);
									break;
								}
							}
						}
					}
				}			
				
				//invert x-axis if nmr or ir spectrum			
				if (spectrum.getType()!=null && (spectrum.getType().equals(SpectrumUtils.NMRSPECTRUMTYPE) || spectrum.getType().equals(SpectrumUtils.IRSPECTRUMTYPE))) {
					peakPlot.getDomainAxis().setInverted(true);
				}
				//y-axis increase upperBound by 20%, so that the label of the highest peak can be seen as well...
				peakPlot.getRangeAxis().setUpperBound(peakPlot.getRangeAxis().getUpperBound() * 1.2);
				peakPlot.getDomainAxis().addChangeListener(externalLimits ?
						new SpectrumAxisListener(page, peakChart,minx,maxx) :
						new SpectrumAxisListener(page, peakChart));
				
				XYItemRenderer renderer = peakPlot.getRenderer();
				//paint both series in black, so they cant be distiguished anymore :)
				renderer.setSeriesPaint(0, Color.black);
				renderer.setSeriesPaint(1, Color.black);
				//make first Series item labels visible and hide second Series ones
				if (net.bioclipse.spectrum.Activator.getDefault().getPluginPreferences().getInt(LABELTHRESHOLD)!=-1){
					renderer.setItemLabelsVisible(null);
					renderer.setSeriesItemLabelsVisible(0, true);
					renderer.setSeriesItemLabelsVisible(1, false);
					XYItemLabelGenerator labelGenerator = new MyXYItemLabelGenerator("{2}", new DecimalFormat("0.00"), new DecimalFormat("0.00"));
					renderer.setItemLabelGenerator(labelGenerator);
					renderer.setItemLabelFont(new Font("Arial", Font.BOLD, 9));
					for (int i=0; i<SERIEShigh.getItemCount(); i++) {
						labelGenerator.generateLabel(dataset, 0, i);
					}
				}
				if (spectrum.getSpectrumDataElements() != null
						&& spectrum.getSpectrumDataElements().size() != 0) {
					peakPlot.getDomainAxis().setUpperBound(
							continuousUpperDomainBound);
					peakPlot.getDomainAxis().setLowerBound(
							continuousLowerDomainBound);
				}
				lastPeakchart = peakChart;
			} else{
				peakChart = ChartFactory.createXYBarChart(null, xAxisLabel, false,
						yAxisLabel, null, PlotOrientation.VERTICAL, false, true,
						false);
				lastPeakchart = null;
			}
			} else {
				peakChart = ChartFactory.createXYBarChart(null, xAxisLabel, false,
						yAxisLabel, null, PlotOrientation.VERTICAL, false, true,
						false);
				lastPeakchart = null;
			}
	}catch(Exception e){
		e.printStackTrace();
		return null;
	}
		return peakChart;
	}

	private static boolean checkIfAllYVal0(List<CMLElement> peaks) {
		Iterator<CMLElement> it = peaks.iterator();
		while (it.hasNext()) {
			CMLPeak peak = (CMLPeak) it.next();
			double yValue = peak.getYValue();
			if (yValue != 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Method for creating a continuous chart
	 * 
	 * @param cmlSpectrum
	 *            The CMLSpectrum for which the chart should be created
	 *            the Workbench page containing the ContinuousSpectrumView and
	 *            the PeakSpectrumView
	 * @param page		The SpectrumCompareView or ChartPage this chart is in
	 * @return the ChartPanel containing the continuous chart
	 * @throws CMLException
	 */
	public static JFreeChart createContinousChart(CMLSpectrum cmlSpectrum, ChartPage page) {
		JFreeChart continuousChart;
		String xAxisLabel = null;
		String yAxisLabel = null;
		if (cmlSpectrum != null && cmlSpectrum.getSpectrumDataElements().size()>0 && cmlSpectrum.getSpectrumDataElements().get(0).getXaxisElements().size()>0) {
			xAxisLabel = cmlSpectrum.getSpectrumDataElements().get(0)
					.getXaxisElements().get(0).getTitle();
			yAxisLabel = cmlSpectrum.getSpectrumDataElements().get(0)
			.getYaxisElements().get(0).getTitle();
			
//			if label are namespaced, remove namespace
			if (xAxisLabel != null && xAxisLabel.indexOf(":") != -1) {
				xAxisLabel = xAxisLabel.substring(xAxisLabel.indexOf(":")+1);
				yAxisLabel = yAxisLabel.substring(yAxisLabel.indexOf(":")+1);
			}
			
			String title = getSpectrumTitle(cmlSpectrum);
			double[] xDataArray = null;
			double[] yDataArray = null;
			xDataArray = cmlSpectrum.getSpectrumDataElements().get(0)
					.getXaxisElements().get(0).getArrayElements().get(0)
					.getDoubles();
			yDataArray = cmlSpectrum.getSpectrumDataElements().get(0)
					.getYaxisElements().get(0).getArrayElements().get(0)
					.getDoubles();

			XYSeries series = new XYSeries("Signal");
			for (int i = 0; i < xDataArray.length; i++) {
				series.add(xDataArray[i], yDataArray[i]);
			}

			XYDataset xyDataset = new XYSeriesCollection(series);
			continuousChart = ChartFactory.createXYLineChart(title, xAxisLabel,
					yAxisLabel, xyDataset, PlotOrientation.VERTICAL, false,
					true, false);
			continuousChart.setAntiAlias(false);

			XYPlot continuousPlot = continuousChart.getXYPlot();
			
//			invert x-axis if nmr or ir spectrum
			if (cmlSpectrum.getType().equals(SpectrumUtils.NMRSPECTRUMTYPE) || cmlSpectrum.getType().equals(SpectrumUtils.IRSPECTRUMTYPE)) {
				continuousPlot.getDomainAxis().setInverted(true);
			}
			
			continuousPlot.setRenderer(new StandardXYItemRenderer());
			continuousPlot.getDomainAxis().addChangeListener(
					new SpectrumAxisListener(page, continuousChart));
			continuousUpperDomainBound = continuousPlot.getDomainAxis()
					.getUpperBound();
			continuousLowerDomainBound = continuousPlot.getDomainAxis()
					.getLowerBound();
			syncronizeLastPeakChart();
		} else {
			continuousChart = ChartFactory.createXYBarChart(null, xAxisLabel,
					false, yAxisLabel, null, PlotOrientation.VERTICAL, false,
					true, false);

		}
		return continuousChart;
	}

	private static String getSpectrumTitle(CMLSpectrum spectrum) {
		String title;
		if (spectrum.getTitle() != null && spectrum.getTitle().length() > 0) {
			title = spectrum.getTitle();
		} else {
			title = spectrum.getId();
		}
		return title;
	}

	private static void syncronizeLastPeakChart() {
		if (lastPeakchart != null) {
			lastPeakchart.getXYPlot().getDomainAxis().setUpperBound(
					continuousUpperDomainBound);
			lastPeakchart.getXYPlot().getDomainAxis().setLowerBound(
					continuousLowerDomainBound);
		}
	}
}
