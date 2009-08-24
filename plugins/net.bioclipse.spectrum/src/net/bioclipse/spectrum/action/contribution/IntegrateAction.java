package net.bioclipse.spectrum.action.contribution;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import net.bioclipse.spectrum.editor.SpectrumEditor;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.xmlcml.cml.element.CMLSpectrum;
import org.xmlcml.cml.element.CMLSpectrumData;

import spok.utils.SpectrumUtils;

public class IntegrateAction extends Action {
	

	private SpectrumEditor view;

	public IntegrateAction() {
		URL url = Platform.getBundle(
		"net.bioclipse.spectrum").getEntry("/icons/integral.gif");
		ImageDescriptor imageDesc = ImageDescriptor.createFromURL(url);
		this.setImageDescriptor(imageDesc);
	}
	
	public void setActiveEditor(SpectrumEditor editor){
		this.view=editor;
		CMLSpectrum spectrum = view.getSpectrum();
		if (spectrum != null) {
			CMLSpectrumData spectrumData = SpectrumUtils.getSpectrumData(spectrum);
			if(spectrumData==null){
				this.setEnabled(false);
			}else{
				this.setEnabled(true);
			}
		}
	}
	
	@Override
	public void run() {
		CMLSpectrum spectrum = view.getSpectrum();
		if (spectrum != null) {
			CMLSpectrumData spectrumData = SpectrumUtils
			.getSpectrumData(spectrum);
			HashMap integralMap = doTrapezoidIntegration(spectrumData);
			XYSeriesCollection collection =  (XYSeriesCollection)view.getChartPage().getContinuousChartPanel().getChart().getXYPlot().getDataset();
			XYSeries series = new XYSeries("Integrals");
			Iterator it = integralMap.keySet().iterator();
			while (it.hasNext()) {
				double xval = ((Double) it.next()).doubleValue();
				double intVal = ((Double) integralMap.get(xval)).doubleValue();
				series.add(xval, intVal);
			}
			collection.addSeries(series);
			view.getPeakTablePage().setDirty(true);
		}
	}
	
	
	private HashMap doTrapezoidIntegration(CMLSpectrumData spectrumData) {
		HashMap<Double, Double> retMap = new HashMap<Double, Double>();
		double[] xDataArray = spectrumData.getXaxisElements().get(0).getArrayElements().get(0).getDoubles();
		double[] yDataArray = spectrumData.getYaxisElements().get(0).getArrayElements().get(0).getDoubles();
		if (xDataArray[0] < xDataArray[xDataArray.length-1]){
			xDataArray = correctArrayDirection(xDataArray);
			yDataArray = correctArrayDirection(yDataArray);
		}
		double area = 0.0;
		for (int i=xDataArray.length-1; i>0; i--) {
			double xVal = xDataArray[i];
			double xMinusOneVal = xDataArray[i-1];
			double xDiff = Math.abs(Math.abs(xVal) - Math.abs(xMinusOneVal));
			double yVal = Math.abs(yDataArray[i]);
			double yValMinusOne = Math.abs(yDataArray[i-1]);
			double middleVal = (xMinusOneVal + xVal) / 2.0 ;
			area = (xDiff * (yVal + yValMinusOne) / 2.0) + area;
			retMap.put(new Double(middleVal), new Double(area));
		}
		return retMap;
	}

	private double[] correctArrayDirection(double[] dataArray) {
		int n = dataArray.length;
		double[] array = new double[n];
		int j = dataArray.length-1;
		for (int i=0; i<n; i++) {
			array[j] = dataArray[i];
			j = j-1;
		}
		return array;
	}
	@Override
	public String getToolTipText() {
		return "Calculate Integrals";
	}
}