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

import org.netbeans.api.queries.VisibilityQuery;
import org.openide.filesystems.FileObject;

/**
 *
 * @author  Marian Petras
 */
final class VisibilityFilter implements FileObjectFilter {
    
    /** 
     */
    public boolean searchFile(FileObject file)
            throws IllegalArgumentException {
        if (file.isFolder()) {
            throw new java.lang.IllegalArgumentException(
                    "file (not folder) expected");                      //NOI18N
        }
        return VisibilityQuery.getDefault().isVisible(file);
    }

    /**
     */
    public int traverseFolder(FileObject folder)
            throws IllegalArgumentException {
        if (!folder.isFolder()) {
            throw new java.lang.IllegalArgumentException(
                    "folder expected");                                 //NOI18N
        }
        return VisibilityQuery.getDefault().isVisible(folder)
               ? TRAVERSE
               : DO_NOT_TRAVERSE;
    }

}
