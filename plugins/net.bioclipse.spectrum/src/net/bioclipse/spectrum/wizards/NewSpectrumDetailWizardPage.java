/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package net.bioclipse.spectrum.wizards;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;

import nu.xom.Builder;
import nu.xom.Element;
import nu.xom.Elements;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.xmlcml.cml.base.CMLElements;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;

import au.com.bytecode.opencsv.CSVReader;

public class NewSpectrumDetailWizardPage extends WizardPage {

	private Combo spectrumTypeCombo;

	private TableViewer tableViewer;

	public static final String MASS = "Mass Spectrum";

	public static final String NMR = "NMR Spectrum";

	public static final String IR = "IR Spectrum";

	public static final String UV = "UV Spectrum";
	
	HashMap<String, Element> spectypemap = new HashMap<String, Element>();
	
	static Table table =null; 
	
	private boolean peakdone=false;

  protected String separator;

	protected NewSpectrumDetailWizardPage() {
		super("SpectrumDetailWizardPage");
		setTitle("New Spectrum Detail Wizard");
		setDescription("This wizard lets you select the Spectrum type and gives you the possibiliy to add peaks");
	}

	public void createControl(Composite parent) {
		try{
			Builder builder = new nu.xom.Builder();
			nu.xom.Document doc = builder.build(this
							.getClass()
							.getClassLoader()
							.getResourceAsStream(
									"net/bioclipse/spectrum/editor/spec.xml"));
			Element docRoot = doc.getRootElement();
			Elements specTypeElements = docRoot.getChildElements("spectrumtype");
			for (int i=0; i<specTypeElements.size(); i++) {
				Element specTypeElement = specTypeElements.get(i);
				String name = specTypeElement.getAttribute("name").getValue();
				spectypemap.put(name, specTypeElement);
			}

		}catch(Exception ex){
			ex.printStackTrace();
			//don't worry, only columns headers not working  
		}
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		layout.verticalSpacing = 9;

		Label spectrumTypeLabel = new Label(container, SWT.None);
		spectrumTypeLabel.setText("Please select the Spectrum type:");

		spectrumTypeCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		Object[] array = spectypemap.keySet().toArray();
		String[] keyArray = new String[array.length];
		for (int i=0; i<array.length; i++) {
			keyArray[i] = (String) array[i];
		} 
		spectrumTypeCombo.setItems(keyArray);
		spectrumTypeCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
						Element selection = spectypemap.get(((Combo) e.getSource()).getText());
						Element xaxisNode = selection.getChildElements("xaxis").get(0);
						Element yaxisNode = selection.getChildElements("yaxis").get(0);
						table.getColumn(0).setText(xaxisNode.getAttributeValue("name"));
						table.getColumn(1).setText(yaxisNode.getAttributeValue("name"));
						dialogChanged();
				} catch (Exception ex) {
					//don't worry, only column headers not working
				}
			}
		});
		spectrumTypeCombo.select(0);
		Label peakListLabel = new Label(container, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		peakListLabel.setLayoutData(gd);
		peakListLabel.setText("Peak List:");

		Composite comp = new Composite(container, SWT.None);
		RowLayout innerLayout = new RowLayout(SWT.VERTICAL);
		comp.setLayout(innerLayout);

		String[] columnNames = new String[2];
		columnNames[0] =  spectypemap.get(spectrumTypeCombo.getText()).getChildElements("xaxis").get(0).getAttributeValue("name");
		columnNames[1] =  spectypemap.get(spectrumTypeCombo.getText()).getChildElements("yaxis").get(0).getAttributeValue("name");
		
			
		
		table = createTable(comp, columnNames);
		tableViewer = createTableViewer(table, columnNames);
		tableViewer.setContentProvider(new PeakContentProvider());
		tableViewer.setLabelProvider(new PeakLabelProvider());
		
		CMLPeakList peakList = new CMLPeakList();
		for (int i = 0; i < 5; i++) {
			CMLPeak peak = new CMLPeak();
			peak.setXValue(0);
			peak.setYValue(0);
			peakList.addPeak(peak);
		}
		tableViewer.setInput(peakList);

		Composite buttonComp = new Composite(comp, SWT.NONE);
		
		RowLayout buttonLayout = new RowLayout(SWT.HORIZONTAL);
		buttonComp.setLayout(buttonLayout);
		createButtons(buttonComp, tableViewer);

		setControl(container);
		dialogChanged();
	}

	private void createButtons(final Composite parent,
			final TableViewer tableViewer) {

		// Create and configure the "Add" button
		Button add = new Button(parent, SWT.PUSH | SWT.CENTER);
		add.setText("Add Peak");

		add.addSelectionListener(new SelectionAdapter() {
			// Add a peakvalue to the PeakTable and refresh the view
			public void widgetSelected(SelectionEvent e) {
				CMLPeak peak = new CMLPeak();
				peak.setXValue(0);
				peak.setYValue(0);
				((CMLPeakList) tableViewer.getInput()).addPeak(peak);
				int elements = tableViewer.getTable().getItemCount();
				tableViewer.getTable().select(elements - 1);
			}
		});
		// Create and configure the "Delete" button
		Button delete = new Button(parent, SWT.PUSH | SWT.CENTER);
		delete.setText("Delete Peak");

		delete.addSelectionListener(new SelectionAdapter() {

			// Remove the selection and refresh the view
			public void widgetSelected(SelectionEvent e) {
				CMLPeak task = (CMLPeak) ((IStructuredSelection) tableViewer
						.getSelection()).getFirstElement();
				if (task != null) {
					tableViewer.remove(task);
					((CMLPeakList) tableViewer.getInput()).removeChild(task);
				}
			}
		});
    // Create and configure the "Inser CSV" button
    Button insertcsv = new Button(parent, SWT.PUSH | SWT.CENTER);
    insertcsv.setText("Insert CSV list");

    insertcsv.addSelectionListener(new SelectionAdapter() {

      // Remove empty entries, add csv values and refresh the view
      public void widgetSelected(SelectionEvent e) {
        CMLPeakList peakList = getPeakList();
        CMLElements<CMLPeak> peaks = peakList.getPeakElements();
        Iterator<CMLPeak> it = peaks.iterator();
        while (it.hasNext()) {
          CMLPeak peak = it.next();
          if (peak.getXValue() == 0 && peak.getYValue() == 0) {
            peakList.removeChild(peak);
          }
        }  
        InputDialog dialog = new InputDialog(NewSpectrumDetailWizardPage.this.getShell());
        String text = dialog.open(NewSpectrumDetailWizardPage.this);
        //String text = new MyJOptionPane().showInputDialog( "Enter your CSV test here" );
        CSVReader reader = new CSVReader(new StringReader(text),separator.charAt( 0 ));
        String [] nextLine;
        try {
            while ((nextLine = reader.readNext()) != null) {
                boolean first=true;
                CMLPeak peak = new CMLPeak();
                for(int i=0;i<nextLine.length;i++){
                    if(!nextLine[i].equals( "" )){
                        if(first){
                            peak.setXValue(Float.parseFloat( nextLine[i]));
                            first=false;
                        }else{
                            peak.setYValue(Float.parseFloat(nextLine[i]));
                        }
                    }
                }
                ((CMLPeakList) tableViewer.getInput()).addPeak(peak);
            }
            tableViewer.refresh();
            dialogChanged();
        } catch ( Exception e1 ) {
            MessageBox mb = new MessageBox(NewSpectrumDetailWizardPage.this.getShell(),SWT.ICON_ERROR | SWT.OK );
            mb.setMessage( "There was some problem reading your input. Most likely, it was corrupt!");
            mb.setText( "Problems reading input" );
            mb.open();
            e1.printStackTrace();
        }
      }
    });

	}

	private TableViewer createTableViewer(final Table table,
			String[] columnNames) {
		final TableViewer tableViewer = new TableViewer(table);
		tableViewer.setUseHashlookup(true);
		tableViewer.setColumnProperties(columnNames);

		CellEditor[] editors = new CellEditor[columnNames.length + 4];

		// Column 2 : X_AXIS (Free text)
		TextCellEditor xEditor = new TextCellEditor(table);
		((Text) xEditor.getControl()).setTextLimit(60);
		((Text) xEditor.getControl()).addListener(SWT.Traverse, new Listener() {
			int highestColumn = 1;

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
				dialogChanged();
			}
		});
    ((Text) xEditor.getControl()).addListener(SWT.KeyDown, new Listener() {
        public void handleEvent(Event event) {
          peakdone=true;
          dialogChanged();
        }
      });
		editors[0] = xEditor;

		// Column 3 : Y_AXIS ;
		TextCellEditor yEditor = new TextCellEditor(table);
		((Text) yEditor.getControl()).setTextLimit(60);
		((Text) yEditor.getControl()).addListener(SWT.Traverse, new Listener() {
			int highestColumn = 1;

			public void handleEvent(Event event) {
				if (event.detail == SWT.TRAVERSE_TAB_PREVIOUS
						|| event.detail == SWT.TRAVERSE_ARROW_PREVIOUS) {
					tableViewer.editElement(tableViewer.getElementAt(table
							.getSelectionIndex()), 0);
				}
				if (event.detail == SWT.TRAVERSE_TAB_NEXT
						|| event.detail == SWT.TRAVERSE_ARROW_NEXT) {
					if (highestColumn == 1
							&& table.getSelectionIndex() < ((CMLPeakList) tableViewer
									.getInput()).getChildCount() - 1)
						tableViewer.editElement(tableViewer.getElementAt(table
								.getSelectionIndex() + 1), 0);
					if (highestColumn > 1)
						tableViewer.editElement(tableViewer.getElementAt(table
								.getSelectionIndex()), 2);
				}
			}
		});
		editors[1] = yEditor;

		for (int i = 0; i < 1; i++) {
			TextCellEditor cellEditor = new TextCellEditor(table);
			((Text) cellEditor.getControl()).setTextLimit(60);
			((Text) cellEditor.getControl()).addListener(SWT.Traverse,
					new PeakTableTabListener(2 + i, tableViewer));
			editors[2 + i] = cellEditor;

		}
		tableViewer.setCellEditors(editors);
		tableViewer.setCellModifier(new PeakCellModifier(columnNames,
				tableViewer));
		return tableViewer;
	}

	private Table createTable(Composite container, String[] columnNames) {
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.HIDE_SELECTION | SWT.LEFT;

		Table table = new Table(container, style);
		RowData rowData = new RowData();
		rowData.height = 100;
		table.setLayoutData(rowData);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		TableColumn[] column = new TableColumn[columnNames.length];

		for (int i = 0; i < columnNames.length; i++) {
			column[i] = new TableColumn(table, SWT.CENTER, i);
			column[i].setText(columnNames[i]);
			column[i].setWidth(100);
		}
		return table;
	}

	public String getSpectrumType() {
		return this.spectypemap.get(spectrumTypeCombo
				.getText()).getAttributeValue("name");
	}

	public CMLPeakList getPeakList() {
	    return (CMLPeakList) tableViewer.getInput();
	}
	
  private void updateStatus(String message) {
      setErrorMessage(message);
      setPageComplete(message == null);
  }
  /**
   * Ensures that spectrum type and peaks are given.
   */

  private void dialogChanged() {
    CMLPeakList peaks = getPeakList();
    String spectype = getSpectrumType();

    if (spectype==null || spectype.length() == 0 || spectype.equals( "unknown" )) {
      updateStatus("Spectrum type must be specified");
      return;
    }

    if(peakdone){
        updateStatus( null );
        return;
    }
    if (peaks == null) {
      updateStatus("At least one peak must be given");
      return;
    }else{
        int realpeakcount=0;
        Iterator<CMLPeak> it = peaks.getPeakElements().iterator();
        while (it.hasNext()) {
          CMLPeak peak = it.next();
          if (peak.getXValue() != 0) {
            realpeakcount++;
          }
        }
        if(realpeakcount==0){
            updateStatus("At least one peak must be given");
            return;
        }
    }
    updateStatus(null);
  }

  /**
   * This class demonstrates how to create your own dialog classes. It allows users
   * to input a String
   */
  public class InputDialog extends Dialog {
    private String message;
    private String input;
    private Text septext=null;
    private NewSpectrumDetailWizardPage wizardpage;

    /**
     * InputDialog constructor
     * 
     * @param parent the parent
     */
    public InputDialog(Shell parent) {
      // Let users override the default styles
      super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
      setText("Input CSV");
      setMessage("Please enter CSV text:");
    }

    /**
     * Gets the message
     * 
     * @return String
     */
    public String getMessage() {
      return message;
    }

    /**
     * Sets the message
     * 
     * @param message the new message
     */
    public void setMessage(String message) {
      this.message = message;
    }

    /**
     * Gets the input
     * 
     * @return String
     */
    public String getInput() {
      return input;
    }

    /**
     * Sets the input
     * 
     * @param input the new input
     */
    public void setInput(String input) {
      this.input = input;
    }

    /**
     * Opens the dialog and returns the input
     * 
     * @return String
     */
    public String open(NewSpectrumDetailWizardPage wizardpage) {
      // Create the dialog window
      this.wizardpage = wizardpage;
      Shell shell = new Shell(getParent(), getStyle());
      shell.setText(getText());
      createContents(shell);
      shell.pack();
      shell.open();
      Display display = getParent().getDisplay();
      while (!shell.isDisposed()) {
        if (!display.readAndDispatch()) {
          display.sleep();
        }
      }
      // Return the entered value, or null
      return input;
    }

    /**
     * Creates the dialog's contents
     * 
     * @param shell the dialog window
     */
    private void createContents(final Shell shell) {
      shell.setLayout(new GridLayout(2, true));

      // Show the message
      Label label = new Label(shell, SWT.NONE);
      label.setText(message);
      GridData data = new GridData();
      data.horizontalSpan = 2;
      label.setLayoutData(data);

      // Display the input box
      final Text text = new Text(shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
      text.setSize( 100,200 );
      data = new GridData(GridData.FILL_HORIZONTAL);
      data.horizontalSpan = 2;
      data.minimumHeight=200;
      data.heightHint=200;
      text.setLayoutData(data);
      
      final Label seplabel = new Label(shell, SWT.NONE);
      seplabel.setText( "Enter your separator:" );
      data = new GridData();
      data.horizontalSpan = 2;
      seplabel.setLayoutData(data);

      // Display the input box
      septext = new Text(shell, SWT.BORDER | SWT.MULTI);
      septext.setText( "," );
      data = new GridData(GridData.FILL_HORIZONTAL);
      data.horizontalSpan = 2;
      septext.setLayoutData(data);

      // Create the cancel button and add a handler
      // so that pressing it will set input to null
      Button cancel = new Button(shell, SWT.PUSH);
      cancel.setText("Cancel");
      data = new GridData(GridData.FILL_HORIZONTAL);
      cancel.setLayoutData(data);
      cancel.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent event) {
          input = null;
          shell.close();
        }
      });

      // Create the OK button and add a handler
      // so that pressing it will set input
      // to the entered value
      Button ok = new Button(shell, SWT.PUSH);
      ok.setText("OK");
      data = new GridData(GridData.FILL_HORIZONTAL);
      ok.setLayoutData(data);
      ok.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent event) {
          input = text.getText();
          wizardpage.separator=septext.getText();
          shell.close();
        }
      });

      // Set the OK button as the default, so
      // user can type input and press Enter
      // to dismiss
      shell.setDefaultButton(ok);
    }
  }  
}
