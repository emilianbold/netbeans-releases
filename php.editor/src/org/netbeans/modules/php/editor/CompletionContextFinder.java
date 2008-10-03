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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.annotations.CheckForNull;
import org.netbeans.modules.gsf.api.annotations.NonNull;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.nav.NavUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTError;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
class CompletionContextFinder {
    private static final List<PHPTokenId[]> CLASS_NAME_TOKENCHAINS = Arrays.asList(
            new PHPTokenId[]{PHPTokenId.PHP_NEW},
            new PHPTokenId[]{PHPTokenId.PHP_NEW, PHPTokenId.WHITESPACE},
            new PHPTokenId[]{PHPTokenId.PHP_NEW, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING
    });

    private static final List<PHPTokenId[]> TYPE_TOKENCHAINS = Arrays.asList(
        new PHPTokenId[]{PHPTokenId.PHP_CATCH, PHPTokenId.PHP_TOKEN},
        new PHPTokenId[]{PHPTokenId.PHP_CATCH, PHPTokenId.WHITESPACE, PHPTokenId.PHP_TOKEN},
        new PHPTokenId[]{PHPTokenId.PHP_CATCH, PHPTokenId.WHITESPACE, PHPTokenId.PHP_TOKEN, PHPTokenId.WHITESPACE},
        new PHPTokenId[]{PHPTokenId.PHP_CATCH, PHPTokenId.WHITESPACE, PHPTokenId.PHP_TOKEN, PHPTokenId.PHP_STRING},
        new PHPTokenId[]{PHPTokenId.PHP_INSTANCEOF, PHPTokenId.WHITESPACE},
        new PHPTokenId[]{PHPTokenId.PHP_INSTANCEOF, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING},
        new PHPTokenId[]{PHPTokenId.PHP_FUNCTION, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING, PHPTokenId.PHP_TOKEN},
        new PHPTokenId[]{PHPTokenId.PHP_FUNCTION, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING, PHPTokenId.WHITESPACE, PHPTokenId.PHP_TOKEN}
    );

    private static final List<PHPTokenId[]> CLASS_MEMBER_TOKENCHAINS = Arrays.asList(
        new PHPTokenId[]{PHPTokenId.PHP_OBJECT_OPERATOR},
        new PHPTokenId[]{PHPTokenId.PHP_OBJECT_OPERATOR, PHPTokenId.PHP_STRING},
        new PHPTokenId[]{PHPTokenId.PHP_OBJECT_OPERATOR, PHPTokenId.PHP_VARIABLE},
        new PHPTokenId[]{PHPTokenId.PHP_OBJECT_OPERATOR, PHPTokenId.PHP_TOKEN},
        new PHPTokenId[]{PHPTokenId.PHP_OBJECT_OPERATOR, PHPTokenId.WHITESPACE},
        new PHPTokenId[]{PHPTokenId.PHP_OBJECT_OPERATOR, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING},
        new PHPTokenId[]{PHPTokenId.PHP_OBJECT_OPERATOR, PHPTokenId.WHITESPACE, PHPTokenId.PHP_VARIABLE},
        new PHPTokenId[]{PHPTokenId.PHP_OBJECT_OPERATOR, PHPTokenId.WHITESPACE, PHPTokenId.PHP_TOKEN}
        );

    private static final List<PHPTokenId[]> STATIC_CLASS_MEMBER_TOKENCHAINS = Arrays.asList(
        new PHPTokenId[]{PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM},
        new PHPTokenId[]{PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM, PHPTokenId.PHP_STRING},
        new PHPTokenId[]{PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM, PHPTokenId.PHP_VARIABLE},
        new PHPTokenId[]{PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM, PHPTokenId.PHP_TOKEN},
        new PHPTokenId[]{PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM, PHPTokenId.WHITESPACE},
        new PHPTokenId[]{PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING},
        new PHPTokenId[]{PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM, PHPTokenId.WHITESPACE, PHPTokenId.PHP_VARIABLE},
        new PHPTokenId[]{PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM, PHPTokenId.WHITESPACE, PHPTokenId.PHP_TOKEN}
        );

    private static final PHPTokenId[] COMMENT_TOKENS = new PHPTokenId[]{
        PHPTokenId.PHP_COMMENT_START, PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_LINE_COMMENT, PHPTokenId.PHP_COMMENT_END};

    private static final List<PHPTokenId[]> PHPDOC_TOKENCHAINS = Arrays.asList(
            new PHPTokenId[]{PHPTokenId.PHPDOC_COMMENT_START},
            new PHPTokenId[]{PHPTokenId.PHPDOC_COMMENT}
            );

