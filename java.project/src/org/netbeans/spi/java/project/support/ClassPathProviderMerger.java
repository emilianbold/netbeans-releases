/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.spi.java.project.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.project.LookupMerger;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Lookup Merger implementation for ClassPathProvider
 * 
 * @author Tomas Zezula, Milos Kleint
 */
final class ClassPathProviderMerger implements LookupMerger<ClassPathProvider> {

    private final ClassPathProvider defaultProvider;

    ClassPathProviderMerger(final ClassPathProvider defaultProvider) {
        assert defaultProvider != null;
        this.defaultProvider = defaultProvider;
    }

    public Class<ClassPathProvider> getMergeableClass() {
        return ClassPathProvider.class;
    }

    public ClassPathProvider merge(Lookup lookup) {
        return new CPProvider(lookup);
    }

    private class CPProvider implements ClassPathProvider {

        private final Lookup lookup;
        private final Map<FileObject, Map<String, ClassPath>> cpCache = new HashMap<FileObject, Map<String, ClassPath>>();

        public CPProvider(final Lookup lookup) {
            assert lookup != null;
            this.lookup = lookup;
        }

        public ClassPath findClassPath(FileObject file, String type) {
            synchronized (cpCache) {
                Map<String, ClassPath> cptype = cpCache.get(file);
                if (cptype != null) {
                    ClassPath path = cptype.get(type);
                    if (path != null) {
                        return path;
                    }
                }
            }
            ProxyClassPathImplementation result = new ProxyClassPathImplementation(defaultProvider, lookup, file, type);
            ClassPath cp = ClassPathFactory.createClassPath(result);
            synchronized (cpCache) {
                Map<String, ClassPath> cptype = cpCache.get(file);
                if (cptype == null) {
                    cptype = new HashMap<String, ClassPath>();
                    cpCache.put(file, cptype);
                }
                cptype.put(type, cp);
            }
            return cp;
        }
    }
    
    /** ProxyClassPathImplementation provides read only proxy for PathResourceImplementations based on original ClassPath items.
     *  The order of the resources is given by the order of its delegates.
     *  The proxy is designed to be used as a union of class paths.
     *  E.g. to be able to easily iterate or listen on all design resources = sources + compile resources
     */
    class ProxyClassPathImplementation implements ClassPathImplementation {

        private PathResourceImplementation[] classPaths;
        private List<PathResourceImplementation> resourcesCache;
        private ArrayList<PropertyChangeListener> listeners;
        private LookupListener lookupList;
        private Lookup.Result<ClassPathProvider> providers;
        private ClassPathProvider mainProvider;
        private PropertyChangeListener classPathsListener;
        private FileObject file;
        private String type;

        public ProxyClassPathImplementation(ClassPathProvider dominant, Lookup context, FileObject fo, String type) {
            assert dominant != null;
            this.type = type;
            this.file = fo;
            mainProvider = dominant;
            providers = context.lookupResult(ClassPathProvider.class);
            classPathsListener = new DelegatesListener();
            
            checkProviders();
            lookupList = new LookupListener() {
                public void resultChanged(LookupEvent ev) {
                    checkProviders();
                }
            };
            providers.addLookupListener(lookupList);
            
        }
        
        private void checkProviders() {
            List<PathResourceImplementation> impls = new ArrayList<PathResourceImplementation>();
            ClassPath mainResult = mainProvider.findClassPath(file, type);
            if (mainResult != null) {
                impls.add(getClassPathImplementation(mainResult));
            }
            
            for (ClassPathProvider prvd : providers.allInstances()) {
                ClassPath path = prvd.findClassPath(file, type);
                if (path != null) {
                    impls.add(getClassPathImplementation(path));
                }
            }
            synchronized (this) {
                if (classPaths != null) {
                    for (PathResourceImplementation old : classPaths) {
                        old.removePropertyChangeListener(classPathsListener);
                    }
                }
                for (PathResourceImplementation cpImpl : impls) {
                    if (cpImpl == null) {
                        continue;
                    }
                    cpImpl.addPropertyChangeListener(classPathsListener);
                }
                this.classPaths = impls.toArray(new PathResourceImplementation[impls.size()]);
            }
            PropertyChangeEvent ev = new PropertyChangeEvent(this, ClassPathImplementation.PROP_RESOURCES, null, null);
            firePropertyChange(ev);
        }

        public List<? extends PathResourceImplementation> getResources() {
            synchronized (this) {
                if (this.resourcesCache != null) {
                    return this.resourcesCache;
                }
            }

            ArrayList<PathResourceImplementation> result = new ArrayList<PathResourceImplementation>(classPaths.length * 10);
            for (PathResourceImplementation cpImpl : classPaths) {
                result.add(cpImpl);
            }

            synchronized (this) {
                if (this.resourcesCache == null) {
                    resourcesCache = Collections.unmodifiableList(result);
                }
                return this.resourcesCache;
            }
        }

        public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
            if (this.listeners == null) {
                this.listeners = new ArrayList<PropertyChangeListener>();
            }
            this.listeners.add(listener);
        }

        public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
            if (this.listeners == null) {
                return;
            }
            this.listeners.remove(listener);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("[");   //NOI18N

            for (PathResourceImplementation cpImpl : this.classPaths) {
                builder.append(cpImpl.toString());
                builder.append(", ");   //NOI18N

            }
            builder.append("]");   //NOI18N

            return builder.toString();
        }

        private void firePropertyChange(PropertyChangeEvent event) {
                PropertyChangeListener[] _listeners;
                synchronized (this) {
                    resourcesCache = null;    //Clean the cache
                    if (listeners == null) {
                        return;
                    }
                    _listeners = listeners.toArray(new PropertyChangeListener[ProxyClassPathImplementation.this.listeners.size()]);
                }
                for (PropertyChangeListener l : _listeners) {
                    l.propertyChange(event);
                }
        }

        private class DelegatesListener implements PropertyChangeListener {

            public void propertyChange(PropertyChangeEvent evt) {
                PropertyChangeEvent event = new PropertyChangeEvent(ProxyClassPathImplementation.this, evt.getPropertyName(), null, null);
                firePropertyChange(event);
            }
        }
    }
    
    static FilteringPathResourceImplementation getClassPathImplementation(ClassPath path) {
        return new ProxyFilteringCPI(path);
    }
    
    private static class ProxyFilteringCPI implements FilteringPathResourceImplementation {
        private ClassPath classpath;

        private ProxyFilteringCPI(ClassPath path) {
            this.classpath = path;
        }

        
        public boolean includes(URL root, String resource) {
            for (ClassPath.Entry ent : classpath.entries()) {
                if (ent.getURL().equals(root)) {
                    return ent.includes(resource);
                }
            }
            return false;
        }

        public URL[] getRoots() {
            ArrayList<URL> urls = new ArrayList<URL>();
            for (ClassPath.Entry ent : classpath.entries()) {
                urls.add(ent.getURL());
            }
            return urls.toArray(new URL[urls.size()]);
        }

        public ClassPathImplementation getContent() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            classpath.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            classpath.removePropertyChangeListener(listener);
        }
        
    }
            
}
