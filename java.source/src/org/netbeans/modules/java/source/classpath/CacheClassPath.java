/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.java.source.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.parsing.impl.indexing.PathRegistry;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
public class CacheClassPath implements ClassPathImplementation, PropertyChangeListener {
    
    public static final boolean KEEP_JARS = Boolean.getBoolean("CacheClassPath.keepJars");     //NOI18N
    private static Logger log = Logger.getLogger(CacheClassPath.class.getName());
    
    private final ClassPath cp;    
    private final boolean translate;
    private final PropertyChangeSupport listeners;
    private List<PathResourceImplementation> cache;
    private long eventId;
    
    /** Creates a new instance of CacheClassPath */
    private CacheClassPath (ClassPath cp, boolean translate) {
        this.listeners = new PropertyChangeSupport (this);
        this.cp = cp;
        this.translate = translate;
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
                this.eventId++;
            }
            this.listeners.firePropertyChange(PROP_RESOURCES,null,null);
        }
    }

    public List<? extends PathResourceImplementation> getResources() {
        long currentEventId;
        synchronized (this) {
            if (this.cache!= null) {
                return this.cache;
            }
            currentEventId = this.eventId;
        }        
        final List<ClassPath.Entry> entries = this.cp.entries();
        final List<PathResourceImplementation> _cache = new LinkedList<PathResourceImplementation> ();            
        final PathRegistry preg = PathRegistry.getDefault();
        for (ClassPath.Entry entry : entries) {
            URL url = entry.getURL();
            URL[] sourceUrls;
            if (translate) {
                sourceUrls = preg.sourceForBinaryQuery(url, this.cp, true);
            }
            else {        
                sourceUrls = new URL[] {url};
            }
            if (sourceUrls != null) {
                for (URL sourceUrl : sourceUrls) {
                    try {
                        File cacheFolder = JavaIndex.getClassFolder(sourceUrl);
                        URL cacheUrl = cacheFolder.toURI().toURL();
                        if (!cacheFolder.exists()) {                                
                            cacheUrl = new URL (cacheUrl.toExternalForm()+"/");     //NOI18N
                        }
                        _cache.add(ClassPathSupport.createResource(cacheUrl));
                    } catch (IOException ioe) {
                        if (log.isLoggable(Level.SEVERE))
                            log.log(Level.SEVERE, ioe.getMessage(), ioe);
                    }
                }
                if (KEEP_JARS && translate) {
                    _cache.add(ClassPathSupport.createResource(url));
                }
            } else {
                if (FileObjects.JAR.equals(url.getProtocol())) {
                    URL foo = FileUtil.getArchiveFile(url);
                    if (!FileObjects.FILE.equals(foo.getProtocol())) {
                        FileObject fo = URLMapper.findFileObject(foo);
                        if (fo != null) {
                            foo = URLMapper.findURL(fo, URLMapper.EXTERNAL);
                            if (FileObjects.FILE.equals(foo.getProtocol())) {
                                url = FileUtil.getArchiveRoot(foo);
                            }
                        }
                    }
                }
                else if (!FileObjects.FILE.equals(url.getProtocol())) {
                    FileObject fo = URLMapper.findFileObject(url);
                    if (fo != null) {
                        URL foo = URLMapper.findURL(fo, URLMapper.EXTERNAL);
                        if (FileObjects.FILE.equals(foo.getProtocol())) {
                            url = foo;
                        }
                    }
                }
                try {
                    File sigs = JavaIndex.getClassFolder(url);
                    URL orl = sigs.toURI ().toURL ();
                    if (!orl.toExternalForm ().endsWith ("/"))
                        orl = new URL (orl.toExternalForm () + "/");
                    _cache.add (ClassPathSupport.createResource(orl));
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
                _cache.add (ClassPathSupport.createResource(url));
            }
        }
        List<? extends PathResourceImplementation> res;
        synchronized (this) {
            if (currentEventId == this.eventId) {
                if (this.cache == null) {
                    this.cache = _cache;
                }
                res = this.cache;
            }
            else {
                res = _cache;
            }            
        }
        assert res != null;
        return res;
    }
    
    
    public static ClassPath forClassPath (final ClassPath cp) {
        assert cp != null;
        return ClassPathFactory.createClassPath(new CacheClassPath(cp,true));
    }
    
    public static ClassPath forBootPath (final ClassPath cp) {
        assert cp != null;
        return ClassPathFactory.createClassPath(new CacheClassPath(cp,true));
    }
    
    public static ClassPath forSourcePath (final ClassPath sourcePath) {
        assert sourcePath != null;
        return ClassPathFactory.createClassPath(new CacheClassPath(sourcePath,false));
    }
    
}
