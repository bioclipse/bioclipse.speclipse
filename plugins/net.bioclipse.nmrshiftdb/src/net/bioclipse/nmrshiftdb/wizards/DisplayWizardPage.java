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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.bioclipse.bibtex.wizards.FolderLabelProvider;
import net.bioclipse.ui.contentlabelproviders.FolderContentProvider;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.client.async.AsyncCall;
import org.apache.axis.client.async.IAsyncResult;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.utils.Options;
import org.apache.axis.utils.XMLUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.jfree.chart.JFreeChart;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.CMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;
import org.xmlcml.cml.element.CMLSpectrum;

import spok.guicomponents.SpectrumChartFactory;
import spok.guicomponents.SpokChartPanel;
import spok.utils.SpectrumUtils;

/**
 * The display wizard page displays a spectrum and allows saving of it.
 */

public class DisplayWizardPage extends WizardPage {
	
	private IResource selectedFolder = null;
	private Text dirText;
	private Text fileText;
	Label errorslabel;
	Button cmlbutton=null;
	Button smrbutton=null;
	CMLSpectrum spectrum;
	String spectrumstring;
	String suffix="cml";
	SpokChartPanel chartPanel=null;
	IAsyncResult ar;
	private boolean error=false;
	private StringBuffer errors=new StringBuffer();
	
	
	public String getSpectrumstring() {
		return spectrumstring;
	}

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public DisplayWizardPage() {
		super("DisplayWizardPage");
		setTitle("Predict from NMRShiftDB wizard");
		setDescription("This wizard predicts a spectrum from NMRShiftDB");
	}
	
