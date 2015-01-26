/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks valueOf Oracle and/or its affiliates.
 * Other names may be trademarks valueOf their respective owners.
 *
 * The contents valueOf this file are subject to the terms valueOf either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy valueOf the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section valueOf the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name valueOf copyright owner]"
 *
 * If you wish your version valueOf this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice valueOf license, a recipient has the option to distribute
 * your version valueOf this file under either the CDDL, the GPL Version 2 or
 * to extend the choice valueOf license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.api.codemodel.visit;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.cnd.api.codemodel.CMCursor;
import org.netbeans.modules.cnd.api.codemodel.CMDiagnostic;
import org.netbeans.modules.cnd.api.codemodel.CMIndex;
import org.netbeans.modules.cnd.api.codemodel.CMSourceRange;
import org.netbeans.modules.cnd.api.codemodel.CMToken;
import org.netbeans.modules.cnd.api.codemodel.CMTranslationUnit;
import org.netbeans.modules.cnd.api.codemodel.CMUnifiedSymbolResolution;
import org.netbeans.modules.cnd.spi.codemodel.CMCursorImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMFileImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMIndexImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMSourceRangeImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMTranslationUnitImplementation;
import org.netbeans.modules.cnd.spi.codemodel.impl.APIAccessor;
import org.netbeans.modules.cnd.spi.codemodel.impl.APIIndexAccessor;
import org.netbeans.modules.cnd.spi.codemodel.visit.CMDeclarationImplementation;
import org.netbeans.modules.cnd.spi.codemodel.visit.CMEntityImplementation;
import org.netbeans.modules.cnd.spi.codemodel.visit.CMEntityReferenceImplementation;
import org.netbeans.modules.cnd.spi.codemodel.visit.CMIncludeImplementation;
import org.netbeans.modules.cnd.spi.codemodel.visit.CMVisitLocationImplementation;
import org.netbeans.modules.cnd.spi.codemodel.providers.CMVisitQueryImplementation;
import org.netbeans.modules.cnd.spi.codemodel.support.SPIUtilities;
import org.netbeans.modules.cnd.spi.codemodel.visit.CMDeclarationContextImplementation;
import org.netbeans.modules.cnd.spi.codemodel.visit.CMReferenceImplementation;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * set of visiting queries.
 *
 * @author Vladimir Voskresensky
 * @author Vladimir Kvashin
 */
public class CMVisitQuery {

    private static CMVisitQueryImplementation.IndexCallbackImplementation wrapIndexCallback(IndexCallback callback) {
        return new CMVisitQueryImplementation.IndexCallbackImplementation(callback);
    }

    private static CMVisitQueryImplementation.IndexCallbackImplementation wrapIndexCallback(
            IndexCallback callback, CMVisitQueryImplementation.IndexCallbackFilter filter) {
        return new CMVisitQueryImplementation.IndexCallbackImplementation(callback, filter);
    }

    private static CMVisitQueryImplementation.CursorVisitorImplementation wrapCursorVisitor(CursorVisitor visitor) {
        return new CMVisitQueryImplementation.CursorVisitorImplementation(visitor);
    }

    private static CMVisitQueryImplementation.CursorAndRangeVisitorImplementation wrapCursorAndRangeVisitor(CursorAndRangeVisitor visitor) {
        return new CMVisitQueryImplementation.CursorAndRangeVisitorImplementation(visitor);
    }

    private static CMVisitQueryImplementation.TokenVisitorImplementation wrapTokenVisitor(TokenVisitor visitor) {
        return new CMVisitQueryImplementation.TokenVisitorImplementation(visitor);
    }

    private static final class DiagnosticsCallbackWrapper implements IndexCallback {

        private final DiagnosticsCallback delegate;

        public DiagnosticsCallbackWrapper(DiagnosticsCallback delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean isCancelled() {
            return delegate.isCancelled();
        }

        @Override
        public void onDiagnostics(Iterable<CMDiagnostic> diagnostics) {
            delegate.onDiagnostics(diagnostics);
        }

        @Override
        public void onIndclude(CMInclude include) {}

        @Override
        public void onTranslationUnit() {}

        @Override
        public void onDeclaration(CMDeclaration decl) {}

        @Override
        public void onReference(CMEntityReference ref) {}
    }

