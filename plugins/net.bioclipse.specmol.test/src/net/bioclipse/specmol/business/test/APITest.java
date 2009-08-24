package net.bioclipse.specmol.business.test;

import net.bioclipse.core.tests.AbstractManagerTest;
import net.bioclipse.managers.business.IBioclipseManager;
import net.bioclipse.specmol.business.ISpecmolManager;
import net.bioclipse.specmol.business.SpecmolManager;

public class APITest extends AbstractManagerTest {
 
   SpecmolManager specmolManager;
 
   @Override
   public IBioclipseManager getManager() {
     return specmolManager;
   }
 
   @Override
   public Class<? extends IBioclipseManager> getManagerInterface() {
     return ISpecmolManager.class;
   }
 
 }
