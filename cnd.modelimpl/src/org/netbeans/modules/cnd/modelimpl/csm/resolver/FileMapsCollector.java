/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.csm.resolver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmUsingDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUsingDirective;
import org.netbeans.modules.cnd.api.model.deep.CsmDeclarationStatement;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.openide.util.CharSequences;

/**
 * extracted part of Resolver3 which is responsible for gathering file maps.
 * 
 * @author Vladimir Voskresensky
 */
public final class FileMapsCollector {
    private final CsmFile currentFile;
    private final CsmFile startFile;

    private final Map<CharSequence, CsmObject/*CsmNamespace or CsmUsingDeclaration*/> usedNamespaces = new LinkedHashMap<>();
    private final Map<CharSequence, CsmNamespace> namespaceAliases = new HashMap<>();
    private final Map<CharSequence, CsmDeclaration> usingDeclarations = new HashMap<>();

    private final Set<CsmFile> visitedFiles = new HashSet<>();
    
    private static final CsmSelect.CsmFilter NO_FILTER = CsmSelect.getFilterBuilder().createOffsetFilter(0, Integer.MAX_VALUE);
    private static final CsmSelect.CsmFilter NAMESPACE_FILTER = CsmSelect.getFilterBuilder().createKindFilter(
            CsmDeclaration.Kind.NAMESPACE_DEFINITION, CsmDeclaration.Kind.NAMESPACE_ALIAS, CsmDeclaration.Kind.USING_DECLARATION, CsmDeclaration.Kind.USING_DIRECTIVE
    );
    private static final CsmSelect.CsmFilter CLASS_FILTER = CsmSelect.getFilterBuilder().createKindFilter(
            CsmDeclaration.Kind.NAMESPACE_DEFINITION, CsmDeclaration.Kind.NAMESPACE_ALIAS, CsmDeclaration.Kind.USING_DECLARATION, CsmDeclaration.Kind.USING_DIRECTIVE, CsmDeclaration.Kind.TYPEDEF, CsmDeclaration.Kind.TYPEALIAS, CsmDeclaration.Kind.CLASS, CsmDeclaration.Kind.ENUM, CsmDeclaration.Kind.STRUCT, CsmDeclaration.Kind.UNION
    );

    public FileMapsCollector(CsmFile file, CsmFile startFile) {
        this.currentFile = file;
        this.startFile = startFile;
    }

    CsmDeclaration getUsingDeclaration(CharSequence name) {
        return usingDeclarations.get(CharSequences.create(name));
    }

    Object getNamespaceAlias(CharSequence name) {
        return namespaceAliases.get(CharSequences.create(name));
    }

    Map<CharSequence, CsmObject/*CsmNamespace or CsmUsingDeclaration*/> getUsedNamespaces() {
        return new HashMap<>(usedNamespaces);
    }

    void rememberResolvedUsing(CharSequence key, CsmNamespace value) {
        usedNamespaces.put(key, value);
    }
    
    void initFileMaps(boolean needClassifiers, int stopAtOffset, Callback callback) {
        // when in parsing mode, we do not gather dependencies for
        // probably not yet parsed files
        if (!FileImpl.isFileBeingParsedInCurrentThread(currentFile)) {
            MapsCollection out = new MapsCollection(EMPTY_CALLBACK, needClassifiers, visitedFiles, usedNamespaces, namespaceAliases, usingDeclarations);
            long time = System.currentTimeMillis();
            initMapsFromIncludeStack(out, currentFile);
            Resolver3.LOGGER.log(Level.FINE, "{0}ms initMapsFromIncludeStack for {1}\n\twith start file {2}\n", new Object[]{System.currentTimeMillis() - time, currentFile.getAbsolutePath(), this.startFile.getAbsolutePath()});
            time = System.currentTimeMillis();
            initMapsFromIncludes(out, currentFile, stopAtOffset);
            Resolver3.LOGGER.log(Level.FINE, "{0}ms initMapsFromIncludes for {1}\n\twith start file {2}\n", new Object[]{System.currentTimeMillis() - time, currentFile.getAbsolutePath(), this.startFile.getAbsolutePath()});
        }
        initMapsFromCurrentFileOnly(needClassifiers, stopAtOffset, callback);
    }

