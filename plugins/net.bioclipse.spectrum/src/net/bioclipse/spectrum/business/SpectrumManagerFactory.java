 /*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stefan Kuhn
 *     
 ******************************************************************************/

package net.bioclipse.spectrum.business;

import net.bioclipse.spectrum.Activator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;

/**
 * 
 * @author shk3
 */
public class SpectrumManagerFactory implements IExecutableExtension, 
                                              IExecutableExtensionFactory {

    private Object manager;
    
    public void setInitializationData(IConfigurationElement config,
            String propertyName, Object data) throws CoreException {
        
        manager = Activator.getDefault().getJavaScriptSpectrumManager();
        if(manager==null) {
            manager = new Object();
        }
    }

    public Object create() throws CoreException {
        return manager;
    }
}
