/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *****************************************************************************/

package net.bioclipse.spectrum.editor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringBufferInputStream;
import java.io.StringWriter;
import java.net.URL;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.ISpectrum;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.spectrum.Activator;
import net.bioclipse.spectrum.business.ISpectrumManager;
import net.bioclipse.spectrum.domain.JumboSpectrum;
import net.bioclipse.spectrum.outline.SpectrumOutlinePage;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
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
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLScalar;
import org.xmlcml.cml.element.CMLSpectrum;

import spok.parser.CMLToJcampSpectrumMapper;

public class SpectrumEditor extends FormEditor {

    public static final String        EDITOR_ID  =
                                                         "net.bioclipse.spectrum.editor.SpectrumEditor";
    private static final Logger       logger     =
                                                         Logger
                                                                 .getLogger( SpectrumEditor.class );
    private static String             filetype;
    private CMLSpectrum               spectrum;
    private TextEditor                textEditor;
    private int                       indexpeak;
    private int                       indexchart;
    private int                       indexsource;
    private PeakTablePage             peakTablePage;
    private ChartPage                 chartPage;
    private GeneralMetadataFormPage[] metadatapages;
    private int[]                     metadataindices;
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
    	            filetype = "net.bioclipse.contenttypes.cml.singleSpectrum1D";
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
        } catch ( Exception e1 ) {
            LogUtils.handleException( e1, logger );
            return;
        }

        if ( spectrum == null ) {
            System.out.println( "Could not parse file!! " );
            return;
        }

        try {

            // access the mapping file directory
            URL varPluginUrl =
                    Platform.getBundle( "net.bioclipse.spectrum" )
                            .getEntry( "/mappingFiles/" );
            String varInstallPath = null;
            try {
                chartPage = new ChartPage();
                chartPage.setSpectrumItem( spectrum );
                indexchart = addPage( chartPage, this.getEditorInput() );
                setPageText( indexchart, "Charts" );
                varInstallPath =
                        FileLocator.toFileURL( varPluginUrl ).getFile();
                // get a file list of contained files and iterate over them
                File dir = new File( varInstallPath );
                File[] files = dir.listFiles();
                metadatapages = new GeneralMetadataFormPage[files.length + 1];
                metadataindices = new int[files.length + 1];
                // we look which metadata/conditions are not filled
                Node metadataList = null;
                if ( spectrum.getMetadataListElements() != null
                     && spectrum.getMetadataListElements().size() > 0 )
                    metadataList =
                            spectrum.getMetadataListElements().get( 0 ).copy();
                Node conditionList = null;
                if ( spectrum.getConditionListElements() != null
                     && spectrum.getConditionListElements().size() > 0 )
                    conditionList =
                            spectrum.getConditionListElements().get( 0 ).copy();
                Element substancelist = null;
                if ( spectrum.getChildCMLElements( "substanceList" ) != null
                     && spectrum.getChildCMLElements( "substanceList" ).size() > 0 )
                    substancelist =
                            spectrum.getChildCMLElements( "substanceList" )
                                    .get( 0 );
                for ( int i = 0; i < files.length; i++ ) {
                    File file = files[i];
                    if ( file.getName().startsWith( "." ) ) {
                        continue;
                    } else {
                        // for every mapping file create a single
                        // MetadataFormPage and add it to this editor
                        metadatapages[i] =
                                new GeneralMetadataFormPage(
                                                             this,
                                                             new FileInputStream(
                                                                                  file ),
                                                             metadataList,
                                                             conditionList,
                                                             substancelist );
                        try {
                            if ( metadatapages[i] != null ) {
                                metadataindices[i] =
                                        this.addPage( metadatapages[i] );
                            }
                        } catch ( PartInitException e ) {
                            e.printStackTrace();
                        }
                    }

                }
                // if there are elements left, put them into an additional page
                if ( (metadataList != null && metadataList.getChildCount() > 0)
                     || (conditionList != null && conditionList.getChildCount() > 0)
                     || (substancelist != null && substancelist
                             .getChildElements().size() > 0) ) {
                    // we build a dynamic mapping file to resuse the existing
                    // metadata pages
                    Element root = new Element( "dictionaryMapping" );
                    root
                            .addAttribute( new Attribute( "label",
                                                          "Unclassified metadata" ) );
                    root.addAttribute( new Attribute( "id", "Unclassified" ) );
                    Element newMetadataList = new Element( "section" );
                    newMetadataList
                            .addAttribute( new Attribute( "name",
                                                          "metadataList" ) );
                    newMetadataList
                            .addAttribute( new Attribute( "label",
                                                          "Metadata List" ) );
                    root.appendChild( newMetadataList );
                    if ( metadataList != null ) {
                        for ( int i = 0; i < metadataList.getChildCount(); i++ ) {
                            if ( metadataList.getChild( i ) instanceof CMLMetadata ) {
                                // this is messy. The metadatapage relies on ids
                                // being there, so we substitute this if no id
                                // is given.
                                // but there could be all sorts weired
                                // attributes in, which we can't really handle.
                                if ( ((CMLMetadata) metadataList.getChild( i ))
                                        .getName() == null
                                     && ((CMLMetadata) metadataList
                                             .getChild( i )).getId() == null ) {
                                    System.out
                                            .println( "metadata element has neither name nor id. We cannot handle this!" );
                                } else {
                                    Element entry = new Element( "entry" );
                                    if ( ((CMLMetadata) metadataList
                                            .getChild( i )).getId() == null ) {
                                        ((CMLMetadata) metadataList
                                                .getChild( i ))
                                                .setId( ((CMLMetadata) metadataList
                                                        .getChild( i ))
                                                        .getName() );
                                        Nodes tempResult =
                                                spectrum
                                                        .getMetadataListElements()
                                                        .get( 0 )
                                                        .query(
                                                                "*[@name='"
                                                                        + ((CMLMetadata) metadataList
                                                                                .getChild( i ))
                                                                                .getName()
                                                                        + "']" );
                                        if ( tempResult != null
                                             && tempResult.size() > 0 ) {
                                            ((CMLMetadata) tempResult.get( 0 ))
                                                    .setId( ((CMLMetadata) metadataList
                                                            .getChild( i ))
                                                            .getName() );
                                        }
                                    }
                                    if ( ((CMLMetadata) metadataList
                                            .getChild( i )).getName() == null )
                                        ((CMLMetadata) metadataList
                                                .getChild( i ))
                                                .setName( ((CMLMetadata) metadataList
                                                        .getChild( i )).getId() );
                                    entry
                                            .addAttribute( new Attribute(
                                                                          "id",
                                                                          ((CMLMetadata) metadataList
                                                                                  .getChild( i ))
                                                                                  .getId() ) );
                                    entry
                                            .addAttribute( new Attribute(
                                                                          "allowedForSpectrumTypes",
                                                                          spectrum
                                                                                  .getType() ) );
                                    entry
                                            .addAttribute( new Attribute(
                                                                          "label",
                                                                          ((CMLMetadata) metadataList
                                                                                  .getChild( i ))
                                                                                  .getName() ) );
                                    newMetadataList.appendChild( entry );
                                }
                            }
                        }
                    }
                    Element newSubstanceList = new Element( "section" );
                    newSubstanceList
                            .addAttribute( new Attribute( "name",
                                                          "substanceList" ) );
                    newSubstanceList
                            .addAttribute( new Attribute( "label",
                                                          "Substance List" ) );
                    root.appendChild( newSubstanceList );
                    if ( substancelist != null ) {
                        for ( int i = 0; i < substancelist.getChildCount(); i++ ) {
                            if ( substancelist.getChild( i ) instanceof CMLScalar ) {
                                if ( ((CMLScalar) substancelist.getChild( i ))
                                        .getDictRef() == null
                                     && ((CMLScalar) substancelist.getChild( i ))
                                             .getId() == null ) {
                                    System.out
                                            .println( "substance element has neither dictref nor id. We cannot handle this!" );
                                } else {
                                    Element entry = new Element( "entry" );
                                    if ( ((CMLScalar) substancelist
                                            .getChild( i )).getId() == null ) {
                                        ((CMLScalar) substancelist.getChild( i ))
                                                .setId( ((CMLScalar) substancelist
                                                        .getChild( i ))
                                                        .getDictRef() );
                                        Nodes tempResult =
                                                spectrum
                                                        .getSubstanceListElements()
                                                        .get( 0 )
                                                        .query(
                                                                "*[@name='"
                                                                        + ((CMLScalar) substancelist
                                                                                .getChild( i ))
                                                                                .getDictRef()
                                                                        + "']" );
                                        if ( tempResult != null
                                             && tempResult.size() > 0 ) {
                                            ((CMLScalar) tempResult.get( 0 ))
                                                    .setId( ((CMLScalar) substancelist
                                                            .getChild( i ))
                                                            .getDictRef() );
                                        }
                                    }
                                    if ( ((CMLScalar) substancelist
                                            .getChild( i )).getDictRef() == null )
                                        ((CMLScalar) substancelist.getChild( i ))
                                                .setDictRef( ((CMLScalar) substancelist
                                                        .getChild( i )).getId() );
                                    entry
                                            .addAttribute( new Attribute(
                                                                          "id",
                                                                          ((CMLScalar) substancelist
                                                                                  .getChild( i ))
                                                                                  .getId() ) );
                                    entry
                                            .addAttribute( new Attribute(
                                                                          "allowedForSpectrumTypes",
                                                                          spectrum
                                                                                  .getType() ) );
                                    entry
                                            .addAttribute( new Attribute(
                                                                          "label",
                                                                          ((CMLScalar) substancelist
                                                                                  .getChild( i ))
                                                                                  .getDictRef() ) );
                                    newSubstanceList.appendChild( entry );
                                }
                            }
                        }
                    }
                    Element newConditionList = new Element( "section" );
                    newConditionList
                            .addAttribute( new Attribute( "name",
                                                          "conditionList" ) );
                    newConditionList
                            .addAttribute( new Attribute( "label",
                                                          "Condition List" ) );
                    root.appendChild( newConditionList );
                    if ( conditionList != null ) {
                        for ( int i = 0; i < conditionList.getChildCount(); i++ ) {
                            if ( conditionList.getChild( i ) instanceof CMLScalar ) {
                                if ( ((CMLScalar) conditionList.getChild( i ))
                                        .getTitle() == null
                                     && ((CMLScalar) conditionList.getChild( i ))
                                             .getId() == null ) {
                                    System.out
                                            .println( "condition element has neither title nor id. We cannot handle this!" );
                                } else {
                                    Element entry = new Element( "entry" );
                                    if ( ((CMLScalar) conditionList
                                            .getChild( i )).getId() == null ) {
                                        ((CMLScalar) conditionList.getChild( i ))
                                                .setId( ((CMLScalar) conditionList
                                                        .getChild( i ))
                                                        .getDictRef() );
                                        Nodes tempResult =
                                                spectrum
                                                        .getConditionListElements()
                                                        .get( 0 )
                                                        .query(
                                                                "*[@name='"
                                                                        + ((CMLScalar) conditionList
                                                                                .getChild( i ))
                                                                                .getDictRef()
                                                                        + "']" );
                                        if ( tempResult != null
                                             && tempResult.size() > 0 ) {
                                            ((CMLScalar) tempResult.get( 0 ))
                                                    .setId( ((CMLScalar) conditionList
                                                            .getChild( i ))
                                                            .getDictRef() );
                                        }
                                    }
                                    if ( ((CMLScalar) conditionList
                                            .getChild( i )).getDictRef() == null )
                                        ((CMLScalar) conditionList.getChild( i ))
                                                .setDictRef( ((CMLScalar) conditionList
                                                        .getChild( i )).getId() );
                                    entry
                                            .addAttribute( new Attribute(
                                                                          "id",
                                                                          ((CMLScalar) conditionList
                                                                                  .getChild( i ))
                                                                                  .getId() ) );
                                    entry
                                            .addAttribute( new Attribute(
                                                                          "allowedForSpectrumTypes",
                                                                          spectrum
                                                                                  .getType() ) );
                                    entry
                                            .addAttribute( new Attribute(
                                                                          "label",
                                                                          ((CMLScalar) conditionList
                                                                                  .getChild( i ))
                                                                                  .getTitle() ) );
                                    newConditionList.appendChild( entry );
                                }
                            }
                        }
                    }
                    Document unclassified = new Document( root );
                    metadatapages[metadatapages.length - 1] =
                            new GeneralMetadataFormPage(
                                                         this,
                                                         new ByteArrayInputStream(
                                                                                   unclassified
                                                                                           .toXML()
                                                                                           .getBytes(
                                                                                                      "US-ASCII" ) ),
                                                         metadataList,
                                                         conditionList,
                                                         substancelist );
                    metadataindices[metadataindices.length - 1] =
                            this
                                    .addPage( metadatapages[metadatapages.length - 1] );
                }
            } catch ( IOException e ) {
                StringWriter strWr = new StringWriter();
                PrintWriter prWr = new PrintWriter( strWr );
            }
            peakTablePage = new PeakTablePage();
            peakTablePage.setSpectrumItem( spectrum );
            indexpeak = addPage( peakTablePage, this.getEditorInput() );
            setPageText( indexpeak, "Peak Table" );
            if(!fromJS){
	            textEditor = new TextEditor();
	            indexsource = addPage( textEditor, getEditorInput() );
	            setPageText( indexsource, "Source" );
            }else{
	            peakTablePage.setDirty( true );
	            for ( int i = 0; i < metadataindices.length; i++ ) {
	                if ( metadatapages[i] != null ) {
	                    metadatapages[i].setDirty( true );
	                }
	            }
	            firePropertyChange( IEditorPart.PROP_DIRTY );
            }
        } catch ( Exception e ) {
            logger
                    .error( "Errors trying to build pages in the SpectrumEditor." );
            e.printStackTrace();
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
	        peakTablePage.setDirty( false );
	        for ( int i = 0; i < metadataindices.length; i++ ) {
	            if ( metadatapages[i] != null ) {
	                metadatapages[i].setDirty( false );
	            }
	        }
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
            	        peakTablePage.setDirty( false );
            	        for ( int i = 0; i < metadataindices.length; i++ ) {
            	            if ( metadatapages[i] != null ) {
            	                metadatapages[i].setDirty( false );
            	            }
            	        }
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
        if ( newPageIndex == indexsource ) {
            updateTextEditor();
        } else {
            // TODO this will cause unwanted updates
            if (!fromJS && textEditor.isDirty() ) {
                updateFromTextEditor();
            }
        }
        if ( newPageIndex == indexpeak ) {
            try {
                peakTablePage.update();
            } catch ( Exception e ) {
                logger.error( "Errors trying to parse a JCamp spectrum." );
                e.printStackTrace();
            }
        }
        if ( newPageIndex == indexchart ) {
            chartPage.update();
        }
        for ( int i = 0; i < metadataindices.length; i++ ) {
            if ( metadataindices[i] == newPageIndex && metadatapages[i] != null ) {
                metadatapages[i].update();
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
            chartPage.setSpectrumItem( spectrum );
            peakTablePage.setSpectrumItem( spectrum );
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
                fOutlinePage = new SpectrumOutlinePage( spectrum );
            }
            return fOutlinePage;
        }
        return super.getAdapter( required );
    }

    public PeakTablePage getPeakTablePage() {

        return peakTablePage;
    }

    public ChartPage getChartPage() {

        return chartPage;
    }
}
