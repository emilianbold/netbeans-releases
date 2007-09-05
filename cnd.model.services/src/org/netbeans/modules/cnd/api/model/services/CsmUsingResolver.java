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

package org.netbeans.modules.cnd.api.model.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUsingDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUsingDirective;
import org.openide.util.Lookup;

/**
 * entry point to resolve using directives and using declarations
 * @author Vladimir Voskresensky
 */
public abstract class CsmUsingResolver {
    /** A dummy resolver that never returns any results.
     */
    private static final CsmUsingResolver EMPTY = new Empty();
    
    /** default instance */
    private static CsmUsingResolver defaultResolver;
    
    protected CsmUsingResolver() {
    }
    
    /** Static method to obtain the resolver.
     * @return the resolver
     */
    public static synchronized CsmUsingResolver getDefault() {
        if (defaultResolver != null) {
            return defaultResolver;
        }
        defaultResolver = Lookup.getDefault().lookup(CsmUsingResolver.class);
        return defaultResolver == null ? EMPTY : defaultResolver;
    }
    
    /**
     * return all using declarations visible for offsetable element, i.e.
     *  using std::cout;
     *  using std::printf;
     *  
     *  void method(){
     *  }
     * returns: std::printf() + std::cout
     *
     * @return sorted collection of declarations visible for input offsetable element through "using" declarations
     */
    public abstract Collection<CsmDeclaration> findUsedDeclarations(CsmFile file, int offset, CsmProject onlyInProject);
    
    /**
     * return all namespace visible for offsetable element, i.e.
     *  using namespace std;
     *  using namespace myNS;
     *  
     *  void method(){
     *  }
     * returns: global namespace (the container of method()) + myNs + std 
     * @return sorted collection of namespaces visible for input offsetable element
     */
    public abstract Collection<CsmNamespace> findVisibleNamespaces(CsmFile file, int offset, CsmProject onlyInProject);

    /**
     * converts collection of using declarations into ordered list of namespaces
     * each namespace occurs only once according it's first using directive in 'decls' list
     */
    public static Collection<CsmNamespace> extractNamespaces(Collection<CsmUsingDirective> decls) {
        // TODO check the correctness of order
        LinkedHashMap<String, CsmNamespace> out = new LinkedHashMap<String, CsmNamespace>(decls.size());
        for (CsmUsingDirective decl : decls) {
            CsmNamespace ref = decl.getReferencedNamespace();
            if (ref != null) {
                String name = decl.getName();
                // remove previous inclusion
                out.remove(name);
                out.put(name, ref);
            }
        }
        return new ArrayList<CsmNamespace>(out.values());
    }
    
    /**
     * converts collection of using declarations into ordered list of namespaces
     * each namespace occurs only once according it's first using directive in 'decls' list
     */
    public static Collection<CsmDeclaration> extractDeclarations(Collection<CsmUsingDeclaration> decls) {
        // TODO check the correctness of order
        LinkedHashMap<String, CsmDeclaration> out = new LinkedHashMap<String, CsmDeclaration>(decls.size());
        for (CsmUsingDeclaration decl : decls) {
            CsmDeclaration ref = decl.getReferencedDeclaration();
            if (ref != null) {
                String name = decl.getName();
                // remove previous inclusion
                out.remove(name);
                out.put(name, ref);
            }
        }
        return new ArrayList<CsmDeclaration>(out.values());        
    }
    
    //
    // Implementation of the default resolver
    //
    private static final class Empty extends CsmUsingResolver {
        Empty() {
        }

        public Collection<CsmDeclaration> findUsedDeclarations(CsmFile file, int offset, CsmProject onlyInProject) {
            return Collections.<CsmDeclaration>emptyList();
        }

        public Collection<CsmNamespace> findVisibleNamespaces(CsmFile file, int offset, CsmProject onlyInProject) {
            return Collections.<CsmNamespace>emptyList();
        }
    }    
}
