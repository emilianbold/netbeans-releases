/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.java.classpath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.openide.filesystems.FileObject;

// XXX do not use nested public classes (Listener & Event)

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
 * Intended usage pattern:
 * </p>
 * <ol>
 * <li><p>When a project is opened using
 * {@link org.netbeans.spi.project.ui.ProjectOpenedHook} it should add any paths
 * it defines or uses. When closed it should remove them.</p></li>
 * <li><p>The <b>Fast&nbsp;Open</b> feature of the editor should operate on sources
 * listed in open paths of type {@link ClassPath#SOURCE}, plus perhaps sources
 * corresponding to paths of type {@link ClassPath#COMPILE} (as determined by
 * {@link org.netbeans.api.java.queries.SourceForBinaryQuery}), plus perhaps
 * sources corresponding to paths of type {@link ClassPath#BOOT}.</p></li>
 * <li><p>The editor code responsible for updating code completion databases may
 * initiate parses of source paths when they are opened (which will typically
 * also trigger dependent parses of class paths).</p></li>
 * <li><p>The <b>Javadoc&nbsp;Index&nbsp;Search</b> feature and <b>View&nbsp;&#8594;
 * Documentation&nbsp;Indices</b> submenu should operate on open Javadoc paths.</p></li>
 * <li><p>Stack trace hyperlinking can use the global list of source paths
 * to find sources (in case no more specific information about their origin is
 * available). Compile and boot paths can be used too in conjunction with a
 * source for binary query.</p></li>
 * </ol>
 * </div>
 * @author Jesse Glick
 * @since org.netbeans.api.java/1 1.4
 */
public final class GlobalPathRegistry {
    
    private static GlobalPathRegistry DEFAULT = new GlobalPathRegistry();
    
    /**
     * Get the singleton instance of the registry.
     * @return the default instance
     */
    public static GlobalPathRegistry getDefault() {
        return DEFAULT;
    }
    
    private final Map/*<String,List<ClassPath>>*/ paths = new HashMap();
    private final List/*<PathRegistryListener>*/ listeners = new ArrayList();
    private Set/*<FileObject>*/ sourceRoots = null;
    
    private GlobalPathRegistry() {}
    
    /** for use from unit test */
    void clear() {
        paths.clear();
        listeners.clear();
    }
    
    /**
     * Find all paths of a certain type.
     * @param id a classpath type, e.g. {@link ClassPath#SOURCE}
     * @return an immutable set of all registered {@link ClassPath}s of that type (may be empty but not null)
     */
    public synchronized Set/*<ClassPath>*/ getPaths(String id) {
        if (id == null) {
            throw new NullPointerException();
        }
        List l = (List)paths.get(id);
        if (l != null && !l.isEmpty()) {
            return Collections.unmodifiableSet(new HashSet(l));
        } else {
            return Collections.EMPTY_SET;
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
        GlobalPathRegistryEvent evt = null;
        GlobalPathRegistryListener[] _listeners = null;
        synchronized (this) {
            List l = (List)this.paths.get(id);
            if (l == null) {
                l = new ArrayList();
                this.paths.put(id, l);
            }
            Set/*<ClassPath>*/ added = listeners.isEmpty() ? null : new HashSet();
            for (int i = 0; i < paths.length; i++) {
                if (paths[i] == null) {
                    throw new NullPointerException("Null path encountered in " + Arrays.asList(paths) + " of type " + id); // NOI18N
                }
                if (added != null && !added.contains(paths[i]) && !l.contains(paths[i])) {
                    added.add(paths[i]);
                }
                l.add(paths[i]);
            }
            if (added != null && !added.isEmpty()) {
                _listeners = (GlobalPathRegistryListener[])listeners.toArray(new GlobalPathRegistryListener[listeners.size()]);
                evt = new GlobalPathRegistryEvent(this, id, Collections.unmodifiableSet(added));
            }
            // Invalidate cache for getSourceRoots and findResource:
            sourceRoots = null;
        }
        if (_listeners != null) {
            assert evt != null;
            for (int i = 0; i < _listeners.length; i++) {
                _listeners[i].pathsAdded(evt);
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
        if (id == null || paths == null) {
            throw new NullPointerException();
        }
        GlobalPathRegistryEvent evt = null;
        GlobalPathRegistryListener[] _listeners = null;
        synchronized (this) {
            List l = (List)this.paths.get(id);
            if (l == null) {
                l = new ArrayList();
            }
            List l2 = new ArrayList(l); // in case IAE thrown below
            Set/*<ClassPath>*/ removed = listeners.isEmpty() ? null : new HashSet();
            for (int i = 0; i < paths.length; i++) {
                if (paths[i] == null) {
                    throw new NullPointerException();
                }
                if (!l2.remove(paths[i])) {
                    throw new IllegalArgumentException("Attempt to remove nonexistent path " + paths[i]); // NOI18N
                }
                if (removed != null && !removed.contains(paths[i]) && !l2.contains(paths[i])) {
                    removed.add(paths[i]);
                }
            }
            this.paths.put(id, l2);
            if (removed != null && !removed.isEmpty()) {
                _listeners = (GlobalPathRegistryListener[])listeners.toArray(new GlobalPathRegistryListener[listeners.size()]);
                evt = new GlobalPathRegistryEvent(this, id, Collections.unmodifiableSet(removed));
            }
            sourceRoots = null;
        }
        if (_listeners != null) {
            assert evt != null;
            for (int i = 0; i < _listeners.length; i++) {
                _listeners[i].pathsRemoved(evt);
            }
        }
    }
    
    /**
     * Add a listener to the registry.
     * @param l a listener to add
     */
    public synchronized void addPathRegistryListener(GlobalPathRegistryListener l) {
        if (l == null) {
            throw new NullPointerException();
        }
        listeners.add(l);
    }
    
    /**
     * Remove a listener to the registry.
     * @param l a listener to remove
     */
    public synchronized void removePathRegistryListener(GlobalPathRegistryListener l) {
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
     * mentioned, it is possible or {@link SourceForBinaryQuery} results to
     * change, which you would not notice. XXX use new listening features of
     * SourceForBinaryQuery to implement proper change firing.
     * @return an immutable set of <code>FileObject</code> source roots
     */
    public synchronized Set/*<FileObject>*/ getSourceRoots() {
        if (sourceRoots == null) {
            sourceRoots = new LinkedHashSet();
            Iterator it = getPaths(ClassPath.SOURCE).iterator();
            while (it.hasNext()) {
                ClassPath sp = (ClassPath)it.next();
                sourceRoots.addAll(Arrays.asList(sp.getRoots()));
            }
            Set/*<ClassPath>*/ compileAndBootPaths = new LinkedHashSet(getPaths(ClassPath.COMPILE));
            compileAndBootPaths.addAll(getPaths(ClassPath.BOOT));
            it = compileAndBootPaths.iterator();
            while (it.hasNext()) {
                ClassPath cp = (ClassPath)it.next();
                Iterator it2 = cp.entries().iterator();
                while (it2.hasNext()) {
                    ClassPath.Entry entry = (ClassPath.Entry)it2.next();
                    FileObject[] someRoots = SourceForBinaryQuery.findSourceRoot(entry.getURL());
                    // XXX it seems that currently PlatformSourceForBinaryQuery
                    // misbehaves and returns src.zip, not its root... so currently
                    // stack trace hyperlinking does not always work for JRE classes
                    sourceRoots.addAll(Arrays.asList(someRoots));
                }
            }
        }
        return sourceRoots;
    }
    
    /**
     * Convenience method to find a particular source file by resource path.
     * This simply uses {@link #getSourceRoots} to find possible roots and
     * looks up the resource among them.
     * In case more than one source root contains the resource, one is chosen
     * arbitrarily.
     * @param resource a resource path, e.g. <samp>somepkg/Foo.java</samp>
     * @return some file found with that path, or null
     */
    public FileObject findResource(String resource) {
        Iterator it = getSourceRoots().iterator();
        while (it.hasNext()) {
            FileObject root = (FileObject)it.next();
            FileObject f = root.getFileObject(resource);
            if (f != null) {
                return f;
            }
        }
        return null;
    }

}
