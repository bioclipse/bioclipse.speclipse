package net.bioclipse.spectrum.editor;

import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.bioclipse.spectrum.Activator;
import nu.xom.Nodes;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.xml.sax.SAXException;
import org.xmlcml.cml.element.CMLConditionList;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMetadataList;
import org.xmlcml.cml.element.CMLScalar;
import org.xmlcml.cml.element.CMLSpectrum;
import org.xmlcml.cml.element.CMLSubstance;
import org.xmlcml.cml.element.CMLSubstanceList;

import spok.parser.JcampToCMLSpectrumMapper;

/**
 * Listener for all fields within the MetadataEditor
 * 
 * @author hel
 *
 */
public class MetadataModifiyListener implements ModifyListener {

	private net.bioclipse.spectrum.editor.GeneralMetadataFormPage page;
	private CMLSpectrum spectrum;


	public void setSpectrum(CMLSpectrum spectrum) {
		this.spectrum = spectrum;
	}


	/**
	 * Constructor
	 * 
	 * @param metadataFormPage The MetadataFormPage
	 */
	public MetadataModifiyListener(net.bioclipse.spectrum.editor.GeneralMetadataFormPage metadataFormPage, CMLSpectrum spectrum) {
		this.page = metadataFormPage;
		this.spectrum = spectrum;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	public void modifyText(ModifyEvent e) {
		Object src = e.getSource();
		String idOfchangedWidget = (String) ((Widget)src).getData();
		String text = null;
		if (src instanceof Combo) {
			text = ((Combo) src).getText();
		}
		else if (src instanceof Text) {
			text = ((Text) src).getText();
		}
		this.page.setDirty(true);
		CMLMetadataList metadataList = new CMLMetadataList();
		CMLConditionList conditionList = new CMLConditionList();
		CMLSubstanceList substanceList = new CMLSubstanceList();
		if (spectrum.getMetadataListElements() != null && spectrum.getMetadataListElements().size() > 0) {
			metadataList = spectrum.getMetadataListElements().get(0);
		}
		else {
			spectrum.addMetadataList(metadataList);
		}
		if (spectrum.getConditionListElements() != null && spectrum.getConditionListElements().size() > 0) {
			conditionList = spectrum.getConditionListElements().get(0);
		}
		else {
			spectrum.addConditionList(conditionList);
		}
		if (spectrum.getChildCMLElements("substanceList") != null && spectrum.getChildCMLElements("substanceList").size() > 0) {
			substanceList = (CMLSubstanceList) spectrum.getChildCMLElements("substanceList").get(0);
		}
		else {
			spectrum.appendChild(substanceList);
		}
		int foundnodes=0;
		Nodes tempResult = metadataList.query("*[@id='" + idOfchangedWidget + "']");
		if (tempResult != null && tempResult.size() > 0) {
			for(int i=0;i<tempResult.size();i++){
				((CMLMetadata) tempResult.get(i)).setContent(text);
				foundnodes++;
			}
		}
		tempResult = conditionList.query("*[@id='" + idOfchangedWidget + "']");
		if (tempResult != null && tempResult.size() > 0) {
			for(int i=0;i<tempResult.size();i++){
				((CMLScalar) tempResult.get(i)).setValue(text);
				foundnodes++;
			}
		}
		tempResult = substanceList.query("*[@id='" + idOfchangedWidget + "']");		
		if (tempResult != null && tempResult.size() > 0) {
			for(int i=0;i<tempResult.size();i++){
				((nu.xom.Text)((CMLSubstance) tempResult.get(i)).getChild(0)).setValue(text);
				foundnodes++;
			}
		}
		//the node is new in xml
		if(foundnodes==0){
			JcampToCMLSpectrumMapper.searchInMappingFiles(idOfchangedWidget, null, text, metadataList, conditionList, substanceList);
		}
		if(metadataList.getMetadataDescendants().size()==0)
			spectrum.removeChild(metadataList);
		if(conditionList.getChildElements().size()==0)
			spectrum.removeChild(conditionList);
		if(substanceList.getSubstanceElements().size()==0)
			spectrum.removeChild(substanceList);
		if(idOfchangedWidget.equals("DATATYPE")){
			//TODO this is a code duplication with PeakTableViewer.configureFromXMLFile()
			String[] columnNames = new String[6];
			Table table;
			HashMap spectypemap = new HashMap();
			String[] cmlPeakFields = { "", "", "", "" };
			CMLSpectrum spectrumItem = null;
			int highestColumn = 1;
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				org.w3c.dom.Document document = builder
						.parse(this
								.getClass()
								.getClassLoader()
								.getResourceAsStream(
										"net/bioclipse/spectrum/editor/spec.xml"));
				org.w3c.dom.NodeList nl = document.getElementsByTagName("spectrumtypes");
				org.w3c.dom.Node spectrumtypes = nl.item(nl.getLength() - 1);
				org.w3c.dom.NodeList spectrumtypelist = spectrumtypes.getChildNodes();
				for (int i = 0; i < spectrumtypelist.getLength(); i++) {
					org.w3c.dom.Node spectrumtype = spectrumtypelist.item(i);
					org.w3c.dom.NodeList mySiblings = spectrumtype.getChildNodes();
					for (int k = 0; k < mySiblings.getLength(); k++) {
						org.w3c.dom.Node currentSibling = mySiblings.item(k);
						if (currentSibling.getNodeName().equals("jcamptypes")) {
							org.w3c.dom.NodeList valueslist = currentSibling.getChildNodes();
							for (int l = 0; l < valueslist.getLength(); l++) {
								if(valueslist.item(l).getNodeName().equals("value") && valueslist.item(l).getChildNodes().item(0).getTextContent().equals(text)){
									spectrum.setType(spectrumtype.getAttributes().getNamedItem("cmltype").getNodeValue());
								}
							}
						}
					}
				}
			}
			catch(Exception ex){
				Activator.handleUnhandleableException(ex);
			}
		}
	}
}
