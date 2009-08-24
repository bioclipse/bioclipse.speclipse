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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Iterator;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.BioObject;
import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.export.FileActions;
import nu.xom.Builder;
import nu.xom.Document;

public class JabrefBibliodata extends BioObject implements IJabrefBibliodata {

	private BibtexDatabase db;
	
    public JabrefBibliodata(BibtexDatabase db) {
        super();
        this.db=db;
    }
    
    public JabrefBibliodata() {
        super();
    }
	
	public BibtexDatabase getJabrefDatabase() {
		return db;
	}

	public Document getJabrefDatabaseAsXml() throws BioclipseException {
		HashSet<String> hs=new HashSet<String>();
		Iterator it=db.getEntries().iterator();
		int i=0;
		while(it.hasNext()){
			BibtexEntry entry=(BibtexEntry)it.next();
			hs.add(entry.getId());
		}
		StringWriter sw=new StringWriter();
		try {
			FileActions.exportDatabase(db, hs, "/resource/layout/", "bibtexml", sw);
	        Builder parser = new Builder();
	        Document doc = parser.build(new StringReader(sw.toString()));
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
			throw new BioclipseException(e.getMessage());
		}
	}

	public String getBibtexML() throws BioclipseException {
		return getJabrefDatabaseAsXml().toXML();
	}
}
