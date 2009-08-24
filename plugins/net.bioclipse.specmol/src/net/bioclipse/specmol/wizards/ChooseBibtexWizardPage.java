/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.specmol.wizards;

import net.bioclipse.bibtex.wizards.FolderLabelProvider;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

public class ChooseBibtexWizardPage extends WizardPage {

	private IFile selectedRes;
	
	protected ChooseBibtexWizardPage() {
		super("Choose Bibtex File");
		setTitle("Assgin Bibtex entries wizard");
		setDescription("This wizard lets you choose a bibtex file to add one of its entries to the molecule or spectrum open in SpecMol editor");
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		layout.verticalSpacing = 9;
		
		final TreeViewer treeViewer = new TreeViewer(container);
		treeViewer.setContentProvider(new BibtexContentProvider());
		treeViewer.setLabelProvider(new DecoratingLabelProvider(
                new FolderLabelProvider(),  PlatformUI
                        .getWorkbench().getDecoratorManager()
                        .getLabelDecorator()));		
		treeViewer.setUseHashlookup(true);
		
		//Layout the tree viewer below the text field
		GridData layoutData = new GridData();
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
				if (sel instanceof IStructuredSelection && ((IStructuredSelection) sel).getFirstElement() instanceof IFile) {
					selectedRes = (IFile)((IStructuredSelection) sel).getFirstElement();
					getWizard().getContainer().updateButtons();
				}
			}
			
		});
		treeViewer.setSelection(new StructuredSelection(ResourcesPlugin.getWorkspace().getRoot().findMember(".")));
		setControl(container);
	}
	
	public boolean canFlipToNextPage(){
		if(selectedRes==null)
			return false;
		else 
			return true;
	}

	public IWizardPage getNextPage() {
		ChooseIdWizardPage page = ((AssignBibtexWizard)this.getWizard()).getEntryPage();
		page.initUi();
		return page;
	}


	public IFile getSelectedRes() {
			return this.selectedRes;
	}

}