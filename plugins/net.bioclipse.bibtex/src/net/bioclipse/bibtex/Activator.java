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
package net.bioclipse.bibtex;

import net.bioclipse.bibtex.business.IBibtexManager;
import net.bioclipse.bibtex.business.IJavaBibtexManager;
import net.bioclipse.bibtex.business.IJavaScriptBibtexManager;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "net.bioclipse.bibtex";

	// The shared instance
	private static Activator plugin;
	
  //For Spring
  private ServiceTracker finderTracker;
  private ServiceTracker jsFinderTracker;

	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		    super.start(context);
		    plugin = this;
        finderTracker = new ServiceTracker( context, 
                IJavaBibtexManager.class.getName(), 
                null );
        
        finderTracker.open();
        
        jsFinderTracker = new ServiceTracker( context,                      
                                              IJavaScriptBibtexManager.class.getName(), 
                                              null );                       
        jsFinderTracker.open();
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
    public IBibtexManager getJavaBibtexManager() {
        IBibtexManager manager = null;
        try {
            manager = (IBibtexManager) finderTracker.waitForService(1000*10);
        } catch (InterruptedException e) {
            throw                                                                
            new IllegalStateException("Could not get the Java bibtex manager", e);  
        }
        if(manager == null) {
            throw new IllegalStateException("Could not get the Java bibtex manager");
        }
        return manager;
    }
    
    
    public IBibtexManager getJSBibtexManager() {                                     
      IBibtexManager jsBibtexManager;                                              
                                                                             
      try {                                                                  
        jsBibtexManager                                                          
            = (IBibtexManager) jsFinderTracker.waitForService(1000*30);        
      }                                                                      
      catch (InterruptedException e) {                                       
        throw                                                                
          new IllegalStateException("Could not get js bibtex manager", e);  
      }                                                                      
      if (jsBibtexManager == null) {                                             
        throw new IllegalStateException("Could not get js bibtex manager"); 
      }                                                                      
      return jsBibtexManager;                                                    
    }

}
