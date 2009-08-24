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
package net.bioclipse.bibtex.test;

import net.bioclipse.bibtex.Activator;

import org.junit.BeforeClass;

public class JavaBibtexManagerPluginTest extends AbstractBibtexManagerPluginTest {

    @BeforeClass 
    public static void setupCDKManagerPluginTest() throws Exception {
            bibtexmanager = Activator.getDefault().getJavaBibtexManager();
    }

}
