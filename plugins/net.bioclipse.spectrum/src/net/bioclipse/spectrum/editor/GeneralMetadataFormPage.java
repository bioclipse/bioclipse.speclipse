package net.bioclipse.spectrum.editor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import net.bioclipse.spectrum.Activator;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import nu.xom.XPathContext;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.xmlcml.cml.base.CMLElements;
import org.xmlcml.cml.element.CMLConditionList;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMetadataList;
import org.xmlcml.cml.element.CMLScalar;
import org.xmlcml.cml.element.CMLSpectrum;
import org.xmlcml.cml.element.CMLSubstance;
import org.xmlcml.cml.element.CMLSubstanceList;

import spok.utils.SpectrumUtils;

public class GeneralMetadataFormPage extends MetadataFormPage {

	private Document metadataMapping;
	public Document getMetadataMapping() {
		return metadataMapping;
	}

	private ScrolledForm form;
	private InputStream mappingFile;
	private Element mappingDOM;
	private String formText;
	private String dictLocationPath;
	private String prefixString;
	List<Text> textfields=new ArrayList<Text>();
	List<Combo> comboboxes=new ArrayList<Combo>();
	List<Element> sections=new ArrayList<Element>();
	List<Element> sections2=new ArrayList<Element>();
	
	public String getPrefixString() {
		return prefixString;
	}


	public GeneralMetadataFormPage(SpectrumEditor editor, InputStream file, Node metadataList, Node conditionList, Element substancelist) {
		super(editor, null, null);
		this.mappingFile = file;
		try{
			mappingDOM  = readMappingFile();
			Attribute prefixAttr = mappingDOM.getAttribute("prefix");
			if (prefixAttr != null) {
				prefixString = prefixAttr.getValue() +  ":";
			}
			String id = mappingDOM.getAttribute("id").getValue();
			this.setTitle(id + " Metadata");
			formText = mappingDOM.getAttribute("label").getValue();
			if (mappingDOM.getAttribute("dictLocation") != null) {
				dictLocationPath = mappingDOM.getAttribute("dictLocation").getValue();
			}			
			//we remove elements from metadatalists and conditionslist, which are used in here
			Elements sections = mappingDOM.getChildElements("section");
			for (int i=0; i<sections.size(); i++) {
				Element section = sections.get(i);
				Elements entries = section.getChildElements("entry");
				for (int k=0; k<entries.size(); k++) {
					Element entry = entries.get(k);
					String elementid = entry.getAttribute("id").getValue();
					if (prefixString != null && id.startsWith(prefixString)) {
						elementid = elementid.substring(prefixString.length());
					}
					if(metadataList!=null){
						Nodes tempResult = metadataList.query("*[@id='" + elementid + "']");
						for(int l=0;l<tempResult.size();l++){
							tempResult.get(l).detach();
						}
					}
					if(conditionList!=null){
						Nodes tempResult = conditionList.query("*[@id='" + elementid + "']");
						for(int l=0;l<tempResult.size();l++){
							tempResult.get(l).detach();
						}
					}
					if(substancelist!=null){
						Nodes tempResult = substancelist.query("*[@id='" + elementid + "']");
						for(int l=0;l<tempResult.size();l++){
							tempResult.get(l).detach();
						}
					}
				}
			}
		}catch(Exception ex){
			Activator.handleUnhandleableException(ex);
		}
	}

	@Override
	protected void createFormContent(IManagedForm managedForm){
		form = managedForm.getForm();
		form.setText(formText);
		ColumnLayout layout = new ColumnLayout();
		layout.topMargin = 0;
		layout.bottomMargin = 5;
		layout.leftMargin = 10;
		layout.rightMargin = 10;
		layout.horizontalSpacing = 10;
		layout.verticalSpacing = 10;
		layout.maxNumColumns = 3;
		layout.minNumColumns = 2;
		form.getBody().setLayout(layout);
		Elements sections = mappingDOM.getChildElements("section");
		fillForm(sections, managedForm);
	}	
	
