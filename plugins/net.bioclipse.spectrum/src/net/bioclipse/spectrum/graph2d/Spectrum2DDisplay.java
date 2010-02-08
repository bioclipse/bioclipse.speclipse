package net.bioclipse.spectrum.graph2d;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.xml.sax.SAXException;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLSpectrum;

public class Spectrum2DDisplay extends EditorPart {
	
	CMLSpectrum spectrum;
	Frame fileTableFrame;
	private boolean isDirty=false;
	
	@Override	
	public void createPartControl(Composite parent) {
		Composite contChartcomposite = new Composite(parent, SWT.EMBEDDED);
		FillLayout layout = new FillLayout(SWT.VERTICAL);
		contChartcomposite.setLayout(layout);
		fileTableFrame = SWT_AWT.new_Frame(contChartcomposite);
		fileTableFrame.add(this.makeGraph());
	}
	
	public Graph2D makeGraph(){
	      Graph2D graph;
	      DataSet data1;
	      Axis    xaxis;
	      Axis    yaxis_left;
	       int i;
	       int j;
	       List<CMLPeak> peaks = spectrum.getPeakListElements().get(0).getPeakChildren();
	       double data[] = new double[2*peaks.size()];
	       graph = new Graph2D();
	       graph.drawzero = false;
	       graph.drawgrid = false;
	       graph.setDataBackground(Color.WHITE);
	       graph.setGraphBackground(Color.WHITE);
	       try {
	          graph.setMarkers(new Markers());
	       } catch(Exception e) {
	          System.out.println("Failed to create Marker URL!");
	       }
	       for(i=j=0; i<peaks.size(); i++,j+=2) {
    		   data[j]=peaks.get(i).getXValue();  
    		   data[j+1]=peaks.get(i).getYValue();
	       }
	       data1 = graph.loadDataSet(data,peaks.size());
	       data1.linestyle = 0;
	       data1.marker    = 1;
	       data1.markerscale = 1.5;
	       data1.markercolor = new Color(0,0,255);
	       xaxis = graph.createAxis(Axis.BOTTOM);
	       xaxis.attachDataSet(data1);
	       xaxis.setTitleFont(new Font("TimesRoman",Font.PLAIN,20));
	       xaxis.setLabelFont(new Font("Helvetica",Font.PLAIN,15));
	       yaxis_left = graph.createAxis(Axis.LEFT);
	       yaxis_left.attachDataSet(data1);
	       yaxis_left.setTitleFont(new Font("TimesRoman",Font.PLAIN,20));
	       yaxis_left.setLabelFont(new Font("Helvetica",Font.PLAIN,15));
	       yaxis_left.setTitleColor( new Color(0,0,255) );
	       return graph;
	}

	@Override
	public void doSave(IProgressMonitor arg0) {
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
		return false;
	}

	@Override
	public void setFocus() {
	}

	
	public void setSpectrumItem(CMLSpectrum spectrumItem) throws ParserConfigurationException, SAXException, IOException{
		if(spectrumItem!=null){
			this.spectrum=spectrumItem;
		}
	}
	
	
    public void setDirty(boolean bool) {
        this.isDirty = bool;
        firePropertyChange(PROP_DIRTY);
    }

	public void update() {
		fileTableFrame.remove(fileTableFrame.getComponent(0));
		fileTableFrame.add(this.makeGraph());
		fileTableFrame.validate();
	}
}
