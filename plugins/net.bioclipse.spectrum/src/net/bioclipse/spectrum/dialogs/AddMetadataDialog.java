package net.bioclipse.spectrum.dialogs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import net.bioclipse.spectrum.editor.GeneralMetadataFormPage;
import net.bioclipse.spectrum.editor.SpectrumEditor;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.xmlcml.cml.base.CMLElements;
import org.xmlcml.cml.element.CMLConditionList;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMetadataList;
import org.xmlcml.cml.element.CMLScalar;
import org.xmlcml.cml.element.CMLSpectrum;
import org.xmlcml.cml.element.CMLSubstance;
import org.xmlcml.cml.element.CMLSubstanceList;

public class AddMetadataDialog extends TitleAreaDialog {

	

	private static final String CONDITION_LIST = "Condition List";
	private static final String SUBSTANCE_LIST = "Substance List";
	private static final String METADATA_LIST = "Metadata List";
	private CMLSpectrum spectrum;
	private final String CATEGORYADDER = "Add new Category";
	private Text nameText;
	private Text valueText;
	private Text presetValueText;
	private Combo sections;
	private Combo categories;
	private Text idText;
	private HashMap mappingFiles = new HashMap();
	private String prefixString = "prefix:";
	private IEditorPart editor;

	public AddMetadataDialog(Shell parentShell, IEditorPart editor) {
		super(parentShell);
		this.editor = editor;
		//TODO
		this.spectrum = ((SpectrumEditor)editor).getSpectrum();
	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		setTitle("Add Metadata entries");
		setMessage("Add new Metadata entry to one of the existing categories, or create a new one");
		return contents;
	}	
	
