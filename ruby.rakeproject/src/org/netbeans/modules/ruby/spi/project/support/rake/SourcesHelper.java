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

package org.netbeans.modules.ruby.spi.project.support.rake;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.ruby.modules.project.rake.RakeBasedProjectFactorySingleton;
import org.netbeans.modules.ruby.modules.project.rake.FileChangeSupport;
import org.netbeans.modules.ruby.modules.project.rake.FileChangeSupportEvent;
import org.netbeans.modules.ruby.modules.project.rake.FileChangeSupportListener;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;

// XXX should perhaps be legal to call add* methods at any time (should update things)
// and perhaps also have remove* methods
// and have code names for each source dir?

// XXX should probably all be wrapped in ProjectManager.mutex

/**
 * Helper class to work with source roots and typed folders of a project.
 * @author Jesse Glick
 */
public final class SourcesHelper {
    
    private class Root {
        protected final String location;
        public Root(String location) {
            this.location = location;
        }
        public final File getActualLocation() {
            String val = evaluator.evaluate(location);
            if (val == null) {
                return null;
            }
            return project.resolveFile(val);
        }
    }
    
    private class SourceRoot extends Root {
        private final String displayName;
        private final Icon icon;
        private final Icon openedIcon;
        public SourceRoot(String location, String displayName, Icon icon, Icon openedIcon) {
            super(location);
            this.displayName = displayName;
            this.icon = icon;
            this.openedIcon = openedIcon;
        }
        public final SourceGroup toGroup(FileObject loc) {
            assert loc != null;
            return GenericSources.group(getProject(), loc, location.length() > 0 ? location : "generic", // NOI18N
                                        displayName, icon, openedIcon);
        }
    }
    
    private final class TypedSourceRoot extends SourceRoot {
        private final String type;
        public TypedSourceRoot(String type, String location, String displayName, Icon icon, Icon openedIcon) {
            super(location, displayName, icon, openedIcon);
            this.type = type;
        }
        public final String getType() {
            return type;
        }
    }
    
    private final RakeProjectHelper project;
    private final PropertyEvaluator evaluator;
    private final List<SourceRoot> principalSourceRoots = new ArrayList<SourceRoot>();
    private final List<Root> nonSourceRoots = new ArrayList<Root>();
    private final List<TypedSourceRoot> typedSourceRoots = new ArrayList<TypedSourceRoot>();
    private int registeredRootAlgorithm;
    /**
     * If not null, external roots that we registered the last time.
     * Used when a property change is encountered, to see if the set of external
     * roots might have changed. Hold the actual files (not e.g. URLs); see
     * {@link #registerExternalRoots} for the reason why.
     */
    private Set<FileObject> lastRegisteredRoots;
    private PropertyChangeListener propChangeL;
    
    /**
     * Create the helper object, initially configured to recognize only sources
     * contained inside the project directory.
     * @param project an Ant project helper
     * @param evaluator a way to evaluate Ant properties used to define source locations
     */
    public SourcesHelper(RakeProjectHelper project, PropertyEvaluator evaluator) {
        this.project = project;
        this.evaluator = evaluator;
    }
    
    /**
     * Add a possible principal source root, or top-level folder which may
     * contain sources that should be considered part of the project.
     * <p>
     * If the actual value of the location is inside the project directory,
     * this is simply ignored; so it safe to configure principal source roots
     * for any source directory which might be set to use an external path, even
     * if the common location is internal.
     * </p>
     * @param location a project-relative or absolute path giving the location
     *                 of a source tree; may contain Ant property substitutions
     * @param displayName a display name (for {@link SourceGroup#getDisplayName})
     * @param icon a regular icon for the source root, or null
     * @param openedIcon an opened variant icon for the source root, or null
     * @throws IllegalStateException if this method is called after either
     *                               {@link #createSources} or {@link #registerExternalRoots}
     *                               was called
     * @see #registerExternalRoots
     * @see Sources#TYPE_GENERIC
     */
    public void addPrincipalSourceRoot(String location, String displayName, Icon icon, Icon openedIcon) throws IllegalStateException {
        if (lastRegisteredRoots != null) {
            throw new IllegalStateException("registerExternalRoots was already called"); // NOI18N
        }
        principalSourceRoots.add(new SourceRoot(location, displayName, icon, openedIcon));
    }
    
