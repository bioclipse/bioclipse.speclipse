package net.bioclipse.nmrshiftdb.business;

import java.util.List;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.PublishedClass;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.TestClasses;
import net.bioclipse.core.TestMethods;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.ISpectrum;
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
                               String password) throws BioclipseException;


  
  public void submitSpecmol(CMLElement cmlcml,
                            String serverurl,
                            String username,
                            String password, BioclipseUIJob<String> uiJob) throws BioclipseException;

  @Recorded
  @PublishedMethod( params = "CMLSpectrum cmlspectrum, boolean subortotal"+
                             ", String serverurl",
                    methodSummary = "Does a search for spectra at "+
                        "serverurl (must point to axis instance)." +
                        "Returns a list of molecules, the AtomContainers "+
                        "retrieved with getAtomContainer have a property " +
                        "'similarity' set, which gives similarity in % as String")
  @TestMethods("testSearchBySpectrum")
  public List<ICDKMolecule> searchBySpectrum(ISpectrum cmlspectrum,
                               boolean subortotal,
                               String serverurl) throws BioclipseException;


  
  public void searchBySpectrum(ISpectrum cmlspectrum,
          boolean subortotal,
          String serverurl, BioclipseUIJob<List<ICDKMolecule>> uiJob) throws BioclipseException;

}
