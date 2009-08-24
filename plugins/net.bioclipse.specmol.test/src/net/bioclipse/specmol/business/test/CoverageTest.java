/*******************************************************************************
 * Copyright (c) 2009  Stefan Kuhn <stefan.kuhn@ebi.ac.uk>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: Bioclipse Project <http://www.bioclipse.net>
 ******************************************************************************/
package net.bioclipse.specmol.business.test;

import net.bioclipse.core.tests.coverage.AbstractCoverageTest;
import net.bioclipse.managers.business.IBioclipseManager;
import net.bioclipse.specmol.business.ISpecmolManager;
import net.bioclipse.specmol.business.SpecmolManager;

/**
 * JUnit tests for checking if the tested Manager is properly tested.
 * 
 * @author egonw
 */
public class CoverageTest extends AbstractCoverageTest {
    
    private static SpecmolManager manager = new SpecmolManager();

    @Override
    public IBioclipseManager getManager() {
        return manager;
    }
    
    @Override
    public Class<? extends IBioclipseManager> getManagerInterface() {
      return ISpecmolManager.class;
    }
}
