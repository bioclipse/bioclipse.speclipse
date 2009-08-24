/*******************************************************************************
 * Copyright (c) 2008-2009  Stefan Kuhn <stefan.kuhn@ebi.ac.uk
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.bibtex.popup.actions;

import net.sf.jabref.JabRef;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;

public class JabRefAction extends AbstractHandler{

    public Object execute( ExecutionEvent event ) throws ExecutionException {
        try{
            ISelection sel=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
            if (sel.isEmpty()==false){
                if (sel instanceof IStructuredSelection) {
                IStructuredSelection ssel = (IStructuredSelection) sel;
              String[] args={((IFile)ssel.getFirstElement()).getLocation().toOSString()};
              JabRef.main(args);
            }
          }
        }catch(Exception ex){
          ex.printStackTrace();
        }
        return null;
    }

}
