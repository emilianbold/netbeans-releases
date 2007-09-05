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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.api.model.services;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
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
    } 
}
