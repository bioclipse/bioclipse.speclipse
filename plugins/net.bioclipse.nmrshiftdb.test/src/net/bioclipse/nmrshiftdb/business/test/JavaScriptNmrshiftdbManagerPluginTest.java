package net.bioclipse.nmrshiftdb.business.test;

import net.bioclipse.nmrshiftdb.Activator;

import org.junit.BeforeClass;

public class JavaScriptNmrshiftdbManagerPluginTest 
       extends AbstractNmrshiftdbManagerPluginTest {

    @BeforeClass 
    public static void setupNmrshiftdbManagerPluginTest() throws Exception {
        nmrshiftdbmmanager = Activator.getDefault().getJavaScriptNmrshiftdbManager();
    }
}
