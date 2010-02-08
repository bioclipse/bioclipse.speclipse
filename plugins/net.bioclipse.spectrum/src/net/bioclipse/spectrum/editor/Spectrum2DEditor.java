/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *****************************************************************************/

package net.bioclipse.spectrum.editor;

import java.io.PrintWriter;
import java.io.StringBufferInputStream;
import java.io.StringWriter;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.ISpectrum;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.spectrum.Activator;
import net.bioclipse.spectrum.business.ISpectrumManager;
import net.bioclipse.spectrum.domain.JumboSpectrum;
import net.bioclipse.spectrum.graph2d.Spectrum2DDisplay;
import net.bioclipse.spectrum.outline.SpectrumOutlinePage;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.jcamp.parser.JCAMPException;
import org.jcamp.parser.JCAMPWriter;
import org.jcamp.spectrum.Spectrum;
import org.xmlcml.cml.base.CMLSerializer;
import org.xmlcml.cml.element.CMLSpectrum;

import spok.parser.CMLToJcampSpectrumMapper;

public class Spectrum2DEditor extends FormEditor {

    public static final String        EDITOR_ID  =
                                                         "net.bioclipse.spectrum.editor.SpectrumEditor";
    private static final Logger       logger     =
                                                         Logger
                                                                 .getLogger( Spectrum2DEditor.class );
    private static String             filetype;
    private CMLSpectrum               spectrum;
    private TextEditor                textEditor;
    private int                       indexpeak;
    private int                       indexchart;
    private int                       indexsource;
    private Spectrum2DDisplay         display2d;
    public final static String        JCAMP_TYPE = "jdx";
    public final static String        CML_TYPE   = "cml";
    private SpectrumOutlinePage       fOutlinePage;
    private boolean 				  fromJS =false;	

