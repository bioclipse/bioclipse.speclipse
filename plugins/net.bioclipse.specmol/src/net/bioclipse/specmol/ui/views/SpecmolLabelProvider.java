package net.bioclipse.specmol.ui.views;

import java.util.HashMap;
import java.util.Map;

import net.bioclipse.specmol.Activator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLSpectrum;


public class SpecmolLabelProvider implements ILabelProvider {

    private final static Map<String,Image> cachedImages
    = new HashMap<String,Image>() {
        {
                put("spectrum", createImage(net.bioclipse.spectrum.Activator.PLUGIN_ID, "icons/", "spectrum.gif"));
                put("molecule", createImage(net.bioclipse.cdk.jchempaint.Activator.PLUGIN_ID,"icons/", "chem.png"));
        }

        private Image createImage( String pluginid, String prefix, String type ) {
          ImageDescriptor desc=Activator.imageDescriptorFromPlugin(
                pluginid, prefix + type);
          if (desc==null) desc=ImageDescriptor.getMissingImageDescriptor();
            return desc.createImage();
        }
    };
    
    public Image getImage( Object element ) {

        if (element instanceof CMLSpectrum) {
            return cachedImages.get("spectrum");
        }
        else if (element instanceof CMLMolecule) {
            return cachedImages.get("molecule");
        }
        else {
            return null;
        }
    }

    public String getText( Object element ) {
        if(element instanceof IFile)
            return ((IFile)element).getName();
        else
            return ((CMLElement)element).getId();
    }

    public void addListener( ILabelProviderListener listener ) {

        // TODO Auto-generated method stub

    }

    public void dispose() {

        // TODO Auto-generated method stub

    }

    public boolean isLabelProperty( Object element, String property ) {

        // TODO Auto-generated method stub
        return false;
    }

    public void removeListener( ILabelProviderListener listener ) {

        // TODO Auto-generated method stub

    }

}
