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
package net.bioclipse.specmol.business;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import net.bioclipse.core.PublishedClass;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.TestClasses;
import net.bioclipse.core.TestMethods;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.ISpecmol;
import net.bioclipse.managers.business.IBioclipseManager;
import net.bioclipse.specmol.domain.IJumboSpecmol;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

@PublishedClass( "Handles CML files containing assigned spectra")
@TestClasses(
    "net.bioclipse.specmol.business.test.APITest," +
    "net.bioclipse.specmol.business.test.AbstractSpecmolManagerPluginTest"
)
public interface ISpecmolManager extends IBioclipseManager {

    /**
     * Loads an assigned spectrum from file using jumbo.
     *
     * @param path The path to the file
     * @return loaded specmol
     * @throws IOException
     * @throws BioclipseException
     * @throws CoreException 
     */
    @Recorded
    @PublishedMethod( params = "String path", 
                      methodSummary = "Loads an assigned spectrum from file. ")
    @TestMethods("testLoadSpecmol_String")
    public IJumboSpecmol loadSpecmol( String path )
        throws IOException, BioclipseException, CoreException;

    /**
     * Load assigned spectrum from an <code>IFile</code> using Jumbo.
     * 
     * @param file to be loaded
     * @return loaded assigned spectrum
     * @throws IOException
     * @throws BioclipseException
     * @throws CoreException 
     */
    @PublishedMethod( params = "IFile file", 
            methodSummary = "Loads an assigned spectrum from file. ")
    @Recorded
    @TestMethods("testLoadSpecmol_IFile")
    public IJumboSpecmol loadSpecmol( IFile file )
        throws IOException, BioclipseException, CoreException;
    
    
    /**
     * @param mol The assigned spectrum to save
     * @param filename Where to save
     * @param filetype Which format to save (for formats, see constants)
     * @throws UnsupportedEncodingException 
     * @throws IllegalStateException
     */
    @PublishedMethod ( params = "JumboSpecmol spemol, String filename",
            methodSummary = "Saves a jubmo specmol to a file" )
    @Recorded
    @TestMethods("testSaveSpecmol_IJumboSpecmol_String")
    public void saveSpecmol(IJumboSpecmol specmol, String filename) 
    	throws BioclipseException, CoreException, UnsupportedEncodingException;

    /**
     * @param mol The assigned spectrum to save
     * @param target Where to save
     * @param filetype Which format to save (for formats, see constants)
     * @throws UnsupportedEncodingException 
     * @throws IllegalStateException
     */
    @PublishedMethod ( params = "JumboSpecmol spemol, IFile target",
            methodSummary = "Saves a jubmo specmol to a file" )
    @Recorded
    @TestMethods("testSaveSpecmol_IJumboSpecmol_IFile")
    public void saveSpecmol(IJumboSpecmol specmol, IFile target) 
    	throws BioclipseException, CoreException, UnsupportedEncodingException;

    
    /**
     * Creates a jumbo specmol from an ISpecmol
     * 
     * @param m
     * @return
     * @throws BioclipseException 
     * @throws UnsupportedEncodingException 
     */
    @PublishedMethod ( params = "ISpecmol s",
                       methodSummary = "Creates a Jumbo specmol from an" +
                                       " ISpecmol" )
    @Recorded
    @TestMethods("testCreate_ISpecmol")
    public IJumboSpecmol create( ISpecmol s ) throws BioclipseException, UnsupportedEncodingException;

    /**
     * Creates a jumbo specmol from a CML String
     * 
     * @param m
     * @return
     * @throws BioclipseException if input is null or parse fails
     * @throws IOException if file cannot be read
     */
    @PublishedMethod ( params = "String cml",
                       methodSummary = "Creates a jumbo specmol from a " +
                                       "CML String" )
    @Recorded
    @TestMethods("testFromCml")
    public IJumboSpecmol fromCml( String cml ) 
                        throws BioclipseException, IOException;

}