	@Override
	protected Control createDialogArea(Composite parent) {
//		top level composite
		Composite parentComposite = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(parentComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		layout.verticalSpacing = 9;
		layout.numColumns = 4;
		
		GridData textData = new GridData();
		textData.widthHint = 112;
		textData.heightHint = 20;
		
		Label idLabel = new Label(composite, SWT.NULL);
		idLabel.setText("Id: ");
        idText = new Text(composite, SWT.BORDER);
        idText.setLayoutData(textData);
        
        Label nameLabel = new Label(composite, SWT.NULL);
		nameLabel.setText("Name: ");
        nameText = new Text(composite, SWT.BORDER);
        nameText.setLayoutData(textData);
        
		Label valueLabel = new Label(composite, SWT.NULL);
        valueLabel.setText("Value: ");
        valueLabel.setLayoutData(textData);
        
        valueText = new Text(composite, SWT.BORDER);
        valueText.setLayoutData(textData);
        valueText.setToolTipText("Please add the value to be stored with this entry in the current spectrum");
		
        Label presetValueLabel = new Label(composite, SWT.NULL);
        presetValueLabel.setText("Preset Values: ");
        presetValueLabel.setLayoutData(textData);
        presetValueLabel.setToolTipText("Please enter values, which should be saved as preset selection possibilites for the MetadataEditor");
        
        GridData textBoxData = new GridData();
        textBoxData.widthHint = 100;
        textBoxData.heightHint = 50;
        presetValueText = new Text(composite, SWT.BORDER | SWT.MULTI);
        presetValueText.setLayoutData(textBoxData);
        presetValueText.setToolTipText("Please add multiple possible entries in separate line");
        
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        
        Label sectionsLabel = new Label(composite, SWT.NULL);
        sectionsLabel.setText("add to Section: ");
        sectionsLabel.setLayoutData(gd);
        
        sections = new Combo(composite, SWT.NULL);
        sections.add(METADATA_LIST);
        sections.add(SUBSTANCE_LIST);
        sections.add(CONDITION_LIST);
        sections.setLayoutData(gd);
        sections.select(0);
        
        Label categoriesLabel = new Label(composite, SWT.NULL);
        categoriesLabel.setText("add to Category: ");
        categoriesLabel.setLayoutData(gd);
        
        categories = new Combo(composite, SWT.NULL);
        readInCategories();
        categories.setLayoutData(gd);
        categories.addSelectionListener(new CategorySelectionListener());
        
		
		return parentComposite;
	}

	private void readInCategories() {
		categories.removeAll();
		URL varPluginUrl = Platform.getBundle(
		"net.bioclipse.spectrum").getEntry("/mappingFiles/");
		String varInstallPath = null;
		GeneralMetadataFormPage formPage = null;
		int selectionIndex = 0;
		try {
			varInstallPath = Platform.asLocalURL(varPluginUrl).getFile();
			//get a file list of contained files and iterate over them
			File dir = new File(varInstallPath);
			File[] files = dir.listFiles();
			for (int i=0; i<files.length; i++) {
				File file = files[i];
				if (file.getName().startsWith(".")) {
					continue;
				}
				else {
					Builder builder = new Builder();
					try {
						Document mapping = builder.build(file);
						String label = mapping.getRootElement().getAttribute("label").getValue();
						if (mapping.getRootElement().getAttribute("prefix") != null) {
							prefixString = mapping.getRootElement().getAttribute("prefix").getValue() + ":";
						}
						if (label != null && label.length() > 0) {
							categories.add(label);
							ArrayList<Object> list = new ArrayList<Object>();
							list.add(file);
							list.add(mapping);
							this.mappingFiles.put(label, list);
							if (label.compareTo("Bioclipse Metadata Entries") == 0) {
								int count = categories.getItemCount();
								selectionIndex = count-1;
							}
						}
					} catch (ValidityException e) {
						e.printStackTrace();
					} catch (ParsingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			categories.add(CATEGORYADDER);
		} catch(IOException e) {
			StringWriter strWr = new StringWriter();
			PrintWriter prWr = new PrintWriter(strWr);
		}
		categories.select(selectionIndex);
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
    	GridLayout layout = new GridLayout();
    	layout.numColumns = 0; // this is incremented by createButton
    	layout.makeColumnsEqualWidth = true;
    	layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
    	layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
    	layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
    	layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
    	composite.setLayout(layout);
    	composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    	composite.setFont(parent.getFont());

		// create help control if needed
        if (isHelpAvailable()) {
        	Control helpControl = createHelpControl(composite);
        	((GridData) helpControl.getLayoutData()).horizontalIndent = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		}
        
        createButtonsForButtonBar(composite);
        return composite;
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, 100, "Add", true);
		this.getButton(100).setToolTipText("Adds the new Meta Data Entry to the mapping file, saves it and updates spectrum");
		createButton(parent, IDialogConstants.OK_ID, "Finish", false);
		this.getButton(IDialogConstants.OK_ID).setToolTipText("Closes this dialog and sets the MetaDataEditor to dirty");
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	@Override
	protected void buttonPressed(int buttonId) {
		if (100 == buttonId) {
			addPressed();
		} else {
			super.buttonPressed(buttonId);
		}
	}

	
	protected void okPressed() {
		//TODO
		//((SpectrumEditor)editor).reload(true);
		//TODO
		//((MetadataEditorInput)((MetadataEditor)editor).getEditorInput()).getResource().fireChange();
		super.okPressed();
//		this.close();
	}
	
	private void addPressed() {
		String name = this.nameText.getText();
		String value = this.valueText.getText();
		String[] valueList = presetValueText.getText().trim().split(System.getProperty("line.separator"));
		String id = this.idText.getText();
		String sectionName = this.sections.getItem(sections.getSelectionIndex());
		String category = this.categories.getItem(categories.getSelectionIndex());
		if (sectionName.compareTo(AddMetadataDialog.METADATA_LIST) == 0) {
			CMLMetadata newMetadata = new CMLMetadata();
			newMetadata.setName(prefixString + name);
			newMetadata.setId(id);
			newMetadata.setContent(value);
			CMLElements<CMLMetadataList> mlists = spectrum.getMetadataListElements();
			if (mlists != null && mlists.size() > 0) {
				CMLMetadataList mlist = mlists.get(0);
				mlist.addMetadata(newMetadata);
			}
			else {
				CMLMetadataList metadataList = new CMLMetadataList();
				metadataList.addMetadata(newMetadata);
				spectrum.addMetadataList(metadataList);
			}
		}
		else if (sectionName.compareTo(AddMetadataDialog.CONDITION_LIST) == 0) {
			CMLScalar condition = new CMLScalar();
			condition.setId(id);
			condition.setTitle(name);
			condition.setValue(value);	
			CMLElements<CMLConditionList> mlists = spectrum.getConditionListElements();
			if (mlists != null && mlists.size() > 0) {
				CMLConditionList mlist = mlists.get(0);
				mlist.addScalar(condition);
			}
			else {
				CMLConditionList conditionList = new CMLConditionList();
				conditionList.addScalar(condition);
				spectrum.addConditionList(conditionList);
			}
		}
		else if (sectionName.compareTo(AddMetadataDialog.SUBSTANCE_LIST) == 0) {
			CMLSubstance substance = new CMLSubstance();
			substance.setId(id);
			substance.setTitle(name);
			nu.xom.Text textNode = new nu.xom.Text(value);
			substance.appendChild(textNode);
			Elements mlists = spectrum.getChildCMLElements("substanceList");
			if (mlists != null && mlists.size() > 0) {
				Element mlist = mlists.get(0);
				mlist.appendChild(substance);
			}
			else {
				CMLSubstanceList substanceList = new CMLSubstanceList();
				substanceList.appendChild(substance);
				spectrum.appendChild(substanceList);
			}
		}
		ArrayList list = (ArrayList) this.mappingFiles.get(category);
		File file = (File) list.get(0);
		Document mapping = (Document) list.get(1);
		Element entry = new Element("entry");
		Attribute idAttr = new Attribute("id", id);
		entry.addAttribute(idAttr);
		Attribute labelAttr = new Attribute("label", name);
		entry.addAttribute(labelAttr);
		if (valueList != null && valueList.length > 0) {
			Element valueListElement = new Element("valueList");
			for (int i=0; i<valueList.length; i++) {
				String listValue = valueList[i];
				//secure, that we dont have empty values
				if (listValue.length() > 0) {
					Element valueElement = new Element("value");
					valueElement.appendChild(listValue);
					valueListElement.appendChild(valueElement);
				}
			}
			//just add valueLists with children
			if (valueListElement.getChildCount() > 0) {
				entry.appendChild(valueListElement);
			}
		}
		 
		Nodes result = mapping.getRootElement().query("//following-sibling::*[@label='" + sectionName + "']");
		if (result.size() > 0) {
			Element section = (Element) result.get(0);
			section.appendChild(entry);
		}
		try {
			FileWriter fwrt = new FileWriter(file);
			fwrt.write(mapping.toXML());
			fwrt.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		resetAllTextFields();
	}
	
	private void resetAllTextFields() {
		this.idText.setText("");
		this.nameText.setText("");
		this.valueText.setText("");
		this.presetValueText.setText("");
	}

	public class CategorySelectionListener implements SelectionListener {

		public void widgetDefaultSelected(SelectionEvent e) {
			
		}

		public void widgetSelected(SelectionEvent e) {
			Combo source = (Combo) e.getSource();
			if (source.getItem(source.getSelectionIndex()).compareTo(CATEGORYADDER) == 0) {
				MappingFileCreationDialog mappingFileCreationDialog = new MappingFileCreationDialog(getShell(), AddMetadataDialog.this);
				mappingFileCreationDialog.open();
				
			}
		}
		
	}

	public void refreshCategories(String labelToSelect) {
		readInCategories();	
		//get the new items index and select the new item
		for (int i=0; i<categories.getItemCount(); i++) {
			String item = categories.getItem(i);
			if (item.compareTo(labelToSelect) == 0) {
				categories.select(i);
				break;
			}
		}
		
	}
	
}
