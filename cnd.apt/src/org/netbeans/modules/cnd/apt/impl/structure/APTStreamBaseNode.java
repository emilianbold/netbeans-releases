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
    private List<Token> tokens;
    
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
        StringBuilder retValue = new StringBuilder("TOKENS{"); // NOI18N
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
            tokens = new ArrayList<Token>();
        }
        tokens.add(token);
    }
    
    protected abstract boolean validToken(Token t);
    
    /** token stream iterator */
    private class TokenStreamIterator implements TokenStream {
        private int index = -1;
        
        public Token nextToken() throws TokenStreamException {
            Token token;
            if (index == -1) {
                token = getToken();
                index++;
            } else if (tokens != null && index < tokens.size()) {
                token = tokens.get(index++);
            } else {
                token = APTUtils.EOF_TOKEN;
            };
            return token;
        }
    }
}
