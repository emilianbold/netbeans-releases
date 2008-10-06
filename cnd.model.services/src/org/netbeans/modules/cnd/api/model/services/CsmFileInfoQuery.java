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

package org.netbeans.modules.cnd.api.model.services;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.openide.util.Lookup;

/**
 * query to obtain information associated with CsmFile object
 * 
 * @author Vladimir Voskresensky
 */
public abstract class CsmFileInfoQuery {
    /** A dummy resolver that never returns any results.
     */
    private static final CsmFileInfoQuery EMPTY = new Empty();
    
    /** default instance */
    private static CsmFileInfoQuery defaultResolver;
    
    protected CsmFileInfoQuery() {
    }
    
    /** Static method to obtain the resolver.
     * @return the resolver
     */
    public static synchronized CsmFileInfoQuery getDefault() {
        if (defaultResolver != null) {
            return defaultResolver;
        }
        defaultResolver = Lookup.getDefault().lookup(CsmFileInfoQuery.class);
        return defaultResolver == null ? EMPTY : defaultResolver;
    }
    
    /**
     * @return list of system include paths used to parse file
     */
    public abstract List<String> getSystemIncludePaths(CsmFile file);
    
    /**
     * @return list of user include paths used to parse file
     */
    public abstract List<String> getUserIncludePaths(CsmFile file);
    
    /**
     * @return list of code blocks which are excluded from compilation
     * due to current set of preprocessor directives
     */
    public abstract List<CsmOffsetable> getUnusedCodeBlocks(CsmFile file);

    /**
     * @return list of macro's usages in the file
     */
    public abstract List<CsmReference> getMacroUsages(CsmFile file);

    /**
     * @return dwarf block offset or null if there are no dwarf blocks in file
     */
    public abstract CsmOffsetable getGuardOffset(CsmFile file);

    /**
     * @return native file item associated with model file
     */
    public abstract NativeFileItem getNativeFileItem(CsmFile file);
    
    /**
     * 
     * @param file header file (for sourse file result is empty list)
     * @return list of include directives from source file to header file
     */
    public abstract List<CsmInclude> getIncludeStack(CsmFile file);

    //
    // Implementation of the default query
    //
    private static final class Empty extends CsmFileInfoQuery {
        Empty() {
        }

        public List<String> getSystemIncludePaths(CsmFile file) {
            return Collections.<String>emptyList();
        }

        public List<String> getUserIncludePaths(CsmFile file) {
            return Collections.<String>emptyList();
        }

        public List<CsmOffsetable> getUnusedCodeBlocks(CsmFile file) {
            return Collections.<CsmOffsetable>emptyList();
        }

        public List<CsmReference> getMacroUsages(CsmFile file) {
            return Collections.<CsmReference>emptyList();
        }

        public CsmOffsetable getGuardOffset(CsmFile file) {
            return null;
        }

        @Override
        public NativeFileItem getNativeFileItem(CsmFile file) {
            return null;
        }

        @Override
        public List<CsmInclude> getIncludeStack(CsmFile file) {
            return Collections.<CsmInclude>emptyList();
        }
    } 
}
