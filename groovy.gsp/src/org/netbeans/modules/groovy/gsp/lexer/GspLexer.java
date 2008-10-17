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

package org.netbeans.modules.groovy.gsp.lexer;

import org.netbeans.modules.groovy.gsp.lexer.GspTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Syntax class for GSP tags, recognizing GSP delimiters.
 *
 * @todo now handling only GEXPR and not GTAG_EXPR, clear this
 * 
 * @author Martin Adamek
 */

public final class GspLexer implements Lexer<GspTokenId> {
    
    private static final int EOF = LexerInput.EOF; // -1
    
    //main internal lexer state
    private int state = INIT;
    
    private boolean stateBackup = false;

    // Internal analyzer states
    private static final int INIT = 0;
    private static final int JEXPR = 1;   // <%= ... %>
    private static final int JSCRIPT = 2; // <% .... %>
    private static final int JDIRECT = 3; // <%@ ... %>
    private static final int JDECLAR = 4; // <%! ... %>
    private static final int GEXPR = 11;   // ${ ... }
    private static final int GSCRIPT = 12; // %{ ... }%
    private static final int GDIRECT = 13; // @{ ... }
    private static final int GDECLAR = 14; // !{ ... }!
//    private static final int GSTART_TAG = 15; // <g:..>
//    private static final int GEND_TAG = 16; // </g:..>
    private static final int GTAG_EXPR = 17; // ${..}

    private static final int ISA_LT                   = 21; // after '<' char
    private static final int ISA_DL                   = 22; // after '$' char
    private static final int ISA_PC                   = 23; // after '%' char
    private static final int ISA_AT                   = 24; // after '@' char
    private static final int ISA_EX                   = 25; // after '!' char
    
    private static final int ISA_LT_PC                = 31; // after '<%'
//    private static final int ISA_LT_G                 = 32; // after '<g'
    private static final int ISA_LT_BS                = 33; // after '</'

//    private static final int ISA_LT_BS_G              = 41; // after '</g'
    
    private static final int JEXPR_PC = 101;
    private static final int JSCRIPT_PC = 102;
    private static final int JDIRECT_PC = 103;
    private static final int JDECLAR_PC = 104;
    private static final int GSCRIPT_PC = 112;
    private static final int GDIRECT_PC = 113;
    private static final int GDECLAR_PC = 114;
    private static final int GSTART_TAG_PC = 115;
    private static final int GEND_TAG_PC = 116;
    
    private LexerInput input;
    private TokenFactory<GspTokenId> tokenFactory;

    public Object state() {
        return state;
    }
    
    public GspLexer(LexerRestartInfo<GspTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        if (info.state() == null) {
            this.state = INIT;
        } else {
            state = ((Integer) info.state()).intValue();
        }
    }
    
    private Token<GspTokenId> token(GspTokenId id) {
        if(input.readLength() == 0) {
            new Exception("Error - token length is zero!; state = " + state).printStackTrace();
        }
        Token<GspTokenId> t = tokenFactory.createToken(id);
        return t;
    }
    
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
                            case '<': state = ISA_LT; break;
                            case '$': state = ISA_DL; break;
                            case '%': state = ISA_PC; break;
                            case '@': state = ISA_AT; break;
                            case '!': state = ISA_EX; break;
                        }
                    }
                    break;
                case ISA_LT: // after <
                    switch (actChar) {
                        case '%': state = ISA_LT_PC; break; // after <%
//                        case 'g': state = ISA_LT_G; break; // after <g
                        case '/': state = ISA_LT_BS; break; // after </
                        default:
                            input.backup(2);
                            state = INIT;
                            stateBackup = true;
                    }
                    break;
                case ISA_DL: // after ${
                    switch (actChar) {
                        case '{': 
                            if (input.readLength() == 2) {
                                state = GEXPR;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                input.backup(2);
                                state = INIT;
                                return token(GspTokenId.HTML);
                            }
                    }
                    break;
                case ISA_PC: // after %{
                    switch (actChar) {
                        case '{':
                            if (input.readLength() == 2) {
                                state = GSCRIPT;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                input.backup(2);
                                state = INIT;
                                return token(GspTokenId.HTML);
                            }
                        default:
                            input.backup(1);
                            state = INIT; //just content
                    }
                    break;
                case ISA_AT: // after @{
                    switch (actChar) {
                        case '{': state = GDIRECT; break;
                    }
                    break;
                case ISA_EX: // after !{
                    switch (actChar) {
                        case '{':
                            if (input.readLength() == 2) {
                                state = GDECLAR;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                input.backup(2);
                                state = INIT;
                                return token(GspTokenId.HTML);
                            }
                        default:
                            input.backup(1);
                            state = INIT; //just content
                    }
                    break;
                case ISA_LT_PC: // after <%
                    switch (actChar) {
                        case '=': 
                            if(input.readLength() == 3) {
                                // after <%=
                                state = JEXPR;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                // GSP symbol, but we also have content language in the buffer
                                input.backup(3); //backup <%=
                                state = INIT;
                                return token(GspTokenId.HTML); //return CL token
                            }
                        case '@':
                            if(input.readLength() == 3) {
                                // after <%@
                                state = JDIRECT;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                // GSP symbol, but we also have content language in the buffer
                                input.backup(3); // backup <%=
                                state = INIT;
                                return token(GspTokenId.HTML); //return CL token
                            }
                        case '!':
                            if(input.readLength() == 3) {
                                // after <%!
                                state = JDECLAR;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                // GSP symbol, but we also have content language in the buffer
                                input.backup(3); //backup <%!
                                state = INIT;
                                return token(GspTokenId.HTML); //return CL token
                            }
                        default:  // GSP scriptlet delimiter '<%'
                            if(input.readLength() == 3) {
                                // just <% + something != [=,#] read
                                state = JSCRIPT;
                                input.backup(1); //backup the third character, it is a part of the Groovy scriptlet
                                return token(GspTokenId.DELIMITER);
                            } else {
                                // GSP symbol, but we also have content language in the buffer
                                input.backup(3); //backup <%@
                                state = INIT;
                                return token(GspTokenId.HTML); //return CL token
                            }
                    }
