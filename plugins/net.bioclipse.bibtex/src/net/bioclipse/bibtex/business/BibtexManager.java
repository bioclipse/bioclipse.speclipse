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
package net.bioclipse.bibtex.business;

import java.io.IOException;
import java.io.InputStreamReader;

import net.bioclipse.bibtex.domain.IJabrefBibliodata;
import net.bioclipse.bibtex.domain.JabrefBibliodata;
import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.managers.business.IBioclipseManager;
import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.Globals;
import net.sf.jabref.JabRefPreferences;
import net.sf.jabref.imports.BibtexParser;
import net.sf.jabref.imports.ParserResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * The manager class for Bibtex. Contains Bibtex related methods.
 *
 */
public class BibtexManager implements IBioclipseManager {
	
    public String getManagerName() {
        return "bibtex";
    }
    
	public IJabrefBibliodata loadBibliodata(IFile file) throws IOException,
	    BioclipseException, CoreException {


		BibtexParser parser=new BibtexParser(new InputStreamReader(file.getContents()));
		
		if (Globals.prefs == null){
		   	Globals.prefs = JabRefPreferences.getInstance();
		}
		ParserResult result=parser.parse();
		BibtexDatabase db=result.getDatabase();
		
		return new JabrefBibliodata(db);
	}
    

	public IJabrefBibliodata loadBibliodata(String path) throws IOException,
			BioclipseException, CoreException {
		
		
		BibtexParser parser=new BibtexParser(new InputStreamReader(ResourcePathTransformer.getInstance().transform( path ).getContents()));
		
		if (Globals.prefs == null){
		   	Globals.prefs = JabRefPreferences.getInstance();
		}
		ParserResult result=parser.parse();
		BibtexDatabase db=result.getDatabase();

		return new JabrefBibliodata(db);
	}
}
