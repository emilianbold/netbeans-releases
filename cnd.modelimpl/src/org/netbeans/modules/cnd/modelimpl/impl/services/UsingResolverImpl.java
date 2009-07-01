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

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUsingDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUsingDirective;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmUsingResolver;

/**
 * implementation of using directives and using declarations resolver
 * @author Vladimir Voskresensky
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.services.CsmUsingResolver.class)
public final class UsingResolverImpl extends CsmUsingResolver implements CsmProgressListener {
    
    public UsingResolverImpl() {
        if (cache) {
            CsmListeners.getDefault().addProgressListener(this);
        }
    }
    
    public Collection<CsmDeclaration> findUsedDeclarations(CsmFile file, int offset, CsmProject onlyInProject) {
        return getCollector(file, offset, onlyInProject).getUsedDeclarations();
    }
    
    public Collection<CsmDeclaration> findUsedDeclarations(CsmNamespace namespace) {
        List<CsmUsingDeclaration> res = new ArrayList<CsmUsingDeclaration>();
        Iterator<CsmOffsetableDeclaration> udecls = CsmSelect.getDeclarations(
                    namespace, CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.USING_DECLARATION));
        while (udecls.hasNext()) {
            res.add((CsmUsingDeclaration) udecls.next());
        }
        // Let's also look for similarly named namespace in libraries,
        // like it's done in CsmProjectContentResolver.getNamespaceMembers()
        if (!namespace.isGlobal()) {
            for(CsmProject lib : namespace.getProject().getLibraries()){
                CsmNamespace ns = lib.findNamespace(namespace.getQualifiedName());
                if (ns != null) {
                    Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDeclarations(
                            ns, CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.USING_DECLARATION));
                    while (it.hasNext()) {
                        res.add((CsmUsingDeclaration) it.next());
                    }
                }
            }
        }
        return extractDeclarations(res);
    }
    
    public Collection<CsmNamespace> findVisibleNamespaces(CsmFile file, int offset, CsmProject onlyInProject) {
        Set<CsmNamespace> seen = new LinkedHashSet<CsmNamespace>();
        Queue<CsmNamespace> queue = new LinkedList<CsmNamespace>(
                getCollector(file, offset, onlyInProject).getVisibleNamespaces());
        findVisibleNamespacesBfs(seen, queue, onlyInProject, file.getProject());
        return seen;
    }

    private void findVisibleNamespacesBfs(Set<CsmNamespace> seen, Queue<CsmNamespace> queue, CsmProject onlyInProject, CsmProject startProject) {
        // breadth-first search in namespace inclusion graph
        while (!queue.isEmpty()) {
            CsmNamespace namespace = queue.poll();
            for (CsmNamespace used : findVisibleNamespaces(namespace, startProject)) {
                if (!seen.contains(used) && !queue.contains(used) &&
                        (onlyInProject == null || onlyInProject == used.getProject())) {
                    queue.add(used);
                }
            }
            seen.add(namespace);
        }
    }

//    public Collection<CsmNamespaceDefinition> findDirectVisibleNamespaceDefinitions(CsmFile file, int offset, CsmProject onlyInProject) {
//        return getCollector(file, offset, onlyInProject).getDirectVisibleNamespaceDefinitions();
//    }
    
    public Collection<CsmUsingDirective> findUsingDirectives(CsmNamespace namespace) {
        List<CsmUsingDirective> res = new ArrayList<CsmUsingDirective>();
        Iterator<CsmOffsetableDeclaration> udirs = CsmSelect.getDeclarations(
                    namespace, CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.USING_DIRECTIVE));
        while (udirs.hasNext()) {
            res.add((CsmUsingDirective)udirs.next());
        }
        return res;
    }

    public Collection<CsmNamespaceAlias> findNamespaceAliases(CsmFile file, int offset, CsmProject onlyInProject) {
        return getCollector(file, offset, onlyInProject).getNamespaceAliases();
    }

    public Collection<CsmNamespaceAlias> findNamespaceAliases(CsmNamespace namespace) {
        List<CsmNamespaceAlias> res = new ArrayList<CsmNamespaceAlias>();
        Iterator<CsmOffsetableDeclaration> udirs = CsmSelect.getDeclarations(
                    namespace, CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.NAMESPACE_ALIAS));
        while (udirs.hasNext()) {
            res.add((CsmNamespaceAlias)udirs.next());
        }
        return res;
    }

    /**
     * converts collection of using declarations into ordered list of namespaces
     * each namespace occurs only once according it's first using directive in 'decls' list
     */
    public static Collection<CsmNamespace> extractNamespaces(Collection<CsmUsingDirective> decls, CsmProject startPrj) {
        // TODO check the correctness of order
        Collection<Pair> namespaces = new LinkedHashSet<Pair>();
        for (CsmUsingDirective decl : decls) {
            CsmNamespace ref = decl.getReferencedNamespace();
            if (ref != null) {
                CsmFile file = decl.getContainingFile();
                if (file != null) {
                    CsmProject proj = file.getProject();
                    if (proj != null) {
                        Pair p = new Pair(ref, proj);
                        namespaces.remove(p);
                        namespaces.add(p);
                    }
                }
            }
        }
        Collection<CsmNamespace> out = new LinkedHashSet<CsmNamespace>();
        Collection<CsmProject> libraries = startPrj.getLibraries();
        for (Pair p : namespaces) {
            for (CsmNamespace ns : findNamespacesInProject(p.proj, p.fqn, libraries)) {
                out.remove(ns);
                out.add(ns);
            }
        }
        return out;
    }

    /**
     * Finds all namespaces visible in given namespace through "using" directives.
     *
     * @param namespace  namespace of interest
     * @return unmodifiable collection of namespaces visible in given namespace though "using" directives
     */
    public Collection<CsmNamespace> findVisibleNamespaces(CsmNamespace namespace, CsmProject startPrj) {
        List<CsmNamespace> res = new ArrayList<CsmNamespace>();
        if (!namespace.isGlobal()) {
            for (CsmNamespace ns : namespace.getNestedNamespaces()) {
                if (ns.getName().length() == 0) {
                    res.add(ns);
                }
            }
        }
        res.addAll(extractNamespaces(findUsingDirectives(namespace), startPrj));
        return res;
    }

    /**
     * Finds namespace in project and it's libraries
     *
     * @param project - project
     * @param namespaceQualifiedName - namespace name
     * @return collection of namespaces
     */
    private static Collection<CsmNamespace> findNamespacesInProject(CsmProject project, CharSequence namespaceQualifiedName, Collection<CsmProject> libs) {
        HashSet<CsmProject> scannedProjects = new HashSet<CsmProject>();
        Collection<CsmNamespace> out = new ArrayList<CsmNamespace>();
        CsmNamespace namespace = project.findNamespace(namespaceQualifiedName);
        if (namespace != null) {
            out.add(namespace);
        }
        scannedProjects.add(project);
        out.addAll(findNamespacesInProjects(libs, namespaceQualifiedName, scannedProjects));
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
            if (!scannedProjects.contains(proj)) {
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

    private static class Pair {

        private final CharSequence fqn;
        private CsmProject proj;

        private Pair(CsmNamespace ref, CsmProject proj) {
            this.fqn = ref.getQualifiedName();
            this.proj = proj;
        }

        @Override
        public int hashCode() {
            return fqn.hashCode() + proj.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Pair) {
                Pair p = (Pair) obj;
                return fqn.equals(p.fqn) && proj.equals(p.proj);
            }
            return false;
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    // try to cache a little the last request
    
    private static final class Lock {}
    private final Object lock = new Lock();
    private Reference<SearchInfo> lastSearch = new SoftReference<SearchInfo>(null);
    
    private final boolean cache = true;
    private FileElementsCollector getCollector(CsmFile file, int offset, CsmProject onlyInProject) {
        if (!cache) {
            return new FileElementsCollector(file, offset, onlyInProject);
        } else {
            synchronized (lock) {
                SearchInfo search = lastSearch.get();
                if (search == null || !search.valid(file, offset, onlyInProject)) {
                    FileElementsCollector collector = new FileElementsCollector(file, offset, onlyInProject);
                    search = new SearchInfo(file, offset, onlyInProject, collector);
                    lastSearch = new SoftReference<SearchInfo>(search);
                } else {
                    search.offset = offset;
                    search.collector.incrementOffset(offset);
                }
                assert search != null;
                assert search.collector != null;
                return search.collector;
            }
        }
    }
    
    private static final class SearchInfo {
        public final CsmFile file;
        public int offset;
        public final FileElementsCollector collector;
        public final CsmProject onlyInProject;
        public SearchInfo(CsmFile file, int offset, CsmProject onlyInProject, FileElementsCollector collector) {
            this.file = file;
            this.offset = offset;
            this.collector = collector;
            this.onlyInProject = onlyInProject;
        }
        
        private boolean valid(CsmFile file, int offset, CsmProject onlyInProject) {
            return this.file.equals(file) && this.offset <= offset && this.onlyInProject == onlyInProject;
        }
    }
    
    public void projectParsingStarted(CsmProject project) {
    }
    
    public void projectFilesCounted(CsmProject project, int filesCount) {
    }
    
    public void projectParsingFinished(CsmProject project) {
        cleanCache();
    }
    
    public void projectParsingCancelled(CsmProject project) {
    }
    
    public void fileInvalidated(CsmFile file) {
    }

    public void fileAddedToParse(CsmFile file) {
    }

    public void fileParsingStarted(CsmFile file) {
    }
    
    public void fileParsingFinished(CsmFile file) {
        cleanCache();
    }
    
    public void projectLoaded(CsmProject project) {
    }
    
    public void parserIdle() {
    }
    
    private void cleanCache() {
        synchronized (lock) {
            lastSearch.clear();
        }
    }
}
