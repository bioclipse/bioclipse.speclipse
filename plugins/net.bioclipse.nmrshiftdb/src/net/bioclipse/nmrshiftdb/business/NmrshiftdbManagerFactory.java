package net.bioclipse.nmrshiftdb.business;


import net.bioclipse.nmrshiftdb.Activator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;

public class NmrshiftdbManagerFactory implements IExecutableExtension,
		IExecutableExtensionFactory {

	private Object nmrshiftdbManager;

	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {

		nmrshiftdbManager = Activator.getDefault().getJavaScriptNmrshiftdbManager();
		if (nmrshiftdbManager == null) {
			nmrshiftdbManager = new Object();
		}
	}

	public Object create() throws CoreException {
		return nmrshiftdbManager;
	}
}
