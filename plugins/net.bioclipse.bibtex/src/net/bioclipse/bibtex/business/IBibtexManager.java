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
package net.bioclipse.bibtex.business;

import java.io.IOException;

import net.bioclipse.bibtex.domain.IJabrefBibliodata;
import net.bioclipse.core.PublishedClass;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.TestClasses;
import net.bioclipse.core.TestMethods;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.managers.business.IBioclipseManager;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

@PublishedClass( "Contains Bibtex related methods")
@TestClasses(
    "net.bioclipse.bibtex.test.APITest," +
    "net.bioclipse.bibtex.test.AbstractBibtexManagerPluginTest"
)
public interface IBibtexManager extends IBioclipseManager {

    /**
     * Loads a bibtex file using jabref.
     *
     * @param path The path to the file
     * @return loaded jabref file
     * @throws IOException
     * @throws BioclipseException
     * @throws CoreException 
     */
    @Recorded
    @PublishedMethod( params = "String path", 
                      methodSummary = "Loads a bibtex file using jabref. ")
    @TestMethods("testLoadBibliodata_String")
    public IJabrefBibliodata loadBibliodata( String path )
        throws IOException, BioclipseException, CoreException;

    /**
     * Loads a bibtex file using jabref.
     *
     * @param path The path to the file
     * @return loaded jabref file
     * @throws IOException
     * @throws BioclipseException
     * @throws CoreException 
     */
    @Recorded
    @PublishedMethod( params = "IFile file", 
                      methodSummary = "Loads a bibtex file using jabref. ")
    @TestMethods("testLoadBibliodata_IFile")
    public IJabrefBibliodata loadBibliodata( IFile file )
        throws IOException, BioclipseException, CoreException;
}