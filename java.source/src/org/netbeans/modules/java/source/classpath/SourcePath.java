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

package org.netbeans.modules.java.source.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.ClassIndexManagerEvent;
import org.netbeans.modules.java.source.usages.ClassIndexManagerListener;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
public class SourcePath implements ClassPathImplementation, ClassIndexManagerListener, PropertyChangeListener {

    private final PropertyChangeSupport listeners = new PropertyChangeSupport(this);
    private final ClassPath delegate;
    private final ClassIndexManager manager;
    private final boolean forcePrefSources;
    private List<PathResourceImplementation> resources;
    private long eventId;
    
    private SourcePath (final ClassPath delegate, final boolean bkgComp) {
        this.delegate = delegate;
        this.manager = ClassIndexManager.getDefault();
        manager.addClassIndexManagerListener(WeakListeners.create(ClassIndexManagerListener.class, this, manager));
        delegate.addPropertyChangeListener(WeakListeners.propertyChange(this, delegate));
        this.forcePrefSources = bkgComp;
    }
    
    public List<? extends PathResourceImplementation> getResources() {
        long currentEventId;
        synchronized (this) {
            if (resources != null) {
                return this.resources;
            }
            currentEventId = this.eventId;
        }
        
        List<PathResourceImplementation> res = new ArrayList<PathResourceImplementation>();
        for (ClassPath.Entry entry : delegate.entries()) {
            if (forcePrefSources || !JavaIndex.isLibrary(entry.getURL())) {
                res.add(new FR (entry));
            }
        }
        synchronized (this) {
            if (currentEventId == this.eventId) {
                if (this.resources == null) {
                    this.resources = res;
                }
                else {
                    res = this.resources;
                }
            }
        }
        assert res != null;
        return res;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        listeners.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        listeners.removePropertyChangeListener(listener);
    }
    
    public void classIndexAdded(ClassIndexManagerEvent event) {
        if (forcePrefSources) {
            return;
        }
        final Set<? extends URL> newRoots = event.getRoots();
        boolean changed = false;
        for (ClassPath.Entry entry : delegate.entries()) {
            changed = newRoots.contains(entry.getURL());
            if (changed) {
                break;
            }
        }
        if (changed) {            
            synchronized (this) {
                this.resources = null;
                this.eventId++;
            }
            listeners.firePropertyChange(PROP_RESOURCES, null, null);
        }
        
    }

    public void classIndexRemoved(ClassIndexManagerEvent event) {
              
    }
    
    public void propertyChange (final PropertyChangeEvent event) {
        synchronized (this) {
            this.resources = null;
            this.eventId++;
        }
        listeners.firePropertyChange(PROP_RESOURCES, null, null);
    }
    
    private static class FR implements FilteringPathResourceImplementation, PropertyChangeListener {
        
        private final ClassPath classPath;
        private final ClassPath.Entry entry;
        private final PropertyChangeSupport support;
        private final URL[] cache;
        
        public FR (final ClassPath.Entry entry) {
            assert entry != null;
            this.support = new PropertyChangeSupport(this);
            this.entry = entry;
            this.classPath = entry.getDefiningClassPath();
            this.classPath.addPropertyChangeListener(WeakListeners.propertyChange(this, classPath));
            this.cache = new URL[] {entry.getURL()};
        }

        public boolean includes(URL root, String resource) {
            assert this.cache[0].equals(root);
            return entry.includes(resource);
        }

        public URL[] getRoots() {
            return cache;
        }

        public ClassPathImplementation getContent() {
            return null;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            this.support.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            this.support.removePropertyChangeListener(listener);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (ClassPath.PROP_INCLUDES.equals(evt.getPropertyName())) {
                this.support.firePropertyChange(PROP_INCLUDES, null, null);
            }
        }
        
    }
    
    public static ClassPath create (final ClassPath cp, final boolean bkgComp) {
        assert cp != null;
        return ClassPathFactory.createClassPath(new SourcePath(cp, bkgComp));
    }

}