    /**
     * Similar to {@link #addPrincipalSourceRoot} but affects only
     * {@link #registerExternalRoots} and not {@link #createSources}.
     * <p class="nonnormative">
     * Useful for project type providers which have external paths holding build
     * products. These should not appear in {@link Sources}, yet it may be useful
     * for {@link FileOwnerQuery} to know the owning project (for example, in order
     * for a project-specific {@link org.netbeans.spi.queries.SourceForBinaryQueryImplementation} to work).
     * </p>
     * @param location a project-relative or absolute path giving the location
     *                 of a non-source tree; may contain Ant property substitutions
     * @throws IllegalStateException if this method is called after
     *                               {@link #registerExternalRoots} was called
     */
    public void addNonSourceRoot(String location) throws IllegalStateException {
        if (lastRegisteredRoots != null) {
            throw new IllegalStateException("registerExternalRoots was already called"); // NOI18N
        }
        nonSourceRoots.add(new Root(location));
    }
    
    /**
     * Add a typed source root which will be considered only in certain contexts.
     * @param location a project-relative or absolute path giving the location
     *                 of a source tree; may contain Ant property substitutions
     * @param type a source root type such as <a href="@JAVA/PROJECT@/org/netbeans/api/gsfpath/project/JavaProjectConstants.html#SOURCES_TYPE_JAVA"><code>JavaProjectConstants.SOURCES_TYPE_JAVA</code></a>
     * @param displayName a display name (for {@link SourceGroup#getDisplayName})
     * @param icon a regular icon for the source root, or null
     * @param openedIcon an opened variant icon for the source root, or null
     * @throws IllegalStateException if this method is called after either
     *                               {@link #createSources} or {@link #registerExternalRoots}
     *                               was called
     */
    public void addTypedSourceRoot(String location, String type, String displayName, Icon icon, Icon openedIcon) throws IllegalStateException {
        if (lastRegisteredRoots != null) {
            throw new IllegalStateException("registerExternalRoots was already called"); // NOI18N
        }
        typedSourceRoots.add(new TypedSourceRoot(type, location, displayName, icon, openedIcon));
    }
    
    private Project getProject() {
        return RakeBasedProjectFactorySingleton.getProjectFor(project);
    }
    
    /**
     * Register all external source or non-source roots using {@link FileOwnerQuery#markExternalOwner}.
     * <p>
     * Only roots added by {@link #addPrincipalSourceRoot} and {@link #addNonSourceRoot}
     * are considered. They are registered if (and only if) they in fact fall
     * outside of the project directory, and of course only if the folders really
     * exist on disk. Currently it is not defined when this file existence check
     * is done (e.g. when this method is first called, or periodically) or whether
     * folders which are created subsequently will be registered, so project type
     * providers are encouraged to create all desired external roots before calling
     * this method.
     * </p>
     * <p>
     * If the actual value of the location changes (due to changes being
     * fired from the property evaluator), roots which were previously internal
     * and are now external will be registered, and roots which were previously
     * external and are now internal will be unregistered. The (un-)registration
     * will be done using the same algorithm as was used initially.
     * </p>
     * <p>
     * Calling this method causes the helper object to hold strong references to the
     * current external roots, which helps a project satisfy the requirements of
     * {@link FileOwnerQuery#EXTERNAL_ALGORITHM_TRANSIENT}.
     * </p>
     * <p>
     * You may <em>not</em> call this method inside the project's constructor, as
     * it requires the actual project to exist and be registered in {@link ProjectManager}.
     * Typically you would use {@link org.openide.util.Mutex#postWriteRequest} to run it
     * later, if you were creating the helper in your constructor, since the project construction
     * normally occurs in read access.
     * </p>
     * @param algorithm an external root registration algorithm as per
     *                  {@link FileOwnerQuery#markExternalOwner}
     * @throws IllegalArgumentException if the algorithm is unrecognized
     * @throws IllegalStateException if this method is called more than once on a
     *                               given <code>SourcesHelper</code> object
     */
    public void registerExternalRoots(int algorithm) throws IllegalArgumentException, IllegalStateException {
        if (lastRegisteredRoots != null) {
            throw new IllegalStateException("registerExternalRoots was already called before"); // NOI18N
        }
        registeredRootAlgorithm = algorithm;
        remarkExternalRoots();
    }
    
