/*******************************************************************************
 *Copyright (c) 2009 The Bioclipse Team and others.
 *All rights reserved. This program and the accompanying materials
 *are made available under the terms of the Eclipse Public License v1.0
 *which accompanies this distribution, and is available at
 *http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package net.bioclipse.spectrum.test;

import net.bioclipse.spectrum.business.test.JavaScriptSpectrumManagerPluginTest;
import net.bioclipse.spectrum.business.test.JavaSpectrumManagerPluginTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(value=Suite.class)
@SuiteClasses( value = { JavaSpectrumManagerPluginTest.class,
                         JavaScriptSpectrumManagerPluginTest.class } )
public class AllSpectrumBusinessPluginTestSuite {

}
