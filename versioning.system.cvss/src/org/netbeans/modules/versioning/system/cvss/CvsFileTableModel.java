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

import java.io.File;
import java.util.*;
import java.lang.ref.SoftReference;

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
    
    private int cacheSerial;
    private CvsFileNode [] cachedNodes;

    private static final Map instanceCache = new HashMap();
    
    static CvsFileTableModel getModel(File [] roots, int includeStatus) {
        Key key = new Key(roots, includeStatus);
        synchronized(instanceCache) {
            SoftReference modelReference = (SoftReference) instanceCache.get(key);
            CvsFileTableModel model = (CvsFileTableModel) (modelReference != null ? modelReference.get() : null);
            if (model == null || CvsVersioningSystem.getInstance().getStatusCache().getSerialVersion() > model.cacheSerial) {
                model = new CvsFileTableModel(roots, includeStatus);
                instanceCache.put(key, new SoftReference(model));
            }
            return model;
        }
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
    public synchronized CvsFileNode [] getNodes() {
        /*
        Note on caching and serial numbers:
        ==================================
        We cannot rely on the cahce's event mechanism in this case. For example: file is modified, cache fires the change
        and then the versioning view refreshes itself. Now the problem is, that the table model MAY NOT know that
        the file changed and returns cached nodes, which is wrong. All depends on the order in which listeners are notified.
        */
        if (cachedNodes == null || cache.getSerialVersion() > cacheSerial) {
            List nodes = new ArrayList();
            for (int i = 0; i < roots.length; i++) {
                File root = roots[i];
                if (root instanceof FlatFolder) {
                    addFlat(nodes, root);
                } else {
                    addRecursively(nodes, root);
                }
            }
            cachedNodes = (CvsFileNode[]) nodes.toArray(new CvsFileNode[nodes.size()]); 
            cacheSerial = cache.getSerialVersion();
        }
        return cachedNodes; 
    }

    /**
     * This method may block for an unspecified amount of time and should not be called
     * from event dispatching threads.
     * 
     * @return array of file nodes that are not up-to-date
     */ 
    public synchronized CvsFileNode [] getNodesCached() throws FileStatusCache.InformationUnavailableException {
        if (cachedNodes == null || cache.getSerialVersion() > cacheSerial) {
            List nodes = new ArrayList();
            for (int i = 0; i < roots.length; i++) {
                File root = roots[i];
                if (root instanceof FlatFolder) {
                    addFlatCached(nodes, root);
                } else {
                    addRecursivelyCached(nodes, root);
                }
            }
            cachedNodes = (CvsFileNode[]) nodes.toArray(new CvsFileNode[nodes.size()]); 
            cacheSerial = cache.getSerialVersion();
        }
        return cachedNodes; 
    }

    private void addFlatCached(List nodes, File file) throws FileStatusCache.InformationUnavailableException {
        int status = cache.getStatusCached(file).getStatus();
        if (file.isDirectory() || (status & FileInformation.FLAG_DIRECTORY) != 0) {
            File [] files = cache.listFilesCached(file);
            for (int i = 0; i < files.length; i++) {
                status = cache.getStatusCached(files[i]).getStatus();
                if (files[i].isDirectory() || (status & FileInformation.FLAG_DIRECTORY) != 0) {
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
        int status = cache.getStatusCached(file).getStatus();
        if (file.isDirectory() || (status & FileInformation.FLAG_DIRECTORY) != 0) {
            File [] files = cache.listFilesCached(file);
            for (int i = 0; i < files.length; i++) {
                addRecursivelyCached(nodes, files[i]);
            }
        } else {
            if ((includeStatus & status) != 0) {
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
        int status = cache.getStatus(file).getStatus();
        if (file.isDirectory() || (status & FileInformation.FLAG_DIRECTORY) != 0) {
            File [] files = cache.listFiles(file);
            for (int i = 0; i < files.length; i++) {
                addRecursively(nodes, files[i]);
            }
        } else {
            if ((includeStatus & status) != 0) {
                nodes.add(new CvsFileNode(file));
            }
        }
    }
    
    private static class Key {
        
        private final File[] roots;
        private final int statuses;
        private int hash;

        Key(File [] roots, int status) {
            this.roots = roots;
            this.statuses = status;
            hash = statuses;
            for (int i = 0; i < roots.length; i++) {
                File root = roots[i];
                hash += root.hashCode();
            }
        }

        public int hashCode() {
            return hash;
        }

        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Key)) return false;
            Key other = (Key) obj;
            return statuses == other.statuses && Arrays.equals(roots, other.roots);
        }
    }
}
