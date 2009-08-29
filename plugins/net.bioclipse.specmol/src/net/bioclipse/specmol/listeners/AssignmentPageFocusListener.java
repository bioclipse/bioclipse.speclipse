/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.specmol.listeners;

import net.bioclipse.specmol.editor.AssignmentPage;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;


public class AssignmentPageFocusListener implements FocusListener {

	private AssignmentPage page;

	public AssignmentPageFocusListener(AssignmentPage page) {
		this.page = page;
	}

	public void focusGained(FocusEvent e) {
		page.setFocus();

	}

	public void focusLost(FocusEvent e) {
		 page.lostFocus();
	}

}