    private static CMVisitQueryImplementation.IndexCallbackImplementation wrapDiagnosticsCallback(DiagnosticsCallback callback) {
        return new CMVisitQueryImplementation.IndexCallbackImplementation(new DiagnosticsCallbackWrapper(callback));
    }

    public interface IndexCallback {

        /**
         * \brief Called periodically to check whether indexing should be
         * aborted.
         *
         * @return Should return false to continue, and true to abort.
         */
        boolean isCancelled();

        /**
         * \brief Called at the end of indexing; passes the complete diagnostic
         * set.
         *
         * @param diagnostics
         */
        void onDiagnostics(Iterable<CMDiagnostic> diagnostics);

        /**
         * \brief Called when a file gets \#included/\#imported.
         *
         * @param include
         */
        void onIndclude(CMInclude include);

        /**
         * \brief Called at the beginning of indexing a translation unit.
         */
        void onTranslationUnit();

        /**
         * \brief Called to index a declaration of an entity.
         *
         * @param decl
         */
        void onDeclaration(CMDeclaration decl);

        /**
         * \brief Called to index a reference of an entity.
         *
         * @param ref
         */
        void onReference(CMEntityReference ref);

        //<editor-fold defaultstate="collapsed" desc="TODO">
        /**
         * TODO: move method to SPI \brief Called when a AST file (PCH or
         * module) gets imported.
         *
         * AST files will not get indexed (there will not be callbacks to index
         * all the entities in an AST file). The recommended action is that, if
         * the AST file is not already indexed, to initiate a new indexing job
         * specific to the AST file.
         */
        //  CXIdxClientASTFile (*importedASTFile)(CXClientData client_data, const CXIdxImportedASTFileInfo *);
        //</editor-fold>
    }

    public static final class VisitOptions {

        /**
         * \brief Used to indicate that no special indexing options are needed.
         */
        public static final VisitOptions None = bitFlag(0x0);

        /**
         * \brief Used to indicate that IndexerCallbacks#indexEntityReference
         * should be invoked for only one reference of an entity per source file
         * that does not also include a declaration/definition of the entity.
         */
        public static final VisitOptions SuppressRedundantRefs = bitFlag(0x1);

        /**
         * \brief Function-local symbols should be indexed. If this is not set
         * function-local symbols will be ignored.
         */
        public static final VisitOptions IndexFunctionLocalSymbols = bitFlag(0x2);

        /**
         * \brief Implicit function/class template instantiations should be
         * indexed. If this is not set, implicit instantiations will be ignored.
         */
        public static final VisitOptions IndexImplicitTemplateInstantiations = bitFlag(0x4);

        /**
         * \brief Suppress all compiler warnings when parsing for indexing.
         */
        public static final VisitOptions SuppressWarnings = bitFlag(0x8);

        /**
         * \brief Skip a function/method body that was already parsed during an
         * indexing session associated with a \c CXIndexAction object. Bodies in
         * system headers are always skipped.
         */
        public static final VisitOptions SkipParsedBodiesInSession = bitFlag(0x10);

        private static VisitOptions bitFlag(int oneBitValue) {
            assert oneBitValue == 0 || ((oneBitValue & (oneBitValue - 1)) == 0) : "must have only one bit set " + Integer.toBinaryString(oneBitValue);
            return new VisitOptions(oneBitValue);
        }

        private final byte value;

        private VisitOptions(int mask) {
            assert Byte.MIN_VALUE <= mask && mask <= Byte.MAX_VALUE : "mask " + mask;
            this.value = (byte) mask;
        }

        public static VisitOptions valueOf(VisitOptions... flags) {
            assert flags != null;
            if (flags.length == 1) {
                return flags[0];
            }
            byte bitOrValue = 0;
            for (VisitOptions f : flags) {
                bitOrValue |= f.value;
            }
            return new VisitOptions(bitOrValue);
        }