    void initMapsFromCurrentFileOnly(boolean needClassifiers, int stopAtOffset, Callback callback) {
        assert stopAtOffset != Integer.MAX_VALUE;
        CsmSelect.CsmFilter filter = CsmSelect.getFilterBuilder().createOffsetFilter(0, stopAtOffset);
        Iterator<CsmOffsetableDeclaration> declarations = CsmSelect.getDeclarations(currentFile, filter);
        MapsCollection out = new MapsCollection(callback, needClassifiers, visitedFiles, usedNamespaces, namespaceAliases, usingDeclarations);
        gatherMaps(declarations, false, stopAtOffset, out);
    }
    
    private void initMapsFromIncludeStack(MapsCollection out, CsmFile aFile) {
        if (aFile == null || out.antiLoop.contains(aFile)) {
            return;
        }
        // gather all visible by this file's include stack
        List<CsmInclude> includeStack = CsmFileInfoQuery.getDefault().getIncludeStack(aFile);
        for (CsmInclude inc : includeStack) {
            CsmFile includedFrom = inc.getContainingFile();
            int incOffset = inc.getStartOffset();
            gatherMaps(includedFrom, incOffset, out);
        }
    }

    private static void initMapsFromIncludes(MapsCollection out, CsmFile aFile, int stopAtOffset) {
        if (aFile == null || out.antiLoop.contains(aFile)) {
            return;
        }
        // gather all visible by #include directives in this file till offset
        Iterator<CsmInclude> iter = CsmSelect.getIncludes(aFile, CsmSelect.getFilterBuilder().createOffsetFilter(0, stopAtOffset));
        while (iter.hasNext()) {
            CsmInclude inc = iter.next();
            if (inc.getStartOffset() >= stopAtOffset) {
                break;
            }
            CsmFile incFile = inc.getIncludeFile();
            if (incFile != null) {
                gatherMaps(incFile, Integer.MAX_VALUE, out);
            }
        }
    }

    interface Callback {

        boolean needToTraverseDeeper(CsmScope scope);

        void onVisibleClassifier(CsmClassifier cls);
    }

    private static final Callback EMPTY_CALLBACK = new FileMapsCollector.Callback() {

        @Override
        public boolean needToTraverseDeeper(CsmScope scope) {
            if (CsmKindUtilities.isNamespace(scope) || CsmKindUtilities.isNamespaceDefinition(scope)) {
                return ((CsmNamedElement)scope).getName().length() == 0;
            }
            return false;
        }

        @Override
        public void onVisibleClassifier(CsmClassifier cls) {
            
        }
    };
    
    private final static class MapsCollection {

        final boolean needClassifiers;
        private final Set<CsmFile> antiLoop;
        private final Map<CharSequence, CsmObject/*CsmNamespace or CsmUsingDeclaration*/> usedNamespaces;
        private final Map<CharSequence, CsmNamespace> namespaceAliases;
        private final Map<CharSequence, CsmDeclaration> usingDeclarations;
        private final Callback callback;

        public MapsCollection(Callback cb, boolean needClassifiers,
                Set<CsmFile> antiLoop,
                Map<CharSequence, CsmObject> usedNamespaces,
                Map<CharSequence, CsmNamespace> namespaceAliases,
                Map<CharSequence, CsmDeclaration> usingDeclarations) {
            this.callback = cb;
            this.needClassifiers = needClassifiers;
            this.antiLoop = antiLoop;
            this.usedNamespaces = usedNamespaces;
            this.namespaceAliases = namespaceAliases;
            this.usingDeclarations = usingDeclarations;
        }

        public boolean needClassifiers() {
            return needClassifiers;
        }

        public boolean needToTraverseDeeper(CsmScope scope) {
            return callback.needToTraverseDeeper(scope);
        }

        public void onVisibleClassifier(CsmClassifier cls) {
            callback.onVisibleClassifier(cls);
        }
    }

