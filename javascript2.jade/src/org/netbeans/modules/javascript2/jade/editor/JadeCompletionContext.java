/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.jade.editor;

import java.util.Arrays;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.jade.editor.lexer.JadeTokenId;

/**
 *
 * @author Petr Pisl
 */
public enum JadeCompletionContext {
    NONE, // There shouldn't be any code completion
    TAG,  // offer only html tags
    TAG_AND_KEYWORD, // tags and keywords
    ATTRIBUTE;   // html attributes
    
    private static final List<Object[]> START_LINE = Arrays.asList(
        new Object[]{JadeTokenId.EOL},
        new Object[]{JadeTokenId.EOL, JadeTokenId.WHITESPACE},
        new Object[]{JadeTokenId.EOL, JadeTokenId.WHITESPACE, JadeTokenId.TAG}
    );
    
    private static final List<Object[]> TAG_POSITON = Arrays.asList(
        new Object[]{JadeTokenId.TAG, JadeTokenId.OPERATOR_COLON},
        new Object[]{JadeTokenId.TAG, JadeTokenId.WHITESPACE},
        new Object[]{JadeTokenId.TAG, JadeTokenId.EOL},
        new Object[]{JadeTokenId.TAG, JadeTokenId.OPERATOR_COLON, JadeTokenId.TAG},
        new Object[]{JadeTokenId.TAG, JadeTokenId.OPERATOR_COLON, JadeTokenId.WHITESPACE},
        new Object[]{JadeTokenId.TAG, JadeTokenId.OPERATOR_COLON, JadeTokenId.WHITESPACE, JadeTokenId.TAG}
    );
    
    private static final List<Object[]> ATTRIBUTE_POSITION = Arrays.asList(
        new Object[]{JadeTokenId.ATTRIBUTE},
        new Object[]{JadeTokenId.BRACKET_LEFT_PAREN},
        new Object[]{JadeTokenId.BRACKET_LEFT_PAREN, JadeTokenId.WHITESPACE}
    );
    
    
    @NonNull
    public static JadeCompletionContext findCompletionContext(ParserResult info, int offset){
        TokenHierarchy<?> th = info.getSnapshot().getTokenHierarchy();
        if (th == null) {
            return NONE;
        }
        TokenSequence<JadeTokenId> ts = th.tokenSequence(JadeTokenId.jadeLanguage());
        if (ts == null) {
            return NONE;
        }
        
        ts.move(offset);
        
        if (!ts.movePrevious()) {
            return TAG_AND_KEYWORD;
        }
        
        Token<JadeTokenId> token = ts.token();
        JadeTokenId id = token.id();
        
        if (id == JadeTokenId.ATTRIBUTE) {
            return ATTRIBUTE;
        }
        
        if (!ts.moveNext()){
            return TAG_AND_KEYWORD;
        }
               
        if (acceptTokenChains(ts, START_LINE, true)) {
            return TAG_AND_KEYWORD;
        }
        
        if (acceptTokenChains(ts, TAG_POSITON, false)) {
            return TAG;
        }
        
        
        
        if (acceptTokenChains(ts, ATTRIBUTE_POSITION, false)) {
            return ATTRIBUTE;
        }
        
//        if (acceptTokenChains(ts, ATTRIBUTE_POSITION_AFTER, true)) {
//            return ATTRIBUTE;
//        }
        
        
        if (id == JadeTokenId.EOL && ts.movePrevious()) {
            token = ts.token(); id = token.id();
            if (id == JadeTokenId.TAG && !ts.movePrevious()) {
                return TAG_AND_KEYWORD;
            }
        }
        
        return NONE;
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

           if (tokenID instanceof JadeTokenId) {
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
