/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openidex.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;

/**
 *
 * @author  Marian Petras
 */
final class SimpleSearchInfo implements SearchInfo {
    
    /**
     * Empty search info object.
     * Its method {@link SearchInfo#canSearch canSearch()}
     * always returns <code>true</code>. Its iterator
     * (returned by method
     * {@link SearchInfo#objectsToSearch objectsToSearch()}) has no elements.
     */
    static final SearchInfo EMPTY_SEARCH_INFO
        = new SearchInfo() {
            public boolean canSearch() {
                return true;
            }
            public java.util.Iterator objectsToSearch() {
                return Collections.EMPTY_LIST.iterator();
            }
        };
        
    /** */
    private final DataFolder rootFolder;
    /** */
    private final boolean recursive;
    /** */
    private final FileObjectFilter[] filters;
    
    /**
     * Creates a new instance of SimpleSearchInfo
     *
     * @param  folder  <!-- PENDING -->
     * @param  filters  <!-- PENDING, accepts null -->
     * @exception  java.lang.IllegalArgumentException
     *             if the <code>folder</code> argument is <code>null</code>
     */
    SimpleSearchInfo(DataFolder folder,
                     boolean recursive,
                     FileObjectFilter[] filters) {
        if (folder == null) {
            throw new IllegalArgumentException();
        }
        
        if ((filters != null) && (filters.length == 0)) {
            filters = null;
        }
        this.rootFolder = folder;
        this.recursive = recursive;
        this.filters = filters;
    }

    /**
     */
    public boolean canSearch() {
        return (filters != null)
               ? checkFolderAgainstFilters(rootFolder.getPrimaryFile())
               : true;
    }

    /**
     */
    public Iterator objectsToSearch() {
        return new SimpleSearchIterator(rootFolder,
                                        recursive,
                                        filters != null ? Arrays.asList(filters)
                                                        : null);
    }
    
    /**
     */
    private boolean checkFolderAgainstFilters(final FileObject folder) {
        assert folder.isFolder();
        
        for (int i = 0; i < filters.length; i++) {
            if (filters[i].traverseFolder(folder)
                    == FileObjectFilter.DO_NOT_TRAVERSE) {
                return false;
            }
        }
        return true;
    }
    
}