//                case ISA_LT_G: // after <g
//                    switch (actChar) {
//                        case ':':
//                            if (input.readLength() == 3) {
//                                state = GSTART_TAG;
//                                break;
//                            } else {
//                                // GSP symbol, but we also have content language in the buffer
//                                input.backup(3); //backup <g:
//                                state = INIT;
//                                return token(GspTokenId.HTML); //return CL token
//                            }
//                        default:
//                            input.backup(3); // return back <g:
//                            state = INIT;
//                            stateBackup = true;
//                    }
//                    break;
                case ISA_LT_BS : // after </
                    switch (actChar) {
//                        case 'g': state = ISA_LT_BS_G; break; // after </g
                        default:
                            input.backup(3);
                            state = INIT;
                            stateBackup = true;
                    }
                    break;
//                case ISA_LT_BS_G: // after </g:
//                    switch (actChar) {
//                        case ':':
//                            if (input.readLength() == 4) {
//                                state = GEND_TAG;
//                                break;
//                            } else {
//                                input.backup(4); // return back </g:
//                                state = INIT;
//                                return token(GspTokenId.HTML);
//                            }
//                        default:
//                            input.backup(4);
//                            state = INIT;
//                            stateBackup = true;
//                    }
//                    break;
                case GEXPR: // after ${
                    switch(actChar) {
                        case '}':
                            if(input.readLength() == 1) {
                                //just the '}' symbol read
                                state = INIT;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                //return the scriptlet content
                                input.backup(1); // backup '%>' we will read JUST them again
                                state = GEXPR;
                                return token(GspTokenId.GROOVY);
                            }
                        default:
                            state = GEXPR;
                            break;
                    }
                    break;
                case JEXPR: // <% .... %>
                    switch(actChar) {
                        case '%':
                            state = JEXPR_PC;
                            break;
                    }
                    break;
                case JEXPR_PC:
                    switch(actChar) {
                        case '>':
                            if(input.readLength() == 2) {
                                //just the '%>' symbol read
                                state = INIT;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                //return the scriptlet content
                                input.backup(2); // backup '%>' we will read JUST them again
                                state = JEXPR;
                                return token(GspTokenId.GROOVY);
                            }
                        default:
                            state = JEXPR;
                            break;
                    }
                    break;
                case JSCRIPT: // <% .... %>
                    switch(actChar) {
                        case '%':
                            state = JSCRIPT_PC;
                            break;
                    }
                    break;
                case JSCRIPT_PC:
                    switch(actChar) {
                        case '>':
                            if(input.readLength() == 2) {
                                //just the '%>' symbol read
                                state = INIT;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                //return the scriptlet content
                                input.backup(2); // backup '%>' we will read JUST them again
                                state = JSCRIPT;
                                return token(GspTokenId.GROOVY);
                            }
                        default:
                            state = JSCRIPT;
                            break;
                    }
                    break;
                case JDIRECT: // <%@ ... %>
                    switch(actChar) {
                        case '%':
                            state = JDIRECT_PC;
                            break;
                    }
                    break;
                case JDIRECT_PC:
                    switch(actChar) {
                        case '>':
                            if(input.readLength() == 2) {
                                //just the '%>' symbol read
                                state = INIT;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                //return the scriptlet content
                                input.backup(2); // backup '%>' we will read JUST them again
                                state = JDIRECT;
                                return token(GspTokenId.GROOVY);
                            }
                        default:
                            state = JDIRECT;
                            break;
                    }
                    break;
                case JDECLAR: // <%! ... %>
                    switch(actChar) {
                        case '%':
                            state = JDECLAR_PC;
                            break;
                    }
                    break;
                case JDECLAR_PC:
                    switch(actChar) {
                        case '>':
                            if(input.readLength() == 2) {
                                //just the '%>' symbol read
                                state = INIT;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                //return the scriptlet content
                                input.backup(2); // backup '%>' we will read JUST them again
                                state = JDECLAR;
                                return token(GspTokenId.GROOVY);
                            }
                        default:
                            state = JDECLAR;
                            break;
                    }
                    break;
                case GSCRIPT: // %{ ... }%
                    switch(actChar) {
                        case '}':
                            state = GSCRIPT_PC;
                            break;
                    }
                    break;
                case GSCRIPT_PC:
                    switch(actChar) {
                        case '%':
                            if(input.readLength() == 2) {
                                //just the '%>' symbol read
                                state = INIT;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                //return the scriptlet content
                                input.backup(2); // backup '%>' we will read JUST them again
                                state = GSCRIPT;
                                return token(GspTokenId.GROOVY);
                            }
                        default:
                            state = GSCRIPT;
                            break;
                    }
                    break;
                case GDIRECT: // @{ ... }
                    switch(actChar) {
                        case '}':
                            if(input.readLength() == 1) {
                                //just the '}' symbol read
                                state = INIT;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                //return the scriptlet content
                                input.backup(1); // backup '%>' we will read JUST them again
                                state = GDIRECT;
                                return token(GspTokenId.GROOVY);
                            }
                        default:
                            state = GDIRECT;
                            break;
                    }
                    break;
                case GDECLAR: // !{ ... }!
                    switch(actChar) {
                        case '}':
                            state = GDECLAR_PC;
                            break;
                    }
                    break;
                case GDECLAR_PC:
                    switch(actChar) {
                        case '!':
                            if(input.readLength() == 2) {
                                //just the '%>' symbol read
                                state = INIT;
                                return token(GspTokenId.DELIMITER);
                            } else {
                                //return the scriptlet content
                                input.backup(2); // backup '%>' we will read JUST them again
                                state = GDECLAR;
                                return token(GspTokenId.GROOVY);
                            }
                        default:
                            state = GDECLAR;
                            break;
                    }
                    break;
