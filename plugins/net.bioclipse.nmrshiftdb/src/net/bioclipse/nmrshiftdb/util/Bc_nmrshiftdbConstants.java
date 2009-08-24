/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stefan Kuhn
 *     
 ******************************************************************************/
package net.bioclipse.nmrshiftdb.util;

public class Bc_nmrshiftdbConstants {
	public static String server="http://www.ebi.ac.uk/nmrshiftdb/axis";
	public static String doublebondconfiguration="doublebondconfiguration";
	
	
	
	  //These are the possible review flags in table Spectrum
	public static String EDITED="edited";
	public static String TRUE="true";
	public static String FALSE="false";
	public static String REJECTED="rejected";
  public static String CHANGE="change";
  //This are possible entries in SearchHistory
  public static String CALCULATEDONLY="calculated spectra only";
  public static String MEASUREDONLY="measured spectra only";
  public static String EXACT="exact";
  public static String REGEXP="regular expression";
  public static String FUZZY="fuzzy";
  public static String FRAGMENT="fragment";
  public static String SUBSPECTRUM="subspectrum search";
  public static String TOTALSPECTRUM="total similarity spectrum search";
  public static String SUBSTRUCTURE_SIMILARITY="similarity search";
  public static String SUBSTRUCTURE_EXACT="substructure search";
  public static String TOTALSTRUCTURE="identity search";
  public static String MOLECULEKEYWORDS_TOTAL="search by molecule keyword";
  public static String SPECTRUMKEYWORDS_TOTAL="search by spectrum keyword";
  public static String MOLECULEKEYWORDS_FRAGMENT="search by molecule keyword fragements";
  public static String SPECTRUMKEYWORDS_FRAGMENT="search by spectrum keyword fragements";
  public static String LITERATURE_TITLE="literature/title";
  public static String LITERATURE_AUTHOR="literature/author";
  public static String COMMENT="comment";
  public static String CANNAME="canonical name";
  public static String SPECLINK="description of spectrum link";
  public static String MOLLINK="description of molecule link";
  public static String MOLKEY="molecule keyword";
  public static String SPECKEY="spectrum keyword";
  public static String CASNUMBER="CAS number";
  public static String CHEMNAME="chemical name";
  public static String CHEMNAMEPUBCHEM="chemical name using Pubchem";
  public static String FORMULA="chemical formula";
  public static String FORMULA_WITH_OTHER="chemical formula (with other elements)";
  public static String MULTIPLICITY="multiplicities";
  public static String POTMULTIPLICITY="potential multiplicities";
  public static String MYSPECTRA="my spectra";
  public static String BROWSE="browse all structures";
  public static String WEIGHT="molecular weight search";
  public static String CONDITIONS="condition search";
  public static String MOLECULE_NR="molecule id";
  public static String SPECTRUM_NR="spectrum id";
  public static String HOSECODE="HOSE code";
  public static String DBE="double bond equivalents";
  public static String SSSR="number of rings in smallest set of smallest rings";
  public static String DBE_RINGS="double bond equivalents/smallest set of smallest rings";
  //Actions
  public static String DELETE_SEARCH_HISTORY="Clear history";
  public static String UPLOAD_JCAMP_SEARCH="uploadjcampsearch";
  //Session attributes
  public static String STRUCTURESHISTORY="structureshistory";
  public static String SEARCHHISTORY="searchhistory";
  public static String INPUT="input";
  public static String MOLFILE="molfile";
  public static String MOLFILEH="molfileh";
  //Type of literature
  public static String JOURNAL_ARTICLE="journal_article";
  public static String MONOGRAPH="monograph";
  public static String OTHER_ARTICLE="other_article";
  
  public static String UNREPORTED="Unreported";
  public static String UNKNOWN="Unknown";
  
  public static String METADATA="metadata";
  public static String SUBSTANCE="substance";
  public static String CONDITION="condition";
  
  //These are used for handling of properties in cml submits. need to duplicate the Bc_nmrshfitdbConstants in bc_nmrshiftdb of bioclipse
	public static String frequency="jcamp:dotOBSERVEFREQUENCY";
	public static String solvent="jcamp:dotSOLVENTNAME";
	public static String temperature="jcamp:TEMPERATURE";
	public static String assignment="nmr:assignmentMethod";
	//public static String nucleus="nmr:OBSERVENUCLEUS"; is in SpecmolEditor since used in specmol as well
	public static String nmrid="nmr:nmrshiftdbid";

}
