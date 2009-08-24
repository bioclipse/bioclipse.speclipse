/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.specmol.wizards;

import java.util.ArrayList;
import java.util.Iterator;

import net.bioclipse.spectrum.filecontentprovider.SpectrumFileContentProvider;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class AddSpectrumWizardPage extends WizardPage {

	private List text;
	private ArrayList<IFile> spectra = new ArrayList<IFile>();
	
	protected AddSpectrumWizardPage(String text) {
		super("Add Spectra");
		setTitle("Add Spectra");
		setDescription(text);
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		layout.verticalSpacing = 9;
		
		TreeViewer treeViewer = new TreeViewer(container);
		treeViewer.setContentProvider(new SpectrumFileContentProvider());
		treeViewer.setUseHashlookup(true);
		
		//Layout the tree viewer below the text field
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.horizontalAlignment = GridData.FILL;
//		layoutData.verticalAlignment = GridData.FILL;
		layoutData.horizontalSpan = 3;
		treeViewer.getControl().setLayoutData(layoutData);
    treeViewer.setLabelProvider(WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
		treeViewer.setInput(ResourcesPlugin.getWorkspace().getRoot().findMember("."));
		treeViewer.expandToLevel(2);
		treeViewer.setSelection(new StructuredSelection(ResourcesPlugin.getWorkspace().getRoot().findMember(".")));
		
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent e) {
				ISelection sel = e.getSelection();
				if (sel instanceof IStructuredSelection) {
					Iterator it = ((IStructuredSelection) sel).iterator();
					while (it.hasNext()) {
						Object element = it.next();
						if (element instanceof IFile) {
							IFile bioRes = (IFile) element;
							if(!spectra.contains(bioRes)){
									text.add(bioRes.getName());
									text.redraw();
									spectra.add(bioRes);
								}
								else {
									setErrorMessage("Please select a Spectrum file!");
								}
							}
						}
					}
				}
		});
		
		Label listLabel = new Label(container, SWT.NULL);
		listLabel.setText("List of spectra to be added to the new Resource:");
		
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		
		listLabel.setLayoutData(gd);
		text = new List(container, SWT.BORDER | SWT.MULTI |SWT.V_SCROLL);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		gd.heightHint = 150;
		gd.widthHint = 250;
		text.setLayoutData(gd);

		final Button addButton = new Button(container, SWT.PUSH);
		addButton.setText("Remove Spectrum");
		addButton.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
					int[] selection = text.getSelectionIndices();
					for (int i=selection.length-1;i>-1;i--) {
						IFile bioRes = (IFile) spectra.get(selection[i]);
						spectra.remove(bioRes);
						text.remove(bioRes.getName());
						text.redraw();
					}
		      }
		});

		setControl(container);
	}

	public ArrayList<IFile> getSpectra() {
		return spectra;
	}



}
