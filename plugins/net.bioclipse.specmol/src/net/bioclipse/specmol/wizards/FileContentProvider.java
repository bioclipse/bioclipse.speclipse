/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package net.bioclipse.specmol.wizards;

import java.util.ArrayList;

import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class FileContentProvider implements ITreeContentProvider {

	private static final Logger logger = Logger.getLogger(FileContentProvider.class);
	
	public FileContentProvider() {
	}

	public void dispose() {

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public Object[] getChildren(Object parentElement) {
		ArrayList<IResource> childElements = new ArrayList<IResource>();
		if(parentElement instanceof IContainer){
			IContainer element = (IContainer) parentElement;
			try {
				for(int i=0;i<element.members().length;i++){
					if(element.members()[i] instanceof IResource && ((IResource)element.members()[i]).getName().charAt(0) != '.')
						childElements.add((IResource)element.members()[i]);
				}
			} catch (CoreException e) {
				LogUtils.handleException(e,logger);
			}
		}
		return childElements.toArray();
	}

	public Object getParent(Object element) {
		return ((IFolder)element).getParent();
	}

	public boolean hasChildren(Object element) {
		if(element instanceof IFolder){
			IFolder folder=(IFolder)element;
			try{
				IResource[] children=folder.members();
				for(int i=0;i<children.length;i++){
					if(children[i] instanceof IFile)
						return true;
				}
			}catch(CoreException ex){
				LogUtils.handleException(ex,logger);
			}
		}else if(element instanceof IProject){
			IProject folder=(IProject)element;
			if(folder.isAccessible()){
				try{
					IResource[] children=folder.members();
					for(int i=0;i<children.length;i++){
						if(children[i] instanceof IFile)
							return true;
					}
				}catch(CoreException ex){
					LogUtils.handleException(ex,logger);
				}
			}
		}
		return false;
	}

}
