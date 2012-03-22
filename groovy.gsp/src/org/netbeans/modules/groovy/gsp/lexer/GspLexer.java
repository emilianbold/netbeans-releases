/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.groovy.gsp.lexer;

import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Syntax class for GSP tags, recognizing GSP delimiters.
 *
 * @author Martin Adamek
 * @author Martin Janicek
 */

public final class GspLexer implements Lexer<GspTokenId> {
    
    private static final int EOF = LexerInput.EOF; // -1
    private LexerState state = LexerState.INIT;
    private boolean stateBackup = false;

    
    private enum LexerState {
        // Internal analyzer states
        // The comment 'after something' indicates which characters have been already
        // read (e.g. if we are in JEXPR state, we have already read characters '<%=')
        // The part 'expecting something' shows us what we are expecting as a next char
        // (e.g. in JEXPR state we are waiting for next % character and then we can move
        // to the next valid state)
        INIT,               // nothing read yet, nothing expected
        JEXPR,              // after '<%= ...'      expecting %
        JSCRIPT,            // after '<% ...'       expecting %
        JDIRECT,            // after '<%@ ...'      expecting %
        JDECLAR,            // after '<%! ...'      expecting %
        GEXPR,              // after '${ ...'       expecting }
        GSCRIPT,            // after '%{ ...'       expecting }
        GDIRECT,            // after '@{ ...'       expecting }
        GDECLAR,            // after '!{ ...'       expecting }
        GSTART_TAG,         // after '<g: ...'      expecting /, \, > or $
        GEND_TAG,           // after '</g: ...'     expecting >
        GINDEPENDENT_TAG,   // after '<g: ... /'    expecting >
        GSTART_TAG_BACKSLASH,// after '<g: ... \'    expecting $
        GTAG_EXPR,          // after '<g: ... $'    expecting {
        GTAG_EXPR_PC,       // after '<g: ... ${'   expecting }
        GTAG_BACKSLASH_EXPR,// after '<g: ... \$'   expecting {

        ISA_LT,             // after '<'            expecting %, g or /
        ISA_DL,             // after '$'            expecting {
        ISA_PC,             // after '%'            expecting {
        ISA_AT,             // after '@'            expecting {
        ISA_EX,             // after '!'            expecting {

        ISA_LT_PC,          // after '<%'           expecting =, @, ! or %
        ISA_LT_G,           // after '<g'           expecting :
        ISA_LT_BS,          // after '</'           expecting g
        ISA_LT_BS_G,        // after '</g'          expecting :

        JEXPR_PC,           // after '<%= ... %'    expecting >
        JSCRIPT_PC,         // after '<% ... %'     expecting >
        JDIRECT_PC,         // after '<%@ ... %'    expecting >
        JDECLAR_PC,         // after '<%! ... %'    expecting >
        GSCRIPT_PC,         // after '%{ ... }'     expecting %
        GDECLAR_PC;         // after '!{ ... }'     expecting !
    }

    private LexerInput input;
    private TokenFactory<GspTokenId> tokenFactory;

    @Override
    public Object state() {
        return state;
    }
    
    public GspLexer(LexerRestartInfo<GspTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        if (info.state() == null) {
            state = LexerState.INIT;
        } else {
            state = (LexerState) info.state();
        }
    }
    
    private Token<GspTokenId> token(GspTokenId id) {
        if(input.readLength() == 0) {
            new Exception("Error - token length is zero!; state = " + state).printStackTrace();
        }
        Token<GspTokenId> t = tokenFactory.createToken(id);
        return t;
    }
    
