/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.queries;

import java.io.File;
import java.util.Iterator;
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
    
    private static final Lookup.Result/*<CollocationQueryImplementation>*/ implementations =
        Lookup.getDefault().lookup(new Lookup.Template(CollocationQueryImplementation.class));
    
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
        assert file1.equals(FileUtil.normalizeFile(file1)) : file1;
        assert file2.equals(FileUtil.normalizeFile(file2)) : file2;
        Iterator it = implementations.allInstances().iterator();
        while (it.hasNext()) {
            CollocationQueryImplementation cqi = (CollocationQueryImplementation)it.next();
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
        assert file.equals(FileUtil.normalizeFile(file)) : file;
        Iterator it = implementations.allInstances().iterator();
        while (it.hasNext()) {
            CollocationQueryImplementation cqi = (CollocationQueryImplementation)it.next();
            File root = cqi.findRoot(file);
            if (root != null) {
                return root;
            }
        }
        return null;
    }
    
}
