package net.bioclipse.nmrshiftdb.business;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.jobs.IReturner;
import net.bioclipse.managers.business.IBioclipseManager;
import net.bioclipse.nmrshiftdb.util.Bc_nmrshiftdbConstants;
import net.bioclipse.spectrum.editor.MetadataUtils;
import nu.xom.Elements;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.utils.Options;
import org.eclipse.core.runtime.IProgressMonitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLSpectrum;

public class NmrshiftdbManager implements IBioclipseManager {

	public String getManagerName() {
		return "nmrshiftdb";
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
