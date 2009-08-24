package net.bioclipse.nmrshiftdb.wizards;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.jobs.BioclipseUIJob;
import net.bioclipse.nmrshiftdb.util.Bc_nmrshiftdbConstants;
import net.bioclipse.specmol.Activator;
import net.bioclipse.specmol.domain.JumboSpecmol;
import net.bioclipse.specmol.editor.SpecMolEditor;
import net.bioclipse.spectrum.editor.MetadataUtils;
import nu.xom.Attribute;
import nu.xom.Elements;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.utils.Options;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlcml.cml.element.CMLBond;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLConditionList;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMetadataList;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLName;
import org.xmlcml.cml.element.CMLScalar;
import org.xmlcml.cml.element.CMLSpectrum;

import spok.utils.SpectrumUtils;

/**
 * This wizard submits an assigned molecule resource to NMRShiftDB.
 */

public class SubmitWizard extends Wizard {

    private PasswordWizardPage  passwordPage;
    private MessagePage         messagePage;
    private IViewPart           view                      = null;
    CMLCml                      cmlcml                    = null;
    IFile                       biores                    = null;
    public final static String  REMEMBER_NMRSHIFTDB_PW    =
                                                                  "REMEMBER_NMRSHIFTDB_PW";
    public final static String  REMEMBER_NMRSHIFTDB_USER  =
                                                                  "REMEMBER_NMRSHIFTDB_USER";
    public final static String  REMEMBER_NMRSHIFTDB_VALUE =
                                                                  "REMEMBER_NMRSHIFTDB_VALUES";
    private static final Logger logger                    =
                                                                  Logger
                                                                          .getLogger( SubmitWizard.class );

    /**
     * Constructor for JCPWizard.
     */
    public SubmitWizard(IViewPart view) {

        super();
        setWindowTitle( "Submit Assigend Spectrum to NMRShiftDB" );
        setNeedsProgressMonitor( true );
        this.view = view;
    }

    /**
     * Adding the page to the wizard.
     */

    public void addPages() {

        ISelection sel =
                PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getSelectionService().getSelection();
        if ( sel instanceof IStructuredSelection ) {
            Object element = ((IStructuredSelection) sel).getFirstElement();
            if ( element instanceof IFile ) {
                biores = (IFile) element;
                try {
                    cmlcml =
                            Activator.getDefault().getJavaSpecmolManager()
                                    .loadSpecmol( biores ).getJumboObject();
                    messagePage = new MessagePage( cmlcml );
                    addPage( messagePage );
                    passwordPage = new PasswordWizardPage( this );
                    addPage( passwordPage );
                } catch ( Exception e ) {
                    LogUtils.handleException( e, logger );
                }
            }
        }
    }

