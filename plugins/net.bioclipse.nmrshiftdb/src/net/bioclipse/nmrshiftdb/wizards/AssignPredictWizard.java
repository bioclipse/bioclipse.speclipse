/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stefan Kuhn
 *     
 ******************************************************************************/
package net.bioclipse.nmrshiftdb.wizards;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.bioclipse.specmol.editor.AssignmentPage;
import net.bioclipse.specmol.editor.SpecMolEditor;
import net.bioclipse.spectrum.editor.MetadataUtils;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.utils.Options;
import org.apache.axis.utils.XMLUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbenchPart;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMetadataList;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLSpectrum;

import spok.utils.SpectrumUtils;

public class AssignPredictWizard extends PredictWizard{
	private CMLMolecule molecule;
	private CMLSpectrum spectrum;
	String nucleus=null;
	AssignmentPage assignmentpage=null;
	private boolean mode =true;

	/**
	 * @param molecule
	 * @param spectrum
	 * @param assignmentpage
	 * @param view
	 * @param mode true=display check, false=do assignment
	 */
	public AssignPredictWizard(CMLMolecule molecule, CMLSpectrum spectrum, AssignmentPage assignmentpage, IWorkbenchPart view, boolean mode) {
		super();
		setWindowTitle("Assign by Prediction");
		setNeedsProgressMonitor(true);
		this.spectrum=spectrum;
		this.molecule=molecule;
		this.assignmentpage=assignmentpage;
		this.mode=mode;
		CMLMetadataList mdl = MetadataUtils.getAllInOneMetadataList(spectrum);
		if(MetadataUtils.getMetadataDescendantsByName(mdl.getMetadataDescendants(),SpecMolEditor.nucleus).size()>0){
			nucleus=MetadataUtils.getMetadataDescendantsByName(mdl.getMetadataDescendants(),SpecMolEditor.nucleus).get(0).getAttributeValue("content");
		}
	}
	
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		serverPage = new ServerWizardPage(nucleus);
		addPage(serverPage);
		if(nucleus==null){
			typePage = new SpectrumTypeWizardPage(nucleus);
			addPage(typePage);
		}
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		if(typePage!=null && typePage.getSelectedFormat()!=null){
			nucleus=typePage.getSelectedFormat();
			if(spectrum.getMetadataListElements().size()==0){
				CMLMetadataList newmdl=new CMLMetadataList();
				spectrum.addMetadataList(newmdl);
			}
			CMLMetadataList metadatalist=spectrum.getMetadataListElements().get(0);
			CMLMetadata metadata=new CMLMetadata();
			metadata.setName(SpecMolEditor.nucleus);
			metadata.setContent(nucleus);
			metadatalist.addMetadata(metadata);
		}		
		//Check if the spectrum type is actually legal
		try{
		    Options opts = new Options(new String[0]);
		    opts.setDefaultURL(getServerPage().getSelectedServer()+"/services/NMRShiftDB");
		    Service  service = new Service();
		    Call     call    = (Call) service.createCall();
		    call.setOperationName("getSpectrumTypes");
		    call.setTargetEndpointAddress( new URL(opts.getURL()) );
		    DocumentBuilder builder;
		    builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		    SOAPBodyElement[] input = new SOAPBodyElement[1];
		    Document doc = builder.newDocument();
		    Element cdataElem;
		    cdataElem = doc.createElementNS("http://www.nmrshiftdb.org/ws/NMRShiftDB/", "getSpectrumTypes");
		    input[0] = new SOAPBodyElement(cdataElem);
		    
		    Vector          elems = (Vector) call.invoke( input );
		    SOAPBodyElement elem  = null ;
		    Element         e     = null ;
		    elem = (SOAPBodyElement) elems.get(0);
		    e    = elem.getAsDOM();
		    String spectrumTypes=e.getFirstChild().getTextContent();
		    StringTokenizer st=new StringTokenizer(spectrumTypes);
		    Vector<String> types=new Vector<String>();
		    while(st.hasMoreTokens()){
		    	types.add(st.nextToken());
		    }
		    if(!types.contains(nucleus)){
				MessageBox mb = new MessageBox(this.getShell(), SWT.OK);
		        mb.setText("Problem with nucleus");
		        mb.setMessage("Your observed nucleus is "+nucleus+". The server can only do "+spectrumTypes+". Please change it!");
		        mb.open();
		    	return false;
			}
		    opts = new Options(new String[0]);
		    opts.setDefaultURL(serverPage.getSelectedServer()+"/services/NMRShiftDB");
		    service = new Service();
		    call    = (Call) service.createCall();
		    call.setTargetEndpointAddress( new URL(opts.getURL()) );
		    input = new SOAPBodyElement[1];
		    builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		    Document document = builder.parse(new ByteArrayInputStream(molecule.toXML().getBytes()));
		    doc = builder.newDocument();
		    cdataElem = doc.createElementNS("http://www.nmrshiftdb.org/ws/NMRShiftDB/", "doPrediction");
		    Element reqElem;
		    reqElem = doc.createElementNS("http://www.nmrshiftdb.org/ws/NMRShiftDB/", "spectrumTypeName");
		    Node node;
		    node = doc.createTextNode(nucleus);
		    reqElem.appendChild(node);
		    Node nodeimp=doc.importNode(document.getChildNodes().item(0),true);
		    cdataElem.appendChild(nodeimp);
		    cdataElem.appendChild(reqElem);
		    input[0] = new SOAPBodyElement(cdataElem);
		    elems = (Vector) call.invoke( input );
		    elem = (SOAPBodyElement) elems.get(0);
		    e    = elem.getAsDOM();
	    	CMLBuilder cmlbuilder = new CMLBuilder();
	    	CMLElement cmlElement = (CMLElement) cmlbuilder.parseString(XMLUtils.ElementToString(e));
			CMLSpectrum spectrumpred=(CMLSpectrum)cmlElement;
			if(mode){
				assignmentpage.updateSpectrum(spectrumpred);
			}else{
				List<CMLElement> peaks = SpectrumUtils.getPeakElements(spectrumpred);
				Iterator<CMLElement> it = peaks.iterator();
				while (it.hasNext()) {
					CMLPeak peak = (CMLPeak) it.next();
					//look for closest peak in "real" spectrum
					List<CMLElement> peaksreal = SpectrumUtils.getPeakElements(spectrum);
					Iterator<CMLElement> it2 = peaksreal.iterator();
					double mindistance=Double.MAX_VALUE;
					CMLPeak bestpeak=null;
					while (it2.hasNext()) {
						CMLPeak peakreal = (CMLPeak) it2.next();
						if(Math.abs((float)(peakreal.getXValue()-peak.getXValue()))<mindistance){
							mindistance=Math.abs((float)(peakreal.getXValue()-peak.getXValue()));
							bestpeak=peakreal;
						}
					}			
					//assign the peak found to the atom with the atomid of the atomref of the predicted peak
					if(bestpeak!=null){
						bestpeak.setAtomRefs(peak.getAtomRefs());
					}
				}
				assignmentpage.updateSpectrum(null);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return true;
	}

	public CMLMolecule getMolecule() {
		return molecule;
	}

	public CMLSpectrum getSpectrum() {
		return spectrum;
	}

}