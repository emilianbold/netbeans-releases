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
import org.netbeans.modules.cnd.api.codemodel.completion.CMCompletionQuery;
import org.netbeans.modules.cnd.spi.codemodel.CMCursorImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMTranslationUnitImplementation;
import org.netbeans.modules.cnd.spi.codemodel.completion.CMCompletionResultImplementation;
import org.netbeans.modules.cnd.spi.codemodel.completion.CMCompletionResultListImplementation;

/**
 *
 * @author Vladimir Voskresensky
 * @author Vladimir Kvashin
 */
public interface CMCompletionQueryImplementation {

    /**
     * service to be registered using
     *
     * @ServiceProvider(path = CMCompletionQueryImplementation.PATH, service = CMCompletionQueryImplementation.class)
     */
    public static final String PATH = "CND/CMCompletionQueryImplementation"; // NOI18N

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
    CMCompletionResultListImplementation getCompletion(CMTranslationUnitImplementation tu, CharSequence fileName, int line, int column, Collection<CMUnsavedFileImplementation> unsavedFiles, CMCompletionQuery.QueryFlags flags);
    
    /**
     * \brief Retrieve a completion string for an arbitrary declaration or macro
     * definition cursor.
     *
     * @param cursor The cursor to query.
     *
     * @return A non-context-sensitive completion string for declaration and
     * macro definition cursors, or NULL for other kinds of cursors.
     */
    CMCompletionResultImplementation getCursorCompletionResult(CMCursorImplementation cursor);
    
}