    private void remarkExternalRoots() throws IllegalArgumentException {
        List<Root> allRoots = new ArrayList<Root>(principalSourceRoots);
        allRoots.addAll(nonSourceRoots);
        Project p = getProject();
        FileObject pdir = project.getProjectDirectory();
        // First time: register roots and add to lastRegisteredRoots.
        // Subsequent times: add to newRootsToRegister and maybe add them later.
        Set<FileObject> newRootsToRegister;
        if (lastRegisteredRoots == null) {
            // First time.
            newRootsToRegister = null;
            lastRegisteredRoots = new HashSet<FileObject>();
            propChangeL = new PropChangeL(); // hold a strong ref
            evaluator.addPropertyChangeListener(WeakListeners.propertyChange(propChangeL, evaluator));
        } else {
            newRootsToRegister = new HashSet<FileObject>();
        }
        // XXX might be a bit more efficient to cache for each root the actualLocation value
        // that was last computed, and just check if that has changed... otherwise we wind
        // up calling APH.resolveFileObject repeatedly (for each property change)
        for (Root r : allRoots) {
            File locF = r.getActualLocation();
            FileObject loc = locF != null ? FileUtil.toFileObject(locF) : null;
            if (loc == null) {
                // Not there; skip it.
                continue;
            }
            if (!loc.isFolder()) {
                // Actually a file. Skip it.
                continue;
            }
            if (FileUtil.getRelativePath(pdir, loc) != null) {
                // Inside projdir already. Skip it.
                continue;
            }
            try {
                Project other = ProjectManager.getDefault().findProject(loc);
                if (other != null) {
                    // This is a foreign project; we cannot own it. Skip it.
                    continue;
                }
            } catch (IOException e) {
                // Assume it is a foreign project and skip it.
                continue;
            }
            // It's OK to go.
            if (newRootsToRegister != null) {
                newRootsToRegister.add(loc);
            } else {
                lastRegisteredRoots.add(loc);
                FileOwnerQuery.markExternalOwner(loc, p, registeredRootAlgorithm);
            }
        }
        if (newRootsToRegister != null) {
            // Just check for changes since the last time.
            Set<FileObject> toUnregister = new HashSet<FileObject>(lastRegisteredRoots);
            toUnregister.removeAll(newRootsToRegister);
            for (FileObject loc : toUnregister) {
                FileOwnerQuery.markExternalOwner(loc, null, registeredRootAlgorithm);
            }
            newRootsToRegister.removeAll(lastRegisteredRoots);
            for (FileObject loc : newRootsToRegister) {
                FileOwnerQuery.markExternalOwner(loc, p, registeredRootAlgorithm);
            }
        }
    }

    /**
     * Create a source list object.
     * <p>
     * All principal source roots are listed as {@link Sources#TYPE_GENERIC} unless they
     * are inside the project directory. The project directory itself is also listed
     * (with a display name according to {@link ProjectUtils#getInformation}), unless
     * it is contained by an explicit principal source root (i.e. ancestor directory).
     * Principal source roots should never overlap; if two configured
     * principal source roots are determined to have the same root folder, the first
     * configured root takes precedence (which only matters in regard to the display
     * name); if one root folder is contained within another, the broader
     * root folder subsumes the narrower one so only the broader root is listed.
     * </p>
     * <p>
     * Other source groups are listed according to the named typed source roots.
     * There is no check performed that these do not overlap (though a project type
     * provider should for UI reasons avoid this situation).
     * </p>
     * <p>
     * Any source roots which do not exist on disk are ignored, as if they had
     * not been configured at all. Currently it is not defined when this existence
     * check is performed (e.g. when this method is called, when the source root
     * is first accessed, periodically, etc.), so project type providers are
     * generally encouraged to make sure all desired source folders exist
     * before calling this method, if creating a new project.
     * </p>
     * <p>
     * Source groups are created according to the semantics described in
     * {@link GenericSources#group}. They are listed in the order they
     * were configured (for those roots that are actually used as groups).
     * </p>
     * <p>
     * You may call this method inside the project's constructor, but
     * {@link Sources#getSourceGroups} may <em>not</em> be called within the
     * constructor, as it requires the actual project object to exist and be
     * registered in {@link ProjectManager}.
     * </p>
     * @return a source list object suitable for {@link Project#getLookup}
     */
    public Sources createSources() {
        return new SourcesImpl();
    }
    
    private final class SourcesImpl implements Sources, PropertyChangeListener, FileChangeSupportListener {
        
        private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
        private boolean haveAttachedListeners;
        private final Set<File> rootsListenedTo = new HashSet<File>();
        /**
         * The root URLs which were computed last, keyed by group type.
         */
        private final Map<String,List<URL>> lastComputedRoots = new HashMap<String,List<URL>>();
        
        public SourcesImpl() {
            evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
        }
        
