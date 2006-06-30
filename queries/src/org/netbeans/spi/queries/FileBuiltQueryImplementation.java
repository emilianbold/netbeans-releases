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

import org.netbeans.api.queries.FileBuiltQuery;
import org.openide.filesystems.FileObject;

/**
 * Test whether a file can be considered to be built (up to date).
 * Register to default lookup.
 * <p class="nonnormative">
 * Rather than registering a global instance, if your implementation
 * is applicable only to project-owned files, you should add it to
 * <a href="@PROJECTS/PROJECTAPI@/org/netbeans/api/project/Project.html#getLookup"><code>Project.getLookup()</code></a>
 * and depend on
 * the <code>org.netbeans.modules.projectapi</code> module.
 * </p>
 * @see FileBuiltQuery
 * @see <a href="@ANT/PROJECT@/org/netbeans/spi/project/support/ant/AntProjectHelper.html#createGlobFileBuiltQuery(java.lang.String[],%20java.lang.String[])"><code>AntProjectHelper.createGlobFileBuiltQuery(...)</code></a>
 * @author Jesse Glick
 */
public interface FileBuiltQueryImplementation {
    
    /**
     * Check whether a (source) file has been <em>somehow</em> built
     * or processed.
     * This would typically mean that at least its syntax has been
     * validated by a build system, some conventional output file exists
     * and is at least as new as the source file, etc.
     * For example, for a <samp>Foo.java</samp> source file, this could
     * check whether <samp>Foo.class</samp> exists (in the appropriate
     * build directory) with at least as new a timestamp.
     * @param file a source file which can be built to a direct product
     * @return a status object that can be queries and listened to,
     *         or null for no answer
     */
    FileBuiltQuery.Status getStatus(FileObject file);
    
}