    public void init( IEditorSite site, IEditorInput input )
                                                            throws PartInitException {

        super.init( site, input );
        setPartName( input.getName() );
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getSite().getShell(),
                                                          "net.bioclipse.spectrum.spectrumeditor");
    }

    public CMLSpectrum getSpectrum() {

        return spectrum;
    }

    /*
     * Creates a number of pages: - (non-Javadoc)
     * @see org.eclipse.ui.part.MultiPageEditorPart#createPages()
     */
    protected void addPages() {

        try {
            Object file = getEditorInput().getAdapter( IFile.class );
            if ( !(file instanceof IFile) ) {
            	file = getEditorInput().getAdapter( ISpectrum.class );
            	if(!(file instanceof ISpectrum)){
            		throw new BioclipseException(
                                              "Invalid editor input: Does not provide an IFile" );
            	}else{
            		spectrum = Activator.getDefault().getJavaSpectrumManager().create((ISpectrum)file).getJumboObject();
    	            filetype = "net.bioclipse.contenttypes.cml.singleSpectrum";
    	            fromJS=true;
    	            setPartName( "UNNAMED" );
            	}
            }else{
	            IFile inputFile = (IFile) file;
	            ISpectrumManager spectrumManager = Activator.getDefault()
	                .getJavaSpectrumManager(); 
	            spectrum = spectrumManager.loadSpectrum(inputFile).getJumboObject();
	            filetype = spectrumManager.detectFileType(
	                inputFile.getFileExtension()
	            );
            }
	        if ( spectrum == null ) {
	            System.out.println( "Could not parse file!! " );
	            return;
	        }
	        display2d = new Spectrum2DDisplay();
	        display2d.setSpectrumItem(spectrum);
	        addPage( display2d, this.getEditorInput() );
	        setPageText( 0, "Graph" );
	        if(!fromJS){
	            textEditor = new TextEditor();
	            indexsource = addPage( textEditor, getEditorInput() );
	            setPageText( indexsource, "Source" );
	        }else{
	        	display2d.setDirty( true );
	            firePropertyChange( IEditorPart.PROP_DIRTY );
	        }
        } catch ( Exception e1 ) {
            LogUtils.handleException( e1, logger, Activator.PLUGIN_ID );
            return;
        }

    }

    protected void handlePropertyChange( int propertyId ) {

        this.firePropertyChange( PROP_DIRTY );
        fOutlinePage.setInput( spectrum );
    }


    @Override
    public void doSave( IProgressMonitor monitor ) {
        this.showBusy( true );
    	if(fromJS){
    		doSaveAs();
    	}else{
	        //if we are not in source, we need to Synch from JCP to texteditor
	        if(this.getActivePage()!=indexsource)
	        updateTextEditor();
	        // Use textEditor to save
	        textEditor.doSave( monitor );
	        display2d.setDirty( false );
	        firePropertyChange( IEditorPart.PROP_DIRTY );
    	}
        this.showBusy( false );
    }

    public void updateTextEditor() {
    	if(fromJS)
    		return;
        String returnVal = null;
        if ( filetype.equals( JCAMP_TYPE ) ) {
            Spectrum jdxspectrum =
                    CMLToJcampSpectrumMapper.mapCMLSpectrumToJcamp( spectrum );
            JCAMPWriter jcamp = JCAMPWriter.getInstance();
            String jcampString = null;
            try {
                jcampString = jcamp.toJCAMP( jdxspectrum );
            } catch ( JCAMPException e ) {
                StringWriter strWr = new StringWriter();
                PrintWriter prWr = new PrintWriter( strWr );
                e.printStackTrace( prWr );
                logger.error( strWr.toString() );
            }
            returnVal = jcampString;
        } else if ( filetype.equals( CML_TYPE ) ) {
            CMLSerializer ser = new CMLSerializer();
            ser.setIndent( 2 );
            String xml = ser.getXML( spectrum );
            returnVal = xml;
        }
        // other filetypes should never be opened in the spectrum editor
        textEditor.getDocumentProvider()
                .getDocument( textEditor.getEditorInput() ).set( returnVal );
    }

    @Override
    public void doSaveAs() {

        IProgressMonitor monitor = new NullProgressMonitor();
        boolean correctfiletype = false;
        IFile target = null;
        int ticks = 10000;
        while ( !correctfiletype ) {
            SaveAsDialog saveasdialog =
                    new SaveAsDialog( this.getSite().getShell() );
            Object file = getEditorInput().getAdapter(IFile.class);
            saveasdialog.setOriginalFile( (IFile) file );
            int result = saveasdialog.open();
            if ( result == SaveAsDialog.CANCEL ) {
                correctfiletype = true;
                target = null;
            } else {
                target =
                        ResourcesPlugin.getWorkspace().getRoot()
                                .getFile( saveasdialog.getResult() );
                String filetype = saveasdialog.getResult().getFileExtension();
                if ( filetype == null )
                    filetype = "none";
                if ( "jx,dx,JX,DX,jdx".indexOf( filetype ) > -1 )
                    filetype = JCAMP_TYPE;
                if ( filetype.equals( CML_TYPE )
                     || filetype.equals( JCAMP_TYPE ) ) {
                    correctfiletype = true;
                    monitor.beginTask( "Writing file", ticks );
                    ISpectrumManager spectrumManager =
                            Activator.getDefault().getJavaSpectrumManager();
                    try {
                        spectrumManager
                                .saveSpectrum( new JumboSpectrum( spectrum ),
                                               target, filetype );
                        if(!fromJS)
                        	textEditor.setInput( new FileEditorInput(target) );
                        setPartName( target.getName() );
            	        display2d.setDirty( false );
            	        firePropertyChange( IEditorPart.PROP_DIRTY );
                    } catch ( Exception ex ) {
                    	LogUtils.handleException(ex, logger, Activator.PLUGIN_ID);
                        correctfiletype = false;
                    }
                } else {
                    MessageDialog
                            .openError(
                                        this.getSite().getShell(),
                                        "No valid file type!",
                                        "Valid file types are "
                                                + JCAMP_TYPE
                                                + " and "
                                                + CML_TYPE
                                                + ". The file extension must be one of these!" );
                }
            }
        }
        monitor.worked( ticks );
    }

    @Override
    public boolean isSaveAsAllowed() {

        return true;
    }

    protected void pageChange( int newPageIndex ) {

        super.pageChange( newPageIndex );
        if ( newPageIndex == 1 ) {
            updateTextEditor();
        } else {
            // TODO this will cause unwanted updates
            if (!fromJS && textEditor.isDirty() ) {
                updateFromTextEditor();
            }
        }
        if ( newPageIndex == 0 ) {
            try {
                display2d.update();
            } catch ( Exception e ) {
                logger.error( "Errors trying to parse a JCamp spectrum." );
                e.printStackTrace();
            }
        }
    }

    private void updateFromTextEditor() {

        try {
            spectrum = Activator.getDefault().getJavaSpectrumManager()
                .loadSpectrum( new StringBufferInputStream( textEditor
                                .getDocumentProvider()
                                .getDocument( textEditor.getEditorInput() )
                                .get() ),filetype ).getJumboObject();
            display2d.setSpectrumItem( spectrum );
        } catch ( Exception e ) {
            MessageBox mb =
                    new MessageBox( new Shell(), SWT.OK | SWT.ICON_WARNING );
            mb.setText( "Error parsing text" );
            mb
                    .setMessage( "The source file text could not be parsed. If you edited it, please go back to the text and try corrting it. If not, your changes will be ignored. The error message was: "
                                 + e.getMessage() + "." );
            mb.open();
            e.printStackTrace();
        }
    }

    public Object getAdapter( Class required ) {

        if ( IContentOutlinePage.class.equals( required ) ) {
            if ( fOutlinePage == null ) {
                fOutlinePage = new SpectrumOutlinePage( spectrum);
            }
            return fOutlinePage;
        }
        return super.getAdapter( required );
    }
}
