/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package net.bioclipse.spectrum.editor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;
import org.xmlcml.cml.element.CMLSpectrum;

import spok.utils.SpectrumUtils;

/**
 * @author Stefan Kuhn
 */

public class PeakTableViewer {
	private static final Logger logger = Logger.getLogger(PeakTableViewer.class);

	HashMap<String,Node> spectypemap = new HashMap<String,Node>();

	Table table;

	TableViewer tableViewer;

	String[] cmlPeakFields = { "", "", "", "" };

	private final String X_AXIS = "xaxis";

	private final String Y_AXIS = "yaxis";

	private String[] columnNames = new String[6];

	CMLSpectrum spectrumItem = null;

	public static final String INVALID_COLUMN = "---";

	int highestColumn = 1;

	PeakTablePage peakTablePage;
	public int[] hashesofpeaks;

	public CMLSpectrum getSpectrumItem() {
		return spectrumItem;
	}

	public void setSpectrumItem(CMLSpectrum spectrumItem) {
		this.spectrumItem = spectrumItem;
	}

	public PeakTableViewer(Composite parent, CMLSpectrum spectrumItem, PeakTablePage peakTablePage) {
		try {
			columnNames[0] = X_AXIS;
			columnNames[1] = Y_AXIS;
			columnNames[2] = INVALID_COLUMN;
			;
			columnNames[3] = INVALID_COLUMN;
			columnNames[4] = INVALID_COLUMN;
			columnNames[5] = INVALID_COLUMN;
			this.addChildControls(parent);
			this.setSpectrumItem(spectrumItem);
			this.peakTablePage=peakTablePage;
		} catch (Exception ex) {
			StringWriter strWr = new StringWriter();
			PrintWriter prWr = new PrintWriter(strWr);
			ex.printStackTrace(prWr);
			logger.debug("problems creating PeakTableViewer "
					+ strWr.toString());
		}
	}

	public TableViewer getTableViewer() {
		return tableViewer;
	}

	public String[] getCmlPeakFields() {
		return cmlPeakFields;
	}

	public Table getTable() {
		return table;
	}

	public void dispose() {
		// Tell the label provider to release its ressources
		tableViewer.getLabelProvider().dispose();
	}

	private void addChildControls(Composite composite) throws Exception {

		// Create a composite to hold the children
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.FILL_BOTH);
		composite.setLayoutData(gridData);

		// Set numColumns to 3 for the buttons
		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 4;
		composite.setLayout(layout);

		// Create the table
		createTable(composite);
		// Create and setup the TableViewer
		createTableViewer();
		tableViewer.setContentProvider(new PeakContentProvider());
		tableViewer.setLabelProvider(new PeakLabelProvider(this));
		// Set the cell modifier for the viewer
		tableViewer.setCellModifier(new PeakCellModifier(this));
		// Set the default sorter for the viewer
		// tableViewer.setSorter(new PeakSorter(X_AXIS,this,false));
		CMLSpectrum spectrum = new CMLSpectrum();
		tableViewer.setInput(spectrum);

