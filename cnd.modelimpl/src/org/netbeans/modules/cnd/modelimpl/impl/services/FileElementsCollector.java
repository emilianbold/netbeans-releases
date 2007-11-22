/*
 * The contents of this destFile are subject to the terms of the Common Development
 *
The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
particular file as subject to the "Classpath" exception as provided
by Sun in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmUsingDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUsingDirective;
import org.netbeans.modules.cnd.api.model.deep.CsmDeclarationStatement;
import org.netbeans.modules.cnd.api.model.services.CsmUsingResolver;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;

/**
 *
 * @author Vladimir Voskresensky
 */
public class FileElementsCollector {
    private final CsmFile destFile;
    private int destOffset;
    private final CsmProject onlyInProject;
    
    private Set<CsmFile> visitedFiles = new HashSet<CsmFile>();
    
    private final ProjectBase project;
    
//    public FileElementsCollector(CsmOffsetable offsetable) {
//        this(offsetable.getContainingFile(), offsetable.getStartOffset(), null);
//    }
    
    public FileElementsCollector(CsmFile file, int offset, CsmProject onlyInProject) {
        this.destFile = file;
        this.project = (ProjectBase) file.getProject();
        this.destOffset = offset;
        this.onlyInProject = onlyInProject;
    }
    
    //private CsmNamespace currentNamespace;
    
//    public FileElementsCollector(CsmFile destFile, int destOffset, Resolver parent) {
//        this.destFile = destFile;
//        this.destOffset = destOffset;
//        parentResolver = parent;
//        this.project = (ProjectBase) destFile.getProject();
//    }
//
//    public FileElementsCollector(CsmOffsetable context, Resolver parent) {
//        this.destFile = context.getContainingFile();
//        this.destOffset = context.getStartOffset();
//        parentResolver = parent;
//        this.project = (ProjectBase) context.getContainingFile().getProject();
//    }
    
    
    private LinkedHashSet<CsmNamespace> directVisibleNamespaces = new LinkedHashSet<CsmNamespace>();
    private LinkedHashSet<CsmUsingDirective> usingNamespaces = new LinkedHashSet<CsmUsingDirective>();
    private LinkedHashSet<CsmNamespaceAlias> namespaceAliases = new LinkedHashSet<CsmNamespaceAlias>()/*<String, CsmNamespace>*/;
    private LinkedHashSet<CsmUsingDeclaration> usingDeclarations = new LinkedHashSet<CsmUsingDeclaration>()/*<String, CsmDeclaration>*/;
    
    private CsmTypedef currTypedef;
    
    private String[] names;
    private int currNamIdx;
    
    private String currName() {
        return (names != null && currNamIdx < names.length) ? names[currNamIdx] : "";
    }
    
    private CsmNamespace containingNamespace;
    private CsmClass containingClass;
    
    private CsmNamespace getContainingNamespace() {
        initContext();
        return containingNamespace;
    }
    
    private CsmClass getContainingClass() {
        initContext();
        return containingClass;
    }
    
    public Collection<CsmUsingDeclaration> getUsingDeclarations() {
        initMaps();
        return Collections.unmodifiableCollection(usingDeclarations);
    }
    
    public Collection<CsmUsingDirective> getUsingDirectives() {
        initMaps();
        return Collections.unmodifiableCollection(usingNamespaces);
    }
    
    public Collection<CsmNamespaceAlias> getNamespaceAliases() {
        initMaps();
        return Collections.unmodifiableCollection(namespaceAliases);
    }
    
    private Collection<CsmDeclaration> visibleUsedDeclarations = null;
    public Collection<CsmDeclaration> getUsedDeclarations() {
        initMaps();
        if (visibleUsedDeclarations == null) {
            visibleUsedDeclarations = CsmUsingResolver.extractDeclarations(usingDeclarations);
        }
        return Collections.unmodifiableCollection(visibleUsedDeclarations);
    }
    
    private Collection<CsmNamespace> visibleNamespaces = null;
    public Collection<CsmNamespace> getVisibleNamespaces() {
        initMaps();
        if (visibleNamespaces == null) {
            visibleNamespaces = CsmUsingResolver.extractNamespaces(usingNamespaces);
            // add scope's and unnamed visible namespaces
            visibleNamespaces.addAll(directVisibleNamespaces);
        }
        return Collections.unmodifiableCollection(visibleNamespaces);
    }
    
    private boolean contextFound = false;
    private void initContext() {
        if( contextFound ) {
            return;
        }
        contextFound = true;
        findContext(destFile.getDeclarations());
    }
    
