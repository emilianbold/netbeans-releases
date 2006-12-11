/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.versioning;

import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.LookupEvent;

import java.io.File;
import java.util.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * Top level versioning manager that mediates communitation between IDE and registered versioning systems.
 * 
 * @author Maros Sandor
 */
public class VersioningManager implements PropertyChangeListener, LookupListener {
    
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
     * Holds all registered versioning systems.
     */
    private final Collection<VersioningSystem> versioningSystems = new ArrayList<VersioningSystem>(2);

    /**
     * What folder is versioned by what versioning system. 
     */
    private Map<File, VersioningSystem> folderOwners = new WeakHashMap<File, VersioningSystem>(100);
    
    private final VersioningSystem NULL_OWNER = new VersioningSystem() {
        public String getDisplayName() {
            return null;
        }
    };
    
    private VersioningManager() {
        filesystemInterceptor = new FilesystemInterceptor();
    }
    
    private void init() {
        initVersioningSystems();
        filesystemInterceptor.init(this);
    }

    private void initVersioningSystems() {
        Lookup.Result<VersioningSystem> result = Lookup.getDefault().lookup(new Lookup.Template<VersioningSystem>(VersioningSystem.class));
        refreshVersioningSystems(result.allInstances());
        result.addLookupListener(this);
    }

    /**
     * List of versioning systems changed.
     * 
     * @param systems new list of versioning systems
     */
    private void refreshVersioningSystems(Collection<? extends VersioningSystem> systems) {
        synchronized(versioningSystems) {
            unloadVersioningSystems();
            loadVersioningSystems(systems);
        }
    }

    private void loadVersioningSystems(Collection<? extends VersioningSystem> systems) {
        versioningSystems.addAll(systems);
        for (VersioningSystem system : versioningSystems) {
            system.addPropertyChangeListener(this);
        }
    }

    private void unloadVersioningSystems() {
        for (VersioningSystem system : versioningSystems) {
            system.removePropertyChangeListener(this);
        }
        versioningSystems.clear();
    }

    InterceptionListener getInterceptionListener() {
        return filesystemInterceptor;
    }

    private synchronized void flushFileOwnerCache() {
        folderOwners.clear();
    }

    synchronized VersioningSystem[] getVersioningSystems() {
        return versioningSystems.toArray(new VersioningSystem[versioningSystems.size()]);
    }

    /**
     * 
     * @param ctx VCSContext to examine
     * @return VersioningSystem systems that manage this context or an empty array if the context is not versioned
     */
    public VersioningSystem[] getOwners(VCSContext ctx) {
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
     * 
     * @param file a file
     * @return VersioningSystem owner of the file or null if the file is not under version control
     */
    public synchronized VersioningSystem getOwner(File file) {
        File folder = file;
        if (file.isFile()) {
            folder = file.getParentFile();
            if (folder == null) return null;
        }
        
        VersioningSystem owner = folderOwners.get(folder);
        if (owner == NULL_OWNER) return null;
        if (owner != null) return owner;
        
        File closestParent = null;
            for (VersioningSystem system : versioningSystems) {
                // XXX HACK! ignore local history
                if(!system.getClass().getName().equals("org.netbeans.modules.localhistory.LocalHistoryVCS")) {
                    File topmost = system.getTopmostManagedParent(folder);                
                    if (topmost != null && (closestParent == null || Utils.isParentOrEqual(closestParent, topmost))) {
                        owner = system;
                        closestParent = topmost;
                    }                    
                }    
            }
                
        if (owner != null) {
            folderOwners.put(folder, owner);
        } else {
            folderOwners.put(folder, NULL_OWNER);
        }
        return owner;
    }

    private VersioningSystem localHistory;
    private Map<File, VersioningSystem> localHistoryFiles = new WeakHashMap<File, VersioningSystem>(100);
    
    public synchronized VersioningSystem getLocalHistory(File file) {
        File folder = file;
        if (file.isFile()) {
            folder = file.getParentFile();
            if (folder == null) return null;
        }
        
        if(localHistory == null) {
            // XXX HACK!
            for (VersioningSystem system : versioningSystems) {
                if(system.getClass().getName().equals("org.netbeans.modules.localhistory.LocalHistoryVCS")) {
                    localHistory = system;
                    break;
                }
            }            
        }
        if(localHistory == null) return null;     
        
        VersioningSystem owner = localHistoryFiles.get(folder);
        if (owner == NULL_OWNER) return null;
        if (owner != null) return owner;
                
        boolean isManaged = localHistory.getTopmostManagedParent(folder) != null;            
        
        if (isManaged) {
            localHistoryFiles.put(folder, localHistory);
            owner = localHistory;
        } else {
            localHistoryFiles.put(folder, NULL_OWNER);
            owner = null;
        }        
        return owner;
    }
    
    public void resultChanged(LookupEvent ev) {
        Lookup.Result<VersioningSystem> result = (Lookup.Result<VersioningSystem>) ev.getSource();
        refreshVersioningSystems(result.allInstances());
    }

    /**
     * Versioning status or other parameter changed. 
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (VersioningSystem.PROP_STATUS_CHANGED.equals(evt.getPropertyName())) {
            Set<File> files = (Set<File>) evt.getNewValue();
            VersioningAnnotationProvider.instance.refreshAnnotations(files);
        } else if (VersioningSystem.PROP_ANNOTATIONS_CHANGED.equals(evt.getPropertyName())) {
            Set<File> files = (Set<File>) evt.getNewValue();
            VersioningAnnotationProvider.instance.refreshAnnotations(files);
        } else if (VersioningSystem.PROP_VERSIONED_ROOTS.equals(evt.getPropertyName())) {
            flushFileOwnerCache();
        }
    }
}
