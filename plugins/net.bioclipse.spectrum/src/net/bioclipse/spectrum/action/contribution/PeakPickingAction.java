package net.bioclipse.spectrum.action.contribution;

import java.net.URL;

import net.bioclipse.core.util.LogUtils;
import net.bioclipse.spectrum.editor.ChartPage;
import net.bioclipse.spectrum.editor.PeakTablePage;
import net.bioclipse.spectrum.editor.SpectrumEditor;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.editors.text.TextEditor;
import org.xmlcml.cml.element.CMLPeakList;
import org.xmlcml.cml.element.CMLSpectrum;
import org.xmlcml.cml.element.CMLSpectrumData;

import spok.utils.PeakPicker;
import spok.utils.SpectrumUtils;

/**
 * Action for the toolbar and popdown menu item "Pick Peaks" of the
 * ContinuousSpectrumView. Realises the peak picking and notifies the
 * PeakSpectrumView, that the SpectrumItem/CMLItem has changed and therefor the
 * chart must be rebuilt.
 * 
 * @author Tobias Helmus
 * @created 19. Dezember 2005
 * 
 */
public class PeakPickingAction extends Action {

	private SpectrumEditor view;
	private static final Logger logger = Logger.getLogger(PeakPickingAction.class);

	/**
	 * @param view
	 */
	public PeakPickingAction() {
		URL url = Platform.getBundle(
		"net.bioclipse.spectrum").getEntry("/icons/peaks.gif");
		ImageDescriptor imageDesc = ImageDescriptor.createFromURL(url);
		this.setImageDescriptor(imageDesc);
	}
	
	
	public void setActiveEditor(SpectrumEditor view) {
		this.view = view;
		CMLSpectrum spectrum = view.getSpectrum();
		if (spectrum != null) {
			CMLSpectrumData spectrumData = SpectrumUtils
					.getSpectrumData(spectrum);
			if(spectrumData==null){
				this.setEnabled(false);
			}else{
				this.setEnabled(true);
			}
		}
	}

	/**
	 * @param action
	 */
	public void run() {
		CMLSpectrum spectrum = view.getSpectrum();
		if (spectrum != null) {
			CMLSpectrumData spectrumData = SpectrumUtils
					.getSpectrumData(spectrum);
			PeakPicker picker = new PeakPicker(spectrumData);
			CMLPeakList peaks = picker.getPeakArray();
			spectrum.addPeakList(peaks);
			view.getPeakTablePage().setDirty(true);
			try {
				if(view.getActiveEditor() instanceof PeakTablePage)
					view.getPeakTablePage().update();
				if(view.getActiveEditor() instanceof ChartPage)
					view.getChartPage().update();
				if(view.getActiveEditor() instanceof TextEditor)
					view.updateTextEditor();
			} catch (Exception e) {
				LogUtils.handleException(e,logger);
			}
		}
	}
	
	@Override
	public String getToolTipText() {
		return "Pick Peaks";
	}
}
