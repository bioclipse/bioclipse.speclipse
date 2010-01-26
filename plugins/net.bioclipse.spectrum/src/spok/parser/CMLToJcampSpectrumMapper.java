/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package spok.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import net.bioclipse.spectrum.Activator;
import net.bioclipse.spectrum.editor.MetadataUtils;
import net.bioclipse.cml.contenttypes.CmlFileDescriber;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import nu.xom.XPathContext;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.jcamp.parser.JCAMPException;
import org.jcamp.spectrum.ArrayData;
import org.jcamp.spectrum.IDataArray1D;
import org.jcamp.spectrum.IOrderedDataArray1D;
import org.jcamp.spectrum.IRSpectrum;
import org.jcamp.spectrum.MassSpectrum;
import org.jcamp.spectrum.NMRSpectrum;
import org.jcamp.spectrum.OrderedArrayData;
import org.jcamp.spectrum.Peak1D;
import org.jcamp.spectrum.Spectrum;
import org.jcamp.spectrum.Spectrum1D;
import org.jcamp.units.CommonUnit;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.base.CMLElements;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMetadataList;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLSpectrum;

import spok.utils.SpectrumUtils;

public class CMLToJcampSpectrumMapper {
	private static final Logger logger = Logger.getLogger(CMLToJcampSpectrumMapper.class);
	
	private static Spectrum1D jcampSpectrum;

	private static boolean fullspec;

	public static Spectrum mapCMLSpectrumToJcamp(CMLSpectrum spectrum) {
		//shk3: I added the jcampSpectrum=null since if no peaks are in the spectrum and
		//the method was already used before, the old spectrum will be used and the 
		//type will be that of the old spectrum no matter what is it now (in mapPeaks,
		//a new spectrum is created always, but this is not always called)
		jcampSpectrum=null;
		fullspec = false;
		if (spectrum.getSpectrumDataElements() != null
				&& spectrum.getSpectrumDataElements().size() > 0) {
			fullspec = true;
		}
		mapPeaks(spectrum);
		mapData(spectrum);
		try{
			mapNotes(spectrum);
		}catch(Exception ex){
			StringWriter strWr = new StringWriter();
			PrintWriter prWr = new PrintWriter(strWr);
			ex.printStackTrace(prWr);
			logger.error(strWr.toString());
			MessageBox mb = new MessageBox(new Shell(), SWT.OK | SWT.ICON_WARNING);
	        mb.setText("Error mapping nodes");
	        mb.setMessage("Mapping the CML metadata to JCAMP nodes failed. The JCMAMP file will not contain all information from the model!");
	        mb.open();
		}
		String title = spectrum.getTitle();
		jcampSpectrum.setTitle(title);
		return (Spectrum) jcampSpectrum;
	}

