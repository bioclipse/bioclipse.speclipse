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
import net.bioclipse.ui.contentlabelproviders.FolderContentProvider;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class SelectFileWizardPage extends WizardPage {

	protected IContainer selectedFolder;
	protected Text dirText;
	private Text fileText;

	protected SelectFileWizardPage() {
		super("Define file");
	}
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label dirLabel = new Label(container, SWT.NULL);
		dirLabel.setText("Enter or select the parent Directory:");
	
		dirText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		dirText.setLayoutData(gd);
		dirText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		
		TreeViewer treeViewer = new TreeViewer(container);
		treeViewer.setContentProvider(new FolderContentProvider());
		treeViewer.setLabelProvider(new DecoratingLabelProvider(
				new FolderLabelProvider(), PlatformUI.getWorkbench()
						.getDecoratorManager().getLabelDecorator()));
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
				if (sel instanceof IStructuredSelection) {
					Object element = ((IStructuredSelection) sel).getFirstElement();
					if (element instanceof IContainer) {
						selectedFolder = (IContainer) element;
						String path = ((IContainer) element).getFullPath().toString();
						dirText.setText(path);
					}
				}
			}
			
		});
		treeViewer.setSelection(new StructuredSelection(ResourcesPlugin.getWorkspace().getRoot().findMember(".")));
		

		Label fileLabel = new Label(container, SWT.NULL);
		fileLabel.setText("&File name:");

		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fileText.setLayoutData(gd);
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		setControl(container);
	}
	
	private void dialogChanged() {
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
	}
	
	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
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

}
