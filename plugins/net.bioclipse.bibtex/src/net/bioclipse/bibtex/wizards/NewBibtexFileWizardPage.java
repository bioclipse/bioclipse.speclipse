/*******************************************************************************
 * Copyright (c) 2008-2009  Stefan Kuhn <stefan.kuhn@ebi.ac.uk
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.bibtex.wizards;


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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (bib).
 */

public class NewBibtexFileWizardPage extends WizardPage {
	private Text dirText;

	private Text fileText;

	private String selectedFormat;

	private IResource selectedFolder = null;
	
	Button yesButton=null;
	
	Button noButton=null;
	
	IStructuredSelection selection=null;

  private String initialfile;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public NewBibtexFileWizardPage(IStructuredSelection selection) {
		super("BibtexFileWizardPage");
		setTitle("New Bibtex File Wizard");
		setDescription("This wizard creates a new bibtex file");
		this.selection = selection;
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
		treeViewer.setSelection(selection);

	    Label labeltemplate = new Label(container, SWT.NULL);
	    labeltemplate.setText("Do you want template entries in the new file? ");
	    yesButton = new Button(container, SWT.RADIO);
	    yesButton.setText("Yes");
	    noButton = new Button(container, SWT.RADIO);
	    noButton.setText("No");
	    noButton.setSelection(true);

		
		label = new Label(container, SWT.NULL);
		label.setText("&File name:");

		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fileText.setText( initialfile );
		fileText.setLayoutData(gd);
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		
		dialogChanged();
		setControl(container);
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

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
		
		if(fileName.indexOf( "." )>-1 && fileName.indexOf( ".bib" )==-1){
		    updateStatus( "File must have .bib extension or none" );
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
		} else {
			return null;
		}
	}

	public String getPathStr() {
		return dirText.getText();
	}

	public String getCompleteFileName() {
		String path = this.getPathStr();
		String fileName = this.getFileName();
		String completePath = path + System.getProperty("file.separator") + fileName;
		if(completePath.indexOf( ".bib" )==-1)
				completePath= completePath + ".bib";
		return completePath;
	}

	public String getSelectedFormat() {
		return selectedFormat;
	}

	public IResource getSelectedFolder() {
		return selectedFolder;
	}

	public Button getYesButton() {
		return yesButton;
	}

    public void setFileName( String findUnusedFileName ) {
        initialfile =  findUnusedFileName;
    }
}