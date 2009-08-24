/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.specmol.domain;

import net.bioclipse.core.domain.ISpecmol;

import org.xmlcml.cml.element.CMLCml;

public interface IJumboSpecmol extends ISpecmol{
	
	public CMLCml getJumboObject();

}
