/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import org.netbeans.modules.cnd.api.model.*;
import java.util.*;
import org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind;
import org.netbeans.modules.cnd.api.model.deep.CsmDeclarationStatement;
import org.netbeans.modules.cnd.api.model.services.CsmClassifierResolver;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.services.CsmUsingResolver;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.modelimpl.csm.ClassForwardDeclarationImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ForwardClass;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.InheritanceImpl;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceImpl;
import org.netbeans.modules.cnd.modelimpl.csm.UsingDeclarationImpl;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.impl.services.UsingResolverImpl;
import org.netbeans.modules.cnd.modelutil.AntiLoop;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;

/**
 * @author Vladimir Kvasihn
 */
public final class Resolver3 implements Resolver {
    
    private final ProjectBase project;
    private final CsmFile file;
    private final CsmFile startFile;
    private int offset;
    private final int origOffset;
    private Resolver parentResolver;
    
    private final List<CharSequence> usedNamespaces = new ArrayList<CharSequence>();
    private final Map<CharSequence, CsmNamespace> namespaceAliases = new HashMap<CharSequence, CsmNamespace>();
    private final Map<CharSequence, CsmDeclaration> usingDeclarations = new HashMap<CharSequence, CsmDeclaration>();
    
    private CsmTypedef currTypedef;
    private CsmClassifier currLocalClassifier;
    
    private CharSequence[] names;
    private int currNamIdx;
    private int interestedKind;
    private boolean resolveInBaseClass;
    private boolean inLocalContext = false;
    
    private CharSequence currName() {
        return (names != null && currNamIdx < names.length) ? names[currNamIdx] : CharSequenceKey.empty();
    }

    private CsmNamespace containingNamespace;
    private CsmClass containingClass;
    private boolean contextFound = false;

    private CsmNamespace getContainingNamespace() {
        if( ! contextFound ) {
            findContext();
        }
        return containingNamespace;
    }
    
    private CsmClass getContainingClass() {
        if( ! contextFound ) {
            findContext();
        }
        return containingClass;
    }
    
    private void findContext() {
        contextFound = true;
        CsmFilter filter = CsmSelect.getFilterBuilder().createOffsetFilter(0, offset);
        findContext(CsmSelect.getDeclarations(file, filter), filter);
    }
    
    private Set<CsmFile> visitedFiles = new HashSet<CsmFile>();
    
    //private CsmNamespace currentNamespace;
    
    public Resolver3(CsmFile file, int offset, Resolver parent, CsmFile startFile) {
        this.file = file;
        this.offset = offset;
        this.origOffset = offset;
        parentResolver = parent;
        this.project = (ProjectBase) file.getProject();
        this.startFile = startFile;
    }
    
    public Resolver3(CsmFile file, int offset, Resolver parent) {
        this(file, offset, parent, (parent == null) ? file : parent.getStartFile());
    }
    
    public Resolver3(CsmOffsetable context, Resolver parent) {
        this(context.getContainingFile(), context.getStartOffset(), parent,
                (parent == null) ? context.getContainingFile() : parent.getStartFile());
    }
    
    private CsmClassifier findClassifier(CsmNamespace ns, CharSequence qualifiedNamePart) {
        CsmClassifier result = null;
        while ( ns != null  && result == null) {
            String fqn = ns.getQualifiedName() + "::" + qualifiedNamePart; // NOI18N
            result = findClassifier(fqn);
            ns = ns.getParent();
        }
        return result;
    }

    private CsmClassifier findClassifier(CharSequence qualifiedName) {
        // try to find visible classifier
        CsmClassifier result = CsmClassifierResolver.getDefault().findClassifierUsedInFile(qualifiedName, getStartFile(), needClasses());
        return result;
    }
    
    public CsmFile getStartFile() {
        return startFile;
    }

    private CsmNamespace findNamespace(CsmNamespace ns, CharSequence qualifiedNamePart) {
        CsmNamespace result = null;
        if (ns == null) {
            result = findNamespace(qualifiedNamePart);
        } else {
            CsmNamespace containingNs = ns;
            while (containingNs != null && result == null) {
                String fqn = (containingNs.isGlobal() ? "" : (containingNs.getQualifiedName() + "::")) + qualifiedNamePart; // NOI18N
                result = findNamespace(fqn);
                containingNs = containingNs.getParent();
            }
        }
        return result;
    }
    
