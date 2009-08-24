package net.bioclipse.specmol.outline;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLSpectrum;

public class SpecmolContentProvider implements ITreeContentProvider {
	
	public List<CMLElement> spectra;

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public Object[] getChildren(Object parentElement) {
	    if(parentElement instanceof CMLCml) {
	        CMLCml box = (CMLCml)parentElement;
	        List<CMLElement> elements=box.getChildCMLElements();
	        spectra=new ArrayList<CMLElement>();
	        for(int i=0;i<elements.size();i++){
	        	if(elements.get(i) instanceof CMLSpectrum)
	        		spectra.add(elements.get(i));
	        }
	        return spectra.toArray(); 
	    }
	    return new Object[0];
	}
	
	public Object[] getElements(Object inputElement) {
	    return getChildren(inputElement);
	}
	
	public Object getParent(Object element) {
	    if(element instanceof CMLElement) {
	        return ((CMLElement)element).getParent();
	    }
	    return null;
	}
	
	public boolean hasChildren(Object element) {
	    return getChildren(element).length > 0;
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

}
