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

package org.netbeans.api.project;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 * Manages loaded projects.
 * @author Jesse Glick
 */
public final class ProjectManager {
    
    // XXX need to figure out how to convince the system that a Project object is modified
    // so that Save All and the exit dialog work... could temporarily use a DataLoader
    // which recognizes project dirs and gives them a SaveCookie, perhaps
    // see also #36280
    // (but currently customizers always save the project on exit, so not so high priority)
    
    // XXX change listeners?
    
    private static final Lookup.Result/*<ProjectFactory>*/ factories =
        Lookup.getDefault().lookup(new Lookup.Template(ProjectFactory.class));
    
    private ProjectManager() {
        factories.addLookupListener(new LookupListener() {
            public void resultChanged(LookupEvent e) {
                synchronized (dir2Proj) {
                    dir2Proj.values().removeAll(Arrays.asList(new Object[] {
                        NO_SUCH_PROJECT,
                        SOME_SUCH_PROJECT,
                    }));
                    // XXX remove everything too? but then e.g. AntProjectFactorySingleton
                    // will stay while its delegates are changed, which does no good
                    // XXX should there be any way to signal that a particular
                    // folder should be "reloaded" by a new factory?
                }
            }
        });
    }
    
    private static final ProjectManager DEFAULT = new ProjectManager();

    /**
     * Returns the singleton project manager instance.
     */
    public static ProjectManager getDefault() {
        return DEFAULT;
    }
    
    private static final Mutex MUTEX = new Mutex();
    /**
     * Get a read/write lock to be used for all project metadata accesses.
     * All methods relating to recognizing and loading projects, saving them,
     * getting or setting their metadata, etc. should be controlled by this
     * mutex and be marked as read operations or write operations. Unless
     * otherwise stated, project-related methods automatically acquire the
     * mutex for you, so you do not necessarily need to pay attention to it;
     * but you may directly acquire the mutex in order to ensure that a block
     * of reads does not have any interspersed writes, or in order to ensure
     * that a write is not clobbering an unrelated write, etc.
     * @return a general read/write lock for project metadata operations of all sorts
     */
    public static Mutex mutex() {
        return MUTEX;
    }
    
    /**
     * Marker for a directory which is known to not be a project.
     */
    private static final Object NO_SUCH_PROJECT = "NO_SUCH_PROJECT"; // NOI18N
    /**
     * Marker for a directory which is known to (probably) be a project but is not loaded.
     */
    private static final Object SOME_SUCH_PROJECT = "SOME_SUCH_PROJECT"; // NOI18N
    /**
     * Marker for a directory which may currently be being loaded as a project.
     * When this is the value, other reader threads should wait for the result.
     */
    private static final Object LOADING_PROJECT = "LOADING_PROJECT"; // NOI18N
    
    /**
     * Cache of loaded projects (modified or not).
     * Also caches a dir which is <em>not</em> a project.
     */
    private final Map/*<FileObject,Reference<Project>|NO_SUCH_PROJECT|SOME_SUCH_PROJECT|LOADING_PROJECT>*/ dir2Proj = new WeakHashMap();
    
    /**
     * Set of modified projects (subset of loaded projects).
     */
    private final Set/*<Project>*/ modifiedProjects = new HashSet();
    
    /**
     * Mapping from projects to the factories that created them.
     */
    private final Map/*<Project,ProjectFactory>*/ proj2Factory = new WeakHashMap();
    
    /**
     * Clear internal state.
     * Useful from unit tests.
     */
    void reset() {
        dir2Proj.clear();
        modifiedProjects.clear();
        proj2Factory.clear();
    }
    