    private CsmNamespace findNamespace(CharSequence qualifiedName) {
        CsmNamespace result = project.findNamespace(qualifiedName);
        if( result == null ) {
            for (Iterator iter = getLibraries().iterator(); iter.hasNext() && result == null;) {
                CsmProject lib = (CsmProject) iter.next();
                result = lib.findNamespace(qualifiedName);
            }
        }
        return result;
    }

    public Collection<CsmProject> getLibraries() {
        return getSearchLibraries(this.startFile.getProject());
    }

    public static Collection<CsmProject> getSearchLibraries(CsmProject prj) {
        if (prj.isArtificial() && prj instanceof ProjectBase) {
            List<ProjectBase> dependentProjects = ((ProjectBase)prj).getDependentProjects();
            Set<CsmProject> libs = new HashSet<CsmProject>();
            for (ProjectBase projectBase : dependentProjects) {
                if (!projectBase.isArtificial()) {
                    libs.addAll(projectBase.getLibraries());
                }
            }
            return libs;
        } else {
            return prj.getLibraries();
        }
    }

    public CsmClassifier getOriginalClassifier(CsmClassifier orig) {
        if (isRecursionOnResolving(INFINITE_RECURSION)) {
            return null;
        }
        AntiLoop set = new AntiLoop(100);
        while (true) {
            set.add(orig);
            CsmClassifier resovedClassifier = null;
            if (CsmKindUtilities.isClassForwardDeclaration(orig)){
                CsmClassForwardDeclaration fd = (CsmClassForwardDeclaration) orig;
                if (fd instanceof ClassForwardDeclarationImpl) {
                    resovedClassifier = ((ClassForwardDeclarationImpl)fd).getCsmClass(this);
                } else {
                    resovedClassifier = fd.getCsmClass();
                }
                if (resovedClassifier == null){
                    break;
                }
            } else if (CsmKindUtilities.isTypedef(orig)) {
                CsmType t = ((CsmTypedef)orig).getType();
                if (t instanceof Resolver.SafeClassifierProvider) {
                    resovedClassifier = ((Resolver.SafeClassifierProvider)t).getClassifier(this);
                } else {
                    resovedClassifier = t.getClassifier();
                }
                if (resovedClassifier == null) {
                    // have to stop with current 'orig' value
                    break;
                }
            } else if (ForwardClass.isForwardClass(orig)) {
                // try to find another class
                resovedClassifier = this.findClassifier(orig.getQualifiedName());
            } else {
                break;
            }
            if (set.contains(resovedClassifier)) {
                // try to recover from this error
                resovedClassifier = findOtherClassifier(orig);
                if (resovedClassifier == null || set.contains(resovedClassifier)) {
                    // have to stop with current 'orig' value
                    break;
                }
            }
            orig = resovedClassifier;            
        }
        return orig;
        
    }

    public static CsmClassifier findOtherClassifier(CsmClassifier out) {
        CsmNamespace ns = CsmBaseUtilities.getClassNamespace(out);
        CsmClassifier cls = null;
        if (ns != null) {
            CsmUID uid = UIDs.get(out);
            CharSequence fqn = out.getQualifiedName();
            Collection<CsmOffsetableDeclaration> col = null;
            if (ns instanceof NamespaceImpl) {
                col = ((NamespaceImpl)ns).getDeclarationsRange(fqn,
                        new Kind[]{Kind.CLASS, Kind.UNION, Kind.STRUCT, Kind.ENUM, Kind.TYPEDEF, Kind.TEMPLATE_DECLARATION, Kind.TEMPLATE_SPECIALIZATION, Kind.CLASS_FORWARD_DECLARATION});

            } else {
                col = ns.getDeclarations();
            }
            for (CsmDeclaration decl : col) {
                if (CsmKindUtilities.isClassifier(decl) && decl.getQualifiedName().equals(fqn)) {
                    if (!UIDs.get(decl).equals(uid)) {
                        cls = (CsmClassifier)decl;
                        if (!ForwardClass.isForwardClass(cls)) {
                            break;
                        }
                    }
                }
            }
        }        
        return cls;
    }         

