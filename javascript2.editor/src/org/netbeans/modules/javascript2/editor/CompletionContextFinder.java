/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor;

import org.netbeans.modules.javascript2.editor.spi.CompletionContext;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.api.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.api.lexer.LexUtilities;

/**
 *
 * @author Petr Pisl
 */
public class CompletionContextFinder {
   
    private static final List<JsTokenId> WHITESPACES_TOKENS = Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.EOL);
    
    private static final List<JsTokenId> CHANGE_CONTEXT_TOKENS = Arrays.asList(
            JsTokenId.OPERATOR_SEMICOLON, JsTokenId.BRACKET_LEFT_CURLY, JsTokenId.BRACKET_RIGHT_CURLY);
    private static final List<Object[]> OBJECT_PROPERTY_TOKENCHAINS = Arrays.asList(
        new Object[]{JsTokenId.OPERATOR_DOT},
        new Object[]{JsTokenId.OPERATOR_DOT, JsTokenId.IDENTIFIER}
    );
    
    private static final List<Object[]> OBJECT_THIS_TOKENCHAINS = Arrays.asList(
        new Object[]{JsTokenId.KEYWORD_THIS, JsTokenId.OPERATOR_DOT},
        new Object[]{JsTokenId.KEYWORD_THIS, JsTokenId.OPERATOR_DOT, JsTokenId.IDENTIFIER}
    );
        
    @NonNull
    static CompletionContext findCompletionContext(ParserResult info, int offset){
        TokenHierarchy<?> th = info.getSnapshot().getTokenHierarchy();
        if (th == null) {
            return CompletionContext.NONE;
        }
        TokenSequence<JsTokenId> ts = th.tokenSequence(JsTokenId.javascriptLanguage());
        if (ts == null) {
            return CompletionContext.NONE;
        }
        
        ts.move(offset);
        
        if (!ts.moveNext() && !ts.movePrevious()){
            return CompletionContext.NONE;
        }
               
        Token<? extends JsTokenId> token = ts.token();
        JsTokenId tokenId =token.id();
        
        if (tokenId == JsTokenId.STRING || tokenId == JsTokenId.STRING_END) {
            return CompletionContext.STRING;
        }
        
        if (acceptTokenChains(ts, OBJECT_THIS_TOKENCHAINS, true)) {
            return CompletionContext.OBJECT_MEMBERS;
        }
        
        if (acceptTokenChains(ts, OBJECT_PROPERTY_TOKENCHAINS, tokenId != JsTokenId.OPERATOR_DOT)) {
            return CompletionContext.OBJECT_PROPERTY;
        }
        
        ts.move(offset); 
        if (ts.moveNext()) {
            if (isPropertyNameContext(ts)) {
                return CompletionContext.OBJECT_PROPERTY_NAME;
            }
        }
        
        ts.move(offset); 
        if (!ts.moveNext()) {
            if (!ts.movePrevious()) {
                return CompletionContext.GLOBAL;
            }
        }
        token = ts.token(); tokenId = token.id();
        if (tokenId == JsTokenId.EOL && ts.movePrevious()) {
            token = ts.token(); tokenId = token.id();
        }
        if (tokenId == JsTokenId.IDENTIFIER || WHITESPACES_TOKENS.contains(tokenId)) {
            if (!ts.movePrevious()) {
                return CompletionContext.GLOBAL;
            }
            token = LexUtilities.findPrevious(ts, WHITESPACES_TOKENS);
        }
        if (CHANGE_CONTEXT_TOKENS.contains(token.id())
                || (WHITESPACES_TOKENS.contains(token.id()) && !ts.movePrevious())) {
            return CompletionContext.GLOBAL;
        }
        if (tokenId == JsTokenId.DOC_COMMENT) {
            return CompletionContext.DOCUMENTATION;
        }
        
        return CompletionContext.EXPRESSION;
    }
    
    private static boolean isPropertyNameContext(TokenSequence<JsTokenId> ts) {
        
        //find the begining of the object literal
        Token<? extends JsTokenId> token = null;
        JsTokenId tokenId;
        
        List<JsTokenId> listIds = Arrays.asList(JsTokenId.OPERATOR_COMMA, JsTokenId.OPERATOR_COLON, JsTokenId.BRACKET_LEFT_CURLY, JsTokenId.OPERATOR_SEMICOLON);
        // find previous , or : or { or ;
        token = LexUtilities.findPreviousToken(ts, listIds);
        tokenId = token.id();
        boolean commaFirst = false;
        if (tokenId == JsTokenId.OPERATOR_COMMA && ts.movePrevious()) {
            token = LexUtilities.findPreviousToken(ts, listIds);
            tokenId = token.id();
            commaFirst = true;
            if (tokenId == JsTokenId.OPERATOR_COLON) {
                // we are in the previous property definition
                return true;
            } 
        } 
        if (tokenId == JsTokenId.BRACKET_LEFT_CURLY && ts.movePrevious()) {
            List<JsTokenId> emptyIds = Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.EOL, JsTokenId.BLOCK_COMMENT);
            // check whether it's the first property in the object literal definion
            token = LexUtilities.findPrevious(ts, emptyIds);
            tokenId = token.id();
            if (tokenId == JsTokenId.BRACKET_LEFT_PAREN || tokenId == JsTokenId.OPERATOR_COMMA || tokenId == JsTokenId.OPERATOR_EQUALS || tokenId == JsTokenId.OPERATOR_COLON) {
                return true;
            } else if (tokenId == JsTokenId.BRACKET_RIGHT_PAREN) {
                // it can be a method definition
                int balance = 1;
                while (ts.movePrevious() && balance > 0) {
                    token = ts.token();
                    tokenId = token.id();
                    if (tokenId == JsTokenId.BRACKET_RIGHT_PAREN) {
                        balance++;
                    } else if (tokenId == JsTokenId.BRACKET_LEFT_PAREN) {
                        balance--;
                    }
                }
                if (balance == 0) {
                    token = LexUtilities.findPrevious(ts, emptyIds);
                    tokenId = token.id();
                    if (tokenId == JsTokenId.KEYWORD_FUNCTION && ts.movePrevious()) {
                        // we found a method definition, now we need to check, whether its in an object literal
                        token = LexUtilities.findPrevious(ts, emptyIds);
                        tokenId = token.id();
                        if (tokenId == JsTokenId.OPERATOR_COLON) {
                            return commaFirst;
                        }
                    }
                }
            }
        }
            
        return false;
    }
    
    private static boolean acceptTokenChains(TokenSequence tokenSequence, List<Object[]> tokenIdChains, boolean movePrevious) {
        for (Object[] tokenIDChain : tokenIdChains){
            if (acceptTokenChain(tokenSequence, tokenIDChain, movePrevious)){
                return true;
            }
        }

        return false;
    }
    
    private static boolean acceptTokenChain(TokenSequence tokenSequence, Object[] tokenIdChain, boolean movePrevious) {
        int orgTokenSequencePos = tokenSequence.offset();
        boolean accept = true;
        boolean moreTokens = movePrevious ? tokenSequence.movePrevious() : true;

        for (int i = tokenIdChain.length - 1; i >= 0; i --){
            Object tokenID = tokenIdChain[i];

            if (!moreTokens){
                accept = false;
                break;
            }

           if (tokenID instanceof JsTokenId) {
                if (tokenSequence.token().id() == tokenID){
                    moreTokens = tokenSequence.movePrevious();
                } else {
                    // NO MATCH
                    accept = false;
                    break;
                }
            } else {
                assert false : "Unsupported token type: " + tokenID.getClass().getName();
            }
        }

        tokenSequence.move(orgTokenSequencePos);
        tokenSequence.moveNext();
       return accept;
    }
}
