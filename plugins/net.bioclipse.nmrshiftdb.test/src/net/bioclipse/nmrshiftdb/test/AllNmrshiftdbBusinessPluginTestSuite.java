/*******************************************************************************
 *Copyright (c) 2009 The Bioclipse Team and others.
 *All rights reserved. This program and the accompanying materials
 *are made available under the terms of the Eclipse Public License v1.0
 *which accompanies this distribution, and is available at
 *http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package net.bioclipse.nmrshiftdb.test;

import net.bioclipse.nmrshiftdb.business.test.JavaScriptNmrshiftdbManagerPluginTest;
import net.bioclipse.nmrshiftdb.business.test.JavaNmrshiftdbManagerPluginTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(value=Suite.class)
@SuiteClasses( value = { JavaNmrshiftdbManagerPluginTest.class,
                         JavaScriptNmrshiftdbManagerPluginTest.class } )
public class AllNmrshiftdbBusinessPluginTestSuite {

}