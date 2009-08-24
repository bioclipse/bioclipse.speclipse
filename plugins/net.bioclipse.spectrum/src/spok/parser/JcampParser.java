/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package spok.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.jcamp.parser.JCAMPException;
import org.jcamp.parser.JCAMPReader;
import org.jcamp.spectrum.IRSpectrum;
import org.jcamp.spectrum.MassSpectrum;
import org.jcamp.spectrum.NMR2DSpectrum;
import org.jcamp.spectrum.NMRSpectrum;
import org.jcamp.spectrum.Spectrum;

/**
 * Parser class for jcampdx spectrum files
 * 
 * @author Tobias Helmus
 * @created 19. Dezember 2005
 */
public class JcampParser {

	private String spectrumtype;

	private File file;

	private Spectrum jcampSpectrum;
	
	private static final Logger logger = Logger.getLogger(JcampParser.class);

	/**
	 * Constructor for the JcampParser object
	 * 
	 * @param filename
	 *            Description of the Parameter
	 * @exception JCAMPException
	 *                Description of the Exception
	 */
	public JcampParser(File filename) throws JCAMPException, IOException{
		this.file = filename;
		String fileString = readFile(filename);
		jcampSpectrum = parseFile(fileString);
	}

	public JcampParser(String fileString) throws JCAMPException{
		jcampSpectrum = parseFile(fileString);
	}
	
	public JcampParser(InputStream inputStream) throws JCAMPException, IOException{
		StringBuffer buffer = new StringBuffer();
		int character;
		while ((character = inputStream.read()) != -1) {
			buffer.append((char) character);
		}
		inputStream.close();
		jcampSpectrum = parseFile(buffer.toString());
	}

	/**
	 * Reads the content of file (filename) into a string
	 * 
	 * @param filename
	 *            name and path of the file to load
	 * @return content of file in one string
	 */
	public String readFile(File filename) throws IOException{
		StringBuffer buffer = new StringBuffer();
		FileReader fileReader = new FileReader(filename);
		BufferedReader input = new BufferedReader(fileReader);
		int character;
		while ((character = input.read()) != -1) {
			buffer.append((char) character);
		}
		input.close();
		return buffer.toString();
	}

	/**
	 * Parses the input file into spectrum object
	 * 
	 * @param jcampString
	 *            String containing the content of jcamp file
	 * @return Spectrum object of the jcamp file
	 * @exception JCAMPException
	 *                Description of the Exception
	 */
	public Spectrum parseFile(String jcampString) throws JCAMPException{
		JCAMPReader jcamp = JCAMPReader.getInstance();
		jcampSpectrum = jcamp.createSpectrum(jcampString);
//		setSpectrumId(jcampSpectrum);
		jcamp = null;
		return jcampSpectrum;
	}

	/**
	 * The main program for the JcampParser class
	 * 
	 * @param args
	 *            The command line arguments
	 * @exception JCAMPException
	 *                Description of the Exception
	 */
	public static void main(String[] args) throws JCAMPException, IOException {
		File file = new File("data/aug07.dx");
		new JcampParser(file);
	}

	/**
	 * Sets the spectrumType attribute of the JcampParser object
	 * 
	 * @param jcamp
	 *            The new spectrumType value
	 */
	public void setSpectrumType(Spectrum jcamp) {
		if (jcamp instanceof NMRSpectrum) {
			setSpectrumType("NMR");
		} else if (jcamp instanceof MassSpectrum) {
			setSpectrumType("Mass");
		} else if (jcamp instanceof IRSpectrum) {
			setSpectrumType("IR");
		} else if (jcamp instanceof NMR2DSpectrum) {
			setSpectrumType("NMR2D");
		} else {
			logger.error("No supported spectrum type!");
		}
	}

	/**
	 * Sets the spectrumType attribute of the JcampParser object
	 * 
	 * @param type
	 *            The new spectrumType value
	 */
	public void setSpectrumType(String type) {
		spectrumtype = type;
	}

	/**
	 * Gets the filename attribute of the JcampParser object
	 * 
	 * @return The filename value
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Gets the spectrumType attribute of the JcampParser object
	 * 
	 * @return The spectrumType value
	 */
	public String getSpectrumType() {
		return spectrumtype;
	}

	/**
	 * Gets the spectrum attribute of the JcampParser object
	 * 
	 * @return The spectrum value
	 */
	public Spectrum getSpectrum() {
		return jcampSpectrum;
	}
}
