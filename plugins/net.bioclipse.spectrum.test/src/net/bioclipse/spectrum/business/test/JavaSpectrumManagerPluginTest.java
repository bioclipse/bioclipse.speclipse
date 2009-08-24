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
package net.bioclipse.spectrum.business.test;

import net.bioclipse.spectrum.Activator;

import org.junit.BeforeClass;

public class JavaSpectrumManagerPluginTest extends AbstractSpectrumManagerPluginTest {

    @BeforeClass 
    public static void setupCDKManagerPluginTest() throws Exception {
            spectrummanager = Activator.getDefault().getJavaSpectrumManager();
    }

}
