/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package net.bioclipse.spectrum.wizards;

import java.io.File;
import java.util.HashMap;

import net.bioclipse.chemoinformatics.wizards.WizardHelper;
import net.bioclipse.spectrum.editor.SpectrumEditor;
import net.bioclipse.ui.contentlabelproviders.FolderContentProvider;
import net.bioclipse.ui.contentlabelproviders.FolderLabelProvider;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (cml).
 */

public class NewSpectrumFileWizardPage extends WizardPage {
	private Text dirText;

	private Text fileText;

	private List list;

	private Text txtExtension;

	private HashMap<String,String> spectrumFormat2ExtensionMap = new HashMap<String,String>();

	private String selectedFormat;

	public static final String CML = "Chemical Markup Language";

	public static final String JCAMP = "Jcamp-dx";

	private IResource selectedFolder = null;

	private boolean extensionSelectable = true;
	
	private IContainer selection;

	{
		spectrumFormat2ExtensionMap.put(CML, SpectrumEditor.CML_TYPE);
		spectrumFormat2ExtensionMap.put(JCAMP, SpectrumEditor.JCAMP_TYPE);
	}

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public NewSpectrumFileWizardPage(String header, String text, IContainer sel) {
		super("SpectrumFileWizardPage");
		setTitle(header);
		setDescription(text);
		selection=sel;
	}


	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label label = new Label(container, SWT.NULL);
		label.setText("&File Directory:");

		dirText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		dirText.setLayoutData(gd);
		gd.horizontalSpan = 3;
		if(selection!=null){
		    dirText.setText( selection.getFullPath().toOSString() );
		    selectedFolder=selection;
		}
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

		// Layout the tree viewer below the text field
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

		label = new Label(container, SWT.NULL);
		label.setText("&File name:");

		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fileText.setLayoutData(gd);
		if(selection != null)
        fileText.setText( WizardHelper.findUnusedFileName(new StructuredSelection(selection), "unnamed", "")  );
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		if (this.extensionSelectable) {
			txtExtension = new Text(container, SWT.BORDER);
			txtExtension.setBounds(260, 25, 50, 25);
			txtExtension.setEnabled(false);

			label = new Label(container, SWT.NONE);
			label.setText("Supported formats:");
	
			list = new List(container, SWT.BORDER);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			list.setLayoutData(gd);
			list.add(CML);
			list.add(JCAMP);
			list.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					String[] plist = list.getSelection();
					if (plist != null) {
						selectedFormat = plist[0];
						txtExtension.setText((String) spectrumFormat2ExtensionMap
								.get(selectedFormat));
					}
				}
			});
			list.select(0);
			String[] plist = list.getSelection();
			if (plist != null) {
				selectedFormat = plist[0];
				txtExtension.setText((String) spectrumFormat2ExtensionMap
						.get(selectedFormat));
			}
		}

		dialogChanged();
		setControl(container);
	}

	/**
	 * Ensures that both text fields are set.
	 */

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
		if(ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(getSelectedFolder().getFullPath().toOSString()+File.separator+getFileName()+ ( getFileName().indexOf( "."+getExtension() ) == -1 ? "."+getExtension() : "" ))).exists()){
		  updateStatus("File already exists");
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
		} else {
			return null;
		}
	}
	
	public String getPathStr() {
		return dirText.getText();
	}

	public String getExtension() {
		return txtExtension.getText();
	}

	public String getCompleteFileName() {
		String path = this.getPathStr();
		String fileName = this.getFileName();
		String extension = this.getExtension();
		String completePath = path + System.getProperty("file.separator")
				+ fileName + extension;
		return completePath;
	}

	public String getSelectedFormat() {
		return selectedFormat;
	}

	public IResource getSelectedFolder() {
		return selectedFolder;
	}
}