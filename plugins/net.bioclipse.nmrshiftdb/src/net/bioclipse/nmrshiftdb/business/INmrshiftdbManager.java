package net.bioclipse.nmrshiftdb.business;

import net.bioclipse.core.PublishedClass;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.TestClasses;
import net.bioclipse.core.TestMethods;
import net.bioclipse.jobs.BioclipseUIJob;
import net.bioclipse.managers.business.IBioclipseManager;

import org.xmlcml.cml.base.CMLElement;

@PublishedClass("Contains Nmrshiftdb methods")
@TestClasses(
    "net.bioclipse.nmrshiftdb.business.test.APITest," +
    "net.bioclipse.nmrshiftdb.business.test.AbstractNmrshiftdbManagerPluginTest"
)
public interface INmrshiftdbManager extends IBioclipseManager {

  @Recorded
  @PublishedMethod( params = "CMLElement cmlcml, String serverurl,"+
                             "String username, String password",
                    methodSummary = "Submits a SpecMol file to NMRShiftDB at "+
                        "serverurl (must point to axis instance) using username" +
                        "and password.  The cml must adhere to NMRShiftDB "+
                        "convention at http://nmrshiftdb.sourceforge.net/nmrshiftdb-convention.html")
  @TestMethods("testSubmitSpecmol")
  public String submitSpecmol(CMLElement cmlcml,
                               String serverurl,
                               String username,
                               String password);


  
  public void submitSpecmol(CMLElement cmlcml,
                            String serverurl,
                            String username,
                            String password, BioclipseUIJob<String> uiJob);

}
