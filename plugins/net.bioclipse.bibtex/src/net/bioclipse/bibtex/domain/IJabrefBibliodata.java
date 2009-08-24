/*******************************************************************************
 * Copyright (c) 2008-2009  Stefan Kuhn <stefan.kuhn@ebi.ac.uk
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.bibtex.domain;

import net.sf.jabref.BibtexDatabase;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IBibliodata;
import nu.xom.Document;


public interface IJabrefBibliodata extends IBibliodata{
	
	public BibtexDatabase getJabrefDatabase();
	
	public String getBibtexML() throws BioclipseException;
	
	public Document getJabrefDatabaseAsXml() throws BioclipseException;

}
