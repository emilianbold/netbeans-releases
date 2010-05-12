/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.css.lexer;

import org.netbeans.api.lexer.Token;
import org.netbeans.modules.css.lexer.api.CssTokenId;
import org.netbeans.modules.css.parser.CssParserConstants;
import org.netbeans.modules.css.parser.CssParserTokenManager;
import org.netbeans.modules.css.parser.PatchedCssParserTokenManager;
import org.netbeans.modules.css.parser.TokenMgrError;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * javacc css token manager wrapper
 *
 * @author Marek Fukala
 * @version 1.00
 */
public final class CssLexer implements Lexer<CssTokenId> {

    private boolean                         inOptionsDialog;
    private LexerInput                      input;
    private int                             lexerState;
    private final TokenFactory<CssTokenId>  tokenFactory;
    private CssParserTokenManager           tokenManager;
    private LexerRestartInfo<CssTokenId>    lexerRestartInfo;

    private static final int JCCTOKEN_TO_TOKENID_INDEX_DIFF = CssParserConstants.LBRACE - CssTokenId.LBRACE.ordinal();

    @Override
    public Object state() {
        return lexerState;
    }

    public CssLexer (LexerRestartInfo<CssTokenId> info) {
        this.lexerRestartInfo = info;
        input = info.input ();
        if (info.state() != null) {
            tokenManager = new PatchedCssParserTokenManager(new LexerCharStream(info), ((Integer) info.state()).intValue());
        } else {
            tokenManager = new PatchedCssParserTokenManager(new LexerCharStream(info));
        }
        this.tokenFactory = info.tokenFactory();
        inOptionsDialog = info.getAttributeValue ("OptionsDialog") != null;
        Integer state = (Integer) lexerRestartInfo.state ();
        lexerState = state == null ?
            0 : (int) state;
    }

    private static CssTokenId[] tokens = {
        CssTokenId.COMMENT, CssTokenId.ATKEYWORD, CssTokenId.CDO, CssTokenId.URL,
        CssTokenId.SEMICOLON, CssTokenId.SELECTOR_NMSTART, CssTokenId.COLON, CssTokenId.CDO,
        CssTokenId.SELECTOR_NMSTART, CssTokenId.CDO, CssTokenId.GT, CssTokenId.CDO,
        CssTokenId.SELECTOR_NMSTART, CssTokenId.CDO, CssTokenId.LBRACE, CssTokenId.CDO,
        CssTokenId.NAME, CssTokenId.CDO, CssTokenId.SEMICOLON, CssTokenId.CDO,
        CssTokenId.NUMBER, CssTokenId.SEMICOLON, CssTokenId.CDO, CssTokenId.NAME,
        CssTokenId.SEMICOLON, CssTokenId.FUNCTION, CssTokenId.STRING, CssTokenId.RBRACE,
        CssTokenId.SEMICOLON, CssTokenId.CDO, CssTokenId.RBRACE
    };

    private static int[] lengths = {
        36, 6, 0, 14, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 2, 4, 0, 0, 
        0, 6, 0, 2, 15, 0, 3, 25, 0, 0, 0, 0
    };

    @Override
    public Token<CssTokenId> nextToken() {
        if (inOptionsDialog) {
            int next = input.read ();
            if (next == LexerInput.EOF)
                return null;
            if (lexerState >= lengths.length)
                return tokenFactory.createToken (CssTokenId.CDO);
            for (int i = 0; i < lengths [lexerState]; i++)
                input.read ();
            return tokenFactory.createToken (tokens [lexerState++]);
        }
        org.netbeans.modules.css.parser.Token token = null;
        try {

            //read a token from the javacc tokenizer
            //this may throw TokenMgrError under some circumstances
            //for example if EOF appears in the middle of regular expression evaluation
            //or just after MORE lexical state (which currently happens for unclosed
            //comments.
            token = tokenManager.getNextToken();

            //looks like we successfully obtained a token
            if (token.image.length() > 0) {

                int idx = token.kind;
                int diff = 0;
                //hack - enum member to int conversion
                switch(idx) {
                    case CssParserConstants.EOF:
                    case CssParserConstants.S:
                        break; //EOF and S tokens, maps properly
                    case CssParserConstants.COMMENT:
                        diff = idx - CssTokenId.COMMENT.ordinal();
                        break; 
                    case CssParserConstants.MSE:
                        diff = idx - CssTokenId.MSE.ordinal();
                        break;
                    default:
                        diff = JCCTOKEN_TO_TOKENID_INDEX_DIFF; //others
                }

                idx -= diff;

                //return netbeans lexer's token based on the type of the obtained javacc token
                //all info like the image and offset will be got from the lexer input
                return tokenFactory.createToken(CssTokenId.values()[idx]);
            }

        } catch (TokenMgrError tme) {
            //something bad happened in the javacc lexer, the following section tries to recover
        }

        //the token got from the javacc lexer has an empty text or a TME has been thrown
        if (lexerRestartInfo.input().readLength() > 0) {
            //there is something in the buffer, return it as unknown token
            return tokenFactory.createToken(CssTokenId.UNKNOWN);
        } else {
            //just EOF in the buffer, finish lexing
            return null;
        }

    }

    @Override
    public void release() {
        tokenManager = null;
    }
}
