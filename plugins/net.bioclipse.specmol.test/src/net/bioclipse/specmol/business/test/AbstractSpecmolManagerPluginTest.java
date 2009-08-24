package net.bioclipse.specmol.business.test;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import net.bioclipse.core.MockIFile;
import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.ISpecmol;
import net.bioclipse.specmol.business.ISpecmolManager;
import net.bioclipse.specmol.domain.IJumboSpecmol;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.junit.Assert;
import org.junit.Test;


public abstract class AbstractSpecmolManagerPluginTest {
    protected static ISpecmolManager specmolmanager;

   
    @Test
    public void testLoadSpecmol_String() throws IOException, 
                                          BioclipseException, 
                                          CoreException, URISyntaxException {

        URI uri = getClass().getResource("/testFiles/Maaranolide_G.xml").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ISpecmol spectrum = specmolmanager.loadSpecmol(path);
        Assert.assertEquals(1,((IJumboSpecmol)spectrum).getJumboObject().getChildCMLElements( "molecule" ).size());
        Assert.assertEquals(1,((IJumboSpecmol)spectrum).getJumboObject().getChildCMLElements( "spectrum" ).size());
    }
    
    @Test
    public void testSaveSpecmol_IJumboSpecmol_IFile() throws URISyntaxException, IOException, BioclipseException, CoreException{
        URI uri = getClass().getResource("/testFiles/Maaranolide_G.xml").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        ISpecmol spectrum = specmolmanager.loadSpecmol( path);
        IFile target=new MockIFile();
        specmolmanager.saveSpecmol((IJumboSpecmol)spectrum, target);
        byte[] bytes=new byte[3];
        target.getContents().read(bytes);
        Assert.assertEquals(60, bytes[0]);
        Assert.assertEquals(63, bytes[1]);
        Assert.assertEquals(120, bytes[2]);
    }
    
    @Test
    public void testSaveSpecmol_IJumboSpecmol_String() throws URISyntaxException, IOException, BioclipseException, CoreException{
        URI uri = getClass().getResource("/testFiles/Maaranolide_G.xml").toURI();
        URL url = FileLocator.toFileURL(uri.toURL());
        String path = url.getFile();
        IJumboSpecmol spectrum = specmolmanager.loadSpecmol( path);
        specmolmanager.saveSpecmol(spectrum, "/Virtual/testSaveSpecmol.cml");
        IFile file= ResourcePathTransformer.getInstance().transform("/Virtual/testSaveSpecmol.cml");
        byte[] bytes=new byte[1000];
        file.getContents().read(bytes);
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<bytes.length;i++){
             sb.append((char)bytes[i]);
        }
        assertEquals(0, sb.toString().indexOf( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
    }
    
    
    @Test
    public void testLoadSpecmol_IFile_IProgressMonitor() throws Exception {
        URI uri = getClass().getResource("/testFiles/Maaranolide_G.xml").toURI();
        URL url = FileLocator.toFileURL(uri.toURL());
        String path = url.getFile();
        ISpecmol spectrum = specmolmanager.loadSpecmol( new MockIFile(path));
        Assert.assertEquals(1,((IJumboSpecmol)spectrum).getJumboObject().getChildCMLElements( "molecule" ).size());
        Assert.assertEquals(1,((IJumboSpecmol)spectrum).getJumboObject().getChildCMLElements( "spectrum" ).size());
    }
    
    @Test
    public void testLoadSpecmol_IFile() throws Exception {

        URI uri = getClass().getResource("/testFiles/Maaranolide_G.xml").toURI();
        URL url = FileLocator.toFileURL(uri.toURL());
        String path = url.getFile();
        ISpecmol spectrum = specmolmanager.loadSpecmol( new MockIFile(path));
        Assert.assertEquals(1,((IJumboSpecmol)spectrum).getJumboObject().getChildCMLElements( "molecule" ).size());
        Assert.assertEquals(1,((IJumboSpecmol)spectrum).getJumboObject().getChildCMLElements( "spectrum" ).size());
    }
    
    
    @Test
    public void testFromCml() throws Exception{
      URI uri = getClass().getResource("/testFiles/Maaranolide_G.xml").toURI();
      URL url = FileLocator.toFileURL(uri.toURL());
      String path = url.getFile();
      FileInputStream fis=new FileInputStream(path);
      StringBuffer strContent=new StringBuffer();
      int ch;
      while( (ch = fis.read()) != -1)
        strContent.append((char)ch);
      ISpecmol spectrum=specmolmanager.fromCml(strContent.toString());
      Assert.assertEquals(1,((IJumboSpecmol)spectrum).getJumboObject().getChildCMLElements( "molecule" ).size());
      Assert.assertEquals(1,((IJumboSpecmol)spectrum).getJumboObject().getChildCMLElements( "spectrum" ).size());      
    }

    @Test
    public void testCreate_ISpecmol() throws Exception{
        URI uri = getClass().getResource("/testFiles/Maaranolide_G.xml").toURI();
        URL url = FileLocator.toFileURL(uri.toURL());
        String path = url.getFile();
        ISpecmol spectrum = specmolmanager.loadSpecmol( new MockIFile(path) );
        IJumboSpecmol jumbospectrum=specmolmanager.create(spectrum);
        Assert.assertTrue(jumbospectrum.getJumboObject().toXML().contains("<atom id=\"a1\" elementType=\"C\" x2=\"387.1799134054031\" y2=\"232.7228590545007\" formalCharge=\"0\" hydrogenCount=\"0\" />"));
    }
}