//                case GSTART_TAG: // <g:..>
//                    switch(actChar) {
//                        case '>':
//                                //just the '>' symbol read
//                                state = INIT;
//                                return token(GspTokenId.GTAG);
//                        default:
//                            state = GSTART_TAG;
//                            break;
//                    }
//                    break;
//                case GEND_TAG: // </g:..>
//                    switch(actChar) {
//                        case '>':
//                                //just the '>' symbol read
//                                state = INIT;
//                                return token(GspTokenId.GTAG);
//                        default:
//                            state = GEND_TAG;
//                            break;
//                    }
//                    break;
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
//            case GSTART_TAG: return token(GspTokenId.GTAG); // <g:..>
//            case GEND_TAG: return token(GspTokenId.GTAG); // </g:..>
            case GTAG_EXPR: return token(GspTokenId.GROOVY); // ${..}
            case ISA_LT: return token(GspTokenId.DELIMITER);
            case ISA_DL: return token(GspTokenId.DELIMITER);
            case ISA_PC: return token(GspTokenId.DELIMITER);
            case ISA_AT: return token(GspTokenId.DELIMITER);
            case ISA_EX: return token(GspTokenId.DELIMITER);
            case ISA_LT_PC: return token(GspTokenId.DELIMITER);
//            case ISA_LT_G: return token(GspTokenId.DELIMITER);
            case ISA_LT_BS: return token(GspTokenId.DELIMITER);
//            case ISA_LT_BS_G: return token(GspTokenId.DELIMITER);
            case JEXPR_PC: return token(GspTokenId.DELIMITER);
            case JSCRIPT_PC: return token(GspTokenId.DELIMITER);
            case JDIRECT_PC: return token(GspTokenId.DELIMITER);
            case JDECLAR_PC: return token(GspTokenId.DELIMITER);
            case GSCRIPT_PC: return token(GspTokenId.DELIMITER);
            case GDIRECT_PC: return token(GspTokenId.DELIMITER);
            case GDECLAR_PC: return token(GspTokenId.DELIMITER);
            case GSTART_TAG_PC: return token(GspTokenId.DELIMITER);
            case GEND_TAG_PC: return token(GspTokenId.DELIMITER);
                
            default:
                System.out.println("GspLexer - unhandled state : " + state);   // NOI18N
        }
        
        return null;
        
    }
    
    public void release() {
    }

}
