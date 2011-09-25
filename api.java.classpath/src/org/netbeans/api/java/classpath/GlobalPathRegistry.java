/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.api.java.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.openide.filesystems.FileObject;

/**
 * Maintains a global registry of "interesting" classpaths of various kinds.
 * You may add and remove different kinds of {@link ClassPath}s to the registry
 * and listen to changes in them.
 * <p>
 * It is permitted to register the same classpath more than once; unregistration
 * keeps track of the number of registrations so that the operation is symmetric.
 * However {@link #getPaths} only ever returns one copy of the classpath, and
 * listeners are only notified the first time a given classpath is added to the
 * registry, or the last time it is removed.
 * (Classpath identity is object identity, so there could be multiple paths
 * returned that at the time share the same list of roots. There may also be
 * several paths which contain some shared roots.)
 * </p>
 * <p>
 * The registry is not persisted between JVM sessions.
 * </p>
 * <div class="nonnormative">
 * <p>
 * Intended usage patterns:
 * </p>
 * <ol>
 * <li><p>When a project is opened using
 * {@link org.netbeans.spi.project.ui.ProjectOpenedHook} it should add any paths
 * it defines, i.e. paths it might return from a
 * {@link org.netbeans.spi.java.classpath.ClassPathProvider}.
 * When closed it should remove them.</p></li>
 * <li><p>The <b>Fast&nbsp;Open</b> feature of the editor and other features which
 * require a global list of relevant sources should use {@link #getSourceRoots} or
 * the equivalent.</p></li>
 * <li><p>The <b>Javadoc&nbsp;Index&nbsp;Search</b> feature and <b>View&nbsp;&#8594;
 * Documentation&nbsp;Indices</b> submenu should operate on open Javadoc paths,
 * meaning that Javadoc corresponding to registered compile and boot classpaths
 * (according to {@link org.netbeans.api.java.queries.JavadocForBinaryQuery}).</p></li>
 * <li><p>Stack trace hyperlinking can use the global list of source paths
 * to find sources, in case no more specific information about their origin is
 * available. The same would be true of debugging: if the debugger cannot find
 * Java-like sources using more precise means ({@link SourceForBinaryQuery}), it
 * can use {@link #findResource} as a fallback.</p></li>
 * </ol>
 * </div>
 * @author Jesse Glick
 * @since org.netbeans.api.java/1 1.4
 */
public final class GlobalPathRegistry {

    private static final Logger LOG = Logger.getLogger(GlobalPathRegistry.class.getName());
    
    private static GlobalPathRegistry DEFAULT = new GlobalPathRegistry();
    
    /**
     * Get the singleton instance of the registry.
     * @return the default instance
     */
    public static GlobalPathRegistry getDefault() {
        return DEFAULT;
    }
    
    private int resetCount;
    private final Map<String,List<ClassPath>> paths = new HashMap<String,List<ClassPath>>();
    private final List<GlobalPathRegistryListener> listeners = new ArrayList<GlobalPathRegistryListener>();
    private Set<FileObject> sourceRoots = null;
    private Set<SourceForBinaryQuery.Result> results = new HashSet<SourceForBinaryQuery.Result>();
    
    
    private final ChangeListener resultListener = new SFBQListener ();
    
