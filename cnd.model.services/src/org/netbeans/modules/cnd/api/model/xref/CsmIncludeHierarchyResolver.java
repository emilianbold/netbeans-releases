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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.api.model.xref;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.openide.util.Lookup;

/**
 * entry point to resolve usages of include directives
 * @author Alexander Simon
 */
public abstract class CsmIncludeHierarchyResolver {
    /** A dummy resolver that never returns any results.
     */
    private static final CsmIncludeHierarchyResolver EMPTY = new Empty();
    
    /** default instance */
    private static CsmIncludeHierarchyResolver defaultResolver;
    
    protected CsmIncludeHierarchyResolver() {
    }
    
    /** Static method to obtain the resolver.
     * @return the resolver
     */
    public static synchronized CsmIncludeHierarchyResolver getDefault() {
        if (defaultResolver != null) {
            return defaultResolver;
        }
        defaultResolver = Lookup.getDefault().lookup(CsmIncludeHierarchyResolver.class);
        return defaultResolver == null ? EMPTY : defaultResolver;
    }
    
    /**
     * Search for usage of referenced file in include directives.
     * Return collection of files that direct include referenced file.
     */
    public abstract Collection<CsmFile> getFiles(CsmFile referencedFile);
    
    /**
     * Search for usage of referenced file in include directives.
     * Return collection of include directives that direct include referenced file.
     */
    public abstract Collection<CsmInclude> getIncldes(CsmFile referencedFile);
    
    //
    // Implementation of the default resolver
    //
    private static final class Empty extends CsmIncludeHierarchyResolver {
        Empty() {
        }

        public Collection<CsmFile> getFiles(CsmFile referencedFile) {
            return Collections.<CsmFile>emptyList();
        }

        public Collection<CsmInclude> getIncldes(CsmFile referencedFile) {
            return Collections.<CsmInclude>emptyList();
        }
    }    
}