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
package net.bioclipse.bibtex.test;

import net.bioclipse.bibtex.business.BibtexManager;
import net.bioclipse.bibtex.business.IBibtexManager;
import net.bioclipse.core.tests.coverage.AbstractCoverageTest;
import net.bioclipse.managers.business.IBioclipseManager;

/**
 * JUnit tests for checking if the tested Manager is properly tested.
 * 
 * @author egonw
 */
public class CoverageTest extends AbstractCoverageTest {
    
    private static BibtexManager manager = new BibtexManager();

    @Override
    public IBioclipseManager getManager() {
        return manager;
    }
    
    @Override
    public Class<? extends IBioclipseManager> getManagerInterface() {
      return IBibtexManager.class;
    }
}
