/*******************************************************************************
 * Copyright (c)      2008  Ola Spjuth <ospjuth@users.sf.net>
 *               2008-2009  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.nmrshiftdb.business.test;

import net.bioclipse.nmrshiftdb.Activator;

import org.junit.BeforeClass;

public class JavaNmrshiftdbManagerPluginTest extends AbstractNmrshiftdbManagerPluginTest {

    @BeforeClass 
    public static void setupNmrshiftdbManagerPluginTest() throws Exception {
        nmrshiftdbmmanager = Activator.getDefault().getJavaNmrshiftdbManager();
    }

}
