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
package org.netbeans.modules.cnd.spi.codemodel.providers;

import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.cnd.api.codemodel.CMDiagnostic;
import org.netbeans.modules.cnd.api.codemodel.CMUnifiedSymbolResolution;
import org.netbeans.modules.cnd.api.codemodel.visit.CMVisitQuery;
import org.netbeans.modules.cnd.api.codemodel.visit.CMVisitQuery.CursorAndRangeVisitor;
import org.netbeans.modules.cnd.api.codemodel.visit.CMVisitQuery.CursorVisitor.ChildVisitRequest;
import org.netbeans.modules.cnd.api.codemodel.visit.CMVisitQuery.IndexCallback;
import org.netbeans.modules.cnd.api.codemodel.visit.CMVisitResult;
import org.netbeans.modules.cnd.spi.codemodel.CMCursorImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMDiagnosticImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMFileImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMIndexImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMSourceRangeImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMTokenImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMTranslationUnitImplementation;
import org.netbeans.modules.cnd.spi.codemodel.impl.APIAccessor;
import org.netbeans.modules.cnd.spi.codemodel.impl.APIIndexAccessor;
import org.netbeans.modules.cnd.spi.codemodel.visit.CMDeclarationImplementation;
import org.netbeans.modules.cnd.spi.codemodel.visit.CMEntityReferenceImplementation;
import org.netbeans.modules.cnd.spi.codemodel.visit.CMIncludeImplementation;

/**
 *
 * @author Vladimir Voskresensky
 */
public interface CMVisitQueryImplementation {

    public static final String PATH = "CND/CMVisitQueryImplementation"; // NOI18N

    public boolean visitIndex(CMIndexImplementation idx, IndexCallbackImplementation callback, CMVisitQuery.VisitOptions options);
    public boolean canVisitIndex(CMIndexImplementation idx, CMVisitQuery.VisitOptions options);

    public boolean visitReferences(CMIndexImplementation idx,
            CMVisitQueryImplementation.IndexCallbackImplementation callback);

    public boolean canVisitReferences(Collection<CMUnifiedSymbolResolution> usrs, CMIndexImplementation idx);

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
    public boolean visitTranslationUnit(CMIndexImplementation idx, CMTranslationUnitImplementation tu, IndexCallbackImplementation callback, CMVisitQuery.VisitOptions options);
    public boolean canVisitTranslationUnit(CMTranslationUnitImplementation tu, CMVisitQuery.VisitOptions options);

    /**
     * \brief Index the given source file and the translation unit corresponding
     * to that file via callbacks implemented through #IndexerCallbacks.
     *
     * \param client_data pointer data supplied by the client, which will
     * be passed to the invoked callbacks.
     *
     * \param index_callbacks Pointer to indexing callbacks that the client
     * implements.
     *
     * \param index_callbacks_size Size of #IndexerCallbacks structure that gets
     * passed in index_callbacks.
     *
     * \param index_options A bitmask of options that affects how indexing is
     * performed. This should be a bitwise OR of the CXIndexOpt_XXX flags.
     *
     * \param out_TU [out] pointer to store a CXTranslationUnit that can be reused
     * after indexing is finished. Set to NULL if you do not require it.
     *
     * @return If there is a failure from which the there is no recovery, returns
     * non-zero, otherwise returns 0.
     *
     * The rest of the parameters are the same as #clang_parseTranslationUnit.
     */
    public boolean visitFile(CMIndexImplementation idx, CMTranslationUnitImplementation tu, String filePath, IndexCallbackImplementation callback, CMVisitQuery.VisitOptions options);
    public boolean canVisitFile(CMTranslationUnitImplementation tu, CMVisitQuery.VisitOptions options);

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
    public CMVisitResult visitEntityReferencedInFile(CMCursorImplementation cursor, CMFileImplementation file, CursorAndRangeVisitorImplementation visitor);
    public boolean canVisitEntityReferencedInFile(CMCursorImplementation cursor, CMFileImplementation file);

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
    public CMVisitResult visitIncludesInFile(CMTranslationUnitImplementation tu, CMFileImplementation file, CursorAndRangeVisitorImplementation visitor);
    public boolean canVisitIncludesInFile(CMTranslationUnitImplementation tu, CMFileImplementation file);

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
    public boolean visitCursorChildren(CMCursorImplementation root, CursorVisitorImplementation visitor);
    public boolean canVisitCursorChildren(CMCursorImplementation impl);


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
     * @param file file to visit
     * @param visitor visitor callback
     * @return one of the Result enumerators.
     */
    public CMVisitResult visitCursorsInFile(CMTranslationUnitImplementation tu, CMFileImplementation file, CursorAndRangeVisitorImplementation visitor);
    public boolean canVisitCursorsInFile(CMFileImplementation file);

