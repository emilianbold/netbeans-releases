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
package org.netbeans.modules.cnd.api.codemodel.completion;

import java.net.URI;
import java.util.Collection;
import org.netbeans.modules.cnd.api.codemodel.CMCursor;
import org.netbeans.modules.cnd.api.codemodel.CMTranslationUnit;
import org.netbeans.modules.cnd.spi.codemodel.CMCursorImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMTranslationUnitImplementation;
import org.netbeans.modules.cnd.spi.codemodel.completion.CMCompletionChunkImplementation;
import org.netbeans.modules.cnd.spi.codemodel.providers.CMCompletionQueryImplementation;
import org.netbeans.modules.cnd.spi.codemodel.completion.CMCompletionResultImplementation;
import org.netbeans.modules.cnd.spi.codemodel.completion.CMCompletionResultListImplementation;
import org.netbeans.modules.cnd.spi.codemodel.impl.APIAccessor;
import org.netbeans.modules.cnd.spi.codemodel.impl.APICompletionAccessor;
import org.netbeans.modules.cnd.spi.codemodel.support.CMFactory;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Vladimir Voskresensky
 * @author Vladimir Kvashin
 */
public final class CMCompletionQuery {

    //<editor-fold defaultstate="collapsed" desc="QueryFlags">
    /**
     * \brief Flags that can be passed to \c clang_codeCompleteAt() to modify
     * its behavior.
     *
     * The enumerators in this enumeration can be bitwise-OR'd together to
     * provide multiple options to \c clang_codeCompleteAt().
     */
    public static final class QueryFlags {

        /**
         * \brief Whether to include macros within the set of code completions
         * returned.
         */
        public static final QueryFlags IncludeMacros = bitFlag(0x01);
        /**
         * \brief Whether to include code patterns for language constructs
         * within the set of code completions, e.g., for loops.
         */
        public static final QueryFlags IncludeCodePatterns = bitFlag(0x02);
        /**
         * \brief Whether to include brief documentation within the set of code
         * completions returned.
         */
        public static final QueryFlags IncludeBriefComments = bitFlag(0x04);

        //<editor-fold defaultstate="collapsed" desc="hidden">
        private static QueryFlags bitFlag(int oneBitValue) {
            assert oneBitValue == 0 || ((oneBitValue & (oneBitValue - 1)) == 0) : "must have only one bit set " + Integer.toBinaryString(oneBitValue);
            return new QueryFlags(oneBitValue);
        }
        private final byte value;

        private QueryFlags(int mask) {
            assert Byte.MIN_VALUE <= mask && mask <= Byte.MAX_VALUE : "mask " + mask;
            this.value = (byte) mask;
        }

        public static QueryFlags valueOf(QueryFlags... flags) {
            assert flags != null;
            if (flags.length == 1) {
                return flags[0];
            }
            byte bitOrValue = 0;
            for (QueryFlags f : flags) {
                bitOrValue |= f.value;
            }
            return new QueryFlags(bitOrValue);
        }

        public byte getValue() {
            return value;
        }
        //</editor-fold>
    }
    //</editor-fold>

