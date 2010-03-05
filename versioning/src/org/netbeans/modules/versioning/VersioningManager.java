/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.versioning;

import java.lang.reflect.Method;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.diff.DiffSidebarManager;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.LookupEvent;

import java.io.File;
import java.util.*;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Top level versioning manager that mediates communitation between IDE and registered versioning systems.
 * 
 * @author Maros Sandor
 */
public class VersioningManager implements PropertyChangeListener, LookupListener, PreferenceChangeListener {
    
    /**
     * Indicates to the Versioning manager that the layout of versioned files may have changed. Previously unversioned 
     * files became versioned, versioned files became unversioned or the versioning system for some files changed.
     * The manager will flush any caches that may be holding such information.  
     * A versioning system usually needs to fire this after an Import action. 
     */
    public static final String EVENT_VERSIONED_ROOTS = "null VCS.VersionedFilesChanged";

    /**
     * The NEW value is a Set of Files whose versioning status changed. This event is used to re-annotate files, re-fetch
     * original content of files and generally refresh all components that are connected to these files.
     */
    public static final String EVENT_STATUS_CHANGED = "Set<File> VCS.StatusChanged";

    /**
     * Used to signal the Versioning manager that some annotations changed. Note that this event is NOT required in case
     * the status of the file changes in which case annotations are updated automatically. Use this event to force annotations
     * refresh in special cases, for example when the format of annotations changes.
     * Use null as new value to force refresh of all annotations.
     */
    public static final String EVENT_ANNOTATIONS_CHANGED = "Set<File> VCS.AnnotationsChanged";


    /**
     * Priority defining the order of versioning systems used when determining the owner of a file. I.e. what versioning system should handle the file.
     * @see #getProperty(String)
     * @see #putProperty(String, Object)
     */
    static final String PROP_PRIORITY = "Integer VCS.Priority"; //NOI18N
    
    private static VersioningManager instance;

    public static synchronized VersioningManager getInstance() {
        if (instance == null) {
            instance = new VersioningManager();
            instance.init();
        }
        return instance;
    }

    // ======================================================================================================

    private final FilesystemInterceptor filesystemInterceptor;

    /**
     * Result of Lookup.getDefault().lookup(new Lookup.Template<VersioningSystem>(VersioningSystem.class));
     */
    private final Lookup.Result<VersioningSystem> systemsLookupResult;
    
    /**
     * Holds all registered versioning systems.
     */
    private final List<VersioningSystem> versioningSystems = new ArrayList<VersioningSystem>(5);

    /**
     * What folder is versioned by what versioning system. 
     * TODO: use SoftHashMap if there is one available in APIs
     */
    private final Map<File, VersioningSystem> folderOwners = new HashMap<File, VersioningSystem>(200);

    /**
     * Holds registered local history system.
     */
    private VersioningSystem localHistory;

    static final Logger LOG = Logger.getLogger("org.netbeans.modules.versioning");

    /**
     * What files or folders are managed by local history.
     * TODO: use SoftHashMap if there is one available in APIs
     */
    private final Map<File, Boolean> localHistoryFiles = new LinkedHashMap<File, Boolean>(200);

    /**
     * Holds methods intercepted by a specific vcs. See {@link #needsLocalHistory(methodName)}
     */
    private Map<String, Set<String>> interceptedMethods = new HashMap<String, Set<String>>();

    private final VersioningSystem NULL_OWNER = new VersioningSystem() {
    };
    
    private VersioningManager() {
        systemsLookupResult = Lookup.getDefault().lookup(new Lookup.Template<VersioningSystem>(VersioningSystem.class));
        filesystemInterceptor = new FilesystemInterceptor();
    }
    
    private void init() {
        systemsLookupResult.addLookupListener(this);
        refreshVersioningSystems();
        filesystemInterceptor.init(this);
        VersioningSupport.getPreferences().addPreferenceChangeListener(this);
    }

    private int refreshSerial;
    
    /**
     * List of versioning systems changed.
     */
    private void refreshVersioningSystems() {
        int rs = ++refreshSerial;
        Collection<? extends VersioningSystem> systems = systemsLookupResult.allInstances();
        if (rs != refreshSerial) {
            // TODO: Workaround for Lookup bug #132145, we have to abort here to keep the freshest list of versioning systems
            return;
        }

        synchronized(versioningSystems) {
            // inline unloadVersioningSystems();
            for (VersioningSystem system : versioningSystems) {
                system.removePropertyChangeListener(this);
            }
            versioningSystems.clear();
            localHistory = null;
            // inline unloadVersioningSystems();

            // inline loadVersioningSystems(systems);
            versioningSystems.addAll(systems);
            Collections.sort(versioningSystems, new ByPriorityComparator());
            Collections.reverse(versioningSystems); // systems with higher priority should be at the end of the list, see getOwner logic
            for (VersioningSystem system : versioningSystems) {
                if (localHistory == null && Utils.isLocalHistory(system)) {
                    localHistory = system;
                }
                system.addPropertyChangeListener(this);
            }
            // inline loadVersioningSystems(systems);
        }

        flushFileOwnerCache();
        refreshDiffSidebars(null);
        VersioningAnnotationProvider.refreshAllAnnotations();
    }

