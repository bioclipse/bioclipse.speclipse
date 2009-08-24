package net.bioclipse.specmol.outline;

import net.bioclipse.specmol.editor.SpecMolEditor;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLCml;

public class SpecmolOutlinePage extends ContentOutlinePage implements ISelectionListener, IAdaptable{
	
    private final String CONTRIBUTOR_ID
    ="net.bioclipse.specmol.outline.SpecmolOutlinePage";
    private CMLCml cmlcml;
    private TreeViewer treeViewer;
    private SpecmolContentProvider contentProvider=new SpecmolContentProvider();
    private SpecMolEditor editor;


	/**
	* Our constructor
	* @param cmlcml The specmol content of the editor
	*/
	public SpecmolOutlinePage(CMLCml cmlcml, SpecMolEditor editor) {
		super();
		this.cmlcml=cmlcml;
		this.editor=editor;
	}


	/**
	* Sets a new input for the outline
	* @param spectrum The new spectrum 
	*/
	public void setInput(CMLCml spectrum){
		treeViewer.setInput(spectrum);
		treeViewer.expandToLevel(2);
	}


	/**
	* Set up the treeviewer for the outline with a spectrum as input
	*/
	public void createControl(Composite parent) {
	
		super.createControl(parent);
	
		treeViewer= getTreeViewer();
		treeViewer.setContentProvider(contentProvider);
		treeViewer.setLabelProvider(new SpecmolLabelProvider());
		treeViewer.addSelectionChangedListener(this);
	
		if (cmlcml==null) return;
	
		setInput(cmlcml);
		getSite().getPage().addSelectionListener(this);
	}
	
	
	/**
	* Update selected items if selected in editor
	*/
	public void selectionChanged(IWorkbenchPart selectedPart,
	                         ISelection selection) {
		if(!(selection instanceof IStructuredSelection) || contentProvider.spectra.indexOf(((IStructuredSelection)selection).getFirstElement())==-1)
			return;
		for(int i=0;i<contentProvider.spectra.size();i++){
			if(contentProvider.spectra.get(i).getId().equals(((CMLElement)((IStructuredSelection) selection).getFirstElement()).getId())){
				editor.getSpecmoleditorpage().setCurrentspectrumnumber(i);
				break;
			}
		}
	}
	
	
	
	/**
	* This is our ID for the TabbedPropertiesContributor
	*/
	public String getContributorId() {
		return CONTRIBUTOR_ID;
	}
	
	
	public Object getAdapter(Class adapter) {
		return null;
	}
}
