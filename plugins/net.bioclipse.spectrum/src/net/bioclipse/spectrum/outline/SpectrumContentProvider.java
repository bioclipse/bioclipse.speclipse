package net.bioclipse.spectrum.outline;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.xmlcml.cml.base.CMLElement;

public class SpectrumContentProvider implements ITreeContentProvider {

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public Object[] getChildren(Object parentElement) {
	    if(parentElement instanceof CMLElement) {
	        CMLElement box = (CMLElement)parentElement;
	        return box.getChildCMLElements().toArray(); 
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
