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

import org.netbeans.api.queries.SharabilityQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author  Marian Petras
 */
final class SharabilityFilter implements FileObjectFilter {

    /** 
     */
    public boolean searchFile(FileObject file)
            throws IllegalArgumentException {
        if (file.isFolder()) {
            throw new java.lang.IllegalArgumentException(
                    "file (not folder) expected");                      //NOI18N
        }

        return SharabilityQuery.getSharability(FileUtil.toFile(file))
               != SharabilityQuery.NOT_SHARABLE;
    }

    /**
     */
    public int traverseFolder(FileObject folder)
            throws IllegalArgumentException {
        if (!folder.isFolder()) {
            throw new java.lang.IllegalArgumentException(
                    "folder expected");                                 //NOI18N
        }

        final int sharability = SharabilityQuery
                                .getSharability(FileUtil.toFile(folder));
        switch (sharability) {
            case SharabilityQuery.NOT_SHARABLE:
                return DO_NOT_TRAVERSE;
            case SharabilityQuery.SHARABLE:
                return TRAVERSE_ALL_SUBFOLDERS;
            default:
                return TRAVERSE;
        }
    }

}
