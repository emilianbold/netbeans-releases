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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.queries;

import java.io.File;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

// XXX perhaps should be in the Filesystems API instead of here?

/**
 * Determine whether files should be shared (for example in a VCS) or are intended
 * to be unshared.
 * Likely to be of use only to a VCS filesystem.
 * <p>
 * This query can be considered to obsolete {@link org.openide.filesystems.FileObject#setImportant}.
 * Unlike that method, the information is pulled by the VCS filesystem on
 * demand, which may be more reliable than ensuring that the information
 * is pushed by a project type (or other implementor) eagerly.
 * @see SharabilityQueryImplementation
 * @author Jesse Glick
 */
public final class SharabilityQuery {
    
    private static final Lookup.Result<SharabilityQueryImplementation> implementations =
        Lookup.getDefault().lookupResult(SharabilityQueryImplementation.class);

    /**
     * Constant indicating that nothing is known about whether a given
     * file should be considered sharable or not.
     * A client should therefore behave in the safest way it can.
     */
    public static final int UNKNOWN = 0;
    
    /**
     * Constant indicating that the file or directory is sharable.
     * In the case of a directory, this means that all files and
     * directories recursively contained in this directory are also
     * sharable.
     */
    public static final int SHARABLE = 1;
    
    /**
     * Constant indicating that the file or directory is not sharable.
     * In the case of a directory, this means that all files and
     * directories recursively contained in this directory are also
     * not sharable.
     */
    public static final int NOT_SHARABLE = 2;
    
    /**
     * Constant indicating that a directory is sharable but files and
     * directories recursively contained in it may or may not be sharable.
     * A client interested in children of this directory should explicitly
     * ask about each in turn.
     */
    public static final int MIXED = 3;
    
    private SharabilityQuery() {}
    
    /**
     * Check whether an existing file is sharable.
     * @param file a file or directory (may or may not already exist)
     * @return one of the constants in this class
     */
    public static int getSharability(File file) {
        if (file == null) throw new IllegalArgumentException();
        assert file.equals(FileUtil.normalizeFile(file)) : "Must pass a normalized file: " + file;
        for (SharabilityQueryImplementation sqi : implementations.allInstances()) {
            int x = sqi.getSharability(file);
            if (x != UNKNOWN) {
                return x;
            }
        }
        return UNKNOWN;
    }
    
}
