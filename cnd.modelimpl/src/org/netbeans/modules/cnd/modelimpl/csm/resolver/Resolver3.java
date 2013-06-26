/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.cnd.modelimpl.csm.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind;
import org.netbeans.modules.cnd.api.model.CsmEnumForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmUsingDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUsingDirective;
import org.netbeans.modules.cnd.api.model.deep.CsmDeclarationStatement;
import org.netbeans.modules.cnd.api.model.services.CsmClassifierResolver;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.services.CsmUsingResolver;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.modelimpl.csm.ForwardClass;
import org.netbeans.modules.cnd.modelimpl.csm.ForwardEnum;
import org.netbeans.modules.cnd.modelimpl.csm.InheritanceImpl;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceImpl;
import org.netbeans.modules.cnd.modelimpl.csm.TemplateUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Unresolved;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.impl.services.BaseUtilitiesProviderImpl;
import org.netbeans.modules.cnd.modelutil.AntiLoop;
import org.openide.util.CharSequences;

/**
 * @author Vladimir Kvasihn
 */
public final class Resolver3 implements Resolver {

    private final ProjectBase project;
    private final CsmFile file;
    private final CsmFile startFile;
    private final int origOffset;
    private Resolver parentResolver;

    private final Map<CharSequence, CsmObject/*CsmNamespace or CsmUsingDeclaration*/> usedNamespaces = new LinkedHashMap<CharSequence, CsmObject>();
    private final Map<CharSequence, CsmNamespace> namespaceAliases = new HashMap<CharSequence, CsmNamespace>();
    private final Map<CharSequence, CsmDeclaration> usingDeclarations = new HashMap<CharSequence, CsmDeclaration>();
    private final Map<CharSequence, CsmClassifier> currUsedClassifiers = new HashMap<CharSequence, CsmClassifier>();

    private CsmClassifier currLocalClassifier;
    private boolean currDone = false;

    private CharSequence[] names;
    private int currNamIdx;
    private int interestedKind;
    private boolean resolveInBaseClass;
    private final boolean SUPRESS_RECURSION_EXCEPTION = Boolean.getBoolean("cnd.modelimpl.resolver3.hide.exception"); // NOI18N

    private CharSequence currName() {
        return (names != null && currNamIdx < names.length) ? names[currNamIdx] : CharSequences.empty();
    }

    private final Context context;
    private Set<CsmFile> visitedFiles = new HashSet<CsmFile>();

    //private CsmNamespace currentNamespace;

    /**
     * should be created by ResolverFactory only
     * @param file file where object to be resolved is located
     * @param offset offset where object to be resolved is located 
     * @param parent parent resolver (can be null)
     * @param startFile start file where resolving started, it affects which objects considered as visible or not while resolving name at (file, offset)
     */
    /*package*/ Resolver3(CsmFile file, int offset, Resolver parent, CsmFile startFile) {
        this.file = file;
        this.origOffset = offset;
        parentResolver = parent;
        this.project = (ProjectBase) file.getProject();
        this.startFile = startFile;
        context = new Context(file, origOffset, this);
    }

    private Resolver3(CsmFile file, int offset, Resolver parent) {
        this(file, offset, parent, (parent == null) ? file : parent.getStartFile());
    }

    private CsmClassifier findClassifier(CsmNamespace ns, CharSequence qualifiedNamePart) {
        CsmClassifier result = null;
        CsmClassifier backupResult = null;
        while ( ns != null  && result == null) {
            String fqn = ns.getQualifiedName() + "::" + qualifiedNamePart; // NOI18N
            CsmClassifier aCls = findClassifierUsedInFile(fqn);
            if (aCls != null) {
                if (!ForwardClass.isForwardClass(aCls) || needForwardClassesOnly()) {
                    return aCls;
                }
                if (backupResult == null) {
                    backupResult = aCls;
                }
            }
            ns = ns.getParent();
        }
        if (result == null) {
            result = backupResult;
        }
        return result;
    }

