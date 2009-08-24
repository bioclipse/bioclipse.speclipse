package net.bioclipse.spectrum.business.test;

import net.bioclipse.core.tests.AbstractManagerTest;
import net.bioclipse.managers.business.IBioclipseManager;
import net.bioclipse.spectrum.business.ISpectrumManager;
import net.bioclipse.spectrum.business.SpectrumManager;

public class APITest extends AbstractManagerTest {
 
   SpectrumManager spectrumManager;
 
   @Override
   public IBioclipseManager getManager() {
     return spectrumManager;
   }
 
   @Override
   public Class<? extends IBioclipseManager> getManagerInterface() {
     return ISpectrumManager.class;
   }
 
 }
