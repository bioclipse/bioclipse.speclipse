package net.bioclipse.specmol.ui.views;

import java.util.HashMap;
import java.util.Map;

import net.bioclipse.core.util.LogUtils;
import net.bioclipse.specmol.Activator;
import net.bioclipse.specmol.domain.IJumboSpecmol;
import nu.xom.Elements;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLSpectrum;


public class SpecmolContentProvider implements ITreeContentProvider {

    private static Map<CMLElement, IFile> parents=new HashMap<CMLElement, IFile>();
    private static final Object[] NO_CHILDREN = new Object[0];
    private final Logger logger = Logger.getLogger(SpecmolContentProvider.class);
    
    public Object[] getChildren( Object parentElement ) {
        if (parentElement instanceof IFile) {
            //must be an assigned spectrum cml file
            IFile modelFile = (IFile) parentElement;
            IJumboSpecmol specmol;
            try {
                specmol = Activator.getDefault().getJavaSpecmolManager().loadSpecmol(modelFile);
                CMLCml cmlcml= specmol.getJumboObject();
                CMLMolecule mol = (CMLMolecule)cmlcml.getChildCMLElement("molecule", 0);
                Elements spectra = cmlcml.getChildCMLElements( "spectrum");
                CMLElement[] children =new CMLElement[spectra.size()+1];
                children[0]=mol;
                parents.put( children[0], modelFile );
                for(int i=0;i<spectra.size();i++){
                    children[i+1]=(CMLElement) spectra.get( i );
                    if(((CMLSpectrum) spectra.get( i )).getId()==null)
                        ((CMLSpectrum) spectra.get( i )).setId( "bcspectrum"+i );
                    parents.put( children[i+1], modelFile );
                }
                return children;
            } catch ( Exception e ) {
                LogUtils.handleException( e, logger );
            }
            return NO_CHILDREN;
        }
        if (parentElement instanceof CMLElement) {
            return NO_CHILDREN;
        }
        return NO_CHILDREN;
    }

    public Object getParent( Object element ) {
        return parents.get( element );
    }

    public boolean hasChildren( Object element ) {
        if(element instanceof IFile)
            return true;
        else
            return false;
    }

    public Object[] getElements( Object inputElement ) {
        return getChildren(inputElement);
    }

    public void dispose() {
    }

    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
    }

}
