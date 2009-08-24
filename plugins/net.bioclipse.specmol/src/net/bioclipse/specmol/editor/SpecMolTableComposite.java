/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.specmol.editor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.bioclipse.specmol.listeners.SpecMolListener;
import net.bioclipse.spectrum.editor.MetadataUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlcml.cml.base.CMLElements;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;
import org.xmlcml.cml.element.CMLSpectrum;

public class SpecMolTableComposite extends Composite implements SelectionListener, SpecMolListener{

	private static Table table;
	private String[] columnNames = new String[4];
	private static AssignmentPage page;
	static DecimalFormat df = new DecimalFormat("#.00");
	static DecimalFormat dfint = new DecimalFormat("#");
	private final String X_AXIS = "xaxis";
	private final String Y_AXIS = "yaxis";
	public static final String ATOMS="atoms";
	public static final String PREDICTION="min-mean-max(median,spheres,values used)";
	private Display display = getShell().getDisplay();
	private Label spectypelabel;
	
	public SpecMolTableComposite(Composite parent, int style, AssignmentPage page) {
		super(parent, style);
		columnNames[0]=X_AXIS;
		columnNames[1]=Y_AXIS;
		columnNames[2]=ATOMS;
		columnNames[3]=PREDICTION;
		init(page);
		SpecMolTableComposite.page = page;
	}

	private void init(AssignmentPage page) {
		GridLayout layout=new GridLayout(1, false);
		layout.horizontalSpacing=0;
		layout.verticalSpacing=0;
		this.setLayout(layout);
		spectypelabel=new Label(this, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		spectypelabel.setLayoutData(gridData);
		table = new Table(this, SWT.NONE);
		table.addSelectionListener(this);
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = SWT.FILL;
		gridData2.grabExcessHorizontalSpace = true;
		gridData2.verticalAlignment = SWT.FILL;
		gridData2.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData2);		
			
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		TableColumn[] column=new TableColumn[columnNames.length];
		for(int i=0;i<columnNames.length;i++){
			column[i]= new TableColumn(table, SWT.CENTER, i);		
			column[i].setText(columnNames[i]);
		}
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		AssignmentController assContr = page.getAssignmentController();
		ArrayList<CMLPeak> list = new ArrayList<CMLPeak>();
		int[] selectedRows = table.getSelectionIndices();
		for (int i=0; i< selectedRows.length; i++) {
			list.add(page.getCurrentSpectrum().getPeakListElements().get(0).getPeakElements().get(selectedRows[i]));
		}
		assContr.setSelection(list, this);		
	}

