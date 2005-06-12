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

import java.io.File;

/**
 * Provides support for listing files that have a common parent and a given versioning status.
 *
 * @author Maros Sandor
 */
public class CvsFileTableModel {
    
    private final int includeStatus;
    private final File [] roots;
    private final FileStatusCache cache;

    CvsFileTableModel(File [] roots, int includeStatus) {
        this.roots = (File[]) roots.clone();
        this.includeStatus = includeStatus; 
        this.cache = CvsVersioningSystem.getInstance().getStatusCache();
    }

    /**
     *
     * @return CvsFileNode [] array of files in this table model
     */
    public CvsFileNode [] getNodes() {
        File [] files = cache.listFiles(roots, includeStatus);
        CvsFileNode [] nodes = new CvsFileNode[files.length];
        for (int i = 0; i < files.length; i++) {
            nodes[i] = new CvsFileNode(files[i]);
        }
        return nodes;
    }
}
