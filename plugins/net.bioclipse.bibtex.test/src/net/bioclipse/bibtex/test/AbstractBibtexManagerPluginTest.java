package net.bioclipse.bibtex.test;

import java.net.URI;
import java.net.URL;

import net.bioclipse.bibtex.business.IBibtexManager;
import net.bioclipse.bibtex.domain.IJabrefBibliodata;
import net.bioclipse.core.MockIFile;

import org.eclipse.core.runtime.FileLocator;
import org.junit.Assert;
import org.junit.Test;


public abstract class AbstractBibtexManagerPluginTest {
    protected static IBibtexManager bibtexmanager;

   
    @Test
    public void testLoadBibliodata_IFile() throws Exception {
        URI uri = getClass().getResource("/testFiles/test.bib").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        IJabrefBibliodata biblio = bibtexmanager.loadBibliodata( new MockIFile(path) );
        Assert.assertEquals(3,biblio.getJabrefDatabase().getEntryCount());
    }
    
    @Test
    public void testLoadBibliodata_String() throws Exception{
        URI uri = getClass().getResource("/testFiles/test.bib").toURI();
        URL url=FileLocator.toFileURL(uri.toURL());
        String path=url.getFile();
        IJabrefBibliodata biblio = bibtexmanager.loadBibliodata( path );
        Assert.assertEquals(3,biblio.getJabrefDatabase().getEntryCount());
    }
}
