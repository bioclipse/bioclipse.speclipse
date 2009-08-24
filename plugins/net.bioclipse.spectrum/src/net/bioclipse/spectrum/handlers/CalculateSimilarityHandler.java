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
package net.bioclipse.spectrum.handlers;

import java.text.DecimalFormat;

import net.bioclipse.core.domain.ISpectrum;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.spectrum.Activator;
import net.bioclipse.spectrum.business.ISpectrumManager;
import net.bioclipse.spectrum.wizards.SimilarityWizard;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * A handler for calculating spectrum similarities
 *
 */
public class CalculateSimilarityHandler extends AbstractHandler {

    private static final Logger logger = Logger.getLogger( CalculateSimilarityHandler.class );

    /* (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    public Object execute( ExecutionEvent event ) throws ExecutionException {

        ISelection sel =
                PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getSelectionService().getSelection();
        DecimalFormat formatter = new DecimalFormat( "0.00" );
        if ( !sel.isEmpty() ) {
            if ( sel instanceof IStructuredSelection ) {
                try {
                    IStructuredSelection ssel = (IStructuredSelection) sel;
                    ISpectrumManager cdkmanager =
                            Activator.getDefault()
                                    .getJavaSpectrumManager();
                    // In case of two files, we compare each other, else we ask
                    // for a comparision file
                    if ( ssel.toArray().length == 2 ) {
                        ISpectrum calculateFor =
                                cdkmanager
                                    .loadSpectrum( (IFile) ssel.toArray()[0]);
                        ISpectrum reference =
                                cdkmanager
                                    .loadSpectrum((IFile) ssel.toArray()[1]);
                        double similarity =
                                cdkmanager.calculateSimilarityWCC( calculateFor,
                                                              reference, 0.01 );
                        MessageBox mb =
                                new MessageBox( new Shell(),
                                                SWT.ICON_INFORMATION | SWT.OK );
                        mb.setText( "Similarity" );
                        mb.setMessage( ((IFile) ssel.toArray()[0]).getName()
                                       + " and "
                                       + ((IFile) ssel.toArray()[1]).getName()
                                       + " similarity: "
                                       + formatter.format( similarity * 100 )
                                       + "%" );
                        mb.open();
                    } else {
                        SimilarityWizard wiz=new SimilarityWizard(ssel);
                        WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wiz);
                        dialog.open();
                    }
                } catch ( Exception ex ) {
                    LogUtils.handleException( ex, logger, Activator.PLUGIN_ID );
                }
            }
        }
        return null;
    }
}
