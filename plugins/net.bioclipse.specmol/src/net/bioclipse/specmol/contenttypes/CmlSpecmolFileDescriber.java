/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.specmol.contenttypes;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import net.bioclipse.specmol.editor.SpecMolEditor;

import org.eclipse.core.internal.content.TextContentDescriber;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.content.IContentDescription;
import org.xmlcml.cml.base.CMLUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

@SuppressWarnings("restriction")
public class CmlSpecmolFileDescriber extends TextContentDescriber {

 	public int describe(InputStream contents, IContentDescription description)
			throws IOException {
		return analyse(new InputStreamReader(contents), description);
	}

	public int describe(Reader contents, IContentDescription description) throws IOException {
		return analyse(contents, description);
	}
	
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		//none right now
	}

	/**
	 * Scan the document, looking for certain key features.
	 * Right now, we look if there is a single molecule in the top hierarchy.
	 * Everything else (multiple spectra, spectra with molecule etc.) is not handled by this contenttype.
	 * 
	 * @param input A Reader that will provide the file contents.
	 * @param description The eclipse IContentDescription of the file.
	 * @return VALID or INVAID
	 * @throws IOException
	 */
	private int analyse(Reader input, IContentDescription description) throws IOException {
		
		int spectrumCount = 0;
		int moleculeCount=0;
		int spectrumTagDepth = 0;
		int moleculeTagDepth = 0;
		boolean checkedNamespace = false;
		boolean hascmlroot=false;

		try {
			XmlPullParserFactory factory = 
				XmlPullParserFactory.newInstance(
						System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
			factory.setNamespaceAware(true);
			factory.setValidating(false);

			XmlPullParser parser = factory.newPullParser();
			parser.setInput(input);
			while (parser.next() != XmlPullParser.END_DOCUMENT) {
				if (parser.getEventType() == XmlPullParser.START_TAG) {
				    String tagName = parser.getName();
				    
				    if (!checkedNamespace && tagName.equalsIgnoreCase(SpecMolEditor.fileextension)) {
				        if (parser.getNamespace().equals(CMLUtil.CML_NS)) {
				            checkedNamespace = true;
				        } else {
				            System.err.println("namespace = " + parser.getNamespace() + " INVALID");
				            return INVALID;
				        }
				        hascmlroot=true;
				    }
				    
					if (tagName.equalsIgnoreCase("spectrum")) {
					    spectrumTagDepth += 1;
					    
					    // only count the top level of molecules, not nested ones.
					    if (spectrumTagDepth == 1) {
					        spectrumCount++;
					    }
					}

					if (tagName.equalsIgnoreCase("molecule")) {
					    moleculeTagDepth += 1;
					    
					    // only count the top level of molecules, not nested ones.
					    if (moleculeTagDepth == 1) {
					        moleculeCount++;
					    }
					}

				} else if (parser.getEventType() == XmlPullParser.END_TAG) {
				    if (spectrumTagDepth > 0 && parser.getName().equalsIgnoreCase("spectrum")) {
				        spectrumTagDepth -= 1;
				    }
				    if (moleculeTagDepth > 0 && parser.getName().equalsIgnoreCase("molecule")) {
				    	moleculeTagDepth -= 1;
				    }
				}
			}
		} catch (XmlPullParserException x) {
			/*
			 * If xmpp fails on the file, it can't be valid CML/XML
			 */
			x.printStackTrace();
			return INVALID;
		}

		if (hascmlroot && spectrumCount >= 1 && moleculeCount==1) {
			return VALID;
		}
		return INVALID;
	}
}
