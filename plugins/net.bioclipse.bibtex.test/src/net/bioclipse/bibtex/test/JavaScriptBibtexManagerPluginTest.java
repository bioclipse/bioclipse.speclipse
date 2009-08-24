package net.bioclipse.bibtex.test;

import org.junit.BeforeClass;
import net.bioclipse.bibtex.Activator;

public class JavaScriptBibtexManagerPluginTest 
       extends AbstractBibtexManagerPluginTest {

    @BeforeClass 
    public static void setupCDKManagerPluginTest() throws Exception {
        bibtexmanager = Activator.getDefault().getJSBibtexManager();
    }
}
