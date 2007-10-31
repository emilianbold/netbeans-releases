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

package org.netbeans.modules.ruby.rhtml.lexer;

import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;
import org.netbeans.modules.ruby.rhtml.*;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Syntax class for RHTML tags, recognizing RHTML delimiters.
 *
 * @author Marek Fukala
 * @author Tor Norbye
 *
 * @version 1.00
 */

public final class RhtmlLexer implements Lexer<RhtmlTokenId> {
    
    private static final int EOF = LexerInput.EOF;
    
    private LexerInput input;
    
    private TokenFactory<RhtmlTokenId> tokenFactory;
    
    public Object state() {
        return state;
    }
    
    //main internal lexer state
    private int state = INIT;
    
    // Internal analyzer states
    private static final int INIT                     = 0;  // initial lexer state = content language
    private static final int ISA_LT                   = 1; // after '<' char
    private static final int ISA_LT_PC                = 2; // after '<%' - comment or directive or scriptlet
    private static final int ISI_SCRIPTLET            = 3; // inside Ruby scriptlet
    private static final int ISI_SCRIPTLET_PC         = 4; // just after % in scriptlet
    private static final int ISI_COMMENT_SCRIPTLET    = 5; // Inside a Ruby comment scriptlet
    private static final int ISI_COMMENT_SCRIPTLET_PC = 6; // just after % in a Ruby comment scriptlet
    private static final int ISI_EXPR_SCRIPTLET       = 7; // inside Ruby expression scriptlet
    private static final int ISI_EXPR_SCRIPTLET_PC    = 8; // just after % in an expression scriptlet
    
    public RhtmlLexer(LexerRestartInfo<RhtmlTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        if (info.state() == null) {
            this.state = INIT;
        } else {
            state = ((Integer) info.state()).intValue();
        }
    }
    
    private Token<RhtmlTokenId> token(RhtmlTokenId id) {
        if(input.readLength() == 0) {
            new Exception("Error - token length is zero!; state = " + state).printStackTrace();
        }
        Token<RhtmlTokenId> t = tokenFactory.createToken(id);
        return t;
    }
    
