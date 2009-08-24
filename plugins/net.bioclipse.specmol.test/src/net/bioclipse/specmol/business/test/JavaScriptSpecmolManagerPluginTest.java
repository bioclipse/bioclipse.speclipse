package net.bioclipse.specmol.business.test;

import net.bioclipse.specmol.Activator;

import org.junit.BeforeClass;

public class JavaScriptSpecmolManagerPluginTest 
       extends AbstractSpecmolManagerPluginTest {

    @BeforeClass 
    public static void setupCDKManagerPluginTest() throws Exception {
        specmolmanager = Activator.getDefault().getJavascriptSpecmolManager();
    }
}
