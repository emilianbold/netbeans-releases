/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

import org.netbeans.modules.cnd.api.model.CsmFile;
import org.openide.util.Lookup;

/**
 * entry point to search references in files
 * @author Vladimir Voskresensky
 */
public abstract class CsmReferenceResolver {
    /** A dummy resolver that never returns any results.
     */
    private static final CsmReferenceResolver EMPTY = new Empty();
    
    /** default instance */
    private static CsmReferenceResolver defaultResolver;
    
    protected CsmReferenceResolver() {
    }
    
    /** Static method to obtain the resolver.
     * @return the resolver
     */
    public static synchronized CsmReferenceResolver getDefault() {
        if (defaultResolver != null) {
            return defaultResolver;
        }
        defaultResolver = (CsmReferenceResolver) Lookup.getDefault().lookup(CsmReferenceResolver.class);
        return defaultResolver == null ? EMPTY : defaultResolver;
    }
    
    /**
     * look for reference on specified position in file
     * @param file file where to search
     * @param offset position in file to find reference
     * @return reference for element on position "offset", null if not found
     */
    public abstract CsmReference findReference(CsmFile file, int offset);

    /**
     * look for reference on specified position in file
     * @param file file where to search
     * @param line line position in file to find reference
     * @param column column position in file to find reference
     * @return reference for element on position "offset", null if not found
     */
//    public abstract CsmReference findReference(CsmFile file, int line, int column);

    //
    // Implementation of the default resolver
    //
    private static final class Empty extends CsmReferenceResolver {
        Empty() {
        }

        public CsmReference findReference(CsmFile file, int offset) {
            return null;
        }

        public CsmReference findReference(CsmFile file, int line, int column) {
            return null;
        }
    }    
}