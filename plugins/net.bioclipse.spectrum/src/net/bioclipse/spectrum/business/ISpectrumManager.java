 /*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ola Spjuth
 *     Stefan Kuhn
 *
 ******************************************************************************/
package net.bioclipse.spectrum.business;

import java.io.IOException;
import java.io.InputStream;

import net.bioclipse.core.PublishedClass;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.TestClasses;
import net.bioclipse.core.TestMethods;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.ISpectrum;
import net.bioclipse.managers.business.IBioclipseManager;
import net.bioclipse.spectrum.domain.IJumboSpectrum;
import net.bioclipse.spectrum.domain.JumboSpectrum;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

@PublishedClass( "Handles spectral data (CML, jcamp-dx formats)")
@TestClasses(
    "net.bioclipse.spectrum.business.test.APITest," +
    "net.bioclipse.spectrum.business.test.AbstractSpectrumManagerPluginTest"
)
public interface ISpectrumManager extends IBioclipseManager {

    /**
     * Loads a single spectrum from file using jumbo/jcampdx-lib.
     *
     * @param path The path to the file
     * @return loaded spectrum
     * @throws IOException
     * @throws BioclipseException
     * @throws CoreException 
     */
    @Recorded
    @PublishedMethod( params = "String path", 
                      methodSummary = "Loads a spectrum from file. ")
    @TestMethods("testLoadSpectrum_String")
    public JumboSpectrum loadSpectrum( String path )
        throws IOException, BioclipseException, CoreException;

    /**
     * Load spectrum from an <code>IFile</code> using Jumbo/jcampdx-lib.
     * 
     * @param file to be loaded
     * @return loaded spectrum
     * @throws IOException
     * @throws BioclipseException
     * @throws CoreException 
     */
    @PublishedMethod( params = "IFile file", 
            methodSummary = "Loads a spectrum from file. ")
    @Recorded
    @TestMethods("testLoadSpectrum_IFile")
    public JumboSpectrum loadSpectrum( IFile file )
        throws IOException, BioclipseException, CoreException;
    
    /**
     * Load spectrum from an <code>InputStream</code> using Jumbo/jcampdx-lib.
     * 
     * @param instream The stream to load from
     * @param fileExtension cml or jdx (used for file type detection)
     * @return loaded spectrum
     * @throws IOException
     * @throws BioclipseException
     * @throws CoreException 
     */
    @PublishedMethod( params = "InputStream instream, String fileExtension", 
            methodSummary = "Loads a spectrum from an InputStream. ")
    @Recorded
    @TestMethods("testLoadSpectrum_IFile")
    public JumboSpectrum loadSpectrum(InputStream instream, String fileExtension) 
        throws IOException, BioclipseException, CoreException;
    
    /**
     * Saves a spectrum. The file gets overwritten if it already exists. If using this in code, 
     * you should check and ask before.
     * 
     * @param mol The molecule to save
     * @param filename File to save to relativ to workspace
     * @param filetype Which format to save (for formats, see constants)
     * @throws IllegalStateException
     */
    @PublishedMethod ( params = "JumboSpectrum spectrum, String filename, String filetype",
            methodSummary = "Saves a jummo spectrum to a file, filetype being 'cml' or 'jdx'" )
    @Recorded
    @TestMethods("testSaveSpectrum_IJumboSpectrum_String_String")
    public void saveSpectrum(IJumboSpectrum spectrum, String filename, String filetype) 
    	throws BioclipseException, CoreException;
    
    /**
     * Saves a spectrum. The file gets overwritten if it already exists. If using this in code, 
     * you should check and ask before.
     * 
     * @param mol The molecule to save
     * @param target Where to save
     * @param filetype Which format to save (for formats, see constants)
     * @throws IllegalStateException
     */
    @PublishedMethod ( params = "JumboSpectrum spectrum, IFile target, String filetype",
            methodSummary = "Saves a jumbo spectrum to a file, filetype being 'cml' or 'jdx'" )
    @Recorded
    @TestMethods("testSaveSpectrum_JumboSpectrum_IFile_String")
    public void saveSpectrum(IJumboSpectrum spectrum, IFile target, String filetype) 
    	throws BioclipseException, CoreException;

    
    /**
     * Creates a jumbo spectrum from an ISpectrum
     * 
     * @param m
     * @return
     * @throws BioclipseException 
     */
    @PublishedMethod ( params = "ISpectrum m",
                       methodSummary = "Creates a jumbo spectrum from a " +
                                       "spectrum (any implementation of ISpectrum "+
                                       "and not necessarily a Jumbo one).")
    @Recorded
    @TestMethods("testCreate_ISpectrum")
    public IJumboSpectrum create( ISpectrum s ) throws BioclipseException;