        public SourceGroup[] getSourceGroups(String type) {
            List<SourceGroup> groups = new ArrayList<SourceGroup>();
            if (type.equals(Sources.TYPE_GENERIC)) {
                List<SourceRoot> roots = new ArrayList<SourceRoot>(principalSourceRoots);
                // Always include the project directory itself as a default:
                roots.add(new SourceRoot("", ProjectUtils.getInformation(getProject()).getDisplayName(), null, null)); // NOI18N
                Map<FileObject,SourceRoot> rootsByDir = new LinkedHashMap<FileObject,SourceRoot>();
                // First collect all non-redundant existing roots.
                for (SourceRoot r : roots) {
                    File locF = r.getActualLocation();
                    if (locF == null) {
                        continue;
                    }
                    listen(locF);
                    FileObject loc = FileUtil.toFileObject(locF);
                    if (loc == null) {
                        continue;
                    }
                    if (rootsByDir.containsKey(loc)) {
                        continue;
                    }
                    rootsByDir.put(loc, r);
                }
                // Remove subroots.
                Iterator<FileObject> it = rootsByDir.keySet().iterator();
                while (it.hasNext()) {
                    FileObject loc = it.next();
                    FileObject parent = loc.getParent();
                    while (parent != null) {
                        if (rootsByDir.containsKey(parent)) {
                            // This is a subroot of something, so skip it.
                            it.remove();
                            break;
                        }
                        parent = parent.getParent();
                    }
                }
                // Everything else is kosher.
                for (Map.Entry<FileObject,SourceRoot> entry : rootsByDir.entrySet()) {
                    groups.add(entry.getValue().toGroup(entry.getKey()));
                }
            } else {
                Set<FileObject> dirs = new HashSet<FileObject>();
                for (TypedSourceRoot r : typedSourceRoots) {
                    if (!r.getType().equals(type)) {
                        continue;
                    }
                    File locF = r.getActualLocation();
                    if (locF == null) {
                        continue;
                    }
                    listen(locF);
                    FileObject loc = FileUtil.toFileObject(locF);
                    if (loc == null) {
                        continue;
                    }
                    if (!dirs.add(loc)) {
                        // Already had one.
                        continue;
                    }
                    groups.add(r.toGroup(loc));
                }
            }
            // Remember what we computed here so we know whether to fire changes later.
            List<URL> rootURLs = new ArrayList<URL>(groups.size());
            for (SourceGroup g : groups) {
                try {
                    rootURLs.add(g.getRootFolder().getURL());
                } catch (FileStateInvalidException e) {
                    assert false : e; // should be a valid file object!
                }
            }
            lastComputedRoots.put(type, rootURLs);
            return groups.toArray(new SourceGroup[groups.size()]);
        }
        
        private void listen(File rootLocation) {
            // #40845. Need to fire changes if a source root is added or removed.
            if (rootsListenedTo.add(rootLocation) && /* be lazy */ haveAttachedListeners) {
                FileChangeSupport.DEFAULT.addListener(this, rootLocation);
            }
        }
        
        public void addChangeListener(ChangeListener listener) {
            if (!haveAttachedListeners) {
                haveAttachedListeners = true;
                for (File rootLocation : rootsListenedTo) {
                    FileChangeSupport.DEFAULT.addListener(this, rootLocation);
                }
            }
            synchronized (listeners) {
                listeners.add(listener);
            }
        }
        
        public void removeChangeListener(ChangeListener listener) {
            synchronized (listeners) {
                listeners.remove(listener);
            }
        }
        
        private void fireChange() {
            ChangeListener[] _listeners;
            synchronized (listeners) {
                if (listeners.isEmpty()) {
                    return;
                }
                _listeners = listeners.toArray(new ChangeListener[listeners.size()]);
            }
            ChangeEvent ev = new ChangeEvent(this);
            for (ChangeListener l : _listeners) {
                l.stateChanged(ev);
            }
        }
        
        private void maybeFireChange() {
            // #47451: check whether anything really changed.
            boolean change = false;
            // Cannot iterate over entrySet, as the map will be modified by getSourceGroups.
            for (String type : new HashSet<String>(lastComputedRoots.keySet())) {
                List<URL> previous = new ArrayList<URL>(lastComputedRoots.get(type));
                getSourceGroups(type);
                List<URL> nue = lastComputedRoots.get(type);
                if (!nue.equals(previous)) {
                    change = true;
                    break;
                }
            }
            if (change) {
                fireChange();
            }
        }

        public void fileCreated(FileChangeSupportEvent event) {
            // Root might have been created on disk.
            maybeFireChange();
        }

        public void fileDeleted(FileChangeSupportEvent event) {
            // Root might have been deleted.
            maybeFireChange();
        }

        public void fileModified(FileChangeSupportEvent event) {
            // ignore; generally should not happen (listening to dirs)
        }
        
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            // Properties may have changed so as cause external roots to move etc.
            maybeFireChange();
        }

    }
    
    private final class PropChangeL implements PropertyChangeListener {
        
        public PropChangeL() {}
        
        public void propertyChange(PropertyChangeEvent evt) {
            // Some properties changed; external roots might have changed, so check them.
            remarkExternalRoots();
        }
        
    }
    
}
