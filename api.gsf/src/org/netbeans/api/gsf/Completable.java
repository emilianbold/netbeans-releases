/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.gsf;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.CompletionProposal;
import org.netbeans.api.gsf.Element;
import org.netbeans.api.gsf.HtmlFormatter;
import org.netbeans.api.gsf.annotations.NonNull;


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
        ALL_COMPLETION;
    }

    /**
     *  @todo Pass in the completion type? (Smart versus documentation etc.)
     *  @todo Pass in the line offsets? Nah, just make the completion provider figure those out.
     */
    List<CompletionProposal> complete(@NonNull CompilationInfo info, int caretOffset, String prefix,
        @NonNull NameKind kind, @NonNull QueryType queryType, boolean caseSensitive, @NonNull HtmlFormatter formatter);

    /**
     *  Return the HTML documentation for the given program element (returned in CompletionProposals
     *  by the complete method)
     */
    String document(@NonNull CompilationInfo info, @NonNull Element element);

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
