/*****************************************************************************
 * Copyright (c) 2008 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *****************************************************************************/

package spok;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Class for generating unique Identifiers
 * 
 * @author hel
 * @created 22. September 2004
 */
public class GenerateId {
	private static String spectrumID;

	/**
	 * Generates a unique identifier by generating a random number and getting
	 * its digest
	 * 
	 * @return the new SpectrumID
	 */
	public static String generateId() {
		try {
			// Initialize SecureRandom
			// This is a lengthy operation, to be done only upon
			// initialization of the application
			SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");

			// generate a random number
			String randomNum = new Integer(prng.nextInt()).toString();

			// get its digest
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			byte[] result = sha.digest(randomNum.getBytes());

			spectrumID = hexEncode(result);
			spectrumID = "sid_" + spectrumID;
		} catch (NoSuchAlgorithmException ex) {
			System.err.println(ex);
		}
		return spectrumID;
	}

	/**
	 * The byte[] returned by MessageDigest does not have a nice textual
	 * representation, so some form of encoding is usually performed.
	 * 
	 * This implementation follows the example of David Flanagan's book "Java In
	 * A Nutshell", and converts a byte array into a String of hex characters.
	 * 
	 * Another popular alternative is to use a "Base64" encoding.
	 * 
	 * @param aInput
	 *            byte array build by GenerateId().
	 * @return the resulting unique identifier string.
	 */
	private static String hexEncode(byte[] aInput) {
		StringBuffer result = new StringBuffer();
		char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		for (int idx = 0; idx < aInput.length; ++idx) {
			byte b = aInput[idx];
			result.append(digits[(b & 0xf0) >> 4]);
			result.append(digits[b & 0x0f]);
		}
		return result.toString();
	}
}
