package net.bioclipse.spectrum.business.test;

import net.bioclipse.spectrum.Activator;

import org.junit.BeforeClass;

public class JavaScriptSpectrumManagerPluginTest 
       extends AbstractSpectrumManagerPluginTest {

    @BeforeClass 
    public static void setupCDKManagerPluginTest() throws Exception {
        spectrummanager = Activator.getDefault().getJavaScriptSpectrumManager();
    }
}
