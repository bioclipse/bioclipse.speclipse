/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.specmol.editor;

import java.io.UnsupportedEncodingException;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.specmol.Activator;
import net.bioclipse.specmol.business.ISpecmolManager;
import net.bioclipse.specmol.domain.JumboSpecmol;
import net.bioclipse.specmol.outline.SpecmolOutlinePage;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMolecule;

public class SpecMolEditor extends MultiPageEditorPart {

	private IEditorInput editorInput;
	private AssignmentPage specmoleditorpage;
	private TextEditor textEditor;
	private SpecmolOutlinePage fOutlinePage;
	private static final Logger logger = Logger.getLogger(SpecMolEditor.class);
	public static String nucleus="nmr:OBSERVENUCLEUS";
	public static String fileextension="cml";
	
	public void setTextEditor(TextEditor textEditor) {
		this.textEditor = textEditor;
	}


	public TextEditor getTextEditor() {
		return textEditor;
	}


	private CMLCml cmlcml;
	private int textpageindex;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.MultiPageEditorPart#createPages()
	 */
	@Override
	protected void createPages() {
		createPage0();
		createPage1();
	}
	
	
	/**
	 * create the assignment editor page and add it to the MultiPageEditor
	 */
	void createPage0(){			
		String name =editorInput.getName();
		this.setPartName(name);
		try{
			specmoleditorpage = new AssignmentPage(cmlcml);
			int assignmentpageindex = this.addPage((IEditorPart) specmoleditorpage, editorInput);
			setPageText(assignmentpageindex, "Assignment");
			this.setActivePage(assignmentpageindex);
		} catch (PartInitException e){
			e.printStackTrace();
		}				
	}
	
	/**
	 * create a xmlEditor and add it to the MultiPageEditor
	 */
	void createPage1(){	
		textEditor = new UneditableTextEditor();
		try {
			textpageindex = this.addPage((IEditorPart) textEditor, getEditorInput());
			setPageText(textpageindex,"Source");
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.MultiPageEditorPart#setPageText(int, java.lang.String)
	 */
	public void setPageText(int index,String text){
		super.setPageText(index,text);
	}

    /**
     * Gets a CMLSpectrum from the editorInput
     * @return The CMLSpectrum in the editorInput, null if not possible
     * @throws BioclipseException 
     */
    private CMLCml getModelFromEditorInput(IEditorInput input) throws BioclipseException{

        Object file = input.getAdapter(IFile.class);
        if (!(file instanceof IFile)) {
            throw new BioclipseException(
                    "Invalid editor input: Does not provide an IFile");
        }

        IFile inputFile = (IFile) file;
        
        try {
	            ISpecmolManager specmolManager = Activator.getDefault().getJavaSpecmolManager();
	            return specmolManager.loadSpecmol(inputFile).getJumboObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		this.specmoleditorpage.setDirty(false);
		try {
			Activator.getDefault().getJavaSpecmolManager().saveSpecmol(new JumboSpecmol(cmlcml),(IFile)editorInput.getAdapter(IFile.class));
		} catch (BioclipseException e) {
			LogUtils.handleException(e,logger);
		} catch (CoreException e) {
			LogUtils.handleException(e,logger);
		} catch (UnsupportedEncodingException e) {
			LogUtils.handleException(e,logger);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
		//Since CML is the only possible format, no save as is needed
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		//Since CML is the only possible format, no save as is needed
		return false;
	}
	
	/**
	 * Init and validate input.
	 */
	public void init(IEditorSite site, IEditorInput editorInput)	
		throws PartInitException {
		super.init(site, editorInput);
		this.editorInput = editorInput;
		try {
			this.cmlcml=this.getModelFromEditorInput(editorInput);
			CMLMolecule cmlmol=(CMLMolecule)cmlcml.getChildCMLElement("molecule", 0);
			for(int i=0;i<cmlmol.getAtomCount();i++){
				if(cmlmol.getAtom(i).getX2Attribute()==null){
					MessageDialog.openInformation(getSite().getShell() , "No 2D coordinates", "At least one atom in the molecule is missing 2D coordinates. We need these for display and assignment!");
					getSite().getWorkbenchWindow().getActivePage().closeEditor(this,false);
					return;
				}
			}
		} catch (BioclipseException e) {
			LogUtils.handleException(e,logger);
		}

		
	}

	/**
	 * get the assignment editor page
	 * 
	 * @return AssignmentPage specmoleditorpage
	 */
	public AssignmentPage getSpecmoleditorpage() {
		return specmoleditorpage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.MultiPageEditorPart#pageChange(int)
	 */
	@Override
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		//since the text is read only, we do if we came from text
		if(newPageIndex==textpageindex && cmlcml!=null && textEditor!=null){
			textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput()).set(cmlcml.toXML());
		}
	}

	
	@Override
	public void setFocus() {
		super.setFocus();
	}
	
	   public Object getAdapter(Class required) {
		      if (IContentOutlinePage.class.equals(required)) {
		         if (fOutlinePage == null) {
		            fOutlinePage = new SpecmolOutlinePage(
		                           cmlcml,this);
		         }
		         return fOutlinePage;
		      }
		      return super.getAdapter(required);
	   }
}
