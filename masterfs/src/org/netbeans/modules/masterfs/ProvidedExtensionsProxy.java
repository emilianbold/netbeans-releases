/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.masterfs;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.masterfs.providers.AnnotationProvider;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions.IOHandler;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Radek Matous
 */
public class ProvidedExtensionsProxy extends ProvidedExtensions {
    private Collection/*AnnotationProvider*/ annotationProviders;
    
    /** Creates a new instance of ProvidedExtensionsProxy */
    public ProvidedExtensionsProxy(Collection/*AnnotationProvider*/ annotationProviders) {
        this.annotationProviders = annotationProviders;
    }
    
    public ProvidedExtensions.IOHandler getRenameHandler(final File from, final String newName) {
        final File to = new File(from.getParentFile(), newName);
        IOHandler retValue = null;
        for (Iterator it = annotationProviders.iterator(); it.hasNext() && retValue == null;) {
            AnnotationProvider provider = (AnnotationProvider) it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener instanceof ProvidedExtensions) {
                retValue = ((ProvidedExtensions)iListener).getRenameHandler(from, newName);
            } 
        }
        return retValue;
    }
    
    public ProvidedExtensions.IOHandler getMoveHandler(final File from, final File to)  {
        IOHandler retValue = null;
        for (Iterator it = annotationProviders.iterator(); it.hasNext() && retValue == null;) {
            AnnotationProvider provider = (AnnotationProvider) it.next();
            InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener instanceof ProvidedExtensions) {
                retValue = ((ProvidedExtensions)iListener).getMoveHandler(from, to);
            }
        }
        return retValue;
    }
    
    public void createFailure(FileObject parent, String name, boolean isFolder) {
        for (Iterator it = annotationProviders.iterator(); it.hasNext();) {
            AnnotationProvider provider = (AnnotationProvider) it.next();
            InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener != null) {
                iListener.createFailure(parent, name, isFolder);
            }
        }
    }
    
    public void beforeCreate(FileObject parent, String name, boolean isFolder) {
        for (Iterator it = annotationProviders.iterator(); it.hasNext();) {
            AnnotationProvider provider = (AnnotationProvider) it.next();
            InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener != null) {
                iListener.beforeCreate(parent, name, isFolder);
            }
        }
    }
    
    public void deleteSuccess(FileObject fo) {
        for (Iterator it = annotationProviders.iterator(); it.hasNext();) {
            AnnotationProvider provider = (AnnotationProvider) it.next();
            InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener != null) {
                iListener.deleteSuccess(fo);
            }
        }
    }
    
    public void deleteFailure(FileObject fo) {
        for (Iterator it = annotationProviders.iterator(); it.hasNext();) {
            AnnotationProvider provider = (AnnotationProvider) it.next();
            InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener != null) {
                iListener.deleteFailure(fo);
            }
        }
    }
    
    public void createSuccess(FileObject fo) {
        for (Iterator it = annotationProviders.iterator(); it.hasNext();) {
            AnnotationProvider provider = (AnnotationProvider) it.next();
            InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener != null) {
                iListener.createSuccess(fo);
            }
        }
    }
    
    public void beforeDelete(FileObject fo) {
        for (Iterator it = annotationProviders.iterator(); it.hasNext();) {
            AnnotationProvider provider = (AnnotationProvider) it.next();
            InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener != null) {
                iListener.beforeDelete(fo);
            }
        }
    }        
}
