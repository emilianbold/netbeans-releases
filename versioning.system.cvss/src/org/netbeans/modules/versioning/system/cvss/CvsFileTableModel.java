/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.modules.versioning.system.cvss.util.FlatFolder;
import org.openide.ErrorManager;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Provides support for listing files that have a common parent and a given versioning status.
 * TODO: integrate functionality provided by this class to FileStatusCache
 * 
 * @author Maros Sandor
 */
public class CvsFileTableModel {
    
    private final int includeStatus;
    private final File [] roots;
    private final FileStatusCache cache;
    
    static CvsFileTableModel getModel(File [] roots, int includeStatus) {
        return new CvsFileTableModel(roots, includeStatus);
    }
    
    private CvsFileTableModel(File [] roots, int includeStatus) {
        this.roots = (File[]) roots.clone();
        this.includeStatus = includeStatus; 
        this.cache = CvsVersioningSystem.getInstance().getStatusCache();
    }

    /**
     * This method may block for an unspecified amount of time and should not be called
     * from event dispatching threads.
     * 
     * @return array of file nodes that are not up-to-date
     */ 
    public CvsFileNode [] getNodes() {
        List nodes = new ArrayList();
        for (int i = 0; i < roots.length; i++) {
            File root = roots[i];
            if (root instanceof FlatFolder) {
                addFlat(nodes, root);
            } else {
                addRecursively(nodes, root);
            }
        }
        return (CvsFileNode[]) nodes.toArray(new CvsFileNode[nodes.size()]); 
    }

    /**
     * This method does not block if the information is not readily available.
     * 
     * @return array of file nodes that are not up-to-date
     */ 
    public CvsFileNode [] getNodesCached() throws FileStatusCache.InformationUnavailableException {
        List nodes = new ArrayList();
        for (int i = 0; i < roots.length; i++) {
            File root = roots[i];
            if (root instanceof FlatFolder) {
                addFlatCached(nodes, root);
            } else {
                addRecursivelyCached(nodes, root);
            }
        }
        return (CvsFileNode[]) nodes.toArray(new CvsFileNode[nodes.size()]);
    }

    // -- private methods ----------------------
    
    private void addFlatCached(List nodes, File file) throws FileStatusCache.InformationUnavailableException {
        FileInformation info = cache.getStatusCached(file);
        int status = cache.getStatusCached(file).getStatus();
        if (info.isDirectory()) {
            File [] files = cache.listFilesCached(file);
            for (int i = 0; i < files.length; i++) {
                info = cache.getStatusCached(files[i]);
                status = info.getStatus();
                if (info.isDirectory()) {
                    continue;
                }
                if ((includeStatus & status) != 0) {
                    nodes.add(new CvsFileNode(files[i]));
                }
            }
        } else {
            if ((includeStatus & status) != 0) {
                nodes.add(new CvsFileNode(file));
            }
        }
    }

    private void addRecursivelyCached(List nodes, File file) throws FileStatusCache.InformationUnavailableException {
        FileInformation info = cache.getStatusCached(file);
        if (info.isDirectory()) {
            File [] files = cache.listFilesCached(file);
            for (int i = 0; i < files.length; i++) {
                addRecursivelyCached(nodes, files[i]);
            }
        } else {
            if ((includeStatus & info.getStatus()) != 0) {
                nodes.add(new CvsFileNode(file));
            }
        }
    }
    
    private void addFlat(List nodes, File file) {
        if (file.isDirectory()) {
            File [] files = cache.listFiles(file);
            for (int i = 0; i < files.length; i++) {
                if ((includeStatus & cache.getStatus(files[i]).getStatus()) != 0) {
                    nodes.add(new CvsFileNode(files[i]));
                }
            }
        } else {
            if ((includeStatus & cache.getStatus(file).getStatus()) != 0) {
                nodes.add(new CvsFileNode(file));
            }
        }
    }

    private void addRecursively(List nodes, File file) {
        FileInformation info = cache.getStatus(file);
        if (info.isDirectory()) {
            File [] files = cache.listFiles(file);
            try {
                String parentPath = file.getCanonicalPath();
                for (int i = 0; i < files.length; i++) {
                    File f = files[i];
                    if (f.isFile() || f.getCanonicalPath().startsWith(parentPath)) {
                        addRecursively(nodes, files[i]);
                    } else {
                        ErrorManager.getDefault().log("Detected non-hiearchical folder structure: " + f.getAbsolutePath());
                    }
                }
            } catch (IOException e) {
                ErrorManager err = ErrorManager.getDefault();
                err.annotate(e, "Can not apply upward symbolic link detection algorithm, assuming cycle risk...");
                err.notify(e);
            }

        } else {
            if ((includeStatus & info.getStatus()) != 0) {
                nodes.add(new CvsFileNode(file));
            }
        }
    }
}
