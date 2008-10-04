/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.usages;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.source.parsing.CachingArchiveProvider;
import org.netbeans.modules.java.source.usages.fcs.FileChangeSupport;
import org.netbeans.modules.java.source.usages.fcs.FileChangeSupportEvent;
import org.netbeans.modules.java.source.usages.fcs.FileChangeSupportListener;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex.Action;
import org.openide.util.WeakSet;

/**
 *
 * @author Jan Lahoda
 */
public class ClassPathRootsListener implements PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(ClassPathRootsListener.class.getName());
    
    private static ClassPathRootsListener INSTANCE;
    
    public static synchronized ClassPathRootsListener getDefault() {
        if (INSTANCE == null) {
            INSTANCE = new ClassPathRootsListener();
        }
        
        return INSTANCE;
    }

    final Map<ClassPath, Collection<ClassPathRootsChangedListener>> cp2Listeners;
    final Map<ClassPath, Set<File>> classPath2Roots;
    final Map<ClassPath, Boolean> classPath2Translate;
    final Map<ClassPath.Entry, File> entry2File;
    final Map<ClassPath.Entry, FileChangeSupportListener> entry2Listener;
    final Map<File, Reference<FileChangeSupportListener>> file2Listener;
    final Map<File, Set<ClassPath>> file2ClassPaths;
    final Map<File, Reference<File>> fileNormalizationFacility;
    
    public ClassPathRootsListener() {
        this.cp2Listeners = new  WeakHashMap<ClassPath, Collection<ClassPathRootsChangedListener>>();
        this.classPath2Roots = new  WeakHashMap<ClassPath, Set<File>>();
        this.classPath2Translate = new  WeakHashMap<ClassPath, Boolean>();
        this.entry2File = new WeakHashMap<ClassPath.Entry, File>();
        this.entry2Listener = new WeakHashMap<ClassPath.Entry, FileChangeSupportListener>();
        this.file2Listener = new WeakHashMap<File, Reference<FileChangeSupportListener>>();
        this.file2ClassPaths = new WeakHashMap<File, Set<ClassPath>>();
        
        this.fileNormalizationFacility = new WeakHashMap<File, Reference<File>>();
    }
    
    public void addClassPathRootsListener(final ClassPath cp, final boolean translate, final ClassPathRootsChangedListener pcl) {
        //#133073:
        ProjectManager.mutex().readAccess(new Action<Void>() {
            public Void run() {
                addClassPathRootsListenerImpl(cp, translate, pcl);
                return null;
            }
        });
    }
    
    private synchronized void addClassPathRootsListenerImpl(ClassPath cp, boolean translate, ClassPathRootsChangedListener pcl) {
        Collection<ClassPathRootsChangedListener> listeners = cp2Listeners.get(cp);
        
        if (listeners == null) {
            cp2Listeners.put(cp, listeners = new  HashSet<ClassPathRootsChangedListener>());
            cp.addPropertyChangeListener(this);
        }
        
        listeners.add(pcl);
        
        classPath2Translate.put(cp, translate);
        
        handleClassPath(cp);
    }
    
    private static final ClassPath EMPTY_CLASSPATH = ClassPathSupport.createClassPath(new FileObject[0]);
    
    private synchronized void handleClassPath(ClassPath cp) {
        Set<File> removedRoots = classPath2Roots.get(cp);
        
        if (removedRoots != null) {
            removedRoots = new  HashSet<File>(removedRoots);
        } else {
            removedRoots = Collections.emptySet();
        }
        
        ClassPath translated;
        Map<URL, ClassPath.Entry> url2Entry = new HashMap<URL, ClassPath.Entry>();

        for (ClassPath.Entry e : cp.entries()) {
            url2Entry.put(e.getURL(), e);
        }

        if (classPath2Translate.get(cp) == Boolean.TRUE) {
            translated = ClasspathInfoAccessor.getINSTANCE().getCachedClassPath(ClasspathInfo.create(EMPTY_CLASSPATH, cp, EMPTY_CLASSPATH), ClasspathInfo.PathKind.COMPILE);
        } else {
            translated = cp;
        }
        
        for (ClassPath.Entry e : translated.entries()) {
            URL url = e.getURL();

            e = url2Entry.get(url);

            if (e == null) {
                continue;
            }

            File f = fileForURL(url);

            Reference<File> rf = fileNormalizationFacility.get(f);
            
            File x = rf != null ? rf.get() : null;
            
            if (x == null) {
                fileNormalizationFacility.put(f, new WeakReference<File>(f));
            } else {
                f = x;
            }

            if (f != null) {
                entry2File.put(e, f);

                Reference<FileChangeSupportListener> r = file2Listener.get(f);
                FileChangeSupportListener l = r != null ? r.get() : null;

                if (l == null) {
                    l = new FileChangeSupportListener() {
                        public void fileCreated(FileChangeSupportEvent event) {
                            LOGGER.log(Level.FINE, "file created: {0}", event.getPath());
                            fileChanged(event.getPath());
                        }

                        public void fileDeleted(FileChangeSupportEvent event) {
                            LOGGER.log(Level.FINE, "file deleted: {0}", event.getPath());
                            fileChanged(event.getPath());
                        }

                        public void fileModified(FileChangeSupportEvent event) {
                            LOGGER.log(Level.FINE, "file modified: {0}", event.getPath());
                            fileChanged(event.getPath());
                        }

                        private void fileChanged(File f) {
                            try {
                                CachingArchiveProvider.getDefault().clearArchive(f.toURI().toURL());
                            } catch (MalformedURLException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            
                            Collection<ClassPath> cps = file2ClassPaths.get(f);

                            if (cps != null) {
                                fireRootsChanged(Collections.unmodifiableCollection(new LinkedList<ClassPath>(cps)), f);
                            }
                        }
                    };
                    
                    FileChangeSupport.DEFAULT.addListener(l, f);
                    
                    file2Listener.put(f, new  WeakReference<FileChangeSupportListener>(l));
                }

                Set<ClassPath> cps = file2ClassPaths.get(f);

                if (cps == null) {
                    file2ClassPaths.put(f, cps = new WeakSet<ClassPath>());
                }

                cps.add(cp);
                
                Set<File> files = classPath2Roots.get(cp);
                
                if (files == null) {
                    classPath2Roots.put(cp, files = new  WeakSet<File>());
                }
                
                files.add(f);
                
                entry2Listener.put(e, l);
                
                removedRoots.remove(f);
            }
        }
        
        for (File r : removedRoots) {
            Set<ClassPath> cps = file2ClassPaths.get(r);
            
            if(cps != null) {
                cps.remove(cp);
                
                if (cps.isEmpty()) {
                    Reference<FileChangeSupportListener> ref = file2Listener.remove(r);
                    FileChangeSupportListener l = ref != null ? ref.get() : null;
                    
                    if (l != null) {
                        FileChangeSupport.DEFAULT.removeListener(l, r);
                    }
                    
                    file2ClassPaths.remove(r);
                }
            }
        }
    }


    static File fileForURL(URL root) {
        File f = fileForURLImpl(root);
        
        if (f == null) {
            return null;
        }
        
        f = FileUtil.normalizeFile(f);
        
        if (f.getAbsolutePath().startsWith(Index.getCacheFolder().getAbsolutePath())) {
            return null;
        }
        
        return f;
    }
    
    private static File fileForURLImpl(URL root) {
        String protocol = root.getProtocol();
        if ("file".equals(protocol)) {
            return new File(URI.create(root.toExternalForm()));
        }
        if ("jar".equals(protocol)) {
            URL inner = FileUtil.getArchiveFile(root);
            protocol = inner.getProtocol();
            if ("file".equals(protocol)) {
                return new File(URI.create(inner.toExternalForm()));
            }
        }
        
        return null;
    }
    
    private void fireRootsChanged(Collection<ClassPath> cps, File binary) {
        Map<ClassPathRootsChangedListener, Boolean> listeners = new IdentityHashMap<ClassPathRootsChangedListener, Boolean>();
        
        synchronized (this) {
            for (ClassPath cp : cps) {
                Collection<ClassPathRootsChangedListener> thisListeners = cp2Listeners.get(cp);

                if (listeners != null) {
                    for (ClassPathRootsChangedListener l : thisListeners) {
                        listeners.put(l, true);
                    }
                }
            }
        }
        
        if (listeners.isEmpty())
            return;
        
        for (ClassPathRootsChangedListener l : listeners.keySet()) {
            l.rootsChanged(cps, binary);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (ClassPath.PROP_ENTRIES.equals(evt.getPropertyName())) {
            ClassPath cp = (ClassPath) evt.getSource();
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "classpath entries changed: cp={0} ({2}), entries={1}", new Object[] {cp, cp.entries(), System.identityHashCode(cp)});
            }
            handleClassPath(cp);
            fireRootsChanged(Collections.singleton(cp), null);
        }
    }
    
    public static interface ClassPathRootsChangedListener {
        public void rootsChanged(Collection<ClassPath> forCPs, File binary);
    }
}