	public void updateTable(CMLSpectrum spectrum, CMLSpectrum predictedSpectrum) {
		if (table != null) {
			table.removeAll();
			if(spectrum.getType()!=null){
				try{
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					DocumentBuilder builder = factory.newDocumentBuilder();
					Document document = builder.parse(this.getClass().getClassLoader().getResourceAsStream("net/bioclipse/plugins/bc_spectrum/views/peakTable/spec.xml"));
					NodeList nl = document.getElementsByTagName("spectrumtypes");
					nl=nl.item(0).getChildNodes();
					for(int i=0;i<nl.getLength();i++){
						String type="";
						NodeList mySiblings = nl.item(i).getChildNodes();
						for (int k = 0; k < mySiblings.getLength(); k++) {
							Node currentSibling = mySiblings.item(k);
							if (currentSibling.getNodeName().equals("cmltype")) {
								type=(currentSibling.getTextContent());
							}
						}
						if(spectrum.getType().equals(type)){
							for (int k = 0; k < mySiblings.getLength(); k++) {
								Node currentSibling = mySiblings.item(k);
								if (currentSibling.getNodeName().equals("xaxis")) {
									table.getColumn(0).setText(currentSibling.getAttributes().getNamedItem("name").getNodeValue());
									columnNames[0] = table.getColumn(0).getText();
								} else if (currentSibling.getNodeName().equals("yaxis")) {
									table.getColumn(1).setText(currentSibling.getAttributes().getNamedItem("name").getNodeValue());
									columnNames[1] = table.getColumn(1).getText();
								} 
							}
						}
					}
				}catch(Exception ex){
					//do not worry, just the setting of column names failed
				}
				spectypelabel.setText(spectrum.getType());
				List<CMLMetadata> nucleusmetadatas = MetadataUtils.getMetadataDescendantsByName(MetadataUtils.getAllInOneMetadataList(spectrum).getMetadataDescendants(),SpecMolEditor.nucleus);
				if(nucleusmetadatas.size()>0){
					spectypelabel.setText(spectypelabel.getText()+" "+nucleusmetadatas.get(0).getContent());
				}
				
			}
			if (spectrum.getPeakListElements() != null && spectrum.getPeakListElements().size() > 0) {
				CMLPeakList peakTable=spectrum.getPeakListElements().get(0);
				if (peakTable != null) {
					for(int i=0;i<peakTable.getPeakElements().size();i++){
					  TableItem item1 = new TableItem(table,0);
					  double xVal = peakTable.getPeakElements().get(i).getXValue();
					  double yVal = peakTable.getPeakElements().get(i).getYValue();
					  if(Double.isNaN(yVal))
						  yVal=1;
					  String refs = null;
					  if (peakTable.getPeakElements().get(i).getAtomRefsAttribute() != null) {
						  refs = peakTable.getPeakElements().get(i).getAtomRefsAttribute().getValue();
					  }
					  StringBuffer prediction=new StringBuffer("");
					  if(predictedSpectrum!=null && peakTable.getPeakElements().get(i).getAtomRefsAttribute()!=null){
						  CMLPeak peak=getCMLPeakByAtomId(predictedSpectrum.getPeakListElements().get(0).getPeakElements(),peakTable.getPeakElements().get(i).getAtomRefsAttribute().getValue());
						  prediction.append(df.format(peak.getXMin())+"-"+df.format(peak.getXValue())+"-"+df.format(peak.getXMax())+" ("+df.format(peak.getXWidth())+", "+dfint.format(peak.getYMin())+", "+dfint.format(peak.getYMax())+")");
						  if(xVal<peak.getXMin() || xVal>peak.getXMax())
							  item1.setBackground(new org.eclipse.swt.graphics.Color(this.getDisplay(),255,1,1));
					  }
					  item1.setText(new String[]{df.format(xVal), df.format(yVal)+"", refs,prediction.toString()});
					}
				}
			}
			TableColumn[] columns = table.getColumns();
			for (int i = 0; i < columns.length; i++) {
				columns[i].pack();
			}
			//TODO in case of predictedSpectrum!=null, it would be nice to make the table broader
			table.redraw();
		}
	}

	private CMLPeak getCMLPeakByAtomId(CMLElements<CMLPeak> peakElements,
			String value) {
		StringTokenizer st=new StringTokenizer(value);
		while(st.hasMoreTokens()){
			String atomid=st.nextToken();
			Iterator<CMLPeak> it=peakElements.iterator();
			while(it.hasNext()){
				CMLPeak peak=it.next();
				if(Arrays.asList(peak.getAtomRefs()).contains(atomid)){
					return peak;
				}
			}
		}
		return null;
	}

	public void selectionChanged(final AssignmentController controller) {
		display.syncExec(new Runnable() {
			public void run() {
				SpecMolTableComposite.table.deselectAll();
				ArrayList<CMLPeak> peaks = controller.getSelectedPeaks();
				ArrayList<TableItem> tableItems = new ArrayList<TableItem>();
				for (int j=0; j<peaks.size(); j++) {
					CMLPeak peak = peaks.get(j);
					for (int i=0; i<table.getItemCount(); i++) {
						if (table.getItem(i).getText(0).equals(SpecMolTableComposite.df.format(peak.getXValue()))) {
							tableItems.add(table.getItem(i));
							break;
						}
					}
				}
				TableItem[] items = new TableItem[tableItems.size()];
				for (int k=0; k<tableItems.size(); k++) {
					items[k] = tableItems.get(k);
				}
				table.setSelection(items);
			}
		});	
	}

	public void unselect() {
		display.syncExec(new Runnable() {
			public void run() {
				SpecMolTableComposite.table.deselectAll();
				if (SpecMolTableComposite.page.getAssignmentController().getSelectedPeaks() != null) {
					SpecMolTableComposite.page.getAssignmentController().getSelectedPeaks().clear();
				}
			}
		});	
	}

}
