/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package net.bioclipse.spectrum.views;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.bioclipse.spectrum.Activator;
import net.bioclipse.spectrum.business.ISpectrumManager;
import net.bioclipse.spectrum.domain.JumboSpectrum;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.JFreeChart;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLSpectrum;

import spok.guicomponents.SpectrumChartFactory;
import spok.guicomponents.SpokChartPanel;
import spok.utils.SpectrumUtils;

/**
 * View displaying a JFreeChart based peak chart as an embedded swing container.
 * 
 * @author Tobias Helmus
 * @created 19. Dezember 2005
 * 
 */
public class SpectrumCompareView extends ViewPart implements ISelectionListener {
	
	public static final String ID = "net.bioclipse.spectrum.views.SpectrumCompareView";
	
	private ArrayList<Composite> controls=new ArrayList<Composite>();

    private Composite parent;
    
    private List<JFreeChart> charts=new ArrayList<JFreeChart>();

	public List<JFreeChart> getCharts() {
		return charts;
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		this.parent=parent;
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.verticalSpacing = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 2;
		parent.setLayout(layout);

		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace=true;
		parent.setLayoutData(layoutData);
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener("net.bioclipse.navigator",this);
	}

	public void setFocus() {
		parent.setFocus();
		parent.redraw();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if(!parent.isDisposed())
			this.reactOnSelection(selection);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();

	}

	private void cleanComposite()
	{
		for(int i=0;i<controls.size();i++)
		{
			Display.getCurrent().syncExec(new MyRunnable (controls.get(i)));
		}
		controls.clear();
	}
	
	  public List<CMLElement> getMatchingSearchShift(List<CMLElement> originalSpectrum, List<CMLElement> searchSpectrum) {
		    List<CMLElement> peakList = new ArrayList<CMLElement>();
		    if(searchSpectrum.size()==0 || originalSpectrum.size()==0)
		    	return peakList;
		    boolean signalsUsed[] = new boolean[originalSpectrum.size()];
		    double threshold=100/originalSpectrum.size();
		    if(searchSpectrum.size()>originalSpectrum.size())
		    	threshold=100/searchSpectrum.size();
		    for (int i = 0; i < searchSpectrum.size(); i++) {
		      int nearestSignal = -1;
		      double difference = Double.MAX_VALUE;
		      for (int m = 0; m < originalSpectrum.size(); m++) {
		        if (java.lang.Math.abs(((CMLPeak)originalSpectrum.get(m)).getXValue() - ((CMLPeak)searchSpectrum.get(i)).getXValue()) < threshold && java.lang.Math.abs(((CMLPeak)originalSpectrum.get(m)).getXValue() - ((CMLPeak)searchSpectrum.get(i)).getXValue()) < difference && signalsUsed[m] == false) {
		          difference = java.lang.Math.abs(((CMLPeak)originalSpectrum.get(m)).getXValue() - ((CMLPeak)searchSpectrum.get(i)).getXValue());
		          nearestSignal = m;
		        }
		      }
		      if(nearestSignal>-1){
		    	  signalsUsed[nearestSignal] = true;
		    	  peakList.add(searchSpectrum.get(i));
		    	  peakList.add(originalSpectrum.get(nearestSignal));
		      }
		    }
		    return (peakList);
	  }
	  
