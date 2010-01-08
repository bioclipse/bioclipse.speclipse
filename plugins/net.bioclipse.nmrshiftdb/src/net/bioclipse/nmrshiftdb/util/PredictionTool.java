package net.bioclipse.nmrshiftdb.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.HOSECodeGenerator;

/**
 *  This class offers a stand-alone prediction based on HOSE codes from NMRShiftDB. Apart from this class, you need a data dump.
 * You can get a jar containing class and dump from any nmrshiftdb server with a URL like http://servername/download/NmrshiftdbServlet/predictor.jar?nmrshiftdbaction=predictor.
 * For an example how to use this class, see the main method. For running this class you need the following additional jars: cdk-core.jar, cdk-extra.jar, JNL.jar.
 * Since the HOSE code table is kept in memory, this class needs a lot of memory depending on the size of the database. We recommand to run the JVM with at least 128 MB memory.
 * Also always set all references to this class to null if you no longer need it in order to have it removed by garbage collection.
 *
 * @author     shk3
 * @created    September 23, 2004
 */
public class PredictionTool {
  private static double[] confidencelimits = null;
  private static HashMap mapsmap = new HashMap();


  /**
   *Constructor for the PredictionTool object
   *
   * @exception  IOException  Problems reading the HOSE code file.
   */
  public PredictionTool() throws IOException {
	String filename = "nmrshiftdb.csv";
	InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);
    BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
    String input;
    while ((input = reader.readLine()) != null) {
        StringTokenizer st2 = new StringTokenizer(input, "|");
        String symbol = st2.nextToken();
        String code = st2.nextToken();
        Double min = new Double(st2.nextToken());
        Double av = new Double(st2.nextToken());
        Double max = new Double(st2.nextToken());
        if (mapsmap.get(symbol) == null) {
          mapsmap.put(symbol, new HashMap());
        }
        ((HashMap) mapsmap.get(symbol)).put(code,new ValueBean(min.floatValue(),av.floatValue(),max.floatValue()));
    }
  }


  /**
   *  The main program for the PredictionTool class.
   *
   * @param  args           The file name of the test mdl file.
   * @exception  Exception  Description of Exception.
   */
  /*public static void main(String[] args) throws Exception {
    MDLV2000Reader mdlreader = new MDLV2000Reader(new FileReader(args[0]));
    IMolecule mol = (IMolecule) mdlreader.read(new org.openscience.cdk.Molecule());
    mol=(IMolecule)AtomContainerManipulator.removeHydrogens(mol);
    new HydrogenAdder().addImplicitHydrogensToSatisfyValency(mol);
    for(int i=0;i<mol.getAtomCount();i++){
    	if(mol.getAtom(i).getHydrogenCount()<0)
    		mol.getAtom(i).setHydrogenCount(0);
    }
    HueckelAromaticityDetector.detectAromaticity(mol, false);
    PredictionTool predictor = new PredictionTool();
    double[] result = predictor.predict(mol, mol.getAtom(1));
  }*/


  /**
   *  This method does a prediction, either from the database or from the mapsmap initialized in the constructor. This should not be used directly when using the stand-alone predictor; use predict() then.
   *
   * @param  comment                    Contains additional text after processing predictRange().
   * @param  mol                        The molecule the atoms comes from.
   * @param  a                          The atom the shift of which to be predicted.
   * @param  commentWithMinMax          Shall min/max values be included in comments.
   * @param  ignoreSpectrum             A molecule to be ignored in the prediction (-1=none).
   * @param  withRange                  Is the range to be calculated as well ? (use only when needed for performance reasons).
   * @param  calculated                 Use calculated spectra.
   * @param  measured                   Use measured spectra.
   * @param  runData                    The current runData object.
   * @param  predictionValuesForApplet  Will become the String to diplay the histogram in the applet, null if not wished.
   * @param  maxSpheresToUse            Restrict number of spheres to use, to use max spheres set -1.
   * @param  cache                      true=Use HOSE_CODES table, false=do join query.
   * @param  hoseCodeOut                Contains the used HOSE_CODE.
   * @param  spheresMax                 Default maximum spheres to use.
   * @param  fromDB                     Do prediction from db or mapsmap?
   * @param  trueonly                   Use only spectra review as true
   * @return                            An array of doubles. Meaning: 0=lower limit, 1=mean, 2=upper limit calculated via confidence limits, 3=median, 4=used spheres, 5=number of values, 6=standard deviation, 7=min value, 8=max value.
   * @exception  Exception              Database problems.
   */
  public static double[] generalPredict(IAtomContainer mol, IAtom a, boolean calculated, boolean measured, int ignoreSpectrum, int ignoreSpectrumEnd, StringBuffer comment, boolean commentWithMinMax, boolean withRange, Map predictionValuesForApplet, int maxSpheresToUse, boolean cache, StringBuffer hoseCodeOut, int spheresMax, boolean trueonly) throws Exception {
	  HOSECodeGenerator hcg = new HOSECodeGenerator();
	  if (maxSpheresToUse == -1 || maxSpheresToUse > spheresMax) {
			  maxSpheresToUse = spheresMax;
	  }
	  double[] returnValues = new double[9];
	    int spheres;
	    for (spheres = maxSpheresToUse; spheres > 0; spheres--) {
	        StringBuffer hoseCodeBuffer = new StringBuffer();
	        StringTokenizer st = new StringTokenizer(hcg.getHOSECode(mol, a, maxSpheresToUse,false), "()/");
	        for (int k = 0; k < spheres; k++) {
	          if (st.hasMoreTokens()) {
	            String partcode = st.nextToken();
	            hoseCodeBuffer.append(partcode);
	          }
	          if (k == 0) {
	            hoseCodeBuffer.append("(");
	          } else if (k == 3) {
	            hoseCodeBuffer.append(")");
	          } else {
	            hoseCodeBuffer.append("/");
	          }
	        }
	        String hoseCode = hoseCodeBuffer.toString();
	        ValueBean l = ((ValueBean) ((HashMap) mapsmap.get(a.getSymbol())).get(hoseCode));
          if (l != null) {
          	  returnValues[0]=l.min;
        	  returnValues[1]=l.average;
        	  returnValues[2]=l.max;
        	  return returnValues;
          }
	    }
  	  returnValues[0]=-1;
	  returnValues[1]=-1;
	  returnValues[2]=-1;
	    return returnValues;
  }

  
  class ValueBean{
	  public float min;
	  public float average;
	  public float max;
	  
	  public ValueBean(float a, float b, float c){
		  min=a;
		  average=b;
		  max=c;
	  }
  }
}

