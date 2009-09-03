/*******************************************************************************
 * Copyright (c) 2007-2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org�epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     shk3
 *     
 *******************************************************************************/
package net.bioclipse.nmrshiftdb.business.test;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.nmrshiftdb.business.INmrshiftdbManager;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.cml.base.CMLElement;

public abstract class AbstractNmrshiftdbManagerPluginTest{

    protected static INmrshiftdbManager nmrshiftdbmmanager;


    @Test
    public void testSubmitSpecmol() throws IOException, 
                                          BioclipseException, 
                                          CoreException, URISyntaxException {
        URI uri = getClass().getResource("/testFiles/Maaranolide_G.xml").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        CMLElement cmlcml = net.bioclipse.cml.managers.Activator.getDefault()
            .getJavaManager().parseFile( path );
        try{
	        String id=nmrshiftdbmmanager.submitSpecmol( cmlcml, "http://localhost:8080/axis",
	                                          "shk3","test");
	        int idint = Integer.parseInt( id );
	        Assert.assertTrue( idint>0 );
        }catch(UndeclaredThrowableException ex){
        	ex.getUndeclaredThrowable().printStackTrace();
        	Assert.fail("Failed. Perhaps no local instance of nmrshiftdb running?");
        }
    }
}
