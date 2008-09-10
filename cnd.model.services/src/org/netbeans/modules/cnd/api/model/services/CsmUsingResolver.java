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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
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
     * @return sorted unmodifiable collection of declarations visible for input offsetable element through "using" declarations
     */
    public abstract Collection<CsmDeclaration> findUsedDeclarations(CsmFile file, int offset, CsmProject onlyInProject);
    
    /**
     * Finds all declarations visible in given namespace through "using" delcarations.
     * 
     * @param namespace  namespace of interest
     * @return unmodifiable collection of declarations visible in given namespace through "using" declarations
     */
    public abstract Collection<CsmDeclaration> findUsedDeclarations(CsmNamespace namespace);
    
    /**
     * return all namespace visible for offsetable element, i.e.
     *  using namespace std;
     *  using namespace myNS;
     *  
     *  void method(){
     *  }
     * returns: global namespace (the container of method()) + myNs + std 
     * @return sorted unmodifiable collection of namespaces visible for input offsetable element
     */
    public abstract Collection<CsmNamespace> findVisibleNamespaces(CsmFile file, int offset, CsmProject onlyInProject);

    /**
     * Finds all "using" directives in given namespace.
     * 
     * @param namespace  namespace of interest
     * @return unmodifiable collection of "using" directives in given namespace
     */
    public abstract Collection<CsmUsingDirective> findUsingDirectives(CsmNamespace namespace);

    /**
     * Finds all namespaces visible in given namespace through "using" directives.
     * 
     * @param namespace  namespace of interest
     * @return unmodifiable collection of namespaces visible in given namespace though "using" directives
     */
    public Collection<CsmNamespace> findVisibleNamespaces(CsmNamespace namespace) {
        return extractNamespaces(findUsingDirectives(namespace));
    }

//    /**
//     * Finds all direct visible namespace definitions.
//     * 
//     * @param namespace  namespace of interest
//     * @return unmodifiable collection of namespace definitions direct visible in includes
//     */
//    public abstract Collection<CsmNamespaceDefinition> findDirectVisibleNamespaceDefinitions(CsmFile file, int offset, CsmProject onlyInProject);

    /**
     * return all namespace aliases visible for offsetable element, i.e.
     *  namespace B = A;
     *  namespace D = E;
     *  
     *  void method(){
     *  }
     * returns: B + D
     * @return sorted unmodifiable collection of namespace aliases visible for input offsetable element
     */
    public abstract Collection<CsmNamespaceAlias> findNamespaceAliases(CsmFile file, int offset, CsmProject onlyInProject);
    
    /**
     * converts collection of using declarations into ordered list of namespaces
     * each namespace occurs only once according it's first using directive in 'decls' list
     */
    public static Collection<CsmNamespace> extractNamespaces(Collection<CsmUsingDirective> decls) {
        // TODO check the correctness of order
        Collection<Pair> namespaces = new LinkedHashSet<Pair>();
        for (CsmUsingDirective decl : decls) {
            CsmNamespace ref = decl.getReferencedNamespace();
            if (ref != null) {
                CsmFile file = decl.getContainingFile();
                if(file != null) {
                    CsmProject proj = file.getProject();
                    if(proj != null) {
                        Pair p = new Pair(ref,proj);
                        namespaces.remove(p);
                        namespaces.add(p);
                    }
                }
            }
        }
        Collection<CsmNamespace> out = new LinkedHashSet<CsmNamespace>();
        for(Pair p : namespaces){
            for(CsmNamespace ns : findNamespacesInProject(p.proj, p.fqn)){
                out.remove(ns);
                out.add(ns);
            }
        }
        return out;
    }

    /**
     * Finds namespace in project and it's libraries
     * 
     * @param project - project
     * @param namespaceQualifiedName - namespace name
     * @return collection of namespaces
     */
    private static Collection<CsmNamespace> findNamespacesInProject(CsmProject project, CharSequence namespaceQualifiedName) {
        HashSet<CsmProject> scannedProjects = new HashSet<CsmProject>();
        Collection<CsmNamespace> out = new ArrayList<CsmNamespace>();
        CsmNamespace namespace = project.findNamespace(namespaceQualifiedName);
        if(namespace != null) {                
            out.add(namespace);
        }
        scannedProjects.add(project);
        out.addAll(findNamespacesInProjects(project.getLibraries(), namespaceQualifiedName, scannedProjects));
        return out;        
    }
    
    /**
     * Finds namespace in projects and libraries
     * 
     * @param project - project
     * @param namespaceQualifiedName - namespace name
     * @param scannedProjects - set of already scanned projects
     * @return collection of namespaces
     */
    private static Collection<CsmNamespace> findNamespacesInProjects(Collection<CsmProject> projects, CharSequence namespaceQualifiedName, HashSet<CsmProject> scannedProjects) {
        Collection<CsmNamespace> out = new ArrayList<CsmNamespace>();
        for (CsmProject proj : projects) {
            if(!scannedProjects.contains(proj)) {
                CsmNamespace namespace = proj.findNamespace(namespaceQualifiedName);
                if (namespace != null) {
                    out.add(namespace);
                }
                scannedProjects.add(proj);
                Collection<CsmProject> libs = proj.getLibraries();
                if (!libs.isEmpty()) {
                    out.addAll(findNamespacesInProjects(libs, namespaceQualifiedName, scannedProjects));
                }
            }
        }        
        return out;
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
                String name = decl.getName().toString();
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
        
        public Collection<CsmDeclaration> findUsedDeclarations(CsmNamespace namespace) {
            return Collections.<CsmDeclaration>emptyList();
        }

        public Collection<CsmUsingDirective> findUsingDirectives(CsmNamespace namespace) {
            return Collections.<CsmUsingDirective>emptyList();
        }

        public Collection<CsmNamespace> findVisibleNamespaces(CsmFile file, int offset, CsmProject onlyInProject) {
            return Collections.<CsmNamespace>emptyList();
        }
        
//        public Collection<CsmNamespaceDefinition> findDirectVisibleNamespaceDefinitions(CsmFile file, int offset, CsmProject onlyInProject) {
//            return Collections.<CsmNamespaceDefinition>emptyList();
//        }
    
        public Collection<CsmNamespaceAlias> findNamespaceAliases(CsmFile file, int offset, CsmProject onlyInProject) {
            return Collections.<CsmNamespaceAlias>emptyList();
        }        
    }    

    private static class Pair {
        private final CharSequence fqn;
        private CsmProject proj;
        private Pair(CsmNamespace ref, CsmProject proj){
            this.fqn = ref.getQualifiedName();
            this.proj = proj;
        }

        @Override
        public int hashCode() {
            return fqn.hashCode()+proj.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Pair) {
                Pair p = (Pair)obj;
                return fqn.equals(p.fqn) && proj.equals(p.proj);
            }
            return false;
        }
    }
}
