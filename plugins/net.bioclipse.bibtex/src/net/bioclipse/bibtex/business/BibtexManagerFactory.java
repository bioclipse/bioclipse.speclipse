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


import net.bioclipse.bibtex.Activator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;

public class BibtexManagerFactory implements IExecutableExtension, 
                                              IExecutableExtensionFactory {

    private Object bibtexManager;
    
    public void setInitializationData(IConfigurationElement config,
            String propertyName, Object data) throws CoreException {
        
        bibtexManager = Activator.getDefault().getJSBibtexManager();
        if(bibtexManager==null) {
            bibtexManager = new Object();
        }
    }

    public Object create() throws CoreException {
        return bibtexManager;
    }
}
