/*******************************************************************************
 * Copyright (c) 2008  Stefan Kuhn <stefan.kuhn@ebi.ac.uk>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: Bioclipse Project <http://www.bioclipse.net>
 ******************************************************************************/
package net.bioclipse.nmrshiftdb.business.test;

import net.bioclipse.core.tests.AbstractManagerTest;
import net.bioclipse.managers.business.IBioclipseManager;
import net.bioclipse.nmrshiftdb.business.INmrshiftdbManager;
import net.bioclipse.nmrshiftdb.business.NmrshiftdbManager;

public class APITest extends AbstractManagerTest {
 
   NmrshiftdbManager nmrshiftdbManager;
 
   @Override
   public IBioclipseManager getManager() {
     return nmrshiftdbManager;
   }
 
   @Override
   public Class<? extends IBioclipseManager> getManagerInterface() {
     return INmrshiftdbManager.class;
   }
 
 }
