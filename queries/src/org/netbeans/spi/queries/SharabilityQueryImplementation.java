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

package org.netbeans.spi.queries;

import java.io.File;

/**
 * Determine whether files should be shared (for example in a VCS) or are intended
 * to be unshared.
 * <div class="nonnormative">
 * <p>
 * Could be implemented e.g. by project types which know that certain files or folders in
 * a project (e.g. <samp>src/</samp>) are intended for VCS sharing while others
 * (e.g. <samp>build/</samp>) are not.
 * </p>
 * <p>
 * Note that the Project API module registers a default implementation of this query
 * which delegates to the project which owns the queried file, if there is one.
 * This is more efficient than searching instances in global lookup, so use that
 * facility wherever possible.
 * </p>
 * </div>
 * <p>
 * Threading note: implementors should avoid acquiring locks that might be held
 * by other threads. Generally treat this interface similarly to SPIs in
 * {@link org.openide.filesystems} with respect to threading semantics.
 * </p>
 * @see org.netbeans.api.queries.SharabilityQuery
 * @see <a href="@ANT/PROJECT@/org/netbeans/spi/project/support/ant/AntProjectHelper.html#createSharabilityQuery(java.lang.String[],%20java.lang.String[])"><code>AntProjectHelper.createSharabilityQuery(...)</code></a>
 * @author Jesse Glick
 */
public interface SharabilityQueryImplementation {
    
    /**
     * Check whether a file or directory should be shared.
     * If it is, it ought to be committed to a VCS if the user is using one.
     * If it is not, it is either a disposable build product, or a per-user
     * private file which is important but should not be shared.
     * @param file a {@link org.openide.filesystems.FileUtil#normalizeFile normalized} file to check for sharability (may or may not yet exist)
     * @return one of {@link org.netbeans.api.queries.SharabilityQuery}'s constants
     */
    int getSharability(File file);
    
}
