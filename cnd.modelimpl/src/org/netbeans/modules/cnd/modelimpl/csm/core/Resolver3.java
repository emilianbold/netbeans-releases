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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import org.netbeans.modules.cnd.api.model.*;
import java.util.*;
import org.netbeans.modules.cnd.api.model.deep.CsmDeclarationStatement;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.InheritanceImpl;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceImpl;
import org.netbeans.modules.cnd.modelimpl.csm.TypeImpl;
import org.netbeans.modules.cnd.modelimpl.csm.UsingDeclarationImpl;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;

/**
 * @author Vladimir Kvasihn
 */
public class Resolver3 implements Resolver {
    private static final int INFINITE_RECURSION = 200;
    private static final int LIMITED_RECURSION = 5;
    
    private ProjectBase project;
    private CsmFile file;
    private int offset;
    private final int origOffset;
    private Resolver parentResolver;
    
    private List<CharSequence> usedNamespaces = new ArrayList<CharSequence>();
    private Map<CharSequence, CsmNamespace> namespaceAliases = new HashMap<CharSequence, CsmNamespace>();
    private Map<CharSequence, CsmDeclaration> usingDeclarations = new HashMap<CharSequence, CsmDeclaration>();
    
    private CsmTypedef currTypedef;
    
    private CharSequence[] names;
    private int currNamIdx;
    private int interestedKind;
    
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
        findContext(file.getDeclarations());
    }
    
    private Set<CsmFile> visitedFiles = new HashSet<CsmFile>();
    
    //private CsmNamespace currentNamespace;
    
    public Resolver3(CsmFile file, int offset, Resolver parent) {
        this.file = file;
        this.offset = offset;
        this.origOffset = offset;
        parentResolver = parent;
        this.project = (ProjectBase) file.getProject();
    }
    
    public Resolver3(CsmOffsetable context, Resolver parent) {
        this.file = context.getContainingFile();
        this.offset = context.getStartOffset();
        this.origOffset = offset;
        parentResolver = parent;
        this.project = (ProjectBase) context.getContainingFile().getProject();
    }
    
    private CsmClassifier findClassifier(CsmNamespace ns, CharSequence qulifiedNamePart) {
        CsmClassifier result = null;
        while ( ns != null  && result == null) {
            String fqn = ns.getQualifiedName() + "::" + qulifiedNamePart; // NOI18N
            result = findClassifier(fqn);
            ns = ns.getParent();
        }
        return result;
    }
    private CsmClassifier findClassifier(CharSequence qualifiedName) {
        CsmClassifier result = project.findClassifier(qualifiedName);
        if( result == null ) {
            for (Iterator iter = project.getLibraries().iterator(); iter.hasNext() && result == null;) {
                CsmProject lib = (CsmProject) iter.next();
                result = lib.findClassifier(qualifiedName);
            }
        }
        return result;
    }
    
    public CsmNamespace findNamespace(CharSequence qualifiedName) {
        CsmNamespace result = project.findNamespace(qualifiedName);
        if( result == null ) {
            for (Iterator iter = project.getLibraries().iterator(); iter.hasNext() && result == null;) {
                CsmProject lib = (CsmProject) iter.next();
                result = lib.findNamespace(qualifiedName);
            }
        }
        return result;
    }
    
    private void findContext(Iterable declarations) {
        for (Iterator it = declarations.iterator(); it.hasNext();) {
            CsmDeclaration decl = (CsmDeclaration) it.next();
            if( decl.getKind() == CsmDeclaration.Kind.NAMESPACE_DEFINITION ) {
                CsmNamespaceDefinition nd = (CsmNamespaceDefinition) decl;
                if( nd.getStartOffset() < this.offset && this.offset < nd.getEndOffset()  ) {
                    containingNamespace = nd.getNamespace();
                    findContext(nd.getDeclarations());
                }
            } else if(   decl.getKind() == CsmDeclaration.Kind.CLASS
                    || decl.getKind() == CsmDeclaration.Kind.STRUCT
                    || decl.getKind() == CsmDeclaration.Kind.UNION ) {
                
                CsmClass cls = (CsmClass) decl;
                if( cls.getStartOffset() < this.offset && this.offset < cls.getEndOffset()  ) {
                    containingClass = cls;
                }
            } else if( decl.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION ) {
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
    
    private boolean isRecursionOnResolving(int maxRecursion) {
        Resolver3 parent = (Resolver3)parentResolver;
        int count = 0;
        while(parent != null) {
            if (parent.origOffset == origOffset && parent.file.equals(file)) {
                return true;
            }
            parent = (Resolver3) parent.parentResolver;
            count++;
            if (count > maxRecursion) {
                return true;
            }
        }
        return false;
    }      
    
    protected void gatherMaps(CsmFile file) {
        if( file == null || visitedFiles.contains(file) ) {
            return;
        }
        visitedFiles.add(file);
        
        for (Iterator<CsmInclude> iter = file.getIncludes().iterator(); iter.hasNext();) {
            CsmInclude inc = iter.next();
            CsmFile incFile = inc.getIncludeFile();
            if( incFile != null ) {
                int oldOffset = offset;
                offset = Integer.MAX_VALUE;
                gatherMaps(incFile);
                offset = oldOffset;
            }
        }
        gatherMaps(file.getDeclarations());
    }
    
    protected void gatherMaps(Iterable declarations) {
        for( Iterator it = declarations.iterator(); it.hasNext(); ) {
            Object o = it.next();
            assert o instanceof CsmOffsetable;
            try {
                int start = ((CsmOffsetable) o).getStartOffset();
                int end = ((CsmOffsetable) o).getEndOffset();
                if( start >= this.offset ) {
                    break;
                }
                //assert o instanceof CsmScopeElement;
                if( o instanceof CsmScopeElement ) {
                    gatherMaps((CsmScopeElement) o, end);
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
    
    private void doProcessTypedefsInUpperNamespaces(CsmNamespaceDefinition nsd) {
        for (Iterator iter = nsd.getDeclarations().iterator(); iter.hasNext();) {
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
    protected void gatherMaps(CsmScopeElement element, int end) {
        
        CsmDeclaration.Kind kind = (element instanceof CsmDeclaration) ? ((CsmDeclaration) element).getKind() : null;
        if( kind == CsmDeclaration.Kind.NAMESPACE_DEFINITION ) {
            CsmNamespaceDefinition nsd = (CsmNamespaceDefinition) element;
            if (nsd.getName().length() == 0) {
                // this is unnamed namespace and it should be considered as
                // it declares using itself
                usedNamespaces.add(nsd.getQualifiedName());
            }
            if( this.offset < end ) {
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
                if( decl.getKind() == CsmDeclaration.Kind.FUNCTION || decl.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION ) {
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
        } else if( element instanceof CsmScope ) {
            if( this.offset < end ) {
                gatherMaps( ((CsmScope) element).getScopeElements());
            }
        } else if( kind == CsmDeclaration.Kind.TYPEDEF && needClassifiers()){
            CsmTypedef typedef = (CsmTypedef) element;
            if( CharSequenceKey.Comparator.compare(currName(),typedef.getName())==0 ) {
                currTypedef = typedef;
            }
        }
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
     * (for example, for std::vector it is new String[] { "std", "vector" })
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
            if (needClassifiers()){
                result = findClassifier(nameTokens[0]);
            }
            if( result == null  && needNamespaces()) {
                result = findNamespace(nameTokens[0]);
            }
            if( result == null && needClassifiers()) {
                containingNS = getContainingNamespace();
                result = findClassifier(containingNS, nameTokens[0]);
            }
            if( result == null && needClassifiers()) {
                CsmClass cls = getContainingClass();
                result = resolveInClass(cls, nameTokens[0]);
                if( result == null ) {
                    result = resolveInBaseClasses(cls, nameTokens[0]);
                }
            }
            if( result == null ) {
                currTypedef = null;
                gatherMaps(file);
                if( currTypedef != null && needClassifiers()) {
                    CsmType type = currTypedef.getType();
                    if( type != null ) {
                        result = getTypeClassifier(type);
                    }
                }
                
                if( result == null ) {
                    CsmDeclaration decl = (CsmDeclaration) usingDeclarations.get(CharSequenceKey.create(nameTokens[0]));
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
            if( result == null && needNamespaces()) {
//                containingNS = getContainingNamespace();
//                result = findClassifier(containingNS, sb.toString());
//            }
//            if( result == null ) {
                result = findNamespace(sb.toString());
            }
            if( result == null && needClassifiers()) {
                gatherMaps(file);
                if( currTypedef != null) {
                    CsmType type = currTypedef.getType();
                    if( type != null ) {
                        result = getTypeClassifier(type);
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
                        } else if( first instanceof CsmClass ) {
                            
                        }
                    }
                }
            }
        }
        if( result == null ) {
            result = project.getDummyForUnresolved(nameTokens, file, offset);
        }
        
//        CsmObject curr = null;
//        for( int i = 0; i < nameTokens.length; i++ ) {
//            String name = nameTokens[i];
//        }
        return result;
    }
    
    private CsmObject getTypeClassifier(CsmType type){
        if (type instanceof TypeImpl) {
            if (isRecursionOnResolving(INFINITE_RECURSION)) {
                return null;
            }
            return ((TypeImpl)type).getClassifier(this);
        }
        return type.getClassifier();
    }
    
    private CsmObject resolveInBaseClasses(CsmClass cls, CharSequence name) {
        return _resolveInBaseClasses(cls, name, new HashSet<CsmClass>());
    }
    
    private CsmObject _resolveInBaseClasses(CsmClass cls, CharSequence name, Set<CsmClass> antiLoop) {
        if( cls != null && cls.isValid()) {
            for( CsmInheritance inh : cls.getBaseClasses() ) {
                CsmClass base = getInheritanceClass(inh);
                if (base != null && !antiLoop.contains(base)) {
                    antiLoop.add(base);
                    CsmObject result = resolveInClass(base, name);
                    if( result != null ) {
                        return result;
                    }
                    result = _resolveInBaseClasses(base, name, antiLoop);
                    if( result != null ) {
                        return result;
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
            return ((InheritanceImpl)inh).getCsmClass(this);
        }
        return inh.getCsmClass();
    }
    
    private CsmObject resolveInClass(CsmClass cls, CharSequence name) {
        if( cls != null && cls.isValid()) {
            String fqn = cls.getQualifiedName() + "::" + name; // NOI18N
            return findClassifier(fqn);
        }
        return null;
    }

    private boolean needClassifiers() {
        return (interestedKind & CLASSIFIER) == CLASSIFIER;
    }
    
    private boolean needNamespaces() {
        return (interestedKind & NAMESPACE) == NAMESPACE;
    }    
}