    InterceptionListener getInterceptionListener() {
        return filesystemInterceptor;
    }

    private void refreshDiffSidebars(Set<File> files) {
        // pushing the change ... DiffSidebarManager may as well listen for changes
        DiffSidebarManager.getInstance().refreshSidebars(files);
    }
    
    private void flushFileOwnerCache() {
        synchronized(folderOwners) {
            folderOwners.clear();
        }
    }

    VersioningSystem[] getVersioningSystems() {
        synchronized(versioningSystems) {
            return versioningSystems.toArray(new VersioningSystem[versioningSystems.size()]);
        }
    }

    /**
     * Determines versioning systems that manage files in given context.
     * 
     * @param ctx VCSContext to examine
     * @return VersioningSystem systems that manage this context or an empty array if the context is not versioned
     */
    VersioningSystem[] getOwners(VCSContext ctx) {
        Set<File> files = ctx.getRootFiles();
        Set<VersioningSystem> owners = new HashSet<VersioningSystem>();
        for (File file : files) {
            VersioningSystem vs = getOwner(file);
            if (vs != null) {
                owners.add(vs);
            }
        }
        return (VersioningSystem[]) owners.toArray(new VersioningSystem[owners.size()]);
    }

    /**
     * Determines the versioning system that manages given file.
     * Owner of a file:
     * - annotates its label in explorers, editor tab, etc.
     * - provides menu actions for it
     * - supplies "original" content of the file
     * 
     * Owner of a file may change over time (one common example is the Import command). In such case, the appropriate 
     * Versioning System is expected to fire the PROP_VERSIONED_ROOTS property change. 
     * 
     * @param file a file
     * @return VersioningSystem owner of the file or null if the file is not under version control
     */
    public VersioningSystem getOwner(File file) {
        LOG.log(Level.FINE, "looking for owner of " + file);
        
        
        /**
         * minor speed optimization, file.isFile may last a while
         * if file is a folder then the owner may be acquired from folderOwners directly before file.isFile call
         * otherwise the owner will be acquired after file.isFile call
         */
        VersioningSystem owner = null;
        synchronized(folderOwners) {
            owner = folderOwners.get(file);
        }
        if (owner == NULL_OWNER) {
            LOG.log(Level.FINE, " cached NULL_OWNER of {0}", new Object[] { file });
            return null;
        }
        if (owner != null) {
            LOG.log(Level.FINE, " cached owner {0} of {1}", new Object[] { owner.getClass().getName(), file });
            return owner;
        }

        File folder = file;
        if (Utils.isFile(file)) {
            folder = file.getParentFile();
            if (folder == null) {
                LOG.log(Level.FINE, " null parent");
                return null;
            }
            synchronized(folderOwners) {
                owner = folderOwners.get(folder);
            }
        }
        
        if (owner == NULL_OWNER) {
            LOG.log(Level.FINE, " cached NULL_OWNER of {0}", new Object[] { folder });
            return null;
        }
        if (owner != null) {
            LOG.log(Level.FINE, " cached owner {0} of {1}", new Object[] { owner.getClass().getName(), folder });
            return owner;
        }
        
        File closestParent = null;

        VersioningSystem[] vs = getVersioningSystems();
        for (VersioningSystem system : vs) {
            if (system != localHistory) {    // currently, local history is never an owner of a file
                File topmost = system.getTopmostManagedAncestor(folder);
                LOG.log(Level.FINE, " {0} returns {1} ", new Object[] { system.getClass().getName(), topmost }) ;
                if (topmost != null && (closestParent == null || Utils.isAncestorOrEqual(closestParent, topmost))) {
                    LOG.log(Level.FINE, " owner = {0}", new Object[] { system.getClass().getName() }) ;
                    owner = system;
                    closestParent = topmost;
                }
            }
        }
                
        synchronized(folderOwners) {
            if (owner != null) {
                LOG.log(Level.FINE, " caching owner {0} of {1}", new Object[] { owner != null ? owner.getClass().getName() : null, folder }) ;
                folderOwners.put(folder, owner);
            } else {
                // nobody owns the folder => all parents aren't owned
                while(folder != null) {
                    LOG.log(Level.FINE, " caching unversioned folder {0}", new Object[] { folder }) ;
                    folderOwners.put(folder, NULL_OWNER);
                    folder = folder.getParentFile();
                }
            }
        }
        
        LOG.log(Level.FINE, "owner = {0}", new Object[] { owner != null ? owner.getClass().getName() : null }) ;
        return owner;
    }
    
