/*******************************************************************************
 * Copyright (c) 2009  Stefan Kuhn <stefan.kuhn@ebi.ac.uk>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.spectrum.filecontentprovider;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.bioclipse.core.util.LogUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * A class implementing ITreeContentProvider and only returning child elements
 * which are spectrum files. This can be used to build TreeViewers for browsing
 * for spectra.
 */
public class SpectrumFileContentProvider implements ITreeContentProvider {

    private static final Logger      logger                  =
                                                                     Logger
                                                                             .getLogger( SpectrumFileContentProvider.class );
    public final static List<String> SUPPORTED_CONTENT_TYPES =
                                                                     new ArrayList<String>() {

                                                                         {
                                                                             add( "net.bioclipse.contenttypes.cml.singleSpectrum" );
                                                                             add( "net.bioclipse.contenttypes.jcampdx" );
                                                                         }
                                                                     };

    public SpectrumFileContentProvider() {

    }

    public void dispose() {

    }

    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {

    }

    public Object[] getElements( Object inputElement ) {

        return getChildren( inputElement );
    }

    public Object[] getChildren( Object parentElement ) {

        ArrayList<IResource> childElements = new ArrayList<IResource>();
        if ( parentElement instanceof IContainer
             && ((IContainer) parentElement).isAccessible() ) {
            IContainer container = (IContainer) parentElement;
            try {
                for ( int i = 0; i < container.members().length; i++ ) {
                    IResource resource = container.members()[i];
                    if ( resource instanceof IFile ) {
                        IContentTypeManager contentTypeManager =
                                Platform.getContentTypeManager();
                        InputStream stream =
                                ((IFile) container.members()[i]).getContents();
                        IContentType contentType =
                                contentTypeManager
                                        .findContentTypeFor(
                                                             stream,
                                                             ((IFile) container
                                                                     .members()[i])
                                                                     .getName() );
                        stream.close();
                        if ( SUPPORTED_CONTENT_TYPES.contains( contentType
                                .getId() ) )
                            childElements.add( resource );
                    } else if ( resource instanceof IContainer
                                && resource.isAccessible()
                                && containsSpectra( (IContainer) resource ) ) {
                        childElements.add( resource );
                    }
                }
            } catch ( CoreException e ) {
                LogUtils.handleException( e, logger );
            } catch ( IOException e ) {
                LogUtils.handleException( e, logger );
            }
        }
        return childElements.toArray();
    }

    private boolean containsSpectra( IContainer container )
                                                           throws CoreException,
                                                           IOException {

        // we first test all the files, that should be fast
        for ( int i = 0; i < container.members().length; i++ ) {
            if ( container.members()[i] instanceof IFile ) {
                IContentTypeManager contentTypeManager =
                        Platform.getContentTypeManager();
                InputStream stream =
                        ((IFile) container.members()[i]).getContents();
                IContentType contentType =
                        contentTypeManager
                                .findContentTypeFor( stream, ((IFile) container
                                        .members()[i]).getName() );
                stream.close();
                if ( contentType != null
                     && SUPPORTED_CONTENT_TYPES.contains( contentType.getId() ) )
                    return true;
            }
        }
        // if none is a molecule, we need to recursively check child folders
        for ( int i = 0; i < container.members().length; i++ ) {
            if ( container.members()[i] instanceof IContainer ) {
                if ( containsSpectra( (IContainer) container.members()[i] ) )
                    return true;
            }
        }
        return false;
    }

    public Object getParent( Object element ) {

        return ((IFolder) element).getParent();
    }

    public boolean hasChildren( Object element ) {

        return getChildren( element ).length > 0;
    }
}