    @Override
    public Token<GspTokenId> nextToken() {
        int actChar;
        while (true) {
            actChar = input.read();
            
            if (actChar == EOF) {
                if(input.readLengthEOF() == 1) {
                    return null; //just EOL is read
                } else {
                    //there is something else in the buffer except EOL
                    //we will return last token now
                    input.backup(1); //backup the EOL, we will return null in next nextToken() call
                    break;
                }
            }

            switch (state) {
                case INIT:
                    if (stateBackup) {
                        stateBackup = false;
                    } else {
                        switch (actChar) {
                            case '<': state = LexerState.ISA_LT; break;
                            case '$': state = LexerState.ISA_DL; break;
                            case '%': state = LexerState.ISA_PC; break;
                            case '@': state = LexerState.ISA_AT; break;
                            case '!': state = LexerState.ISA_EX; break;
                            // Only for HTML elements ending with '>'
                            case '>': state = LexerState.INIT; return token(GspTokenId.HTML);
                        }
                    }
                    break;
                case ISA_LT: // after <
                    switch (actChar) {
                        case '%': state = LexerState.ISA_LT_PC; break; // after <%
                        case 'g': state = LexerState.ISA_LT_G; break; // after <g
                        case '/': state = LexerState.ISA_LT_BS; break; // after </
                        default:
                            input.backup(2);
                            state = LexerState.INIT;
                            stateBackup = true;
                    }
                    break;
                case ISA_DL: // after ${
                    switch (actChar) {
                        case '{': 
                            if (input.readLength() == 2) {
                                state = LexerState.GEXPR;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                input.backup(2);
                                state = LexerState.INIT;
                                return token(GspTokenId.HTML);
                            }
                    }
                    break;
                case ISA_PC: // after %{
                    switch (actChar) {
                        case '{':
                            if (input.readLength() == 2) {
                                state = LexerState.GSCRIPT;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                input.backup(2);
                                state = LexerState.INIT;
                                return token(GspTokenId.HTML);
                            }
                        default:
                            input.backup(1);
                            state = LexerState.INIT; //just content
                    }
                    break;
                case ISA_AT: // after @{
                    switch (actChar) {
                        case '{': state = LexerState.GDIRECT; break;
                    }
                    break;
                case ISA_EX: // after !{
                    switch (actChar) {
                        case '{':
                            if (input.readLength() == 2) {
                                state = LexerState.GDECLAR;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                input.backup(2);
                                state = LexerState.INIT;
                                return token(GspTokenId.HTML);
                            }
                        default:
                            input.backup(1);
                            state = LexerState.INIT; //just content
                    }
                    break;
                case ISA_LT_PC: // after <%
                    switch (actChar) {
                        case '=': 
                            if(input.readLength() == 3) {
                                // after <%=
                                state = LexerState.JEXPR;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                // GSP symbol, but we also have content language in the buffer
                                input.backup(3); //backup <%=
                                state = LexerState.INIT;
                                return token(GspTokenId.HTML); //return CL token
                            }
                        case '@':
                            if(input.readLength() == 3) {
                                // after <%@
                                state = LexerState.JDIRECT;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                // GSP symbol, but we also have content language in the buffer
                                input.backup(3); // backup <%@
                                state = LexerState.INIT;
                                return token(GspTokenId.HTML); //return CL token
                            }
                        case '!':
                            if(input.readLength() == 3) {
                                // after <%!
                                state = LexerState.JDECLAR;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                // GSP symbol, but we also have content language in the buffer
                                input.backup(3); //backup <%!
                                state = LexerState.INIT;
                                return token(GspTokenId.HTML); //return CL token
                            }
                        default:  // GSP scriptlet delimiter '<%'
                            if(input.readLength() == 3) {
                                // just <% + something != [=,#] read
                                state = LexerState.JSCRIPT;
                                input.backup(1); //backup the third character, it is a part of the Groovy scriptlet
                                return token(GspTokenId.DELIMITER);
                            } else {
                                // GSP symbol, but we also have content language in the buffer
                                input.backup(3); //backup <% + something
                                state = LexerState.INIT;
                                return token(GspTokenId.HTML); //return CL token
                            }
                    }
                case ISA_LT_G: // after <g
                    switch (actChar) {
                        case ':':
                            if (input.readLength() == 3) {
                                state = LexerState.GSTART_TAG;
                                break;
                            } else {
                                // GSP symbol, but we also have content language in the buffer
                                input.backup(3); //backup <g:
                                state = LexerState.INIT;
                                return token(GspTokenId.HTML); //return CL token
                            }
                        default:
                            input.backup(3); // return back <g:
                            state = LexerState.INIT;
                            stateBackup = true;
                    }
                    break;
                case ISA_LT_BS : // after </
                    switch (actChar) {
                        case 'g': 
                            state = LexerState.ISA_LT_BS_G;
                            break; // after </g
                        default:
                            input.backup(3);
                            state = LexerState.INIT;
                            stateBackup = true;
                    }
                    break;
                case ISA_LT_BS_G: // after </g
                    switch (actChar) {
                        case ':':
                            if (input.readLength() == 4) {
                                state = LexerState.GEND_TAG;
                                break;
                            } else {
                                input.backup(4); // return back </g:
                                state = LexerState.INIT;
                                return token(GspTokenId.HTML);
                            }
                        default:
                            input.backup(4);
                            state = LexerState.INIT;
                            stateBackup = true;
                    }
                    break;
                case GEXPR: // after ${
                    switch(actChar) {
                        case '}':
                            if(input.readLength() == 1) {
                                //just the '}' symbol read
                                state = LexerState.INIT;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                //return the scriptlet content
                                input.backup(1); // backup '%>' we will read JUST them again
                                state = LexerState.GEXPR;
                                return token(GspTokenId.GROOVY);
                            }
                        default:
                            state = LexerState.GEXPR;
                            break;
                    }
                    break;
                case JEXPR: // <% .... %>
                    switch(actChar) {
                        case '%':
                            state = LexerState.JEXPR_PC;
                            break;
                    }
                    break;
                case JEXPR_PC:
                    switch(actChar) {
                        case '>':
                            if(input.readLength() == 2) {
                                //just the '%>' symbol read
                                state = LexerState.INIT;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                //return the scriptlet content
                                input.backup(2); // backup '%>' we will read JUST them again
                                state = LexerState.JEXPR;
                                return token(GspTokenId.GROOVY);
                            }
                        default:
                            state = LexerState.JEXPR;
                            break;
                    }
                    break;
                case JSCRIPT: // <% .... %>
                    switch(actChar) {
                        case '%':
                            state = LexerState.JSCRIPT_PC;
                            break;
                    }
                    break;
                case JSCRIPT_PC:
                    switch(actChar) {
                        case '>':
                            if(input.readLength() == 2) {
                                //just the '%>' symbol read
                                state = LexerState.INIT;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                //return the scriptlet content
                                input.backup(2); // backup '%>' we will read JUST them again
                                state = LexerState.JSCRIPT;
                                return token(GspTokenId.GROOVY);
                            }
                        default:
                            state = LexerState.JSCRIPT;
                            break;
                    }
                    break;
                case JDIRECT: // <%@ ... %>
                    switch(actChar) {
                        case '%':
                            state = LexerState.JDIRECT_PC;
                            break;
                    }
                    break;
                case JDIRECT_PC:
                    switch(actChar) {
                        case '>':
                            if(input.readLength() == 2) {
                                //just the '%>' symbol read
                                state = LexerState.INIT;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                //return the scriptlet content
                                input.backup(2); // backup '%>' we will read JUST them again
                                state = LexerState.JDIRECT;
                                return token(GspTokenId.GROOVY);
                            }
                        default:
                            state = LexerState.JDIRECT;
                            break;
                    }
                    break;
                case JDECLAR: // <%! ... %>
                    switch(actChar) {
                        case '%':
                            state = LexerState.JDECLAR_PC;
                            break;
                    }
                    break;
                case JDECLAR_PC:
                    switch(actChar) {
                        case '>':
                            if(input.readLength() == 2) {
                                //just the '%>' symbol read
                                state = LexerState.INIT;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                //return the scriptlet content
                                input.backup(2); // backup '%>' we will read JUST them again
                                state = LexerState.JDECLAR;
                                return token(GspTokenId.GROOVY);
                            }
                        default:
                            state = LexerState.JDECLAR;
                            break;
                    }
                    break;
                case GSCRIPT: // %{ ... }%
                    switch(actChar) {
                        case '}':
                            state = LexerState.GSCRIPT_PC;
                            break;
                    }
                    break;
                case GSCRIPT_PC:
                    switch(actChar) {
                        case '%':
                            if(input.readLength() == 2) {
                                //just the '%>' symbol read
                                state = LexerState.INIT;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                //return the scriptlet content
                                input.backup(2); // backup '%>' we will read JUST them again
                                state = LexerState.GSCRIPT;
                                return token(GspTokenId.GROOVY);
                            }
                        default:
                            state = LexerState.GSCRIPT;
                            break;
                    }
                    break;
                case GDIRECT: // @{ ... }
                    switch(actChar) {
                        case '}':
                            if(input.readLength() == 1) {
                                //just the '}' symbol read
                                state = LexerState.INIT;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                //return the scriptlet content
                                input.backup(1); // backup '%>' we will read JUST them again
                                state = LexerState.GDIRECT;
                                return token(GspTokenId.GROOVY);
                            }
                        default:
                            state = LexerState.GDIRECT;
                            break;
                    }
                    break;
                case GDECLAR: // !{ ... }!
                    switch(actChar) {
                        case '}':
                            state = LexerState.GDECLAR_PC;
                            break;
                    }
                    break;
                case GDECLAR_PC:
                    switch(actChar) {
                        case '!':
                            if(input.readLength() == 2) {
                                //just the '%>' symbol read
                                state = LexerState.INIT;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                //return the scriptlet content
                                input.backup(2); // backup '%>' we will read JUST them again
                                state = LexerState.GDECLAR;
                                return token(GspTokenId.GROOVY);
                            }
                        default:
                            state = LexerState.GDECLAR;
                            break;
                    }
                    break;
                case GSTART_TAG: // after <g: 
                    switch(actChar) {
                        case '>':
                            state = LexerState.INIT;
                            return token(GspTokenId.GTAG);
                        case '/':
                            state = LexerState.GINDEPENDENT_TAG;
                            break;
                        case '\\':
                            state = LexerState.GSTART_TAG_BACKSLASH;
                            break;
                        case '$':
                            state = LexerState.GTAG_EXPR;
                            break;
                        default:
                            state = LexerState.GSTART_TAG;
                            break;
                    }
                    break;
                case GEND_TAG: // after </g:
                    switch(actChar) {
                        case '>':
                            //just the '>' symbol read
                            state = LexerState.INIT;
                            return token(GspTokenId.GTAG);
                        default:
                            state = LexerState.GEND_TAG;
                            break;
                    }
                    break;
                case GINDEPENDENT_TAG: // after <g: ... /
                    switch(actChar) {
                        case '>':
                            state = LexerState.INIT;
                            return token(GspTokenId.GTAG);
                        default:
                            state = LexerState.GSTART_TAG;
                            break;
                    }
                    break;
                case GSTART_TAG_BACKSLASH: // after <g: ... \
                    switch (actChar) {
                        case '$':
                            state = LexerState.GTAG_BACKSLASH_EXPR;
                            break;
                        default:
                            state = LexerState.GSTART_TAG;
                            break;
                    }
                    break;
                case GTAG_BACKSLASH_EXPR: // after <g: ...\$
                    switch (actChar) {
                        case '{':
                            if (input.readLength() == 3) {
                                state = LexerState.GTAG_EXPR_PC;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                input.backup(3);
                                state = LexerState.GSTART_TAG;
                                return token(GspTokenId.GTAG);
                            }
                    }
                case GTAG_EXPR: // after <g: ... $
                    switch (actChar) {
                        case '{':
                            if (input.readLength() == 2) {
                                state = LexerState.GTAG_EXPR_PC;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                input.backup(2);
                                state = LexerState.GSTART_TAG;
                                return token(GspTokenId.GTAG);
                            }
                        default:
                            state = LexerState.GSTART_TAG;
                            break;
                    }
                    break;
                case GTAG_EXPR_PC: // after <g: ... ${ or <g: .../${
                    switch(actChar) {
                        case '}':
                            if(input.readLength() == 1) {
                                state = LexerState.GSTART_TAG;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                input.backup(1);
                                state = LexerState.GTAG_EXPR_PC;
                                return token(GspTokenId.GROOVY_EXPR);
                            }
                        default:
                            state = LexerState.GTAG_EXPR_PC;
                            break;
                    }

            }
        }
            
        // At this stage there's no more text in the scanned buffer.
        // Scanner first checks whether this is completely the last
        // available buffer.
        
        switch (state) {
            case INIT:
                if (input.readLength() == 0) {
                    return null;
                } else {
                    return token(GspTokenId.HTML);
                }
//            case HTML:
//                if (input.readLength() == 0) {
//                    return null;
//                } else {
//                    return token(GspTokenId.HTML);
//                }
            case JEXPR: return token(GspTokenId.GROOVY);   // <%= ... %>
            case JSCRIPT: return token(GspTokenId.GROOVY); // <% .... %>
            case JDIRECT: return token(GspTokenId.GROOVY); // <%@ ... %>
            case JDECLAR: return token(GspTokenId.GROOVY); // <%! ... %>
            case GEXPR: return token(GspTokenId.GROOVY);   // ${ ... }
            case GSCRIPT: return token(GspTokenId.GROOVY); // %{ ... }%
            case GDIRECT: return token(GspTokenId.GROOVY); // @{ ... }
            case GDECLAR: return token(GspTokenId.GROOVY); // !{ ... }!
            case GSTART_TAG: return token(GspTokenId.GTAG); // <g:..>
            case GEND_TAG: return token(GspTokenId.GTAG); // </g:..>
            case GINDEPENDENT_TAG: return token(GspTokenId.GTAG); // <g:.. />
            case ISA_LT: return token(GspTokenId.DELIMITER);
            case ISA_DL: return token(GspTokenId.DELIMITER);
            case ISA_PC: return token(GspTokenId.DELIMITER);
            case ISA_AT: return token(GspTokenId.DELIMITER);
            case ISA_EX: return token(GspTokenId.DELIMITER);
            case ISA_LT_PC: return token(GspTokenId.DELIMITER);
            case ISA_LT_G: return token(GspTokenId.DELIMITER);
            case ISA_LT_BS: return token(GspTokenId.DELIMITER);
            case ISA_LT_BS_G: return token(GspTokenId.DELIMITER);
            case JEXPR_PC: return token(GspTokenId.DELIMITER);
            case JSCRIPT_PC: return token(GspTokenId.DELIMITER);
            case JDIRECT_PC: return token(GspTokenId.DELIMITER);
            case JDECLAR_PC: return token(GspTokenId.DELIMITER);
            case GSCRIPT_PC: return token(GspTokenId.DELIMITER);
            case GDECLAR_PC: return token(GspTokenId.DELIMITER);
                
            default:
                System.out.println("GspLexer - unhandled state : " + state);   // NOI18N
        }
        
        return null;
        
    }
    
    @Override
    public void release() {
    }
}