	//function for status bar
	boolean flag;
	public boolean initUi() throws Exception{
		flag=false;
		Shell shell=new Shell();
		 IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
	          public void run(IProgressMonitor monitor)throws InvocationTargetException, InterruptedException {
	            monitor.beginTask("Comunicating with the Server!!!",15);
	            for(int i=0; i<10; i++) {
	            	try{
	            		if(i==0)
	            			ui0();
	            		if(i==1)
	            			ui1();
	            		if(i==2)
	            			ui2();
	            		if(i==3)
	            			ui3();
	            		if(i==4)
	            			ui4();
	            		if(i==5)
	            			ui5();
	            		if(i==6)
	            			ui6();
	            		if(i==7)
	            		{
	            			//The start of code to make cancel button workable during communication with server
	            			AsyncCall acall = new AsyncCall(call);
	            			ar = acall.invoke(input);
	            			org.apache.axis.client.async.Status status = null;
	            			while ((status = ar.getStatus()) == org.apache.axis.client.async.Status.NONE && monitor.isCanceled() == false) {
	            				Thread.sleep(50);
	            			}
	            			error=false;
	            			if (status == org.apache.axis.client.async.Status.EXCEPTION) {
        						error=true;
	            				DisplayWizardPage.this.getControl().getDisplay().syncExec(
        			    	      new Runnable() {
        			    	        public void run(){
        			    	              ar.getException().printStackTrace();
        	            				    MessageBox mb = new MessageBox(new Shell(), SWT.OK);
        	            		        mb.setText("Server problem");
        	            		        mb.setMessage("There was a problem on server.\r\nTry again later or ask the administrator of the server you were using!");
        	            		        mb.open();
        			    	        }
        			    	      });
	            		    }else{
		            			//The end of code to make cancel button workable during communication with server
		            			monitor.worked(5);
		            			ui7();
	            		    }
	            		}
	            		if(i==8 && !error)
	            			ui8();
	            		if(i==9 && !error)
	            			ui9();
	            	}catch(Exception e){
	            			flag=false;
	              			e.printStackTrace();
            			}
	            	if(monitor.isCanceled()) {
	            		flag=false;
	            		monitor.done();
	                return;
	              }
	              monitor.worked(1);
	              flag=true;
	            }
	           monitor.done();
	          }
	        };
	        
	        ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
	        try {
	          dialog.run(true, true, runnableWithProgress);
	        } catch (InvocationTargetException e) {
	        	flag=false;
	        	e.printStackTrace();
	        } catch (InterruptedException e) {
	        	flag=false;
	        	e.printStackTrace();
	        }
		return flag;
	}
	//the orignal initUi in parts
	
	Options opts;
	Service  service;
	Call     call;
	DocumentBuilder builder;
	IAtomContainer ac;
	Object first;
	StringWriter output;
	CMLWriter cmlwriter;
	SOAPBodyElement[] input;
	Document document;
	Document doc;
	Element cdataElem;
	Element reqElem;
	Element calculatedElem;
	Node node;
	Node nodeimp;
	Vector elems;
	SOAPBodyElement elem;
	Element e;
	CMLBuilder cmlbuilder;
	CMLElement cmlElement;
	List<CMLElement> peaks;
	Iterator it;
	
	public void ui0()throws Exception
	{
		this.getControl().getDisplay().asyncExec(new Runnable(){
				public void run(){
					try{
					  opts = new Options(new String[0]);
					  if(((PredictWizard)DisplayWizardPage.this.getWizard()).getServerPage().getSelectedServer().charAt(((PredictWizard)DisplayWizardPage.this.getWizard()).getServerPage().getSelectedServer().length()-1)=='/')
					      opts.setDefaultURL(((PredictWizard)DisplayWizardPage.this.getWizard()).getServerPage().getSelectedServer()+"services/NMRShiftDB");
					  else
					      opts.setDefaultURL(((PredictWizard)DisplayWizardPage.this.getWizard()).getServerPage().getSelectedServer()+"/services/NMRShiftDB");
					}catch(Exception ex){
						ex.printStackTrace();
					}
				}
		});
	    service = new Service();
	}
	
	public void ui1()throws Exception
	{
	      while(opts==null)
	        Thread.sleep( 100 );
		    call = (Call) service.createCall();
		    call.setTargetEndpointAddress( new URL(opts.getURL()) );
		    call.setOperationName("doPrediction");
		    input = new SOAPBodyElement[1];
	}
	
	public void ui2()throws Exception
	{
		builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    ac=null;
	}
	
	public void ui3()throws Exception
	{
		ac=((PredictWizard)DisplayWizardPage.this.getWizard()).getAc();
	}
	
	public void ui4()throws Exception
	{
		output = new StringWriter();
		cmlwriter=new CMLWriter(output);
		cmlwriter.write(ac);
		cmlwriter.close();
	}
	
	public void ui5()throws Exception
	{
		 document = builder.parse(new ByteArrayInputStream(output.toString().getBytes()));
		 doc = builder.newDocument();
		 cdataElem = doc.createElementNS(opts.getURL(), "doPrediction");
		 reqElem = doc.createElementNS(opts.getURL(), "spectrumTypeName");
		 calculatedElem = doc.createElementNS(opts.getURL(), "useCalculated");
	}
	
	public void ui6()throws Exception
	{
		
	    node = doc.createTextNode(((PredictWizard)this.getWizard()).getTypePage().getSelectedFormat());
	    reqElem.appendChild(node);
      Node calculatednode = doc.createTextNode(((PredictWizard)this.getWizard()).getTypePage().getCalculated());
      calculatedElem.appendChild(calculatednode);
	    nodeimp=doc.importNode(document.getChildNodes().item(0),true);
	    cdataElem.appendChild(nodeimp);
	    cdataElem.appendChild(reqElem);
	    cdataElem.appendChild(calculatedElem);
	    input[0] = new SOAPBodyElement(cdataElem);
	}
	
	public void ui7()throws Exception
	{
		
		 elems = (Vector)ar.getResponse();
		 elem  = (SOAPBodyElement) elems.get(0);
		 e     = elem.getAsDOM();
	}
	
	public void ui8()throws Exception
	{
		cmlbuilder = new CMLBuilder();
    	cmlElement = (CMLElement) cmlbuilder.parseString(XMLUtils.ElementToString(e));
    	spectrumstring=XMLUtils.ElementToString(e);
		
		spectrum=(CMLSpectrum)cmlElement;
		peaks = SpectrumUtils.getPeakElements(spectrum);
	}
	
	public void ui9() throws Exception{
		errors = new StringBuffer();
		it = peaks.iterator();
		CMLPeakList newPeaks=new CMLPeakList();
		while (it.hasNext()) {
			CMLPeak peak = (CMLPeak) it.next();
			if(peak.getConvention()!=null && peak.getConvention().toString().equals( "Prediction impossible" )){
			    errors.append(peak.getAtomRefs()[0]+";");
			}else{
  			if(peak.getAttribute("yValue")==null)
  				peak.setYValue(1);
  			if(Double.isNaN(peak.getYValue())){
  				peak.setYValue(1);
  			}
  			newPeaks.addPeak( peak );
			}
		}
		spectrum.removeChild( spectrum.getPeakListElements().get( 0 ));
		spectrum.addPeakList( newPeaks );
		chartPanel.setSpectrum(spectrum);
		if(!errors.toString().equals( "" )){
		    this.getControl().getDisplay().asyncExec(new Runnable(){
		        public void run(){
		            errorslabel.setText( "No predictions possible for these atoms: "+errors );
		        }
		    });
		}
	}
	
	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		try{
				GridLayout layout = new GridLayout();
				layout.numColumns = 3;
				layout.verticalSpacing = 9;
				Composite outercontainer=new Composite(parent, SWT.NULL);
				outercontainer.setLayout(layout);
				GridData layoutData = new GridData(GridData.FILL_BOTH);
				outercontainer.setLayoutData(layoutData);
	
				
				Composite container = new Composite(outercontainer, SWT.EMBEDDED);
				GridData gd = new GridData(GridData.FILL_HORIZONTAL);
				gd.horizontalSpan = 3;
				container.setLayoutData(gd);
				
				Frame  fileTableFrame = SWT_AWT.new_Frame(container);
				fileTableFrame.setLayout(new BorderLayout());
				JFreeChart chart = SpectrumChartFactory.createPeakChart(null,null, null,false);
				chart.setTitle("empty chart");
				chartPanel = new SpokChartPanel(chart, "peak", null,null);
				
				fileTableFrame.add(chartPanel,  BorderLayout.CENTER);
				
			  errorslabel= new Label(outercontainer, SWT.BORDER | SWT.SINGLE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        errorslabel.setLayoutData(gd);
				
				dirText = new Text(outercontainer, SWT.BORDER | SWT.SINGLE);
				gd = new GridData(GridData.FILL_HORIZONTAL);
				gd.horizontalSpan = 3;
				dirText.setLayoutData(gd);
				dirText.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						dialogChanged();
					}
				});
	
				
				TreeViewer treeViewer = new TreeViewer(outercontainer);
				treeViewer.setContentProvider(new FolderContentProvider());
				treeViewer.setLabelProvider(new DecoratingLabelProvider(
		                new FolderLabelProvider(),  PlatformUI
		                        .getWorkbench().getDecoratorManager()
		                        .getLabelDecorator()));		
				treeViewer.setUseHashlookup(true);
				
				//Layout the tree viewer below the text field
				layoutData = new GridData();
				layoutData.grabExcessHorizontalSpace = true;
				layoutData.grabExcessVerticalSpace = true;
				layoutData.horizontalAlignment = GridData.FILL;
				layoutData.verticalAlignment = GridData.FILL;
				layoutData.horizontalSpan = 3;
				treeViewer.getControl().setLayoutData(layoutData);
				
				treeViewer.setInput(ResourcesPlugin.getWorkspace().getRoot().findMember("."));
				treeViewer.expandToLevel(2);
				treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
	
					public void selectionChanged(SelectionChangedEvent event) {
						ISelection sel = event.getSelection();
						if (sel instanceof IStructuredSelection) {
							Object element = ((IStructuredSelection) sel)
							.getFirstElement();
							if (element instanceof IFolder) {
								selectedFolder = (IFolder) element;
								String path = ((IFolder) element).getFullPath().toOSString();
								dirText.setText(path);
							} else if(element instanceof IProject){
								selectedFolder = (IProject) element;
								dirText.setText(((IProject) element).getFullPath().toOSString());
							}
						}
					}
				});
				treeViewer.setSelection(new StructuredSelection(ResourcesPlugin.getWorkspace().getRoot().findMember(".")));
				
	
				Label label = new Label(outercontainer, SWT.NULL);
				label.setText("&File name:");
	
				fileText = new Text(outercontainer, SWT.BORDER | SWT.SINGLE);
				gd = new GridData(GridData.FILL_HORIZONTAL);
				gd.horizontalSpan = 2;
				fileText.setLayoutData(gd);
				fileText.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						dialogChanged();
					}
				});
	
				
	
			    cmlbutton = new Button(outercontainer, SWT.RADIO);
			    cmlbutton.setText("Do you want to create a CMLSpect file containing the precicted spectrum");
				gd = new GridData(GridData.FILL_HORIZONTAL);
				gd.horizontalSpan = 3;
				cmlbutton.setLayoutData(gd);

			    
			    smrbutton = new Button(outercontainer, SWT.RADIO);
			    smrbutton.setText("or a SpecMol resource containging structure and spectrum");
				gd = new GridData(GridData.FILL_HORIZONTAL);
				gd.horizontalSpan = 3;
				smrbutton.setLayoutData(gd);
			    cmlbutton.setSelection(true);		

				setControl(outercontainer);
				dialogChanged();
	
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public String getFileName() {
		if (fileText != null) {
			return fileText.getText();
		}
		else {
			return null;
		}
	}

	public String getPathStr() {
		return dirText.getText();
	}
	
	public IResource getSelectedFolder() {
		return selectedFolder;
	}
	
	private void dialogChanged() {
    	this.setPageComplete(false);
		String fileName = getFileName();
		String dirStr = getPathStr();
		
		if (dirStr.length() == 0) {
			updateStatus("Directory must be specified");
			return;
		}
		
		if (fileName == null || fileName.length() == 0) {
			updateStatus("File name must be specified");
			return;
		}
		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("File name must be valid");
			return;
		}
		updateStatus(null);
		if(isCurrentPage())
        	this.setPageComplete(true);
	}


	private void updateStatus(String message) {
		setErrorMessage(message);
	}
	
	public String getCompleteFileName() {
		String path = this.getPathStr();
		String fileName = this.getFileName();
		String completePath = path + System.getProperty("file.separator")
				+ fileName + "."+suffix;
		return completePath;
	}

	public Button getCmlbutton() {
		return cmlbutton;
	}

	public CMLSpectrum getSpectrum() {
		return spectrum;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
}