	private static void mapPeaks(CMLSpectrum spectrum) {
		if (spectrum.getPeakListElements().size() != 0) {
			List<CMLElement> peaks = SpectrumUtils.getPeakElements(spectrum);
			Iterator<CMLElement> it = peaks.iterator();
			Peak1D[] jcampPeaks = new Peak1D[peaks.size()];
			int i = 0;
			while (it.hasNext()) {
				CMLPeak peak = (CMLPeak) it.next();
				if (peak.getXValue() != 0 || peak.getYValue() != 0) {
					Peak1D jcampPeak = new Peak1D(peak.getXValue(), peak
							.getYValue());
					jcampPeak.setHeight(peak.getYValue());
					jcampPeaks[i] = jcampPeak;
					i += 1;
				}
			}
			if (spectrum.getType().compareTo("massSpectrum") == 0) {
				double[][] xy = null;
				try {
					xy = SpectrumUtils.peakTableToPeakSpectrum(jcampPeaks);
				} catch (JCAMPException e) {
					StringWriter strWr = new StringWriter();
					PrintWriter prWr = new PrintWriter(strWr);
					e.printStackTrace(prWr);
					logger.error(strWr.toString());
				}
				IOrderedDataArray1D x = new OrderedArrayData(xy[0],
						CommonUnit.mz);
				IDataArray1D y = new ArrayData(xy[1], CommonUnit.intensity);
				if (!fullspec) {
					jcampSpectrum = new MassSpectrum(x, y, false);
				} else {
					jcampSpectrum = new MassSpectrum(x, y, true);
				}
			} else if (spectrum.getType().compareTo("NMR") == 0) {
				double[][] xy = null;
				ArrayList<Object> list = getRefFreNuc(spectrum);
				double reference = 0;
				if (list.get(0) != null) {
					reference = ((Double) list.get(0)).doubleValue();
				}
				double freq = ((Double) list.get(1)).doubleValue();
				String nucleus = (String) list.get(2);
				try {
					xy = SpectrumUtils.peakTableToPeakSpectrum(jcampPeaks);
				} catch (JCAMPException e) {
					StringWriter strWr = new StringWriter();
					PrintWriter prWr = new PrintWriter(strWr);
					e.printStackTrace(prWr);
					logger.error(strWr.toString());
				}
				IOrderedDataArray1D x = new OrderedArrayData(xy[0],
						CommonUnit.mz);
				IDataArray1D y = new ArrayData(xy[1], CommonUnit.intensity);

				if (!fullspec) {
					jcampSpectrum = new NMRSpectrum(x, y, nucleus, freq,
							reference, false, Activator.getDefault().getModePreference());
				} else {
					jcampSpectrum = new NMRSpectrum(x, y, nucleus, freq,
							reference, true, Activator.getDefault().getModePreference());
				}
			}else if (spectrum.getType().compareTo("IR") == 0) {
				double[][] xy = null;
				try {
					xy = SpectrumUtils.peakTableToPeakSpectrum(jcampPeaks);
				} catch (JCAMPException e) {
					e.printStackTrace();
					StringWriter strWr = new StringWriter();
					PrintWriter prWr = new PrintWriter(strWr);
					e.printStackTrace(prWr);
					logger.error(strWr.toString());
				}
				IOrderedDataArray1D x = new OrderedArrayData(xy[0],
						CommonUnit.perCM);
				IDataArray1D y = new ArrayData(xy[1], CommonUnit.intensity);
				if (!fullspec) {
					jcampSpectrum = new IRSpectrum(x, y, false);
				} else {
					jcampSpectrum = new IRSpectrum(x, y, true);
				}
			}else{
				MessageDialog.openError(new Shell(), "Unknown spectrum type", "The spectrum had no known type. The file net.bioclipse.spectrum/net/bioclipse/spectrum/editor/spec.xml contains valid types!");
				return;
			}

			jcampSpectrum.setPeakTable(jcampPeaks);
		}
	}

	private static void mapData(CMLSpectrum spectrum) {
		// ArrayList xyData = spectrum.getXYData();
		double[] jcampXData = null;
		double[] jcampYData = null;
		if (SpectrumUtils.getXDataElements(spectrum) != null) {
			jcampXData = SpectrumUtils.getXDataElements(spectrum);
		}
		if (SpectrumUtils.getYDataElements(spectrum) != null) {
			jcampYData = SpectrumUtils.getYDataElements(spectrum);
		} else {
			jcampXData = new double[0];
			jcampYData = new double[0];
		}
		OrderedArrayData xData = null;
		ArrayData yData = null;
		if (spectrum.getType().compareTo("massSpectrum") == 0) {
			xData = new OrderedArrayData(jcampXData, CommonUnit.mz);
			yData = new ArrayData(jcampYData, CommonUnit.percentIntensity);
		} else if (spectrum.getType().compareTo("NMR") == 0) {
			xData = new OrderedArrayData(jcampXData, CommonUnit.ppm);
			yData = new ArrayData(jcampYData, CommonUnit.percentIntensity);
		} else if (spectrum.getType().compareTo("IR") == 0) {
			xData = new OrderedArrayData(jcampXData, CommonUnit.perCM);
			yData = new ArrayData(jcampYData, CommonUnit.percentIntensity);
		}
		if (jcampSpectrum != null) {
			jcampSpectrum.setData(xData, yData);
			// jcampSpectrum.setXData(xData);
			// jcampSpectrum.setYData(yData);
		} else {
			if (xData.getUnit() == CommonUnit.ppm) {
				ArrayList<Object> list = getRefFreNuc(spectrum);
				double reference = ((Double) list.get(0)).doubleValue();
				double freq = ((Double) list.get(1)).doubleValue();
				String nucleus = (String) list.get(2);
				jcampSpectrum = new NMRSpectrum(xData, yData, nucleus, freq,
						reference, true, Activator.getDefault().getModePreference());
			} else if (xData.getUnit() == CommonUnit.mz) {
				jcampSpectrum = new MassSpectrum(xData, yData, true);
			} else if (xData.getUnit() == CommonUnit.perCM) {
				jcampSpectrum = new IRSpectrum(xData, yData, true);
				
			}
		}
	}

