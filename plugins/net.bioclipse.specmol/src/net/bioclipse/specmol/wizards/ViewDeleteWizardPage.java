/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/
package net.bioclipse.specmol.wizards;

import java.util.HashMap;

import nu.xom.Element;
import nu.xom.Elements;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLSpectrum;

public class ViewDeleteWizardPage extends WizardPage {
	
	private CMLCml cml=null;
	CMLMolecule mol;
	CMLSpectrum spec;
	HashMap<Integer, Element> mapcml=new HashMap<Integer, Element>();
	HashMap<Integer, Element> mapmol=new HashMap<Integer, Element>();
	HashMap<Integer, Element> mapspec=new HashMap<Integer, Element>();
	List list1=null;
	List list2=null;
	List list3=null;
	
	public ViewDeleteWizardPage(CMLCml cml, CMLMolecule mol, CMLSpectrum spec){
		super("View and delete Bibtex entries");
		setTitle("View and delete Bibtex entries");
		setDescription("Entries you choose here will be deleted");
		this.cml=cml;
		this.mol=mol;
		this.spec=spec;
	}

	public void createControl(Composite parent) {
		try{
			Composite container = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			container.setLayout(layout);
			layout.numColumns = 1;
			//handle bibtex in root
			Elements cmlbis=cml.getChildElements("entry","http://bibtexml.sf.net/");
			if(cmlbis.size()>0){
				Label labelcml = new Label(container, SWT.NULL);
			    labelcml.setText("Bibtex entries in root element");
				list1 = new List(container, SWT.SCROLL_PAGE | SWT.SCROLL_LINE);
				for(int i=0;i<cmlbis.size();i++){
					Element el=cmlbis.get(i);
					list1.add(makeString(el.getChildElements().get(0)));
					mapcml.put(new Integer(i),el);
				}
			}
			//handle bibtex in mol
			Elements cmlmol=mol.getChildElements("entry","http://bibtexml.sf.net/");
			if(cmlmol.size()>0){
				Label labelmol = new Label(container, SWT.NULL);
			    labelmol.setText("Bibtex entries of molecule");
				list2 = new List(container, SWT.SCROLL_PAGE | SWT.SCROLL_LINE);
				for(int i=0;i<cmlmol.size();i++){
					Element el=cmlmol.get(i);
					list2.add(makeString(el.getChildElements().get(0)));
					mapmol.put(new Integer(i),el);
				}
				
			}
			//handle bibtex in spec
			Elements cmlspec=spec.getChildElements("entry","http://bibtexml.sf.net/");
			if(cmlspec.size()>0){
				Label labelspec = new Label(container, SWT.NULL);
			    labelspec.setText("Bibtex entries of spectrum");
				list3 = new List(container, SWT.SCROLL_PAGE | SWT.SCROLL_LINE);
				for(int i=0;i<cmlspec.size();i++){
					Element el=cmlspec.get(i);
					list3.add(makeString(el.getChildElements().get(0)));
					mapspec.put(new Integer(i),el);
				}
			}
			setControl(container);
		}catch(Exception ex){
			ex.printStackTrace();
		}		
	}
	
	private String makeString(Element el){
		return el.getChildElements("author","http://bibtexml.sf.net/").get(0).getValue()+": "+el.getChildElements("title","http://bibtexml.sf.net/").get(0).getValue();
	}

	public List getList1() {
		return list1;
	}

	public List getList2() {
		return list2;
	}

	public List getList3() {
		return list3;
	}

}
