package net.bioclipse.spectrum.dialogs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class MappingFileCreationDialog extends TitleAreaDialog{


	

	private Text idText;
	private Label idLabel;
	private Label labelLabel;
	private Text labelText;
	private Label fileNameLabel;
	private Text fileNameText;
	private AddMetadataDialog parentDialog;

	public MappingFileCreationDialog(Shell parentShell, AddMetadataDialog parentDialog) {
		super(parentShell);
		this.parentDialog = parentDialog;
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		setTitle("Add Category");
		setMessage("Create a new mapping file defining a new Metadata Category");
		return contents;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite parentComposite = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(parentComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		layout.verticalSpacing = 9;
		layout.numColumns = 4;
		
		GridData textData = new GridData();
		textData.widthHint = 112;
		
		fileNameLabel = new Label(composite, SWT.NULL);
		fileNameLabel.setText("File Name: ");
		fileNameText = new Text(composite, SWT.BORDER);
		fileNameText.setLayoutData(textData);
		
		idLabel = new Label(composite, SWT.NULL);
		idLabel.setText("Id: ");
		idText = new Text(composite, SWT.BORDER);
		idText.setLayoutData(textData);
		
		GridData labelData = new GridData();
		labelData.widthHint = 150;
		labelData.horizontalSpan = 3;
		
		labelLabel = new Label(composite, SWT.NULL);
		labelLabel.setText("Label: ");
		labelText = new Text(composite, SWT.BORDER);
		labelText.setLayoutData(labelData);
		
//		dictLocationLabel = new Label(composite, SWT.NULL);
//		dictLocationLabel.setText("Dictionary Location: ");
//		dictLocationText = new Text(composite, SWT.BORDER);
//		
//		prefixLabel = new Label(composite, SWT.NULL);
//		prefixLabel.setText("Prefix: ");
//		prefixText = new Text(composite, SWT.BORDER);
//		
//		namespaceLabel = new Label(composite, SWT.NULL);
//		namespaceLabel.setText("Namespace: ");
//		namespaceText = new Text(composite, SWT.BORDER);
		
		return parentComposite;
	}
	
	@Override
	protected void okPressed() {
		URL varPluginUrl = Platform.getBundle(
		"net.bioclipse.spectrum").getEntry("/mappingFiles/");
		String varInstallPath = null;
		try {
			varInstallPath = Platform.asLocalURL(varPluginUrl).getFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String fileName = fileNameText.getText();
		if (fileName == null || fileName.length() == 0) {
			fileName = idText.getText();
		}
		if (fileName != null && fileName.length() > 0) {
			String completeFilePath = varInstallPath + fileName + ".xml";
			File file = new File(completeFilePath);
			if (file.exists()) {
				String message = "The file " + completeFilePath + " already exists! Should it be overwritten?";
				boolean question = MessageDialog.openQuestion(getShell(), "File already exists", message);
				if (!question) {
					this.cancelPressed();
					this.close();
				}
			}
			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			Document doc = createXMLDoc();
			try {
				FileOutputStream fop = new FileOutputStream(file);
				Serializer serializer = new Serializer(fop, "ISO-8859-1");
				serializer.setIndent(4);
				serializer.write(doc);  
				fop.close();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.parentDialog.refreshCategories(labelText.getText());
		this.close();
	}

	private Document createXMLDoc() {
		Element dictionaryMapping = new Element("dictionaryMapping");
		Attribute id = new Attribute("id", idText.getText());
		dictionaryMapping.addAttribute(id);
		Attribute label = new Attribute("label", labelText.getText());
		dictionaryMapping.addAttribute(label);
		
		HashMap<String, String> sectionMap = new HashMap<String, String>();
		sectionMap.put("conditionList", "Condition List");
		sectionMap.put("substanceList", "Substance List");
		sectionMap.put("metadataList", "Metadata List");
		Iterator keyIt = sectionMap.keySet().iterator();
		while (keyIt.hasNext()) {
			String sectionName = (String) keyIt.next();
			String sectionLabel = sectionMap.get(sectionName);
			Element section = new Element ("section");
			Attribute nameAttr = new Attribute("name", sectionName);
			section.addAttribute(nameAttr);
			Attribute labelAttr = new Attribute("label", sectionLabel);
			section.addAttribute(labelAttr);
			dictionaryMapping.appendChild(section);
		}
		Document doc = new Document(dictionaryMapping);
		return doc;
	}

}