    public interface IndexCallbackFilter {
        boolean filterIndclude(CMIncludeImplementation include);
        boolean filterDeclaration(CMDeclarationImplementation decl);
        boolean filterReference(CMEntityReferenceImplementation ref);
    }

    public final class IndexCallbackImplementation {

        private final IndexCallback delegate;
        private final IndexCallbackFilter filter;

        public IndexCallbackImplementation(IndexCallback delegate) {
            this.delegate = delegate;
            this.filter = null;
        }

        public IndexCallbackImplementation(IndexCallback delegate, IndexCallbackFilter filter) {
            this.delegate = delegate;
            this.filter = filter;
        }

        /**
         * \brief Called periodically to check whether indexing should be
         * aborted.
         *
         * @return Should return false to continue, and true to abort.
         */
        public boolean isCancelled() {
            return delegate.isCancelled();
        }

        /**
         * \brief Called at the end of indexing; passes the complete diagnostic
         * set.
         *
         * @param diagnostics
         */
        public void onDiagnostics(final Iterable<CMDiagnosticImplementation> diagnostics) {
            delegate.onDiagnostics(new Iterable<CMDiagnostic>() {
                @Override
                public Iterator<CMDiagnostic> iterator() {
                    final Iterator<CMDiagnosticImplementation> delegate = diagnostics.iterator();
                    return new Iterator<CMDiagnostic>() {
                        @Override
                        public boolean hasNext() {
                            return delegate.hasNext();
                        }

                        @Override
                        public CMDiagnostic next() {
                            return APIAccessor.get().createDiagnostic(delegate.next());
                        }

                        @Override
                        public void remove() {
                            delegate.remove();
                        }
                    };
                }
            });
        }

        /**
         * \brief Called when a file gets \#included/\#imported.
         *
         * @param include
         */
        public void onIndclude(CMIncludeImplementation include) {
            if (filter == null || filter.filterIndclude(include)) {
                delegate.onIndclude(APIIndexAccessor.get().createInclude(include));
            }
        }

        /**
         * \brief Called at the beginning of indexing a translation unit.
         */
        public void onTranslationUnit() {
            delegate.onTranslationUnit();
        }

        /**
         * \brief Called to index a declaration of an entity.
         *
         * @param decl
         */
        public void onDeclaration(CMDeclarationImplementation decl) {
            if (filter == null || filter.filterDeclaration(decl)) {
                delegate.onDeclaration(APIIndexAccessor.get().createDeclaration(decl));
            }
        }

        /**
         * \brief Called to index a reference of an entity.
         *
         * @param ref
         */
        public void onReference(CMEntityReferenceImplementation ref) {
            if (filter == null || filter.filterReference(ref)) {
                delegate.onReference(APIIndexAccessor.get().createEntityReference(ref));
            }
        }

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
    
    public final class CursorAndRangeVisitorImplementation {

        private final CursorAndRangeVisitor delegate;

        public CursorAndRangeVisitorImplementation(CursorAndRangeVisitor delegate) {
            this.delegate = delegate;
        }

        /**
         * \brief Called periodically to check whether indexing should be
         * aborted.
         *
         * @return Should return false to continue, and true to abort.
         */
        public boolean isCancelled() {
            return delegate.isCancelled();
        }

        /**
         * handle cursor visit action.
         *
         * @param cur visited cursor
         * @param curRange cursor's source range
         * @return true to continue, false to break visiting.
         */
        public boolean visit(CMCursorImplementation cur, CMSourceRangeImplementation curRange) {
            return delegate.visit(APIAccessor.get().createCursor(cur), APIAccessor.get().createSourceRange(curRange));
        }
    }
    
    public final class CursorVisitorImplementation {
        private final  CMVisitQuery.CursorVisitor delegete;
                
        public CursorVisitorImplementation(CMVisitQuery.CursorVisitor delegate) {
            this.delegete = delegate;
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
        public ChildVisitRequest visit(CMCursorImplementation cursor, CMCursorImplementation parent) {
            return delegete.visit(APIAccessor.get().createCursor(cursor), APIAccessor.get().createCursor(parent));
        }
    }

    public final class TokenVisitorImplementation {
        private final  CMVisitQuery.TokenVisitor delegete;
                
        public TokenVisitorImplementation(CMVisitQuery.TokenVisitor delegate) {
            this.delegete = delegate;
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
         * @param token
         * @param parent
         * @return
         */
        public CMVisitQuery.TokenVisitor.TokenVisitRequest visit(CMTokenImplementation token) {
            return delegete.visit(APIAccessor.get().createToken(token));
        }
    }
    
    public boolean tokenizeSourceRange(CMTranslationUnitImplementation tu, CMSourceRangeImplementation range, TokenVisitorImplementation visitor);
    public boolean canTokenizeSourceRange(CMTranslationUnitImplementation tu, CMSourceRangeImplementation range);
    
}
