/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth
 *     Jonathan Alvarsson
 *     Stefan Kuhn
 *     
 ******************************************************************************/
package net.bioclipse.specmol.business;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.ISpecmol;
import net.bioclipse.managers.business.IBioclipseManager;
import net.bioclipse.specmol.domain.IJumboSpecmol;
import net.bioclipse.specmol.domain.JumboSpecmol;
import net.bioclipse.specmol.editor.SpecMolEditor;
import nu.xom.Element;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.base.CMLSerializer;
import org.xmlcml.cml.base.CMLUtil;
import org.xmlcml.cml.element.CMLCml;

import spok.utils.SpectrumUtils;

/**
 * The manager class for CDK. Contains CDK related methods.
 * 
 * @author olas
 *
 */
public class SpecmolManager implements IBioclipseManager {
	
    public String getManagerName() {
        return "specmol";
    }

	public IJumboSpecmol create(ISpecmol s) throws BioclipseException, UnsupportedEncodingException {
		return new JumboSpecmol(parseCml(new ByteArrayInputStream(s.getCML().getBytes("US-ASCII"))));
	}

	public IJumboSpecmol loadSpecmol(IFile file, IProgressMonitor monitor)
			throws IOException, BioclipseException, CoreException {
        if ( monitor == null ) {
            monitor = new NullProgressMonitor();
        }
        int ticks = 10000;
        monitor.beginTask( "Reading file", ticks );
        IJumboSpecmol result;
        try{
            InputStream instream = file.getContents();
            CMLCml cmlcml=parseCml(instream);
            JumboSpecmol jsm=new JumboSpecmol(cmlcml);
            result = jsm;
        }finally {
            monitor.done();
        }
        return result;
	}
		
	private CMLCml parseCml(InputStream instream) throws BioclipseException{

		StringBuffer buffer = new StringBuffer();
		int character;
		CMLElement cmlElement = null;
		try {
			while ((character = instream.read()) != -1) {
				buffer.append((char) character);
			}
			instream.close();
			CMLBuilder builder = new CMLBuilder(false);
			Element element = null;
			try {
				cmlElement =  (CMLElement) builder.parseString(buffer.toString());
			} 
			//TODO dirty! but for some reason if there is no cml namespace, parsing fails with a ClassCastException
			// so i parse string into an nu.xom.element, set the namespace to cml namespace for
			// all subelements an reParse the toXML() of the nu.xom.element into cml - not elegant, but 
			// works for the moment
			catch (ClassCastException ex) {
					element =  (Element) builder.parseString(buffer.toString());
					SpectrumUtils.namespaceThemAll(element.getChildElements());
					element.setNamespaceURI(CMLUtil.CML_NS);
					cmlElement = (CMLElement) builder.parseString(element.toXML());
			}
		} catch (Exception e) {
			throw new BioclipseException(e.getMessage());
		}
		return (CMLCml)cmlElement;
	}

	public IJumboSpecmol fromCml(String cml) throws BioclipseException,
			IOException {
		return new JumboSpecmol(parseCml(new ByteArrayInputStream(cml.getBytes("US-ASCII"))));
	}

	public void saveSpecmol(IJumboSpecmol specmol, IFile target)
			throws BioclipseException, CoreException, UnsupportedEncodingException {
        IProgressMonitor monitor = new NullProgressMonitor();
    	try{
	        int ticks = 10000;
	        monitor.beginTask( "Writing file", ticks );
	        CMLSerializer ser = new CMLSerializer();
			ser.setIndent(2);
			String towrite = ser.getXML(specmol.getJumboObject());
	    	if(target.exists()){
	        	 target.setContents(new ByteArrayInputStream(towrite.getBytes("US-ASCII")), false, true, monitor);
	    	} else {
		    	target.create(new ByteArrayInputStream(towrite.getBytes("US-ASCII")), false, monitor);
	    	}
	        monitor.worked(ticks);
		}
		finally {
			monitor.done();
		}
	}
}
