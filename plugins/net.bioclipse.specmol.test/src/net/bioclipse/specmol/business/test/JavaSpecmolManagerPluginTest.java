/*******************************************************************************
 * Copyright (c) 2009  Stefan Kuhn <stefan.kuhn@ebi.ac.uk>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.specmol.business.test;

import net.bioclipse.specmol.Activator;

import org.junit.BeforeClass;

public class JavaSpecmolManagerPluginTest extends AbstractSpecmolManagerPluginTest {

    @BeforeClass 
    public static void setupCDKManagerPluginTest() throws Exception {
            specmolmanager = Activator.getDefault().getJavaSpecmolManager();
    }

}