    /**
     * Returns local history module that handles the given file.
     * 
     * @param file the file to examine
     * @return VersioningSystem local history versioning system or null if there is no local history for the file
     */
    VersioningSystem getLocalHistory(File file) {
        if (localHistory == null) return null;

        synchronized(localHistoryFiles) {
            Boolean isManagedByLocalHistory = localHistoryFiles.get(file);
            if (isManagedByLocalHistory != null && isManagedByLocalHistory) {
                return localHistory;
            }
        }
        File folder = file;
        if (Utils.isFile(file)) {
            folder = file.getParentFile();
            if (folder == null) return null;
        }

        synchronized(localHistoryFiles) {
            Boolean isManagedByLocalHistory = localHistoryFiles.get(folder);
            if (isManagedByLocalHistory != null) {
                return isManagedByLocalHistory ? localHistory : null;
            }
        }

        boolean isManaged = localHistory.getTopmostManagedAncestor(folder) != null;            
        if (isManaged) {
            putLocalHistoryFile(Boolean.TRUE, folder);
            return localHistory;
        } else {
            isManaged = localHistory.getTopmostManagedAncestor(file) != null;
            putLocalHistoryFile(isManaged, file, folder);
            return null;
        }        
    }

    private void putLocalHistoryFile(Boolean b, File... files) {
        synchronized(localHistoryFiles) {
            if(localHistoryFiles.size() > 1500) {
                Iterator<File> it = localHistoryFiles.keySet().iterator();
                for (int i = 0; i < 150; i++) {
                    it.next();
                    it.remove();
                }
            }
            for (File file : files) {
                localHistoryFiles.put(file, b);
            }
        }
    }

    public void resultChanged(LookupEvent ev) {
        refreshVersioningSystems();
    }

    /**
     * Versioning status or other parameter changed. 
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (EVENT_STATUS_CHANGED.equals(evt.getPropertyName())) {
            Set<File> files = (Set<File>) evt.getNewValue();
            VersioningAnnotationProvider.instance.refreshAnnotations(files);
            refreshDiffSidebars(files);
        } else if (EVENT_ANNOTATIONS_CHANGED.equals(evt.getPropertyName())) {
            Set<File> files = (Set<File>) evt.getNewValue();
            VersioningAnnotationProvider.instance.refreshAnnotations(files);
        } else if (EVENT_VERSIONED_ROOTS.equals(evt.getPropertyName())) {
            if(evt.getSource() == localHistory) {
                synchronized(localHistoryFiles) {
                    localHistoryFiles.clear();
                }
            } else {
                flushFileOwnerCache();
                refreshDiffSidebars(null);
            }
        }
    }

    public void preferenceChange(PreferenceChangeEvent evt) {
        VersioningAnnotationProvider.instance.refreshAnnotations(null);
    }

    /**
     * Determines if the given methodName is implemented by local histories {@link VCSInterceptor}
     *
     * @param methodName
     * @return <code>true</code> if the given methodName is implemented by local histories {@link VCSInterceptor}
     * otherwise <code>false</code>
     */
    boolean needsLocalHistory(String methodName) {
        boolean ret = false;
        try {
            if(localHistory == null) {
                return ret;
}
            Set<String> s = interceptedMethods.get(localHistory.getClass().getName());
            if(s == null) {
                s = new HashSet<String>();
                Method[] m = localHistory.getVCSInterceptor().getClass().getDeclaredMethods();
                for (Method method : m) {
                    if((method.getModifiers() & Modifier.PUBLIC) != 0) {
                        s.add(method.getName());
                    }
                }
                interceptedMethods.put(localHistory.getClass().getName(), s);
            }
            ret = s.contains(methodName);
            return ret;
        } finally {
            LOG.log(Level.FINE, "needsLocalHistory method [{0}] returns {1}", new Object[] {methodName, ret});
        }
    }

    /**
     * Sorts versioning systems according to their priority. Systems with lower value (higher priority) will be stated before those with higher value (lower priority) in the given list.
     */
    private static final class ByPriorityComparator implements Comparator<VersioningSystem> {
        @Override
        public int compare(VersioningSystem o1, VersioningSystem o2) {
            return Utils.getPriority(o1).compareTo(Utils.getPriority(o2));
        }
    }
}
