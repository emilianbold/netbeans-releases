/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
        assert file.equals(FileUtil.normalizeFile(file)) : "Must pass a normalized file: " + file + " vs. " + FileUtil.normalizeFile(file);
        for (SharabilityQueryImplementation sqi : implementations.allInstances()) {
            int x = sqi.getSharability(file);
            if (x != UNKNOWN) {
                return x;
            }
        }
        return UNKNOWN;
    }
    
}
