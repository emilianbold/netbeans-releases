/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
