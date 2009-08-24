/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package net.bioclipse.specmol;

import net.bioclipse.core.util.LogUtils;
import net.bioclipse.specmol.business.IJavaScriptSpecmolManager;
import net.bioclipse.specmol.business.IJavaSpecmolManager;
import net.bioclipse.specmol.business.ISpecmolManager;

import org.apache.log4j.Logger;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	private static final Logger logger = Logger.getLogger(Activator.class);
	
	// The plug-in ID
	public static final String PLUGIN_ID = "net.bioclipse.specmol";

	// The shared instance
	private static Activator plugin;
	
  // For Spring: Java flavored manager
  private ServiceTracker finderTracker;
  // For Spring: JavaScript flavored manager
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
                IJavaSpecmolManager.class.getName(), 
                null );
        
        finderTracker.open();
        jsFinderTracker = new ServiceTracker( context, 
                                            IJavaScriptSpecmolManager.class.getName(), 
                                            null );
                                    
        jsFinderTracker.open();	}

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
		
    public ISpecmolManager getJavaSpecmolManager() {
        ISpecmolManager manager = null;
        try {
            manager = (ISpecmolManager) finderTracker.waitForService(1000*10);
        } catch (InterruptedException e) {
            LogUtils.debugTrace(logger, e);
        }
        if(manager == null) {
            throw new IllegalStateException("Could not get the SpecMol manager");
        }
        return manager;
    }
    
    public ISpecmolManager getJavascriptSpecmolManager() {
        ISpecmolManager manager = null;
        try {
            manager = (ISpecmolManager) jsFinderTracker.waitForService(1000*10);
        } catch (InterruptedException e) {
            LogUtils.debugTrace(logger, e);
        }
        if(manager == null) {
            throw new IllegalStateException("Could not get the SpecMol manager");
        }
        return manager;
    }

}
