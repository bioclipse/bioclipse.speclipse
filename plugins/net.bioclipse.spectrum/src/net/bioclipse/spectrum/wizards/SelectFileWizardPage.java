/*******************************************************************************
 * Copyright (c) 2009  Stefan Kuhn <stefan.kuhn@ebi.ac.uk>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.spectrum.wizards;

import java.io.IOException;

import net.bioclipse.core.util.LogUtils;
import net.bioclipse.spectrum.Activator;
import net.bioclipse.spectrum.filecontentprovider.SpectrumFileContentProvider;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import spok.utils.SpectrumUtils;

/**
 * A wizard page for selecting a single spectrum file.
 */
public class SelectFileWizardPage extends WizardPage {

    private IStructuredSelection selectedFiles = null;
    private static final Logger  logger        = Logger.getLogger( SelectFileWizardPage.class );

    protected SelectFileWizardPage() {

        super( "Spectrum similarity calculation" );
        setTitle( "Select file to compare to" );
        setDescription( "Spectrum similarity will be calculated against this file." );
    }

    public void createControl( Composite parent ) {
        Composite container = new Composite( parent, SWT.NULL );
        GridLayout layout = new GridLayout();
        container.setLayout( layout );
        layout.numColumns = 2;
        layout.verticalSpacing = 9;
        final TreeViewer treeViewer = new TreeViewer( container );
        treeViewer.setContentProvider( new SpectrumFileContentProvider() );
        treeViewer.setLabelProvider( WorkbenchLabelProvider
                .getDecoratingWorkbenchLabelProvider() );
        treeViewer.setUseHashlookup( true );
        // Layout the tree viewer below the text field
        GridData layoutData = new GridData();
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.grabExcessVerticalSpace = true;
        layoutData.horizontalAlignment = GridData.FILL;
        layoutData.verticalAlignment = GridData.FILL;
        layoutData.horizontalSpan = 3;
        treeViewer.getControl().setLayoutData( layoutData );
        treeViewer.setInput( ResourcesPlugin.getWorkspace().getRoot()
                .findMember( "." ) );
        treeViewer.expandToLevel( 2 );
        treeViewer
                .addSelectionChangedListener( new ISelectionChangedListener() {

                    public void selectionChanged( SelectionChangedEvent event ) {

                        SelectFileWizardPage.this.setPageComplete( false );
                        ISelection sel = event.getSelection();
                        if ( sel instanceof IStructuredSelection ) {
                            selectedFiles = (IStructuredSelection) sel;
                            try {
                                if ( selectedFiles.size() == 1
                                     && containsSpectrum( selectedFiles ) )
                                    SelectFileWizardPage.this
                                            .setPageComplete( true );
                            } catch ( Exception e ) {
                                LogUtils.handleException( e, logger, Activator.PLUGIN_ID );
                            }
                        }
                    }
                } );
        treeViewer.setSelection( new StructuredSelection( ResourcesPlugin
                .getWorkspace().getRoot().findMember( "." ) ) );
        setPageComplete( false );
        setControl( container );
    }

    public IStructuredSelection getSelectedRes() {
        return this.selectedFiles;
    }

    private boolean containsSpectrum( IStructuredSelection selectedFiles )
            throws CoreException, IOException {
        if ( selectedFiles != null ) {
            for ( int i = 0; i < selectedFiles.toArray().length; i++ ) {
                if ( selectedFiles.toArray()[i] instanceof IFile ) {
                    if ( SpectrumUtils.isSpectrum( (IFile) selectedFiles
                            .toArray()[i] ) )
                        return true;
                }
            }
        }
        return false;
    }
}
