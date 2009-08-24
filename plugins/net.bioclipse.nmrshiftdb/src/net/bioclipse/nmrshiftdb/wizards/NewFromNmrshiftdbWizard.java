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

import java.net.URL;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.bioclipse.core.util.LogUtils;
import net.bioclipse.nmrshiftdb.util.NmrshiftdbUtils;
import net.bioclipse.specmol.Activator;
import net.bioclipse.specmol.domain.JumboSpecmol;
import net.bioclipse.specmol.editor.SpecMolEditor;
import net.bioclipse.spectrum.editor.SpectrumEditor;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.utils.Options;
import org.apache.axis.utils.XMLUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMolecule;

public class NewFromNmrshiftdbWizard extends Wizard implements INewWizard{
	
	
	private static final Logger logger = Logger.getLogger(NewFromNmrshiftdbWizard.class);
	private NewFromNmrshiftdbWizardPage newMolPage;
	protected String searchtext;
	protected String searchmode;
	protected String searchfield;
	private ServerWizardPage serverPage;
	
	public NewFromNmrshiftdbWizard(){
		setWindowTitle("Query NMRShiftDB");
	}
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}
	
	public NewFromNmrshiftdbWizardPage getNewMolPage() {
		return newMolPage;
	}
	
	public void addPages()  
	{  
		// create and add first page
		serverPage=new ServerWizardPage("dummy");
		addPage(serverPage);
		newMolPage=new NewFromNmrshiftdbWizardPage();
		addPage(newMolPage);
	}
	
	
	
	public boolean performFinish() {
		try {
			Options opts = new Options(new String[0]);
		    opts.setDefaultURL(serverPage.getSelectedServer()+"/services/NMRShiftDB");
		    Service  service = new Service();
		    Call     call    = (Call) service.createCall();
		    call.setOperationName("doSearch");
		    call.setTargetEndpointAddress( new URL(opts.getURL()) );
		    DocumentBuilder builder;
		    builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		    SOAPBodyElement[] input = new SOAPBodyElement[1];
		    Document doc = builder.newDocument();
		    Element cdataElem;
		    cdataElem = doc.createElementNS("http://www.nmrshiftdb.org/ws/NMRShiftDB/", "doSearch");
		    Element reqElem;
		    reqElem = doc.createElementNS("http://www.nmrshiftdb.org/ws/NMRShiftDB/", "searchstring");
		    Node node;
		    node = doc.createTextNode(NmrshiftdbUtils.replaceSpaces(newMolPage.getSearchstring()));
		    reqElem.appendChild(node);
		    Element reqElem2;
		    reqElem2 = doc.createElementNS("http://www.nmrshiftdb.org/ws/NMRShiftDB/", "searchtype");
		    Node node2;
		    node2 = doc.createTextNode(NmrshiftdbUtils.replaceSpaces(newMolPage.getTypemap().get(newMolPage.getSearchtype())));
		    reqElem2.appendChild(node2);
		    Element reqElem3;
		    reqElem3 = doc.createElementNS("http://www.nmrshiftdb.org/ws/NMRShiftDB/", "searchfield");
		    Node node3;
		    node3 = doc.createTextNode(NmrshiftdbUtils.replaceSpaces(newMolPage.getFieldmap().get(newMolPage.getSearchfield())));
		    reqElem3.appendChild(node3);
		    cdataElem.appendChild(reqElem);
		    cdataElem.appendChild(reqElem2);
		    cdataElem.appendChild(reqElem3);
		    input[0] = new SOAPBodyElement(cdataElem);
		    Vector elems = (Vector) call.invoke( input );
		    SOAPBodyElement elem = (SOAPBodyElement) elems.get(0);
		    Element e    = elem.getAsDOM();
	    	CMLBuilder cmlbuilder = new CMLBuilder();
	    	CMLElement cmlElement = (CMLElement) cmlbuilder.parseString(XMLUtils.ElementToString(e));
    		IFolder folder=NmrshiftdbUtils.createVirtualFolder();
	    	for(int i=0;i<cmlElement.getChildCount();i++){
	    		net.bioclipse.specmol.Activator.getDefault().getJavaSpecmolManager().saveSpecmol(new JumboSpecmol((CMLCml)cmlElement.getChildCMLElements().get(i)),folder.getFile(((CMLMolecule)cmlElement.getChildCMLElements().get(i).getChildCMLElement("molecule",0)).getId() + "." + SpectrumEditor.CML_TYPE));
	    	}
		} catch (Exception e) {
			LogUtils.handleException(e, logger, Activator.PLUGIN_ID);
			return false;
		}
		return true;
	}
	
	
	public boolean canFinish(){
		if(!newMolPage.getSearchstring().equals(""))
			return true;
		else
			return false;
	}

}
