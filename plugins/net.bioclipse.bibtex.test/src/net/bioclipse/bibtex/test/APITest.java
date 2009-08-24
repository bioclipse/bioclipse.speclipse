package net.bioclipse.bibtex.test;

import net.bioclipse.bibtex.business.BibtexManager;
import net.bioclipse.bibtex.business.IBibtexManager;
import net.bioclipse.core.tests.AbstractManagerTest;
import net.bioclipse.managers.business.IBioclipseManager;


public class APITest extends AbstractManagerTest {
 
   BibtexManager bibtexManager;
 
   @Override
   public IBioclipseManager getManager() {
     return bibtexManager;
   }
 
   @Override
   public Class<? extends IBioclipseManager> getManagerInterface() {
     return IBibtexManager.class;
   }
 
 }
