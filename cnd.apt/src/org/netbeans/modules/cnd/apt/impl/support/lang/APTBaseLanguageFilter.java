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

package org.netbeans.modules.cnd.apt.impl.support.lang;

import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.support.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 *
 * @author Vladimir Voskresensky
 */
public abstract class APTBaseLanguageFilter implements APTLanguageFilter {
    private Map filter/*<getTokenTextKey(token),Integer(ttype)>*/ = new HashMap();    

    // uncomment to use reduced memory
//    private static final int BUFFERED_COUNT = 256;
//    private static final Integer[] int2Int;
//    static {
//        int2Int = new Integer[BUFFERED_COUNT];
//        for (int i = 0; i < BUFFERED_COUNT; i++) {
//            int2Int[i] = null;
//        }
//    }
    
    /**
     * Creates a new instance of APTBaseLanguageFilter
     */
    protected APTBaseLanguageFilter() {
    }

    public TokenStream getFilteredStream(TokenStream origStream) {
        return new FilterStream(origStream);
    }
   
//    // do necessary initializations in derived classes by calling 
//    // filter() method to fill up the filter    
//    protected abstract void initialize();
    
    /**
     * add token's key to be filtered
     * the token stream returned from getFilteredStream
     * will change the type of original token to new token type
     * if original token has the filtered textKey value
     */  
    protected void filter(String text, int ttype) {
        Object textKey = APTUtils.getTextKey(text);
        // uncomment to use reduced memory
//        if (ttype < BUFFERED_COUNT) {
//            if (int2Int[ttype] == null) {
//                int2Int[ttype] = new Integer(ttype);
//            }
//        }
//        Integer val = (ttype < BUFFERED_COUNT) ? int2Int[ttype] : new Integer(ttype);
//        assert(val != null);
//        filter.put(textKey, val);
        filter.put(textKey, new Integer(ttype));
    }
    
    private Token onID(Token token) {
        Integer newType = (Integer)filter.get(APTUtils.getTokenTextKey(token));
        if (newType != null) {
            int ttype = newType.intValue();
            token = createKeyword(token, ttype);
        }
        return token;        
    }
    
    private Token createKeyword(Token token, int ttype) {
        APTToken newToken = APTUtils.createAPTToken(token, ttype);
        return (Token)newToken;
    }
    
    private final class FilterStream implements TokenStream {
        private TokenStream orig;
        public FilterStream(TokenStream orig) {
            this.orig = orig;
        }
        
        public Token nextToken() throws TokenStreamException {
            Token token = orig.nextToken();
            if (token.getType() == APTTokenTypes.ID) {
                token = onID(token);
            }
            return token;
        }        
    }
}