    private static void gatherMaps(CsmFile file, int stopAtOffset, MapsCollection out) {
        if (file == null || out.antiLoop.contains(file)) {
            return;
        }
        out.antiLoop.add(file);
        CsmSelect.CsmFilter filter;
        if (stopAtOffset == Integer.MAX_VALUE) {
            filter = NO_FILTER;
        } else {
            filter = CsmSelect.getFilterBuilder().createOffsetFilter(0, stopAtOffset);
        }
        // gather file's #include maps
        Iterator<CsmInclude> iter = CsmSelect.getIncludes(file, filter);
        while (iter.hasNext()) {
            CsmInclude inc = iter.next();
            if (inc.getStartOffset() >= stopAtOffset) {
                break;
            }
            CsmFile incFile = inc.getIncludeFile();
            if (incFile != null) {
                gatherMaps(incFile, Integer.MAX_VALUE, out);
            }
        }
        if (stopAtOffset == Integer.MAX_VALUE) {
            if (out.needClassifiers()) {
                filter = CLASS_FILTER;
            } else {
                filter = NAMESPACE_FILTER;
            }
        }
        // gather own maps up to stop offset
        Iterator<CsmOffsetableDeclaration> declarations = CsmSelect.getDeclarations(file, filter);
        gatherMaps(declarations, false, stopAtOffset, out);
    }

