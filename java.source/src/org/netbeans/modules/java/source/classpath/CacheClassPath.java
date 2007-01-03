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

package org.netbeans.modules.java.source.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.java.source.usages.Index;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.ErrorManager;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
public class CacheClassPath implements ClassPathImplementation, PropertyChangeListener {
    
    private final ClassPath cp;    
    private final boolean translate;
    private final boolean isBoot;
    private PropertyChangeSupport listeners;
    private List<PathResourceImplementation> cache;
    
    /** Creates a new instance of CacheClassPath */
    private CacheClassPath (ClassPath cp, boolean translate, boolean isBoot) {
        this.listeners = new PropertyChangeSupport (this);
        this.cp = cp;
        this.translate = translate;
        this.isBoot = isBoot;
        this.cp.addPropertyChangeListener (WeakListeners.propertyChange(this,cp));
    }

    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        this.listeners.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        this.listeners.addPropertyChangeListener(listener);
    }
    
    public void propertyChange (final PropertyChangeEvent event) {
        if (ClassPath.PROP_ENTRIES.equals(event.getPropertyName())) {
            synchronized (this) {
                this.cache = null;
            }
            this.listeners.firePropertyChange(PROP_RESOURCES,null,null);
        }
    }

    public synchronized List<? extends PathResourceImplementation> getResources() {
        if (this.cache == null) {            
            List<ClassPath.Entry> entries = this.cp.entries();
            this.cache = new LinkedList<PathResourceImplementation> ();
            if (isBoot && entries.size() == 0) {
                JavaPlatform defaultPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
                assert defaultPlatform != null;
                entries = defaultPlatform.getBootstrapLibraries().entries();
                assert entries.size() > 0;
                for (ClassPath.Entry entry : entries) {
                    this.cache.add (ClassPathSupport.createResource(entry.getURL()));
                }
            }
            else {
                boolean hasSigFiles = true;
                if (!translate) {
                    for (ClassPath.Entry entry : entries) {
                        if (!"file".equals(entry.getURL().getProtocol()))  {  //NOI18N
                            hasSigFiles = false;
                            break;
                        }
                    }
                }                
                final GlobalSourcePath gsp = GlobalSourcePath.getDefault();
                for (ClassPath.Entry entry : entries) {
                    URL url = entry.getURL();
                    URL[] sourceUrls;
                    if (translate) {
                        sourceUrls = gsp.getSourceRootForBinaryRoot(url, this.cp, true);
                    }
                    else if (hasSigFiles) {        
                        sourceUrls = new URL[] {url};
                    }
                    else {
                        sourceUrls = new URL[0];
                    }
                    if (sourceUrls != null) {
                        for (URL sourceUrl : sourceUrls) {
                            try {
                                File cacheFolder = Index.getClassFolder(new File (URI.create(sourceUrl.toExternalForm())));
                                URL cacheUrl = cacheFolder.toURI().toURL();
                                if (!cacheFolder.exists()) {                                
                                    cacheUrl = new URL (cacheUrl.toExternalForm()+"/");     //NOI18N
                                }
                                this.cache.add(ClassPathSupport.createResource(cacheUrl));
                            } catch (IOException ioe) {
                                ErrorManager.getDefault().notify(ioe);
                            }
                        }
                    } else {
                        this.cache.add (ClassPathSupport.createResource(url));
                    }
                }
            }
        }
        return this.cache;
    }
    
    
    public static ClassPath forClassPath (final ClassPath cp) {
        assert cp != null;
        return ClassPathFactory.createClassPath(new CacheClassPath(cp,true,false));
    }
    
    public static ClassPath forBootPath (final ClassPath cp) {
        assert cp != null;
        return ClassPathFactory.createClassPath(new CacheClassPath(cp,true,true));
    }
    
    public static ClassPath forSourcePath (final ClassPath sourcePath) {
        assert sourcePath != null;
        return ClassPathFactory.createClassPath(new CacheClassPath(sourcePath,false,false));
    }
    
}