	private static ArrayList<Object> getRefFreNuc(CMLSpectrum spectrum) {
		ArrayList<Object> list = new ArrayList<Object>();
		CMLElements<CMLMetadataList> mlists = spectrum
				.getMetadataListElements();
		Iterator<CMLMetadataList> it = mlists.iterator();
		Double refVal = new Double(0);
		Double freqVal = new Double(1);
		String nucVal = null;
		while (it.hasNext()) {
			CMLMetadataList mlist = it.next();
			List<CMLMetadata> ref = MetadataUtils.getMetadataDescendantsByName(mlist.getMetadataDescendants(),"jcampdx:SHIFTREFERENCE");
			String val;
			if (ref != null && ref.size() > 0) {
				val = ref.get(0).getValue().trim();
				if (val.length() > 0) {
					refVal = new Double(ref.get(0).getValue());
				}
			}
			List<CMLMetadata> freq = MetadataUtils.getMetadataDescendantsByName(mlist.getMetadataDescendants(),"jcampdx:OBSERVEFREQUENCY");
			if (freq != null && freq.size() > 0) {
				val = freq.get(0).getValue().trim();
				if (val.length() > 0) {
					freqVal = new Double(freq.get(0).getValue());
				}
			}
			List<CMLMetadata> nuc = MetadataUtils.getMetadataDescendantsByName(mlist.getMetadataDescendants(),"jcampdx:OBSERVENUCLEUS");
			if (nuc != null && nuc.size() > 0) {
				nucVal = nuc.get(0).getValue();
			}
		}
		list.add(refVal);
		list.add(freqVal);
		list.add(nucVal);
		return list;
	}

	private static void mapNotes(CMLSpectrum spectrum) throws ValidityException, ParsingException, IOException {
		HashMap<String, String> notes = SpectrumUtils.getNotesAsMap(spectrum);
		Iterator<String> notesIterator = notes.keySet().iterator();
		boolean title=false;
		while (notesIterator.hasNext()) {
			String id = (String) notesIterator.next();
			String content = (String) notes.get(id);
			
			Properties notesProps = new Properties();
			URL varPluginUrl = Platform.getBundle(
					"net.bioclipse.spectrum").getEntry("/");
			String varInstallPath = null;
			try {
				varInstallPath = FileLocator.toFileURL(varPluginUrl).getFile();
			} catch (IOException e) {
				StringWriter strWr = new StringWriter();
				logger.error(strWr.toString());
			}
			File notePropsFile = new File(varInstallPath + "notes.properties");
			FileInputStream inStream = new FileInputStream(notePropsFile);
			notesProps.load(inStream);
			Element jcampDict = SpectrumUtils.readJCampDict();
			XPathContext xpathContext = new XPathContext("jcampDictNS", CmlFileDescriber.NS_CML);
			Nodes resultNodes = jcampDict.query("//jcampDictNS:entry[@id='" + id +"']", xpathContext);
			String name = null;
			String jcampName = null;
			if (resultNodes.size() == 1) {
				Element result = (Element) resultNodes.get(0);
				if (result != null) {
					name = result.getAttributeValue("term");
				}
			}
			
			if (name != null) {
				jcampName = (String) notesProps.get(name + ".jcamp");
			}
			jcampName = (String) notesProps.get(id + ".jcamp");
			if (jcampName != null) {
				if(jcampName.equals("TITLE")){
					if(!title){
						jcampSpectrum.setNote(jcampName, content);
						title=true;
					}
				}
			} else {
				if (id!=null && id.startsWith("dot")) {
					id = "." + id.substring(3);
				}
				if(id!=null && content!=null && !id.equals("TITLE"))
					jcampSpectrum.setNote(id, content);
			}
		}
	}
}
