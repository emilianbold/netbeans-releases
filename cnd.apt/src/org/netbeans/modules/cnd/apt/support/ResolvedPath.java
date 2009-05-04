/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.apt.support;

import org.netbeans.modules.cnd.utils.cache.FilePathCache;

/**
 *
 * @author Alexander Simon
 */
public final class ResolvedPath {
    private final CharSequence folder;
    private final CharSequence path;
    private final CharSequence notNormalizedPath;
    private final boolean isDefaultSearchPath;
    private final int index;
    
    public ResolvedPath(CharSequence folder, CharSequence path, CharSequence notNormalizedPath, boolean isDefaultSearchPath, int index) {
        this.folder = FilePathCache.getManager().getString(folder);
        this.path = FilePathCache.getManager().getString(path);
        this.notNormalizedPath = FilePathCache.getManager().getString(notNormalizedPath);
        this.isDefaultSearchPath = isDefaultSearchPath;
        this.index = index;
    }
    /**
     * Resolved file path (normalized version)
     */
    public CharSequence getPath(){
        return path;
    }

    /**
     * Resolved file path (not normalized version)
     * @return
     */
    public CharSequence getNotNormalizedPath() {
        return notNormalizedPath;
    }

    /**
     * Include path used for resolving file path
     */
    public CharSequence getFolder(){
        return folder;
    }

    /**
     * Returns true if path resolved from default path
     */
    public boolean isDefaultSearchPath(){
        return isDefaultSearchPath;
    }

    /**
     * Returns index of resolved path in user and system include paths
     */
    public int getIndex(){
        return index;
    }
    
    @Override
    public String toString(){
        return path+" in "+folder; // NOI18N
    }
}