        public int value() {
            return value;
        }
    }

    private static final Lookup.Result<CMVisitQueryImplementation> idxQueries;

    static {
        idxQueries = Lookups.forPath(CMVisitQueryImplementation.PATH).lookupResult(CMVisitQueryImplementation.class);
    }

    /**
     * visit all translation units in index.
     *
     * @param idx
     * @param callback
     * @param options
     * @return
     */
    public static boolean visitIndex(CMIndex idx, IndexCallback callback, VisitOptions options) {
        boolean out = false;
        for (CMVisitQueryImplementation q : idxQueries.allInstances()) {
            CMIndexImplementation impl = APIAccessor.get().getIndexImpl(idx);
            if (q.canVisitIndex(impl, options)) {
                out |= q.visitIndex(impl, wrapIndexCallback(callback), options);
            }
        }
        return out;
    }

    /**
     * visit all references to one of the given entities
     *
     * @param usrs
     * @param indices
     * @param callback
     * @return
     * @deprecated
     */
    public static boolean visitReferences(
            Collection<CMUnifiedSymbolResolution> usrs,
            Collection<CMIndex> indices, IndexCallback callback) {

        boolean out = false;
        final Set<CMUnifiedSymbolResolution> usrSet = usrs == null ? null : new HashSet<>(usrs);

        CMVisitQueryImplementation.IndexCallbackFilter filter = new CMVisitQueryImplementation.IndexCallbackFilter() {
            @Override
            public boolean filterIndclude(CMIncludeImplementation include) {
                return false;
            }

            @Override
            public boolean filterDeclaration(CMDeclarationImplementation decl) {
                //visit all 
                if (usrSet == null) {
                    return true;
                }
                CMEntityImplementation entity = decl.getEntity();
                if (entity != null) {
                    CMUnifiedSymbolResolution usr = entity.getUSR();
                    if (usr != null) {
                        return usrSet.contains(usr);
                    }
                }
                return false;
            }

            @Override
            public boolean filterReference(CMEntityReferenceImplementation ref) {
                //visit all 
                if (usrSet == null) {
                    return true;
                }

                CMEntityImplementation entity = ref.getReferencedEntity();
                if (entity != null) {
                    CMUnifiedSymbolResolution usr = entity.getUSR();
                    if (usr != null) {
                        return usrSet.contains(usr);
                    }
                }
                return false;
            }
        };

        for (CMVisitQueryImplementation q : idxQueries.allInstances()) {
            for (CMIndex idx : indices) {
                CMIndexImplementation impl = APIAccessor.get().getIndexImpl(idx);
                if (q.canVisitReferences(usrs, impl)) {
                    out |= q.visitReferences(impl, wrapIndexCallback(callback, filter));
                }
            }
        }
        return out;
    }

    /**
     * \brief Index the given translation unit via callbacks implemented through
     * #IndexerCallbacks.
     *
     * The order of callback invocations is not guaranteed to be the same as
     * when indexing a source file. The high level order will be:
     *
     * <pre>
     * -Preprocessor callbacks invocations
     * -Declaration/reference callbacks invocations
     * -Diagnostic callback invocations
     * </pre>
     *
     * @param tu
     * @param callback
     * @param options
     *
     * @return If there is a failure from which the there is no recovery,
     * returns false, otherwise returns true.
     */
    public static boolean visitTranslationUnit(CMTranslationUnit tu, IndexCallback callback, VisitOptions options) {
        boolean out = false;
        for (CMVisitQueryImplementation q : idxQueries.allInstances()) {
            CMTranslationUnitImplementation impl = APIAccessor.get().getTUImpl(tu);
            Collection<CMIndex> indices = SPIUtilities.getIndices();
            for (CMIndex cMIndex : indices) {
                CMIndexImplementation indexImpl = APIAccessor.get().getIndexImpl(cMIndex);
                if (q.canVisitTranslationUnit(impl, options)) {
                    out |= q.visitTranslationUnit(indexImpl, impl, wrapIndexCallback(callback), options);
                }
            }
        }
        return out;
    }

