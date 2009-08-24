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

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.jobs.BioclipseUIJob;
import net.bioclipse.nmrshiftdb.util.NmrshiftdbUtils;
import net.bioclipse.specmol.domain.JumboSpecmol;
import net.bioclipse.spectrum.editor.SpectrumEditor;
import nu.xom.converters.DOMConverter;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.utils.Options;
import org.apache.axis.utils.XMLUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.libio.cml.Convertor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMolecule;

/**
 * This is a sample new wizard. Its role is to create a new file 
 * resource in the provided container. If the container resource
 * (a folder or a project) is selected in the workspace 
 * when the wizard is opened, it will accept it as the target
 * container. The wizard creates one file with the extension
 * "cml". If a sample multi-page editor (also available
 * as a template) is registered for the same extension, it will
 * be able to open it.
 */

public class DownloadSpectraWizard extends Wizard{
	protected DownloadSpectraServerWizardPage serverPage;
	private IWorkbenchPart view=null;
	private IAtomContainer ac;
	private static final Logger logger = Logger.getLogger(DownloadSpectraServerWizardPage.class);

	/**
	 * Constructor for JCPWizard.
	 */
	public DownloadSpectraWizard(IWorkbenchPart view) {
		super();
		setNeedsProgressMonitor(true);
		this.view=view;
		ISelection sel=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		if (sel.isEmpty()==false){
		   if (sel instanceof IStructuredSelection) {
		       IStructuredSelection ssel = (IStructuredSelection) sel;
		       try {
		    	   IFile cdkres = (IFile)ssel.getFirstElement();
             Activator.getDefault().getJavaCDKManager().loadMolecule( cdkres,
                  new BioclipseUIJob<ICDKMolecule>() {

                 @Override
                 public void runInUI() {
                     ac=getReturnValue().getAtomContainer();                     
                 }
             });
		       } catch (Exception e) {
		           throw new RuntimeException(e.getMessage());
		       }
		    }
		}
	}
	
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		serverPage = new DownloadSpectraServerWizardPage();
		addPage(serverPage);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		try{
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
		    
		    /*DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		    DocumentBuilder db = dbf.newDocumentBuilder();
		    Node node = db.parse(new ByteArrayInputStream(new Convertor(true,"").cdkAtomContainerToCMLMolecule(ac).toXML().getBytes()));*/
		    Node node=DOMConverter.convert(new nu.xom.Document(new Convertor(true,"").cdkAtomContainerToCMLMolecule(ac)),builder.getDOMImplementation()).getFirstChild();
		    reqElem.appendChild(doc.importNode(node,true));
		    Element reqElem2;
		    reqElem2 = doc.createElementNS("http://www.nmrshiftdb.org/ws/NMRShiftDB/", "searchtype");
		    Node node2;
		    node2 = doc.createTextNode("--");
		    reqElem2.appendChild(node2);
		    Element reqElem3;
		    reqElem3 = doc.createElementNS("http://www.nmrshiftdb.org/ws/NMRShiftDB/", "searchfield");
		    Node node3;
		    node3 = doc.createTextNode(NmrshiftdbUtils.replaceSpaces(serverPage.selectedOption()));
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
	    	if(cmlElement.getChildCount()>0){
	    		IFolder folder=NmrshiftdbUtils.createVirtualFolder();
		    	for(int i=0;i<cmlElement.getChildCount();i++){
		    		net.bioclipse.specmol.Activator.getDefault().getJavaSpecmolManager().saveSpecmol(new JumboSpecmol((CMLCml)cmlElement.getChildCMLElements().get(i)),folder.getFile(((CMLMolecule)cmlElement.getChildCMLElements().get(i).getChildCMLElement("molecule",0)).getId() + "." + SpectrumEditor.CML_TYPE));
		    	}
	    	}else{
	    		MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "No results", "No spectra in NMRShiftDB for this structure!" );
	    	}
		}catch(Exception ex){
			LogUtils.handleException(ex,logger);
		}
		return true;
	}

	public IWorkbenchPart getView() {
		return view;
	}
}