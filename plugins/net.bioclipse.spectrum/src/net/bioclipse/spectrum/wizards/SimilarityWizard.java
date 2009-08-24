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

import java.text.DecimalFormat;

import net.bioclipse.core.domain.ISpectrum;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.spectrum.Activator;
import net.bioclipse.spectrum.business.ISpectrumManager;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.MessageBox;

/**
 * A wizard for selecting a spectrum file for similariy calculation and for executing the 
 * calculation.
 *
 */
public class SimilarityWizard extends Wizard {

    private SelectFileWizardPage         selectFilePage;
    private static final Logger  logger = Logger.getLogger( SimilarityWizard.class );
    private IStructuredSelection ssel;

    /**
     * @param ssel The selection in the navigator, containing the files to calculate similarity for.
     */
    public SimilarityWizard(IStructuredSelection ssel) {
        setWindowTitle( "Calculate spectrum similarity" );
        setNeedsProgressMonitor( true );
        this.ssel = ssel;
    }

    /**
     * Adding the page to the wizard.
     */

    public void addPages() {
        selectFilePage = new SelectFileWizardPage();
        addPage( selectFilePage );
    }

    @Override
    public boolean performFinish() {
        try {
            final ISpectrumManager spectrummanager =
                    Activator.getDefault()
                            .getJavaSpectrumManager();
            IStructuredSelection referenceselection =
                    selectFilePage.getSelectedRes();
            ISpectrum reference =
                    spectrummanager.loadSpectrum( (IFile) referenceselection
                            .getFirstElement());
            StringBuffer sb = new StringBuffer();
            DecimalFormat formatter = new DecimalFormat( "0.00" );
            for ( int i = 0; i < ssel.size(); i++ ) {
                ISpectrum mol =
                        spectrummanager.loadSpectrum( (IFile) ssel.toArray()[i]);
                double sim = spectrummanager.calculateSimilarityWCC( mol, reference, 0.01);
                sb.append(((IFile) ssel.toArray()[i]).getName()+": "+formatter.format( sim * 100 )+"%\r\n");
            }
            MessageBox mb = new MessageBox(this.getShell());
            mb.setText( "Similarities to "+ ( (IFile) referenceselection.getFirstElement()).getName());
            mb.setMessage( sb.toString() );
            mb.open();
        } catch ( Exception ex ) {
            LogUtils.handleException( ex, logger, Activator.PLUGIN_ID );
        }
        return true;
    }

}
