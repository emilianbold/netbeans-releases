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

package org.netbeans.api.project;

import java.io.IOException;
import java.lang.ref.Reference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.projectapi.TimedWeakReference;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.Union2;
import org.openide.util.WeakSet;

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
    
    private static final Logger LOG = Logger.getLogger(ProjectManager.class.getName());
    
    private static final Lookup.Result<ProjectFactory> factories =
        Lookup.getDefault().lookupResult(ProjectFactory.class);
    
    private ProjectManager() {
        factories.addLookupListener(new LookupListener() {
            public void resultChanged(LookupEvent e) {
                clearNonProjectCache();
            }
        });
    }
    
    private static final ProjectManager DEFAULT = new ProjectManager();

    /**
     * Returns the singleton project manager instance.
     * @return the default instance
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
    
    private static enum LoadStatus {
        /**
         * Marker for a directory which is known to not be a project.
         */
        NO_SUCH_PROJECT,
        /**
         * Marker for a directory which is known to (probably) be a project but is not loaded.
         */
        SOME_SUCH_PROJECT,
        /**
         * Marker for a directory which may currently be being loaded as a project.
         * When this is the value, other reader threads should wait for the result.
         */
        LOADING_PROJECT;
        
        public boolean is(Union2<Reference<Project>,LoadStatus> o) {
            return o != null && o.hasSecond() && o.second() == this;
        }
        
        public Union2<Reference<Project>,LoadStatus> wrap() {
            return Union2.createSecond(this);
        }
    }
    
    /**
     * Cache of loaded projects (modified or not).
     * Also caches a dir which is <em>not</em> a project.
     */
    private final Map<FileObject,Union2<Reference<Project>,LoadStatus>> dir2Proj = new WeakHashMap<FileObject,Union2<Reference<Project>,LoadStatus>>();
    
    /**
     * Set of modified projects (subset of loaded projects).
     */
    private final Set<Project> modifiedProjects = new HashSet<Project>();
    
    private final Set<Project> removedProjects = new WeakSet<Project>();
    
    /**
     * Mapping from projects to the factories that created them.
     */
    private final Map<Project,ProjectFactory> proj2Factory = new WeakHashMap<Project,ProjectFactory>();
    
    /**
     * Checks for deleted projects.
     */
    private final FileChangeListener projectDeletionListener = new ProjectDeletionListener();
    
    /**
     * The thread which is currently loading a project, if any.
     */
    private ThreadLocal<Boolean> loadingThread = new ThreadLocal<Boolean>();
    
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
     * <p>
     * Acquires read access.
     * </p>
     * <p>
     * It is <em>not</em> guaranteed that the returned instance will be identical
     * to that which is created by the appropriate {@link ProjectFactory}. In
     * particular, the project manager is free to return only wrapper <code>Project</code>
     * instances which delegate to the factory's implementation. If you know your
     * factory created a particular project, you cannot safely cast the return value
     * of this method to your project type implementation class; you should instead
     * place an implementation of some suitable private interface into your project's
     * lookup, which would be safely proxied.
     * </p>
     * @param projectDirectory the project top directory
     * @return the project (object identity may or may not vary between calls)
     *         or null if the directory is not recognized as a project by any
     *         registered {@link ProjectFactory}
     *         (might be null even if {@link #isProject} returns true)
     * @throws IOException if the project was recognized but could not be loaded
     * @throws IllegalArgumentException if the supplied file object is null or not a folder
     */
    public Project findProject(final FileObject projectDirectory) throws IOException, IllegalArgumentException {
        if (projectDirectory == null) {
            throw new IllegalArgumentException("Attempted to pass a null directory to findProject"); // NOI18N
        }
        if (!projectDirectory.isFolder()) {
            throw new IllegalArgumentException("Attempted to pass a non-directory to findProject: " + projectDirectory); // NOI18N
        }
        try {
            return mutex().readAccess(new Mutex.ExceptionAction<Project>() {
                public Project run() throws IOException {
                    // Read access, but still needs to synch on the cache since there
                    // may be >1 reader.
                    try {
                        boolean wasSomeSuchProject;
                    synchronized (dir2Proj) {
                        Union2<Reference<Project>,LoadStatus> o;
                        do {
                            o = dir2Proj.get(projectDirectory);
                            if (LoadStatus.LOADING_PROJECT.is(o)) {
                                try {
                                    if (Boolean.TRUE.equals(loadingThread.get())) {
                                        throw new IllegalStateException("Attempt to call ProjectManager.findProject within the body of ProjectFactory.loadProject (hint: try using ProjectManager.mutex().postWriteRequest(...) within the body of your Project's constructor to prevent this)"); // NOI18N
                                    }
                                    LOG.log(Level.FINE, "findProject({0}) in {1}: waiting for LOADING_PROJECT...", new Object[] {projectDirectory, Thread.currentThread().getName()});
                                    dir2Proj.wait();
                                    LOG.log(Level.FINE, "findProject({0}) in {1}: ...done waiting for LOADING_PROJECT", new Object[] {projectDirectory, Thread.currentThread().getName()});
                                } catch (InterruptedException e) {
                                    LOG.log(Level.WARNING, null, e);
                                }
                            }
                        } while (LoadStatus.LOADING_PROJECT.is(o));
                        assert !LoadStatus.LOADING_PROJECT.is(o);
                        wasSomeSuchProject = LoadStatus.SOME_SUCH_PROJECT.is(o);
                        if (LoadStatus.NO_SUCH_PROJECT.is(o)) {
                            LOG.log(Level.FINE, "findProject({0}) in {1}: NO_SUCH_PROJECT", new Object[] {projectDirectory, Thread.currentThread().getName()});
                            return null;
                        } else if (o != null && !LoadStatus.SOME_SUCH_PROJECT.is(o)) {
                            Project p = o.first().get();
                            if (p != null) {
                                LOG.log(Level.FINE, "findProject({0}) in {1}: cached project", new Object[] {projectDirectory, Thread.currentThread().getName()});
                                return p;
                            }
                        }
                        // not in cache
                        dir2Proj.put(projectDirectory, LoadStatus.LOADING_PROJECT.wrap());
                        loadingThread.set(Boolean.TRUE);
                        LOG.log(Level.FINE, "findProject({0}) in {1}: will load new project...", new Object[] {projectDirectory, Thread.currentThread().getName()});
                    }
                    boolean resetLP = false;
                    try {
                        Project p = createProject(projectDirectory);
                        LOG.log(Level.FINE, "findProject({0}) in {1}: created new project", new Object[] {projectDirectory, Thread.currentThread().getName()});
                        //Thread.dumpStack();
                        synchronized (dir2Proj) {
                            dir2Proj.notifyAll();
                            projectDirectory.addFileChangeListener(projectDeletionListener);
                            if (p != null) {
                                dir2Proj.put(projectDirectory, Union2.<Reference<Project>,LoadStatus>createFirst(new TimedWeakReference<Project>(p)));
                                resetLP = true;
                                return p;
                            } else {
                                dir2Proj.put(projectDirectory, LoadStatus.NO_SUCH_PROJECT.wrap());
                                resetLP = true;
                                if (wasSomeSuchProject) {
                                    LOG.log(Level.FINE, "Directory {0} was initially claimed to be a project folder but really was not", FileUtil.getFileDisplayName(projectDirectory));
                                }
                                return null;
                            }
                        }
                    } catch (IOException e) {
                        LOG.log(Level.FINE, "findProject({0}) in {1}: error loading project: {2}", new Object[] {projectDirectory, Thread.currentThread().getName(), e});
                        // Do not cache the exception. Might be useful in some cases
                        // but would also cause problems if there were a project that was
                        // temporarily corrupted, fP is called, then it is fixed, then fP is
                        // called again (without anything being GC'd)
                        throw e;
                    } finally {
                        loadingThread.set(Boolean.FALSE);
                        if (!resetLP) {
                            // IOException or a runtime exception interrupted.
                            LOG.log(Level.FINE, "findProject({0}) in {1}: cleaning up after error", new Object[] {projectDirectory, Thread.currentThread().getName()});
                            synchronized (dir2Proj) {
                                assert LoadStatus.LOADING_PROJECT.is(dir2Proj.get(projectDirectory));
                                dir2Proj.remove(projectDirectory);
                                dir2Proj.notifyAll(); // make sure other threads can continue
                            }
                        }
                    }
    // Workaround for issue #51911:
    // Log project creation exception here otherwise it can get lost
    // in following scenario:
    // If project creation calls ProjectManager.postWriteRequest() (what for 
    // example FreeformSources.initSources does) and then it throws an 
    // exception then this exception can get lost because leaving read mutex
    // will immediately execute the runnable posted by 
    // ProjectManager.postWriteRequest() and if this runnable fails (what
    // for FreeformSources.initSources will happen because
    // AntBasedProjectFactorySingleton.getProjectFor() will not find project in
    // its helperRef cache) then only this second fail is logged, but the cause - 
    // the failure to create project - is never logged. So, better log it here:
                    } catch (Error e) {
                        LOG.log(Level.FINE, null, e);
                        throw e;
                    } catch (RuntimeException e) {
                        LOG.log(Level.FINE, null, e);
                        throw e;
                    } catch (IOException e) {
                        LOG.log(Level.FINE, null, e);
                        throw e;
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
        assert mutex().isReadAccess();
        ProjectStateImpl state = new ProjectStateImpl();
        for (ProjectFactory factory : factories.allInstances()) {
            Project p = factory.loadProject(dir, state);
            if (p != null) {
                Logger.getLogger("TIMER").log(Level.FINE, "Project: {0}", p);
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
     * <p class="nonnormative">
     * You do <em>not</em> need to call this method if you just plan to call {@link #findProject}
     * afterwards. It is intended for only those clients which would discard the
     * result of {@link #findProject} other than to check for null, and which
     * can also tolerate false positives.
     * </p>
     * @param projectDirectory a directory which may be some project's top directory
     * @return true if the directory is likely to contain a project according to
     *              some registered {@link ProjectFactory}
     * @throws IllegalArgumentException if the supplied file object is null or not a folder
     */
    public boolean isProject(final FileObject projectDirectory) throws IllegalArgumentException {
        if (projectDirectory == null) {
            throw new IllegalArgumentException("Attempted to pass a null directory to isProject"); // NOI18N
        }
        if (!projectDirectory.isFolder() ) {
            //#78215 it can happen that a no longer existing folder is queried. throw 
            // exception only for real wrong usage..
            if (projectDirectory.isValid()) {
                throw new IllegalArgumentException("Attempted to pass a non-directory to isProject: " + projectDirectory); // NOI18N
            } else {
                return false;
            }
        }
        return mutex().readAccess(new Mutex.Action<Boolean>() {
            public Boolean run() {
                synchronized (dir2Proj) {
                    Union2<Reference<Project>,LoadStatus> o;
                    do {
                        o = dir2Proj.get(projectDirectory);
                        if (LoadStatus.LOADING_PROJECT.is(o)) {
                            try {
                                dir2Proj.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } while (LoadStatus.LOADING_PROJECT.is(o));
                    assert !LoadStatus.LOADING_PROJECT.is(o);
                    if (LoadStatus.NO_SUCH_PROJECT.is(o)) {
                        return false;
                    } else if (o != null) {
                        // Reference<Project> or SOME_SUCH_PROJECT
                        return true;
                    }
                    // Not in cache.
                    dir2Proj.put(projectDirectory, LoadStatus.LOADING_PROJECT.wrap());
                }
                boolean resetLP = false;
                try {
                    boolean p = checkForProject(projectDirectory);
                    synchronized (dir2Proj) {
                        resetLP = true;
                        dir2Proj.notifyAll();
                        if (p) {
                            dir2Proj.put(projectDirectory, LoadStatus.SOME_SUCH_PROJECT.wrap());
                            return true;
                        } else {
                            dir2Proj.put(projectDirectory, LoadStatus.NO_SUCH_PROJECT.wrap());
                            return false;
                        }
                    }
                } finally {
                    if (!resetLP) {
                        // some runtime exception interrupted.
                        assert LoadStatus.LOADING_PROJECT.is(dir2Proj.get(projectDirectory));
                        dir2Proj.remove(projectDirectory);
                    }
                }
            }
        });
    }
    
    private boolean checkForProject(FileObject dir) {
        assert dir != null;
        assert dir.isFolder() : dir;
        assert mutex().isReadAccess();
        Iterator it = factories.allInstances().iterator();
        while (it.hasNext()) {
            ProjectFactory factory = (ProjectFactory)it.next();
            if (factory.isProject(dir)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Clear the cached list of folders thought <em>not</em> to be projects.
     * This may be useful after creating project metadata in a folder, etc.
     * Cached project objects, i.e. folders that <em>are</em> known to be
     * projects, are not affected.
     */
    public void clearNonProjectCache() {
        synchronized (dir2Proj) {
            dir2Proj.values().removeAll(Arrays.asList(new Object[] {
                LoadStatus.NO_SUCH_PROJECT.wrap(),
                LoadStatus.SOME_SUCH_PROJECT.wrap(),
            }));
            // XXX remove everything too? but then e.g. AntProjectFactorySingleton
            // will stay while its delegates are changed, which does no good
            // XXX should there be any way to signal that a particular
            // folder should be "reloaded" by a new factory?
        }
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
            LOG.log(Level.FINE, "markModified({0})", p.getProjectDirectory());
            mutex().writeAccess(new Mutex.Action<Void>() {
                public Void run() {
                    if (!proj2Factory.containsKey(p)) {
                        throw new IllegalStateException("An attempt to call ProjectState.markModified on a deleted project: " + p.getProjectDirectory()); // NOI18N
                    }
                    modifiedProjects.add(p);
                    return null;
                }
            });
        }

        public void notifyDeleted() throws IllegalStateException {
            assert p != null;
            mutex().writeAccess(new Mutex.Action<Void>() {
                public Void run() {
                    if (proj2Factory.get(p) == null) {
                        throw new IllegalStateException("An attempt to call notifyDeleted more than once. Project: " + p.getProjectDirectory()); // NOI18N
                    }
                    
                    dir2Proj.remove(p.getProjectDirectory());
                    proj2Factory.remove(p);
                    modifiedProjects.remove(p);
                    removedProjects.add(p);
                    return null;
                }
            });
        }

    }
    
    /**
     * Get a list of all projects which are modified and need to be saved.
     * <p>Acquires read access.
     * @return an immutable set of projects
     */
    public Set<Project> getModifiedProjects() {
        return mutex().readAccess(new Mutex.Action<Set<Project>>() {
            public Set<Project> run() {
                return new HashSet<Project>(modifiedProjects);
            }
        });
    }
    
    /**
     * Check whether a given project is current modified.
     * <p>Acquires read access.
     * @param p a project loaded by this manager
     * @return true if it is modified, false if has been saved since the last modification
     * @throws IllegalArgumentException if the project was not created through this manager
     */
    public boolean isModified(final Project p) throws IllegalArgumentException {
        return mutex().readAccess(new Mutex.Action<Boolean>() {
            public Boolean run() {
                synchronized (dir2Proj) {
                    if (!proj2Factory.containsKey(p)) {
                        throw new IllegalArgumentException("Project " + p + " not created by " + ProjectManager.this + " or was already deleted"); // NOI18N
                    }
                }
                return modifiedProjects.contains(p);
            }
        });
    }
    
    /**
     * Save one project (if it was in fact modified).
     * <p>Acquires write access.</p>
     * <p class="nonnormative">
     * Although the project infrastructure permits a modified project to be saved
     * at any time, current UI principles dictate that the "save project" concept
     * should be internal only - i.e. a project customizer should automatically
     * save the project when it is closed e.g. with an "OK" button. Currently there
     * is no UI display of modified projects; this module does not ensure that modified projects
     * are saved at system exit time the way modified files are, though the Project UI
     * implementation module currently does this check.
     * </p>
     * @param p the project to save
     * @throws IOException if it cannot be saved
     * @throws IllegalArgumentException if the project was not created through this manager
     */
    public void saveProject(final Project p) throws IOException, IllegalArgumentException {
        try {
            mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
                    //removed projects are the ones that cannot be mapped to an existing project type anymore.
                    if (removedProjects.contains(p)) {
                        return null;
                    }
                    if (!proj2Factory.containsKey(p)) {
                        throw new IllegalArgumentException("Project " + p + " not created by " + ProjectManager.this + " or was already deleted"); // NOI18N
                    }
                    if (modifiedProjects.contains(p)) {
                        ProjectFactory f = proj2Factory.get(p);
                        f.saveProject(p);
                        LOG.log(Level.FINE, "saveProject({0})", p.getProjectDirectory());
                        modifiedProjects.remove(p);
                    }
                    return null;
                }
            });
        } catch (MutexException e) {
            //##91398 have a more descriptive error message, in case of RO folders.
            // the correct reporting still up to the specific project type.
            if (!p.getProjectDirectory().canWrite()) {
                throw new IOException("Project folder is not writeable.");
            }
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
            mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
                    Iterator<Project> it = modifiedProjects.iterator();
                    while (it.hasNext()) {
                        Project p = it.next();
                        ProjectFactory f = proj2Factory.get(p);
                        assert f != null : p;
                        f.saveProject(p);
                        LOG.log(Level.FINE, "saveProject({0})", p.getProjectDirectory());
                        it.remove();
                    }
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
    }
    
    /**
     * Checks whether a project is still valid.
     * <p>Acquires read access.</p>
     *
     * @since 1.6
     *
     * @param p a project loaded by this manager
     * @return true if the project is still valid, false if it has been deleted
     */
    public boolean isValid(final Project p) {
        return mutex().readAccess(new Mutex.Action<Boolean>() {
            public Boolean run() {
                synchronized (dir2Proj) {
                    return proj2Factory.containsKey(p);
                }
            }
        });
    }
    
    /**
     * Removes cache entries for deleted projects.
     */
    private final class ProjectDeletionListener extends FileChangeAdapter {
        
        public ProjectDeletionListener() {}

        public void fileDeleted(FileEvent fe) {
            synchronized (dir2Proj) {
                dir2Proj.remove(fe.getFile());
            }
        }
        
    }
    
}
