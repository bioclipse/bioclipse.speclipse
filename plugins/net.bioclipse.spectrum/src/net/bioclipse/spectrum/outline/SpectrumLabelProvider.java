package net.bioclipse.spectrum.outline;

import net.bioclipse.spectrum.Activator;
import nu.xom.Text;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLConditionList;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMetadataList;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;
import org.xmlcml.cml.element.CMLScalar;
import org.xmlcml.cml.element.CMLSubstance;
import org.xmlcml.cml.element.CMLSubstanceList;

public class SpectrumLabelProvider implements ILabelProvider {

    private final static Image spectrumImage 
    = Activator.imageDescriptorFromPlugin(
        Activator.PLUGIN_ID, "icons/spectrum.gif").createImage();
    private final static Image metadataImage 
    = Activator.imageDescriptorFromPlugin(
        Activator.PLUGIN_ID, "icons/metadata.gif").createImage();
    
	
	public Image getImage(Object element){
		CMLElement cmlelement=(CMLElement)element;
		if(cmlelement instanceof CMLMetadata || cmlelement instanceof CMLMetadataList || cmlelement instanceof CMLConditionList || cmlelement instanceof CMLSubstanceList || cmlelement instanceof CMLSubstance || cmlelement instanceof CMLScalar)
			return metadataImage;
		else
			return spectrumImage;
	}

	public String getText(Object element) {
		CMLElement cmlelement=(CMLElement)element;
		String text=cmlelement.getLocalName();
		if(cmlelement instanceof CMLMetadata)
			text=((CMLMetadata)cmlelement).getId()+": "+((CMLMetadata)cmlelement).getContent();
		else if(cmlelement instanceof CMLMetadataList)
			text="Metadata List";
		else if(cmlelement instanceof CMLPeakList)
			text="Peak List";
		else if(cmlelement instanceof CMLPeak)
			text=((CMLPeak)cmlelement).getXValue()+" "+((CMLPeak)cmlelement).getYValue();
		else if(cmlelement instanceof CMLConditionList)
			text="Condition List";
		else if(cmlelement instanceof CMLSubstanceList)
			text="Substance List";
		else if(cmlelement instanceof CMLScalar)
			text=((CMLScalar)cmlelement).getTitle()+" "+((CMLScalar)cmlelement).getValue();
		else if(cmlelement instanceof CMLSubstance)
			text=((CMLSubstance)cmlelement).getTitle()+" "+((Text)cmlelement.getChild(0)).getValue();
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
