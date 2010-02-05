/*******************************************************************************
  * Copyright (c) 2010 Stefan Kuhn
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stefan Kuhn - initial API and implementation
 ******************************************************************************/
package net.bioclipse.spectrum.describer;

import net.bioclipse.core.domain.IBioObject;
import net.bioclipse.spectrum.Activator;
import net.bioclipse.spectrum.business.ISpectrumManager;
import net.bioclipse.ui.business.describer.IBioObjectDescriber;


/**
 * An IBioObjectDescriber associating every ISpectrum with the SpectrumEditor.
 *
 */
public class SpectrumEditorDescriber implements IBioObjectDescriber {

    ISpectrumManager spectrum;

    public SpectrumEditorDescriber() {

        spectrum=Activator.getDefault().getJavaSpectrumManager();
    }

    public String getPreferredEditorID( IBioObject object ) {
       	return "net.bioclipse.spectrum.editor.SpectrumEditor";
    }

}
