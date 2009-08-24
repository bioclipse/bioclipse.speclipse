package net.bioclipse.spectrum.action.contribution;

import java.net.URL;

import net.bioclipse.spectrum.dialogs.AddMetadataDialog;
import net.bioclipse.spectrum.editor.SpectrumEditorContributor;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorPart;

public class AddMetadataAction extends Action {

	private SpectrumEditorContributor contributor;

	public AddMetadataAction(SpectrumEditorContributor contributor) {
		this.contributor = contributor;
		URL url = Platform.getBundle(
		"net.bioclipse.spectrum").getEntry("/icons/add_metadata.gif");
		ImageDescriptor imageDesc = ImageDescriptor.createFromURL(url);
		this.setImageDescriptor(imageDesc);
	}

	@Override
	public void run() {
		IEditorPart editor = contributor.getEditor();
		AddMetadataDialog dialog = new AddMetadataDialog(editor.getSite().getShell(), editor);
		dialog.open();
//		super.run();
	}

	@Override
	public String getToolTipText() {
		return "Create new Metadata Entries";
	}

}
