package net.bioclipse.specmol.actions;

import java.io.StringBufferInputStream;
import java.util.HashMap;
import java.util.Map;

import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;
import net.bioclipse.chemoinformatics.wizards.WizardHelper;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.specmol.Activator;
import net.bioclipse.specmol.domain.IJumboSpecmol;
import net.bioclipse.specmol.ui.views.SpecmolContentProvider;
import net.bioclipse.spectrum.editor.SpectrumEditor;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLSpectrum;

import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;

import spok.utils.SpectrumUtils;


public class OpenInEditorAction implements IHandler, IPropertyListener {
    
    private static Map<IEditorPart,IFile> orignalFiles=new HashMap<IEditorPart,IFile>();
    private static Map<IEditorPart,CMLElement> orignalChildren=new HashMap<IEditorPart,CMLElement>();
    private static Logger logger = Logger.getLogger(OpenInEditorAction.class);
    
    public void addHandlerListener( IHandlerListener handlerListener ) {
    }

    public void dispose() {
    }

    public Object execute( ExecutionEvent event ) throws ExecutionException {
        ISelection sel=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
        if (sel instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) sel).getFirstElement();
            //we close the edi
            IEditorPart[] editors = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditors();
            for(int i=0;i<editors.length;i++){
                if(((IFile)new SpecmolContentProvider().getParent( element )).equals( editors[i].getEditorInput().getAdapter( IFile.class ) ))
                    if(editors[i].isDirty()){
                        boolean answer = MessageDialog.openQuestion( editors[i].getSite().getShell(), "Closing editor", "You are working on the parent of the entry to edit ("+((IFile)new SpecmolContentProvider().getParent( element )).getName()+"). We need to close this editor. Should the changes be saved?" );
                        if(answer){
                            editors[i].getSite().getPage().saveEditor( editors[i], false );
                        }
                    }
                    editors[i].getSite().getPage().closeEditor(editors[i], false);
            }
            if(element instanceof CMLMolecule){
                IEditorDescriptor desc = PlatformUI.getWorkbench().
                getEditorRegistry().getDefaultEditor( ((CMLMolecule)element).getId()+".cml",Platform.getContentTypeManager().getContentType( "net.bioclipse.contenttypes.cml.singleMolecule2d" ));
                try {
                    IFile tmpFile= net.bioclipse.core.Activator.getVirtualProject().getFile(WizardHelper.findUnusedFileName(new StructuredSelection(net.bioclipse.core.Activator.getVirtualProject()), ((CMLMolecule)element).getId(), ".cml") );
                    tmpFile.create( new StringBufferInputStream(((CMLMolecule)element).toXML()), IFile.FORCE, null);
                    IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new FileEditorInput(tmpFile), desc.getId());
                    editor.addPropertyListener( this );
                    //we remember which files this element came from and
                    //the element in the content provider for the chilren.
                    orignalFiles.put( editor, (IFile)new SpecmolContentProvider().getParent( element ) );
                    orignalChildren.put(editor,(CMLElement)element);
                    System.err.println((IFile)new SpecmolContentProvider().getParent( element ));
                } catch ( Exception e ) {
                    LogUtils.handleException( e, logger );
                }
            }else if(element instanceof CMLSpectrum){
                //for some reason, empty xmlns="" tend to show up, we elimenate these
                SpectrumUtils.namespaceThemAll( ((CMLSpectrum)element).getChildElements() );
                IEditorDescriptor desc = PlatformUI.getWorkbench().
                getEditorRegistry().getDefaultEditor( ((CMLSpectrum)element).getId()+".cml",Platform.getContentTypeManager().getContentType( "net.bioclipse.contenttypes.cml.singleSpectrum" ));
                try {
                    IFile tmpFile= net.bioclipse.core.Activator.getVirtualProject().getFile( WizardHelper.findUnusedFileName(new StructuredSelection(net.bioclipse.core.Activator.getVirtualProject()), ((CMLSpectrum)element).getId(), ".cml") );
                    tmpFile.create( new StringBufferInputStream(((CMLSpectrum)element).toXML()), IFile.FORCE, null);
                    IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new FileEditorInput(tmpFile), desc.getId());
                    editor.addPropertyListener( this );
                    orignalFiles.put( editor, (IFile)new SpecmolContentProvider().getParent( element ) );
                    System.err.println((IFile)new SpecmolContentProvider().getParent( element ));
                    orignalChildren.put(editor,(CMLElement)element);
                } catch ( Exception e ) {
                    LogUtils.handleException( e, logger );
                }                
            }
        }
        return null;
    }

    public boolean isEnabled() {
        return true;
    }

    public boolean isHandled() {
        return true;
    }

    public void removeHandlerListener( IHandlerListener handlerListener ) {
    }

    public void propertyChanged( Object source, int propId ) {
        try{
            IJumboSpecmol specmol = Activator.getDefault().getJavaSpecmolManager().loadSpecmol( orignalFiles.get( source ) );
            CMLCml cmlcml=specmol.getJumboObject();
            if(source instanceof JChemPaintEditor){
              //we update the children content provider content
              orignalChildren.get( source ).removeChildren();
              for(int i=0;i<cmlcml.getChildCount();i++){
                  if(cmlcml.getChild( i ) instanceof CMLMolecule){
                      cmlcml.replaceChild( cmlcml.getChild( i ),new CMLBuilder().parseString( ((JChemPaintEditor)source).getCDKMolecule().toCML()));
                      for(int k=0;k<cmlcml.getChild( i ).getChildCount();k++){
                          if(cmlcml.getChild( i ).getChild( k )!=null)
                              orignalChildren.get( source ).appendChild( cmlcml.getChild( i ).getChild( k ).copy() );
                      }
                      break;
                  }
              }
            }
            if(source instanceof SpectrumEditor){
                CMLSpectrum spectrum = ((SpectrumEditor)source).getSpectrum();
                orignalChildren.get( source ).removeChildren();
                if(spectrum.getId().indexOf( "bcspectrum" )==0){
                    cmlcml.replaceChild( cmlcml.getChildCMLElements( "spectrum" ).get( Integer.parseInt( spectrum.getId().substring( 10 ) ) ), spectrum );
                    for(int k=0;k<cmlcml.getChild( Integer.parseInt( spectrum.getId().substring( 10 )) ).getChildCount();k++){
                        if(cmlcml.getChild( Integer.parseInt( spectrum.getId().substring( 10 )) ).getChild( k )!=null)
                            orignalChildren.get( source ).appendChild( cmlcml.getChild( Integer.parseInt( spectrum.getId().substring( 10 )) ).getChild( k ).copy() );
                    }
                }else{
                  for(int i=0;i<cmlcml.getChildCount();i++){
                      if(cmlcml.getChild( i ) instanceof CMLSpectrum && ((CMLSpectrum)cmlcml.getChild( i )).getId().equals( spectrum.getId())){
                          cmlcml.replaceChild( cmlcml.getChild( i ),spectrum);
                          for(int k=0;k<cmlcml.getChild( i ).getChildCount();k++){
                              if(cmlcml.getChild( i ).getChild( k )!=null)
                                  orignalChildren.get( source ).appendChild( cmlcml.getChild( i ).getChild( k ).copy() );
                          }
                          break;
                      }
                  }
                }
            }
            try{
                //we update the file the edited content came from
                orignalFiles.get( source ).setContents( new StringBufferInputStream(cmlcml.toXML()), IFile.FORCE,null);
            }catch(IllegalArgumentException ex){
                //for some reason, we get a strange error here when saving the change
                //from changing the spectrum, but content is there, so we ignore this.
            }
            //TODO update specmoleditor
        }catch(Exception ex){
            LogUtils.handleException( ex, logger );
        }
    }

}
