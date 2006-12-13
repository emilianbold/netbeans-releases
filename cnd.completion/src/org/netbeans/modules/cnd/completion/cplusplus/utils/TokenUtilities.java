/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.completion.cplusplus.utils;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenProcessor;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.completion.cplusplus.NbCsmSyntaxSupport;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;
import org.openide.ErrorManager;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class TokenUtilities {
    
    /** Creates a new instance of TokenUtilities */
    private TokenUtilities() {
    }
    
    public static Token getToken(Document doc, int offset) {
        if (!(doc instanceof BaseDocument))
            return null;
        
        try {
            BaseDocument bdoc = (BaseDocument) doc;
            JTextComponent target = Utilities.getFocusedComponent();
            
            if (target == null || target.getDocument() != bdoc)
                return null;
            
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
            ErrorManager.getDefault().notify(e);
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
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
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
}
