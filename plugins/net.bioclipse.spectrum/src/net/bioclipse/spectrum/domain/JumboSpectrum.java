/*******************************************************************************
 * Copyright (c) 2008-2009  Stefan Kuhn <stefan.kuhn@ebi.ac.uk>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.spectrum.domain;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.BioObject;

import org.xmlcml.cml.element.CMLSpectrum;

public class JumboSpectrum extends BioObject implements IJumboSpectrum {

	private CMLSpectrum spectrum;
	
  public JumboSpectrum(CMLSpectrum spectrum) {
      super();
      this.spectrum=spectrum;
  }
  
  /*
   * Needed by Spring
   */
  public JumboSpectrum() {
      super();
  }
	
	public String getCML() throws BioclipseException {
		return spectrum.toXML();
	}
	
	public CMLSpectrum getJumboObject(){
		return spectrum;
	}

}