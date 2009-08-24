package net.bioclipse.spectrum.editor;

import net.bioclipse.spectrum.editor.SpectrumEditor;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;

/**
 * General class for metadata editor pages
 * @author hel
 *
 */
public class MetadataFormPage extends FormPage {

	private boolean isDirty;
	protected SpectrumEditor editor;
	protected MetadataModifiyListener modifyListener;

	/**
	 * Constructor
	 * 
	 * @param editor	the pages' editor
	 * @param id		the id of the page - can be null
	 * @param title		the title of the page, which will be shown on the page	
	 */
	public MetadataFormPage(FormEditor editor, String id, String title) {
		super(editor, id, title);
		this.editor = (SpectrumEditor) editor;
		modifyListener = new MetadataModifiyListener((GeneralMetadataFormPage)this, this.editor.getSpectrum());
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormPage#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return this.isDirty;
	}
	
	/**
	 * For setting the page to dirty
	 * 
	 * @param state the dirty state
	 */
	public void setDirty(boolean state) {
        this.isDirty = state;
        firePropertyChange(PROP_DIRTY);
	}

}
