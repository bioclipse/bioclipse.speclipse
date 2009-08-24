/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package net.bioclipse.spectrum.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import nu.xom.Elements;

import org.apache.log4j.Logger;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.base.CMLElements;
import org.xmlcml.cml.element.AbstractMetadata;
import org.xmlcml.cml.element.CMLConditionList;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMetadataList;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLSpectrum;
import org.xmlcml.cml.element.CMLSubstanceList;

import spok.parser.JcampToCMLSpectrumMapper;

public class MetadataUtils {

	private static final Logger logger = Logger.getLogger(MetadataUtils.class);
	
	private static TreeMap jcampKeyMap;

	static List<CMLElement> mlist = new ArrayList<CMLElement>();

	static HashMap<String,CMLMetadata> metaDataMap = new HashMap<String,CMLMetadata>();

	static CMLMetadataList mlistContainer = new CMLMetadataList();


	private static CMLMetadataList createMetadataList(String key, CMLSpectrum spectrum) {
		CMLMetadataList theList = null;
		TreeMap notes = (TreeMap) jcampKeyMap.get(key);
		if (key.compareTo("msKeys") == 0 || key.compareTo("nmrKeys") == 0
				|| key.compareTo("irKeys") == 0) {
			key = "JCamp Spectrum Keys";
		}
		CMLMetadata newNote = null;
		List<CMLMetadataList> mlists = getMetadataListDescendantsByName(
				spectrum, key);
		if (mlists.size() > 1) {
			logger.debug("multiple lists - should not happen");
		} else if (mlists.size() == 0) {
			theList = new CMLMetadataList();
			theList = getAllInOneMetadataList(spectrum);
		} else {
			theList = mlists.get(0);
			Set keys = notes.keySet();
			Iterator it = keys.iterator();
			while (it.hasNext()) {
				String noteKey = (String) it.next();
				String k = (String) notes.get(noteKey);
				String name;
				if (k != null) {
					name = "jcampdx:" + JcampToCMLSpectrumMapper.sanitize(k);
				} else {
					name = "jcampdx:"
							+ JcampToCMLSpectrumMapper.sanitize(noteKey);
				}
				List<CMLMetadata> noteDesc = getMetadataDescendantsByName(theList.getMetadataDescendants(),name);
				if (noteDesc != null && noteDesc.size() != 0) {
					continue;
				} else {
					newNote = new CMLMetadata();
					newNote.setName(name);
					newNote.setTitle(noteKey);
					theList.addMetadata(newNote);
				}
			}
		}
		return theList;
	}
	
	public static List<CMLMetadata> getMetadataDescendantsByName(List<CMLMetadata> noteDescAll, String name){
		List<CMLMetadata> noteDesc = new ArrayList<CMLMetadata>();
		for(int i=0;i<noteDescAll.size();i++){
			if(noteDescAll.get(i).getName()!=null && noteDescAll.get(i).getName().equals(name))
				noteDesc.add(noteDescAll.get(i));
		}
		return noteDesc;
	}

	private static HashMap getMetadataListFromCMLSpectrumByID(String key, CMLSpectrum spectrum) {
		Iterator<CMLMetadataList> it = spectrum
				.getMetadataListElements().iterator();
		CMLMetadataList returnList = null;
		while (it.hasNext()) {
			CMLMetadataList mlist = (CMLMetadataList) it.next().copy();
			if (mlist.getName() != null && mlist.getName().compareTo(key) == 0) {
				returnList = mlist;
				break;
			}
		}
		if (returnList != null) {
			CMLElements<CMLMetadata> notes = returnList.getMetadataElements();
			Iterator<CMLMetadata> notesIt = notes.iterator();
			while (notesIt.hasNext()) {
				CMLMetadata note = notesIt.next();
				metaDataMap.put(note.getName(), note);
			}
		}
		return metaDataMap;
	}


	public static CMLMetadataList getAllInOneMetadataList(
			CMLSpectrum cmlSpectrum) {
		mlistContainer.removeChildren();
		Elements metadatalists = cmlSpectrum.getChildCMLElements("metadataList");
		for(int i=0;i<metadatalists.size();i++){
			CMLMetadataList list = (CMLMetadataList) metadatalists.get(i);
			CMLElements<CMLMetadata> mElements = list.getMetadataElements();
			Iterator<CMLMetadata> j = mElements.iterator();
			while (j.hasNext()) {
				mlistContainer.addMetadata((AbstractMetadata) j.next().copy());
			}
		}
		return mlistContainer;
	}


	public static CMLMetadataList getAllInOneMetadataList(
			CMLMolecule cmlMolecule) {
		mlistContainer.removeChildren();
		Elements metadatalists = cmlMolecule.getChildCMLElements("metadataList");
		for(int i=0;i<metadatalists.size();i++){
			CMLMetadataList list = (CMLMetadataList) metadatalists.get(i);
			CMLElements<CMLMetadata> mElements = list.getMetadataElements();
			Iterator<CMLMetadata> j = mElements.iterator();
			while (j.hasNext()) {
				mlistContainer.addMetadata((AbstractMetadata) j.next().copy());
			}
		}
		return mlistContainer;
	}

	public static List<CMLMetadataList> getMetadataListDescendantsByName(
			CMLSpectrum spectrum, String name) {
		List<CMLMetadataList> newMetadataList = new ArrayList<CMLMetadataList>();
		// logger.debug("name: " + name);
		if (name != null && spectrum != null) {
			CMLElements<CMLMetadataList> metadataLists = spectrum
					.getMetadataListElements();
			for (CMLMetadataList metadataList : metadataLists) {
				if (name.equals(metadataList.getName())) {
					newMetadataList.add(metadataList);
				}
			}
		}
		return newMetadataList;
	}

	public static List<CMLElement> createDisplayList(CMLSpectrum spectrum) {
		List<CMLElement> all = new Vector<CMLElement>();
		if (spectrum != null && spectrum.getMetadataListElements() != null && spectrum.getMetadataListElements().size() > 0) {
			CMLMetadataList metadataList = spectrum.getMetadataListElements().get(0);
			all.add(metadataList);
		}
		if (spectrum != null && spectrum.getConditionListElements() != null && spectrum.getConditionListElements().size() > 0) {
			CMLConditionList conditionList = spectrum.getConditionListElements().get(0);
			all.add(conditionList);
		}
		if (spectrum != null && spectrum.getDescendants("substanceList", null, true) != null && spectrum.getDescendants("substanceList", null, true).size()> 0) {
			CMLSubstanceList substanceList = (CMLSubstanceList) spectrum.getDescendants("substanceList", null, true).get(0);
			all.add(substanceList);
		}	
		return all;
	}
}
