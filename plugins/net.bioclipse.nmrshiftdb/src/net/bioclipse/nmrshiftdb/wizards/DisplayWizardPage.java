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
import java.util.Iterator;
import java.util.List;

import net.bioclipse.bibtex.wizards.FolderLabelProvider;
import net.bioclipse.core.domain.ISpectrum;
import net.bioclipse.jobs.BioclipseUIJob;
import net.bioclipse.nmrshiftdb.Activator;
import net.bioclipse.spectrum.domain.JumboSpectrum;
import net.bioclipse.ui.contentlabelproviders.FolderContentProvider;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogPage;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.jfree.chart.JFreeChart;
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
	String suffix="cml";
	SpokChartPanel chartPanel=null;
	private StringBuffer errors=new StringBuffer();
	
	
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
		Activator.getDefault().getJavaNmrshiftdbManager().predictSpectrum(((PredictWizard)DisplayWizardPage.this.getWizard()).getAc(), ((PredictWizard)this.getWizard()).getTypePage().getSelectedType(), ((PredictWizard)this.getWizard()).getTypePage().getCalculated(), ((PredictWizard)this.getWizard()).getTypePage().isLocal(), ((PredictWizard)this.getWizard()).getServerPage().getSelectedServer(), new BioclipseUIJob<ISpectrum>() {
            @Override
            public void runInUI() {
    			ISpectrum result = getReturnValue();
				spectrum=((JumboSpectrum)result).getJumboObject();
				List<CMLElement> peaks = SpectrumUtils.getPeakElements(spectrum);
				errors = new StringBuffer();
				Iterator<CMLElement> it = peaks.iterator();
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
				    DisplayWizardPage.this.getControl().getDisplay().asyncExec(new Runnable(){
				        public void run(){
				            errorslabel.setText( "No predictions possible for these atoms: "+errors );
							((PredictWizard)DisplayWizardPage.this.getWizard()).getTypePage().setPageComplete(false);
				        }
				    });
				}else{
					((PredictWizard)DisplayWizardPage.this.getWizard()).getTypePage().setPageComplete(true);
				}
            }			
		});
		return true;
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