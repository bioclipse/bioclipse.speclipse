/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.specmol.domain;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.BioObject;

import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLSpectrum;

public class JumboSpecmol extends BioObject implements IJumboSpecmol {

	private CMLCml specmol;
	
    public JumboSpecmol(CMLCml spectrum) {
        super();
        this.specmol=spectrum;
    }
    
    public JumboSpecmol() {
        super();
    }
	
	public String getCML() throws BioclipseException {
		return specmol.toXML();
	}
	
	public CMLCml getJumboObject(){
		return specmol;
	}

}
