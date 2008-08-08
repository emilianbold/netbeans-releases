/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.CodeCompletionContext;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.CodeCompletionHandler;
import org.netbeans.modules.gsf.api.CodeCompletionResult;
import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.gsf.api.ParameterInfo;
import org.netbeans.modules.php.editor.index.IndexedClass;
import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.index.IndexedElement;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.index.IndexedInterface;
import org.netbeans.modules.php.editor.index.IndexedVariable;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.nav.NavUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.DoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.ExpressionStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.GlobalStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.IfStatement;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;
import org.netbeans.modules.php.editor.parser.astnodes.StaticStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.WhileStatement;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class PHPCodeCompletion implements CodeCompletionHandler {
    private static final Logger LOGGER = Logger.getLogger(PHPCodeCompletion.class.getName());
    private static final List<String> INVALID_PROPOSALS_FOR_CLS_MEMBERS =
            Arrays.asList(new String[] {"__construct","__destruct"});//NOI18N
    
    private static final List<String> CLASS_CONTEXT_KEYWORD_PROPOSAL =
            Arrays.asList(new String[] {"abstract","const","function", "private",
            "protected", "public", "static", "var"});//NOI18N
    private static final List<String> INHERITANCE_KEYWORDS =
            Arrays.asList(new String[] {"extends","implements"});//NOI18N
    private static final List<PHPTokenId[]> NONE_TOKENCHAINS = Arrays.asList(
        new PHPTokenId[]{PHPTokenId.PHP_CLASS},
        new PHPTokenId[]{PHPTokenId.PHP_CLASS, PHPTokenId.WHITESPACE},
         new PHPTokenId[]{PHPTokenId.PHP_EXTENDS, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING, PHPTokenId.WHITESPACE},
         new PHPTokenId[]{PHPTokenId.PHP_IMPLEMENTS, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING, PHPTokenId.WHITESPACE}         
        );
    private static final List<PHPTokenId[]> CLASS_NAME_TOKENCHAINS = Arrays.asList(
        new PHPTokenId[]{PHPTokenId.PHP_NEW},
        new PHPTokenId[]{PHPTokenId.PHP_NEW, PHPTokenId.WHITESPACE},
        new PHPTokenId[]{PHPTokenId.PHP_NEW, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING},
        new PHPTokenId[]{PHPTokenId.PHP_CLASS, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING, PHPTokenId.WHITESPACE, PHPTokenId.PHP_EXTENDS},
        new PHPTokenId[]{PHPTokenId.PHP_CLASS, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING, PHPTokenId.WHITESPACE, PHPTokenId.PHP_EXTENDS, PHPTokenId.WHITESPACE},
        new PHPTokenId[]{PHPTokenId.PHP_CLASS, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING, PHPTokenId.WHITESPACE, PHPTokenId.PHP_EXTENDS, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING}
        );

    private static final List<PHPTokenId[]> INTERFACE_TOKENCHAINS = Arrays.asList(
        new PHPTokenId[]{PHPTokenId.PHP_IMPLEMENTS},
        new PHPTokenId[]{PHPTokenId.PHP_IMPLEMENTS, PHPTokenId.WHITESPACE},
        new PHPTokenId[]{PHPTokenId.PHP_INTERFACE, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING, PHPTokenId.WHITESPACE, PHPTokenId.PHP_EXTENDS},
        new PHPTokenId[]{PHPTokenId.PHP_INTERFACE, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING, PHPTokenId.WHITESPACE, PHPTokenId.PHP_EXTENDS, PHPTokenId.WHITESPACE},
        new PHPTokenId[]{PHPTokenId.PHP_INTERFACE, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING, PHPTokenId.WHITESPACE, PHPTokenId.PHP_EXTENDS, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING}
    );

    private static final List<PHPTokenId[]> TYPE_TOKENCHAINS = Arrays.asList(
        new PHPTokenId[]{PHPTokenId.PHP_FUNCTION, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING, PHPTokenId.PHP_TOKEN},
        new PHPTokenId[]{PHPTokenId.PHP_FUNCTION, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING, PHPTokenId.WHITESPACE, PHPTokenId.PHP_TOKEN}
    );

    private static final List<PHPTokenId[]> CLASS_MEMBER_TOKENCHAINS = Arrays.asList(
        new PHPTokenId[]{PHPTokenId.PHP_OBJECT_OPERATOR},
        new PHPTokenId[]{PHPTokenId.PHP_OBJECT_OPERATOR, PHPTokenId.PHP_STRING},
        new PHPTokenId[]{PHPTokenId.PHP_OBJECT_OPERATOR, PHPTokenId.PHP_VARIABLE},
        new PHPTokenId[]{PHPTokenId.PHP_OBJECT_OPERATOR, PHPTokenId.PHP_TOKEN}
        );

    private static final List<PHPTokenId[]> STATIC_CLASS_MEMBER_TOKENCHAINS = Arrays.asList(
        new PHPTokenId[]{PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM},
        new PHPTokenId[]{PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM, PHPTokenId.PHP_STRING},
        new PHPTokenId[]{PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM, PHPTokenId.PHP_VARIABLE},
        new PHPTokenId[]{PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM, PHPTokenId.PHP_TOKEN}
        );

    private static final List<PHPTokenId[]> COMMENT_TOKENCHAINS = Arrays.asList(
            new PHPTokenId[]{PHPTokenId.PHP_COMMENT_START},
            new PHPTokenId[]{PHPTokenId.PHP_COMMENT},
            new PHPTokenId[]{PHPTokenId.PHP_LINE_COMMENT}
            );

    private static final List<PHPTokenId[]> PHPDOC_TOKENCHAINS = Arrays.asList(
            new PHPTokenId[]{PHPTokenId.PHPDOC_COMMENT_START},
            new PHPTokenId[]{PHPTokenId.PHPDOC_COMMENT}
            );

    private static final List<PHPTokenId[]> INHERITANCE_TOKENCHAINS = Arrays.asList(
            new PHPTokenId[]{PHPTokenId.PHP_CLASS,PHPTokenId.WHITESPACE,PHPTokenId.PHP_STRING, PHPTokenId.WHITESPACE},
            new PHPTokenId[]{PHPTokenId.PHP_CLASS,PHPTokenId.WHITESPACE,PHPTokenId.PHP_STRING, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING},
            new PHPTokenId[]{PHPTokenId.PHP_INTERFACE, PHPTokenId.WHITESPACE,PHPTokenId.PHP_STRING, PHPTokenId.WHITESPACE},
            new PHPTokenId[]{PHPTokenId.PHP_INTERFACE, PHPTokenId.WHITESPACE,PHPTokenId.PHP_STRING, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING}
            );
    private static final List<PHPTokenId[]> INHERITANCE_TOKENCHAINS_CONDITIONAL = Collections.singletonList(
            new PHPTokenId[]{PHPTokenId.PHP_CLASS,PHPTokenId.WHITESPACE,PHPTokenId.PHP_STRING}
            );

    private static final List<PHPTokenId[]> FUNCTION_TOKENCHAINS_CONDITIONAL = Collections.singletonList(
                        new PHPTokenId[]{PHPTokenId.PHP_FUNCTION}
            );

    private static final List<PHPTokenId[]> FUNCTION_TOKENCHAINS = Arrays.asList(
            new PHPTokenId[]{PHPTokenId.PHP_FUNCTION,PHPTokenId.WHITESPACE},
            new PHPTokenId[]{PHPTokenId.PHP_FUNCTION,PHPTokenId.WHITESPACE,PHPTokenId.PHP_STRING}
            );

    private static final List<PHPTokenId[]> CLASS_CONTEXT_KEYWORDS_TOKENCHAINS = Arrays.asList(
            new PHPTokenId[]{PHPTokenId.PHP_PRIVATE},
            new PHPTokenId[]{PHPTokenId.PHP_PRIVATE,PHPTokenId.WHITESPACE},
            new PHPTokenId[]{PHPTokenId.PHP_PRIVATE,PHPTokenId.WHITESPACE,PHPTokenId.PHP_STRING},
            new PHPTokenId[]{PHPTokenId.PHP_PROTECTED},
            new PHPTokenId[]{PHPTokenId.PHP_PROTECTED,PHPTokenId.WHITESPACE},
            new PHPTokenId[]{PHPTokenId.PHP_PROTECTED,PHPTokenId.WHITESPACE,PHPTokenId.PHP_STRING},
            new PHPTokenId[]{PHPTokenId.PHP_PUBLIC},
            new PHPTokenId[]{PHPTokenId.PHP_PUBLIC,PHPTokenId.WHITESPACE},
            new PHPTokenId[]{PHPTokenId.PHP_PUBLIC,PHPTokenId.WHITESPACE,PHPTokenId.PHP_STRING},
            new PHPTokenId[]{PHPTokenId.PHP_STATIC},
            new PHPTokenId[]{PHPTokenId.PHP_STATIC,PHPTokenId.WHITESPACE},
            new PHPTokenId[]{PHPTokenId.PHP_STATIC,PHPTokenId.WHITESPACE,PHPTokenId.PHP_STRING},
            new PHPTokenId[]{PHPTokenId.PHP_ABSTRACT},
            new PHPTokenId[]{PHPTokenId.PHP_ABSTRACT,PHPTokenId.WHITESPACE},
            new PHPTokenId[]{PHPTokenId.PHP_ABSTRACT,PHPTokenId.WHITESPACE,PHPTokenId.PHP_STRING},
            new PHPTokenId[]{PHPTokenId.PHP_CURLY_OPEN},
            new PHPTokenId[]{PHPTokenId.PHP_CURLY_CLOSE},
            new PHPTokenId[]{PHPTokenId.PHP_CURLY_CLOSE,PHPTokenId.WHITESPACE},
            new PHPTokenId[]{PHPTokenId.PHP_CURLY_CLOSE,PHPTokenId.WHITESPACE,PHPTokenId.PHP_STRING},
            new PHPTokenId[]{PHPTokenId.PHP_CURLY_OPEN},
            new PHPTokenId[]{PHPTokenId.PHP_CURLY_OPEN,PHPTokenId.WHITESPACE},
            new PHPTokenId[]{PHPTokenId.PHP_CURLY_OPEN,PHPTokenId.WHITESPACE,PHPTokenId.PHP_STRING},
            new PHPTokenId[]{PHPTokenId.PHP_SEMICOLON},
            new PHPTokenId[]{PHPTokenId.PHP_SEMICOLON,PHPTokenId.WHITESPACE},
            new PHPTokenId[]{PHPTokenId.PHP_SEMICOLON,PHPTokenId.WHITESPACE,PHPTokenId.PHP_STRING}
            );


    static enum CompletionContext {EXPRESSION, HTML, CLASS_NAME, INTERFACE_NAME, TYPE_NAME, STRING,
        CLASS_MEMBER, STATIC_CLASS_MEMBER, PHPDOC, INHERITANCE, METHOD_NAME,
        CLASS_CONTEXT_KEYWORDS, NONE};

    private final static String[] PHP_KEYWORDS = {"__FILE__", "exception",
        "__LINE__", "array()", "class", "const", "continue", "die()", "echo()", "empty()", "endif",
        "eval()", "exit()", "for", "foreach", "function", "global", "if",
        "include()", "include_once()", "isset()", "list()", "new",
        "print()", "require()", "require_once()", "return", "static",
        "switch", "unset()", "use", "var", "while",
        "__FUNCTION__", "__CLASS__", "__METHOD__", "final", "php_user_filter",
        "interface", "implements", "extends", "public", "private",
        "protected", "abstract", "clone", "try", "catch", "throw"
    };

    private final static String[] PHP_CLASS_KEYWORDS = {
        "$this->", "self::", "parent::"
    };

    private final static Collection<Character> AUTOPOPUP_STOP_CHARS = new TreeSet<Character>(
            Arrays.asList(' ', '=', ';', '+', '-', '*', '/', '%'));

    private boolean caseSensitive;
    private NameKind nameKind;

    static CompletionContext findCompletionContext(CompilationInfo info, int caretOffset){
        Document document = info.getDocument();
        if (document == null) {
            return CompletionContext.NONE;
        }
        TokenHierarchy th = TokenHierarchy.get(document);
        TokenSequence<PHPTokenId> tokenSequence = th.tokenSequence();
        tokenSequence.move(caretOffset);
        if (!tokenSequence.moveNext() && !tokenSequence.movePrevious()){
            return CompletionContext.NONE;
        }
        PHPTokenId tokenId = tokenSequence.token().id();
        int tokenIdOffset = tokenSequence.token().offset(th);

        switch (tokenId){
            case T_INLINE_HTML:
                return CompletionContext.HTML;
            case PHP_CONSTANT_ENCAPSED_STRING:
                if (tokenSequence.token().text().charAt(0) == '"') {
                    return CompletionContext.STRING;
                } else {
                    return CompletionContext.NONE;
                }
            default:
        }

        if (acceptTokenChains(tokenSequence, NONE_TOKENCHAINS)){
            return CompletionContext.NONE;
        } else if (acceptTokenChains(tokenSequence, CLASS_NAME_TOKENCHAINS)){
            return CompletionContext.CLASS_NAME;
        } else if (acceptTokenChains(tokenSequence, CLASS_MEMBER_TOKENCHAINS)){
            return CompletionContext.CLASS_MEMBER;
        } else if (acceptTokenChains(tokenSequence, STATIC_CLASS_MEMBER_TOKENCHAINS)){
            return CompletionContext.STATIC_CLASS_MEMBER;
        } else if (acceptTokenChains(tokenSequence, COMMENT_TOKENCHAINS)){
            return CompletionContext.NONE;
        } else if (acceptTokenChains(tokenSequence, PHPDOC_TOKENCHAINS)){
            return CompletionContext.PHPDOC;
        } else if (acceptTokenChains(tokenSequence, INHERITANCE_TOKENCHAINS)){
            return CompletionContext.INHERITANCE;
        } else if (acceptTokenChains(tokenSequence, INHERITANCE_TOKENCHAINS_CONDITIONAL)
                && tokenIdOffset != caretOffset){
            return CompletionContext.INHERITANCE;
        } else if (acceptTokenChains(tokenSequence, INTERFACE_TOKENCHAINS)){
            return CompletionContext.INTERFACE_NAME;
        } else if (acceptTokenChains(tokenSequence, TYPE_TOKENCHAINS)){
            return CompletionContext.TYPE_NAME;
        } else if (isInsideClassDeclarationBlock(info, caretOffset, tokenSequence)) {
            if (acceptTokenChains(tokenSequence, CLASS_CONTEXT_KEYWORDS_TOKENCHAINS)) {
                return CompletionContext.CLASS_CONTEXT_KEYWORDS;
            } else if (acceptTokenChains(tokenSequence, FUNCTION_TOKENCHAINS)) {
                return CompletionContext.METHOD_NAME;
            } else if (acceptTokenChains(tokenSequence, FUNCTION_TOKENCHAINS_CONDITIONAL)
                    && tokenIdOffset != caretOffset) {
                return CompletionContext.METHOD_NAME;
            }
            return CompletionContext.NONE;
        } else if (acceptTokenChains(tokenSequence, FUNCTION_TOKENCHAINS)
                || acceptTokenChains(tokenSequence, FUNCTION_TOKENCHAINS_CONDITIONAL)){
            // ordinary (non-method) function name
            return CompletionContext.NONE;
        }
        return CompletionContext.EXPRESSION;
    }

    private static boolean acceptTokenChains(TokenSequence tokenSequence, List<PHPTokenId[]> tokenIdChains){
        int maxLen = 0;

        for (PHPTokenId tokenIds[] : tokenIdChains){
            if (maxLen < tokenIds.length){
                maxLen = tokenIds.length;
            }
        }

        Token preceedingTokens[] = getPreceedingTokens(tokenSequence, maxLen);

        chain_search:
        for (PHPTokenId tokenIds[] : tokenIdChains){

            int startWithinPrefix = preceedingTokens.length - tokenIds.length;

            if (startWithinPrefix >= 0){
                for (int i = 0; i < tokenIds.length; i ++){
                    if (tokenIds[i] != preceedingTokens[i + startWithinPrefix].id()){
                        continue chain_search;
                    }
                }

                return true;
            }
        }

        return false;
    }

    private static Token[] getPreceedingTokens(TokenSequence tokenSequence, int maxNumberOfTokens){
        int orgOffset = tokenSequence.offset();
        LinkedList<Token> tokens = new LinkedList<Token>();

        for (int i = 0; i < maxNumberOfTokens; i++) {
            if (!tokenSequence.movePrevious()){
                break;
            }

            tokens.addFirst(tokenSequence.token());
        }

        tokenSequence.move(orgOffset);
        tokenSequence.moveNext();
        return tokens.toArray(new Token[tokens.size()]);
    }

    private synchronized static boolean isInsideClassDeclarationBlock(CompilationInfo info,
            int caretOffset, TokenSequence tokenSequence){
        List<ASTNode> nodePath = NavUtils.underCaret(info, caretOffset);
        boolean methDecl = false;
        for (ASTNode aSTNode : nodePath) {
            if (aSTNode instanceof FunctionDeclaration && !methDecl) {
                return false;
            } else if (aSTNode instanceof MethodDeclaration) {
                methDecl = true;
            }
        }
        int orgOffset = tokenSequence.offset();
        try {
            int curly_open = 0;
            int curly_close = 0;
            while (tokenSequence.movePrevious()) {
                Token token = tokenSequence.token();
                TokenId id = token.id();
                if (id.equals(PHPTokenId.PHP_CURLY_OPEN)) {
                    curly_open++;
                } else if (id.equals(PHPTokenId.PHP_CURLY_CLOSE)) {
                    curly_close++;
                } else if ((id.equals(PHPTokenId.PHP_FUNCTION) ||
                        id.equals(PHPTokenId.PHP_WHILE) || id.equals(PHPTokenId.PHP_IF)) 
                        && (curly_open > curly_close)) {
                    return false;
                } else if (id.equals(PHPTokenId.PHP_CLASS)) {
                    boolean isClassScope = curly_open > 0 && (curly_open + curly_close) % 2 == 1;
                    return isClassScope;
                }
            }
        } finally {
            tokenSequence.move(orgOffset);
            tokenSequence.moveNext();
        }
        return false;
    }

    public CodeCompletionResult complete(CodeCompletionContext completionContext) {
        long startTime = 0;

        if (LOGGER.isLoggable(Level.FINE)){
            startTime = System.currentTimeMillis();
        }

        CompilationInfo info = completionContext.getInfo();
        int caretOffset = completionContext.getCaretOffset();
        String prefix = completionContext.getPrefix();
        this.caseSensitive = completionContext.isCaseSensitive();
        this.nameKind = caseSensitive ? NameKind.PREFIX : NameKind.CASE_INSENSITIVE_PREFIX;

        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();

        PHPParseResult result = (PHPParseResult) info.getEmbeddedResult(PHPLanguage.PHP_MIME_TYPE, caretOffset);

        if (result.getProgram() == null){
            return CodeCompletionResult.NONE;
        }

        CompletionContext context = findCompletionContext(info, caretOffset);
        LOGGER.fine("CC context: " + context);

        if (context == CompletionContext.NONE){
            return CodeCompletionResult.NONE;
        }

        PHPCompletionItem.CompletionRequest request = new PHPCompletionItem.CompletionRequest();
        request.anchor = caretOffset - prefix.length();
        request.result = result;
        request.info = info;
        request.prefix = prefix;
        request.index = PHPIndex.get(request.info.getIndex(PHPLanguage.PHP_MIME_TYPE));

        try {
            request.currentlyEditedFileURL = result.getFile().getFileObject().getURL().toString();
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }


        switch(context){
            case EXPRESSION:
                autoCompleteExpression(proposals, request);
                break;
            case HTML:
                proposals.add(new PHPCompletionItem.KeywordItem("<?php", request)); //NOI18N
                proposals.add(new PHPCompletionItem.KeywordItem("<?=", request)); //NOI18N
                break;
            case CLASS_NAME:
                autoCompleteClassNames(proposals, request);
                break;
            case INTERFACE_NAME:
                autoCompleteInterfaceNames(proposals, request);
                break;
            case TYPE_NAME:
                autoCompleteClassNames(proposals, request);
                autoCompleteInterfaceNames(proposals, request);
                break;
            case STRING:
                // LOCAL VARIABLES
                proposals.addAll(getVariableProposals(request.result.getProgram().getStatements(), request));
                break;
            case CLASS_MEMBER:
                autoCompleteClassMembers(proposals, request, false);
                break;
            case STATIC_CLASS_MEMBER:
                autoCompleteClassMembers(proposals, request, true);
                break;
            case PHPDOC:
                PHPDOCCodeCompletion.complete(proposals, request);
                break;
            case CLASS_CONTEXT_KEYWORDS:
                autoCompleteKeywords(proposals, request, CLASS_CONTEXT_KEYWORD_PROPOSAL);
                break;
            /*case ACCESS_MODIFIER:
                autoCompleteKeywords(proposals, request, AFTER_ACCESS_MODIFIER_KEYWORD_PROPOSAL);
                break;
             */
            case METHOD_NAME:
                autoCompleteMethodName(proposals, request);
                break;
            case INHERITANCE:
                autoCompleteKeywords(proposals, request, INHERITANCE_KEYWORDS);
                break;
        }

        if (LOGGER.isLoggable(Level.FINE)){
            long time = System.currentTimeMillis() - startTime;
            LOGGER.fine(String.format("complete() took %d ms, result contains %d items", time, proposals.size()));
        }

        return new PHPCompletionResult(completionContext, proposals);
    }

    private void autoCompleteClassNames(List<CompletionProposal> proposals, PHPCompletionItem.CompletionRequest request) {
        for (IndexedClass clazz : request.index.getClasses(request.result, request.prefix, nameKind)) {
            proposals.add(new PHPCompletionItem.ClassItem(clazz, request));
        }
    }

    private void autoCompleteInterfaceNames(List<CompletionProposal> proposals, PHPCompletionItem.CompletionRequest request) {
        for (IndexedInterface iface : request.index.getInterfaces(request.result, request.prefix, nameKind)) {
            proposals.add(new PHPCompletionItem.InterfaceItem(iface, request));
        }
    }

    private void autoCompleteMethodName(List<CompletionProposal> proposals,
            PHPCompletionItem.CompletionRequest request) {
        for (String keyword : PredefinedSymbols.MAGIC_METHODS) {
            if (keyword.startsWith(request.prefix)) {
                proposals.add(new PHPCompletionItem.SpecialFunctionItem(keyword, request));
            }
        }
    //autoCompleteKeywords(proposals, request, METHOD_NAME_PROPOSALS);
    }
    private void autoCompleteKeywords(List<CompletionProposal> proposals,
            PHPCompletionItem.CompletionRequest request, List<String> keywordList) {
        for (String keyword : keywordList) {
            if (keyword.startsWith(request.prefix)) {
                proposals.add(new PHPCompletionItem.KeywordItem(keyword, request));
            }
        }

    }
    private void autoCompleteClassMembers(List<CompletionProposal> proposals,
            PHPCompletionItem.CompletionRequest request, boolean staticContext) {
        Document document = request.info.getDocument();
        if (document == null) {
            return;
        }

        TokenHierarchy th = TokenHierarchy.get(document);
        TokenSequence<PHPTokenId> tokenSequence = th.tokenSequence();
        tokenSequence.move(request.anchor);
        if (tokenSequence.movePrevious())
        {
            boolean instanceContext = !staticContext;
            boolean includeInherited = true;
            boolean moreTokens = true;
            int attrMask = Modifier.PUBLIC;

            if (tokenSequence.token().id() == PHPTokenId.WHITESPACE) {
                moreTokens = tokenSequence.movePrevious();
            }

            moreTokens = tokenSequence.movePrevious();

            String varName = tokenSequence.token().text().toString();
            String typeName = null;
            List<String> invalidProposalsForClsMembers = INVALID_PROPOSALS_FOR_CLS_MEMBERS;
            if (varName.equals("self")) { //NOI18N
                ClassDeclaration classDecl = findEnclosingClass(request.info, request.anchor);
                if (classDecl != null) {
                    typeName = classDecl.getName().getName();
                    staticContext = instanceContext = true;
                    includeInherited = false;
                    attrMask |= (Modifier.PROTECTED | Modifier.PRIVATE);
                }
            } else if (varName.equals("parent")) { //NOI18N
                invalidProposalsForClsMembers = Collections.emptyList();
                ClassDeclaration classDecl = findEnclosingClass(request.info, request.anchor);
                if (classDecl != null) {
                    Identifier superIdentifier = classDecl.getSuperClass();
                    if (superIdentifier != null) {
                        typeName = superIdentifier.getName();
                        staticContext = instanceContext = true;
                        attrMask |= Modifier.PROTECTED;
                    }
                }
            } else if (varName.equals("$this")) { //NOI18N
                ClassDeclaration classDecl = findEnclosingClass(request.info, request.anchor);
                if (classDecl != null) {
                    typeName = classDecl.getName().getName();
                    staticContext = false;
                    instanceContext = true;
                    attrMask |= (Modifier.PROTECTED | Modifier.PRIVATE);
                }
            } else {
                if (staticContext) {
                    typeName = varName;
                } else {
                    Collection<IndexedConstant> vars = getVariables(request.result, request.index,
                            request.result.getProgram().getStatements(),
                            varName, request.anchor, request.currentlyEditedFileURL);

                    if (vars != null) {
                        for (IndexedConstant var : vars){
                            if (var.getName().equals(varName)){ // can be just a prefix
                                typeName = var.getTypeName();
                                break;
                            }
                        }
                    }
                }
            }

            if (typeName != null){
                Collection<IndexedFunction> methods = includeInherited ?
                    request.index.getAllMethods(request.result, typeName, request.prefix, nameKind, attrMask) :
                    request.index.getMethods(request.result, typeName, request.prefix, nameKind, attrMask);

                for (IndexedFunction method : methods){
                    if (staticContext && method.isStatic() || instanceContext && !method.isStatic()) {
                        for (int i = 0; i <= method.getOptionalArgs().length; i ++){
                            if (!invalidProposalsForClsMembers.contains(method.getName())) {
                                proposals.add(new PHPCompletionItem.FunctionItem(method, request, i));
                            }
                        }
                    }
                }

                String prefix = (staticContext && request.prefix.startsWith("$")) //NOI18N
                        ? request.prefix.substring(1) : request.prefix;
                Collection<IndexedConstant> properties = includeInherited ?
                    request.index.getAllProperties(request.result, typeName, prefix, nameKind, attrMask) :
                    request.index.getProperties(request.result, typeName, prefix, nameKind, attrMask);

                for (IndexedConstant prop : properties){
                    if (staticContext && prop.isStatic() || instanceContext && !prop.isStatic()) {
                        PHPCompletionItem.VariableItem item = new PHPCompletionItem.VariableItem(prop, request);

                        if (!staticContext) {
                            item.doNotInsertDollarPrefix();
                        }

                        proposals.add(item);
                    }
                }

                if (staticContext) {
                    Collection<IndexedConstant> classConstants = request.index.getAllClassConstants(
                            request.result, typeName, request.prefix, nameKind);
                    for (IndexedConstant constant : classConstants) {
                        proposals.add(new PHPCompletionItem.VariableItem(constant, request));
                    }
                }
            }
        }
    }

    private static ClassDeclaration findEnclosingClass(CompilationInfo info, int offset) {
        List<ASTNode> nodes = NavUtils.underCaret(info, offset);
        for(ASTNode node : nodes) {
            if (node instanceof ClassDeclaration) {
                return (ClassDeclaration) node;
            }
        }
        return null;
    }

    private void autoCompleteExpression(List<CompletionProposal> proposals, PHPCompletionItem.CompletionRequest request) {
        // KEYWORDS
        for (String keyword : PHP_KEYWORDS) {
            if (startsWith(keyword, request.prefix)) {
                proposals.add(new PHPCompletionItem.KeywordItem(keyword, request));
            }
        }

        PHPIndex index = request.index;
        if (request.prefix.length() == 0) {
            Collection<IndexedConstant> localVars = getLocalVariables(request.result.getProgram().getStatements(), request.prefix, request.anchor, request.currentlyEditedFileURL);
            Map<String, IndexedConstant> allVars = new LinkedHashMap<String, IndexedConstant>();
            
            for (IndexedConstant var : localVars){
                allVars.put(var.getName(), var);
            }
            
            for (IndexedElement element : index.getAll(request.result, request.prefix, nameKind)) {
                if (element instanceof IndexedFunction) {
                    IndexedFunction function = (IndexedFunction) element;
                    for (int i = 0; i <= function.getOptionalArgs().length; i++) {
                        proposals.add(new PHPCompletionItem.FunctionItem(function, request, i));
                    }
                }
                else if (element instanceof IndexedConstant) {
                    proposals.add(new PHPCompletionItem.ConstantItem((IndexedConstant)element, request));
                }
                else if (element instanceof IndexedClass) {
                    proposals.add(new PHPCompletionItem.ClassItem((IndexedClass)element, request));
                }
                else if (element instanceof IndexedVariable) {
                    IndexedConstant topLevelVar = (IndexedConstant) element;
                    if (!request.currentlyEditedFileURL.equals(topLevelVar.getFilenameUrl())){
                        IndexedConstant localVar = allVars.get(topLevelVar.getName());
                        if (localVar == null || localVar.getOffset() != topLevelVar.getOffset()) {
                            IndexedConstant original = allVars.put(topLevelVar.getName(), topLevelVar);
                            if (original != null && localVars.contains(original)) {
                                allVars.put(original.getName(), original);
                            }
                        }
                    }
                }
            }
            
            for (IndexedConstant var : allVars.values()){
                CodeUtils.resolveFunctionType(request.result, index, allVars, var);
                proposals.add(new PHPCompletionItem.VariableItem(var, request));
            }
        }
        else {
            // FUNCTIONS
            for (IndexedFunction function : index.getFunctions(request.result, request.prefix, nameKind)) {
                for (int i = 0; i <= function.getOptionalArgs().length; i++) {
                    proposals.add(new PHPCompletionItem.FunctionItem(function, request, i));
                }
            }

            // CONSTANTS
            for (IndexedConstant constant : index.getConstants(request.result, request.prefix, nameKind)) {
                proposals.add(new PHPCompletionItem.ConstantItem(constant, request));
            }
            
            // CLASS NAMES
            // TODO only show classes with static elements
            autoCompleteClassNames(proposals, request);
            
            // LOCAL VARIABLES
            proposals.addAll(getVariableProposals(request.result.getProgram().getStatements(), request));
        }

        // Special keywords applicable only inside a class
        ClassDeclaration classDecl = findEnclosingClass(request.info, request.anchor);
        if (classDecl != null) {
            for (String keyword : PHP_CLASS_KEYWORDS) {
                if (startsWith(keyword, request.prefix)) {
                    proposals.add(new PHPCompletionItem.KeywordItem(keyword, request));
                }
            }
        }
    }

    private Collection<CompletionProposal> getVariableProposals(Collection<Statement> statementList,
            PHPCompletionItem.CompletionRequest request){

        Collection<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
        Collection<IndexedConstant> allVars = getVariables(request.result, request.index,
                statementList, request.prefix, request.anchor, request.currentlyEditedFileURL);

        for (IndexedConstant localVar : allVars){
            CompletionProposal proposal = new PHPCompletionItem.VariableItem(localVar, request);
            proposals.add(proposal);
        }

        for (String name : PredefinedSymbols.SUPERGLOBALS){
            if (isPrefix("$" + name, request.prefix)) { //NOI18N
                CompletionProposal proposal = new PHPCompletionItem.SuperGlobalItem(request, name);
                proposals.add(proposal);
            }
        }

        return proposals;
    }

    public Collection<IndexedConstant> getVariables(PHPParseResult context,  PHPIndex index, Collection<Statement> statementList,
            String namePrefix, int position, String localFileURL){
        Collection<IndexedConstant> localVars = getLocalVariables(statementList, namePrefix, position, localFileURL);
        Map<String, IndexedConstant> allVars = new LinkedHashMap<String, IndexedConstant>();

        for (IndexedConstant var : localVars){
            allVars.put(var.getName(), var);
        }

        for (IndexedConstant topLevelVar : index.getTopLevelVariables(context, namePrefix, NameKind.PREFIX)){
            if (!localFileURL.equals(topLevelVar.getFilenameUrl())){
                IndexedConstant localVar = allVars.get(topLevelVar.getName());

                 if (localVar == null || localVar.getOffset() != topLevelVar.getOffset()){
                    IndexedConstant original = allVars.put(topLevelVar.getName(), topLevelVar);
                    if (original != null && localVars.contains(original)) {
                        allVars.put(original.getName(), original);
                    }
                 }
            }
        }

        for (IndexedConstant var : allVars.values()){
            CodeUtils.resolveFunctionType(context, index, allVars, var);
        }

        return allVars.values();
    }

    private void getLocalVariables_indexVariable(Variable var,
            Map<String, IndexedConstant> localVars,
            String namePrefix, String localFileURL, String type) {

        String varName = CodeUtils.extractVariableName(var);

        if (isPrefix(varName, namePrefix)) {
            IndexedConstant ic = new IndexedConstant(varName, null,
                    null, localFileURL, var.getStartOffset(), 0, type);

            localVars.put(varName, ic);
        }
    }

    private boolean isPrefix(String name, String prefix){
        return name != null && (name.startsWith(prefix)
                || nameKind == NameKind.CASE_INSENSITIVE_PREFIX && name.toLowerCase().startsWith(prefix.toLowerCase()));
    }

    private void getLocalVariables_indexVariableInAssignment(Expression expr,
            Map<String, IndexedConstant> localVars,
            String namePrefix, String localFileURL) {

        if (expr instanceof Assignment) {
            Assignment assignment = (Assignment) expr;

            if (assignment.getLeftHandSide() instanceof Variable) {
                Variable variable = (Variable) assignment.getLeftHandSide();
                String varType = CodeUtils.extractVariableTypeFromAssignment(assignment);

                getLocalVariables_indexVariable(variable, localVars, namePrefix,
                        localFileURL, varType);
            }

            if (assignment.getRightHandSide() instanceof Assignment){
                getLocalVariables_indexVariableInAssignment(assignment.getRightHandSide(),
                        localVars, namePrefix, localFileURL);
            }
        }
    }

    private Collection<IndexedConstant> getLocalVariables(Collection<Statement> statementList, String namePrefix, int position, String localFileURL){
        Map<String, IndexedConstant> localVars = new HashMap<String, IndexedConstant>();

        for (Statement statement : statementList){
            if (statement.getStartOffset() > position){
                break; // no need to analyze statements after caret offset
            }

            if (statement instanceof ExpressionStatement){
                Expression expr = ((ExpressionStatement)statement).getExpression();
                getLocalVariables_indexVariableInAssignment(expr, localVars, namePrefix, localFileURL);

            } else if (statement instanceof GlobalStatement) {
                GlobalStatement globalStatement = (GlobalStatement) statement;

                for (Variable var : globalStatement.getVariables()){
                    getLocalVariables_indexVariable(var, localVars, namePrefix, localFileURL, null);
                }
            } else if (statement instanceof StaticStatement) {
                StaticStatement staticStatement = (StaticStatement) statement;

                for (Variable var : staticStatement.getVariables()){
                    getLocalVariables_indexVariable(var, localVars, namePrefix, localFileURL, null);
                }
            }
            else if (!offsetWithinStatement(position, statement)){
                continue;
            }

            if (statement instanceof Block) {
                Block block = (Block) statement;

                getLocalVariables_MergeResults(localVars,
                        getLocalVariables(block.getStatements(), namePrefix, position, localFileURL));

            } else if (statement instanceof IfStatement){
                IfStatement ifStmt = (IfStatement)statement;
                getLocalVariables_indexVariableInAssignment(ifStmt.getCondition(), localVars, namePrefix, localFileURL);

                if (offsetWithinStatement(position, ifStmt.getTrueStatement())) {
                    getLocalVariables_MergeResults(localVars,
                            getLocalVariables(Collections.singleton(ifStmt.getTrueStatement()), namePrefix, position, localFileURL));

                } else if (ifStmt.getFalseStatement() != null // false statement ('else') is optional
                        && offsetWithinStatement(position, ifStmt.getFalseStatement())) {

                    getLocalVariables_MergeResults(localVars,
                            getLocalVariables(Collections.singleton(ifStmt.getFalseStatement()), namePrefix, position, localFileURL));
                }
            } else if (statement instanceof WhileStatement) {
                WhileStatement whileStatement = (WhileStatement) statement;
                getLocalVariables_indexVariableInAssignment(whileStatement.getCondition(), localVars, namePrefix, localFileURL);

                getLocalVariables_MergeResults(localVars,
                        getLocalVariables(Collections.singleton(whileStatement.getBody()), namePrefix, position, localFileURL));
            }  else if (statement instanceof DoStatement) {
                DoStatement doStatement = (DoStatement) statement;

                getLocalVariables_MergeResults(localVars,
                        getLocalVariables(Collections.singleton(doStatement.getBody()), namePrefix, position, localFileURL));
            } else if (statement instanceof ForStatement) {
                ForStatement forStatement = (ForStatement) statement;

                for (Expression expr : forStatement.getInitializers()){
                    getLocalVariables_indexVariableInAssignment(expr, localVars, namePrefix, localFileURL);
                }

                getLocalVariables_MergeResults(localVars,
                        getLocalVariables(Collections.singleton(forStatement.getBody()), namePrefix, position, localFileURL));
            } else if (statement instanceof ForEachStatement) {
                ForEachStatement forEachStatement = (ForEachStatement) statement;

                if (forEachStatement.getKey() instanceof Variable) {
                    Variable var = (Variable) forEachStatement.getKey();
                    getLocalVariables_indexVariable(var, localVars, namePrefix, localFileURL, null);
                }

                if (forEachStatement.getValue() instanceof Variable) {
                    Variable var = (Variable) forEachStatement.getValue();
                    getLocalVariables_indexVariable(var, localVars, namePrefix, localFileURL, null);
                }

                getLocalVariables_indexVariableInAssignment(forEachStatement.getValue(), localVars, namePrefix, localFileURL);

                getLocalVariables_MergeResults(localVars,
                        getLocalVariables(Collections.singleton(forEachStatement.getStatement()), namePrefix, position, localFileURL));
            } else if (statement instanceof FunctionDeclaration) {
                localVars.clear();
                FunctionDeclaration functionDeclaration = (FunctionDeclaration) statement;

                for (FormalParameter param : functionDeclaration.getFormalParameters()) {
                    Expression parameterName = param.getParameterName();
                    if (parameterName instanceof Reference) {
                        Reference ref = (Reference) parameterName;
                        parameterName = ref.getExpression();
                    }
                    if (parameterName instanceof Variable) {
                        String varName = CodeUtils.extractVariableName((Variable) parameterName);
                        String type = param.getParameterType() != null ? param.getParameterType().getName() : null;

                        if (isPrefix(varName, namePrefix)) {
                            IndexedConstant ic = new IndexedConstant(varName, null,
                                    null, localFileURL, -1, 0, type);

                            localVars.put(varName, ic);
                        }
                    }
                }

                getLocalVariables_MergeResults(localVars,
                            getLocalVariables(Collections.singleton((Statement)functionDeclaration.getBody()), namePrefix, position, localFileURL));

            } if (statement instanceof MethodDeclaration) {
                MethodDeclaration methodDeclaration = (MethodDeclaration) statement;

                getLocalVariables_MergeResults(localVars,
                    getLocalVariables(Collections.singleton((Statement)methodDeclaration.getFunction()), namePrefix, position, localFileURL));

            } else if (statement instanceof ClassDeclaration) {
                ClassDeclaration classDeclaration = (ClassDeclaration) statement;

                getLocalVariables_MergeResults(localVars,
                    getLocalVariables(Collections.singleton((Statement)classDeclaration.getBody()), namePrefix, position, localFileURL));
            }

        }



        return localVars.values();
    }

    private void getLocalVariables_MergeResults(Map<String, IndexedConstant> existingMap, Collection<IndexedConstant> newValues){
        for (IndexedConstant var : newValues){
            existingMap.put(var.getName(), var);
        }
    }

    private static boolean offsetWithinStatement(int offset, Statement statement){
        return statement.getEndOffset() >= offset && statement.getStartOffset() <= offset;
    }

    public String document(CompilationInfo info, ElementHandle element) {
        return DocRenderer.document(info, element);
    }

    public ElementHandle resolveLink(String link, ElementHandle originalHandle) {
        return null;
    }

    private static final boolean isPHPIdentifierPart(char c){
        return Character.isJavaIdentifierPart(c) || c == '@';
    }

    public String getPrefix(CompilationInfo info, int caretOffset, boolean upToOffset) {
        try {
            BaseDocument doc = (BaseDocument) info.getDocument();
            if (doc == null) {
                return null;
            }

           // TokenHierarchy<Document> th = TokenHierarchy.get((Document) doc);
            doc.readLock(); // Read-lock due to token hierarchy use

            try {
                int lineBegin = Utilities.getRowStart(doc, caretOffset);
                if (lineBegin != -1) {
                    int lineEnd = Utilities.getRowEnd(doc, caretOffset);
                    String line = doc.getText(lineBegin, lineEnd - lineBegin);
                    int lineOffset = caretOffset - lineBegin;
                    int start = lineOffset;
                    if (lineOffset > 0) {
                        for (int i = lineOffset - 1; i >= 0; i--) {
                            char c = line.charAt(i);
                            if (!isPHPIdentifierPart(c)) {
                                break;
                            } else {
                                start = i;
                            }
                        }
                    }

                    // Find identifier end
                    String prefix;
                    if (upToOffset) {
                        prefix = line.substring(start, lineOffset);
                        int lastIndexOfDollar = prefix.lastIndexOf('$');//NOI18N
                        if (lastIndexOfDollar > 0) {
                            prefix = prefix.substring(lastIndexOfDollar);
                        }
                    } else {
                        if (lineOffset == line.length()) {
                            prefix = line.substring(start);
                        } else {
                            int n = line.length();
                            int end = lineOffset;
                            for (int j = lineOffset; j < n; j++) {
                                char d = line.charAt(j);
                                // Try to accept Foo::Bar as well
                                if (!isPHPIdentifierPart(d)) {
                                    break;
                                } else {
                                    end = j + 1;
                                }
                            }
                            prefix = line.substring(start, end);
                        }
                    }

                    if (prefix.length() > 0) {
                        if (prefix.endsWith("::")) {
                            return "";
                        }

                        if (prefix.endsWith(":") && prefix.length() > 1) {
                            return null;
                        }

                        // Strip out LHS if it's a qualified method, e.g.  Benchmark::measure -> measure
                        int q = prefix.lastIndexOf("::");

                        if (q != -1) {
                            prefix = prefix.substring(q + 2);
                        }

                        // The identifier chars identified by JsLanguage are a bit too permissive;
                        // they include things like "=", "!" and even "&" such that double-clicks will
                        // pick up the whole "token" the user is after. But "=" is only allowed at the
                        // end of identifiers for example.
                        if (prefix.length() == 1) {
                            char c = prefix.charAt(0);
                            if (!(isPHPIdentifierPart(c) || c == '@' || c == '$' || c == ':')) {
                                return null;
                            }
                        } else {
                            for (int i = prefix.length() - 2; i >= 0; i--) { // -2: the last position (-1) can legally be =, ! or ?

                                char c = prefix.charAt(i);
                                if (i == 0 && c == ':') {
                                    // : is okay at the begining of prefixes
                                } else if (!(isPHPIdentifierPart(c) || c == '@' || c == '$')) {
                                    prefix = prefix.substring(i + 1);
                                    break;
                                }
                            }
                        }
                    }
                    return prefix;
                }
            } finally {
                doc.readUnlock();
            }
        // Else: normal identifier: just return null and let the machinery do the rest
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }

        return null;
    }

    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        if(typedText.length() == 0) {
            return QueryType.NONE;
        }
        char lastChar = typedText.charAt(typedText.length() - 1);

        if (AUTOPOPUP_STOP_CHARS.contains(Character.valueOf(lastChar))){
            return QueryType.STOP;
        }

        Document document = component.getDocument();
        TokenHierarchy th = TokenHierarchy.get(document);
        TokenSequence<PHPTokenId> ts = th.tokenSequence(PHPTokenId.language());
        int offset = component.getCaretPosition();
        int diff = ts.move(offset);
        if(diff > 0 && ts.moveNext() || ts.movePrevious()) {
            Token t = ts.token();
            if(t.id() == PHPTokenId.PHP_OBJECT_OPERATOR
                    || t.id() == PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM
                    || t.id() == PHPTokenId.PHP_TOKEN && lastChar == '$'
                    || t.id() == PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING && lastChar == '$'
                    || t.id() == PHPTokenId.PHPDOC_COMMENT && lastChar == '@') {
                return QueryType.ALL_COMPLETION;
                // magic methods
            } else if (lastChar == '_' && acceptTokenChains(ts, FUNCTION_TOKENCHAINS)) {
                return QueryType.ALL_COMPLETION;
            }

        }
        return QueryType.NONE;
    }



    public String resolveTemplateVariable(String variable, CompilationInfo info, int caretOffset, String name, Map parameters) {
        return null;
    }

    public Set<String> getApplicableTemplates(CompilationInfo info, int selectionBegin, int selectionEnd) {
        return null;
    }

    public ParameterInfo parameters(CompilationInfo info, int caretOffset, CompletionProposal proposal) {
        //TODO: return the info for functions and methods
        return ParameterInfo.NONE;
    }

    private boolean startsWith(String theString, String prefix) {
        if (prefix.length() == 0) {
            return true;
        }

        return caseSensitive ? theString.startsWith(prefix)
                : theString.toLowerCase().startsWith(prefix.toLowerCase());
    }
}