    public interface DiagnosticsCallback {

        /**
         * \brief Called periodically to check whether indexing should be
         * aborted.
         *
         * @return Should return false to continue, and true to abort.
         */
        boolean isCancelled();

        /**
         * \brief Called at the end of indexing; passes the complete diagnostic
         * set.
         *
         * @param diagnostics
         */
        void onDiagnostics(Iterable<CMDiagnostic> diagnostics);
    }

    public static boolean visitDiagnostics(URI uri, DiagnosticsCallback callback) {
        boolean out = false;
        VisitOptions options = VisitOptions.valueOf(
                VisitOptions.IndexFunctionLocalSymbols,
                VisitOptions.IndexImplicitTemplateInstantiations);



        for (CMVisitQueryImplementation q : idxQueries.allInstances()) {
            for (CMTranslationUnit unit : SPIUtilities.getTranslationUnits(uri)) {
                CMTranslationUnitImplementation tuImpl = APIAccessor.get().getTUImpl(unit);
                Collection<CMIndex> indices = SPIUtilities.getIndices();
                for (CMIndex cMIndex : indices) {
                    CMIndexImplementation indexImpl = APIAccessor.get().getIndexImpl(cMIndex);
                    if (q.canVisitFile(tuImpl, options)) {
                        out |= q.visitFile(indexImpl, tuImpl, uri.getPath(), wrapDiagnosticsCallback(callback), options);
                    }                    
                }

            }
        }
        return out;
    }

    public interface CursorAndRangeVisitor {

        /**
         * \brief Called periodically to check whether indexing should be
         * aborted.
         *
         * @return Should return false to continue, and true to abort.
         */
        boolean isCancelled();

        /**
         * handle cursor visit action.
         *
         * @param cur visited cursor
         * @param curRange cursor's source range
         * @return true to continue, false to break visiting.
         */
        boolean visit(CMCursor cur, CMSourceRange curRange);
    }

    /**
     * \brief Visit the given file tokens providing cursors for each token that
     * can be mapped to a specific entity within the abstract syntax tree.
     *
     * This token-annotation routine is equivalent to invoking getCursor() for
     * the source locations of each of the tokens. The cursors provided are
     * filtered, so that only those cursors that have a direct correspondence to
     * the token are accepted. For example, given a function call \c f(x),
     * getCursor() would provide the following cursors:
     *
     * <code>
     * - when the cursor is over the 'f', a DeclRefExpr cursor referring to 'f'.
     * - when the cursor is over the '(' or the ')', a CallExpr referring to 'f'.
     * - when the cursor is over the 'x', a DeclRefExpr cursor referring to 'x'.
     * </code>
     *
     * Only the first and last of these cursors will occur within the visitor,
     * since the tokens "f" and "x' directly refer to a function and a variable,
     * respectively, but the parentheses are just a small part of the full
     * syntax of the function call expression, which is not provided as an
     * annotation.
     *
     * @param units translation units to visit
     * @param uri file to visit
     * @param visitor visitor callback
     * @return one of the Result enumerators.
     */
    public static CMVisitResult visitCursorsInFile(Collection<CMTranslationUnit> units, URI uri, CursorAndRangeVisitor visitor) {
        CMVisitResult out = CMVisitResult.Invalid;        
        for (CMVisitQueryImplementation q : idxQueries.allInstances()) {
            for (CMTranslationUnit unit : units) {
                CMTranslationUnitImplementation tu  = APIAccessor.get().getTUImpl(unit);
                CMFileImplementation fImpl = tu.getFile(uri);
                if (q.canVisitCursorsInFile(fImpl)) {
                    CMVisitResult visit = q.visitCursorsInFile(tu, fImpl, wrapCursorAndRangeVisitor(visitor));
                }
            }
        }
        return out;
    }

