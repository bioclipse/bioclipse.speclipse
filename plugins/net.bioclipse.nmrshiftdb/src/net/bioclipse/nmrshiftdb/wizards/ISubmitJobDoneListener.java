package net.bioclipse.nmrshiftdb.wizards;

import java.util.List;

import nu.xom.Elements;

import org.w3c.dom.Element;
import org.xmlcml.cml.element.CMLSpectrum;

public interface ISubmitJobDoneListener {

	public void processDownloadedContent(Element e, Elements spectra,List<CMLSpectrum> removedSpectra) throws Exception;
	
}