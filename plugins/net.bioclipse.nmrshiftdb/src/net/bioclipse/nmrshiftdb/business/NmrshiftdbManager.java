package net.bioclipse.nmrshiftdb.business;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cml.contenttypes.CmlFileDescriber;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.domain.ISpectrum;
import net.bioclipse.jobs.IReturner;
import net.bioclipse.managers.business.IBioclipseManager;
import net.bioclipse.nmrshiftdb.util.Bc_nmrshiftdbConstants;
import net.bioclipse.spectrum.editor.MetadataUtils;
import nu.xom.Elements;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.utils.Options;
import org.apache.axis.utils.XMLUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.io.CMLReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLSpectrum;

public class NmrshiftdbManager implements IBioclipseManager {

  	public String getManagerName() {
		return "nmrshiftdb";
	}

	public void searchBySpectrum(ISpectrum cmlspectrum,
              boolean subortotal,
              String serverurl,
              IReturner returner,
              IProgressMonitor monitor) 
	  			throws BioclipseException {
        monitor.beginTask( "Searching in NMRShiftDB", IProgressMonitor.UNKNOWN );
        try{
		    Options opts = new Options(new String[0]);
		    opts.setDefaultURL(serverurl+"/services/NMRShiftDB");
		    Service  service = new Service();
		    final Call call    = (Call) service.createCall();
		    call.setTargetEndpointAddress( new URL(opts.getURL()) );
		    call.setTimeout(1000000);
			final SOAPBodyElement[] input = new SOAPBodyElement[1];
		    DocumentBuilder builder;
		    builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		    Document doc = builder.newDocument();
		    Element cdataElem;
		    cdataElem = doc.createElementNS(opts.getURL(), "doElucidate");
		    Element reqElem;
		    reqElem = doc.createElementNS(opts.getURL(), "suborwhole");
		    Node node;
		    node = doc.createTextNode(subortotal ? "sub" : "whole");//signifies type of search
		    reqElem.appendChild(node);
	        Document document = builder.parse(new ByteArrayInputStream(cmlspectrum.getCML().getBytes()));
		    Node nodeimp=doc.importNode(document.getFirstChild(),true);
		    cdataElem.appendChild(nodeimp);
		    cdataElem.appendChild(reqElem);
		    input[0] = new SOAPBodyElement(cdataElem);
			List<IMolecule> result = new ArrayList<IMolecule>();
	    	Vector          elems = (Vector) call.invoke( input );		    
	    	SOAPBodyElement elem = (SOAPBodyElement) elems.get(0);
	    	Element e    = elem.getAsDOM();
	    	CMLBuilder cmlbuilder = new CMLBuilder();
    		CMLElement cmlElement = (CMLElement) cmlbuilder.parseString(XMLUtils.ElementToString(e));
			CMLCml cmlelud=(CMLCml)cmlElement;
			for(int i=0;i<cmlelud.getCMLChildCount("molecule");i++){
				nu.xom.Element mol=cmlelud.getChildElements().get(i);
				mol.setNamespaceURI(CmlFileDescriber.NS_CML);
				CMLReader reader = new CMLReader(new ByteArrayInputStream(mol.toXML().getBytes()));
		        IChemFile file = (IChemFile)reader.read(DefaultChemObjectBuilder.getInstance().newChemFile());
		        
		        CMLMolecule cmlMol = (CMLMolecule)cmlelud.getChildElements(
                      "molecule",CmlFileDescriber.NS_CML).get(i);
		        List<CMLMetadata> metadataList = 
		            MetadataUtils.getAllInOneMetadataList(cmlMol)
		                         .getMetadataDescendants();
		        
		         
		        List<CMLMetadata> descendents = 
		            MetadataUtils.getMetadataDescendantsByName(
		                metadataList,"qname:similarity");
			        
			        String similarity = descendents.get(0).getContent();
 
			        ICDKManager manager = 
			            net.bioclipse.cdk.business.Activator
			                    .getDefault().getJavaCDKManager();
			        CDKMolecule molecule = 
			            new CDKMolecule(
			                    file.getChemSequence(0)
			                    .getChemModel(0)
			                    .getMoleculeSet()
			                    .getAtomContainer(0));
			        molecule.getAtomContainer().setProperty("similarity", similarity);
		        result.add(molecule);
	    	}
	        returner.completeReturn(result );
	        monitor.done();
	      }catch(Exception ex){
	          throw new BioclipseException(ex.getMessage(), ex);
	      }
	}
  
