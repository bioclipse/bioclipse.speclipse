package net.bioclipse.nmrshiftdb.util;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;


public class NmrshiftdbUtils {
	
	private final static String NMRSHIFTDB_FOLDER_NAME = "NMRShiftDB Results";

	  /**
	   *  Removes the last character from a string buffer if it is a , or ;.
	   *
	   * @param  s  The string buffer to deal with.
	   * @return    Description of the Returned Value
	   */
	  public static StringBuffer removeLastComma(StringBuffer s) {
	    if (s.length() > 0 && (s.charAt(s.length() - 1) == ' ')) {
	      s.deleteCharAt(s.length() - 1);
	    }
	    if (s.length() > 0 && (s.charAt(s.length() - 1) == ';' || s.charAt(s.length() - 1) == ',')) {
	      s.deleteCharAt(s.length() - 1);
	    }
	    return (s);
	  }
	  
	  
		public static String replaceSpaces(String molecule2) {
			StringBuffer result = new StringBuffer();
			for (int i=0; i<molecule2.length(); i++) {
				if (Character.isWhitespace(molecule2.charAt(i))) {
					result.append("+");
				} else {
					result.append(molecule2.charAt(i));
				}
			}
			return result.toString();
		}
		
		public static IFolder createVirtualFolder() throws CoreException {
			final IProject root = net.bioclipse.core.Activator.getVirtualProject();
			// find a folder name which is not used yet
			int counter = 1;
			String folderName = NMRSHIFTDB_FOLDER_NAME + " " + counter;
			while (root.exists(new Path(folderName))) {
				counter++;
				folderName = NMRSHIFTDB_FOLDER_NAME + " " + counter;
			}
			root.getFolder(folderName).create(true,true, new NullProgressMonitor());
			return root.getFolder(folderName);
		}
}
