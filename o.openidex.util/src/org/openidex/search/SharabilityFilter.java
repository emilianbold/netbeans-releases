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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004 Sun
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
