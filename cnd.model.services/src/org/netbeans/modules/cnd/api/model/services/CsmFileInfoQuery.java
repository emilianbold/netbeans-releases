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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.api.model.services;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
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
     * @return list of class fields usages in the method's body
     */
    public abstract List<CsmReference> getClassFields(CsmFile file);

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

        public List<CsmReference> getClassFields(CsmFile file) {
            return Collections.<CsmReference>emptyList();
        }
    } 
}
