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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.completion.cplusplus.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenProcessor;
import org.netbeans.modules.cnd.completion.cplusplus.NbCsmSyntaxSupport;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class TokenUtilities {
    
    /** Creates a new instance of TokenUtilities */
    private TokenUtilities() {
    }
    
    public static List<Token> getTokens(Document doc, int start, int end) {
        if (!(doc instanceof BaseDocument))
            return Collections.<Token>emptyList();
        
        try {
            SyntaxSupport sup = ((BaseDocument) doc).getSyntaxSupport();
            NbCsmSyntaxSupport nbSyntaxSup = (NbCsmSyntaxSupport)sup.get(NbCsmSyntaxSupport.class);
            if (nbSyntaxSup == null) {
                String name = (String) doc.getProperty(Document.TitleProperty);
                System.err.println("document " + name + " doesn't have NbCsmSyntaxSupport syntax support " + doc);
                System.err.println("document " + (CndLexerUtilities.getCppTokenSequence(doc, start) == null ? "DOESN'T" : "") + " have " + "lexer info");
                return Collections.<Token>emptyList();
            }
           
            AllTokensTP tp = new AllTokensTP();
            nbSyntaxSup.tokenizeText(tp, start, end, true);
            return tp.getTokens();
        } catch (BadLocationException e) {
            // there could be some modifications in document
            return Collections.<Token>emptyList();
        }
    }
    
    public static Token getToken(Document doc, int offset) {
        if (!(doc instanceof BaseDocument))
            return null;
        
        try {
            BaseDocument bdoc = (BaseDocument) doc;
            
            SyntaxSupport sup = bdoc.getSyntaxSupport();
            NbCsmSyntaxSupport nbSyntaxSup = (NbCsmSyntaxSupport)sup.get(NbCsmSyntaxSupport.class);
            if (nbSyntaxSup == null) {
                return null;
            }
            FirstTokenTP fttp = new FirstTokenTP();
            nbSyntaxSup.tokenizeText(fttp, offset, doc.getLength(), true);
            Token token = fttp.getToken();
            
            return token;
        } catch (BadLocationException e) {
            // there could be some modifications in document
            return null;
        }
    }
    
    private static int TOKEN_LIMIT = 100;
    
    public static int correctOffsetToID(BaseDocument doc, int offset) {
        if (doc == null) {
            return offset;
        }
        SyntaxSupport sup = doc.getSyntaxSupport();
        if (sup == null) {
            return offset;
        }
        NbCsmSyntaxSupport nbSyntaxSup = (NbCsmSyntaxSupport)sup.get(NbCsmSyntaxSupport.class);
        if (nbSyntaxSup == null) {
            return offset;
        }
        int tokenLimitCounter = 0;
        int origOffset = offset;
        boolean wasNonWS = false;
        try {
            while (tokenLimitCounter < TOKEN_LIMIT && offset >= 0) {
                TokenID token = nbSyntaxSup.getTokenID(offset);
                tokenLimitCounter++;
                if (token == null) {
                    return origOffset;
                } else if (token == CCTokenContext.IDENTIFIER || 
                        token == CCTokenContext.SYS_INCLUDE ||
                        token == CCTokenContext.USR_INCLUDE) {
                    return offset;
                } else if (token == CCTokenContext.WHITESPACE) {
                    offset--;
                    if (wasNonWS) {
                        return origOffset;
                    }                    
                } else if (token.getCategory() == CCTokenContext.OPERATORS) {
                    offset--;
                    if (wasNonWS) {
                        return origOffset;
                    }
                    wasNonWS = true;
                } else {
                    return origOffset;
                }
            }
        } catch (BadLocationException e) {
            // there could be some modifications in document
        }
        return origOffset;
    }
    
    private static class FirstTokenTP implements TokenProcessor {
        
        private Token token;
        private int bufferStartPos;
        private char[] buffer;
        public Token getToken() {
            return token;
        }
        
        public boolean token(TokenID tokenID, TokenContextPath tokenContextPath,
                int offset, int tokenLen) {
            String text = new String(buffer, offset, tokenLen);
            token = new Token(bufferStartPos + offset, tokenLen, tokenID, text);
            return false; // no more tokens
        }
        
        public int eot(int offset) {
            return 0;
        }
        
        public void nextBuffer(char[] buffer, int offset, int len,
                int startPos, int preScan, boolean lastBuffer) {
            this.buffer = buffer;
            bufferStartPos = startPos - offset;
        }
        
    }

    private static class AllTokensTP implements TokenProcessor {
        
        private List<Token> tokens = new ArrayList<Token>();
        private int bufferStartPos;
        private char[] buffer;
        
        public List<Token> getTokens() {
            return tokens;
        }
        
        public boolean token(TokenID tokenID, TokenContextPath tokenContextPath,
                int offset, int tokenLen) {
            String text = new String(buffer, offset, tokenLen);
            tokens.add( new Token(bufferStartPos + offset, tokenLen, tokenID, text) );
            return true;
        }
        
        public int eot(int offset) {
            return 0;
        }
        
        public void nextBuffer(char[] buffer, int offset, int len,
                int startPos, int preScan, boolean lastBuffer) {
            this.buffer = buffer;
            bufferStartPos = startPos - offset;
        }
        
    }
}
