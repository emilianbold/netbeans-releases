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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openidex.search;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 *
 * @author  Marian Petras
 */
class SimpleSearchIterator implements Iterator<DataObject> {

    /** current enumeration of children */
    private Enumeration<DataObject> childrenEnum;
    /**
     * filters to be applied on the current enumeration of children
     * ({@link #childrenEnum})
     */
    private List<FileObjectFilter> filters;
    /**
     * contains either an equal copy of {@link #filters} or <code>null</code>
     */
    private List<FileObjectFilter> filtersCopy;
    /** */
    private final boolean recursive;
    /** stack of the ancestor folders' children enumerations */
    private final List<Enumeration<DataObject>> enums
            = new ArrayList<Enumeration<DataObject>>();            //unsynced stack
    /**
     * stack of filter lists to be applied on children of the ancestor folders
     * ({@link #enums})
     */
    private final List<List<FileObjectFilter>> filterLists
            = new ArrayList<List<FileObjectFilter>>();      //unsynced stack
    /** whether value of {@link #nextObject} is up-to-date */
    private boolean upToDate = false;
    /**
     * <code>DataObject</code> to be returned the next time method
     * {@link #next()} is called
     */
    private DataObject nextObject;

    /**
     */
    SimpleSearchIterator(DataFolder folder,
                         boolean recursive,
                         List<FileObjectFilter> filters) {
        this.childrenEnum = folder.children(false);
        this.recursive = recursive;
        this.filters = (filters != null) ? new ArrayList<FileObjectFilter>(filters)
                                         : null;
    }

    /**
     */
    public boolean hasNext() {
        if (!upToDate) {
            update();
        }
        return nextObject != null;
    }

    /** 
     */
    public DataObject next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        upToDate = false;
        return nextObject;
    }

    /**
     */
    private void update() {
        assert upToDate == false;
        assert childrenEnum != null;
        do {
            if (childrenEnum.hasMoreElements()) {
                DataObject dataObject = childrenEnum.nextElement();
                FileObject file = dataObject.getPrimaryFile();
                if (file.isFolder()) {
                    if (!recursive) {
                        continue;
                    }
                    
                    if (filters != null) {
                        final List<FileObjectFilter> subfolderFilters = checkFolderFilters(file);
                        if (subfolderFilters == null) {
                            continue;
                        }
                        
                        filterLists.add(filters);
                        if (subfolderFilters.size() != filters.size()) {
                            filters = (!subfolderFilters.isEmpty())
                                      ? subfolderFilters
                                      : null;
                        }
                    } else {
                        filterLists.add(null);
                    }
                    enums.add(childrenEnum);
                    childrenEnum = ((DataFolder) dataObject).children(false);
                } else {
                    if ((filters != null) && !checkFileFilters(file)) {
                        continue;
                    }
                    
                    nextObject = dataObject;
                    break;
                }
            } else {
                assert enums.isEmpty() == filterLists.isEmpty();
                
                nextObject = null;
                
                if (enums.isEmpty()) {
                    childrenEnum = null;
                    continue;
                }
                
                /* pop an element from the stack of children enumerations: */
                childrenEnum = enums.remove(enums.size() - 1);
                
                /* pop an element from the stack of FileObjectFilters: */
                filters = filterLists.remove(filterLists.size() - 1);
                if ((filtersCopy != null)
                        && (filtersCopy.size() != filters.size())) {
                    filtersCopy = null;
                }
            }
        } while (childrenEnum != null);
        
        upToDate = true;
    }
    
    /**
     * Computes a list of filters to be applied on the folder's children.
     * The current list of filters is used as a base and then each filter
     * is checked with the folder as a parameter.
     * <p>
     * If any of the filters returns <code>DO_NOT_TRAVERSE</code>,
     * <code>the method returns <code>null</code> and no further filters
     * are checked.
     * If a filter returns <code>TRAVERSE_ALL_SUBFOLDERS</code>,
     * the filter is removed from the base as it needs not be applied
     * on the folder's children. The remaining list of filters is returned
     * as a result.
     *
     * @param  folder  folder to compute children filters for
     * @return  list of filters to be applied on the folder's children;
     *          or <code>null</code> if the folder should not be traversed
     */
    private List<FileObjectFilter> checkFolderFilters(final FileObject folder) {
        assert folder.isFolder();
        assert filters != null;
        
        if (filtersCopy == null) {
            filtersCopy = new ArrayList<FileObjectFilter>(filters);
        }
        
        List<FileObjectFilter> result = filtersCopy;
        cycle:
        for (Iterator<FileObjectFilter> i = result.iterator(); i.hasNext(); ) {
            FileObjectFilter filter = i.next();
            final int traverseCommand = filter.traverseFolder(folder);
            switch (traverseCommand) {
                case FileObjectFilter.TRAVERSE:
                    break;
                case FileObjectFilter.DO_NOT_TRAVERSE:
                    result = null;
                    break cycle;
                case FileObjectFilter.TRAVERSE_ALL_SUBFOLDERS:
                    i.remove();
                    filtersCopy = null;
                    break;
                default:
                    assert false;
                    break;
            }
        }
        
        return result;
    }
    
    /**
     * Checks whether the file passes all of the current
     * {@link #filters}.
     *
     * @param  file  file to be checked
     * @return  <code>true</code> if the file passed all of the filters,
     *          <code>false</code> otherwise
     */
    private boolean checkFileFilters(FileObject file) {
        assert file.isFolder() == false;
        assert filters != null;
        
        for (FileObjectFilter filter : filters) {
            if (!filter.searchFile(file)) {
                return false;
            }
        }
        
        return true;
    }

    /**
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
}
