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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.debug.DebugUtils;
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
    
    private List<Token> params = null;
    private List<Token> bodyTokens = null;
    
    private byte state = BEFORE_MACRO_NAME;
    
    private static final byte BEFORE_MACRO_NAME = 0;
    private static final byte AFTER_MACRO_NAME = 1;
    private static final byte IN_PARAMS = 2;
    private static final byte IN_BODY = 3;
    private static final byte IN_BODY_AFTER_SHARP = 4;
    private static final byte ERROR = 5;
    
    /** Copy constructor */
    /**package*/APTDefineNode(APTDefineNode orig) {
        super(orig);
        this.params = orig.params;
        this.bodyTokens = orig.bodyTokens;
        this.state = orig.state;
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
    
    public Collection<Token> getParams() {
        if (params == null) {
            return null;
        } else {
            return Collections.<Token>unmodifiableList(params);// != null ? (Token[]) params.toArray(new Token[params.size()]) : null;
        }
    }
    
    public boolean isFunctionLike() {
        return params != null;
    }
    
    /**
     * returns List of Tokens of macro body
     */
    public List<Token> getBody() {
        return bodyTokens != null ? bodyTokens : Collections.<Token>emptyList();
    }
    
    /**
     * returns true if #define directive is valid
     */
    public boolean isValid() {
        return state != ERROR;
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
                        params = new ArrayList<Token>();
                        state = IN_PARAMS;
                    } else {
                        if (bodyTokens == null) {
                            bodyTokens = new ArrayList<Token>();
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
                            params.add(APTUtils.VA_ARGS_TOKEN);
                            break;
                        default:
                            // eat comma and comments and leave IN_PARAMS state
                            if (!(token.getType() == APTTokenTypes.COMMA || APTUtils.isCommentToken(token.getType()))) {
                                // error check
                                if (DebugUtils.STANDALONE) {
                                    System.err.printf("line %d: \"%s\" may not appear in macro parameter list\n", // NOI18N
                                            getToken().getLine(), token.getText());
                                } else {
                                    APTUtils.LOG.log(Level.SEVERE, "line {0}: {1} may not appear in macro parameter list", // NOI18N
                                            new Object[] {getToken().getLine(), token.getText()} );
                                }                                
                                state = ERROR;
                            }
                            break;
                    }
                    break;
                }
                case IN_BODY:
                {
                    // init body list if necessary
                    if (bodyTokens == null) {
                        bodyTokens = new ArrayList<Token>();
                    }
                    // check for errors:
                    if (token.getType() == APTTokenTypes.SHARP) {
                        state = IN_BODY_AFTER_SHARP;
                    }
                    bodyTokens.add(token);
                    break;
                }
                case IN_BODY_AFTER_SHARP:
                {
                    bodyTokens.add(token);
                    // skip comments
                    if (APTUtils.isCommentToken(token.getType())) {
                        // stay in the current state
                    } else if (token.getType() == APTTokenTypes.ID) {
                        // error check: token after # must be parameter
                        state = isInParamList(token) ? IN_BODY : ERROR;
                    } else {
                        // only id is accepted after #
                        state = ERROR;
                    }                   
                    if (state == ERROR) {
                        if (DebugUtils.STANDALONE) {
                            System.err.printf("line %d: '#' is not followed by a macro parameter\n", // NOI18N
                                    getToken().getLine());
                        } else {
                            APTUtils.LOG.log(Level.SEVERE, "line {0}: '#' is not followed by a macro parameter", // NOI18N
                                    new Object[] {getToken().getLine()} );
                        }                                
                    }
                    break;
                }
                case ERROR:
                {
                    // eat all after error
                    break;
                }
                default:
                    assert(false) : "unexpected state"; // NOI18N
            }
            return true;
        }
    }
    
    private boolean isInParamList(Token id) {
        assert id != null;
        if (params == null) {
            return false;
        }
        for (Token param : params) {
            if (param.getText().equals(id.getText())) {
                return true;
            }
        }
        return false;
    }
    @Override
    public String getText() {
        String ret = super.getText();
        String paramStr = ""; // NOI18N
        if (params != null) {
            paramStr = "PARAMS{" + APTUtils.toString(new ListBasedTokenStream(this.params)) + "}"; // NOI18N
        }
        String bodyStr;
        if (bodyTokens != null) {
            bodyStr = "BODY{" + APTUtils.toString(new ListBasedTokenStream(getBody())) + "}"; // NOI18N
        } else {
            bodyStr = "{NO BODY}"; // NOI18N
        }
        return ret + paramStr + bodyStr;
    }
}