    /**
     * \brief Visit all cursor-referenced positions in a specific file (mark
     * occurrences).
     *
     * @param cursor pointing to a declaration or a reference of interested
     * entity.
     *
     * @param file to search for references.
     *
     * @param visitor callback that will receive pairs of CMCursor/CMSourceRange
     * for each reference found. The CMSourceRange will point inside the file;
     * if the reference is inside a macro (and not a macro argument) the
     * CMSourceRange will be invalid.
     *
     * @return one of the Result enumerators.
     */
    public CMVisitResult visitEntityReferencedInFile(CMCursor cursor, URI uri, CursorAndRangeVisitor visitor) {
        CMCursorImplementation cImpl = APIAccessor.get().getCursorImpl(cursor);
        for (CMVisitQueryImplementation q : idxQueries.allInstances()) {
            for (CMTranslationUnit unit : SPIUtilities.getTranslationUnits(uri)) {
                CMTranslationUnitImplementation tu  = APIAccessor.get().getTUImpl(unit);
                CMFileImplementation fImpl = tu.getFile(uri);
                if (q.canVisitEntityReferencedInFile(cImpl, fImpl)) {
                    CMVisitResult visit = q.visitEntityReferencedInFile(cImpl, fImpl, wrapCursorAndRangeVisitor(visitor));
                }

            }
        }
        return CMVisitResult.Invalid;
    }

    /**
     * \brief Find #import/#include directives in a specific file.
     *
     * @param tu translation unit containing the file to query.
     *
     * @param file to search for #import/#include directives.
     *
     * @param visitor callback that will receive pairs of CMCursor/CMSourceRange
     * for each directive found.
     *
     * @return one of the Result enumerators.
     */
    public CMVisitResult visitIncludesInFile(CMTranslationUnit tu, URI uri, CursorAndRangeVisitor visitor) {
        CMVisitResult out = CMVisitResult.Invalid;
        for (CMVisitQueryImplementation q : idxQueries.allInstances()) {
            for (CMTranslationUnit unit : SPIUtilities.getTranslationUnits(uri)) {
                CMTranslationUnitImplementation tuImpl  = APIAccessor.get().getTUImpl(tu);
                CMFileImplementation fImpl = tuImpl.getFile(uri);                
                if (q.canVisitIncludesInFile(tuImpl, fImpl)) {
                    CMVisitResult visit = q.visitIncludesInFile(tuImpl, fImpl, wrapCursorAndRangeVisitor(visitor));
                }
            }
        }
        return out;
    }

    public interface CursorVisitor {

        /**
         * \brief Describes how the traversal of the children of a particular
         * cursor should proceed after visiting a particular child cursor.
         *
         * A value of this enumeration type should be returned by each \c
         * CXCursorVisitor to indicate how visitCursorChildren() proceed.
         */
        public enum ChildVisitRequest {

            Invalid(-1),
            /**
             * \brief Terminates the cursor traversal.
             */
            Break(0),
            /**
             * \brief Continues the cursor traversal with the next sibling of
             * the cursor just visited, without visiting its children.
             */
            Continue(1),
            /**
             * \brief Recursively traverse the children of this cursor, using
             * the same visitor and client data.
             */
            Recurse(2);

            //<editor-fold defaultstate="collapsed" desc="hidden">
            public static ChildVisitRequest valueOf(int val) {
                byte langVal = (byte) val;
                for (ChildVisitRequest kind : ChildVisitRequest.values()) {
                    if (kind.value == langVal) {
                        return kind;
                    }
                }
                assert false : "unsupported kind " + val;
                return Invalid;
            }

            private final byte value;

            private ChildVisitRequest(int lang) {
                this.value = (byte) lang;
            }

            public int getValue() {
                return value;
            }
            //</editor-fold>
        }

        /**
         * \brief Visitor invoked for each cursor found by a traversal.
         *
         * This visitor function will be invoked for each cursor found by
         * visitCursorChildren(). Its first argument is the cursor being
         * visited, its second argument is the parent visitor for that cursor,
         * and its third argument is the client data provided to
         * visitCursorChildren().
         *
         * The visitor should return one of the \c ChildVisitRequest values to
         * direct visitCursorChildren().
         *
         * @param cursor
         * @param parent
         * @return
         */
        ChildVisitRequest visit(CMCursor cursor, CMCursor parent);
    }

