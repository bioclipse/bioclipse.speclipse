/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth
 *     Jonathan Alvarsson
 *     Stefan Kuhn
 *     
 ******************************************************************************/
package net.bioclipse.spectrum.business;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringBufferInputStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.ISpectrum;
import net.bioclipse.managers.business.IBioclipseManager;
import net.bioclipse.spectrum.domain.IJumboSpectrum;
import net.bioclipse.spectrum.domain.JumboSpectrum;
import net.bioclipse.spectrum.editor.SpectrumEditor;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.jcamp.parser.JCAMPException;
import org.jcamp.parser.JCAMPWriter;
import org.jcamp.spectrum.Spectrum;
import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.base.CMLSerializer;
import org.xmlcml.cml.base.CMLUtil;
import org.xmlcml.cml.element.CMLPeak;
import org.xmlcml.cml.element.CMLPeakList;
import org.xmlcml.cml.element.CMLSpectrum;
import org.xmlcml.cml.element.CMLSpectrumData;

import spok.parser.CMLToJcampSpectrumMapper;
import spok.parser.JcampParser;
import spok.parser.JcampToCMLSpectrumMapper;
import spok.utils.PeakPicker;
import spok.utils.SpectrumUtils;
import spok.utils.WCCTool;

/**
 * The manager class for CDK. Contains CDK related methods.
 * 
 * @author Stefan Kuhn
 *
 */
public class SpectrumManager implements IBioclipseManager {
	
    private static final Logger logger 
        = Logger.getLogger(SpectrumManager.class);
    
    public String getManagerName() {
        return "spectrum";
    }

	public IJumboSpectrum create(ISpectrum s) throws BioclipseException {
		return new JumboSpectrum(parseCML(new StringBufferInputStream(s.getCML())));
	}

	public JumboSpectrum loadSpectrum(String path) throws IOException,
			BioclipseException, CoreException {
		return loadSpectrum(ResourcePathTransformer.getInstance().transform( path ).getContents(),ResourcePathTransformer.getInstance().transform( path ).getFileExtension());
	}

	public JumboSpectrum loadSpectrum(IFile file, IProgressMonitor monitor)
			throws IOException, BioclipseException, CoreException {
        if ( monitor == null ) {
            monitor = new NullProgressMonitor();
        }
        int ticks = 10000;
        monitor.beginTask( "Reading file", ticks );
        JumboSpectrum result;
        try{
        	result=loadSpectrum( file.getContents(), file.getFileExtension() );
        }finally {
            monitor.done();
        }
        return result;
	}

  public JumboSpectrum loadSpectrum(InputStream instream, String fileExtension) throws IOException,
			BioclipseException, CoreException{
        
      String filetype = detectFileType( fileExtension );
    	if(filetype.equals( SpectrumEditor.JCAMP_TYPE )){
        return new JumboSpectrum(parseJDX(instream));
    	}else if(filetype.equals( SpectrumEditor.CML_TYPE )){
    		return new JumboSpectrum(parseCML(instream));
    	}else{
    		throw new BioclipseException("Unknown file extension");
    	}
	}

