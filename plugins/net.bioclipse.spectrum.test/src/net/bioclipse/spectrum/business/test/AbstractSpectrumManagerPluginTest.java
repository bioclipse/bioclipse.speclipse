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
package net.bioclipse.spectrum.business.test;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.Assert;
import net.bioclipse.core.MockIFile;
import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.ISpectrum;
import net.bioclipse.spectrum.business.ISpectrumManager;
import net.bioclipse.spectrum.domain.IJumboSpectrum;
import net.bioclipse.spectrum.domain.JumboSpectrum;
import net.bioclipse.spectrum.editor.SpectrumEditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

public abstract class AbstractSpectrumManagerPluginTest{

    protected static ISpectrumManager spectrummanager;


    @Test
    public void testLoadSpectrum_String() throws IOException, 
                                          BioclipseException, 
                                          CoreException, URISyntaxException {

        URI uri = getClass().getResource("/testFiles/aug07.dx").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ISpectrum spectrum = spectrummanager.loadSpectrum( path);
        Assert.assertEquals(0,((IJumboSpectrum)spectrum).getJumboObject().getPeakListElements().size());
        Assert.assertEquals(1,((IJumboSpectrum)spectrum).getJumboObject().getSpectrumDataElements().size());
        uri = getClass().getResource("/testFiles/spectrum3.xml").toURI();
        url=FileLocator.toFileURL(uri.toURL());
        path=url.getFile();
        spectrum = spectrummanager.loadSpectrum( path);
        Assert.assertEquals(0,((IJumboSpectrum)spectrum).getJumboObject().getPeakListElements().size());
        Assert.assertEquals(1,((IJumboSpectrum)spectrum).getJumboObject().getSpectrumDataElements().size());
    }
    
    @Test
    public void testSaveSpectrum_JumboSpectrum_IFile_String() throws URISyntaxException, IOException, BioclipseException, CoreException{
        URI uri = getClass().getResource("/testFiles/aug07.dx").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ISpectrum spectrum = spectrummanager.loadSpectrum( path);
        IFile target=new MockIFile();
        spectrummanager.saveSpectrum((JumboSpectrum)spectrum, target, SpectrumEditor.JCAMP_TYPE);
        byte[] bytes=new byte[3];
        target.getContents().read(bytes);
        Assert.assertEquals(35, bytes[0]);
        Assert.assertEquals(35, bytes[1]);
        Assert.assertEquals(84, bytes[2]);
    }
    
