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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.gsf.api;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.JTextComponent;

import org.netbeans.modules.gsf.api.annotations.CheckForNull;
import org.netbeans.modules.gsf.api.annotations.NonNull;


/**
 * Provide code completion for this language. This implementation
 * is responsible for all the analysis around the given caret offset.
 * A code completion provider should be smart and for example limit
 * alternatives not just by the given identifier prefix at the caret offset,
 * but also by the surrounding context; for example, if we're doing
 * code completion inside an expression that is part of a {@code return}
 * statement, the types should be limited by the return type of the current
 * method, and so on.
 *
 * A default SPI implementation is available that will perform some of this
 * analysis assuming it's applied to a parse tree using the other SPI default
 * implementation classes.
 *
 * @todo Rename me to CodeCompletion or CompletionProvider or Completer or something like that
 * @todo Instead of passing in caseSensitive, should I pass in a Comparator which should be used
 *   for determining eligibility? That way it's completely insulated from the clients
 * @todo Pass in completion mode such that I can do different stuff for smart-completion
 *
 * @author Tor Norbye
 */
public interface Completable {
    enum QueryType {
        COMPLETION,
        DOCUMENTATION,
        TOOLTIP,
        ALL_COMPLETION,
        NONE,
        STOP
    }

    List<CompletionProposal> complete(@NonNull CompilationInfo info, int caretOffset, String prefix,
        @NonNull NameKind kind, @NonNull QueryType queryType, boolean caseSensitive, @NonNull HtmlFormatter formatter);

    /**
     *  Return the HTML documentation for the given program element (returned in CompletionProposals
     *  by the complete method)
     */
    String document(@NonNull CompilationInfo info, @NonNull ElementHandle element);
    
    /**
     * Resolve a link that was written into the HTML returned by {@link #document}.
     *
     * @param link The link, which can be in any format chosen by the {@link #document} method.
     *   However, links starting with www or standard URL format (http://, etc.)
     *   will automatically be handled by the browser, so avoid this format.
     * @param originalHandle The handle to the documentation item where the link was generated.
     * @return An ElementHandle that will be passed in to {@link #document} to
     *   compute the new documentation to be warped to.
     */
    @CheckForNull
    ElementHandle resolveLink(@NonNull String link, ElementHandle originalHandle);

    /**
     * Compute the prefix to be used for completion at the given caretOffset
     * @param info The compilation info with parse tree info etc.
     * @param caretOffset The caret offset where completion was requested
     * @param upToOffset If true, provide a prefix only up to the caretOffset. Otherwise,
     *   compute the entire completion symbol under the caret. (The former is used
     *   to bring up a set of completion alternatives, whereas the latter is used
     *   to for example bring up the documentation under the symbol.)
     */
    String getPrefix(@NonNull CompilationInfo info, int caretOffset, boolean upToOffset);

    /**
     * Consider a keystroke and decide whether it should automatically invoke some type
     * of completion. If so, return the desired type, otherwise return QueryType.NONE.
     * @return A QueryType if automatic completion should be initiated, or {@link QueryType.NONE}
     *   if it should be left alon, or {@link QueryType.STOP} if completion should be terminated
     */
    QueryType getAutoQuery(@NonNull JTextComponent component, String typedText);
    
    // TODO: 
    // processKey action stuff from GsfCompletionItem to handle "(", "." etc.
    
    
    
    /**
     * Perform code template parameter evaluation for use in code template completion
     * or editing. The actual set of parameters defined by the language plugins
     * is not defined and will be language specific. Return null if the variable
     * is not known or supported.
     * 
     * @todo This may need a better home than the Code Completion interface;
     *  while templates are used in template code completion it's unrelated to
     *  the regular Ruby code completion.
     */
    String resolveTemplateVariable(String variable, @NonNull CompilationInfo info, int caretOffset, 
            @NonNull String name, Map parameters);
    
    /**
     * Compute the set of applicable templates for a given text selection
     */
    Set<String> getApplicableTemplates(@NonNull CompilationInfo info, int selectionBegin, int selectionEnd);
    
    /**
     * Compute parameter info for the given offset - parameters surrounding the given
     * offset, which particular parameter in that list we're currently on, and so on.
     * @param info The compilation info to pick an AST from
     * @param caretOFfset The caret offset for the completion request
     * @param proposal May be null, but if not, provide the specific completion proposal
     *   that the parameter list is requested for
     */
    ParameterInfo parameters(@NonNull CompilationInfo info, int caretOffset, CompletionProposal proposal);
}