    private void findContext(Iterator it, CsmFilter filter) {
        while(it.hasNext()) {
            CsmDeclaration decl = (CsmDeclaration) it.next();
            if( decl.getKind() == CsmDeclaration.Kind.NAMESPACE_DEFINITION ) {
                CsmNamespaceDefinition nd = (CsmNamespaceDefinition) decl;
                if( nd.getStartOffset() < this.offset && this.offset < nd.getEndOffset()  ) {
                    containingNamespace = nd.getNamespace();
                    findContext(CsmSelect.getDeclarations(nd, filter), filter);
                }
            } else if(   decl.getKind() == CsmDeclaration.Kind.CLASS
                    || decl.getKind() == CsmDeclaration.Kind.STRUCT
                    || decl.getKind() == CsmDeclaration.Kind.UNION ) {
                
                CsmClass cls = (CsmClass) decl;
                if( cls.getStartOffset() < this.offset && this.offset < cls.getEndOffset()  ) {
                    containingClass = cls;
                    findContext(CsmSelect.getClassMembers(containingClass, filter), filter);
                }
            } else if( decl.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION ||
                    decl.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION) {
                CsmFunctionDefinition fd = (CsmFunctionDefinition) decl;
                if( fd.getStartOffset() < this.offset && this.offset < fd.getEndOffset()  ) {
                    CsmNamespace ns = CsmBaseUtilities.getFunctionNamespace(fd);
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
        if (fd instanceof FunctionDefinitionImpl) {
            if (isRecursionOnResolving(INFINITE_RECURSION)) {
                return null;
            }
            return ((FunctionDefinitionImpl)fd).getDeclaration(this);
        }
        return fd.getDeclaration();
    }
    
    public boolean isRecursionOnResolving(int maxRecursion) {
        Resolver3 parent = (Resolver3)parentResolver;
        int count = 0;
        while(parent != null) {
            if (parent.origOffset == origOffset && parent.file.equals(file)) {
                if (TRACE_RECURSION) { traceRecursion(); }
                return true;
            }
            parent = (Resolver3) parent.parentResolver;
            count++;
            if (count > maxRecursion) {
                if (TRACE_RECURSION) { traceRecursion(); }
                return true;
            }
        }
        return false;
    }

    private CsmObject resolveInUsings(CsmNamespace containingNS, CharSequence nameToken) {
        if (isRecursionOnResolving(INFINITE_RECURSION)) {
            return null;
        }
        CsmObject result = null;
        for (CsmUsingDirective udir : CsmUsingResolver.getDefault().findUsingDirectives(containingNS)) {
            String fqn = udir.getName() + "::" + nameToken; // NOI18N
            result = findClassifier(fqn);
            if (result != null) {
                break;
            }
        }
        if (result == null) {
            CsmUsingResolver ur = CsmUsingResolver.getDefault();
             Collection<CsmDeclaration> decls = null;
            if (ur instanceof UsingResolverImpl) {
                 decls = ((UsingResolverImpl)ur).findUsedDeclarations(containingNS,this);
            } else {
                 decls = ur.findUsedDeclarations(containingNS);
            }
            for (CsmDeclaration decl : decls) {
                if (CharSequenceKey.Comparator.compare(nameToken, decl.getName()) == 0) {
                    if (CsmKindUtilities.isClassifier(decl) && needClassifiers()) {
                        result = decl;
                        break;
                    } else if (CsmKindUtilities.isClass(decl) && needClasses()) {
                        result = decl;
                        break;
                    }
                }
            }
        }
        return result;
    }
    
    private void traceRecursion(){
        System.out.println("Detected recursion in resolver:"); // NOI18N
        System.out.println("\t"+this); // NOI18Nv
        Resolver3 parent = (Resolver3)parentResolver;
        while(parent != null) {
            System.out.println("\t"+parent); // NOI18N
            parent = (Resolver3) parent.parentResolver;
        }
        new Exception().printStackTrace();
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(file.getAbsolutePath()+":"+origOffset); // NOI18N
        buf.append(":Looking for "); // NOI18N
        if (needClassifiers()) {
            if (needClasses()) {
                buf.append("c"); // NOI18N
            } else {
                buf.append("C"); // NOI18N
            }
        }
        if (needNamespaces()) {
            buf.append("N"); // NOI18N
        }    
        buf.append(":"+currName()); // NOI18N
        for(int i = 0; i < names.length; i++){
            if (i == 0) {
                buf.append("?"); // NOI18N
            } else {
                buf.append("::"); // NOI18N
            }
            buf.append(names[i]); // NOI18N
        }

        if (containingClass != null) {
            buf.append(":Class="+containingClass.getName()); // NOI18N
        }
        if (containingNamespace != null) {
            buf.append(":NS="+containingNamespace.getName()); // NOI18N
        }
        return buf.toString();
    }

    private void gatherMaps(CsmFile file) {
        if( file == null || visitedFiles.contains(file) ) {
            return;
        }
        visitedFiles.add(file);
        
        CsmFilter filter = CsmSelect.getFilterBuilder().createOffsetFilter(0, offset);
        Iterator<CsmInclude> iter = CsmSelect.getIncludes(file, filter);
        while (iter.hasNext()){
            CsmInclude inc = iter.next();
            CsmFile incFile = inc.getIncludeFile();
            if( incFile != null ) {
                int oldOffset = offset;
                offset = Integer.MAX_VALUE;
                gatherMaps(incFile);
                offset = oldOffset;
            }
        }
        gatherMaps(CsmSelect.getDeclarations(file, filter));
    }
    
    private void gatherMaps(Iterable<? extends CsmObject> declarations) {
        gatherMaps(declarations.iterator());
    }
    
    private void gatherMaps(Iterator<? extends CsmObject> it) {
        while(it.hasNext()) {
            CsmObject o = it.next();
            assert o instanceof CsmOffsetable;
            try {
                int start = ((CsmOffsetable) o).getStartOffset();
                int end = ((CsmOffsetable) o).getEndOffset();
                if( start >= this.offset ) {
                    break;
                }
                //assert o instanceof CsmScopeElement;
                if( o instanceof CsmScopeElement ) {
                    
                    // not yet in local context, but jumping into it
                    boolean oldValue = inLocalContext;
                    if (!inLocalContext && CsmKindUtilities.isFunctionDefinition(o)) {
                        inLocalContext = true;
                    }
                    gatherMaps((CsmScopeElement) o, end);
                    inLocalContext = oldValue;
                } else {
                    if( FileImpl.reportErrors ) {
                        System.err.println("Expected CsmScopeElement, got " + o);
                    }
                }
            } catch (NullPointerException ex) {
                if( FileImpl.reportErrors ) {
                    // FIXUP: do not crush on NPE
                    System.err.println("Unexpected NULL element in declarations collection");
                    DiagnosticExceptoins.register(ex);
                }
            }
        }
    }
    
    private CsmClassifier findNestedClassifier(CsmClassifier clazz) {
        if (CsmKindUtilities.isClass(clazz)) {
            Iterator<CsmMember> it = CsmSelect.getClassMembers((CsmClass)clazz,
                    CsmSelect.getFilterBuilder().createNameFilter(currName(), true, true, false));
            while(it.hasNext()) {
                CsmMember member = it.next();
                if( CharSequenceKey.Comparator.compare(currName(),member.getName())==0 ) {
                    if(CsmKindUtilities.isClassifier(member)) {
                        return (CsmClassifier) member;
                    }            
                }
            }
        }
        return null;
    }
    
    private void doProcessTypedefsInUpperNamespaces(CsmNamespaceDefinition nsd) {
        CsmFilter filter =  CsmSelect.getFilterBuilder().createKindFilter(
                                  CsmDeclaration.Kind.NAMESPACE_DEFINITION,
                                  CsmDeclaration.Kind.TYPEDEF);
        for (Iterator iter = CsmSelect.getDeclarations(nsd, filter); iter.hasNext();) {
            CsmDeclaration decl = (CsmDeclaration) iter.next();
            if( decl.getKind() == CsmDeclaration.Kind.NAMESPACE_DEFINITION ) {
                processTypedefsInUpperNamespaces((CsmNamespaceDefinition) decl);
            } else if( decl.getKind() == CsmDeclaration.Kind.TYPEDEF ) {
                CsmTypedef typedef = (CsmTypedef) decl;
                if( CharSequenceKey.Comparator.compare(currName(),typedef.getName())==0 ) {
                    currTypedef = typedef;
                }
            }
        }
    }
    
    private void processTypedefsInUpperNamespaces(CsmNamespaceDefinition nsd) {
        if( CharSequenceKey.Comparator.compare(nsd.getName(),currName())==0 )  {
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
     * It is quaranteed that element.getStartOffset < this.offset
     */
    private void gatherMaps(CsmScopeElement element, int end) {
        
        CsmDeclaration.Kind kind = (element instanceof CsmDeclaration) ? ((CsmDeclaration) element).getKind() : null;
        if( kind == CsmDeclaration.Kind.NAMESPACE_DEFINITION ) {
            CsmNamespaceDefinition nsd = (CsmNamespaceDefinition) element;
            if (nsd.getName().length() == 0) {
                // this is unnamed namespace and it should be considered as
                // it declares using itself
                usedNamespaces.add(nsd.getQualifiedName());
            }
            if (this.offset < end || isInContext(nsd)) {
                //currentNamespace = nsd.getNamespace();
                gatherMaps(nsd.getDeclarations());
            } else if (needClassifiers()){
                processTypedefsInUpperNamespaces(nsd);
            }
        } else if( kind == CsmDeclaration.Kind.NAMESPACE_ALIAS ) {
            CsmNamespaceAlias alias = (CsmNamespaceAlias) element;
            namespaceAliases.put(alias.getAlias(), alias.getReferencedNamespace());
        } else if( kind == CsmDeclaration.Kind.USING_DECLARATION ) {
            CsmDeclaration decl = resolveUsingDeclaration((CsmUsingDeclaration) element);
            if( decl != null ) {
                CharSequence id;
                if( decl.getKind() == CsmDeclaration.Kind.FUNCTION || decl.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION ||
                        decl.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND || decl.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND) {
                    // TODO: decide how to resolve functions
                    id = ((CsmFunction) decl).getSignature();
                } else {
                    id = decl.getName();
                }
                usingDeclarations.put(id, decl);
            }
        } else if( kind == CsmDeclaration.Kind.USING_DIRECTIVE ) {
            CsmUsingDirective udir = (CsmUsingDirective) element;
            usedNamespaces.add(udir.getName()); // getReferencedNamespace()
        } else if( element instanceof CsmDeclarationStatement ) {
            CsmDeclarationStatement ds = (CsmDeclarationStatement) element;
            if( ds.getStartOffset() < this.offset ) {
                gatherMaps( ((CsmDeclarationStatement) element).getDeclarators());
            }
        } else if (CsmKindUtilities.isScope(element)) {
            if (inLocalContext && needClassifiers() && CsmKindUtilities.isClassifier(element)) {
                if (CharSequenceKey.Comparator.compare(currName(), ((CsmClassifier)element).getName()) == 0) {
                    currLocalClassifier = (CsmClassifier)element;
                }
            }
            if (this.offset < end || isInContext((CsmScope) element)) {
                gatherMaps( ((CsmScope) element).getScopeElements());
            }
        } else if( kind == CsmDeclaration.Kind.TYPEDEF && needClassifiers()){
            CsmTypedef typedef = (CsmTypedef) element;
            if( CharSequenceKey.Comparator.compare(currName(),typedef.getName())==0 ) {
                currTypedef = typedef;
            }
        }
    }

    private boolean isInContext(CsmScope scope) {
        if (!CsmKindUtilities.isClass(scope) && !CsmKindUtilities.isNamespace(scope)) {
            return false;
        }
        CsmQualifiedNamedElement el = (CsmQualifiedNamedElement)scope;
        CsmNamespace ns = getContainingNamespace();
        if (ns != null && startsWith(ns.getQualifiedName(), el.getQualifiedName())) {
            return true;
        }
        CsmClass cls = getContainingClass();
        if (cls != null && startsWith(cls.getQualifiedName(), el.getQualifiedName())) {
            return true;
        }
        return false;
    }

    private boolean startsWith(CharSequence qname, CharSequence prefix) {
        if (qname.length() < prefix.length()) {
            return false;
        }
        for (int i = 0; i < prefix.length(); ++i) {
            if (qname.charAt(i) != prefix.charAt(i)) {
                return false;
            }
        }
        return qname.length() == prefix.length()
                || qname.charAt(prefix.length()) == ':'; // NOI18N
    }

    private CsmDeclaration resolveUsingDeclaration(CsmUsingDeclaration udecl){
        CsmDeclaration decl = null;
        if (udecl instanceof UsingDeclarationImpl) {
            if (isRecursionOnResolving(LIMITED_RECURSION)) {
                return null;
            }
            decl = ((UsingDeclarationImpl)udecl).getReferencedDeclaration(this);
        }
        return decl;
    }
    
    
    public CsmObject resolve(CharSequence qualified, int interestedKind) {
        return resolve(Utils.splitQualifiedName(qualified.toString()), interestedKind);
    }
    
    /**
     * Resolver class or namespace name.
     * Why class or namespace? Because in usage of kind org::vk::test
     * you don't know which is class and which is namespace name
     *
     * @param nameTokens tokenized name to resolve
     * (for example, for std::vector it is new CharSequence[] { "std", "vector" })
     *
     * @param context declaration within which the name found
     *
     * @return object of the following class:
     *  CsmClass
     *  CsmEnum
     *  CsmNamespace
     */
    public CsmObject resolve(CharSequence[] nameTokens, int interestedKind) {
        CsmObject result = null;
        
        names = nameTokens;
        currNamIdx = 0;
        this.interestedKind = interestedKind;
        CsmNamespace containingNS = null;
        
        if( nameTokens.length == 1 ) {
            if( result == null && needClassifiers()) {
                CsmClass cls = getContainingClass();
                result = resolveInClass(cls, nameTokens[0]);
                if( result == null ) {
                    if (parentResolver == null || !((Resolver3)parentResolver).resolveInBaseClass) {
                        result = resolveInBaseClasses(cls, nameTokens[0]);
                    }
                }
            }
            if (result == null && needClassifiers()) {
                containingNS = getContainingNamespace();
                result = findClassifier(containingNS, nameTokens[0]);
                if (result == null && containingNS != null) {
                    result = resolveInUsings(containingNS, nameTokens[0]);
                }
            }
            if( result == null  && needNamespaces()) {
                containingNS = getContainingNamespace();
                result = findNamespace(containingNS, nameTokens[0]);
            }
            if (result == null  && needClassifiers()){
                result = findClassifier(nameTokens[0]);
            }
            if( result == null ) {
                currTypedef = null;
                currLocalClassifier = null;
                gatherMaps(file);
                if( currLocalClassifier != null && needClassifiers()) {
                    result = currLocalClassifier;
                }
                if( currTypedef != null && needClassifiers()) {
                    result = currTypedef;
                }

                if( result == null ) {
                    CsmDeclaration decl = usingDeclarations.get(CharSequenceKey.create(nameTokens[0]));
                    if( decl != null ) {
                        result = decl;
                    }
                }
                
                if( result == null && needClassifiers()) {
                    for (Iterator<CharSequence> iter = usedNamespaces.iterator(); iter.hasNext();) {
                        String nsp = iter.next().toString();
                        String fqn = nsp + "::" + nameTokens[0]; // NOI18N
                        result = findClassifier(fqn);
                        if (result == null) {
                            result = findClassifier(containingNS, fqn);
                        }
                        if (result == null) {
                            CsmNamespace ns = findNamespace(nsp);
                            if (ns != null) {
                                result = resolveInUsings(ns, nameTokens[0]);
                            }
                        }
                        if( result != null ) {
                            break;
                        }
                    }
                }
                
                if( result == null && needNamespaces()) {
                    Object o = namespaceAliases.get(CharSequenceKey.create(nameTokens[0]));
                    if( o instanceof CsmNamespace ) {
                        result = (CsmNamespace) o;
                    }
                }

                if( result == null && needNamespaces()) {
                    for (Iterator<CharSequence> iter = usedNamespaces.iterator(); iter.hasNext();) {
                        String nsp = iter.next().toString();
                        String fqn = nsp + "::" + nameTokens[0]; // NOI18N
                        result = findNamespace(fqn);
                        if( result != null ) {
                            break;
                        }
                    }
                }
            }
        } else if( nameTokens.length > 1 ) {
            StringBuilder sb = new StringBuilder(nameTokens[0]);
            for (int i = 1; i < nameTokens.length; i++) {
                sb.append("::"); // NOI18N
                sb.append(nameTokens[i]);
            }
            if (needClassifiers()) {
                result = findClassifier(sb.toString());
            }
            if( result == null && needClassifiers()) {
                containingNS = getContainingNamespace();
                result = findClassifier(containingNS, sb.toString());                
            }
            if( result == null && needNamespaces()) {
                containingNS = getContainingNamespace();
                result = findNamespace(containingNS, sb.toString());
            }
            if( result == null && needClassifiers()) {
                gatherMaps(file);
                if( currTypedef != null) {
                    CsmType type = currTypedef.getType();
                    if( type != null ) {
                        CsmClassifier currentClassifier = getTypeClassifier(type);
                        while (currNamIdx < names.length -1 && currentClassifier != null) {
                            currNamIdx++;
                            currentClassifier = findNestedClassifier(currentClassifier);
                            if (CsmKindUtilities.isTypedef(currentClassifier)) {
                                CsmType curType = ((CsmTypedef)currentClassifier).getType();
                                currentClassifier = curType == null ? null : getTypeClassifier(curType);
                            }
                        }
                        if (currNamIdx == names.length - 1) {
                            result = currentClassifier;
                        }
                    }
                }
                
                if( result == null ) {
                    for (Iterator<CharSequence> iter = usedNamespaces.iterator(); iter.hasNext();) {
                        String nsp = iter.next().toString();
                        String fqn = nsp + "::" + sb; // NOI18N
                        result = findClassifier(fqn);
                        if( result != null ) {
                            break;
                        }
                    }
                }

                if( result == null ) {
                    CsmObject first = new Resolver3(this.file, this.origOffset, this).resolve(nameTokens[0], NAMESPACE);
                    if( first != null ) {
                        if( first instanceof CsmNamespace ) {
                            NamespaceImpl ns = (NamespaceImpl) first;
                            sb = new StringBuilder(ns.getQualifiedName());
                            for (int i = 1; i < nameTokens.length; i++) {
                                sb.append("::"); // NOI18N
                                sb.append(nameTokens[i]);
                            }
                            result = findClassifier(sb.toString());
                            if (result == null) {
                                sb = new StringBuilder(nameTokens[1]);
                                for (int i = 2; i < nameTokens.length; i++) {
                                    sb.append("::"); // NOI18N
                                    sb.append(nameTokens[i]);
                                }
                                result = resolveInUsings(ns, sb.toString());
                            }
                        } else if( first instanceof CsmClass ) {
                            
                        }
                    }
                }
            }

            if (result == null && needNamespaces()) {
                CsmObject obj = new Resolver3(this.file, this.origOffset, this).resolve(nameTokens[0], NAMESPACE);
                if (obj instanceof CsmNamespace) {
                    CsmNamespace ns = (CsmNamespace) obj;
                    for (int i = 1; i < nameTokens.length; i++) {
                        CsmNamespace newNs = null;
                        CharSequence name = nameTokens[i];
                        Collection<CsmNamespaceAlias> aliases = CsmUsingResolver.getDefault().findNamespaceAliases(ns);
                        for (CsmNamespaceAlias alias : aliases) {
                            if (alias.getAlias().toString().equals(name.toString())) {
                                newNs = alias.getReferencedNamespace();
                                break;
                            }
                        }
                        if (newNs == null) {
                            Collection<CsmNamespace> namespaces = ns.getNestedNamespaces();
                            for (CsmNamespace namespace : namespaces) {
                                if (namespace.getName().toString().equals(name.toString())) {
                                    newNs = namespace;
                                    break;
                                }
                            }
                        }
                        ns = newNs;
                        if (ns == null) {
                            break;
                        }
                    }
                    result = ns;
                }
            }
        }
        return result;
    }
    
    private CsmClassifier getTypeClassifier(CsmType type){
        if (type instanceof SafeClassifierProvider) {
            if (isRecursionOnResolving(INFINITE_RECURSION)) {
                return null;
            }
            return ((SafeClassifierProvider)type).getClassifier(this);
        }
        return type.getClassifier();
    }
    
    private CsmObject resolveInBaseClasses(CsmClass cls, CharSequence name) {
        resolveInBaseClass = true;
        CsmObject res = _resolveInBaseClasses(cls, name, new HashSet<CharSequence>(), 0);
        resolveInBaseClass = false;
        return res;
    }
    
    private CsmObject _resolveInBaseClasses(CsmClass cls, CharSequence name, Set<CharSequence> antiLoop, int depth) {
        if (depth == 50) {
            new Exception("Too many loops in resolver!!!").printStackTrace(System.err); // NOI18N
            return null;
        }
        if(isNotNullNotUnresolved(cls)) {
            List<CsmClass> toAnalyze = getClassesContainers(cls);
            for (CsmClass csmClass : toAnalyze) {
                for (CsmInheritance inh : csmClass.getBaseClasses()) {
                    CsmClass base = getInheritanceClass(inh);
                    if (base != null && !antiLoop.contains(base.getQualifiedName())) {
                        antiLoop.add(base.getQualifiedName());
                        CsmObject result = resolveInClass(base, name);
                        if (result != null) {
                            return result;
                        }
                        result = _resolveInBaseClasses(base, name, antiLoop, depth + 1);
                        if (result != null) {
                            return result;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private CsmClass getInheritanceClass(CsmInheritance inh){
        if (inh instanceof InheritanceImpl) {
            if (isRecursionOnResolving(INFINITE_RECURSION)) {
                return null;
            }
            CsmClassifier out = ((InheritanceImpl)inh).getClassifier(this);
            out = getOriginalClassifier(out);
            if (CsmKindUtilities.isClass(out)) {
                return (CsmClass) out;
            }
        }
        return getCsmClass(inh);
    }

    private CsmClass getCsmClass(CsmInheritance inh) {
        CsmClassifier classifier;
        if (inh instanceof Resolver.SafeClassifierProvider) {
            classifier = ((Resolver.SafeClassifierProvider)inh).getClassifier(this);
        } else {
            classifier = inh.getClassifier();
        }
        classifier = getOriginalClassifier(classifier);
        if (CsmKindUtilities.isClass(classifier)) {
            return (CsmClass)classifier;
        }
        return null;
    }

    private boolean isNotNullNotUnresolved(Object obj) {
        return obj != null && !Unresolved.isUnresolved(obj);
    }

    private CsmObject resolveInClass(CsmClass cls, CharSequence name) {
        if(isNotNullNotUnresolved(cls)){
            List<CsmClass> classesContainers = getClassesContainers(cls);
            for (CsmClass csmClass : classesContainers) {
                CsmClassifier classifier = null;
                CsmFilter filter = CsmSelect.getFilterBuilder().createNameFilter(name, true, true, false);
                Iterator<CsmMember> it = CsmSelect.getClassMembers(csmClass, filter);
                while (it.hasNext()) {
                    CsmMember member = it.next();
                    if (CsmKindUtilities.isClassifier(member)) {
                        classifier = (CsmClassifier) member;
                        if (!CsmKindUtilities.isClassForwardDeclaration(classifier)) {
                            return classifier;
                        }
                    }
                }
                if (classifier != null) {
                    return classifier;
                }
            }
        }
        return null;
    }

    private List<CsmClass> getClassesContainers(CsmClass cls) {
        List<CsmClass> out = new ArrayList<CsmClass>();
        CsmScope container = cls;
        while (CsmKindUtilities.isClass(container)) {
            out.add((CsmClass)container);
            container = ((CsmClass)container).getScope();
        }
        return out;
    }
    
    private boolean needClassifiers() {
        return ((interestedKind & CLASSIFIER) == CLASSIFIER) || needClasses();
    }
    
    private boolean needNamespaces() {
        return (interestedKind & NAMESPACE) == NAMESPACE;
    }    

    private boolean needClasses() {
        return (interestedKind & CLASS) == CLASS;
    }   
}
