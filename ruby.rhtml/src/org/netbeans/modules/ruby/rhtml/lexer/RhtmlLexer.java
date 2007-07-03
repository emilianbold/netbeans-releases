/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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
                            state = ISI_COMMENT_SCRIPTLET_PC;
                            break;
                    }
                    break;
                    
                    
                case ISI_SCRIPTLET:
                    switch(actChar) {
                        case '%':
                            state = ISI_SCRIPTLET_PC;
                            break;
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
                            state = ISI_EXPR_SCRIPTLET_PC;
                            break;
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