    /**
     * This method is called when 'Finish' button is pressed in the wizard. We
     * will create an operation and run it using wizard as execution context.
     */
    public boolean performFinish() {

        try {
            // we get available spectrum types for checking
            Options opts2 = new Options( new String[0] );
            opts2.setDefaultURL( getPasswordPage().getSelectedServer()
                                 + "/services/NMRShiftDB" );
            Service service2 = new Service();
            Call call2 = (Call) service2.createCall();
            call2.setOperationName( "getSpectrumTypes" );
            call2.setTargetEndpointAddress( new URL( opts2.getURL() ) );
            DocumentBuilder builder2;
            builder2 =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder();
            SOAPBodyElement[] input2 = new SOAPBodyElement[1];
            Document doc2 = builder2.newDocument();
            Element cdataElem2;
            cdataElem2 =
                    doc2.createElementNS( opts2.getURL(), "getSpectrumTypes" );
            input2[0] = new SOAPBodyElement( cdataElem2 );
            Vector elems2 = (Vector) call2.invoke( input2 );
            SOAPBodyElement elem2 = null;
            Element e2 = null;
            elem2 = (SOAPBodyElement) elems2.get( 0 );
            e2 = elem2.getAsDOM();
            String spectrumTypes = e2.getFirstChild().getTextContent();
            if ( this.getPasswordPage().getSelection().getSelection() ) {
                this.setPwPreference( this.getPasswordPage().getPassword() );
                this.setUserPreference( this.getPasswordPage().getUsername() );
                this.setNmrshiftdbPreference( true );
            } else {
                this.setPwPreference( "" );
                this.setUserPreference( "" );
                this.setNmrshiftdbPreference( false );
            }
            // handle molecule
            StringTokenizer namest =
                    new StringTokenizer( messagePage.text1mol.getText().trim(),
                                         ";" );
            CMLMolecule molecule =
                    (CMLMolecule) cmlcml.getChildCMLElements( "molecule" )
                            .get( 0 );
            for ( int i = 0; i < molecule.getNameElements().size(); i++ ) {
                molecule.removeChild( molecule.getNameElements().get( 0 ) );
            }
            while ( namest.hasMoreTokens() ) {
                CMLName name = new CMLName();
                name.setXMLContent( namest.nextToken() );
                molecule.addName( name );
            }
            if ( !messagePage.text2mol.getText().trim().equals( "" ) ) {
                CMLName name = null;
                Iterator<CMLName> names = molecule.getNameElements().iterator();
                while ( names.hasNext() ) {
                    CMLName namelocal = names.next();
                    if ( namelocal.getConvention()!=null && namelocal.getConvention().equals( "CAS" ) )
                        name = namelocal;
                }
                if ( name == null ) {
                    name = new CMLName();
                    Attribute attribute = new Attribute( "convention", "CAS" );
                    name.addAttribute( attribute );
                    molecule.addName( name );
                }
                name.setXMLContent( messagePage.text2mol.getText().trim() );
            }
            CMLMetadataList mlist = null;
            if ( molecule.getChildCMLElements( "metadataList" ).size() == 0 ) {
                mlist = new CMLMetadataList();
                molecule.appendChild( mlist );
            } else {
                mlist =
                        (CMLMetadataList) molecule
                                .getChildCMLElements( "metadataList" ).get( 0 );
            }
            removeAllChilds( mlist, "nmr:keyword" );
            StringTokenizer keywords =
                    new StringTokenizer( messagePage.text3mol.getText(), ";" );
            while ( keywords.hasMoreTokens() ) {
                CMLMetadata metadata = new CMLMetadata();
                metadata.setName( "nmr:keyword" );
                metadata.setContent( keywords.nextToken() );
                mlist.addMetadata( metadata );
            }
            removeAllChilds( mlist, "nmr:link" );
            StringTokenizer links =
                    new StringTokenizer( messagePage.text4mol.getText(), "\r\n" );
            while ( links.hasMoreTokens() ) {
                CMLMetadata metadata = new CMLMetadata();
                metadata.setName( "nmr:link" );
                metadata.setContent( links.nextToken() );
                mlist.addMetadata( metadata );
            }
            CMLReader cmlreader =
                    new CMLReader( new ByteArrayInputStream( molecule.toXML()
                            .getBytes() ) );
            IMolecule cdkmol =
                    ((ChemFile) cmlreader.read( new ChemFile() ))
                            .getChemSequence( 0 ).getChemModel( 0 )
                            .getMoleculeSet().getMolecule( 0 );
            Iterable<IBond> bonds = cdkmol.bonds();
            SmilesGenerator sg = new SmilesGenerator();
            int l = 0;
            for ( IBond cdkBond : bonds ) {
                if ( sg.isValidDoubleBondConfiguration( cdkmol, cdkBond ) ) {
                    CMLBond bond =
                            molecule
                                    .getBond( molecule.getAtomById( cdkBond
                                            .getAtom( 0 ).getID() ), molecule
                                            .getAtomById( cdkBond.getAtom( 1 )
                                                    .getID() ) );
                    if ( messagePage.doublebondconfigurations.get( l )
                            .getSelection() ) {
                        if ( bond
                                .getFirstChildElement( Bc_nmrshiftdbConstants.doublebondconfiguration ) == null ) {
                            nu.xom.Element element =
                                    new nu.xom.Element(
                                                        Bc_nmrshiftdbConstants.doublebondconfiguration,
                                                        "http://www.nmrshiftdb.org/" );
                            bond.appendChild( element );
                        }
                    } else {
                        nu.xom.Element element =
                                bond
                                        .getFirstChildElement( Bc_nmrshiftdbConstants.doublebondconfiguration );
                        if ( element != null )
                            bond.removeChild( element );
                    }
                    l++;
                }
            }
            // handle spectra
            final List<CMLSpectrum> removedSpectra = new Vector<CMLSpectrum>();
            Elements spectra = cmlcml.getChildCMLElements( "spectrum" );
            StringBuffer problems = new StringBuffer();
            for ( int k = 0; k < spectra.size(); k++ ) {
                CMLSpectrum spectrum = (CMLSpectrum) spectra.get( k );
                if ( messagePage.getSelections().get( k ).getSelection() ) {
                    // Check if the spectrum type is ok
                    if ( messagePage.getTexts5().get( k ).getText().trim()
                            .equals( "" )
                         || spectrumTypes.indexOf( messagePage.getTexts5()
                                 .get( k ).getText().trim() ) == -1 ) {
                        problems
                                .append( "The spectrum "
                                         + spectrum.getId()
                                         + " has an observed nucleus which is not allowed on the server. Possible values are: "
                                         + spectrumTypes
                                         + ". If you want a new nucleus, contact the email give on www.nmrshiftdb.org!\r\n" );
                    } else if ( spectrum.getType() == null
                                || (!spectrum.getType()
                                        .equals( SpectrumUtils.NMRSPECTRUMTYPE ) && !spectrum
                                        .getType().equals( "nmr" )) ) {
                        problems
                                .append( "The spectrum "
                                         + spectrum.getId()
                                         + " is not of type 'NMR'. All spectra you want to submit must be "
                                         + SpectrumUtils.NMRSPECTRUMTYPE
                                         + ". Change the type if you are sure it is an NMR spectrum or exclude the spectrum from submit!" );
                    } else {
                        if ( spectrum.getConditionListElements().size() > 0 ) {
                            Elements els =
                                    spectrum.getConditionListElements().get( 0 )
                                            .getChildCMLElements( "scalar" );
                            boolean found = false;
                            for ( int i = 0; i < els.size(); i++ ) {
                                if ( els.get( i ).getAttribute( "dictRef" ) != null
                                     && els
                                             .get( i )
                                             .getAttribute( "dictRef" )
                                             .getValue()
                                             .equals(
                                                      Bc_nmrshiftdbConstants.frequency ) ) {
                                    ((CMLScalar) els.get( i ))
                                            .setValue( messagePage.getTexts1()
                                                    .get( k ).getText().trim() );
                                    found = true;
                                    break;
                                }
                            }
                            if ( !found ) {
                                CMLScalar scalar = new CMLScalar();
                                scalar
                                        .setDictRef( Bc_nmrshiftdbConstants.frequency );
                                scalar.setValue( messagePage.getTexts1()
                                        .get( k ).getText().trim() );
                                spectrum.getConditionListElements().get( 0 )
                                        .addScalar( scalar );
                            }
                        } else {
                            CMLConditionList condlist = new CMLConditionList();
                            CMLScalar scalar = new CMLScalar();
                            scalar
                                    .setDictRef( Bc_nmrshiftdbConstants.frequency );
                            scalar.setValue( messagePage.getTexts1().get( k )
                                    .getText().trim() );
                            condlist.addScalar( scalar );
                            spectrum.addConditionList( condlist );
                        }
                        Elements els =
                                spectrum.getConditionListElements().get( 0 )
                                        .getChildCMLElements( "scalar" );
                        boolean found = false;
                        for ( int i = 0; i < els.size(); i++ ) {
                            if ( els.get( i ).getAttribute( "dictRef" ) != null
                                 && els
                                         .get( i )
                                         .getAttribute( "dictRef" )
                                         .getValue()
                                         .equals(
                                                  Bc_nmrshiftdbConstants.solvent ) ) {
                                ((CMLScalar) els.get( i ))
                                        .setValue( messagePage.getTexts2()
                                                .get( k ).getText().trim() );
                                found = true;
                                break;
                            }
                        }
                        if ( !found ) {
                            CMLScalar scalar = new CMLScalar();
                            scalar.setDictRef( Bc_nmrshiftdbConstants.solvent );
                            scalar.setValue( messagePage.getTexts2().get( k )
                                    .getText().trim() );
                            spectrum.getConditionListElements().get( 0 )
                                    .addScalar( scalar );
                        }
                        found = false;
                        for ( int i = 0; i < els.size(); i++ ) {
                            if ( els.get( i ).getAttribute( "dictRef" ) != null
                                 && els
                                         .get( i )
                                         .getAttribute( "dictRef" )
                                         .getValue()
                                         .equals(
                                                  Bc_nmrshiftdbConstants.temperature ) ) {
                                ((CMLScalar) els.get( i ))
                                        .setValue( messagePage.getTexts3()
                                                .get( k ).getText().trim() );
                                found = true;
                                break;
                            }
                        }
                        if ( !found ) {
                            CMLScalar scalar = new CMLScalar();
                            scalar
                                    .setDictRef( Bc_nmrshiftdbConstants.temperature );
                            scalar.setValue( messagePage.getTexts3().get( k )
                                    .getText().trim() );
                            spectrum.getConditionListElements().get( 0 )
                                    .addScalar( scalar );
                        }
                        if ( spectrum.getMetadataListElements().size() == 0 ) {
                            CMLMetadataList metadata = new CMLMetadataList();
                            spectrum.addMetadataList( metadata );
                        }
                        List<CMLMetadata> assignmentmetadatas =
                                MetadataUtils
                                        .getMetadataDescendantsByName(
                                                                       MetadataUtils
                                                                               .getAllInOneMetadataList(
                                                                                                         spectrum )
                                                                               .getMetadataDescendants(),
                                                                       Bc_nmrshiftdbConstants.assignment );
                        if ( assignmentmetadatas.size() == 0 ) {
                            CMLMetadata metadata = new CMLMetadata();
                            metadata
                                    .setName( Bc_nmrshiftdbConstants.assignment );
                            spectrum.getMetadataListElements().get( 0 )
                                    .addMetadata( metadata );
                            assignmentmetadatas.add( metadata );
                        }
                        assignmentmetadatas.get( 0 )
                                .setContent(
                                             messagePage.getTexts4().get( k )
                                                     .getText().trim() );
                        List<CMLMetadata> nucleusmetadatas =
                                MetadataUtils
                                        .getMetadataDescendantsByName(
                                                                       MetadataUtils
                                                                               .getAllInOneMetadataList(
                                                                                                         spectrum )
                                                                               .getMetadataDescendants(),
                                                                       SpecMolEditor.nucleus );
                        if ( nucleusmetadatas.size() == 0 ) {
                            CMLMetadata metadata = new CMLMetadata();
                            metadata.setName( SpecMolEditor.nucleus );
                            spectrum.getMetadataListElements().get( 0 )
                                    .addMetadata( metadata );
                            nucleusmetadatas.add( metadata );
                        }
                        nucleusmetadatas.get( 0 )
                                .setContent(
                                             messagePage.getTexts5().get( k )
                                                     .getText().trim() );
                        mlist = spectrum.getMetadataListElements().get( 0 );
                        removeAllChilds( mlist, "nmr:keyword" );
                        StringTokenizer keywordsspec =
                                new StringTokenizer( messagePage.texts6.get( k )
                                        .getText(), ";" );
                        while ( keywordsspec.hasMoreTokens() ) {
                            CMLMetadata metadata = new CMLMetadata();
                            metadata.setName( "nmr:keyword" );
                            metadata.setContent( keywordsspec.nextToken() );
                            mlist.addMetadata( metadata );
                        }
                        removeAllChilds( mlist, "nmr:link" );
                        StringTokenizer linksspec =
                                new StringTokenizer( messagePage.texts7.get( k )
                                        .getText(), "\r\n" );
                        while ( linksspec.hasMoreTokens() ) {
                            CMLMetadata metadata = new CMLMetadata();
                            metadata.setName( "nmr:link" );
                            metadata.setContent( linksspec.nextToken() );
                            mlist.addMetadata( metadata );
                        }
                        boolean allHaveMulti = true;
                        for ( int i = 0; i < spectrum.getPeakListElements()
                                .get( 0 ).getPeakElements().size(); i++ ) {
                            if ( spectrum.getPeakListElements().get( 0 )
                                    .getPeakElements().get( i )
                                    .getPeakMultiplicity() == null
                                 || spectrum.getPeakListElements().get( 0 )
                                         .getPeakElements().get( i )
                                         .getPeakMultiplicity().equals( "" ) ) {
                                allHaveMulti = false;
                                break;
                            }
                        }
                        for ( int i = 0; i < spectrum.getPeakListElements()
                                .get( 0 ).getPeakElements().size(); i++ ) {
                            if ( spectrum.getPeakListElements().get( 0 )
                                    .getPeakElements().get( i ).getAtomRefs() == null ) {
                                MessageBox mb2 =
                                        new MessageBox( this.getShell(),
                                                        SWT.YES | SWT.NO );
                                mb2.setText( "No assignment for "
                                             + spectrum.getPeakListElements()
                                                     .get( 0 )
                                                     .getPeakElements().get( i )
                                                     .getXValue() );
                                mb2
                                        .setMessage( "The peak "
                                                     + spectrum
                                                             .getPeakListElements()
                                                             .get( 0 )
                                                             .getPeakElements()
                                                             .get( i )
                                                             .getXValue()
                                                     + " has no atoms assigned. Do you want to continue the submit nevertheless?" );
                                if ( mb2.open() == SWT.NO ) {
                                    return false;
                                }
                            }
                        }
                        if ( messagePage.getTexts5().get( k ).getText().trim()
                                .equals( "13C" )
                             && !allHaveMulti ) {
                            MessageBox mb =
                                    new MessageBox( this.getShell(), SWT.YES
                                                                     | SWT.NO );
                            mb.setText( "No multiplicities" );
                            mb
                                    .setMessage( "The spectrum "
                                                 + spectrum.getId()
                                                 + " is a 13C spectrum and does not have multiplicities for all peaks. We could autogenerate these from hydrogen count. Shall we do this?" );
                            if ( mb.open() == SWT.YES ) {
                                net.bioclipse.cdk.business.Activator
                                        .getDefault()
                                        .getJavaCDKManager()
                                        .addImplicitHydrogens(
                                                               new CDKMolecule(
                                                                                cdkmol ) );
                                for ( int i = 0; i < spectrum
                                        .getPeakListElements().get( 0 )
                                        .getPeakElements().size(); i++ ) {
                                    if ( spectrum.getPeakListElements().get( 0 )
                                            .getPeakElements().get( i )
                                            .getAtomRefs() != null
                                         && spectrum.getPeakListElements()
                                                 .get( 0 ).getPeakElements()
                                                 .get( i ).getAtomRefs().length > 0 ) {
                                        int hs =
                                                getAtomById(
                                                             cdkmol,
                                                             spectrum
                                                                     .getPeakListElements()
                                                                     .get( 0 )
                                                                     .getPeakElements()
                                                                     .get( i )
                                                                     .getAtomRefs()[0] )
                                                        .getHydrogenCount();
                                        List<IAtom> connatoms =
                                                cdkmol
                                                        .getConnectedAtomsList( getAtomById(
                                                                                             cdkmol,
                                                                                             spectrum
                                                                                                     .getPeakListElements()
                                                                                                     .get(
                                                                                                           0 )
                                                                                                     .getPeakElements()
                                                                                                     .get(
                                                                                                           i )
                                                                                                     .getAtomRefs()[0] ) );
                                        for ( int m = 0; m < connatoms.size(); m++ ) {
                                            if ( ((IAtom) connatoms.get( m ))
                                                    .getSymbol().equals( "H" ) )
                                                hs++;
                                        }
                                        // connected hs dazu
                                        if ( hs == 0 )
                                            spectrum.getPeakListElements()
                                                    .get( 0 ).getPeakElements()
                                                    .get( i )
                                                    .setPeakMultiplicity( "S" );
                                        if ( hs == 1 )
                                            spectrum.getPeakListElements()
                                                    .get( 0 ).getPeakElements()
                                                    .get( i )
                                                    .setPeakMultiplicity( "D" );
                                        if ( hs == 2 )
                                            spectrum.getPeakListElements()
                                                    .get( 0 ).getPeakElements()
                                                    .get( i )
                                                    .setPeakMultiplicity( "T" );
                                        if ( hs == 3 )
                                            spectrum.getPeakListElements()
                                                    .get( 0 ).getPeakElements()
                                                    .get( i )
                                                    .setPeakMultiplicity( "Q" );
                                    }
                                }
                            }
                        }
                    }
                } else {
                    removedSpectra.add( spectrum );
                }
            }
            
            for ( int k = 0; k < removedSpectra.size(); k++ ) {
                cmlcml.removeChild( removedSpectra.get( k ) );                
            }
            try {
                Activator.getDefault().getJavaSpecmolManager()
                    .saveSpecmol( new JumboSpecmol( cmlcml ), biores );
            } catch ( Exception e ) {
                LogUtils.handleException( e, logger, net.bioclipse.nmrshiftdb.Activator.ID );
            }
            if ( problems.toString().equals( "" ) ) {
                net.bioclipse.nmrshiftdb.Activator.getDefault().getJavaNmrshiftdbManager().submitSpecmol( cmlcml, passwordPage.getSelectedServer(), getPasswordPage().getUsername(), getPasswordPage().getPassword(), new BioclipseUIJob<String>() {
                    @Override
                    public void runInUI() {
                        for ( int i = 0; i < removedSpectra.size(); i++ ) {
                            cmlcml.appendChild( removedSpectra.get( i ) );
                        }
                        try {
                            Activator.getDefault().getJavaSpecmolManager()
                                .saveSpecmol( new JumboSpecmol( cmlcml ), biores );
                        } catch ( Exception e ) {
                            LogUtils.handleException( e, logger, net.bioclipse.nmrshiftdb.Activator.ID );
                        }
                        MessageBox mb = new MessageBox( new Shell(), SWT.OK );
                        mb.setText( "Successfully submitted" );
                        mb
                                .setMessage( "Thanks for your contribution! The id of your entry is "
                                             + getReturnValue() );
                        mb.open();
                    }
                    
                });
            } else {
                MessageBox mb = new MessageBox( this.getShell(), SWT.OK );
                mb.setText( "Problem submitting - please correct" );
                mb.setMessage( problems.toString() );
                mb.open();
                for ( int i = 0; i < removedSpectra.size(); i++ ) {
                    cmlcml.appendChild( removedSpectra.get( i ) );
                }
                return false;
            }
        } catch ( Exception ex ) {
            LogUtils.handleException( ex, logger, net.bioclipse.nmrshiftdb.Activator.ID );
        }
        return true;
    }