    /**
     * Creates an empty jumbo spectrum.
     * 
     * @return
     * @throws BioclipseException 
     */
    @PublishedMethod ( params = "",
                       methodSummary = "Creates an empty jumbo spectrum.")
    @Recorded
    @TestMethods("testCreateEmpty")
    public IJumboSpectrum createEmpty() throws BioclipseException;

    /**
     * Creates a jumbo spectrum from a CML String
     * 
     * @param cml The CML spectrum as a string
     * @return The created IJumboSpectrum
     * @throws BioclipseException if input is null or parse fails
     * @throws IOException if file cannot be read
     */
    @PublishedMethod ( params = "String cml",
                       methodSummary = "Creates a jumbo spectrum from a " +
                                       "CML String" )
    @Recorded
    @TestMethods("testFromCml")
    public IJumboSpectrum fromCml( String cml ) 
                        throws BioclipseException, IOException;
    
    /**
     * Does a peak picking
     * 
     * @param spectrumInput The spectrum to pick 
     * @return The spectrum with the peaks added
     * @throws BioclipseException if input is null or parse fails
     * @throws IOException if file cannot be read
     */
    @PublishedMethod ( params = "IJumboSpectrum spectruminput",
                       methodSummary = "Does a peak picking on spectruminput and adds a peak list" +
                                       "(existing peaks are conserved" )
    @Recorded
    @TestMethods("testPickPeaks_IJumboSpectrum")
    public IJumboSpectrum pickPeaks(IJumboSpectrum spectruminput);

    /**
     * Calculates a weighted cross correlation similarity of two spectra
     * 
     * @param  positions1 Shifts of first spectrum
     * @param  intensities1 Intensities of first spectrum
     * @param  positions2 Shifts of second spectrum
     * @param  intensities2 Intensities of second spectrum
     * @param  width
     * @return The similarity
     */
    @PublishedMethod (
        doi="10.1002/1096-987X(200102)22:3<273::AID-JCC1001>3.0.CO;2-0",
        params = "double[] positions1, double[] intensities1,"+
            "double[] positions2, double[] intensities2,double width",
        methodSummary = "Calculates weighted cross correlation of two set of" +
            " peaks and intensities (see the paper behind the given DOI)" )
    @Recorded
    @TestMethods("testCalculateSimilarityWCCWithIntensities")
    public double calculateSimilarityWCC( double[] positions1, double[] intensities1,
                            double[] positions2, double[] intensities2,
                            double width );
    
    /**
     * Calculates a weighted cross correlation similarity of two spectra
     * 
     * @param  positions1 Shifts of first spectrum
     * @param  positions2 Shifts of second spectrum
     * @param  width
     * @return The similarity
     */
    @PublishedMethod (
        doi="10.1002/1096-987X(200102)22:3<273::AID-JCC1001>3.0.CO;2-0",
        params = "double[] positions1, double[] positions2, double width",
        methodSummary = "Calculates weighted cross correlation of two sets of" +
            "peaks. Intensities are set to 1 (see the paper behind the" +
            "given DOI)." )
    @Recorded
    @TestMethods("testCalculateSimilarityWCC")
    public double calculateSimilarityWCC( double[] positions1,
                            double[] positions2,
                            double width );

    /**
     * Calculates a weighted cross correlation similarity of two spectra.
     * 
     * @param  spectrum1 The first spectrum
     * @param  spectrum2 The second spectrum
     * @param  width
     * @return The similarity
     */
    @PublishedMethod (
        doi="10.1002/1096-987X(200102)22:3<273::AID-JCC1001>3.0.CO;2-0",
        params = "ISpectrum spectrum1, ISpectrum spectrum2, double width",
        methodSummary = "Calculates weighted cross correlation of peaks in " +
            "two spectra (see the paper behind the given DOI). Both" +
            "spectra must have a peak list.")
    @Recorded
    @TestMethods("testCalculateSimilarityWCC_ISpectrum_ISpectrum")
    public double calculateSimilarityWCC( ISpectrum spectrum1, ISpectrum spectrum2,
                            double width ) throws BioclipseException;


    /**
     * Detects specterum file types
     * 
     * @param  extension The fiel extension
     * @return The type (SpectrumEditor.CML_TYPE or SpectrumEditor.JCAMP_TYPE)
     */
    @PublishedMethod ( params = "String extension",
       methodSummary = "Detects a spectrumfiletype based on "+
                       "extension which must be the file extension "+
                       "without dot. Result is "+
                       "SpectrumEditor.CML_TYPE or SpectrumEditor.JCAMP_TYPE.")
    @Recorded
    @TestMethods("testDetectFileType")
    public String detectFileType( String extension ) throws BioclipseException;
}