	private void fillForm(Elements sections, IManagedForm managedForm) {
		for (int i=0; i<sections.size(); i++) {
			Element section = sections.get(i);
			if (section != null) {
				try{
					createSubSection(section, managedForm);
				}
				catch(Exception ex){
					Activator.handleUnhandleableException(ex);
				}
			}
		}
		
	}
	private void createSubSection(Element section, IManagedForm managedForm) throws ValidityException, IOException, ParsingException {
		String sectionLabel = section.getAttributeValue("label");
		Composite client = createSection(managedForm, sectionLabel, sectionLabel, 2);
		FormToolkit toolkit = managedForm.getToolkit();
		GridData gd = new GridData();
		gd.widthHint = 75;	
		Element dict = null;
		if (this.dictLocationPath != null) {
			dict = readDict();
		}
		
		Elements entries = section.getChildElements("entry");
		for (int i=0; i<entries.size(); i++) {
			Element entry = entries.get(i);
			String id = entry.getAttribute("id").getValue();
			String toolTip = null;
			if (prefixString != null && id.startsWith(prefixString)) {
				id = id.substring(prefixString.length());
			}
			String entryLabel = entry.getAttribute("label").getValue();
			if (dict != null) {
				String prefix = dict.getNamespacePrefix();
				if (prefix == null || prefix.length() < 1) {
					prefix = "prefix";
				}
				String uri = dict.getNamespaceURI();
				XPathContext xpathContext = new XPathContext(prefix, uri);
				Nodes resultNodes = dict.query("//" + prefix + ":entry[@id='" + id +"']", xpathContext);
				Element result = null;
				if (resultNodes.size() == 1) {
					result = (Element) resultNodes.get(0);
					if (result != null) {					
						Elements definitionResult = result.getChildElements("definition", uri);
						if (definitionResult != null && definitionResult.size() > 0) {
							toolTip = definitionResult.get(0).getValue().trim();
							StringTokenizer tokenizer = new StringTokenizer(toolTip);
							StringBuffer buffer = new StringBuffer();
							while (tokenizer.hasMoreTokens()) {
								String token = tokenizer.nextToken();
								buffer.append(token + " ");
							}
							toolTip = buffer.toString();
						}
					}
				}	
				Label label = toolkit.createLabel(client, entryLabel); 
				label.setToolTipText(toolTip);
			}
			else {
				Label label = toolkit.createLabel(client, entryLabel); 
			}
			
			Elements entryValueList = entry.getChildElements("valueList");
			String fillText = getFillText(id);
			if (entryValueList != null && entryValueList.size() > 0) {
				Elements values = entryValueList.get(0).getChildElements("value");
				ArrayList<String> valueArray = getValuesAsStringArray(values);
				Combo valuesField = new Combo(client,SWT.DROP_DOWN);
				String[] array = new String[valueArray.size()];
				array = (String[]) valueArray.toArray(array);
				valuesField.setItems(array);
				valuesField.setLayoutData(gd);
				int index = 0;
				if (fillText != null && fillText.length() > 0) {
					int textIndex = valueArray.indexOf(SpectrumUtils.toJcampGeneric(fillText.trim()));
					if (textIndex != -1) {
						index = textIndex;
					}
				}
				valuesField.select(index);
				valuesField.setData(id);
				valuesField.addModifyListener(modifyListener);
				comboboxes.add(valuesField);
				sections2.add(section);
			}
			else {
				Text text = toolkit.createText(client, fillText, SWT.BORDER); 
				text.setLayoutData(gd);
				text.setData(id);
				text.addModifyListener(modifyListener);
				textfields.add(text);
				sections.add(section);
			}
		}	
	}

	private ArrayList<String> getValuesAsStringArray(Elements values) {
		ArrayList<String> returnArray = new ArrayList<String>();
		for (int i=0; i<values.size(); i++) {
			String value = values.get(i).getValue();
			returnArray.add(value);
		}
		returnArray.add(0, "");
		return returnArray;
	}

	private String getFillText(String id) {
		CMLSpectrum spectrum = ((SpectrumEditor) this.getEditor()).getSpectrum();
		CMLElements<CMLMetadataList> metadataLists = spectrum.getMetadataListElements();
		CMLElements<CMLConditionList> conditionLists = spectrum.getConditionListElements();
		Elements substanceLists = spectrum.getChildCMLElements("substanceList");
		String fillText = "";
		if (id.compareTo("DATATYPE") == 0) {
			fillText = spectrum.getType();
			return fillText;
		}
		else if (metadataLists != null && metadataLists.size() > 0) {
			CMLMetadataList metadataList = metadataLists.get(0);
			Nodes tempResult = metadataList.query("*[@id='" + id + "']");
			if (tempResult != null && tempResult.size() > 0) {
				fillText = ((CMLMetadata) tempResult.get(0)).getContent();
				return fillText;
			}
			
		}
		if (conditionLists != null && conditionLists.size() > 0) {
			CMLConditionList conditionList = conditionLists.get(0);
			Nodes tempResult = conditionList.query("*[@id='" + id + "']");
			if (tempResult != null && tempResult.size() > 0) {
				fillText = ((CMLScalar) tempResult.get(0)).getValue();
				return fillText;
			}
			
		}
		if (substanceLists != null && substanceLists.size() > 0) {
			CMLSubstanceList substanceList = (CMLSubstanceList) substanceLists.get(0);
			Nodes tempResult = substanceList.query("*[@id='" + id + "']");		
			if (tempResult != null && tempResult.size() > 0) {
				fillText = ((CMLSubstance) tempResult.get(0)).getValue();
				return fillText;
			}
		}
		return fillText;		
	}

	private Element readDict() throws IOException, ValidityException, ParsingException {
		Builder builder = new Builder();
		Document jcampDict = null;
		URL varPluginUrl = Platform.getBundle(
		"net.bioclipse.cml").getEntry(dictLocationPath);
		String varInstallPath = Platform.asLocalURL(varPluginUrl).getFile();
		jcampDict = builder.build(varInstallPath + "jcampDXDict.xml");
		return jcampDict.getRootElement();
	}

	private Element readMappingFile() throws ValidityException, ParsingException, IOException {
		Builder builder = new Builder();		
		metadataMapping = builder.build(mappingFile);
		Element rootElem = metadataMapping.getRootElement();
		return rootElem;
	}

	private Composite createSection(IManagedForm mform, String title,
			String desc, int numColumns) {
		final ScrolledForm form = mform.getForm();
		FormToolkit toolkit = mform.getToolkit();
		Section section = toolkit.createSection(form.getBody(), Section.TWISTIE
				| Section.TITLE_BAR | Section.DESCRIPTION | Section.EXPANDED);
		section.setText(title);
		toolkit.createCompositeSeparator(section);
		Composite client = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = numColumns;
		client.setLayout(layout);
		section.setClient(client);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(false);
			}
		});
		return client;
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		//this is never used, since saving is done via text editor	
	}


	public void update() {
		for(int i=0;i<textfields.size();i++){
			String fillText = getFillText( (String) textfields.get(i).getData());
			if(fillText!=null)
				textfields.get(i).setText(fillText);
		}
		for(int i=0;i<comboboxes.size();i++){
			String fillText = getFillText( (String) comboboxes.get(i).getData());
			if(fillText!=null)
				comboboxes.get(i).setText(fillText);
		}
		modifyListener.setSpectrum(editor.getSpectrum());
	}	
}