    public Token<RhtmlTokenId> nextToken() {
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
                    switch (actChar) {
//                        case '\n':
//                            return token(RhtmlTokenId.EOL);
                        case '<':
                            state = ISA_LT;
                            break;
                    }
                    break;
                    
                case ISA_LT:
                    switch (actChar) {
                        case '%':
                            state = ISA_LT_PC;
                            break;
                        default:
                            state = INIT; //just content
//                            state = ISI_TAG_ERROR;
//                            break;
                    }
                    break;
                    
                case ISA_LT_PC:
                    switch (actChar) {
                        case '=': 
                            if(input.readLength() == 3) {
                                // just <%! or <%= read
                                state = ISI_EXPR_SCRIPTLET;
                                return token(RhtmlTokenId.DELIMITER);
                            } else {
                                // RHTML symbol, but we also have content language in the buffer
                                input.backup(3); //backup <%=
                                state = INIT;
                                return token(RhtmlTokenId.HTML); //return CL token
                            }
                        case '#':
                            if(input.readLength() == 3) {
                                // just <%! or <%= read
                                state = ISI_COMMENT_SCRIPTLET;
                                return token(RhtmlTokenId.DELIMITER);
                            } else {
                                //jsp symbol, but we also have content language in the buffer
                                input.backup(3); //backup <%! or <%=
                                state = INIT;
                                return token(RhtmlTokenId.HTML); //return CL token
                            }
                        default:  // RHTML scriptlet delimiter '<%'
                            if(input.readLength() == 3) {
                                // just <% + something != [=,#] read
                                state = ISI_SCRIPTLET;
                                input.backup(1); //backup the third character, it is a part of the java scriptlet
                                return token(RhtmlTokenId.DELIMITER);
                            } else {
                                // RHTML symbol, but we also have content language in the buffer
                                input.backup(3); //backup <%@
                                state = INIT;
                                return token(RhtmlTokenId.HTML); //return CL token
                            }
                    }
                    
                case ISI_COMMENT_SCRIPTLET:
                    switch(actChar) {
                        case '%':
                            if (input.readLength() == 1) {
                                state = ISI_COMMENT_SCRIPTLET_PC;
                                break;
                            } else {
                                input.backup(1);
                                return token(RhtmlTokenId.RUBYCOMMENT);
                            }
                    }
                    break;
                    
                    
                case ISI_SCRIPTLET:
                    switch(actChar) {
                        case '%':
                            if (input.readLength() == 1) {
                                state = ISI_SCRIPTLET_PC;
                                break;
                            } else {
                                input.backup(1);
                                return token(RhtmlTokenId.RUBY);
                            }
                    }
                    break;
                    

                case ISI_SCRIPTLET_PC:
                    switch(actChar) {
                        case '>':
                            if(input.readLength() == 2) {
                                //just the '%>' symbol read
                                state = INIT;
                                return token(RhtmlTokenId.DELIMITER);
                            } else {
                                //return the scriptlet content
                                input.backup(2); // backup '%>' we will read JUST them again
                                state = ISI_SCRIPTLET;
                                return token(RhtmlTokenId.RUBY);
                            }
                        default:
                            state = ISI_SCRIPTLET;
                            break;
                    }
                    break;

                case ISI_EXPR_SCRIPTLET:
                    switch(actChar) {
                        case '%':
                            if (input.readLength() == 1) {
                                state = ISI_EXPR_SCRIPTLET_PC;
                                break;
                            } else {
                                input.backup(1);
                                return token(RhtmlTokenId.RUBY);
                            }
                    }
                    break;
                    

                case ISI_EXPR_SCRIPTLET_PC:
                    switch(actChar) {
                        case '>':
                            if(input.readLength() == 2) {
                                //just the '%>' symbol read
                                state = INIT;
                                return token(RhtmlTokenId.DELIMITER);
                            } else {
                                //return the scriptlet content
                                input.backup(2); // backup '%>' we will read JUST them again
                                state = ISI_EXPR_SCRIPTLET;
                                return token(RhtmlTokenId.RUBY_EXPR);
                            }
                        default:
                            state = ISI_EXPR_SCRIPTLET;
                            break;
                    }
                    break;
                    
                case ISI_COMMENT_SCRIPTLET_PC:
                    switch(actChar) {
                        case '>':
                            if(input.readLength() == 2) {
                                //just the '%>' symbol read
                                state = INIT;
                                return token(RhtmlTokenId.DELIMITER);
                            } else {
                                //return the scriptlet content
                                input.backup(2); // backup '%>' we will read JUST them again
                                state = ISI_COMMENT_SCRIPTLET;
                                return token(RhtmlTokenId.RUBYCOMMENT);
                            }
                        default:
                            state = ISI_COMMENT_SCRIPTLET;
                            break;
                    }
                    break;
            }
            
        }
        
        // At this stage there's no more text in the scanned buffer.
        // Scanner first checks whether this is completely the last
        // available buffer.
        
        switch(state) {
            case INIT:
                if (input.readLength() == 0) {
                    return null;
                } else {
                    return token(RhtmlTokenId.HTML);
                }
            case ISA_LT:
                state = INIT;
                return token(RhtmlTokenId.DELIMITER);
            case ISA_LT_PC:
                state = INIT;
                return token(RhtmlTokenId.DELIMITER);
            case ISI_SCRIPTLET_PC:
                state = INIT;
                return token(RhtmlTokenId.DELIMITER);
            case ISI_SCRIPTLET:
                state = INIT;
                return token(RhtmlTokenId.RUBY);
            case ISI_EXPR_SCRIPTLET_PC:
                state = INIT;
                return token(RhtmlTokenId.DELIMITER);
            case ISI_EXPR_SCRIPTLET:
                state = INIT;
                return token(RhtmlTokenId.RUBY_EXPR);
            case ISI_COMMENT_SCRIPTLET_PC:
                state = INIT;
                return token(RhtmlTokenId.DELIMITER);
            case ISI_COMMENT_SCRIPTLET:
                state = INIT;
                return token(RhtmlTokenId.RUBYCOMMENT);
                
                
            default:
                System.out.println("RhtmlLexer - unhandled state : " + state);   // NOI18N
        }
        
        return null;
        
    }
    
    public void release() {
    }
}
