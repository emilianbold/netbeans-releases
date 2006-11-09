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
     * What file is versioned by what versioning system. Files stored here are only topmost versioned folders. 
     */
    private final Map<File, VersioningSystem> managedRoots = new HashMap<File, VersioningSystem>(20);  
    
    /**
     * Holds all registered versioning systems.
     */
    private final Collection<VersioningSystem> versioningSystems = new ArrayList<VersioningSystem>(2);

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
        managedRoots.clear();
    }
    
    public synchronized VersioningSystem getOwner(File file) {
        for (Map.Entry<File, VersioningSystem> entry : managedRoots.entrySet()) {
            if (Utils.isParentOrEqual(entry.getKey(), file)) {
                return entry.getValue();
            }
        }
        
        // we do not know yet whom the file belongs to
        File closestParent = null;
        VersioningSystem owner = null; 
        for (VersioningSystem system : versioningSystems) {
            File topmost = system.getTopmostManagedParent(file);
            if (topmost != null && (closestParent == null || Utils.isParentOrEqual(closestParent, topmost))) {
                owner = system;
                closestParent = topmost;
            }
        }
        
        if (owner != null) {
            removeDescendants(closestParent, owner);
        }
        return owner;
    }

    private void removeDescendants(File topmost, VersioningSystem system) {
        for (Iterator<Map.Entry<File, VersioningSystem>> i = managedRoots.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<File, VersioningSystem> entry = i.next();
            if (system == entry.getValue()) {
                if (Utils.isParentOrEqual(entry.getKey(), topmost)) {
                    return;
                }
                if (Utils.isParentOrEqual(topmost, entry.getKey())) {
                    i.remove();
                }
            }
        }
        managedRoots.put(topmost, system);
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
