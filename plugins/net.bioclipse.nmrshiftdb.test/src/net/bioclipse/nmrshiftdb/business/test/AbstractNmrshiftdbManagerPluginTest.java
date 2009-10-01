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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.domain.ISpecmol;
import net.bioclipse.core.domain.ISpectrum;
import net.bioclipse.nmrshiftdb.business.INmrshiftdbManager;
import net.bioclipse.spectrum.domain.IJumboSpectrum;
import net.bioclipse.spectrum.domain.JumboSpectrum;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.cml.base.CMLElement;

public abstract class AbstractNmrshiftdbManagerPluginTest{

    protected static INmrshiftdbManager nmrshiftdbmmanager;
    

    @Test
    public void testGeneralSearch() throws URISyntaxException, MalformedURLException, IOException, BioclipseException, CoreException{
        List<ISpecmol> result=nmrshiftdbmmanager.generalSearch("subergorgiol", "exact","chemical name", "http://www.ebi.ac.uk/nmrshiftdb/axis");
        Assert.assertEquals( result.size(),1 );
    }

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
        }catch(BioclipseException ex){
        	ex.printStackTrace();
        	Assert.fail("Failed. Perhaps no local instance of nmrshiftdb running?");
        }
    }
    
    @Test
    public void testSearchBySpectrum() throws URISyntaxException, MalformedURLException, IOException, BioclipseException, CoreException{
    	URI uri = getClass().getResource("/testFiles/testspectrum.cml").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        JumboSpectrum cmlspectrum = net.bioclipse.spectrum.Activator.getDefault()
            .getJavaSpectrumManager().loadSpectrum( path );
        List<ICDKMolecule> result=nmrshiftdbmmanager.searchBySpectrum(cmlspectrum, true, "http://www.ebi.ac.uk/nmrshiftdb/axis");
        Assert.assertTrue( result.size()>0 );
        Assert.assertEquals( "100.00 %", result.get(0).getAtomContainer().getProperty("similarity"));
    }
    
    @Test
    public void testPredictRemote() throws URISyntaxException, MalformedURLException, IOException, BioclipseException, CoreException{
    	URI uri = getClass().getResource("/testFiles/subergorgiol.mol").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        IMolecule mol = net.bioclipse.cdk.business.Activator.getDefault().
        	getJavaCDKManager().loadMolecule(path);
        ISpectrum result=nmrshiftdbmmanager.predictSpectrum(mol, "13C", false, false,  "http://www.ebi.ac.uk/nmrshiftdb/axis");
        Assert.assertEquals(15, ((IJumboSpectrum)result).getJumboObject().getPeakListElements().get(0).getPeakChildren().size() );
    }

	@Test
	public void testPredictLocal() throws URISyntaxException, MalformedURLException, IOException, BioclipseException, CoreException{
		URI uri = getClass().getResource("/testFiles/subergorgiol.mol").toURI();
	    URL url=FileLocator.toFileURL(uri.toURL());
	    String path=url.getFile();
	    IMolecule mol = net.bioclipse.cdk.business.Activator.getDefault().
	    	getJavaCDKManager().loadMolecule(path);
	    ISpectrum result=nmrshiftdbmmanager.predictSpectrum(mol, "13C", false, true, null);
	    Assert.assertEquals(15, ((IJumboSpectrum)result).getJumboObject().getPeakListElements().get(0).getPeakChildren().size() );
	}
}