    private PropertyChangeListener classpathListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            synchronized (GlobalPathRegistry.this) {
                //Reset cache
                GlobalPathRegistry.this.resetSourceRootsCache ();
            }
        }
    };
    
    private GlobalPathRegistry() {}
    
    /** for use from unit test */
    void clear() {
        paths.clear();
        listeners.clear();
        sourceRoots = null;
    }
    
    /**
     * Find all paths of a certain type.
     * @param id a classpath type, e.g. {@link ClassPath#SOURCE}
     * @return an immutable set of all registered {@link ClassPath}s of that type (may be empty but not null)
     */
    public synchronized Set<ClassPath> getPaths(String id) {
        if (id == null) {
            throw new NullPointerException();
        }
        List<ClassPath> l = paths.get(id);
        if (l != null && !l.isEmpty()) {
            return Collections.unmodifiableSet(new HashSet<ClassPath>(l));
        } else {
            return Collections.<ClassPath>emptySet();
        }
    }
    
    /**
     * Register some classpaths of a certain type.
     * @param id a classpath type, e.g. {@link ClassPath#SOURCE}
     * @param paths a list of classpaths to add to the registry
     */
    public void register(String id, ClassPath[] paths) {
        if (id == null || paths == null) {
            throw new NullPointerException();
        }
        // Do not log just when firing an event, since there may no listeners.
        LOG.log(Level.FINE, "registering paths {0} of type {1}", new Object[] {Arrays.asList(paths), id});
        GlobalPathRegistryEvent evt = null;
        GlobalPathRegistryListener[] _listeners = null;
        synchronized (this) {
            List<ClassPath> l = this.paths.get(id);
            if (l == null) {
                l = new ArrayList<ClassPath>();
                this.paths.put(id, l);
            }
            Set<ClassPath> added = listeners.isEmpty() ? null : new HashSet<ClassPath>();
            for (ClassPath path : paths) {
                if (path == null) {
                    throw new NullPointerException("Null path encountered in " + Arrays.asList(paths) + " of type " + id); // NOI18N
                }
                if (added != null && !added.contains(path) && !l.contains(path)) {
                    added.add(path);
                }
                if (!l.contains(path)) {
                    path.addPropertyChangeListener(classpathListener);
                }
                l.add(path);
            }
            if (added != null && !added.isEmpty()) {
                _listeners = listeners.toArray(new GlobalPathRegistryListener[listeners.size()]);
                evt = new GlobalPathRegistryEvent(this, id, Collections.unmodifiableSet(added));
            }
            // Invalidate cache for getSourceRoots and findResource:
            resetSourceRootsCache ();
        }
        if (_listeners != null) {
            assert evt != null;
            for (GlobalPathRegistryListener listener : _listeners) {
                listener.pathsAdded(evt);
            }
        }
    }
    
    /**
     * Unregister some classpaths of a certain type.
     * @param id a classpath type, e.g. {@link ClassPath#SOURCE}
     * @param paths a list of classpaths to remove from the registry
     * @throws IllegalArgumentException if they had not been registered before
     */
    public void unregister(String id, ClassPath[] paths) throws IllegalArgumentException {
        LOG.log(Level.FINE, "unregistering paths {0} of type {1}", new Object[] {Arrays.asList(paths), id});
        if (id == null || paths == null) {
            throw new NullPointerException();
        }
        GlobalPathRegistryEvent evt = null;
        GlobalPathRegistryListener[] _listeners = null;
        synchronized (this) {
            List<ClassPath> l = this.paths.get(id);
            if (l == null) {
                l = new ArrayList<ClassPath>();
            }
            List<ClassPath> l2 = new ArrayList<ClassPath>(l); // in case IAE thrown below
            Set<ClassPath> removed = listeners.isEmpty() ? null : new HashSet<ClassPath>();
            for (ClassPath path : paths) {
                if (path == null) {
                    throw new NullPointerException();
                }
                if (!l2.remove(path)) {
                    throw new IllegalArgumentException("Attempt to remove nonexistent path [" + path + 
                            "] from list of registered paths ["+l2+"] for id "+id+". All paths: "+this.paths); // NOI18N
                }
                if (removed != null && !removed.contains(path) && !l2.contains(path)) {
                    removed.add(path);
                }
                if (!l2.contains(path)) {
                    path.removePropertyChangeListener(classpathListener);
                }
            }
            this.paths.put(id, l2);
            if (removed != null && !removed.isEmpty()) {
                _listeners = listeners.toArray(new GlobalPathRegistryListener[listeners.size()]);
                evt = new GlobalPathRegistryEvent(this, id, Collections.unmodifiableSet(removed));
            }
            resetSourceRootsCache ();
        }
        if (_listeners != null) {
            assert evt != null;
            for (GlobalPathRegistryListener listener : _listeners) {
                listener.pathsRemoved(evt);
            }
        }
    }
    
    /**
     * Add a listener to the registry.
     * @param l a listener to add
     */
    public synchronized void addGlobalPathRegistryListener(GlobalPathRegistryListener l) {
        if (l == null) {
            throw new NullPointerException();
        }
        listeners.add(l);
    }
    
    /**
     * Remove a listener to the registry.
     * @param l a listener to remove
     */
    public synchronized void removeGlobalPathRegistryListener(GlobalPathRegistryListener l) {
        if (l == null) {
            throw new NullPointerException();
        }
        listeners.remove(l);
    }
    
    /**
     * Convenience method to find all relevant source roots.
     * This consists of:
     * <ol>
     * <li>Roots of all registered {@link ClassPath#SOURCE} paths.
     * <li>Sources (according to {@link SourceForBinaryQuery}) of all registered
     *     {@link ClassPath#COMPILE} paths.
     * <li>Sources of all registered {@link ClassPath#BOOT} paths.
     * </ol>
     * Order is not significant.
     * <p>
     * Currently there is no reliable way to listen for changes in the
     * value of this method: while you can listen to changes in the paths
     * mentioned, it is possible for {@link SourceForBinaryQuery} results to
     * change. In the future a change listener might be added for the value
     * of the source roots.
     * </p>
     * <p>
     * Note that this method takes no account of package includes/excludes.
     * </p>
     * @return an immutable set of <code>FileObject</code> source roots
     */
    public Set<FileObject> getSourceRoots() {        
        int currentResetCount;
        Set<ClassPath> sourcePaths, compileAndBootPaths;
        synchronized (this) {
            if (this.sourceRoots != null) {
                return this.sourceRoots;
            }            
            currentResetCount = this.resetCount;
            sourcePaths = getPaths(ClassPath.SOURCE);
            compileAndBootPaths = new LinkedHashSet<ClassPath>(getPaths(ClassPath.COMPILE));
            compileAndBootPaths.addAll(getPaths(ClassPath.BOOT));
        }
        
        Set<FileObject> newSourceRoots = new LinkedHashSet<FileObject>();
        for (ClassPath sp : sourcePaths) {
            newSourceRoots.addAll(Arrays.asList(sp.getRoots()));
        }
        
        final List<SourceForBinaryQuery.Result> newResults = new LinkedList<SourceForBinaryQuery.Result> ();
        final ChangeListener tmpResultListener = new SFBQListener ();
        for (ClassPath cp : compileAndBootPaths) {
            for (ClassPath.Entry entry : cp.entries()) {
                SourceForBinaryQuery.Result result = SourceForBinaryQuery.findSourceRoots(entry.getURL());
                result.addChangeListener(tmpResultListener);
                newResults.add (result);
                FileObject[] someRoots = result.getRoots();
                newSourceRoots.addAll(Arrays.asList(someRoots));
            }
        }
        
        newSourceRoots = Collections.unmodifiableSet(newSourceRoots);        
        synchronized (this) {
            if (this.resetCount == currentResetCount) {
                this.sourceRoots = newSourceRoots;
                removeTmpSFBQListeners (newResults, tmpResultListener, true);
                this.results.addAll (newResults);
            }
            else {
                removeTmpSFBQListeners (newResults, tmpResultListener, false);
            }
            return newSourceRoots;
        }        
    }
    
    
    private void removeTmpSFBQListeners (List<? extends SourceForBinaryQuery.Result> results, ChangeListener listener, boolean addListener) {
        for (SourceForBinaryQuery.Result res : results) {
            if (addListener) {
                res.addChangeListener (this.resultListener);
            }
            res.removeChangeListener(listener);
        }
    }
    
    /**
     * Convenience method to find a particular source file by resource path.
     * This simply uses {@link #getSourceRoots} to find possible roots and
     * looks up the resource among them.
     * In case more than one source root contains the resource, one is chosen
     * arbitrarily.
     * As with {@link ClassPath#findResource}, include/exclude lists can affect the result.
     * @param resource a resource path, e.g. <samp>somepkg/Foo.java</samp>
     * @return some file found with that path, or null
     */
    public FileObject findResource(String resource) {
        for (ClassPath cp : getPaths(ClassPath.SOURCE)) {
            FileObject f = cp.findResource(resource);
            if (f != null) {
                return f;
            }
        }
        for (FileObject root : getSourceRoots()) {
            FileObject f = root.getFileObject(resource);
            if (f != null) {
                // Make sure it is not from one of the above, since they control incl/excl.
                for (ClassPath cp : getPaths(ClassPath.SOURCE)) {
                    if (cp.findOwnerRoot(f) != null) {
                        return null;
                    }
                }
                return f;
            }
        }
        return null;
    }
    
    
    private synchronized void resetSourceRootsCache () {
        this.sourceRoots = null;
        for (Iterator< ? extends SourceForBinaryQuery.Result>  it = results.iterator(); it.hasNext();) {
            SourceForBinaryQuery.Result result = it.next();
            it.remove();
            result.removeChangeListener(this.resultListener);
        }
        this.resetCount++;
    }
    
    private class SFBQListener implements ChangeListener {
        
        public void stateChanged (ChangeEvent event) {
            synchronized (GlobalPathRegistry.this) {
                //Reset cache
                GlobalPathRegistry.this.resetSourceRootsCache ();
            }
        }
    };
    
    /**
     * Testability
     * Used by unit GlobalPathRegistryTest
     * @return set of {@link SourceForBinaryQuery.Result} the {@link GlobalPathRegistry}
     * listens on.
     */
    Set<? extends SourceForBinaryQuery.Result> getResults () {
        return Collections.unmodifiableSet(this.results);
    }

}