		// Add the buttons
		createButtons(composite);
	}

	private void createTable(Composite parent) {
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.HIDE_SELECTION | SWT.RESIZE;

		table = new Table(parent, style);

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 3;
		table.setLayoutData(gridData);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		TableColumn[] column = new TableColumn[columnNames.length];

		for (int i = 0; i < columnNames.length; i++) {
			column[i] = new TableColumn(table, SWT.CENTER, i);
			column[i].setText(columnNames[i]);
			column[i].setWidth(100);
			column[i].addSelectionListener(new MySelectionAdapter(i, this));
		}
	}

	private void createTableViewer() {

		tableViewer = new TableViewer(table);
		tableViewer.setUseHashlookup(true);

		tableViewer.setColumnProperties(columnNames);

		// Create the cell editors
		CellEditor[] editors = new CellEditor[columnNames.length + 4];

		// Column 2 : X_AXIS (Free text)
		TextCellEditor xEditor = new TextCellEditor(table);
		((Text) xEditor.getControl()).setTextLimit(60);
		((Text) xEditor.getControl()).addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event event) {
				if (event.detail == SWT.TRAVERSE_TAB_PREVIOUS
						|| event.detail == SWT.TRAVERSE_ARROW_PREVIOUS) {
					if (table.getSelectionIndex() > 0)
						tableViewer.editElement(tableViewer.getElementAt(table
								.getSelectionIndex() - 1), highestColumn);
				}
				if (event.detail == SWT.TRAVERSE_TAB_NEXT
						|| event.detail == SWT.TRAVERSE_ARROW_NEXT) {
					tableViewer.editElement(tableViewer.getElementAt(table
							.getSelectionIndex()), 1);
				}
			}
		});
		editors[0] = xEditor;

		// Column 3 : Y_AXIS ;
		TextCellEditor yEditor = new TextCellEditor(table);
		((Text) yEditor.getControl()).setTextLimit(60);
		((Text) yEditor.getControl()).addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event event) {
				if (event.detail == SWT.TRAVERSE_TAB_PREVIOUS
						|| event.detail == SWT.TRAVERSE_ARROW_PREVIOUS) {
					tableViewer.editElement(tableViewer.getElementAt(table
							.getSelectionIndex()), 0);
					// logger.debug(table.getSelectionIndex());
				}
				if (event.detail == SWT.TRAVERSE_TAB_NEXT
						|| event.detail == SWT.TRAVERSE_ARROW_NEXT) {
					if (highestColumn == 1
							&& table.getSelectionIndex() < ((CMLSpectrum) tableViewer
									.getInput()).getPeakListElements().get(0).getChildCount() - 1)
						tableViewer.editElement(tableViewer.getElementAt(table
								.getSelectionIndex() + 1), 0);
					if (highestColumn > 1)
						tableViewer.editElement(tableViewer.getElementAt(table
								.getSelectionIndex()), 2);
				}
			}
		});
		editors[1] = yEditor;

		for (int i = 0; i < 4; i++) {
			TextCellEditor cellEditor = new TextCellEditor(table);
			((Text) cellEditor.getControl()).setTextLimit(60);
			((Text) cellEditor.getControl()).addListener(SWT.Traverse,
					new MyListener(2 + i));
			editors[2 + i] = cellEditor;

		}
		// Assign the cell editors to the viewer
		tableViewer.setCellEditors(editors);

	}

	public class MyListener implements Listener {
		private int myColumn = 0;

		public MyListener(int myColumn) {
			this.myColumn = myColumn;
		}

		public void handleEvent(Event event) {
			if (event.detail == SWT.TRAVERSE_TAB_PREVIOUS
					|| event.detail == SWT.TRAVERSE_ARROW_PREVIOUS) {
				// if(table.getSelectionIndex()> n)
				tableViewer.editElement(tableViewer.getElementAt(table
						.getSelectionIndex()), myColumn - 1);
			}
			if (event.detail == SWT.TRAVERSE_TAB_NEXT
					|| event.detail == SWT.TRAVERSE_ARROW_NEXT) {
				if (highestColumn == myColumn
						&& table.getSelectionIndex() < ((CMLSpectrum) tableViewer
								.getInput()).getPeakListElements().get(0).getChildCount() - 1)
					tableViewer.editElement(tableViewer.getElementAt(table
							.getSelectionIndex() + 1), 0);
				if (highestColumn > myColumn)
					tableViewer.editElement(tableViewer.getElementAt(table
							.getSelectionIndex()), myColumn + 1);
			}
		}

	}

	public class MySelectionAdapter extends SelectionAdapter {
		private int columnNumber;

		private boolean reverseSort = false;

		private PeakTableViewer peakTableViewer;

		public MySelectionAdapter(int columnNumber,
				PeakTableViewer peakTableViewer) {
			this.columnNumber = columnNumber;
			this.peakTableViewer = peakTableViewer;
		}

		public void widgetSelected(SelectionEvent e) {
			try {
				tableViewer.setSorter(new PeakSorter(columnNames[columnNumber],
						peakTableViewer, reverseSort));
				// reverseSort = reverseSort?false:true;
				if (reverseSort) {
					reverseSort = false;
				} else {
					reverseSort = true;
				}
			} catch (Exception ex) {
				StringWriter strWr = new StringWriter();
				PrintWriter prWr = new PrintWriter(strWr);
				ex.printStackTrace(prWr);
				logger.error(strWr.toString());
			}
		}
	}

	public class PeakContentProvider implements IStructuredContentProvider {

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		// Return the peaks as an array of Objects
		public Object[] getElements(Object parent) {
			CMLSpectrum cpl = (CMLSpectrum) tableViewer.getInput();
			List<CMLElement> peaks = SpectrumUtils.getPeakElements(cpl);
			return peaks.toArray();
		}
	}

	public void configureFromXMLFile()
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		//TODO do parsing only once, code duplication with MetadataModifyListener
		Document document = builder
				.parse(this
						.getClass()
						.getClassLoader()
						.getResourceAsStream(
								"net/bioclipse/spectrum/editor/spec.xml"));
		NodeList nl = document.getElementsByTagName("spectrumtypes");
		Node spectrumtypes = nl.item(nl.getLength() - 1);
		NodeList spectrumtypelist = spectrumtypes.getChildNodes();
		Node unknownnode=null;
		for (int i = 0; i < spectrumtypelist.getLength(); i++) {
			Node spectrumtype = spectrumtypelist.item(i);
			if (spectrumtype.getNodeType() != Node.TEXT_NODE) {
				spectypemap.put(spectrumtype.getAttributes().getNamedItem(
						"cmltype").getNodeValue(), spectrumtype);
				if(spectrumtype.getAttributes().getNamedItem("name").getNodeValue().equals("unknown"))
					unknownnode=spectrumtype;
			}
		}	
		Node myNode = (Node) spectypemap.get(spectrumItem.getType());
		if(myNode==null){
			myNode=unknownnode;
		}
		NodeList mySiblings = myNode.getChildNodes();
		for (int k = 0; k < mySiblings.getLength(); k++) {
			Node currentSibling = mySiblings.item(k);
			if (currentSibling.getNodeName().equals("xaxis")) {
				table.getColumn(0).setText(
						currentSibling.getAttributes()
								.getNamedItem("name")
								.getNodeValue());
				columnNames[0] = table.getColumn(0).getText();
			} else if (currentSibling.getNodeName().equals(
					"yaxis")) {
				table.getColumn(1).setText(
						currentSibling.getAttributes()
								.getNamedItem("name")
								.getNodeValue());
				columnNames[1] = table.getColumn(1).getText();
			} else if (currentSibling.getNodeName().equals(
					"additionalFields")) {
				NodeList fields = currentSibling
						.getChildNodes();
				int m = 0;
				for (int l = 0; l < fields.getLength(); l++) {
					if (fields.item(l).getNodeType() != Node.TEXT_NODE) {
						Node field = fields.item(l);
						table.getColumn(2 + m).setText(
								field.getAttributes()
										.getNamedItem("name")
										.getNodeValue());
						table.getColumn(2 + m).setWidth(100);
						cmlPeakFields[m] = field
								.getAttributes().getNamedItem(
										"cmlpeakfield")
								.getNodeValue();
						columnNames[2 + m] = field
								.getAttributes().getNamedItem(
										"name").getNodeValue();
						m++;
					}
				}
				highestColumn = 1 + m;
				for (int l = m; l < 4; l++) {
					table.getColumn(2 + l).setText(
							INVALID_COLUMN);
					table.getColumn(2 + l).setWidth(100);
					cmlPeakFields[l] = "";
					columnNames[2 + l] = INVALID_COLUMN;
				}
			}
		}
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumn(i).pack();
		}			
		tableViewer.setInput(spectrumItem);
		hashesofpeaks=new int[spectrumItem.getPeakListElements().get(0).getChildCount()];
		CMLPeak[] array = new CMLPeak[spectrumItem.getPeakListElements().get(0).getChildCount()];
    for (int i = 0; i < hashesofpeaks.length; i++) {
      array[i] = (CMLPeak) spectrumItem.getPeakListElements().get(0).getPeakElements().get(i);
      if(array[i]!=null)
          hashesofpeaks[i]=array[i].hashCode();
    }
	}

	// Add the "Add", "Delete", "Copy to clipboard" buttons
	private void createButtons(final Composite parent) throws Exception {

		// Create and configure the "Add" button
		Button add = new Button(parent, SWT.PUSH | SWT.CENTER);
		add.setText("Add");

		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.widthHint = 80;
		add.setLayoutData(gridData);
		add.addSelectionListener(new SelectionAdapter() {
			// Add a peakvalue to the PeakTable and refresh the view
			public void widgetSelected(SelectionEvent e) {
				CMLPeak peak = new CMLPeak();
				peak.setXValue(0);
				peak.setYValue(0);
				if(((CMLSpectrum) tableViewer.getInput()).getPeakListElements().size()==0)
					((CMLSpectrum) tableViewer.getInput()).addPeakList(new CMLPeakList());
				((CMLSpectrum) tableViewer.getInput()).getPeakListElements().get(0).addPeak(peak);
				tableViewer.add(peak);
				CMLPeak[] array = new CMLPeak[spectrumItem.getPeakListElements().get(0).getChildCount()];
				hashesofpeaks=new int[spectrumItem.getPeakListElements().get(0).getChildCount()];
				for (int i = 0; i < array.length; i++) {
					array[i] = (CMLPeak) spectrumItem.getPeakListElements().get(0).getPeakElements().get(i);
					hashesofpeaks[i]=array[i].hashCode();
				}
				tableViewer.setSelection(new StructuredSelection(peak));
				peakTablePage.setDirty(true);
			}
		});
		// Create and configure the "Delete" button
		Button delete = new Button(parent, SWT.PUSH | SWT.CENTER);
		delete.setText("Delete");
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.widthHint = 80;
		delete.setLayoutData(gridData);

		delete.addSelectionListener(new SelectionAdapter() {

			// Remove the selection and refresh the view
			public void widgetSelected(SelectionEvent e) {
				CMLPeak task = (CMLPeak) ((IStructuredSelection) tableViewer
						.getSelection()).getFirstElement();
				if (task != null) {
					tableViewer.remove(task);
					CMLSpectrum spectrum = ((CMLSpectrum) tableViewer.getInput());
					spectrum.getPeakListElements().get( 0 ).removeChild(task);
					peakTablePage.setDirty(true);
				}
			}
		});
    // Create and configure the "export" button
    Button export = new Button(parent, SWT.PUSH | SWT.CENTER);
    export.setText("Copy to clipboard");
    gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
    gridData.widthHint = 180;
    export.setLayoutData(gridData);
    export.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
          List<CMLElement> peaks = SpectrumUtils.getPeakElements( spectrumItem );
          StringBuffer sb=new StringBuffer();
          for(int i=0;i<peaks.size();i++){
              sb.append( ((CMLPeak)peaks.get( i )).getXValue()+" "+((CMLPeak)peaks.get( i )).getYValue()+"\r\n" );
          }
          Clipboard clipboard = new Clipboard(PeakTableViewer.this.getControl().getDisplay());
          TextTransfer textTransfer = TextTransfer.getInstance();
          clipboard.setContents(new String[]{sb.toString()}, new Transfer[]{textTransfer});
          clipboard.dispose();
      }
    });

	}

	public java.util.List<String> getColumnNames() {
		return Arrays.asList(columnNames);
	}

	public ISelection getSelection() {
		return tableViewer.getSelection();
	}

	/**
	 * Return the parent composite
	 */
	public Control getControl() {
		return table.getParent();
	}

}