    @Test
    public void testSaveSpectrum_IJumboSpectrum_String_String() throws URISyntaxException, IOException, BioclipseException, CoreException{
        URI uri = getClass().getResource("/testFiles/spectrum3.xml").toURI();
        URL url = FileLocator.toFileURL(uri.toURL());
        String path = url.getFile();
        JumboSpectrum spectrum = spectrummanager.loadSpectrum( path);
        String filename = "/Virtual/testSaveSpectrum"+System.currentTimeMillis()+".jdx";
        spectrummanager.saveSpectrum(spectrum, filename, SpectrumEditor.JCAMP_TYPE);
  	    byte[] bytes=new byte[1000];
        IFile file= ResourcePathTransformer.getInstance().transform(filename);
        file.getContents().read(bytes);
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<bytes.length;i++){
             sb.append((char)bytes[i]);
        }
        assertEquals(0, sb.toString().indexOf( "##TITLE=" ));
    }
    
    @Test
    public void testLoadSpectrum_IFile() throws IOException, 
                                          BioclipseException, 
                                          CoreException, URISyntaxException {

        URI uri = getClass().getResource("/testFiles/spectrum3.xml").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ISpectrum spectrum = spectrummanager.loadSpectrum( new MockIFile(path));
        Assert.assertEquals(0,((IJumboSpectrum)spectrum).getJumboObject().getPeakListElements().size());
        Assert.assertEquals(1,((IJumboSpectrum)spectrum).getJumboObject().getSpectrumDataElements().size());
    }
    
    @Test
    public void testLoadSpectrum_InputStream_String() throws IOException, 
                                          BioclipseException, 
                                          CoreException, URISyntaxException {

        URI uri = getClass().getResource("/testFiles/spectrum3.xml").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ISpectrum spectrum = spectrummanager.loadSpectrum( new MockIFile(path).getContents(),SpectrumEditor.CML_TYPE);
        Assert.assertEquals(0,((IJumboSpectrum)spectrum).getJumboObject().getPeakListElements().size());
        Assert.assertEquals(1,((IJumboSpectrum)spectrum).getJumboObject().getSpectrumDataElements().size());
    }
    
    @Test
    public void testFromCml() throws BioclipseException, IOException, URISyntaxException{
        URI uri = getClass().getResource("/testFiles/spectrum3.xml").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        FileInputStream fis=new FileInputStream(path);
        StringBuffer strContent=new StringBuffer();
        int ch;
        while( (ch = fis.read()) != -1)
          strContent.append((char)ch);
        ISpectrum spectrum=spectrummanager.fromCml(strContent.toString());
        Assert.assertEquals(0,((IJumboSpectrum)spectrum).getJumboObject().getPeakListElements().size());
        Assert.assertEquals(1,((IJumboSpectrum)spectrum).getJumboObject().getSpectrumDataElements().size());      
    }

    @Test
    public void testCreate_ISpectrum() throws IOException, 
                                          BioclipseException, 
                                          CoreException, URISyntaxException {
        URI uri = getClass().getResource("/testFiles/spectrum3.xml").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ISpectrum spectrum = spectrummanager.loadSpectrum( new MockIFile(path));
        IJumboSpectrum jumbospectrum=spectrummanager.create(spectrum);
        Assert.assertTrue(jumbospectrum.getJumboObject().toXML().contains("<metadata name=\"dc:origin\">D.HENNEBERG, MAX-PLANCK INSTITUTE, MULHEIM, WEST GERMANY</metadata>"));
    }

    @Test
    public void testPickPeaks_IJumboSpectrum() throws IOException, 
                                          BioclipseException, 
                                          CoreException, URISyntaxException {
        URI uri = getClass().getResource("/testFiles/spectrum3.xml").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ISpectrum spectrum = spectrummanager.loadSpectrum( new MockIFile(path));
        IJumboSpectrum jumbospectrum=spectrummanager.pickPeaks((IJumboSpectrum)spectrum);
        Assert.assertEquals(1,jumbospectrum.getJumboObject().getPeakListElements().get(0).getPeakElements().size());
    }
    
    @Test
    public void testCalculateSimilarityWCCWithIntensities() throws Exception{
        double[] peaks1=new double[]{1,2,3};
        double[] peaks2=new double[]{1,2,3};
        double[] intensities1=new double[]{1,1,0.5};
        double[] intensities2=new double[]{1,1,0.5};
        double similarity=spectrummanager.calculateSimilarityWCC( peaks1, intensities1, peaks2, intensities2, 1 );
        Assert.assertEquals( 1.0,similarity,0.01 );
        peaks2=new double[]{1,2};
        intensities2=new double[]{1,0.5};
        similarity=spectrummanager.calculateSimilarityWCC( peaks1, intensities1, peaks2, intensities2, 1 );
        Assert.assertEquals( 0.89,similarity,0.01 );
    }
    
    @Test
    public void testCalculateSimilarityWCC() throws Exception{
        double[] peaks1=new double[]{1,2,3};
        double[] peaks2=new double[]{1,2,3};
        double similarity=spectrummanager.calculateSimilarityWCC( peaks1, peaks2, 1 );
        Assert.assertEquals( 1.0,similarity,0.01 );
        peaks2=new double[]{1,2};
        similarity=spectrummanager.calculateSimilarityWCC( peaks1, peaks2, 1 );
        Assert.assertEquals( 0.81,similarity,0.01 );
    }
    
    @Test
    public void testCalculateSimilarityWCC_ISpectrum_ISpectrum() throws Exception{
        URI uri = getClass().getResource("/testFiles/spectrum2-local.xml").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ISpectrum spectrum = spectrummanager.loadSpectrum( new MockIFile(path));
        double similarity=spectrummanager.calculateSimilarityWCC( spectrum, spectrum, 1 );
        Assert.assertEquals( 1.0,similarity,0.01 );
    }
    
    @Test
    public void testDetectFileType() throws Exception{
        URI uri = getClass().getResource("/testFiles/spectrum3.xml").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        Assert.assertEquals( SpectrumEditor.CML_TYPE, spectrummanager.detectFileType( new MockIFile(path).getFileExtension() ));
        uri = getClass().getResource("/testFiles/aug07.dx").toURI();
        url=FileLocator.toFileURL(uri.toURL());
        path=url.getFile();
        Assert.assertEquals( SpectrumEditor.JCAMP_TYPE, spectrummanager.detectFileType( new MockIFile(path).getFileExtension() ));
    }
    
    @Test
    public void testLoadAndPeakPickWithHkoExample() throws Exception{
        URI uri = getClass().getResource("/testFiles/spectrum3.xml").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ISpectrum spectrum = spectrummanager.loadSpectrum( new MockIFile(path));
        Assert.assertEquals(0,((IJumboSpectrum)spectrum).getJumboObject().getPeakListElements().size());
        Assert.assertEquals(1,((IJumboSpectrum)spectrum).getJumboObject().getSpectrumDataElements().size());
        IJumboSpectrum peakspectrum=spectrummanager.pickPeaks((IJumboSpectrum)spectrum);
        Assert.assertEquals(1,peakspectrum.getJumboObject().getPeakListElements().get(0).getPeakElements().size());
        Assert.assertEquals(42.0,peakspectrum.getJumboObject().getPeakListElements().get(0).getPeakElements().get( 0 ).getXValue(),.1);
        
    }
    
    @Test
    public void testCreateEmpty() throws BioclipseException{
    	IJumboSpectrum spectrum = spectrummanager.createEmpty();
    	Assert.assertEquals(1, spectrum.getJumboObject().getPeakListElements().size());
    }
}
