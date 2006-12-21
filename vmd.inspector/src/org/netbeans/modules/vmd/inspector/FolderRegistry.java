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
package org.netbeans.modules.vmd.inspector;


import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.netbeans.modules.vmd.api.inspector.InspectorFolder;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.openide.util.Mutex;

final class FolderRegistry {

    private static final HashMap<String, WeakReference<FolderRegistry>> registries = new HashMap<String, WeakReference<FolderRegistry>> ();

    private Mutex mutex = new Mutex();
    private HashMap<TypeID, InspectorFolder> folders = new HashMap<TypeID, InspectorFolder>();
    private GlobalFolderRegistry globalFolderRegistry;

    static FolderRegistry getRegistry(String projectType, String projectID)  {
        synchronized (registries) {
            WeakReference<FolderRegistry> ref = registries.get(projectID);
            FolderRegistry registry = ref != null ? ref.get() : null;
            if (registry == null) {
                registry = new FolderRegistry(projectType);
                registries.put(projectID, new WeakReference<FolderRegistry>(registry));
            }
            return registry;
        }
    }

    private FolderRegistry(String projectType) {
        globalFolderRegistry = GlobalFolderRegistry.getGlobalFolderRegistry(projectType);
        reload();
    }

    private boolean isAccess() {
        return mutex.isReadAccess() || mutex.isWriteAccess();
    }

    void readAccess(final Runnable runnable) {
        globalFolderRegistry.readAccess(new Runnable() {
            public void run() {
                mutex.readAccess(runnable);
            }
        });
    }

    private void writeAccess(final Runnable runnable) {
        globalFolderRegistry.readAccess(new Runnable() {
            public void run() {
                mutex.writeAccess(runnable);
            }
        });
    }

    private void reload() {
        writeAccess(new Runnable() {
            public void run() {
                reloadCore();
            }
        });
    }

    private void reloadCore() {
        HashMap<TypeID, InspectorFolder> tempFolders = new HashMap<TypeID, InspectorFolder>();
        Collection<InspectorFolder> folders = globalFolderRegistry.getInspectorFolder();

        for (InspectorFolder folder : folders)
            tempFolders.put(folder.getTypeID(), folder);

        this.folders = tempFolders;
    }

    Collection<InspectorFolder> getFolders() {
        assert isAccess();
        return Collections.unmodifiableCollection(folders.values());
    }

    void addListener(Listener listener) {
        globalFolderRegistry.addListener(listener);
    }

    void removeListener(Listener listener){
        globalFolderRegistry.removeListener(listener);
    }

    interface Listener{
        void notifyRegistryContentChange();
    }
}