    /**
     * Find an open project corresponding to a given project directory.
     * Will be created in memory if necessary.
     * <p>Acquires read access.
     * @param projectDirectory the project top directory
     * @return the project (object identity may or may not vary between calls)
     *         or null if the directory is not recognized as a project by any
     *         registered {@link ProjectFactory}
     *         (might be null even if {@link #isProject} returns true)
     * @throws IOException if the project was recognized but could not be loaded
     */
    public Project findProject(final FileObject projectDirectory) throws IOException {
        try {
            return (Project)mutex().readAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    // Read access, but still needs to synch on the cache since there
                    // may be >1 reader.
                    synchronized (dir2Proj) {
                        Object o;
                        do {
                            o = dir2Proj.get(projectDirectory);
                            if (o == LOADING_PROJECT) {
                                try {
                                    dir2Proj.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        } while (o == LOADING_PROJECT);
                        assert o != LOADING_PROJECT;
                        if (o == NO_SUCH_PROJECT) {
                            return null;
                        } else if (o != null && o != SOME_SUCH_PROJECT) {
                            Reference r = (Reference)o;
                            Project p = (Project)r.get();
                            if (p != null) {
                                return p;
                            }
                        }
                        // not in cache
                        dir2Proj.put(projectDirectory, LOADING_PROJECT);
                    }
                    boolean resetLP = false;
                    try {
                        Project p = createProject(projectDirectory);
                        synchronized (dir2Proj) {
                            dir2Proj.notifyAll();
                            if (p != null) {
                                dir2Proj.put(projectDirectory, new SoftReference(p));
                                resetLP = true;
                                return p;
                            } else {
                                dir2Proj.put(projectDirectory, NO_SUCH_PROJECT);
                                resetLP = true;
                                return null;
                            }
                        }
                    } catch (IOException e) {
                        // Do not cache the exception. Might be useful in some cases
                        // but would also cause problems if there were a project that was
                        // temporarily corrupted, fP is called, then it is fixed, then fP is
                        // called again (without anything being GC'd)
                        throw e;
                    } finally {
                        if (!resetLP) {
                            // IOException or a runtime exception interrupted.
                            assert dir2Proj.get(projectDirectory) == LOADING_PROJECT;
                            dir2Proj.remove(projectDirectory);
                        }
                    }
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
    }
    
    /**
     * Create a project from a given directory.
     * @param dir the project dir
     * @return a project made from it, or null if it is not recognized
     * @throws IOException if there was a problem loading the project
     */
    private Project createProject(FileObject dir) throws IOException {
        assert dir != null;
        assert dir.isFolder();
        //assert mutex().canRead();
        ProjectStateImpl state = new ProjectStateImpl();
        Iterator it = factories.allInstances().iterator();
        while (it.hasNext()) {
            ProjectFactory factory = (ProjectFactory)it.next();
            Project p = factory.loadProject(dir, state);
            if (p != null) {
                proj2Factory.put(p, factory);
                state.attach(p);
                return p;
            }
        }
        return null;
    }
    
    /**
     * Check whether a given directory is likely to contain a project without
     * actually loading it.
     * Should be faster and use less memory than {@link #findProject} when called
     * on a large number of directories.
     * <p>The result is not guaranteed to be accurate; there may be false positives
     * (directories for which <code>isProject</code> is true but {@link #findProject}
     * will return false), for example if there is trouble loading the project.
     * False negatives are possible only if there are bugs in the project factory.</p>
     * <p>Acquires read access.</p>
     * @param projectDirectory a directory which may be some project's top directory
     * @return true if the directory is likely to contain a project according to
     *              some registered {@link ProjectFactory}
     */
    public boolean isProject(final FileObject projectDirectory) {
        return ((Boolean)mutex().readAccess(new Mutex.Action() {
            public Object run() {
                synchronized (dir2Proj) {
                    Object o;
                    do {
                        o = dir2Proj.get(projectDirectory);
                        if (o == LOADING_PROJECT) {
                            try {
                                dir2Proj.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } while (o == LOADING_PROJECT);
                    assert o != LOADING_PROJECT;
                    if (o == NO_SUCH_PROJECT) {
                        return Boolean.FALSE;
                    } else if (o != null) {
                        // Reference<Project> or SOME_SUCH_PROJECT
                        return Boolean.TRUE;
                    }
                    // Not in cache.
                    dir2Proj.put(projectDirectory, LOADING_PROJECT);
                }
                boolean resetLP = false;
                try {
                    boolean p = checkForProject(projectDirectory);
                    synchronized (dir2Proj) {
                        resetLP = true;
                        dir2Proj.notifyAll();
                        if (p) {
                            dir2Proj.put(projectDirectory, SOME_SUCH_PROJECT);
                            return Boolean.TRUE;
                        } else {
                            dir2Proj.put(projectDirectory, NO_SUCH_PROJECT);
                            return Boolean.FALSE;
                        }
                    }
                } finally {
                    if (!resetLP) {
                        // some runtime exception interrupted.
                        assert dir2Proj.get(projectDirectory) == LOADING_PROJECT;
                        dir2Proj.remove(projectDirectory);
                    }
                }
            }
        })).booleanValue();
    }
    
    private boolean checkForProject(FileObject dir) {
        assert dir != null;
        assert dir.isFolder();
        //assert mutex().canRead();
        Iterator it = factories.allInstances().iterator();
        while (it.hasNext()) {
            ProjectFactory factory = (ProjectFactory)it.next();
            if (factory.isProject(dir)) {
                return true;
            }
        }
        return false;
    }
    
    private final class ProjectStateImpl implements ProjectState {
        
        private Project p;
        
        void attach(Project p) {
            assert p != null;
            assert this.p == null;
            this.p = p;
        }
        
        public void markModified() {
            assert p != null;
            mutex().writeAccess(new Mutex.Action() {
                public Object run() {
                    assert proj2Factory.get(p) != null;
                    modifiedProjects.add(p);
                    return null;
                }
            });
        }
        
    }
    
    /**
     * Get a list of all projects which are modified and need to be saved.
     * <p>Acquires read access.
     * @return an immutable set of {@link Project}s
     */
    public Set/*<Project>*/ getModifiedProjects() {
        return (Set/*<Project>*/)mutex().readAccess(new Mutex.Action() {
            public Object run() {
                return new HashSet(modifiedProjects);
            }
        });
    }
    
    /**
     * Check whether a given project is current modified.
     * <p>Acquires read access.
     * @param p a project loaded by this manager
     * @throws IllegalArgumentException if the project was not created through this manager
     */
    public boolean isModified(final Project p) throws IllegalArgumentException {
        return ((Boolean)mutex().readAccess(new Mutex.Action() {
            public Object run() {
                synchronized (dir2Proj) {
                    if (!proj2Factory.containsKey(p)) {
                        throw new IllegalArgumentException("Project " + p + " not created by " + ProjectManager.this); // NOI18N
                    }
                }
                return Boolean.valueOf(modifiedProjects.contains(p));
            }
        })).booleanValue();
    }
    
    /**
     * Save one project (if it was in fact modified).
     * <p>Acquires write access.</p>
     * <p class="nonnormative">
     * Although the project infrastructure permits a modified project to be saved
     * at any time, current UI principles dictate that the "save project" concept
     * should be internal only - i.e. a project customizer should automatically
     * save the project when it is closed e.g. with an "OK" button. Currently there
     * is no UI display of modified projects nor it is ensured that modified projects
     * are saved at system exit time the way modified files are.
     * </p>
     * @param p the project to save
     * @throws IOException if it cannot be saved
     * @throws IllegalArgumentException if the project was not created through this manager
     */
    public void saveProject(final Project p) throws IOException, IllegalArgumentException {
        try {
            mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    if (!proj2Factory.containsKey(p)) {
                        throw new IllegalArgumentException("Project " + p + " not created by " + ProjectManager.this); // NOI18N
                    }
                    if (modifiedProjects.contains(p)) {
                        ProjectFactory f = (ProjectFactory)proj2Factory.get(p);
                        f.saveProject(p);
                        modifiedProjects.remove(p);
                    }
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
    }
    
    /**
     * Save all modified projects.
     * <p>Acquires write access.
     * @throws IOException if any of them cannot be saved
     */
    public void saveAllProjects() throws IOException {
        try {
            mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    Iterator it = modifiedProjects.iterator();
                    while (it.hasNext()) {
                        Project p = (Project)it.next();
                        ProjectFactory f = (ProjectFactory)proj2Factory.get(p);
                        f.saveProject(p);
                        it.remove();
                    }
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
    }
    
}