    private static final List<PHPTokenId[]> FUNCTION_TOKENCHAINS = Arrays.asList(
            new PHPTokenId[]{PHPTokenId.PHP_FUNCTION},
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
            new PHPTokenId[]{PHPTokenId.PHP_FINAL},
            new PHPTokenId[]{PHPTokenId.PHP_FINAL,PHPTokenId.WHITESPACE},
            new PHPTokenId[]{PHPTokenId.PHP_FINAL,PHPTokenId.WHITESPACE,PHPTokenId.PHP_STRING},
            new PHPTokenId[]{PHPTokenId.PHP_CURLY_OPEN},
            new PHPTokenId[]{PHPTokenId.PHP_LINE_COMMENT},
            new PHPTokenId[]{PHPTokenId.PHP_LINE_COMMENT,PHPTokenId.WHITESPACE},
            new PHPTokenId[]{PHPTokenId.PHP_LINE_COMMENT, PHPTokenId.WHITESPACE,PHPTokenId.PHP_STRING},
            new PHPTokenId[]{PHPTokenId.PHP_COMMENT_END},
            new PHPTokenId[]{PHPTokenId.PHP_COMMENT_END, PHPTokenId.WHITESPACE},
            new PHPTokenId[]{PHPTokenId.PHP_COMMENT_END,PHPTokenId.WHITESPACE,PHPTokenId.PHP_STRING},
            new PHPTokenId[]{PHPTokenId.PHP_COMMENT_END,PHPTokenId.PHP_STRING},
            new PHPTokenId[]{PHPTokenId.PHPDOC_COMMENT_END},
            new PHPTokenId[]{PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.WHITESPACE},
            new PHPTokenId[]{PHPTokenId.PHPDOC_COMMENT_END,PHPTokenId.WHITESPACE,PHPTokenId.PHP_STRING},
            new PHPTokenId[]{PHPTokenId.PHPDOC_COMMENT_END,PHPTokenId.PHP_STRING},
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


       private static final List<PHPTokenId[]> SERVER_ARRAY_TOKENCHAINS = Collections.singletonList(
            new PHPTokenId[]{PHPTokenId.PHP_VARIABLE, PHPTokenId.PHP_TOKEN});

       private static final List<String> SERVER_ARRAY_TOKENTEXTS =
               Arrays.asList(new String[] {"$_SERVER","["});//NOI18N


    static enum CompletionContext {EXPRESSION, HTML, CLASS_NAME, INTERFACE_NAME, TYPE_NAME, STRING,
        CLASS_MEMBER, STATIC_CLASS_MEMBER, PHPDOC, INHERITANCE, EXTENDS, IMPLEMENTS, METHOD_NAME,
        CLASS_CONTEXT_KEYWORDS, SERVER_ENTRY_CONSTANTS, NONE};

    static enum KeywordCompletionType {SIMPLE, CURSOR_INSIDE_BRACKETS, ENDS_WITH_CURLY_BRACKETS,
    ENDS_WITH_SPACE, ENDS_WITH_SEMICOLON, ENDS_WITH_COLON};
    
        @NonNull
    static CompletionContext findCompletionContext(CompilationInfo info, int caretOffset){
       Document document = info.getDocument();
        if (document == null) {
            return CompletionContext.NONE;
        }

        TokenSequence<PHPTokenId> tokenSequence = LexUtilities.getPHPTokenSequence(document, caretOffset);
        if (tokenSequence == null) {
            return CompletionContext.NONE;
        }
        TokenHierarchy th = TokenHierarchy.get(document);
        tokenSequence.move(caretOffset);
        if (!tokenSequence.moveNext() && !tokenSequence.movePrevious()){
            return CompletionContext.NONE;
        }
        Token<PHPTokenId> token = tokenSequence.token();
        PHPTokenId tokenId =token.id();
        int tokenIdOffset = tokenSequence.token().offset(th);

        switch (tokenId){
            case T_INLINE_HTML:
                return CompletionContext.HTML;
            case PHP_CONSTANT_ENCAPSED_STRING:
                char encChar = tokenSequence.token().text().charAt(0);
                if (encChar == '"') {//NOI18N
                    if (acceptTokenChains(tokenSequence, SERVER_ARRAY_TOKENCHAINS)
                            && acceptTokenChainTexts(tokenSequence, SERVER_ARRAY_TOKENTEXTS)) {
                        return CompletionContext.SERVER_ENTRY_CONSTANTS;
                    }
                    return CompletionContext.STRING;
                } else if (encChar == '\'') {//NOI18N
                    if (acceptTokenChains(tokenSequence, SERVER_ARRAY_TOKENCHAINS)
                            && acceptTokenChainTexts(tokenSequence, SERVER_ARRAY_TOKENTEXTS)) {
                        return CompletionContext.SERVER_ENTRY_CONSTANTS;
                    }
                }
                return CompletionContext.NONE;
            default:
        }
        CompletionContext clsIfaceDeclContext = getClsIfaceDeclContext(token,
                (caretOffset-tokenIdOffset), tokenSequence);
        if (clsIfaceDeclContext != null) {
            return clsIfaceDeclContext;
        }

        if (acceptTokenChains(tokenSequence, CLASS_NAME_TOKENCHAINS)){
            return CompletionContext.CLASS_NAME;
        } else if (acceptTokenChains(tokenSequence, CLASS_MEMBER_TOKENCHAINS)){
            return CompletionContext.CLASS_MEMBER;
        } else if (acceptTokenChains(tokenSequence, STATIC_CLASS_MEMBER_TOKENCHAINS)){
            return CompletionContext.STATIC_CLASS_MEMBER;
        } else if (isOneOfTokens(tokenSequence, COMMENT_TOKENS)){
            return CompletionContext.NONE;
        } else if (acceptTokenChains(tokenSequence, PHPDOC_TOKENCHAINS)){
            return CompletionContext.PHPDOC;
        } else if (acceptTokenChains(tokenSequence, TYPE_TOKENCHAINS)){
            return CompletionContext.TYPE_NAME;
        } else if (isInsideClassIfaceDeclarationBlock(info, caretOffset, tokenSequence)) {
            if (acceptTokenChains(tokenSequence, CLASS_CONTEXT_KEYWORDS_TOKENCHAINS)) {
                return CompletionContext.CLASS_CONTEXT_KEYWORDS;
            } else if (acceptTokenChains(tokenSequence, FUNCTION_TOKENCHAINS)) {
                return CompletionContext.METHOD_NAME;
            }
            return CompletionContext.NONE;
        }
        return CompletionContext.EXPRESSION;
    }

    private static boolean isOneOfTokens(TokenSequence tokenSequence, PHPTokenId[] tokenIds){
        TokenId searchedId = tokenSequence.token().id();

        for (TokenId tokenId : tokenIds){
            if (tokenId.equals(searchedId)){
                return true;
            }
        }

        return false;
    }

    private static boolean acceptTokenChainTexts(TokenSequence tokenSequence, List<String> tokenTexts) {
        Token[] preceedingTokens = getPreceedingTokens(tokenSequence, tokenTexts.size());
        if (preceedingTokens.length != tokenTexts.size()) {
            return false;
        }
        for (int idx = 0; idx < preceedingTokens.length; idx++) {
            String expectedText = tokenTexts.get(idx);
            if (!expectedText.contentEquals(preceedingTokens[idx].text())) {
                return false;
            }
        }
        return true;
    }

    private static boolean acceptTokenChains(TokenSequence tokenSequence, List<PHPTokenId[]> tokenIdChains) {
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

    @CheckForNull
    private static CompletionContext getClsIfaceDeclContext(Token<PHPTokenId> token, int tokenOffset,TokenSequence tokenSequence) {
        boolean isClass = false;
        boolean isIface = false;
        boolean isExtends = false;
        boolean isImplements = false;
        boolean isString = false;
        Token<PHPTokenId> stringToken = null;
        Token[] preceedingLineTokens = getPreceedingLineTokens(token, tokenOffset, tokenSequence);
        for (Token cToken : preceedingLineTokens) {
            TokenId id = cToken.id();
            boolean nokeywords = !isIface && !isClass && !isExtends && !isImplements;
            if (id.equals(PHPTokenId.PHP_CLASS)) {
                isClass = true;
                break;
            } else if (id.equals(PHPTokenId.PHP_INTERFACE)) {
                isIface = true;
                break;
            } else if (id.equals(PHPTokenId.PHP_EXTENDS)) {
                isExtends = true;
            } else if (id.equals(PHPTokenId.PHP_IMPLEMENTS)) {
                isImplements = true;
            } else if (nokeywords && id.equals(PHPTokenId.PHP_STRING)) {
                isString = true;
                stringToken = cToken;
            } else {
                if (nokeywords && id.equals(PHPTokenId.PHP_CURLY_OPEN)) {
                    return null;
                }
            }
        }
        if (isClass || isIface) {
            if (isImplements) {
                return CompletionContext.INTERFACE_NAME;
            } else if (isExtends) {
                if (isString && isClass && stringToken != null && tokenOffset == 0
                        && preceedingLineTokens.length > 0 && preceedingLineTokens[0].text().equals(stringToken.text())  ) {
                    return CompletionContext.CLASS_NAME;
                } else if (isString && isClass) {
                    return CompletionContext.IMPLEMENTS;
                } else if (!isString && isClass) {
                    return CompletionContext.CLASS_NAME;
                } else if (isIface) {
                    return CompletionContext.INTERFACE_NAME;
                }
                return !isString ? isClass ? CompletionContext.CLASS_NAME : CompletionContext.INTERFACE_NAME : isClass ? CompletionContext.IMPLEMENTS : CompletionContext.INTERFACE_NAME;
            } else if (isIface) {
                return !isString ? CompletionContext.NONE : CompletionContext.EXTENDS;
            } else if (isClass) {
                return !isString ? CompletionContext.NONE : CompletionContext.INHERITANCE;
            }
        }
        return null;
    }

    static boolean lineContainsAny(Token<PHPTokenId> token,int tokenOffset, TokenSequence tokenSequence, List<PHPTokenId> ids) {
        Token[] preceedingLineTokens = getPreceedingLineTokens(token, tokenOffset, tokenSequence);
        for (Token t : preceedingLineTokens) {
            if (ids.contains(t.id())) {
                return true;
            }
        }
        return false;
    }
    /**
     * @return all preceding tokens for current line
     */
    private static Token[] getPreceedingLineTokens(Token<PHPTokenId> token,int tokenOffset, TokenSequence tokenSequence){
        int orgOffset = tokenSequence.offset();
        LinkedList<Token> tokens = new LinkedList<Token>();
        if (token.id() != PHPTokenId.WHITESPACE ||
                token.text().subSequence(0, 
                Math.min(token.text().length(), tokenOffset)).toString().indexOf("\n") == -1) {//NOI18N
            while (true) {
                if (!tokenSequence.movePrevious()) {
                    break;
                }
                Token cToken = tokenSequence.token();
                if (cToken.id() == PHPTokenId.WHITESPACE &&
                        cToken.text().toString().indexOf("\n") != -1) {//NOI18N
                    break;
                }                
                tokens.addLast(cToken);
            }
        }

        tokenSequence.move(orgOffset);
        tokenSequence.moveNext();
        return tokens.toArray(new Token[tokens.size()]);
    }

    private synchronized static boolean isInsideClassIfaceDeclarationBlock(CompilationInfo info,
            int caretOffset, TokenSequence tokenSequence){
        List<ASTNode> nodePath = NavUtils.underCaret(info, lexerToASTOffset(info, caretOffset));
        boolean methDecl = false;
        boolean funcDecl = false;
        boolean clsDecl = false;
        boolean isClassInsideFunc = false;
        boolean isFuncInsideClass = false;
        for (ASTNode aSTNode : nodePath) {
            if (aSTNode instanceof FunctionDeclaration) {
                funcDecl = true;
                if (clsDecl) isFuncInsideClass = true;
            } else if (aSTNode instanceof MethodDeclaration) {
                methDecl = true;
            } else if (aSTNode instanceof ClassDeclaration) {
                clsDecl = true;
                if (funcDecl) isClassInsideFunc = true;
            } else if (aSTNode instanceof InterfaceDeclaration) {
                clsDecl = true;
            }
        }
        if (funcDecl && !methDecl && !clsDecl) {
            final StringBuilder sb = new StringBuilder();
            new DefaultVisitor(){
                @Override
                public void visit(ASTError astError) {
                    super.visit(astError);
                    sb.append(astError.toString());
                }
            }.scan(Utils.getRoot(info));
            if (sb.length() == 0) {
                return false;
            }
        }
        if (isClassInsideFunc && !isFuncInsideClass) {
            return true;
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
                        id.equals(PHPTokenId.PHP_WHILE) ||
                        id.equals(PHPTokenId.PHP_IF) ||
                        id.equals(PHPTokenId.PHP_FOR) ||
                        id.equals(PHPTokenId.PHP_FOREACH) ||
                        id.equals(PHPTokenId.PHP_TRY) ||
                        id.equals(PHPTokenId.PHP_CATCH))
                        && (curly_open > curly_close)) {
                    return false;
                } else if (id.equals(PHPTokenId.PHP_CLASS) || id.equals(PHPTokenId.PHP_INTERFACE)) {
                    boolean isClassScope = curly_open > 0 && (curly_open > curly_close);
                    return isClassScope;
                }
            }
        } finally {
            tokenSequence.move(orgOffset);
            tokenSequence.moveNext();
        }
        return false;
    }

    static int lexerToASTOffset (PHPParseResult result, int lexerOffset) {
        if (result.getTranslatedSource() != null) {
            return result.getTranslatedSource().getAstOffset(lexerOffset);
        }
        return lexerOffset;
    }

    static int lexerToASTOffset(CompilationInfo info, int lexerOffset) {
        PHPParseResult result = (PHPParseResult) info.getEmbeddedResult(PHPLanguage.PHP_MIME_TYPE, lexerOffset);
        return lexerToASTOffset(result, lexerOffset);
    }
}
