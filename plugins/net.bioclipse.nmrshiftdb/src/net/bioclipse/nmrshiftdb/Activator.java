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
package net.bioclipse.nmrshiftdb;

import net.bioclipse.core.util.LogUtils;
import net.bioclipse.nmrshiftdb.business.IJavaNmrshiftdbManager;
import net.bioclipse.nmrshiftdb.business.IJavaScriptNmrshiftdbManager;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The main plugin class to be used in the desktop.
 */
public class Activator extends AbstractUIPlugin {
    
  private static final Logger logger = Logger.getLogger(Activator.class);

	//The shared instance.
	private static Activator plugin;
	public final static String ID="net.bioclipse.nmrshiftdb";
	
	private ServiceTracker finderTracker;
	private ServiceTracker jsFinderTracker;
	
	/**
	 * The constructor.
	 */
	public Activator() {}
	
	public void start(BundleContext context) throws Exception {
	  super.start(context);
		plugin = this;
    finderTracker = new ServiceTracker( context, 
                                        IJavaNmrshiftdbManager.class.getName(), 
                                        null );
    
    finderTracker.open();
    jsFinderTracker = new ServiceTracker( context, 
                                        IJavaScriptNmrshiftdbManager.class.getName(), 
                                        null );
    jsFinderTracker.open();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
  public IJavaNmrshiftdbManager getJavaNmrshiftdbManager() {
      IJavaNmrshiftdbManager manager = null;
          try {
              manager = (IJavaNmrshiftdbManager)
                finderTracker.waitForService(1000);
          } catch (InterruptedException e) {
              LogUtils.debugTrace(logger, e);
          }
          if (manager == null) {
              throw new IllegalStateException(
                  "Could not get the nmrshiftdb manager");
          }
          return manager;
      }
   
   public IJavaScriptNmrshiftdbManager getJavaScriptNmrshiftdbManager() {
       IJavaScriptNmrshiftdbManager manager = null;
       try {
           manager = (IJavaScriptNmrshiftdbManager) jsFinderTracker.waitForService(1000*10);
       } catch (InterruptedException e) {
           LogUtils.debugTrace(logger, e);
       }
       if(manager == null) {
           throw new IllegalStateException("Could not get the nmrshiftdb manager");
       }
       return manager;
   }

}