    private CsmClassifier findClassifierUsedInFile(CharSequence qualifiedName) {
        // try to find visible classifier
        CsmClassifier result = null;
        final CharSequence id = CharSequences.create(qualifiedName);
        CsmClassifier globalResult = CsmClassifierResolver.getDefault().findClassifierUsedInFile(id, getStartFile(), needClasses());
        // first of all - check local context
        if (!currDone) {
            currLocalClassifier = null;
            gatherMaps(file, false, origOffset);
            currDone = true;
        }
        if (currLocalClassifier != null && needClassifiers()) {
            result = currLocalClassifier;
        }
        if (result == null) {
            if (currUsedClassifiers.containsKey(id)) {
                result = currUsedClassifiers.get(id);
            } else {
                result = globalResult;
                currUsedClassifiers.put(id, result);
            }
        }
        return result;
    }

    @Override
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
            for (Iterator<CsmProject> iter = getLibraries().iterator(); iter.hasNext() && result == null;) {
                CsmProject lib = iter.next();
                result = lib.findNamespace(qualifiedName);
            }
        }
        return result;
    }

    @Override
    public Collection<CsmProject> getLibraries() {
        return getSearchLibraries(this.startFile.getProject());
    }

    public static Collection<CsmProject> getSearchLibraries(CsmProject prj) {
        if (prj.isArtificial() && prj instanceof ProjectBase) {
            Set<CsmProject> libs = new HashSet<CsmProject>();
            for (ProjectBase projectBase : ((ProjectBase)prj).getDependentProjects()) {
                if (!projectBase.isArtificial()) {
                    libs.addAll(projectBase.getLibraries());
                }
            }
            return libs;
        } else {
            return prj.getLibraries();
        }
    }

    @Override
    public CsmClassifier getOriginalClassifier(CsmClassifier orig) {
        if (isRecursionOnResolving(INFINITE_RECURSION)) {
            return null;
        }
        AntiLoop set = new AntiLoop(100);
        while (true) {
            set.add(orig);
            CsmClassifier resovedClassifier;
            if (CsmKindUtilities.isClassForwardDeclaration(orig)){
                CsmClassForwardDeclaration fd = (CsmClassForwardDeclaration) orig;
                resovedClassifier = fd.getCsmClass();
                if (resovedClassifier == null){
                    break;
                }
            } else if (CsmKindUtilities.isEnumForwardDeclaration(orig)) {
                CsmEnumForwardDeclaration fd = (CsmEnumForwardDeclaration) orig;
                resovedClassifier = fd.getCsmEnum();
                if (resovedClassifier == null) {
                    break;
                } 
            } else if (CsmKindUtilities.isTypedef(orig)) {
                CsmType t = ((CsmTypedef)orig).getType();
                resovedClassifier = t.getClassifier();
                if (resovedClassifier == null) {
                    // have to stop with current 'orig' value
                    break;
                }
            } else if (ForwardClass.isForwardClass(orig) || ForwardEnum.isForwardEnum(orig)) {
                // try to find another classifier
                resovedClassifier = findClassifierUsedInFile(orig.getQualifiedName());
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

    private CsmClassifier findOtherClassifier(CsmClassifier out) {
        CsmNamespace ns = BaseUtilitiesProviderImpl.getImpl()._getClassNamespace(out);
        CsmClassifier cls = null;
        if (ns != null) {
            CsmUID<?> uid = UIDs.get(out);
            CharSequence fqn = out.getQualifiedName();
            Collection<CsmOffsetableDeclaration> col;
            if (ns instanceof NamespaceImpl) {
                col = ((NamespaceImpl)ns).getDeclarationsRange(fqn,
                        new Kind[]{Kind.CLASS, Kind.UNION, Kind.STRUCT, Kind.ENUM, Kind.TYPEDEF,
                            Kind.TEMPLATE_DECLARATION, Kind.TEMPLATE_SPECIALIZATION,
                            Kind.CLASS_FORWARD_DECLARATION, Kind.ENUM_FORWARD_DECLARATION});

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

    @Override
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
            if(fqn.startsWith("::")) { // NOI18N
                fqn = fqn.substring(2);
            }
            result = findClassifierUsedInFile(fqn);
            if (result != null) {
                break;
            }
        }
        if (result == null) {
            CsmUsingResolver ur = CsmUsingResolver.getDefault();
            Collection<CsmDeclaration> decls;
            decls = ur.findUsedDeclarations(containingNS);
            for (CsmDeclaration decl : decls) {
                if (CharSequences.comparator().compare(nameToken, decl.getName()) == 0) {
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

    void traceRecursion(){
        System.out.println("Detected recursion in resolver:"); // NOI18N
        System.out.println("\t"+this); // NOI18Nv
        Resolver3 parent = (Resolver3)parentResolver;
        while(parent != null) {
            System.out.println("\t"+parent); // NOI18N
            parent = (Resolver3) parent.parentResolver;
        }
        new Exception().printStackTrace(System.err);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(file.getAbsolutePath()).append(":").append(origOffset); // NOI18N
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
        buf.append(":").append(currName()); // NOI18N
        for(int i = 0; i < names.length; i++){
            if (i == 0) {
                buf.append("?"); // NOI18N
            } else {
                buf.append("::"); // NOI18N
            }
            buf.append(names[i]); // NOI18N
        }

        if (context.getContainingClass() != null) {
            buf.append(":Class=").append(context.getContainingClass().getName()); // NOI18N
        }
        if (context.getContainingNamespace() != null) {
            buf.append(":NS=").append(context.getContainingNamespace().getName()); // NOI18N
        }
        return buf.toString();
    }

    private static final CsmFilter NO_FILTER = CsmSelect.getFilterBuilder().createOffsetFilter(0, Integer.MAX_VALUE);
    private static final CsmFilter NAMESPACE_FILTER = CsmSelect.getFilterBuilder().createKindFilter(
                         CsmDeclaration.Kind.NAMESPACE_DEFINITION
                       , CsmDeclaration.Kind.NAMESPACE_ALIAS
                       , CsmDeclaration.Kind.USING_DECLARATION
                       , CsmDeclaration.Kind.USING_DIRECTIVE
                       );
    private static final CsmFilter CLASS_FILTER = CsmSelect.getFilterBuilder().createKindFilter(
                         CsmDeclaration.Kind.NAMESPACE_DEFINITION
                       , CsmDeclaration.Kind.NAMESPACE_ALIAS
                       , CsmDeclaration.Kind.USING_DECLARATION
                       , CsmDeclaration.Kind.USING_DIRECTIVE
                       , CsmDeclaration.Kind.TYPEDEF
                       , CsmDeclaration.Kind.CLASS
                       , CsmDeclaration.Kind.ENUM
                       , CsmDeclaration.Kind.STRUCT
                       , CsmDeclaration.Kind.UNION
                       );

    private void gatherMaps(CsmFile file, boolean visitIncludedFiles, int offset) {
        if( file == null || visitedFiles.contains(file) ) {
            return;
        }
        visitedFiles.add(file);
        CsmFilter filter;
        if (offset == Integer.MAX_VALUE) {
            filter = NO_FILTER;
        } else {
            filter = CsmSelect.getFilterBuilder().createOffsetFilter(0, offset);
        }
        if (visitIncludedFiles) {
            Iterator<CsmInclude> iter = CsmSelect.getIncludes(file, filter);
            while (iter.hasNext()){
                CsmInclude inc = iter.next();
                CsmFile incFile = inc.getIncludeFile();
                if( incFile != null ) {
                    gatherMaps(incFile, true, Integer.MAX_VALUE);
                }
            }
        }
        if (offset == Integer.MAX_VALUE) {
            if (needClassifiers()) {
                filter = CLASS_FILTER;
            } else {
                filter = NAMESPACE_FILTER;
            }
        }
        Iterator<CsmOffsetableDeclaration> declarations = CsmSelect.getDeclarations(file, filter);
        gatherMaps(declarations, false, offset);
        if (!visitIncludedFiles) {
            visitedFiles.remove(file);
        }
    }

    private void gatherMaps(Iterable<? extends CsmObject> declarations, boolean inLocalContext, int offset) {
        gatherMaps(declarations.iterator(), inLocalContext, offset);
    }

    private void gatherMaps(Iterator<? extends CsmObject> it, boolean inLocalContext, int offset) {
        while(it.hasNext()) {
            CsmObject o = it.next();
            assert o == null || o instanceof CsmOffsetable : "non CsmOffsetable" + o;
            try {
                int start = ((CsmOffsetable) o).getStartOffset();
                int end = ((CsmOffsetable) o).getEndOffset();
                if( start >= offset ) {
                    break;
                }
                //assert o instanceof CsmScopeElement;
                if( o instanceof CsmScopeElement ) {
                    if (!inLocalContext && CsmKindUtilities.isFunctionDefinition(o)) {
                        if (end >= offset) {
                            gatherMaps((CsmScopeElement) o, end, true, offset);
                        }
                    } else {
                        gatherMaps((CsmScopeElement) o, end, inLocalContext, offset);
                    }
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
                if( CharSequences.comparator().compare(currName(),member.getName())==0 ) {
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
        for (Iterator<CsmOffsetableDeclaration> iter = CsmSelect.getDeclarations(nsd, filter); iter.hasNext();) {
            CsmOffsetableDeclaration decl = iter.next();
            if( decl.getKind() == CsmDeclaration.Kind.NAMESPACE_DEFINITION ) {
                processTypedefsInUpperNamespaces((CsmNamespaceDefinition) decl);
            } else if( decl.getKind() == CsmDeclaration.Kind.TYPEDEF ) {
                CsmTypedef typedef = (CsmTypedef) decl;
                if( CharSequences.comparator().compare(currName(),typedef.getName())==0 ) {
                    currLocalClassifier = typedef;
                }
            }
        }
    }

    private void processTypedefsInUpperNamespaces(CsmNamespaceDefinition nsd) {
        if( CharSequences.comparator().compare(nsd.getName(),currName())==0 )  {
            currNamIdx++;
            doProcessTypedefsInUpperNamespaces(nsd);
        } else {
            CsmNamespace cns = context.getContainingNamespace();
            if( cns != null ) {
                if( cns.equals(nsd.getNamespace())) {
                    doProcessTypedefsInUpperNamespaces(nsd);
                }
            }
        }
    }

    /**
     * It is guaranteed that element.getStartOffset < this.offset
     */
    private void gatherMaps(CsmScopeElement element, int end, boolean inLocalContext, int offset) {

        CsmDeclaration.Kind kind = (element instanceof CsmDeclaration) ? ((CsmDeclaration) element).getKind() : null;
        if (kind != null) {
            switch (kind) {
                case NAMESPACE_DEFINITION: {
                    CsmNamespaceDefinition nsd = (CsmNamespaceDefinition) element;
                    if (nsd.getName().length() == 0) {
                        // this is unnamed namespace and it should be considered as
                        // it declares using itself
                        usedNamespaces.put(nsd.getQualifiedName(), nsd.getNamespace());
                    }
                    if (offset < end || isInContext(nsd)) {
                        //currentNamespace = nsd.getNamespace();
                        gatherMaps(nsd.getDeclarations(), inLocalContext, offset);
                    } else if (needClassifiers()){
                        processTypedefsInUpperNamespaces(nsd);
                    }
                    return;
                }
                case NAMESPACE_ALIAS: {
                    CsmNamespaceAlias alias = (CsmNamespaceAlias) element;
                    namespaceAliases.put(alias.getAlias(), alias.getReferencedNamespace());
                    return;
                }
                case USING_DECLARATION: {
                    CsmDeclaration decl = resolveUsingDeclaration((CsmUsingDeclaration) element);
                    if( decl != null ) {
                        CharSequence id;
                        if( decl.getKind() == CsmDeclaration.Kind.FUNCTION ||
                                decl.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION ||
                                decl.getKind() == CsmDeclaration.Kind.FUNCTION_LAMBDA ||
                                decl.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND ||
                                decl.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION) {
                            // TODO: decide how to resolve functions
                            id = ((CsmFunction) decl).getSignature();
                        } else {
                            id = decl.getName();
                        }
                        usingDeclarations.put(id, decl);
                    }
                    return;
                }
                case USING_DIRECTIVE:{
                    CsmUsingDirective udir = (CsmUsingDirective) element;
                    CharSequence name = udir.getName();
                    if (!usedNamespaces.containsKey(name)) {
                        usedNamespaces.put(name, udir); // getReferencedNamespace()
                    }
                    return;
                }
                case TYPEDEF: {
                    CsmTypedef typedef = (CsmTypedef) element;
                    // don't want typedef to find itself
                    if( offset > end && CharSequences.comparator().compare(currName(),typedef.getName())==0 ) {
                        currLocalClassifier = typedef;
                    }
                    return;
                }
            }
        }
        if( element instanceof CsmDeclarationStatement ) {
            CsmDeclarationStatement ds = (CsmDeclarationStatement) element;
            if( ds.getStartOffset() < offset ) {
                gatherMaps( ((CsmDeclarationStatement) element).getDeclarators(), inLocalContext, offset);
            }
        } else if (CsmKindUtilities.isScope(element)) {
            if (inLocalContext && needClassifiers() && CsmKindUtilities.isClassifier(element)) {
                // don't want forward to find itself
                if (!CsmKindUtilities.isClassForwardDeclaration(element) || (offset > end)) {
                    if (CharSequences.comparator().compare(currName(), ((CsmClassifier)element).getName()) == 0) {
                        currLocalClassifier = (CsmClassifier)element;
                    }
                }
            }
            if (offset < end || isInContext((CsmScope) element)) {
                gatherMaps( ((CsmScope) element).getScopeElements(), inLocalContext, offset);
            }
        }
    }

    private boolean isInContext(CsmScope scope) {
        if (!CsmKindUtilities.isClass(scope) && !CsmKindUtilities.isNamespace(scope)) {
            return false;
        }
        CsmQualifiedNamedElement el = (CsmQualifiedNamedElement)scope;
        CsmNamespace ns = context.getContainingNamespace();
        if (ns != null && startsWith(ns.getQualifiedName(), el.getQualifiedName())) {
            return true;
        }
        CsmClass cls = context.getContainingClass();
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
        if (isRecursionOnResolving(LIMITED_RECURSION)) {
            return null;
        }
        return  udecl.getReferencedDeclaration();
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
    @Override
    public CsmObject resolve(CharSequence[] nameTokens, int interestedKind) {
        CsmObject result = null;

        names = nameTokens;
        currNamIdx = 0;
        this.interestedKind = interestedKind;
        if( nameTokens.length == 1 ) {
            result = resolveSimpleName(result, nameTokens[0], interestedKind);
        } else if( nameTokens.length > 1 ) {
            result = resolveCompoundName(nameTokens, result, interestedKind);
        }
        return result;
    }

    private CsmObject resolveSimpleName(CsmObject result, CharSequence name, int interestedKind) {
        CsmNamespace containingNS = null;
        if (result == null && needClassifiers()) {
            CsmClass cls = context.getContainingClass();
            result = resolveInClass(cls, name);
            if (result == null) {
                if (parentResolver == null || !((Resolver3) parentResolver).resolveInBaseClass) {
                    result = resolveInBaseClasses(cls, name);
                    if(needTemplateClassesOnly() && !CsmKindUtilities.isTemplate(result)) {
                        result = null;
                    }
                }
            }
        }
        if (result == null && needClassifiers()) {
            containingNS = context.getContainingNamespace();
            result = findClassifier(containingNS, name);
            if (result == null && containingNS != null) {
                result = resolveInUsings(containingNS, name);
            }
        }
        if (result == null && needNamespaces()) {
            containingNS = context.getContainingNamespace();
            result = findNamespace(containingNS, name);
        }
        if (needClassifiers() && 
                (result == null ||
                 (!needForwardClassesOnly() && ForwardClass.isForwardClass(result)))) {
            CsmObject oldResult = result;
            result = findClassifierUsedInFile(name);
            if(needTemplateClassesOnly() && !CsmKindUtilities.isTemplate(result)) {
                result = null;
            }
            if (result == null) {
                result = oldResult;
            }
        }
        CsmObject backupResult = result;
        if (!needForwardClassesOnly() && ForwardClass.isForwardClass(result)) {
            // try to find not forward class
            result = null;
        }
        if (result == null) {
            gatherMaps(file, !FileImpl.isFileBeingParsedInCurrentThread(file), origOffset);
            if (currLocalClassifier != null && needClassifiers()) {
                result = currLocalClassifier;
            }
            if (result == null) {
                CsmDeclaration decl = usingDeclarations.get(CharSequences.create(name));
                if (decl != null) {
                    result = decl;
                }
            }
            if (result == null && needClassifiers()) {
                for (Map.Entry<CharSequence, CsmObject> entry : usedNamespaces.entrySet()) {
                    String nsp = entry.getKey().toString();
                    String fqn = nsp + "::" + name; // NOI18N
                    result = findClassifierUsedInFile(fqn);
                    if (result == null) {
                        result = findClassifier(containingNS, fqn);
                    }
                    if (result == null) {
                        CsmObject val = entry.getValue();
                        if (CsmKindUtilities.isUsingDirective(val)) {
                            // replace using namespace by referenced namespace
                            val = ((CsmUsingDirective)val).getReferencedNamespace();
                            entry.setValue(val);
                        }
                        if (val == null) {
                            val = findNamespace(nsp);
                            entry.setValue(val);
                        }
                        if (CsmKindUtilities.isNamespace(val)) {
                            CsmNamespace ns = (CsmNamespace)val;
                            if (!nsp.contains(ns.getQualifiedName())) {
                                fqn = ns.getQualifiedName().toString() + "::" + name; // NOI18N
                                result = findClassifierUsedInFile(fqn);
                            }
                            if (result == null) {
                                result = resolveInUsings(ns, name);
                            }
                        }
                    }
                    if (result != null) {
                        break;
                    }
                }
            }
            if (result == null && needNamespaces()) {
                Object o = namespaceAliases.get(CharSequences.create(name));
                if (o instanceof CsmNamespace) {
                    result = (CsmNamespace) o;
                }
            }
            if (result == null && needNamespaces()) {
                for (Map.Entry<CharSequence, CsmObject> entry : usedNamespaces.entrySet()) {
                    String nsp = entry.getKey().toString();
                    String fqn = nsp + "::" + name; // NOI18N
                    result = findNamespace(fqn);
                    if (result != null) {
                        break;
                    } else {
                        CsmObject val = entry.getValue();
                        if (CsmKindUtilities.isUsingDirective(val)) {
                            // replace using namespace by referenced namespace
                            val = ((CsmUsingDirective) val).getReferencedNamespace();
                            entry.setValue(val);
                            if (val != null) {
                                Collection<CsmNamespaceAlias> aliases = CsmUsingResolver.getDefault().findNamespaceAliases((CsmNamespace)val);
                                for (CsmNamespaceAlias alias : aliases) {
                                    if (alias.getAlias().toString().equals(name.toString())) {
                                        result = alias.getReferencedNamespace();
                                        break;
                                    }
                                }
                            }
                            if (result != null) {
                                break;
                            }
                        }
                    }
                }
            }
        }   
        if (result == null) {
             result = backupResult;
        }
        if (result == null) {
            if (TemplateUtils.isTemplateQualifiedName(name.toString())) {
                Resolver aResolver = ResolverFactory.createResolver(file, origOffset);
                try {
                    result = aResolver.resolve(Utils.splitQualifiedName(TemplateUtils.getTemplateQualifiedNameWithoutSiffix(name.toString())), TEMPLATE_CLASS);
                } finally {
                    ResolverFactory.releaseResolver(aResolver);
                }
            }
        }
        if(needTemplateClassesOnly() && !CsmKindUtilities.isTemplate(result)) {
            result = null;
        }
        if (result == null && needClassifiers() && !needForwardClassesOnly()) {
            result = resolve(Utils.splitQualifiedName(name.toString()), CLASS_FORWARD);
        }
        if(needForwardClassesOnly() && !CsmKindUtilities.isClassForwardDeclaration(result)) {
            result = null;
        }
        return result;
    }

    private String fullName(CharSequence[] nameTokens) {
        StringBuilder sb = new StringBuilder(nameTokens[0]);
        for (int i = 1; i < nameTokens.length; i++) {
            sb.append("::"); // NOI18N
            sb.append(nameTokens[i]);
        }
        return sb.toString();
    }

    private CsmObject resolveCompoundName(CharSequence[] nameTokens, CsmObject result, int interestedKind) {
        CsmNamespace containingNS;
        String fullName = fullName(nameTokens);
        if (needClassifiers()) {
            result = findClassifierUsedInFile(fullName);
        }
        if (result == null && needClassifiers()) {
            containingNS = context.getContainingNamespace();
            result = findClassifier(containingNS, fullName);
        }
        if (result == null && needNamespaces()) {
            containingNS = context.getContainingNamespace();
            result = findNamespace(containingNS, fullName);
        }
        if (result == null && needClassifiers()) {
            gatherMaps(file, !FileImpl.isFileBeingParsedInCurrentThread(file), origOffset);
            if (currLocalClassifier != null && CsmKindUtilities.isTypedef(currLocalClassifier)) {
                CsmType type = ((CsmTypedef)currLocalClassifier).getType();
                if (type != null) {
                    CsmClassifier currentClassifier = getTypeClassifier(type);
                    while (currNamIdx < names.length - 1 && currentClassifier != null) {
                        currNamIdx++;
                        currentClassifier = findNestedClassifier(currentClassifier);
                        if (CsmKindUtilities.isTypedef(currentClassifier)) {
                            CsmType curType = ((CsmTypedef) currentClassifier).getType();
                            currentClassifier = curType == null ? null : getTypeClassifier(curType);
                        }
                    }
                    if (currNamIdx == names.length - 1) {
                        result = currentClassifier;
                    }
                }
            }
            if (result == null) {
                for (Iterator<CharSequence> iter = usedNamespaces.keySet().iterator(); iter.hasNext();) {
                    String nsp = iter.next().toString();
                    String fqn = nsp + "::" + fullName; // NOI18N
                    result = findClassifierUsedInFile(fqn);
                    if (result != null) {
                        break;
                    }
                }
            }
            if (result == null) {
                CsmNamespace ns = null;
                String nsName = nameTokens[0].toString(); // NOI18N
                int i;
                for (i = 1; i < nameTokens.length; i++) {
                    CsmObject nsObj = null;
                    Resolver aResolver = ResolverFactory.createResolver(file, origOffset);
                    try {
                        nsObj = aResolver.resolve(Utils.splitQualifiedName(nsName), NAMESPACE);
                    } finally {
                        ResolverFactory.releaseResolver(aResolver);
                    }                    
                    if (nsObj instanceof CsmNamespace) {
                        ns = (CsmNamespace)nsObj;                                            
                        CharSequence token = nameTokens[i];
                        nsName = ns.getQualifiedName() + "::" + token; // NOI18N
                    } else {
                        break;
                    }
                }
                i--;
                if (ns != null) {
                    StringBuilder sb = new StringBuilder(ns.getQualifiedName());
                    for (int j = i; j < nameTokens.length; j++) {
                        sb.append("::"); // NOI18N
                        sb.append(nameTokens[j]);
                    }
                    result = findClassifierUsedInFile(sb.toString());
                    if (result == null) {
                        sb = new StringBuilder(nameTokens[i]);
                        for (int j = i + 1; j < nameTokens.length; j++) {
                            sb.append("::"); // NOI18N
                            sb.append(nameTokens[j]);
                        }
                        result = resolveInUsings(ns, sb.toString());
                    }
                }
            }
        }
        if (result == null && needNamespaces()) {
            CsmObject obj = null;
            Resolver aResolver = ResolverFactory.createResolver(file, origOffset);
            try {
                obj = aResolver.resolve(Utils.splitQualifiedName(nameTokens[0].toString()), NAMESPACE);
            } finally {
                ResolverFactory.releaseResolver(aResolver);
            }
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
        if (result == null) {
            if (TemplateUtils.isTemplateQualifiedName(fullName.toString())) {
                StringBuilder sb2 = new StringBuilder(TemplateUtils.getTemplateQualifiedNameWithoutSiffix(nameTokens[0].toString()));
                for (int i = 1; i < nameTokens.length; i++) {
                    sb2.append("::"); // NOI18N
                    sb2.append(TemplateUtils.getTemplateQualifiedNameWithoutSiffix(nameTokens[i].toString()));
                }
                Resolver aResolver = ResolverFactory.createResolver(file, origOffset);
                try {
                    result = aResolver.resolve(Utils.splitQualifiedName(sb2.toString()), interestedKind);
                } finally {
                    ResolverFactory.releaseResolver(aResolver);
                }
            }
        }
        if (result == null && needClassifiers() && !needForwardClassesOnly()) {
            result = resolve(nameTokens, CLASS_FORWARD);
        }
        if(needForwardClassesOnly() && !CsmKindUtilities.isClassForwardDeclaration(result)) {
            result = null;
        }        
        return result;
    }

    private CsmClassifier getTypeClassifier(CsmType type){
        if (isRecursionOnResolving(INFINITE_RECURSION)) {
            return null;
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
            String msg = "Recursion in resolver3:resolveInBaseClasses[" + name + "]" + this.file.getAbsolutePath() + ":" + this.origOffset; // NOI18N
            if (SUPRESS_RECURSION_EXCEPTION) {
                Utils.LOG.warning(msg);
            } else {
                new Exception(msg).printStackTrace(System.err);
            }
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
            CsmClassifier out = inh.getClassifier();
            out = getOriginalClassifier(out);
            if (CsmKindUtilities.isClass(out)) {
                return (CsmClass) out;
            }
        }
        return getCsmClass(inh);
    }

    private CsmClass getCsmClass(CsmInheritance inh) {
        CsmClassifier classifier = inh.getClassifier();
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
        return ((interestedKind & CLASSIFIER) == CLASSIFIER) || needClasses() || needTemplateClasses() || needForwardClasses();
    }

    private boolean needNamespaces() {
        return (interestedKind & NAMESPACE) == NAMESPACE;
    }

    private boolean needClasses() {
        return (interestedKind & CLASS) == CLASS || needTemplateClasses() || needForwardClasses();
    }

    private boolean needTemplateClasses() {
        return (interestedKind & TEMPLATE_CLASS) == TEMPLATE_CLASS;
    }
    
    private boolean needTemplateClassesOnly() {
        return interestedKind == TEMPLATE_CLASS;
    }

    private boolean needForwardClasses() {
        return (interestedKind & CLASS_FORWARD) == CLASS_FORWARD;
    }
    
    private boolean needForwardClassesOnly() {
        return interestedKind == CLASS_FORWARD;
    }
}