  private static CMLSpectrum parseCML( InputStream instream ) {

      CMLBuilder builder = new CMLBuilder( false );
      CMLElement cmlElement = null;
      Element element = null;
      StringBuffer buffer = new StringBuffer();
      try {
          int character;
          while ( (character = instream.read()) != -1 ) {
              buffer.append( (char) character );
          }
          instream.close();
          cmlElement = (CMLElement) builder.parseString( buffer.toString() );
      } catch ( IOException e ) {
          StringWriter strWr = new StringWriter();
          PrintWriter prWr = new PrintWriter( strWr );
          e.printStackTrace( prWr );
          logger.error( strWr.toString() );
      } catch ( ValidityException e ) {
          StringWriter strWr = new StringWriter();
          PrintWriter prWr = new PrintWriter( strWr );
          e.printStackTrace( prWr );
          logger.error( strWr.toString() );
      } catch ( ParsingException e ) {
          StringWriter strWr = new StringWriter();
          PrintWriter prWr = new PrintWriter( strWr );
          e.printStackTrace( prWr );
          logger.error( strWr.toString() );
      }
      // dirty! but for some reason if there is no cml namespace,
      // parsing fails with a ClassCastException
      // so i parse string into an nu.xom.element, set the namespace to
      // cml namespace for
      // all subelements an reParse the toXML() of the nu.xom.element into
      // cml - not elegant, but
      // works for the moment
      catch ( ClassCastException ex ) {
          try {
              element = (Element) builder.parseString( buffer.toString() );
              SpectrumUtils.namespaceThemAll( element
                      .getChildElements() );
              element.setNamespaceURI( CMLUtil.CML_NS );
              cmlElement = (CMLElement) builder.parseString( element.toXML() );
          } catch ( ValidityException e ) {
              StringWriter strWr = new StringWriter();
              PrintWriter prWr = new PrintWriter( strWr );
              e.printStackTrace( prWr );
              logger.error( strWr.toString() );
          } catch ( ParsingException e ) {
              StringWriter strWr = new StringWriter();
              PrintWriter prWr = new PrintWriter( strWr );
              e.printStackTrace( prWr );
              logger.error( strWr.toString() );
          } catch ( IOException e ) {
              StringWriter strWr = new StringWriter();
              PrintWriter prWr = new PrintWriter( strWr );
              e.printStackTrace( prWr );
              logger.error( strWr.toString() );
          }
      }

      if ( cmlElement == null ) {
          logger.error( "Errors trying to parse a JCamp spectrum." );
          return null;
      }

      List<CMLElement> spectrumList =
              cmlElement.getDescendants( "spectrum", null, true );
      // spectrumList = cmlElement.getDescendants("spectrum");
      if ( spectrumList != null && spectrumList.size() != 0
           || cmlElement.getLocalName().compareTo( "spectrum" ) == 0 ) {

          // Only one spectrum
          if ( cmlElement.getLocalName().compareTo( "spectrum" ) == 0 ) {
              return (CMLSpectrum) cmlElement;
          } else {
              // TODO the describer needs to handle these as well
              // Only one spectrum, but in a spectrumList
              if ( spectrumList.size() == 1 ) {
                  return (CMLSpectrum) spectrumList.get( 0 );
              }
          }
      }
      return null;
  }

  private static CMLSpectrum parseJDX( InputStream inputStream ) {

      Spectrum jdxSpectrum = null;

      try {
          jdxSpectrum = new JcampParser( inputStream ).getSpectrum();
          CMLSpectrum spectrum =
                  JcampToCMLSpectrumMapper
                          .mapJcampToCMLSpectrum( jdxSpectrum );
          if ( spectrum != null )
              return spectrum;

      } catch ( Exception e ) {
          logger.error( "Errors trying to parse a JCamp spectrum." );
          e.printStackTrace();
          return null;
      }

      return null;
  }

  public void saveSpectrum(IJumboSpectrum spectrum, IFile target,
			String filetype) throws BioclipseException, CoreException {
        IProgressMonitor monitor = new NullProgressMonitor();
    	try{
	        int ticks = 10000;
	        monitor.beginTask( "Writing file", ticks );
	    	String towrite;
			if (filetype.equals(SpectrumEditor.JCAMP_TYPE)) {
				Spectrum jdxspectrum = CMLToJcampSpectrumMapper
						.mapCMLSpectrumToJcamp(spectrum.getJumboObject());
				JCAMPWriter jcamp = JCAMPWriter.getInstance();
				String jcampString;
				try {
					jcampString = jcamp.toJCAMP(jdxspectrum);
				} catch (JCAMPException e) {
					throw new BioclipseException(e.getMessage());
				}
				towrite = jcampString;
			} else if (filetype.equals(SpectrumEditor.CML_TYPE)) {
				CMLSerializer ser = new CMLSerializer();
				ser.setIndent(2);
				String xml = ser.getXML(spectrum.getJumboObject());
				towrite = xml;
	    	} else {
	    		throw new BioclipseException("Filetype "+filetype+" not supported!");
	    	}
	    	if(target.exists()){
	        	 target.setContents(new StringBufferInputStream(towrite), false, true, monitor);
	    	} else {
		    	target.create(new StringBufferInputStream(towrite), false, monitor);
	    	}
	        monitor.worked(ticks);
		}
		finally {
			monitor.done();
		}
	}

	public IJumboSpectrum fromCml(String cml) throws BioclipseException,
			IOException {
		return new JumboSpectrum(parseCML(new StringBufferInputStream(cml)));
	}
	
