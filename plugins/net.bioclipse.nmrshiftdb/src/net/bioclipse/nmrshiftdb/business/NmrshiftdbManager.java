package net.bioclipse.nmrshiftdb.business;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
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
import net.bioclipse.core.domain.ISpecmol;
import net.bioclipse.core.domain.ISpectrum;
import net.bioclipse.jobs.IReturner;
import net.bioclipse.managers.business.IBioclipseManager;
import net.bioclipse.nmrshiftdb.util.Bc_nmrshiftdbConstants;
import net.bioclipse.nmrshiftdb.util.NmrshiftdbUtils;
import net.bioclipse.specmol.domain.JumboSpecmol;
import net.bioclipse.spectrum.editor.MetadataUtils;
import net.xomtools.CMLExtractor;
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
import org.w3c.dom.Text;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;
import org.xmlcml.cml.element.CMLSpectrum;

import spok.utils.SpectrumUtils;

public class NmrshiftdbManager implements IBioclipseManager {

  	public String getManagerName() {
		return "nmrshiftdb";
	}


	public void generalSearch(String searchstring, String searchtype, String searchfield,
              String serverurl,
              IReturner returner,
              IProgressMonitor monitor) 
	  			throws BioclipseException {
        monitor.beginTask( "Searching in NMRShiftDB", IProgressMonitor.UNKNOWN );
        try{
			Options opts = new Options(new String[0]);
		    opts.setDefaultURL(serverurl+"/services/NMRShiftDB");
		    Service  service = new Service();
		    Call     call    = (Call) service.createCall();
		    call.setOperationName("doSearch");
		    call.setTargetEndpointAddress( new URL(opts.getURL()) );
		    DocumentBuilder builder;
		    builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		    SOAPBodyElement[] input = new SOAPBodyElement[1];
		    Document doc = builder.newDocument();
		    Element cdataElem;
		    cdataElem = doc.createElementNS("http://www.nmrshiftdb.org/ws/NMRShiftDB/", "doSearch");
		    Element reqElem;
		    reqElem = doc.createElementNS("http://www.nmrshiftdb.org/ws/NMRShiftDB/", "searchstring");
		    Node node;
		    node = doc.createTextNode(NmrshiftdbUtils.replaceSpaces(searchstring));
		    reqElem.appendChild(node);
		    Element reqElem2;
		    reqElem2 = doc.createElementNS("http://www.nmrshiftdb.org/ws/NMRShiftDB/", "searchtype");
		    Node node2;
		    node2 = doc.createTextNode(NmrshiftdbUtils.replaceSpaces(searchtype));
		    reqElem2.appendChild(node2);
		    Element reqElem3;
		    reqElem3 = doc.createElementNS("http://www.nmrshiftdb.org/ws/NMRShiftDB/", "searchfield");
		    Node node3;
		    node3 = doc.createTextNode(NmrshiftdbUtils.replaceSpaces(searchfield));
		    reqElem3.appendChild(node3);
		    cdataElem.appendChild(reqElem);
		    cdataElem.appendChild(reqElem2);
		    cdataElem.appendChild(reqElem3);
		    input[0] = new SOAPBodyElement(cdataElem);
		    Vector elems = (Vector) call.invoke( input );
		    SOAPBodyElement elem = (SOAPBodyElement) elems.get(0);
		    Element e    = elem.getAsDOM();
	    	CMLBuilder cmlbuilder = new CMLBuilder();
	    	CMLElement cmlElement = (CMLElement) cmlbuilder.parseString(XMLUtils.ElementToString(e));
	    	List<ISpecmol> result = new ArrayList<ISpecmol>();
	    	for(int i=0;i<cmlElement.getChildCount();i++){
	    		result.add(new JumboSpecmol((CMLCml)cmlElement.getChildCMLElements().get(i)));
	    	}
	        returner.completeReturn(result );
	        monitor.done();
	    }catch(Exception ex){
	          throw new BioclipseException(ex.getMessage(), ex);
	    }
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

  public void predictSpectrum(IMolecule molecule, String type, boolean useCalculated, boolean local,
		    String serverurl,
            IReturner returner,
            IProgressMonitor monitor) 
	  			throws BioclipseException {
      monitor.beginTask( "Performing Prediction", IProgressMonitor.UNKNOWN );
      try{
    	  CMLSpectrum spectrum;
    	  org.openscience.cdk.interfaces.IMolecule cdkmol=DefaultChemObjectBuilder.getInstance().newMolecule(net.bioclipse.cdk.business.Activator.getDefault().getJavaCDKManager().asCDKMolecule(molecule).getAtomContainer());
		if(local){
    		  net.bioclipse.nmrshiftdb.util.PredictionTool predtool= new net.bioclipse.nmrshiftdb.util.PredictionTool();
	  	  	  	spectrum=new CMLSpectrum();
		  	  	spectrum.setType("NMR");
		  	  	spectrum.setNamespaceURI(CMLExtractor.CML_NAMESPACE);
		  	  	CMLPeakList peakList=new CMLPeakList();
		  	  	spectrum.addPeakList(peakList);
    		  for(int i=0;i<cdkmol.getAtomCount();i++){
    			  //TODO not good
  	    		if(type.indexOf(cdkmol.getAtom(i).getSymbol())>-1){
	    			try{
	    				StringBuffer sb = new StringBuffer();
	    				double[] result=predtool.generalPredict(cdkmol, cdkmol.getAtom(i),true, true, -1, -1, sb, false, true, null, 6, false, sb, 6, true);
		    			//double[] result=predtool.predict(cdkmol, cdkmol.getAtom(i));
	    				System.err.println(sb.toString());
		    			CMLPeak peak=new CMLPeak();
		    			peak.setXMin(result[0]);
		    			peak.setXValue(result[1]);
		    			peak.setXMax(result[2]);
		    			peak.setXUnits("units:ppm");
		    			peak.setXWidth(result[3]);
		    			peak.setYMin(result[4]);
		    			peak.setYMax(result[5]);
		    			peak.setId("p"+i);
		    			String[] atomrefs=new String[]{cdkmol.getAtom(i).getID()};
		    			peak.setAtomRefs(atomrefs);
		    			peakList.addPeak(peak);
	    			}catch(Exception ex){
		    			CMLPeak peak=new CMLPeak();
		    			peak.setConvention("Prediction impossible");
		    			peak.setId("p"+i);
		    			String[] atomrefs=new String[]{cdkmol.getAtom(i).getID()};
		    			peak.setAtomRefs(atomrefs);
		    			peakList.addPeak(peak);
	    			}
	    		}
    		  }
    	  }else{
  			Options opts = new Options(new String[0]);
		    opts.setDefaultURL(serverurl+"/services/NMRShiftDB");
		    Service  service = new Service();
		    Call     call    = (Call) service.createCall();
		    call.setOperationName("doSearch");
		    call.setTargetEndpointAddress( new URL(opts.getURL()) );
		    DocumentBuilder builder;
		    builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		    Document document = builder.parse(new ByteArrayInputStream(molecule.toCML().getBytes()));
		    Document doc = builder.newDocument();
			Element cdataElem = doc.createElementNS(opts.getURL(), "doPrediction");
			Element reqElem = doc.createElementNS(opts.getURL(), "spectrumTypeName");
			Element calculatedElem = doc.createElementNS(opts.getURL(), "useCalculated");
		    Text node = doc.createTextNode(type);
		    reqElem.appendChild(node);
		    Node calculatednode = doc.createTextNode(useCalculated ? "true" : "false");
		    calculatedElem.appendChild(calculatednode);
		    Node nodeimp=doc.importNode(document.getChildNodes().item(0),true);
		    cdataElem.appendChild(nodeimp);
		    cdataElem.appendChild(reqElem);
		    cdataElem.appendChild(calculatedElem);
		    SOAPBodyElement[] input=new SOAPBodyElement[1];
		    input[0] = new SOAPBodyElement(cdataElem);
	        call.setTargetEndpointAddress( new URL( opts.getURL() ) );
	        Vector elems = (Vector) call.invoke( input );
	        SOAPBodyElement elem = null;
	        elem = (SOAPBodyElement) elems.get( 0 );
	    	Element e    = elem.getAsDOM();
	    	CMLBuilder cmlbuilder = new CMLBuilder();
	    	CMLElement cmlElement = (CMLElement) cmlbuilder.parseString(XMLUtils.ElementToString(e));
			spectrum=(CMLSpectrum)cmlElement;
    	  }
		StringBuffer errors = new StringBuffer();
		List<CMLElement> peaks = SpectrumUtils.getPeakElements(spectrum);
		Iterator<CMLElement> it = peaks.iterator();
		CMLPeakList newPeaks=new CMLPeakList();
		while (it.hasNext()) {
			CMLPeak peak = (CMLPeak) it.next();
			if(peak.getConvention()!=null && peak.getConvention().toString().equals( "Prediction impossible" )){
			    errors.append(peak.getAtomRefs()[0]+";");
			}else{
  			if(peak.getAttribute("yValue")==null)
  				peak.setYValue(1);
  			if(Double.isNaN(peak.getYValue())){
  				peak.setYValue(1);
  			}
  			newPeaks.addPeak( peak );
			}
		}
		spectrum.removeChild( spectrum.getPeakListElements().get( 0 ));
		spectrum.addPeakList( newPeaks );
		  returner.completeReturn(net.bioclipse.spectrum.Activator.getDefault().getJavaSpectrumManager().fromCml(spectrum.toXML()));
	        monitor.done();
	    }catch(Exception ex){
	          throw new BioclipseException(ex.getMessage(), ex);
	    }
	}
  
  public String getSpectrumTypes(String serverurl) throws BioclipseException{
	  try{
	    Options opts = new Options(new String[0]);
	    opts.setDefaultURL(serverurl+"/services/NMRShiftDB");
	    Service  service = new Service();
	    Call     call    = (Call) service.createCall();
	    call.setOperationName("getSpectrumTypes");
	    call.setTargetEndpointAddress( new URL(opts.getURL()) );
	    DocumentBuilder builder;
	    builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    SOAPBodyElement[] input = new SOAPBodyElement[1];
	    Document doc = builder.newDocument();
	    Element cdataElem;
	    cdataElem = doc.createElementNS(opts.getURL(), "getSpectrumTypes");
	    input[0] = new SOAPBodyElement(cdataElem);
        
	    Vector          elems = (Vector) call.invoke( input );
	    SOAPBodyElement elem  = null ;
	    Element         e     = null ;
	    elem = (SOAPBodyElement) elems.get(0);
	    e    = elem.getAsDOM();
	    return e.getFirstChild().getTextContent();
	  }catch(Exception ex){
		  throw new BioclipseException(ex.getMessage());
	  }
  }
}
