package net.bioclipse.spectrum.contenttypes;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Nodes;
import nu.xom.XPathContext;

import org.eclipse.core.internal.content.ContentMessages;
import org.eclipse.core.internal.content.TextContentDescriber;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.osgi.util.NLS;
import org.xmlcml.cml.base.CMLUtil;
import org.xmlcml.cml.element.CMLSpectrum;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

@SuppressWarnings("restriction")
public class CmlSpectrumFileDescriber extends TextContentDescriber 
	implements IExecutableExtension {

	private static final String TYPE_TO_FIND = "type"; //$NON-NLS-1$
	private String type = null;
	
	private static final String COUNT_TO_FIND = "count"; //$NON-NLS-1$
	private String count = null;
	
	public int describe(InputStream contents, IContentDescription description)
			throws IOException {
		return analyse(new InputStreamReader(contents), description);
	}

	public int describe(Reader contents, IContentDescription description) throws IOException {
		return analyse(contents, description);
	}


	/**
	 * Scan the document, looking for certain key features.
	 * Right now, we look if there is a single molecule in the top hierarchy.
	 * Everything else (multiple spectra, spectra with molecule etc.) is not handled by this contenttype.
	 * 
	 * @param input A Reader that will provide the file contents.
	 * @param description The eclipse IContentDescription of the file.
	 * @return VALID or INVAID
	 * @throws IOException
	 */
	private int analyse(Reader input, IContentDescription description) throws IOException {
		
		int spectrumCount = 0;
		int spectrumTagDepth = 0;
		boolean checkedNamespace = false;
		int moleculeCount = 0;
		boolean hascmlroot=false;
		String spectrumType = null;

		try {
			XmlPullParserFactory factory = 
				XmlPullParserFactory.newInstance(
						System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
			factory.setNamespaceAware(true);
			factory.setValidating(false);

			XmlPullParser parser = factory.newPullParser();
			parser.setFeature("http://xmlpull.org/v1/doc/features.html#xml-roundtrip", true);
			parser.setInput(input);
			while (parser.next() != XmlPullParser.END_DOCUMENT) {
				if (parser.getEventType() == XmlPullParser.START_TAG) {
					String tagName = parser.getName();
				    
				    if (!checkedNamespace && tagName.equalsIgnoreCase("cml")) {
				        if (parser.getNamespace().equals(CMLUtil.CML_NS)) {
				            checkedNamespace = true;
				        } else {
				            System.err.println("namespace = " + parser.getNamespace() + " INVALID");
				            return INVALID;
				        }
				        hascmlroot=true;
				    }
				    
					if (tagName.equalsIgnoreCase("spectrum")) {
					    spectrumTagDepth += 1;
					    
					    // only count the top level of molecules, not nested ones.
					    if (spectrumTagDepth == 1) {
					    	//we look for a type in the spectrum tag which we later
					    	//use for 1d/2d distinction.
				            Pattern pattern = Pattern.compile("type=\".*\"");
				            CharSequence inputStr = parser.getText();
				            Matcher matcher = pattern.matcher(inputStr);
				            if(matcher.find()){
				            	String match = matcher.group();
				    		 	spectrumType = match.substring(6,match.length()-1);
				            }
					        spectrumCount++;
					    }
					}
					
					if (tagName.equalsIgnoreCase("molecule")) {
					    moleculeCount++;
					}

				} else if (parser.getEventType() == XmlPullParser.END_TAG) {
				    if (spectrumTagDepth > 0 && parser.getName().equalsIgnoreCase("spectrum")) {
				        spectrumTagDepth -= 1;
				    }
				}
			}
		} catch (XmlPullParserException x) {
			/*
			 * Commented out errors - there is a bug in the handling of jdx files where 
			 * bioclipse tries to hand them to the CmlSpectrumFileDescriber.
			 */
//			x.printStackTrace();
//			throw new IOException(x.getMessage());
			return INVALID;
		}

		if (spectrumCount == 1 && count.equals("single") && (moleculeCount==0 || !hascmlroot)) {
			if(type.equals("2D")){
				//this is a very crude hack to find 2d spectra
				if(spectrumType!=null && spectrumType.contains("2d"))
					return VALID;
				else
					return INVALID;
			}else{
				if(spectrumType!=null && spectrumType.contains("2d"))
					return INVALID;
				else
					return VALID;
			}
		}else if(spectrumCount > 1 && count.equals("multi") && (moleculeCount==0 || !hascmlroot)){
			return VALID;
		}
		return INVALID;
	}
	
	/**
	 * Store parameters
	 */
	@SuppressWarnings("unchecked")
	public void setInitializationData(final IConfigurationElement config, final String propertyName, final Object data) throws CoreException {
		if (data instanceof String)
			count = (String) data;
		else if (data instanceof Hashtable) {
			Hashtable parameters = (Hashtable) data;
 			type = (String) parameters.get(TYPE_TO_FIND);
 			count = (String) parameters.get(COUNT_TO_FIND);
		}
		if (count == null) {
			String message = NLS.bind(ContentMessages.content_badInitializationData, CmlSpectrumFileDescriber.class.getName());
			throw new CoreException(new Status(IStatus.ERROR, ContentMessages.OWNER_NAME, 0, message, null));
		}
	}
}