    /**
     * \brief Visit the children of a particular cursor.
     *
     * This function visits all the direct children of the given cursor,
     * invoking the given \p visitor function with the cursors of each visited
     * child. The traversal may be recursive, if the visitor returns \c
     * ChildVisitRequest.Recurse. The traversal may also be ended prematurely,
     * if the visitor returns \c ChildVisitRequest.Break.
     *
     * @param root the cursor whose child may be visited. All kinds of cursors
     * can be visited, including invalid cursors (which, by definition, have no
     * children).
     *
     * @param visitor the visitor function that will be invoked for each child
     * of \p parent.
     *
     * @return false value if the traversal was terminated prematurely by the
     * visitor returning \c ChildVisitRequest.Break.
     */
    public static boolean visitCursorChildren(CMCursor root, CursorVisitor visitor) {
        boolean out = false;
        for (CMVisitQueryImplementation q : idxQueries.allInstances()) {
            CMCursorImplementation impl = APIAccessor.get().getCursorImpl(root);
            if (q.canVisitCursorChildren(impl)) {
                out |= q.visitCursorChildren(impl, wrapCursorVisitor(visitor));
            }
        }
        return out;
    }
    
    public interface TokenVisitor {

        public enum TokenVisitRequest {

            Invalid(-1),
            /**
             * \brief Terminates the tokens traversal.
             */
            Break(0),
            /**
             * \brief Continues the tokens traversal with the next sibling of
             * the token just visited.
             */
            Continue(1);

            //<editor-fold defaultstate="collapsed" desc="hidden">
            public static TokenVisitRequest valueOf(int val) {
                byte langVal = (byte) val;
                for (TokenVisitRequest kind : TokenVisitRequest.values()) {
                    if (kind.value == langVal) {
                        return kind;
                    }
                }
                assert false : "unsupported kind " + val;
                return Invalid;
            }

            private final byte value;

            private TokenVisitRequest(int lang) {
                this.value = (byte) lang;
            }

            public int getValue() {
                return value;
            }
            //</editor-fold>
        }

        TokenVisitRequest visit(CMToken token);
    }    

    public static boolean tokenizeSourceRange(CMTranslationUnit tu, CMSourceRange tokenizedRange, TokenVisitor visitor) {
        boolean out = false;
        CMSourceRangeImplementation rangeImpl = APIAccessor.get().getSourceRangeImpl(tokenizedRange);
        CMTranslationUnitImplementation tuImpl = APIAccessor.get().getTUImpl(tu);
        for (CMVisitQueryImplementation q : idxQueries.allInstances()) {
            if (q.canTokenizeSourceRange(tuImpl, rangeImpl)) {
                out |= q.tokenizeSourceRange(tuImpl, rangeImpl, wrapTokenVisitor(visitor));
            }
        }
        return out;
    }
    
    //<editor-fold defaultstate="collapsed" desc="hidden impl">
    static {
        APIIndexAccessor.register(new APIIndexAccessorImpl());
    }

    private static final class APIIndexAccessorImpl extends APIIndexAccessor {

        @Override
        public CMEntity createEntity(CMEntityImplementation impl) {
            return CMEntity.fromImpl(impl);
        }

        @Override
        public CMDeclaration createDeclaration(CMDeclarationImplementation impl) {
            return CMDeclaration.fromImpl(impl);
        }

        @Override
        public CMEntityReference createEntityReference(CMEntityReferenceImplementation impl) {
            return CMEntityReference.fromImpl(impl);
        }

        @Override
        public CMInclude createInclude(CMIncludeImplementation impl) {
            return CMInclude.fromImpl(impl);
        }

        @Override
        public CMVisitLocation createVisitLocation(CMVisitLocationImplementation impl) {
            return CMVisitLocation.fromImpl(impl);
        }

        @Override
        public CMReferenceImplementation getReferenceImpl(CMReference ref) {
            return ref.getImpl();
        }   

        @Override
        public CMDeclarationContextImplementation getDeclarationContextImpl(CMDeclarationContext dc) {
            return dc.getImpl();
        }                
    }
    //</editor-fold>

}
