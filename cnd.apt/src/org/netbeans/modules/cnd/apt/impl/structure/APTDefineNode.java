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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.apt.utils.ListBasedTokenStream;

/**
 * #define directive implementation
 * @author Vladimir Voskresensky
 */
public final class APTDefineNode extends APTMacroBaseNode 
                                    implements APTDefine, Serializable {
    private static final long serialVersionUID = -99267816578145490L;
    
    private List params = null;
    private List bodyTokens = null;
    // TODO: how to save memory here? state is needed only on initializing
    transient private byte state = BEFORE_MACRO_NAME;
    
    private static final byte BEFORE_MACRO_NAME = 0;
    private static final byte AFTER_MACRO_NAME = 1;
    private static final byte IN_PARAMS = 2;
    private static final byte IN_BODY = 3;
    
    /** Copy constructor */
    /**package*/APTDefineNode(APTDefineNode orig) {
        super(orig);
        this.params = orig.params;
        this.bodyTokens = orig.bodyTokens;
    }
    
    /** Constructor for serialization */
    protected APTDefineNode() {
    }

    /** Creates a new instance of APTDefineNode */
    public APTDefineNode(Token token) {
        super(token);
    }

    public final int getType() {
        return APT.Type.DEFINE;
    }
    
    public Collection getParams() {
        if (params == null) {
            return null;
        } else {
            return Collections.unmodifiableList(params);// != null ? (Token[]) params.toArray(new Token[params.size()]) : null;
        }
    }
    
    public boolean isFunctionLike() {
        return params != null;
    }
    
    /**
     * returns List of Tokens of macro body
     */
    public List getBody() {
        return bodyTokens != null ? bodyTokens : Collections.EMPTY_LIST;
    }
    
    public boolean accept(Token token) {
        int ttype = token.getType();
        if (APTUtils.isEndDirectiveToken(ttype)) {
            return false;
        } else {
            switch (state) {
                case BEFORE_MACRO_NAME:
                {
                    // allow base class to remember macro nam
                    boolean accepted = super.accept(token);
                    assert(accepted);
                    state = AFTER_MACRO_NAME;
                    break;
                }
                case AFTER_MACRO_NAME:
                {
                    if (token.getType() == APTTokenTypes.FUN_LIKE_MACRO_LPAREN) {
                        params = new ArrayList();
                        state = IN_PARAMS;
                    } else {
                        if (bodyTokens == null) {
                            bodyTokens = new ArrayList();
                        }
                        bodyTokens.add(token);                        
                        state = IN_BODY;
                    }
                    break;
                }
                case IN_PARAMS:
                {
                    switch (token.getType()) {
                        case APTTokenTypes.ID:
                            params.add(token);
                            // leave IN_PARAMS state
                            break;
                        case APTTokenTypes.RPAREN:
                            state = IN_BODY;
                            break;
                        case APTTokenTypes.ELLIPSIS:
                            // TODO: need to support ELLIPSIS for IZ#83949
                            break;
                        default:
                            // eat comma and comments and leave IN_PARAMS state
                            assert (token.getType() == APTTokenTypes.COMMA || APTUtils.isCommentToken(token.getType()));
                            break;
                    }
                    break;
                }
                case IN_BODY:
                {
                    // init body list if necessary
                    if (bodyTokens == null) {
                        bodyTokens = new ArrayList();
                    }
                    bodyTokens.add(token);
                    break;
                }
                default:
                    assert(false) : "unexpected state";
            }
            return true;
        }
    }
    
    public String getText() {
        String ret = super.getText();
        String paramStr = "";
        if (params != null) {
            paramStr = "PARAMS{" + APTUtils.toString(new ListBasedTokenStream(this.params)) + "}";
        }
        String bodyStr;
        if (bodyTokens != null) {
            bodyStr = "BODY{" + APTUtils.toString(new ListBasedTokenStream(getBody())) + "}";
        } else {
            bodyStr = "{NO BODY}";
        }
        return ret + paramStr + bodyStr;
    }
}