    private IAtom getAtomById( IAtomContainer ac, String id ) {

        for ( int i = 0; i < ac.getAtomCount(); i++ ) {
            if ( ac.getAtom( i ).getID().equals( id ) )
                return ac.getAtom( i );
        }
        return null;
    }

    public PasswordWizardPage getPasswordPage() {

        return passwordPage;
    }

    public IViewPart getView() {

        return view;
    }

    public String getUserPreference() {

        return net.bioclipse.nmrshiftdb.Activator.getDefault()
                .getPluginPreferences().getString( REMEMBER_NMRSHIFTDB_USER );
    }

    public String getPwPreference() {

        return net.bioclipse.nmrshiftdb.Activator.getDefault()
                .getPluginPreferences().getString( REMEMBER_NMRSHIFTDB_PW );
    }

    public boolean getNmrshiftdbPreference() {

        return net.bioclipse.nmrshiftdb.Activator.getDefault()
                .getPluginPreferences().getBoolean( REMEMBER_NMRSHIFTDB_VALUE );
    }

    public void setPwPreference( String value ) {

        net.bioclipse.nmrshiftdb.Activator.getDefault().getPluginPreferences()
                .setValue( REMEMBER_NMRSHIFTDB_PW, value );
    }

    public void setUserPreference( String value ) {

        net.bioclipse.nmrshiftdb.Activator.getDefault().getPluginPreferences()
                .setValue( REMEMBER_NMRSHIFTDB_USER, value );
    }

    public void setNmrshiftdbPreference( boolean value ) {

        net.bioclipse.nmrshiftdb.Activator.getDefault().getPluginPreferences()
                .setValue( REMEMBER_NMRSHIFTDB_VALUE, value );
    }

    private void removeAllChilds( CMLMetadataList ml, String name ) {

        List<CMLMetadata> l =
                MetadataUtils.getMetadataDescendantsByName( ml
                        .getMetadataDescendants(), name );
        Iterator<CMLMetadata> it = l.iterator();
        while ( it.hasNext() )
            ml.removeChild( (CMLMetadata) it.next() );
    }
}