	public IJumboSpectrum pickPeaks(IJumboSpectrum spectruminput) throws BioclipseException{
		CMLSpectrumData spectrumData = SpectrumUtils.getSpectrumData(spectruminput.getJumboObject());
		if(spectrumData==null)
			throw new BioclipseException("No continuous data in this spectrum");
		PeakPicker picker = new PeakPicker(spectrumData);
		CMLPeakList peaks = picker.getPeakArray();
		spectruminput.getJumboObject().addPeakList(peaks);
		return spectruminput;
	}

	public void saveSpectrum(IJumboSpectrum spectrum, String filename,
			String filetype) throws BioclipseException, CoreException {
		if(filename.indexOf("."+filetype)==-1)
			filename=filename+"."+filetype;
		IFile target = ResourcePathTransformer.getInstance().transform(filename);
		//IFile target=ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(filename));
		if(target.exists()){
			throw new BioclipseException("File already exists!");
		}
		this.saveSpectrum(spectrum, target, filetype);
	}

  public double calculateSimilarityWCC( double[] positions1, double[] positions2,
                            double width ) {

        // one carbon per peak
        double[] intensities1 = new double[positions1.length];
        for ( int i = 0; i < intensities1.length; i++ )
            intensities1[i] = 1.0;
        double[] intensities2 = new double[positions2.length];
        for ( int i = 0; i < intensities2.length; i++ )
            intensities2[i] = 1.0;

        return WCCTool.wcc( positions1, intensities1, positions2, intensities2,
                            width );
    }

  public double calculateSimilarityWCC( double[] positions1, double[] intensities1,
                            double[] positions2, double[] intensities2,
                            double width ) {

        return WCCTool.wcc( positions1, intensities1, positions2, intensities2,
                            width );
    }

  public double calculateSimilarityWCC( ISpectrum spectrum1, ISpectrum spectrum2,
                                        double width ) throws BioclipseException {
      CMLSpectrum cmlspectrum1;
      if(spectrum1 instanceof IJumboSpectrum)
          cmlspectrum1 = ((IJumboSpectrum)spectrum1).getJumboObject();
      else
          cmlspectrum1 = create( spectrum1 ).getJumboObject();
      CMLSpectrum cmlspectrum2;
      if(spectrum2 instanceof IJumboSpectrum)
          cmlspectrum2 = ((IJumboSpectrum)spectrum2).getJumboObject();
      else
          cmlspectrum2 = create( spectrum2 ).getJumboObject();
      if(cmlspectrum1.getPeakListElements().size()==0 || cmlspectrum2.getPeakListElements().size()==0)
          throw new BioclipseException("Spectra do not contain peak lists");
      List<CMLElement> peaks1 = SpectrumUtils.getPeakElements(cmlspectrum1);
      Iterator<CMLElement> it = peaks1.iterator();
      double[] shifts1 = new double[peaks1.size()];
      double[] intensities1 = new double[peaks1.size()];
      int i = 0;
      while (it.hasNext()) {
        CMLPeak peak = (CMLPeak) it.next();
        shifts1[i] = peak.getXValue();
        if(peak.getYValueAttribute()!=null)
            intensities1[i]=peak.getYValue();
        else
            intensities1[i]=1;
        i += 1;
      }
      List<CMLElement> peaks2 = SpectrumUtils.getPeakElements(cmlspectrum2);
      it = peaks2.iterator();
      double[] shifts2 = new double[peaks2.size()];
      double[] intensities2 = new double[peaks2.size()];
      i = 0;
      while (it.hasNext()) {
        CMLPeak peak = (CMLPeak) it.next();
        shifts2[i] = peak.getXValue();
        if(peak.getYValueAttribute()!=null)
            intensities2[i]=peak.getYValue();
        else
            intensities2[i]=1;
        i += 1;
      }
      return calculateSimilarityWCC( shifts1, intensities1, shifts2, intensities2, width );
  }

  public String detectFileType( String extension ) throws BioclipseException {
      String filetype = extension;
      if(filetype==null)
        filetype="";
      if("jx,dx,JX,DX,jdx".indexOf(filetype)>-1){
        return SpectrumEditor.JCAMP_TYPE;
      }else if("cml,xml".indexOf(filetype)>-1){
        return SpectrumEditor.CML_TYPE;
      }else{
        throw new BioclipseException("Unknown file extension");
      }
  }
  
  public IJumboSpectrum createEmpty() throws BioclipseException{
	  IJumboSpectrum spectrum = new JumboSpectrum();
	  spectrum.getJumboObject().addPeakList(new CMLPeakList());
	  return spectrum;
  }
}
