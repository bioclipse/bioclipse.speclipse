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
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.nmrshiftdb.util.NmrshiftdbUtils;
import net.bioclipse.spectrum.editor.MetadataUtils;
import net.bioclipse.cml.contenttypes.CmlFileDescriber;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.client.async.AsyncCall;
import org.apache.axis.client.async.IAsyncResult;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.utils.Options;
import org.apache.axis.utils.XMLUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.io.formats.CMLFormat;
import org.openscience.cdk.io.formats.IChemFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMetadataList;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLSpectrum;


/**
 * This wizard performs a spectrum search on NMRShiftDB for a spectrum and stores the result in a virtual folder
 */

public class ElucidateWizard extends Wizard{
	private ElucidateServerPage serverPage;
	CMLSpectrum cmlspectrum =null;
	String VIRTUAL_FOLDER="NMRShiftDB spectrum search results";
	IAsyncResult ar;
	Call     call;
	AsyncCall acall;
	SOAPBodyElement[] input;
	Shell shell;
	Boolean flag;
	private static final Logger logger = Logger.getLogger(ElucidateWizard.class);

	/**
	 * Constructor for JCPWizard.
	 */
	public ElucidateWizard(CMLSpectrum cmlspectrum) {
		super();
		setWindowTitle("Search NMRShiftDB by Spectrum");
		setNeedsProgressMonitor(true);
		this.cmlspectrum=cmlspectrum;
	}
	
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		serverPage=new ElucidateServerPage();
		addPage(serverPage);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	
	public boolean performFinish() {
		flag=true;
		try{
		    //The submit call			
		    Options opts = new Options(new String[0]);
		    opts.setDefaultURL(serverPage.getSelectedServer()+"/services/NMRShiftDB");
		    Service  service = new Service();
		    call    = (Call) service.createCall();
		    call.setTargetEndpointAddress( new URL(opts.getURL()) );
		    call.setTimeout(1000000);
			input = new SOAPBodyElement[1];
		    DocumentBuilder builder;
		    builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		    Document doc = builder.newDocument();
		    Element cdataElem;
		    cdataElem = doc.createElementNS(opts.getURL(), "doElucidate");
		    Element reqElem;
		    reqElem = doc.createElementNS(opts.getURL(), "suborwhole");
		    Node node;
		    node = doc.createTextNode(serverPage.selectedOption());//signifies type of search
		    reqElem.appendChild(node);
	        Document document = builder.parse(new ByteArrayInputStream(cmlspectrum.toXML().getBytes()));
		    Node nodeimp=doc.importNode(document.getFirstChild(),true);
		    cdataElem.appendChild(nodeimp);
		    cdataElem.appendChild(reqElem);
		    input[0] = new SOAPBodyElement(cdataElem);
		    
		    //Start of code of statusbar and cancel button
		    shell=new Shell();
			IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
		          public void run(IProgressMonitor monitor)throws InvocationTargetException, InterruptedException {
		        	  try{
		          monitor.beginTask("Comunicating with the Server!!!",15);
		          while (true) {
		          		monitor.worked(1);
		            	acall = new AsyncCall(call);
		            	ar = acall.invoke(input);
		            	org.apache.axis.client.async.Status status = null;
		            	monitor.worked(5);
		            	int i=0;
		            	boolean waitingMessage=true;
		            	while ((status = ar.getStatus()) == org.apache.axis.client.async.Status.NONE && monitor.isCanceled() == false) {
		            				Thread.sleep(50);
		            				i++;
		            				if(i==350)
		            				{
		            					if(waitingMessage)
		            					{
		            						monitor.subTask("Still working...?");
		            						waitingMessage=false;
		            					}
		            					else
		            					{
		            						monitor.subTask("Please be patient, still doing, but seems to take a while...");
		            						waitingMessage=true;
		            					}
		            					i=0;
		            				}
		            	}
		            	if (status == org.apache.axis.client.async.Status.EXCEPTION) {
		            		ElucidateWizard.this.getPages()[0].getControl().getDisplay().syncExec(
	        			    	      new Runnable() {
	        			    	        public void run(){
	        	            			MessageBox mb = new MessageBox(new Shell(), SWT.OK);
						            		mb.setText("Server problem");
						            		mb.setMessage("There was a problem with the server.\r\nPlease Try again later!");
						            		mb.open();
	        			    	        }
	        			    	      });						            		
		            	}
		            	monitor.worked(9);
		            	if(monitor.isCanceled()) {
		            		monitor.done();
		            		flag=false;
		                return;
		              }
		              monitor.done();
		              break;
		           }
		        	  }catch(Throwable ex){
		        		  ex.printStackTrace();
		        	  }
		        }
			};
			
		    ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
		    dialog.run(true, true, runnableWithProgress);
		    //End of code for statusbar and cancel button
		    if(flag)
		    {
		    	Vector          elems = (Vector) ar.getResponse();		    
		    	SOAPBodyElement elem = (SOAPBodyElement) elems.get(0);
		    	Element e    = elem.getAsDOM();
		    	CMLBuilder cmlbuilder = new CMLBuilder();
	    		CMLElement cmlElement = (CMLElement) cmlbuilder.parseString(XMLUtils.ElementToString(e));
				CMLCml cmlelud=(CMLCml)cmlElement;
				//We seave the results in a virtual folder
				IFolder virtualfolder=NmrshiftdbUtils.createVirtualFolder();
				for(int i=0;i<cmlelud.getCMLChildCount("molecule");i++){
					nu.xom.Element mol=cmlelud.getChildElements().get(i);
					mol.setNamespaceURI(CmlFileDescriber.NS_CML);
					CMLReader reader = new CMLReader(new ByteArrayInputStream(mol.toXML().getBytes()));
			        IChemFile file = (IChemFile)reader.read(DefaultChemObjectBuilder.getInstance().newChemFile());
			        
			        CMLMolecule cmlMol = (CMLMolecule)cmlelud.getChildElements(
                            "molecule",CmlFileDescriber.NS_CML).get(i);
			        List<CMLMetadata> metadataList = 
			            MetadataUtils.getAllInOneMetadataList(cmlMol)
			                         .getMetadataDescendants();
			        
			         
			        List<CMLMetadata> descendents = 
			            MetadataUtils.getMetadataDescendantsByName(
			                metadataList,"qname:similarity");
			        
			        String similarity = descendents.get(0).getContent();
   
			        ICDKManager manager = 
			            net.bioclipse.cdk.business.Activator
			                    .getDefault().getJavaCDKManager();
			        CDKMolecule molecule = 
			            new CDKMolecule(
			                    file.getChemSequence(0)
			                    .getChemModel(0)
			                    .getMoleculeSet()
			                    .getAtomContainer(0));
			        
			        String filename = (similarity.length() == 7 ? "0"
                            + similarity : (similarity.length() == 6 ? "00"
                            + similarity : similarity))
                            + " similarity" + i + ".cml";
			        
					manager.saveMolecule(molecule, 
					                     virtualfolder.getFile(filename), 
					                     (IChemFormat)CMLFormat.getInstance());

		    	}
		    }
		 }catch(Exception ex){
			 LogUtils.handleException(ex,logger);
		}
		
		return true;
	}
}