    private boolean mapsGathered = false;
    private void initMaps() {
        if( mapsGathered ) {
            return;
        }
        mapsGathered = true;
        gatherFileMaps(this.destFile);
//        initContext();
    }
    
//    private CsmClassifier findClassifier(CsmNamespace ns, String qulifiedNamePart) {
//        CsmClassifier result = null;
//        while ( ns != null  && result == null) {
//            String fqn = ns.getQualifiedName() + "::" + qulifiedNamePart; // NOI18N
//            result = findClassifier(fqn);
//            ns = ns.getParent();
//        }
//        return result;
//    }
//
//    private CsmClassifier findClassifier(String qualifiedName) {
//        CsmClassifier result = project.findClassifier(qualifiedName);
//        if( result == null ) {
//            for (Iterator iter = project.getLibraries().iterator(); iter.hasNext() && result == null;) {
//                CsmProject lib = (CsmProject) iter.next();
//                result = lib.findClassifier(qualifiedName);
//            }
//        }
//        return result;
//    }
//
//    public CsmNamespace findNamespace(String qualifiedName) {
//        CsmNamespace result = project.findNamespace(qualifiedName);
//        if( result == null ) {
//            for (Iterator iter = project.getLibraries().iterator(); iter.hasNext() && result == null;) {
//                CsmProject lib = (CsmProject) iter.next();
//                result = lib.findNamespace(qualifiedName);
//            }
//        }
//        return result;
//    }
    
    private boolean isInside(CsmOffsetable elem, int offset) {
        return elem.getStartOffset() < offset && offset < elem.getEndOffset();
    }
    
    private void findContext(Iterable declarations) {
        for (Iterator it = declarations.iterator(); it.hasNext();) {
            CsmDeclaration decl = (CsmDeclaration) it.next();
            if(CsmKindUtilities.isNamespaceDefinition(decl)) {
                CsmNamespaceDefinition nd = (CsmNamespaceDefinition) decl;
                if( isInside(nd, this.destOffset) ) {
                    containingNamespace = nd.getNamespace();
                    findContext(nd.getDeclarations());
                }
            } else if(CsmKindUtilities.isClass(decl)) {
                
                CsmClass cls = (CsmClass) decl;
                if( isInside(cls, this.destOffset)  ) {
                    containingClass = cls;
                    Collection inners;
                    if (cls.getFriends().size() > 0) {
                        inners = new TreeSet(FileImpl.START_OFFSET_COMPARATOR);
                        inners.addAll(cls.getMembers());
                        inners.addAll(cls.getFriends());
                    } else {
                        inners = cls.getMembers();
                    }
                    findContext(inners);
                }
            } else if(CsmKindUtilities.isFunctionDefinition(decl)) {
                CsmFunctionDefinition fd = (CsmFunctionDefinition) decl;
                if( isInside(fd, this.destOffset) ) {
                    CsmNamespace ns = getFunctionDefinitionNamespace(fd);
                    if( ns != null && ! ns.isGlobal() ) {
                        containingNamespace = ns;
                    }
                    CsmFunction fun = getFunctionDeclaration(fd);
                    if( fun != null && CsmKindUtilities.isMethodDeclaration(fun) ) {
                        containingClass = ((CsmMethod) fun).getContainingClass();
                    }
                }
            }
        }
    }
    
    private CsmFunction getFunctionDeclaration(CsmFunctionDefinition fd){
        return fd.getDeclaration();
    }
    
    private CsmNamespace getFunctionDefinitionNamespace(CsmFunctionDefinition def) {
        CsmFunction fun = getFunctionDeclaration(def);
        if( fun != null ) {
            CsmScope scope = fun.getScope();
            if( CsmKindUtilities.isNamespace(scope)) {
                CsmNamespace ns = (CsmNamespace) scope;
                return ns;
            } else if( CsmKindUtilities.isClass(scope) ) {
                return getClassNamespace((CsmClass) scope);
            }
        }
        return null;
    }
    
    private CsmNamespace getClassNamespace(CsmClass cls) {
        CsmScope scope = cls.getScope();
        while( scope != null ) {
            if( CsmKindUtilities.isNamespace(scope) ) {
                return (CsmNamespace) scope;
            }
            if( CsmKindUtilities.isScopeElement(scope) ) {
                scope = ((CsmScopeElement)scope).getScope();
            } else {
                break;
            }
        }
        return null;
    }
    
    protected void gatherFileMaps(CsmFile file) {
        if( visitedFiles.contains(file) ) {
            return;
        }
        visitedFiles.add(file);
        
        for (Iterator<CsmInclude> iter = file.getIncludes().iterator(); iter.hasNext();) {
            CsmInclude inc = iter.next();
            // check that include is above the end offset
            if (inc.getEndOffset() < this.destOffset) {
                CsmFile incFile = inc.getIncludeFile();
                if( incFile != null && (onlyInProject == null || incFile.getProject() == onlyInProject)) {
                    // in includes look for everything
                    int oldOffset = this.destOffset;
                    this.destOffset = Integer.MAX_VALUE;
                    gatherFileMaps(incFile);
                    // restore previous value
                    this.destOffset = oldOffset;
                }
            }
        }
        // gother this file maps 
        gatherDeclarationsMaps(file.getDeclarations());
    }
    
