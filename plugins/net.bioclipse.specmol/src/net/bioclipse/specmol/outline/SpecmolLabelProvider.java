package net.bioclipse.specmol.outline;

import net.bioclipse.spectrum.Activator;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLSpectrum;

public class SpecmolLabelProvider implements ILabelProvider {

    private final static Image spectrumImage 
    = Activator.imageDescriptorFromPlugin(
        Activator.PLUGIN_ID, "icons/spectrum.gif").createImage();
    
	
	public Image getImage(Object element){
		CMLElement cmlelement=(CMLElement)element;
		if(cmlelement instanceof CMLSpectrum)
			return spectrumImage;
		else 
			return null;
	}

	public String getText(Object element) {
		CMLElement cmlelement=(CMLElement)element;
		String text=cmlelement.getLocalName();
		if(cmlelement instanceof CMLSpectrum)
			text=((CMLSpectrum)cmlelement).getId();
		return text;
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}
}
