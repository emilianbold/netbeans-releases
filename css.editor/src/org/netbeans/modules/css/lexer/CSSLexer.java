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
package org.netbeans.modules.css.lexer;

import org.netbeans.api.lexer.Token;
import org.netbeans.modules.css.lexer.api.CSSTokenId;
import org.netbeans.modules.css.parser.CSSParserTokenManager;
import org.netbeans.modules.css.parser.TokenMgrError;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * javacc css token manager wrapper
 *
 * @author Marek Fukala
 * @version 1.00
 */
public final class CSSLexer implements Lexer<CSSTokenId> {

    private int lexerState;
    private final TokenFactory<CSSTokenId> tokenFactory;
    private CSSParserTokenManager tokenManager;
    private LexerRestartInfo<CSSTokenId> lexerRestartInfo;

    public Object state() {
        return lexerState;
    }

    public CSSLexer(LexerRestartInfo<CSSTokenId> info) {
        this.lexerRestartInfo = info;
        if (info.state() != null) {
            tokenManager = new CSSParserTokenManager(new LexerCharStream(info), ((Integer) info.state()).intValue());
        } else {
            tokenManager = new CSSParserTokenManager(new LexerCharStream(info));
        }
        this.tokenFactory = info.tokenFactory();
    }

    public Token<CSSTokenId> nextToken() {
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

                int array_index = token.kind;

                //hack - enum member to int conversion
                //see the SACParserConstants indexes
                if (array_index == 0 || array_index == 1) {
                    //EOF or S tokens
                    //no change, jj token indexes match lexer token indexes
                    //        } else if(array_index == 3) {
                    //            //COMMENT
                    //            array_index = 2;
                } else {
                    //the rest of tokens 
                    array_index -= 3;
                }

                //return netbeans lexer's token based on the type of the obtained javacc token
                //all info like the image and offset will be got from the lexer input
                return tokenFactory.createToken(CSSTokenId.values()[array_index]);
            }

        } catch (TokenMgrError tme) {
            //something bad happened in the javacc lexer, the following section tries to recover
        }

        //the token got from the javacc lexer has an empty text or a TME has been thrown
        if (lexerRestartInfo.input().readLength() > 0) {
            //there is something in the buffer, return it as unknown token
            return tokenFactory.createToken(CSSTokenId.UNKNOWN);
        } else {
            //just EOF in the buffer, finish lexing
            return null;
        }



//        int actChar;
//
//        while (true) {
//            actChar = input.read();
//
//            if (actChar == LexerInput.EOF) {
//                if (input.readLengthEOF() == 1) {
//                    return null; //just EOL is read
//                } else {
//                    //there is something else in the buffer except EOL
//                    //we will return last token now
//                    input.backup(1); //backup the EOL, we will return null in next nextToken() call
//                    break;
//                }
//            }
//
//            switch (lexerState) {
//            }
//            
//            
//        } // end of while(offset...)
//
//        /** At this stage there's no more text in the scanned buffer.
//         * Scanner first checks whether this is completely the last
//         * available buffer.
//         */
//        switch (lexerState) {
//            case 0:
//                if (input.readLength() == 0) {
//                    return null;
//                }
//                break;
////            case ISI_TEXT:
////                return token(CSSTokenId.STYLE);
//
//        }
//
//        return null;
    }

//    private Token<CSSTokenId> token(CSSTokenId tokenId) {
//        if (input.readLength() == 0) {
//            System.out.println("Found zero length token: ");
//        }
//        System.out.println("[" + this.getClass().getSimpleName() + "] token ('" + input.readText().toString() + "'; id=" + tokenId + "; state=" + state() + ")\n");
//
//        return tokenFactory.createToken(tokenId);
//    }
    public void release() {
        tokenManager = null;
    }
}