    protected void gatherDeclarationsMaps(Iterable declarations) {
        for( Iterator it = declarations.iterator(); it.hasNext(); ) {
            CsmOffsetable o = (CsmOffsetable) it.next();
            try {
                int start = o.getStartOffset();
                int end = o.getEndOffset();
                if( start >= this.destOffset ) {
                    break;
                }
                //assert o instanceof CsmScopeElement;
                if(CsmKindUtilities.isScopeElement((CsmObject)o)) {
                    gatherScopeElementMaps((CsmScopeElement) o, end);
                } else {
                    if( FileImpl.reportErrors ) {
                        System.err.println("Expected CsmScopeElement, got " + o);
                    }
                }
            } catch (NullPointerException ex) {
                if( FileImpl.reportErrors ) {
                    // FIXUP: do not crush on NPE
                    System.err.println("Unexpected NULL element in declarations collection");
                    ex.printStackTrace(System.err);
                }
            }
        }
    }
    
    private void doProcessTypedefsInUpperNamespaces(CsmNamespaceDefinition nsd) {
        for (Iterator iter = nsd.getDeclarations().iterator(); iter.hasNext();) {
            CsmDeclaration decl = (CsmDeclaration) iter.next();
            if( decl.getKind() == CsmDeclaration.Kind.NAMESPACE_DEFINITION ) {
                processTypedefsInUpperNamespaces((CsmNamespaceDefinition) decl);
            } else if( decl.getKind() == CsmDeclaration.Kind.TYPEDEF ) {
                CsmTypedef typedef = (CsmTypedef) decl;
                if( currName().equals(typedef.getName()) ) {
                    currTypedef = typedef;
                }
            }
        }
    }
    
    private void processTypedefsInUpperNamespaces(CsmNamespaceDefinition nsd) {
        if( nsd.getName().equals(currName()) ) {
            currNamIdx++;
            doProcessTypedefsInUpperNamespaces(nsd);
        } else {
            CsmNamespace cns = getContainingNamespace();
            if( cns != null ) {
                if( cns.equals(nsd.getNamespace())) {
                    doProcessTypedefsInUpperNamespaces(nsd);
                }
            }
        }
    }
    
    /**
     * It is quaranteed that element.getStartOffset < this.destOffset
     */
    protected void gatherScopeElementMaps(CsmScopeElement element, int end) {
        
        CsmDeclaration.Kind kind = CsmKindUtilities.isDeclaration(element) ? ((CsmDeclaration) element).getKind() : null;
        if( kind == CsmDeclaration.Kind.NAMESPACE_DEFINITION ) {
            CsmNamespaceDefinition nsd = (CsmNamespaceDefinition) element;
            if (nsd.getName().length() == 0) {
                // this is unnamed namespace and it should be considered as
                // it declares using itself
                directVisibleNamespaces.add(nsd.getNamespace());
            }
            if( this.destOffset < end ) {
                // put in the end of list
                directVisibleNamespaces.remove(nsd.getNamespace());
                directVisibleNamespaces.add(nsd.getNamespace());
                //currentNamespace = nsd.getNamespace();
                gatherDeclarationsMaps(nsd.getDeclarations());
//            } else {
//                processTypedefsInUpperNamespaces(nsd);
            }
        } else if( kind == CsmDeclaration.Kind.NAMESPACE_ALIAS ) {
            CsmNamespaceAlias alias = (CsmNamespaceAlias) element;
            namespaceAliases.add(alias);
//            namespaceAliases.put(alias.getAlias(), alias.getReferencedNamespace());
        } else if( kind == CsmDeclaration.Kind.USING_DECLARATION ) {
            CsmUsingDeclaration udecl = (CsmUsingDeclaration) element;
            usingDeclarations.add(udecl);
//            CsmDeclaration decl = udecl.getReferencedDeclaration();
//            if( decl != null ) {
//                String id;
//                if( decl.getKind() == CsmDeclaration.Kind.FUNCTION || decl.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION ) {
//                    // TODO: decide how to resolve functions
//                    id = ((CsmFunction) decl).getSignature();
//                } else {
//                    id = decl.getName();
//                }
//                usingDeclarations.put(id, decl);
//            }
        } else if( kind == CsmDeclaration.Kind.USING_DIRECTIVE ) {
            CsmUsingDirective udir = (CsmUsingDirective) element;
            usingNamespaces.add(udir);
//            directVisibleNamespaces.add(udir.getName()); // getReferencedNamespace()
        } else if( CsmKindUtilities.isDeclarationStatement(element) ) {
            CsmDeclarationStatement ds = (CsmDeclarationStatement) element;
            if( ds.getStartOffset() < this.destOffset ) {
                gatherDeclarationsMaps( ((CsmDeclarationStatement) element).getDeclarators());
            }
        } else if( CsmKindUtilities.isScope(element) ) {
            if( this.destOffset < end ) {
                gatherDeclarationsMaps( ((CsmScope) element).getScopeElements());
            }
        } else if( kind == CsmDeclaration.Kind.TYPEDEF ) {
            CsmTypedef typedef = (CsmTypedef) element;
            if( currName().equals(typedef.getName()) ) {
                currTypedef = typedef;
            }
        }
    }    
    
}
