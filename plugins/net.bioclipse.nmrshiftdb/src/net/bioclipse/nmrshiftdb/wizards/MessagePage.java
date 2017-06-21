package net.bioclipse.nmrshiftdb.wizards;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Vector;

import net.bioclipse.nmrshiftdb.util.Bc_nmrshiftdbConstants;
import net.bioclipse.nmrshiftdb.util.NmrshiftdbUtils;
import net.bioclipse.specmol.editor.SpecMolEditor;
import net.bioclipse.spectrum.editor.MetadataUtils;
import nu.xom.Elements;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.geometry.BondTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.xmlcml.cml.element.CMLBond;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLSpectrum;



/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (cml).
 */

public class MessagePage extends WizardPage {
	private CMLCml cmlcml; 
	Vector<Text> texts1=new Vector<Text>();
	Vector<Text> texts2=new Vector<Text>();
	Vector<Text> texts3=new Vector<Text>();
	Vector<Text> texts4=new Vector<Text>();
	Vector<Text> texts5=new Vector<Text>();
	Vector<Text> texts6=new Vector<Text>();
	Vector<Text> texts7=new Vector<Text>();
	Vector<Button> selections=new Vector<Button>();
	Text text1mol=null;
	Text text2mol=null;
	Text text3mol=null;
	Text text4mol=null;
	Vector<Button> doublebondconfigurations=new Vector<Button>();
	private boolean allright=false;
	private Text textNucleus;

