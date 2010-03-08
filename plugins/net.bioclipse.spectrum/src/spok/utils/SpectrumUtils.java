/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package spok.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.jcamp.parser.JCAMPException;
import org.jcamp.parser.Utils;
import org.jcamp.spectrum.Peak1D;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.base.CMLElements;
import org.xmlcml.cml.base.CMLUtil;
import org.xmlcml.cml.element.CMLConditionList;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMetadataList;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;
import org.xmlcml.cml.element.CMLScalar;
import org.xmlcml.cml.element.CMLSpectrum;
import org.xmlcml.cml.element.CMLSpectrumData;
import org.xmlcml.cml.element.CMLSubstance;
import org.xmlcml.cml.element.CMLSubstanceList;

/**
 * @author Tobias Helmus
 * @created 19. Dezember 2005
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class SpectrumUtils {
	private static HashMap specrumTypeSynonyms = new HashMap();

	private static TreeMap jcampKeys = null;

	private static HashMap jcampGenerics = new HashMap();

	public static String MSSPECTRUMTYPE = "massSpectrum";
	public static String NMRSPECTRUMTYPE = "NMR";
	public static String IRSPECTRUMTYPE = "infrared";
	
	
	/**
	 * @param cmlSpectrum
	 * @return
	 */
	public static BitSet createSpectrumFingerprint(CMLSpectrum cmlSpectrum) {
		CMLElements<CMLPeak> peakList = cmlSpectrum.getPeakListElements()
				.get(0).getPeakElements();
		return createSpectrumFingerprint(peakList);
	}

	public static BitSet createSpectrumFingerprint(CMLElements peakList) {
		double[] xValues = new double[peakList.size()];
		for (int i = 0; i < peakList.size(); i++) {
			CMLPeak peak = (CMLPeak) peakList.get(i);
			xValues[i] = peak.getXValue();
		}
		Arrays.sort(xValues);
		double maxPeak = xValues[xValues.length - 1];
		BitSet fingerprint = new BitSet();
		for (int i = 0; i < Math.ceil(maxPeak / 2.0); i++) {
			int base = i * 2;
			int end = base + 4;
			if (isSignalInRange(xValues, base, end)) {
				fingerprint.set(i + 1);
			}
		}
		return fingerprint;
	}

	/**
	 * @param peakList
	 * @param base
	 * @param end
	 * @return
	 */
	public static boolean isSignalInRange(double[] xValues, int base, int end) {
		for (int i = 0; i < xValues.length; i++) {
			if (xValues[i] >= base && xValues[i] <= end) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param cmlSpec
	 * @return
	 */
	public static List<CMLElement> getPeakElements(CMLSpectrum cmlSpec) {
		List<CMLElement> allpeaks=new ArrayList<CMLElement>();
		CMLElements cmlElements = cmlSpec.getPeakListElements();
		for(int i=0;i<cmlElements.size();i++){
			CMLPeakList cmlPeakList = (CMLPeakList) cmlElements.get(1);
			if(cmlPeakList!=null){
				List<CMLElement> foundpeaks=cmlPeakList.getDescendants("peak", null, true);
				for(int k=0;k<foundpeaks.size();k++)
					allpeaks.add((CMLPeak)foundpeaks.get(k));
			}
		}
		List<CMLElement> foundpeaks=cmlSpec.getDescendants("peak", null, true);
		for(int k=0;k<foundpeaks.size();k++)
			allpeaks.add((CMLPeak)foundpeaks.get(k));
		return allpeaks;
	}
	
	
	/**
	 * @param spectrum
	 * @return
	 */
	public static double getHighestY(CMLSpectrum spectrum) {
		List<CMLElement> peaks = getPeakElements(spectrum);
		CMLPeak peak = getHighestPeak(peaks);
		return peak.getYValue();
	}

	/**
	 * @param peaks
	 * @return
	 */
	private static CMLPeak getHighestPeak(List<CMLElement> peaks) {
		Iterator<CMLElement> it = peaks.iterator();
		CMLPeak highestPeak = new CMLPeak();
		highestPeak.setYValue(0);
		highestPeak.setXValue(0);
		while (it.hasNext()) {
			CMLPeak peak = (CMLPeak)it.next();
			if (peak.getYValue() > highestPeak.getYValue()) {
				highestPeak = peak;
			}
		}
		return highestPeak;
	}

	/**
	 * @param spectrum
	 * @return
	 */
	public static double[] getXDataElements(CMLSpectrum spectrum) {
		double[] xvals = null;
		if (spectrum.getSpectrumDataElements() != null
				&& spectrum.getSpectrumDataElements().get(0) != null) {
			xvals = spectrum.getSpectrumDataElements().get(0)
					.getXaxisElements().get(0).getArrayElements().get(0)
					.getDoubles();
		}
		return xvals;
	}

	/**
	 * @param spectrum
	 * @return
	 */
	public static double[] getYDataElements(CMLSpectrum spectrum) {
		double[] yvals = null;
		if (spectrum.getSpectrumDataElements() != null
				&& spectrum.getSpectrumDataElements().get(0) != null) {
			yvals = spectrum.getSpectrumDataElements().get(0)
					.getYaxisElements().get(0).getArrayElements().get(0)
					.getDoubles();
		}
		return yvals;
	}

	/**
	 * @param spectrum
	 * @return
	 */
	public static CMLMetadataList getJCampMetaData(CMLSpectrum spectrum) {
		CMLMetadataList jcampList = null;
		Iterator<CMLMetadataList> it = spectrum.getMetadataListElements()
				.iterator();
		while (it.hasNext()) {
			CMLMetadataList mlist = it.next();
			if (mlist.getId() != null && mlist.getId().compareTo("jcamp") == 0) {
				jcampList = mlist;
			}
		}
		return jcampList;
	}

	public static CMLMetadataList getOtherMetaData(CMLSpectrum spectrum) {
		CMLMetadataList containerList = null;
		Iterator<CMLMetadataList> it = spectrum.getMetadataListElements()
				.iterator();
		while (it.hasNext()) {
			CMLMetadataList mlist = it.next();
			if (mlist.getId() != null && mlist.getId().compareTo("jcamp") == 1) {
				containerList.addMetadataList(mlist);
			}
		}
		return containerList;
	}

	/**
	 * @param spectrum
	 * @return
	 */
	public static CMLSpectrumData getSpectrumData(CMLSpectrum spectrum) {
		CMLSpectrumData data = spectrum.getSpectrumDataElements().get(0);
		return data;
	}

	/**
	 * @param spectrum
	 * @return
	 */
	public static boolean spectrumHasPeaks(CMLSpectrum spectrum) {
		if (spectrum.getPeakListElements().size() != 0) {
			if (spectrum.getPeakListElements().get(0).getPeakElements().size() != 0) {
				return true;
			}
		}
		return false;
	}

	public static TreeMap readJCAMPProperties() {
		Properties notesProps = new Properties();
		URL varPluginUrl = Platform.getBundle(
				"net.bioclipse.spectrum").getEntry("/");
		try {
			String varInstallPath = FileLocator.toFileURL(varPluginUrl).getFile();
			File notePropsFile = new File(varInstallPath + "notes.properties");
			FileInputStream inStream = new FileInputStream(notePropsFile);
			notesProps.load(inStream);
		} catch (java.io.IOException e) {
			File notePropsFile2 = new File("spok/gui/notes.properties");
			FileInputStream ins = null;
			try {
				ins = new FileInputStream(notePropsFile2);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			try {
				notesProps.load(ins);
			} catch (java.io.IOException ioe) {
				ioe.printStackTrace();
			}
		}
		Enumeration notesNames = notesProps.propertyNames();
		TreeMap allKeys = new TreeMap();
		TreeMap substanceKeys = new TreeMap();
		TreeMap instrumentKeys = new TreeMap();
		TreeMap generalKeys = new TreeMap();
		TreeMap msKeys = new TreeMap();
		TreeMap nmrKeys = new TreeMap();
		TreeMap irKeys = new TreeMap();
		while (notesNames.hasMoreElements()) {
			String key = (String) notesNames.nextElement();
			if (key.indexOf('.') < 0) {
				String jcampName = (String) notesProps.get(key + ".jcamp");
				if (jcampName != null) {
					jcampName = Utils.normalizeLabel(jcampName);
					if (jcampName.startsWith(".")) {
						jcampName = jcampName.substring(1);
					}
				}
				String displayName = (String) notesProps.get(key);

				if (displayName != null && jcampName != null) {
					allKeys.put(jcampName, displayName);
				}
				if (key.startsWith("substance") && displayName != null) {
					substanceKeys.put(displayName, jcampName);
				} else if (key.startsWith("instrument") && displayName != null) {
					instrumentKeys.put(displayName, jcampName);
				} else if (key.startsWith("ms") && displayName != null) {
					msKeys.put(displayName, jcampName);
				} else if (key.startsWith("nmr")) {
					if (displayName != null) {
						nmrKeys.put(displayName, jcampName);
					}
				} else if (key.startsWith("ir") && displayName != null) {
					irKeys.put(displayName, jcampName);
				} else if (displayName != null) {
					generalKeys.put(displayName, jcampName);
				}
			}
		}
		jcampKeys = new TreeMap();
		jcampKeys.put("allKeys", allKeys);
		jcampKeys.put("JCamp Substance Keys", substanceKeys);
		jcampKeys.put("JCamp Instrument Keys", instrumentKeys);
		jcampKeys.put("msKeys", msKeys);
		jcampKeys.put("nmrKeys", nmrKeys);
		jcampKeys.put("irKeys", irKeys);
		jcampKeys.put("JCamp General Keys", generalKeys);

		return jcampKeys;
	}

	public static HashMap<String, String> getNotesAsMap(CMLSpectrum spectrum) {
		HashMap<String, String> notes = new HashMap<String, String>();
		if (spectrum.getMetadataListElements() != null && spectrum.getMetadataListElements().size() > 0) {
			CMLMetadataList metadataList = spectrum.getMetadataListElements().get(0);
			CMLElements metadataElements = metadataList.getMetadataElements();
			Iterator metadataIterator = metadataElements.iterator();
			while (metadataIterator.hasNext()) {
				CMLMetadata metadata = (CMLMetadata) metadataIterator.next();
				notes.put(metadata.getId(), metadata.getContent());
			}
		}
		if (spectrum.getConditionListElements() != null && spectrum.getConditionListElements().size() > 0) {
			CMLConditionList conditionList = spectrum.getConditionListElements().get(0);
			CMLElements conditionElements = conditionList.getScalarElements();
			Iterator conditionIterator = conditionElements.iterator();
			while (conditionIterator.hasNext()) {
				CMLScalar condition = (CMLScalar) conditionIterator.next();
				notes.put(condition.getId(), condition.getValue());
			}
		}
		if (spectrum.getChildCMLElements("substanceList") != null && spectrum.getChildCMLElements("substanceList").size() > 0){
			CMLSubstanceList substanceList = (CMLSubstanceList) spectrum.getChildCMLElements("substanceList").get(0);
			String xml = substanceList.toXML();
			CMLElements substanceElements = substanceList.getSubstanceElements();
			Iterator substanceIterator = substanceElements.iterator();
			while (substanceIterator.hasNext()) {
				CMLSubstance substance = (CMLSubstance) substanceIterator.next();
				notes.put(substance.getId(), substance.getValue());
			}
		}
		return notes;
	}

	public static String getNormalizedSpectrumType(String type) {
		if (specrumTypeSynonyms.size() == 0) {
			fillSpectrumTypeSynonyms();
		}
		String upType = type.toUpperCase();
		String retValue = (String) specrumTypeSynonyms.get(upType);
		return retValue;
	}

	private static void fillSpectrumTypeSynonyms() {
		specrumTypeSynonyms.put("MASSSPECTRUM", MSSPECTRUMTYPE);
		specrumTypeSynonyms.put("MASS SPECTRUM", MSSPECTRUMTYPE);
		specrumTypeSynonyms.put("MS SPECTRUM", MSSPECTRUMTYPE);
		specrumTypeSynonyms.put("MASS", MSSPECTRUMTYPE);
		specrumTypeSynonyms.put("MS", MSSPECTRUMTYPE);

		specrumTypeSynonyms.put("NMR SPECTRUM", NMRSPECTRUMTYPE);
		specrumTypeSynonyms.put("NMR", NMRSPECTRUMTYPE);

		specrumTypeSynonyms.put("IR SPECTRUM", IRSPECTRUMTYPE);
		specrumTypeSynonyms.put("IR", IRSPECTRUMTYPE);
		specrumTypeSynonyms.put("INFRAREDR SPECTRUM", IRSPECTRUMTYPE);
		specrumTypeSynonyms.put("INFRARED", IRSPECTRUMTYPE);
	}
	
	/**
	 * Transforms a string to its jcamp generic variant, if the string is not in the jamp generics list, the string itself is returned back
	 * @param string
	 * @return
	 */
	public static String toJcampGeneric(String string) {
		if (jcampGenerics.size() == 0) {
			fillJcampGenerics();
		}
		String retVal = (String) jcampGenerics.get(string);
		if (retVal != null) {
			return retVal;
		}
		else {
			return string;
		}
	}

	private static void fillJcampGenerics() {
		jcampGenerics.put(NMRSPECTRUMTYPE, "NMR SPECTRUM");
		jcampGenerics.put(MSSPECTRUMTYPE, "MASS SPECTRUM");
	}

	public static double[][] peakTableToPeakSpectrum(Peak1D[] peaks)
			throws JCAMPException {
		int n = peaks.length;
		if (n == 0)
			throw new JCAMPException("empty peak table");
		Arrays.sort(peaks);
		ArrayList px = new ArrayList(n);
		ArrayList py = new ArrayList(n);
		double x0 = peaks[0].getPosition()[0];
		double y0 = peaks[0].getHeight();
		for (int i = 1; i < n; i++) {
			double x = peaks[i].getPosition()[0];
			double y = peaks[i].getHeight();
			if (x - x0 > Double.MIN_VALUE) {
				px.add(new Double(x0));
				py.add(new Double(y0));
				x0 = x;
				y0 = y;
			} else {
				y0 += y;
			}
		}
		px.add(new Double(x0));
		py.add(new Double(y0));
		double[][] xy = new double[2][px.size()];
		for (int i = 0; i < px.size(); i++) {
			xy[0][i] = ((Double) px.get(i)).doubleValue();
			xy[1][i] = ((Double) py.get(i)).doubleValue();
		}
		return xy;
	}

	public static TreeMap getJcampKeys() {
		if (jcampKeys == null) {
			readJCAMPProperties();
		}
		return jcampKeys;
	}
	
	public static Element readJCampDict() throws ValidityException, ParsingException, IOException {
		Builder builder = new Builder();
		URL varPluginUrl = Platform.getBundle(
		"net.bioclipse.cml").getEntry("/dict10/simple/");
		String varInstallPath = null;
		try {
			varInstallPath = Platform.asLocalURL(varPluginUrl).getFile();
		} catch (IOException e) {
			StringWriter strWr = new StringWriter();
			PrintWriter prWr = new PrintWriter(strWr);
		}
			
		Document jcampDict = builder.build(varInstallPath + "jcampDXDict.xml");
		return jcampDict.getRootElement();
	}
	

  /**
   * sets the namespace of all elements and subelements of this Elements to
   * cml namespace, if no namespace is given
   * 
   * @param elements
   */
  public static void namespaceThemAll(Elements elements) {
    for (int i = 0; i < elements.size(); i++) {
      Element elem = elements.get(i);
      //try to set all non set namespaces to cml - is dirty, should be changed
      //in a way, so that it is not tried to namespace non cmlElements to cml...
      if(elem.getNamespaceURI().equals( "" ))
          elem.setNamespaceURI( CMLUtil.CML_NS );
      if (elem.getNamespaceDeclarationCount() == 0) {
        elem.setNamespaceURI(CMLUtil.CML_NS);
      }
      if (elem.getChildCount() != 0) {
        namespaceThemAll(elem.getChildElements());
      }
    }
  }
  

  public final static List<String> SUPPORTED_CONTENT_TYPES =
      new ArrayList<String>() {{
         add("net.bioclipse.contenttypes.jcampdx");
         add("net.bioclipse.contenttypes.cml.singleSpectrum1D");
         add("net.bioclipse.contenttypes.cml.singleSpectrum2D");
         add("net.bioclipse.contenttypes.cml.multipleSpectrum");
      }};
  
  /**
   * Tells if a file is a spectrum according to Bioclipse content types.
   * 
   * @param file The file to check
   * @return true=is any of the spectrum content types, false= is not.
   * @exception CoreException if this method fails. Reasons include:
   * <ul>
   * <li> This resource does not exist.</li>
   * <li> This resource is not local.</li>
   * <li> The workspace is not in sync with the corresponding location
   *       in the local file system.</li>
   * </ul>
   * @throws IOException if an error occurs while reading the contents 
   */
  public static boolean isSpectrum(IFile file) throws CoreException, IOException{
      if(!file.exists())
          return false;
      IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
      InputStream stream = file.getContents();
      IContentType contentType = contentTypeManager.findContentTypeFor(stream, file.getName());
      stream.close();
      if (contentType != null &&
          SUPPORTED_CONTENT_TYPES.contains(contentType.getId()))
          return true;
      else
          return false;
  }
}