    private static void gatherMaps(Iterator<? extends CsmObject> it, boolean inLocalContext, int stopAtOffset, MapsCollection out) {
        while (it.hasNext()) {
            CsmObject o = it.next();
            assert o == null || CsmKindUtilities.isOffsetable(o) : "non CsmOffsetable" + o;
            if (o == null) {
                if (FileImpl.reportErrors) {
                    // FIXUP: do not crush on NPE
                    DiagnosticExceptoins.register(new NullPointerException("Unexpected NULL element in declarations collection")); // NOI18N
                }
                continue;
            }
            int start = ((CsmOffsetable) o).getStartOffset();
            int end = ((CsmOffsetable) o).getEndOffset();
            if (start >= stopAtOffset) {
                break;
            }
            if (CsmKindUtilities.isScopeElement(o)) {
                if (!inLocalContext && CsmKindUtilities.isFunctionDefinition(o)) {
                    if (end >= stopAtOffset) {
                        gatherMaps((CsmScopeElement) o, end, true, stopAtOffset, out);
                    }
                } else {
                    gatherMaps((CsmScopeElement) o, end, inLocalContext, stopAtOffset, out);
                }
            } else {
                if (FileImpl.reportErrors) {
                    System.err.println("Expected CsmScopeElement, got " + o);
                }
            }
        }
    }

//    private void doProcessTypedefsInUpperNamespaces(CsmNamespaceDefinition nsd, MapsCollection out) {
//        CsmFilter filter =  CsmSelect.getFilterBuilder().createKindFilter(
//                                  CsmDeclaration.Kind.NAMESPACE_DEFINITION,
//                                  CsmDeclaration.Kind.TYPEDEF,
//                                  CsmDeclaration.Kind.TYPEALIAS);
//        for (Iterator<CsmOffsetableDeclaration> iter = CsmSelect.getDeclarations(nsd, filter); iter.hasNext();) {
//            CsmOffsetableDeclaration decl = iter.next();
//            if( decl.getKind() == CsmDeclaration.Kind.NAMESPACE_DEFINITION ) {
//                processTypedefsInUpperNamespaces((CsmNamespaceDefinition) decl, out);
//            } else if( decl.getKind() == CsmDeclaration.Kind.TYPEDEF || decl.getKind() == CsmDeclaration.Kind.TYPEALIAS ) {
//                CsmTypedef typedef = (CsmTypedef) decl;
//                out.onVisibleClassifier(typedef);
//            }
//        }
//    }
//
//    private void processTypedefsInUpperNamespaces(CsmNamespaceDefinition nsd, MapsCollection out) {
//        if( CharSequences.comparator().compare(nsd.getName(),currName())==0 )  {
//            currNamIdx++;
//            doProcessTypedefsInUpperNamespaces(nsd, out);
//        } else {
//            CsmNamespace cns = context.getContainingNamespace();
//            if( cns != null ) {
//                if( cns.equals(nsd.getNamespace())) {
//                    doProcessTypedefsInUpperNamespaces(nsd, out);
//                }
//            }
//        }
//    }
    /**
     * It is guaranteed that element.getStartOffset < this.offset
     */
    private static void gatherMaps(CsmScopeElement element, int endOfScope, boolean inLocalContext, int stopAtOffset, MapsCollection out) {

        CsmDeclaration.Kind kind = CsmKindUtilities.isDeclaration(element) ? ((CsmDeclaration) element).getKind() : null;
        if (kind != null) {
            switch (kind) {
                case NAMESPACE_DEFINITION: {
                    CsmNamespaceDefinition nsd = (CsmNamespaceDefinition) element;
                    if (nsd.getName().length() == 0) {
                        // this is unnamed namespace and it should be considered as
                        // it declares using itself
                        out.usedNamespaces.put(nsd.getQualifiedName(), nsd.getNamespace());
                    }
                    if (stopAtOffset < endOfScope || out.needToTraverseDeeper(nsd)) {
                        //currentNamespace = nsd.getNamespace();
                        gatherMaps(nsd.getDeclarations().iterator(), inLocalContext, stopAtOffset, out);
                    } else if (out.needClassifiers()) {
                        // VV: removed this phase
//                        processTypedefsInUpperNamespaces(nsd, out);
                    }
                    return;
                }
                case NAMESPACE_ALIAS: {
                    CsmNamespaceAlias alias = (CsmNamespaceAlias) element;
                    out.namespaceAliases.put(alias.getAlias(), alias.getReferencedNamespace());
                    return;
                }
                case USING_DECLARATION: {
                    CsmDeclaration decl = ((CsmUsingDeclaration) element).getReferencedDeclaration();
                    if (decl != null) {
                        CharSequence id;
                        if (decl.getKind() == CsmDeclaration.Kind.FUNCTION
                                || decl.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION
                                || decl.getKind() == CsmDeclaration.Kind.FUNCTION_LAMBDA
                                || decl.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND
                                || decl.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION) {
                            // TODO: decide how to resolve functions
                            id = ((CsmFunction) decl).getSignature();
                        } else {
                            id = decl.getName();
                        }
                        out.usingDeclarations.put(id, decl);
                    }
                    return;
                }
                case USING_DIRECTIVE: {
                    CsmUsingDirective udir = (CsmUsingDirective) element;
                    CharSequence name = udir.getName();
                    if (!out.usedNamespaces.containsKey(name)) {
                        out.usedNamespaces.put(name, udir); // getReferencedNamespace()
                    }
                    return;
                }
                case TYPEALIAS:
                case TYPEDEF: {
                    CsmTypedef typedef = (CsmTypedef) element;
                    // don't want typedef to find itself
                    if (stopAtOffset > endOfScope) {
                        out.onVisibleClassifier(typedef);
                    }
                    return;
                }
            }
        }
        if (CsmKindUtilities.isDeclarationStatement(element)) {
            CsmDeclarationStatement ds = (CsmDeclarationStatement) element;
            if (ds.getStartOffset() < stopAtOffset) {
                gatherMaps(((CsmDeclarationStatement) element).getDeclarators().iterator(), inLocalContext, stopAtOffset, out);
            }
        } else if (CsmKindUtilities.isScope(element)) {
            if (inLocalContext && out.needClassifiers() && CsmKindUtilities.isClassifier(element)) {
                // don't want forward to find itself
                if (!CsmKindUtilities.isClassForwardDeclaration(element) || (stopAtOffset > endOfScope)) {
                    out.onVisibleClassifier((CsmClassifier) element);
                }
            }
            if (stopAtOffset < endOfScope || out.needToTraverseDeeper((CsmScope) element)) {
                gatherMaps(((CsmScope) element).getScopeElements().iterator(), inLocalContext, stopAtOffset, out);
            }
        }
    }
}
