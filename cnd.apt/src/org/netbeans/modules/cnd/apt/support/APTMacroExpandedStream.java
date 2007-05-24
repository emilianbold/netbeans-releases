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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.apt.support;

import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;
import org.netbeans.modules.cnd.apt.support.APTExpandedStream;
import org.netbeans.modules.cnd.apt.impl.support.MacroExpandedToken;

/**
 *
 * @author Vladimir Voskresensky
 */
public class APTMacroExpandedStream extends APTExpandedStream {
    
    public APTMacroExpandedStream(TokenStream stream, APTMacroCallback callback) {
        super(stream, callback);
    }

    protected TokenStream createMacroBodyWrapper(Token token, APTMacro macro) throws TokenStreamException, RecognitionException {
        TokenStream origExpansion = super.createMacroBodyWrapper(token, macro);
        Token last = getLastExtractedParamRPAREN();
        if (last == null) {
            last = token;
        }
        TokenStream expandedMacros = new MacroExpandWrapper(token, origExpansion, last);
        return expandedMacros;
    }   
    
    private static final class MacroExpandWrapper implements TokenStream {
        private final Token expandedFrom;
        private final TokenStream expandedMacros;
        private final Token endOffsetToken;
        
        public MacroExpandWrapper(Token expandedFrom, TokenStream expandedMacros, Token endOffsetToken) {
            this.expandedFrom = expandedFrom;
            this.expandedMacros = expandedMacros;
            assert endOffsetToken != null : "end offset token must be valid";
            this.endOffsetToken = endOffsetToken;
        }
        
        public Token nextToken() throws TokenStreamException {
            Token expandedTo = expandedMacros.nextToken();
            Token outToken = new MacroExpandedToken(expandedFrom, expandedTo, endOffsetToken);
            return outToken;
        }        
    }
}
