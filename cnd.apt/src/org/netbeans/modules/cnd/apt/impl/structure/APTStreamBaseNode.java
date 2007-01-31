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

package org.netbeans.modules.cnd.apt.impl.structure;

import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * base implementation of nodes with associated stream
 *
 * @author Vladimir Voskresensky
 */
public abstract class APTStreamBaseNode extends APTTokenBasedNode
                                        implements Serializable {
    private static final long serialVersionUID = -1498074871896804293L;
    private List tokens;
    
    /** Copy constructor */
    /**package*/ APTStreamBaseNode(APTStreamBaseNode orig) {
        super(orig);
        this.tokens = orig.tokens;
    }
    
    /** Constructor for serialization **/
    protected APTStreamBaseNode() {
    }
    
    /**
     * Creates a new instance of APTStreamBaseNode
     */
    public APTStreamBaseNode(Token token) {
        super(token);
        assert (validToken(token)) : "must init only from valid tokens"; // NOI18N
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // implementation of abstract methods
    
    public boolean accept(Token token) {
        boolean accepted = false;
        if (validToken(token)) {
            accepted = true;
            appendToken(token);
        }
        return accepted;
    }
    
    /**
     * APTStream node doesn't have children
     */
    public APT getFirstChild() {
        return null;
    }
    
    /**
     * APTStream node doesn't have children
     */
    public void setFirstChild(APT child) {
        assert(false) : "stream node doesn't support children"; // NOI18N
    }
    
    /** returns list of tokens */
    public String getText() {
        StringBuffer retValue = new StringBuffer("TOKENS{"); // NOI18N
        try {
            TokenStream ts = getTokenStream();
            for (Token token = ts.nextToken(); !APTUtils.isEOF(token);) {
                assert(token != null) : "list of tokens must not have 'null' elements"; // NOI18N
                retValue.append(token.toString());
                token = ts.nextToken();
                if (!APTUtils.isEOF(token)) {
                    retValue.append("; "); // NOI18N
                }
            }
        } catch (TokenStreamException ex) {
            assert(false);
        }
        return retValue.append('}').toString(); // NOI18N
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // implementation of APTStream interface
    
    /**
     * returns reset token stream of the node;
     * use this method to get first access to token stream,
     * do not use this method as each time getter,
     * reset stream means, that token stream's iterator
     * moved to the begin of the stream
     */
    public TokenStream getTokenStream() {
        return new TokenStreamIterator();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // reset tokens
    public void dispose() {
        super.dispose();
        tokens = null;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // help implementation methods
    
    private void appendToken(Token token) {
        assert (validToken(token)) : "must append only valid tokens"; // NOI18N
        if (tokens == null) {
            tokens = new ArrayList();
        }
        tokens.add(token);
    }
    
    protected abstract boolean validToken(Token t);
    
    /** token stream iterator */
    private class TokenStreamIterator implements TokenStream {
        private int index = -1;
        
        public Token nextToken() throws TokenStreamException {
            Token token = null;
            if (index == -1) {
                token = getToken();
                index++;
            } else if (tokens != null && index < tokens.size()) {
                token = (Token) tokens.get(index++);
            } else {
                token = APTUtils.EOF_TOKEN;
            };
            return token;
        }
    }
}
