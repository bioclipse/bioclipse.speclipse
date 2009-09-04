package net.bioclipse.nmrshiftdb.business;

import java.util.List;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.PublishedClass;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.TestClasses;
import net.bioclipse.core.TestMethods;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.ISpecmol;
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
  @PublishedMethod( params = "ISpectrum cmlspectrum, boolean subortotal"+
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

  @Recorded
  @PublishedMethod( params = "String searchstring, String searchtype, String searchfield"+
                             ", String serverurl",
                    methodSummary = "Does a search for NMRShiftDB datasets at "+
                        "serverurl (must point to axis instance)." +
                        "Returns a list of Assigned Spectra. Possible values for "+
                        "searchtype are 'exact', 'regular expression', 'fuzzy' "+
                        " and 'fragment'. Possible values for searchfield are "+
                        "'subspectrum search', 'total similarity spectrum search'"+
                        "'my spectra', 'molecular weight search', 'condition search', "+
                        "'molecule id', 'spectrum id', 'HOSE code', 'double bond equivalents', "+
                        "'number of rings in smallest set of smallest rings', 'double bond equivalents/smallest set of smallest rings' "+
                        "'similarity search', 'substructure search', 'identity search', '"+
                        "'search by molecule keyword', 'search by spectrum keyword', "+
                        "'search by molecule keyword fragements', 'search by spectrum keyword fragements', "+
                        "'literature/title', 'literature/author', 'comment', 'canonical name', "+
                        "'description of spectrum link', 'description of molecule link', 'molecule keyword', "+
                        "'spectrum keyword', 'CAS number', 'chemical name', 'chemical name using Pubchem', "+
                        "'chemical formula', 'chemical formula (with other elements)', 'multiplicities' or "+
                        "'potential multiplicities'. Searchstring must be a text, "+
                        "exept for 'similarity search', 'substructure search', 'identity search', "+
                        "where a CML structure is needed and 'subspectrum search'/'total similarity spectrum search'"+
                        "where a spectrum must given like TODO.")
  @TestMethods("testGeneralSearch")
  public List<ISpecmol> generalSearch(String searchstring, String searchtype, 
		  	String searchfield, String serverurl) throws BioclipseException;


  
  public void generalSearch(String searchstring, String searchtype, String searchfield,
          String serverurl, BioclipseUIJob<List<ISpecmol>> uiJob) throws BioclipseException;
}
