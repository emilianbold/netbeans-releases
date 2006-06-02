/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.queries;

import java.io.File;
import org.netbeans.spi.queries.CollocationQueryImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * Find out whether some files logically belong in one directory tree,
 * for example as part of a VCS checkout.
 * @see CollocationQueryImplementation
 * @author Jesse Glick
 */
public final class CollocationQuery {
    
    private static final Lookup.Result<CollocationQueryImplementation> implementations =
        Lookup.getDefault().lookupResult(CollocationQueryImplementation.class);
    
    private CollocationQuery() {}
    
    /**
     * Check whether two files are logically part of one directory tree.
     * For example, if both files are stored in CVS, with the same server
     * (<code>CVSROOT</code>) they might be considered collocated.
     * If nothing is known about them, return false.
     * @param file1 one file
     * @param file2 another file
     * @return true if they are probably part of one logical tree
     */
    public static boolean areCollocated(File file1, File file2) {
        if (!file1.equals(FileUtil.normalizeFile(file1))) {
            throw new IllegalArgumentException("Parameter file1 was not "+  // NOI18N
                "normalized. Was "+file1+" instead of "+FileUtil.normalizeFile(file1));  // NOI18N
        }
        if (!file2.equals(FileUtil.normalizeFile(file2))) {
            throw new IllegalArgumentException("Parameter file2 was not "+  // NOI18N
                "normalized. Was "+file2+" instead of "+FileUtil.normalizeFile(file2));  // NOI18N
        }
        for (CollocationQueryImplementation cqi : implementations.allInstances()) {
            if (cqi.areCollocated(file1, file2)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Find a root of a logical tree containing this file, if any.
     * @param file a file on disk
     * @return an ancestor directory which is the root of a logical tree,
     *         if any (else null)
     */
    public static File findRoot(File file) {
        if (!file.equals(FileUtil.normalizeFile(file))) {
            throw new IllegalArgumentException("Parameter file was not "+  // NOI18N
                "normalized. Was "+file+" instead of "+FileUtil.normalizeFile(file));  // NOI18N
        }
        for (CollocationQueryImplementation cqi : implementations.allInstances()) {
            File root = cqi.findRoot(file);
            if (root != null) {
                return root;
            }
        }
        return null;
    }
    
}