    /**
     * \brief Perform code completion at a given location in some translation
     * unit(s) associated with the file.
     *
     * This function performs code completion at a particular file, line, and
     * column within source code, providing results that suggest potential code
     * snippets based on the context of the completion. The basic model for code
     * completion is that Clang will parse a complete source file, performing
     * syntax checking up to the location where code-completion has been
     * requested. At that point, a special code-completion token is passed to
     * the parser, which recognizes this token and determines, based on the
     * current location in the C/Objective-C/C++ grammar and the state of
     * semantic analysis, what completions to provide. These completions are
     * returned via a new \c CXCodeCompleteResults structure.
     *
     * Code completion itself is meant to be triggered by the client when the
     * user types punctuation characters or whitespace, at which point the
     * code-completion location will coincide with the cursor. For example, if
     * \c p is a pointer, code-completion might be triggered after the "-" and
     * then after the ">" in \c p->. When the code-completion location is afer
     * the ">", the completion results will provide, e.g., the members of the
     * struct that "p" points to. The client is responsible for placing the
     * cursor at the beginning of the token currently being typed, then
     * filtering the results based on the contents of the token. For example,
     * when code-completing for the expression \c p->get, the client should
     * provide the location just after the ">" (e.g., pointing at the "g") to
     * this code-completion hook. Then, the client can filter the results based
     * on the current token text ("get"), only showing those results that start
     * with "get". The intent of this interface is to separate the relatively
     * high-latency acquisition of code-completion results from the filtering of
     * results on a per-character basis, which must have a lower latency.
     *
     * @param TU The translation unit in which code-completion should occur. The
     * source files for this translation unit need not be completely up-to-date
     * (and the contents of those source files may be overridden via \p
     * unsaved_files). Cursors referring into the translation unit may be
     * invalidated by this invocation.
     *
     * @param complete_filename The name of the source file where code
     * completion should be performed. This filename may be any file included in
     * the translation unit.
     *
     * @param complete_line The line at which code-completion should occur.
     *
     * @param complete_column The column at which code-completion should occur.
     * Note that the column should point just after the syntactic construct that
     * initiated code completion, and not in the middle of a lexical token.
     *
     * @param unsaved_files the Tiles that have not yet been saved to disk but
     * may be required for parsing or code completion, including the contents of
     * those files. The contents and name of these files (as specified by
     * CXUnsavedFile) are copied when necessary, so the client only needs to
     * guarantee their validity until the call to this function returns.
     *
     * @param num_unsaved_files The number of unsaved file entries in \p
     * unsaved_files.
     *
     * @param options Extra options that control the behavior of code
     * completion, expressed as a bitwise OR of the enumerators of the
     * CXCodeComplete_Flags enumeration. The \c
     * clang_defaultCodeCompleteOptions() function returns a default set of
     * code-completion options.
     *
     * @return If successful, a new \c CXCodeCompleteResults structure
     * containing code-completion results, which should eventually be freed with
     * \c clang_disposeCodeCompleteResults(). If code completion fails, returns
     * NULL.
     */
    public static CMCompletionResultList getCompletion(Collection<CMTranslationUnit> units, URI uri, int line, int column, QueryFlags flags) {
        for (CMCompletionQueryImplementation q : ccQueries.allInstances()) {
            // TODO:
            if (!units.isEmpty()) {
                for (CMTranslationUnit unit : units) {
                    CMTranslationUnitImplementation tu = APIAccessor.get().getTUImpl(unit);
                    CMCompletionResultListImplementation out = q.getCompletion(tu, uri.getPath(), line, column, tu.getUnsavedFiles(), flags);
                    if (out != null && out.getItems().iterator().hasNext()) {
                        return CMFactory.CompletionAPI.createCompletionResultList(out);
                    }
                }
            }
        }
        return null;
    }

    /**
     * \brief Retrieve a completion string for an arbitrary declaration or macro
     * definition cursor.
     *
     * @param cursor The cursor to query.
     *
     * @return A non-context-sensitive completion string for declaration and
     * macro definition cursors, or NULL for other kinds of cursors.
     */
    public static CMCompletionResult getCursorCompletionResult(CMCursor cursor) {
        for (CMCompletionQueryImplementation q : ccQueries.allInstances()) {
            CMCursorImplementation cursorImpl = APIAccessor.get().getCursorImpl(cursor);
            CMCompletionResultImplementation out = q.getCursorCompletionResult(cursorImpl);
            if (out != null) {
                return CMFactory.CompletionAPI.createCompletionResult(out);
            }
        }
        return null;
    }
    //<editor-fold defaultstate="collapsed" desc="hidden impl">
    private static final Lookup.Result<CMCompletionQueryImplementation> ccQueries;

    static {
        ccQueries = Lookups.forPath(CMCompletionQueryImplementation.PATH).lookupResult(CMCompletionQueryImplementation.class);
    }

    private CMCompletionQuery() {
    }

    static {
        APICompletionAccessor.register(new APIAccessorImpl());
    }

    private static final class APIAccessorImpl extends APICompletionAccessor {

        @Override
        public CMCompletionResultList createCompletionResultList(CMCompletionResultListImplementation impl) {
            return new CMCompletionResultList(impl);
        }

        @Override
        public CMCompletionResult createCompletionResult(CMCompletionResultImplementation impl) {
            return CMCompletionResult.fromImpl(impl);
        }

        @Override
        public CMCompletionChunk createCompletionChunk(CMCompletionChunkImplementation impl) {
            return CMCompletionChunk.fromImpl(impl);
        }
    }
    //</editor-fold>
}
