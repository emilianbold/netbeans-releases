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
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private static ThreadLocal  reentrantCheck = new ThreadLocal();
    
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
                ProvidedExtensions.DeleteHandler delegate = ((ProvidedExtensions)iListener).getDeleteHandler(f);
                retValue = delegate != null ? new DelegatingDeleteHandler(delegate) : null;
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
                ProvidedExtensions.IOHandler delgate = ((ProvidedExtensions)iListener).getRenameHandler(from, newName);
                retValue = delgate != null ? new DelegatingIOHandler(delgate) : null;
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
                ProvidedExtensions.IOHandler delgate = ((ProvidedExtensions)iListener).getMoveHandler(from, to);
                retValue = delgate != null ? new DelegatingIOHandler(delgate) : null;                
            }
        }
        return retValue;
    }
    
    public void createFailure(final FileObject parent, final String name, final boolean isFolder) {
        for (Iterator it = annotationProviders.iterator(); it.hasNext();) {
            AnnotationProvider provider = (AnnotationProvider) it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener != null) {
                runCheckCode(new Runnable() {
                    public void run() {
                        iListener.createFailure(parent, name, isFolder);
                    }
                });                                                                                                                                                                                                                                                                                
            }
        }
    }
    
    public void beforeCreate(final FileObject parent, final String name, final boolean isFolder) {
        for (Iterator it = annotationProviders.iterator(); it.hasNext();) {
            AnnotationProvider provider = (AnnotationProvider) it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener != null) {
                runCheckCode(new Runnable() {
                    public void run() {
                        iListener.beforeCreate(parent, name, isFolder);
                    }
                });                                                                                                                                                                                                                                                
            }
        }
    }
    
    public void deleteSuccess(final FileObject fo) {
        for (Iterator it = annotationProviders.iterator(); it.hasNext();) {
            AnnotationProvider provider = (AnnotationProvider) it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener != null) {
                runCheckCode(new Runnable() {
                    public void run() {
                        iListener.deleteSuccess(fo);
                    }
                });                                                                                                                                                                                                                
            }
        }
    }
    
    public void deleteFailure(final FileObject fo) {
        for (Iterator it = annotationProviders.iterator(); it.hasNext();) {
            AnnotationProvider provider = (AnnotationProvider) it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener != null) {
                runCheckCode(new Runnable() {
                    public void run() {
                        iListener.deleteFailure(fo);
                    }
                });                                                                                                                                                                                
            }
        }
    }
    
    public void createSuccess(final FileObject fo) {
        for (Iterator it = annotationProviders.iterator(); it.hasNext();) {
            AnnotationProvider provider = (AnnotationProvider) it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener != null) {
                runCheckCode(new Runnable() {
                    public void run() {
                        iListener.createSuccess(fo);
                    }
                });                                                                                                                                                
            }
        }
    }
    
    public void beforeDelete(final FileObject fo) {
        for (Iterator it = annotationProviders.iterator(); it.hasNext();) {
            AnnotationProvider provider = (AnnotationProvider) it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener != null) {
                runCheckCode(new Runnable() {
                    public void run() {
                        iListener.beforeDelete(fo);
                    }
                });                                                                                                                
            }
        }
    }       

    public void beforeChange(final FileObject f) {    
        for (Iterator it = annotationProviders.iterator(); it.hasNext();) {
            AnnotationProvider provider = (AnnotationProvider) it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener instanceof ProvidedExtensions) {
                runCheckCode(new Runnable() {
                    public void run() {
                        ((ProvidedExtensions)iListener).beforeChange(f);
                    }
                });                                                                                
            }
        }
    }

    public void fileLocked(final FileObject fo) {
        for (Iterator it = annotationProviders.iterator(); it.hasNext();) {
            AnnotationProvider provider = (AnnotationProvider) it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener instanceof ProvidedExtensions) {
                runCheckCode(new Runnable() {
                    public void run() {
                        ((ProvidedExtensions)iListener).fileLocked(fo);
                    }
                });                                                
            }
        }
    }

    public void fileUnlocked(final FileObject fo) {
        for (Iterator it = annotationProviders.iterator(); it.hasNext();) {
            AnnotationProvider provider = (AnnotationProvider) it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener instanceof ProvidedExtensions) {
                runCheckCode(new Runnable() {
                    public void run() {
                        ((ProvidedExtensions)iListener).fileUnlocked(fo);
                    }
                });                
            }
        }
    }        
    
    public static void checkReentrancy() {
        if (reentrantCheck.get() != null) {            
            Logger.getLogger("org.netbeans.modules.masterfs.ProvidedExtensionsProxy").log(Level.INFO,"Unexpected reentrant call", new Throwable());//NOI18N
            
        }
    }
        
    private static void runCheckCode(Runnable code) {
        try {
            reentrantCheck.set(Boolean.TRUE);
            code.run();
        } finally {
            reentrantCheck.set(null);
        }
    }
    
    private class DelegatingDeleteHandler implements ProvidedExtensions.DeleteHandler {
        private ProvidedExtensions.DeleteHandler delegate;
        private DelegatingDeleteHandler(final ProvidedExtensions.DeleteHandler delegate) {
            this.delegate = delegate;
        }
        public boolean delete(final File file) {
            final boolean[] retval = new boolean[1];
            runCheckCode(new Runnable() {
                public void run() {
                    retval[0] = delegate.delete(file);                    
                }
            });
            return retval[0];
        }        
    }
    
    private class DelegatingIOHandler implements ProvidedExtensions.IOHandler {
        private ProvidedExtensions.IOHandler delegate;
        private DelegatingIOHandler(final ProvidedExtensions.IOHandler delegate) {
            this.delegate = delegate;
        }        
        public void handle() throws IOException {
            final IOException[] retval = new IOException[1];
            runCheckCode(new Runnable() {
                public void run() {
                    try {
                        delegate.handle();
                    } catch (IOException ex) {
                        retval[0] = ex;
                    }
                }
            });
            if (retval[0] != null) {
                throw retval[0];
            }
        }
    }    
}