  public void submitSpecmol(CMLElement cmlelement,
                            String serverurl,
                            String username,
                            String password,
                            IReturner returner,
                            IProgressMonitor monitor) 
              throws BioclipseException {
      try {
        monitor.beginTask( "Submitting to NMRShiftDB", IProgressMonitor.UNKNOWN );
        if(!(cmlelement instanceof CMLCml))
            throw new BioclipseException("cmlelement must be instanceof CMLCml");
        CMLCml cmlcml = (CMLCml)cmlelement;
        
        Options opts = new Options( new String[0] );
        opts.setDefaultURL( serverurl
                            + "/services/NMRShiftDB" );
        SOAPBodyElement[] input = new SOAPBodyElement[1];
        DocumentBuilder builder;
        builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.newDocument();
        Element cdataElem;
        cdataElem = doc.createElementNS( opts.getURL(), "doSubmit" );
        Element reqElem;
        reqElem = doc.createElementNS( opts.getURL(), "username" );
        Node node;
        node = doc.createTextNode( username );
        reqElem.appendChild( node );
        Element reqElem2;
        reqElem2 = doc.createElementNS( opts.getURL(), "password" );
        Node node2;
        node2 = doc.createTextNode( password );
        reqElem2.appendChild( node2 );
        Document document =
            builder.parse( new ByteArrayInputStream( cmlcml.toXML()
           .getBytes( "UTF-8" ) ) );
        Node nodeimp = doc.importNode( document.getFirstChild(), true );
        cdataElem.appendChild( nodeimp );
        cdataElem.appendChild( reqElem );
        cdataElem.appendChild( reqElem2 );

    
        input[0] = new SOAPBodyElement( cdataElem );
        Service service = new Service();
        Call call = (Call) service.createCall();
        call.setTargetEndpointAddress( new URL( opts.getURL() ) );
    
        Vector elems = (Vector) call.invoke( input );
        SOAPBodyElement elem = null;
        elem = (SOAPBodyElement) elems.get( 0 );
        Element e = elem.getAsDOM();
        String ids = e.getFirstChild().getTextContent();
        StringTokenizer idsst = new StringTokenizer( ids );
        Elements spectra = cmlcml.getChildCMLElements( "spectrum" );
        for ( int k = 0; k < spectra.size(); k++ ) {
            CMLSpectrum spectrum = (CMLSpectrum) spectra.get( k );
            List<CMLMetadata> nmridmetadatas =
                    MetadataUtils
                            .getMetadataDescendantsByName(
                                                           MetadataUtils
                                                                   .getAllInOneMetadataList(
                                                                                             spectrum )
                                                                   .getMetadataDescendants(),
                                                           Bc_nmrshiftdbConstants.nmrid );
            if ( nmridmetadatas.size() > 0 ) {
                nmridmetadatas.get( 0 )
                        .setContent( idsst.nextToken() );
            } else {
                CMLMetadata metadata = new CMLMetadata();
                metadata.setName( Bc_nmrshiftdbConstants.nmrid );
                metadata.setContent( idsst.nextToken() );
                spectrum.getMetadataListElements().get( 0 )
                        .addMetadata( metadata );
            }
        }
        returner.completeReturn( e.getFirstChild().getTextContent() );
        monitor.done();
      }catch(Exception ex){
          throw new BioclipseException(ex.getMessage(), ex);
      }      
  }
}
