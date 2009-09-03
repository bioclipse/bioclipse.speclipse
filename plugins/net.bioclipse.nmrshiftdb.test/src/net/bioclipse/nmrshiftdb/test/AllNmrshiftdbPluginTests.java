package net.bioclipse.nmrshiftdb.test;

import net.bioclipse.nmrshiftdb.business.test.JavaNmrshiftdbManagerPluginTest;
import net.bioclipse.nmrshiftdb.business.test.JavaScriptNmrshiftdbManagerPluginTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  JavaNmrshiftdbManagerPluginTest.class,
  JavaScriptNmrshiftdbManagerPluginTest.class
})
public class AllNmrshiftdbPluginTests {

}