	  public void update(final ArrayList<CMLSpectrum> spectra, final double minx, final double maxx) {
		parent.getDisplay().syncExec(new Runnable(){ 
			public void run() {
				try{
					//look for matching peaks if more than one spectrum
					List<CMLElement> matches=new ArrayList<CMLElement>();
					if(spectra.size()>1)
					{
						matches=getMatchingSearchShift(SpectrumUtils.getPeakElements(spectra.get(0)),SpectrumUtils.getPeakElements(spectra.get(1)));
						for(int i=2;i<spectra.size();i++){
							matches=getMatchingSearchShift(matches,SpectrumUtils.getPeakElements(spectra.get(i)));
						}
					}
					cleanComposite();
					for(int i=0;i<spectra.size();i++){
						Composite peakChartcomposite = new Composite(parent, SWT.EMBEDDED);
						GridLayout layout = new GridLayout();
						peakChartcomposite.setLayout(layout);
						GridData layoutData = new GridData(GridData.FILL_BOTH);
						peakChartcomposite.setLayoutData(layoutData);
						Frame fileTableFrame = SWT_AWT.new_Frame(peakChartcomposite);
						fileTableFrame.setLayout(new BorderLayout());
						JFreeChart chart = SpectrumChartFactory.createPeakChart(spectra.get(i), matches, SpectrumCompareView.this,minx*.8,maxx*1.2);
						chart.getXYPlot().getDomainAxis().setUpperBound(maxx*1.2);
						chart.getXYPlot().getDomainAxis().setLowerBound(minx*.8);
						charts.add(chart);
						SpectrumCompareView.this.charts.add(chart);
						SpokChartPanel chartPanel = new SpokChartPanel(chart, "peak", spectra.get(i),null);
						fileTableFrame.add(chartPanel, BorderLayout.CENTER);
						controls.add(peakChartcomposite);
					}
					parent.layout(true,true);
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
		});
	}


	private void reactOnSelection(ISelection selection) {
		if (!selection.isEmpty()) {
			ISpectrumManager spectrumManager = Activator.getDefault()
			    .getJavaSpectrumManager();
			if (selection instanceof IStructuredSelection) {
					Iterator it=((IStructuredSelection)selection).iterator();
					ArrayList<CMLSpectrum> al=new ArrayList<CMLSpectrum>(1);
					double minx=Double.MAX_VALUE;
					double maxx=Double.MIN_VALUE;
					while(it.hasNext()){
						Object o=it.next();
						if (o instanceof IFile) {
							try {
								if(SpectrumUtils.isSpectrum( (IFile)o )){
									JumboSpectrum js=spectrumManager.loadSpectrum((IFile)o);
									if (SpectrumUtils.spectrumHasPeaks(js.getJumboObject())) {
										al.add(js.getJumboObject());
										List<CMLElement> peaks = SpectrumUtils.getPeakElements(js.getJumboObject());
										Iterator<CMLElement> it2 = peaks.iterator();
										double maxy=Double.MIN_VALUE;
										while (it2.hasNext()) {
											CMLPeak peak = (CMLPeak) it2.next();
											if (peak.getXValue() > maxx) {
												maxx = peak.getXValue();
											}
											if (peak.getXValue() < minx) {
												minx = peak.getXValue();
											}
											if(peak.getYValue() > maxy)
											    maxy = peak.getYValue();
										}
										//this is to have same y range and a label to ensure uniform 
										//width of graphics
                    it2 = peaks.iterator();
                    while (it2.hasNext()) {
                      CMLPeak peak = (CMLPeak) it2.next();
                      if(Double.isNaN( peak.getYValue() ))
                          peak.setYValue( 100 );
                      else
                          peak.setYValue( (peak.getYValue()/maxy)*100 );
                    }
                    if(js.getJumboObject().getPeakListElements().get(0)
                            .getPeakElements().get(0).getXUnits()==null)
                        js.getJumboObject().getPeakListElements().get(0)
                            .getPeakElements().get(0).setXUnits("Unspecified");
                    if(js.getJumboObject().getPeakListElements().get(0)
                            .getPeakElements().get(0).getYUnits()==null)
                        js.getJumboObject().getPeakListElements().get(0)
                            .getPeakElements().get(0).setYUnits("Unspecified");
									}
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					this.update(al,minx, maxx);
			}
		}
	}
	
	class MyRunnable implements Runnable{
		private Composite control;
		
		public MyRunnable(Composite control){
			this.control=control;
		}
		public void run () {
			control.dispose();
		}
	}
}
