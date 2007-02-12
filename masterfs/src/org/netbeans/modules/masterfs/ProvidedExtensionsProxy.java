/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
    
    public ProvidedExtensions.DeleteHandler getDeleteHandler(final File f) {
        ProvidedExtensions.DeleteHandler retValue = null;
        for (Iterator it = annotationProviders.iterator(); it.hasNext() && retValue == null;) {
            AnnotationProvider provider = (AnnotationProvider) it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener instanceof ProvidedExtensions) {
                retValue = ((ProvidedExtensions)iListener).getDeleteHandler(f);
            } 
        }
        return retValue;                        
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

    public void beforeChange(FileObject f) {    
        for (Iterator it = annotationProviders.iterator(); it.hasNext();) {
            AnnotationProvider provider = (AnnotationProvider) it.next();
            InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener instanceof ProvidedExtensions) {
                ((ProvidedExtensions)iListener).beforeChange(f);
            }
        }
    }
}