	public boolean isAllright() {
		return allright;
	}
	
	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public MessagePage(CMLCml cmlcml) {
		super("MessagePage");
		setTitle("Submit to NMRShiftDB wizard");
		setDescription("Here you can check and change the conditions used for the NMRShiftDB submit. You can also enter these in the spectrum editor");
		this.cmlcml=cmlcml;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		GridData gridData2 = new GridData();
		gridData2.widthHint=400;
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
		Elements spectra = cmlcml.getChildCMLElements("spectrum");
	    TabFolder folder = new TabFolder(container , SWT.NONE);
		TabItem tabitemmol = new TabItem(folder , SWT.NONE);
	    Composite compositemol = new Composite(folder , SWT.NONE);
	    tabitemmol.setControl(compositemol);
	    tabitemmol.setText("Molecule");
		GridLayout layoutmol = new GridLayout();
		compositemol.setLayout(layoutmol);
		layoutmol.numColumns = 2;
		layoutmol.verticalSpacing = 9;
		Label label1mol=new Label(compositemol, SWT.NULL);
		label1mol.setText("Names (separated by ;):");
		text1mol=new Text(compositemol,SWT.WRAP | SWT.BORDER);
		text1mol.setLayoutData(gridData2);
    text1mol.addTraverseListener(new TraverseListener() {
        public void keyTraversed(TraverseEvent e) {
          if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
            e.doit = true;
          }
        }
      });
		Label label3mol=new Label(compositemol, SWT.NULL);
		label3mol.setText("Keywords (separated by ;)");
		text3mol=new Text(compositemol,SWT.WRAP | SWT.BORDER);
		text3mol.setLayoutData(gridData2);
    text3mol.addTraverseListener(new TraverseListener() {
        public void keyTraversed(TraverseEvent e) {
          if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
            e.doit = true;
          }
        }
      });
		StringBuffer keywords=new StringBuffer();
		CMLMolecule molecule=((CMLMolecule)cmlcml.getChildCMLElements("molecule").get(0));
		if(molecule.getChildCMLElements("metadataList").size()>0){
			List<CMLMetadata> keywordmetadatas = MetadataUtils.getMetadataDescendantsByName(MetadataUtils.getAllInOneMetadataList(molecule).getMetadataDescendants(),"nmr:keyword");
			for(int i=0;i<keywordmetadatas.size();i++){
				keywords.append(keywordmetadatas.get(i).getContent()+"; ");
			}
		}
		keywords=NmrshiftdbUtils.removeLastComma(keywords);
		text3mol.setText(keywords.toString());
		Label label4mol=new Label(compositemol, SWT.NULL);
		label4mol.setText("Weblinks to the structure, one per line and a comment, seperated by ;\r\nExample:\r\nwww.sample.org/sample;Mr. X's paper about this molecule\r\nwww.test.com;Also about this molecule)");
		text4mol=new Text(compositemol,SWT.MULTI | SWT.BORDER);
		GridData gridData3 = new GridData();
		gridData3.widthHint=400;
		gridData3.heightHint=100;
		text4mol.setLayoutData(gridData3);
    text4mol.addTraverseListener(new TraverseListener() {
        public void keyTraversed(TraverseEvent e) {
          if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
            e.doit = true;
          }
        }
      });
		StringBuffer links=new StringBuffer();
		if(molecule.getChildCMLElements("metadataList").size()>0){
			List<CMLMetadata> linkmetadatas = MetadataUtils.getMetadataDescendantsByName(MetadataUtils.getAllInOneMetadataList(molecule).getMetadataDescendants(),"nmr:link");
			for(int i=0;i<linkmetadatas.size();i++){
				links.append(linkmetadatas.get(i).getContent()+"\r\n");
			}
		}
		links=NmrshiftdbUtils.removeLastComma(links);
		text4mol.setText(links.toString());		
		Label label2mol=new Label(compositemol, SWT.NULL);
		label2mol.setText("CAS number:");
		text2mol=new Text(compositemol,SWT.WRAP | SWT.BORDER);
		text2mol.setLayoutData(gridData2);
    text2mol.addTraverseListener(new TraverseListener() {
        public void keyTraversed(TraverseEvent e) {
          if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
            e.doit = true;
          }
        }
      });
		StringBuffer names=new StringBuffer();
		for(int i=0;i<molecule.getNameElements().size();i++){
			if(molecule.getNameElements().get(i).getAttribute("convention")!=null && ((CMLMolecule)cmlcml.getChildCMLElements("molecule").get(0)).getNameElements().get(i).getAttribute("convention").getValue().equals("CAS"))
				text2mol.setText(molecule.getNameElements().get(i).getXMLContent());
			else
				names.append(molecule.getNameElements().get(i).getXMLContent()+";");
		}
		text1mol.setLayoutData(gridData2);
		text1mol.setText(names.toString());		
		//handle the double bond configurations
		Label doublebondLabel=new Label(compositemol, SWT.NULL);
		doublebondLabel.setText("Check if there is a valid double bond configuration round this bond");
		GridData gdmol = new GridData(GridData.FILL_HORIZONTAL);
		gdmol.horizontalSpan = 2;
		doublebondLabel.setLayoutData(gdmol);
		try{
	    	CMLReader cmlreader=new CMLReader(new ByteArrayInputStream(molecule.toXML().getBytes()));
	    	IAtomContainer cdkmol=((ChemFile)cmlreader.read(new ChemFile())).getChemSequence(0).getChemModel(0).getMoleculeSet().getAtomContainer(0);
		    Iterable<IBond> bonds = cdkmol.bonds();
		    SmilesGenerator sg = new SmilesGenerator();
		    for(IBond cdkBond : bonds) {
		      if (BondTools.isValidDoubleBondConfiguration(cdkmol, cdkBond)) {
		    	  Button button=new Button(compositemol,SWT.CHECK);
		    	  Label label=new Label(compositemol, SWT.NULL);
		    	  label.setText((cdkmol.getAtomNumber(cdkBond.getAtom(0)) + 1) + " = " + (cdkmol.getAtomNumber(cdkBond.getAtom(1)) + 1));
		    	  doublebondconfigurations.add(button);
		    	  CMLBond bond=molecule.getBond(molecule.getAtomById(cdkBond.getAtom(0).getID()), molecule.getAtomById(cdkBond.getAtom(1).getID()));
	    		  if(bond.getFirstChildElement(Bc_nmrshiftdbConstants.doublebondconfiguration)!=null){
	    			  button.setSelection(true);
	    		  }
		      }
		    }
		}catch(Exception ex){
			System.err.println("Problems reading double bond configuration");
			ex.printStackTrace();
		}
		for(int h=0;h<spectra.size();h++){
			CMLSpectrum spectrum=(CMLSpectrum)spectra.get(h);
			StringBuffer warning=new StringBuffer();
			List<CMLMetadata> assignmentmetadatas = MetadataUtils.getMetadataDescendantsByName(MetadataUtils.getAllInOneMetadataList(spectrum).getMetadataDescendants(),Bc_nmrshiftdbConstants.assignment);
			if(assignmentmetadatas.size()>0){
				List<CMLMetadata> nmridmetadatas = MetadataUtils.getMetadataDescendantsByName(MetadataUtils.getAllInOneMetadataList(spectrum).getMetadataDescendants(),Bc_nmrshiftdbConstants.nmrid);
					if(nmridmetadatas.size()>0)
						warning.append("This spectrum has already been submitted to NMRShiftDB (id: "+nmridmetadatas.get(0).getContent()+"). Your submit will be treated as an edit!");
			}
			TabItem tabitem = new TabItem(folder , SWT.NONE);
		    Composite composite = new Composite(folder , SWT.NONE);
		    tabitem.setControl(composite);
		    tabitem.setText("Spectrum "+spectrum.getId());
			GridLayout layout2 = new GridLayout();
			composite.setLayout(layout2);
			layout2.numColumns = 2;
			layout2.verticalSpacing = 9;
			Button selection = new Button(composite, SWT.CHECK);
			Label selectionlabel=new Label(composite, SWT.NULL);
			selectionlabel.setText("Include this spectrum in the submit");
			selection.setSelection(true);
			selections.add(selection);			
			Label warningLabel=new Label(composite, SWT.NULL);
			warningLabel.setText(warning.toString());
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			warningLabel.setLayoutData(gd);
			Label label1=new Label(composite, SWT.NULL);
			label1.setText("Frequency [Mhz] ("+Bc_nmrshiftdbConstants.frequency+"):");
			Text text1=new Text(composite,SWT.WRAP | SWT.BORDER);
			text1.setLayoutData(gridData2);
	    text1.addTraverseListener(new TraverseListener() {
	        public void keyTraversed(TraverseEvent e) {
	          if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
	            e.doit = true;
	          }
	        }
	      });
			text1.setText("Unreported");
			if(spectrum.getConditionListElements().size()>0){
				Elements els=spectrum.getConditionListElements().get(0).getChildCMLElements("scalar");
				for(int i=0;i<els.size();i++){
					if(els.get(i).getAttribute("dictRef")!=null && els.get(i).getAttribute("dictRef").getValue().equals(Bc_nmrshiftdbConstants.frequency)){
						text1.setText(els.get(i).getValue().toString());
						break;
					}
				}
			}
			Label label2=new Label(composite, SWT.NULL);
			label2.setText("Solvent ("+Bc_nmrshiftdbConstants.solvent+"):");
			Text text2=new Text(composite,SWT.WRAP | SWT.BORDER);
			text2mol.setLayoutData(gridData2);
			text2.setText("Unreported");
			text2.setLayoutData(gridData2);
	    text2.addTraverseListener(new TraverseListener() {
	        public void keyTraversed(TraverseEvent e) {
	          if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
	            e.doit = true;
	          }
	        }
	      });
			if(spectrum.getConditionListElements().size()>0){
				Elements els=spectrum.getConditionListElements().get(0).getChildCMLElements("scalar");
				for(int i=0;i<els.size();i++){
					if(els.get(i).getAttribute("dictRef")!=null && els.get(i).getAttribute("dictRef").getValue().equals(Bc_nmrshiftdbConstants.solvent)){
						text2.setText(els.get(i).getValue().toString());
						break;
					}
				}
			}
			Label label3=new Label(composite, SWT.NULL);
			label3.setText("Temperature [K]("+Bc_nmrshiftdbConstants.temperature+"):");
			Text text3=new Text(composite,SWT.WRAP | SWT.BORDER);
			text3.setLayoutData(gridData2);
			text3.setText("Unreported");
			text3.setLayoutData(gridData2);
	    text3.addTraverseListener(new TraverseListener() {
	        public void keyTraversed(TraverseEvent e) {
	          if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
	            e.doit = true;
	          }
	        }
	      });
			if(spectrum.getConditionListElements().size()>0){
				Elements els=spectrum.getConditionListElements().get(0).getChildCMLElements("scalar");
				for(int i=0;i<els.size();i++){
					if(els.get(i).getAttribute("dictRef")!=null && els.get(i).getAttribute("dictRef").getValue().equals(Bc_nmrshiftdbConstants.temperature)){
						text3.setText(els.get(i).getValue().toString());
						break;
					}
				}
			}
			Label label4=new Label(composite, SWT.NULL);
			label4.setText("Assignment method ("+Bc_nmrshiftdbConstants.assignment+"):");
			Text text4=new Text(composite,SWT.WRAP | SWT.BORDER);
			text4.setLayoutData(gridData2);
			text4.setText("Unreported");
			text4.setLayoutData(gridData2);
	    text4.addTraverseListener(new TraverseListener() {
	        public void keyTraversed(TraverseEvent e) {
	          if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
	            e.doit = true;
	          }
	        }
	      });
			if(assignmentmetadatas.size()>0){
				CMLMetadata metadata=assignmentmetadatas.get(0);
				text4.setText(metadata.getContent());
			}
			Label label5=new Label(composite, SWT.NULL);
			label5.setText("Measured nucleus ("+SpecMolEditor.nucleus+"):");
			textNucleus=new Text(composite,SWT.WRAP | SWT.BORDER);
			textNucleus.setLayoutData(gridData2);
	    textNucleus.addTraverseListener(new TraverseListener() {
	        public void keyTraversed(TraverseEvent e) {
	          if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
	            e.doit = true;
	          }
	        }
	      });
			List<CMLMetadata> nucleusmetadatas = MetadataUtils.getMetadataDescendantsByName(MetadataUtils.getAllInOneMetadataList(spectrum).getMetadataDescendants(),SpecMolEditor.nucleus);
			if(nucleusmetadatas.size()>0){
				CMLMetadata metadata=nucleusmetadatas.get(0);
				textNucleus.setText(metadata.getContent());
			}
			textNucleus.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					checkForCompletion(true);
				}
			});			
			Label label6=new Label(composite, SWT.NULL);
			label6.setText("Categories (separated by ;)");
			Text text6=new Text(composite,SWT.WRAP | SWT.BORDER);
			text6.setLayoutData(gridData2);
	    text6.addTraverseListener(new TraverseListener() {
	        public void keyTraversed(TraverseEvent e) {
	          if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
	            e.doit = true;
	          }
	        }
	      });
			StringBuffer categories=new StringBuffer();
			List<CMLMetadata> spectrumkeywordmetadatas = MetadataUtils.getMetadataDescendantsByName(MetadataUtils.getAllInOneMetadataList(spectrum).getMetadataDescendants(),"nmr:keyword");
			for(int i=0;i<spectrumkeywordmetadatas.size();i++){
				categories.append(spectrumkeywordmetadatas.get(i).getContent()+"; ");
			}
			categories=NmrshiftdbUtils.removeLastComma(categories);
			text6.setText(categories.toString());
			Label label7=new Label(composite, SWT.NULL);
			label7.setText("Weblinks to the spectrum, one per line and a comment, seperated by ;\r\nExample:\r\nwww.sample.org/sample;Mr. X's paper about this molecule\r\nwww.test.com;Also about this molecule)");
			Text text7=new Text(composite,SWT.MULTI | SWT.BORDER);
			text7.setLayoutData(gridData3);
	    text7.addTraverseListener(new TraverseListener() {
	        public void keyTraversed(TraverseEvent e) {
	          if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
	            e.doit = true;
	          }
	        }
	      });
			StringBuffer linksspec=new StringBuffer();
			List<CMLMetadata> spectrumlinkmetadatas = MetadataUtils.getMetadataDescendantsByName(MetadataUtils.getAllInOneMetadataList(spectrum).getMetadataDescendants(),"nmr:link");
			for(int i=0;i<spectrumlinkmetadatas.size();i++){
				linksspec.append(spectrumlinkmetadatas.get(i).getContent()+"\r\n");
			}
			linksspec=NmrshiftdbUtils.removeLastComma(linksspec);
			text7.setText(linksspec.toString());		
			texts1.add(text1);
			texts2.add(text2);
			texts3.add(text3);
			texts4.add(text4);
			texts5.add(textNucleus);
			texts6.add(text6);
			texts7.add(text7);
		}
		setControl(container);
		checkForCompletion(false);
	}

	public Vector<Text> getTexts1() {
		return texts1;
	}

	public Vector<Text> getTexts2() {
		return texts2;
	}

	public Vector<Text> getTexts3() {
		return texts3;
	}

	public Vector<Text> getTexts4() {
		return texts4;
	}

	public Vector<Text> getTexts5() {
		return texts5;
	}

	public Vector<Button> getSelections() {
		return selections;
	}
	/**
	 * If page not complete, set error messages
	 */
	protected void checkForCompletion(boolean inoperation) {
		setErrorMessage(null);
		allright=true;
		if (textNucleus.getText() == null || textNucleus.getText().compareTo("") == 0){
			this.setErrorMessage("Every spectrum needs to have a nucleus!");
			allright=false;
		}
		if(allright){
			this.setPageComplete(true);
		}else{
			this.setPageComplete(false);
		}
		if(inoperation)
			getWizard().getContainer().updateButtons();
	}
}
