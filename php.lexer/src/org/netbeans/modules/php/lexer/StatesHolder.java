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
 * Contributor(s): The Original Software is NetBeans. The Initial
 * Developer of the Original Software is Sun Microsystems, Inc. Portions
 * Copyright 1997-2006 Sun Microsystems, Inc. All Rights Reserved.
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
package org.netbeans.modules.php.lexer;

import org.netbeans.api.lexer.Token;
import org.netbeans.modules.php.lexer.State.States;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * @author ads
 */
final class StatesHolder {

    private StatesHolder() {
    }

    static class AfterFirstPStateStrategy extends AbstractStateStrategy implements
            StateStrategy
    {

        /*
         * (non-Javadoc)
         * 
         * @see org.netbeans.modules.php.lexer.StateStrategy#getState()
         */
        public States getState() {
            return States.ISA_LT_QP;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.netbeans.modules.php.lexer.StateStrategy#getToken(int,
         *      org.netbeans.spi.lexer.LexerInput,
         *      org.netbeans.modules.php.lexer.State,
         *      org.netbeans.spi.lexer.TokenFactory)
         */
        public Token<PhpTokenId> getToken( int currentChar, LexerInput input,
                State state, TokenFactory<PhpTokenId> factory )
        {
            if (currentChar == 'h') {
                state.setCurrentState(States.ISA_LT_QPH);
            }
            else {
                if (input.readLength() == 4) {
                    input.backup(2); // put back 'p@' symbols that are
                    // followed after <?
                    state.setCurrentState(States.ISI_PHP);
                    return token(PhpTokenId.DELIMITER1, input, state
                            .getCurrentState(), factory);
                }
                else {
                    input.backup(4); // put back '<?p@' symbols
                    state.setCurrentState(States.INIT);
                    return token(PhpTokenId.HTML, input, state
                            .getCurrentState(), factory);
                }
            }
            return null;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.php.lexer.StateStrategy#handleFinalState(org.netbeans.spi.lexer.LexerInput, org.netbeans.modules.php.lexer.State, org.netbeans.spi.lexer.TokenFactory)
         */
        public Token<PhpTokenId> handleFinalState( LexerInput input, State state, 
                TokenFactory<PhpTokenId> factory ) 
        {
            state.setCurrentState( States.INIT );
            return token(PhpTokenId.DELIMITER2 , input , state.getCurrentState() 
                    , factory );
        }

    }

    static class AfterLeftAngleBracketStrategy extends AbstractStateStrategy
        implements StateStrategy 
    {

        /*
         * (non-Javadoc)
         * 
         * @see org.netbeans.modules.php.lexer.StateStrategy#getState()
         */
        public States getState() {
            return States.ISA_LT;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.netbeans.modules.php.lexer.StateStrategy#getToken(int,
         *      org.netbeans.spi.lexer.LexerInput,
         *      org.netbeans.modules.php.lexer.State,
         *      org.netbeans.spi.lexer.TokenFactory)
         */
        public Token<PhpTokenId> getToken( int currentChar, LexerInput input,
                State state, TokenFactory<PhpTokenId> factory )
        {
            switch (currentChar) {
                case '%':
                    state.setCurrentState(States.ISA_LT_PC);
                    break;
                case '?':
                    state.setCurrentState(States.ISA_LT_Q);
                    break;
                default:
                    state.setCurrentState(States.INIT); // just content
            }
            return null;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.php.lexer.StateStrategy#handleFinalState(org.netbeans.spi.lexer.LexerInput, org.netbeans.modules.php.lexer.State, org.netbeans.spi.lexer.TokenFactory)
         */
        public Token<PhpTokenId> handleFinalState( LexerInput input, State state, 
                TokenFactory<PhpTokenId> factory ) 
        {
            state.setCurrentState( States.INIT);
            return token(PhpTokenId.DELIMITER , input , state.getCurrentState() , 
                    factory);
        }

    }

    static class AfterLongPhpStartStrategy extends AbstractStateStrategy implements
            StateStrategy
    {

        /*
         * (non-Javadoc)
         * 
         * @see org.netbeans.modules.php.lexer.StateStrategy#getState()
         */
        public States getState() {
            return States.ISA_LT_QPHP;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.netbeans.modules.php.lexer.StateStrategy#getToken(int,
         *      org.netbeans.spi.lexer.LexerInput,
         *      org.netbeans.modules.php.lexer.State,
         *      org.netbeans.spi.lexer.TokenFactory)
         */
        public Token<PhpTokenId> getToken( int currentChar, LexerInput input,
                State state, TokenFactory<PhpTokenId> factory )
        {
            // <?php + some symbol is red. Need to check curent symbol
            if (input.readLength() != 6) {
                input.backup(6); // backup <?php@
                state.setCurrentState(States.INIT);
                return token(PhpTokenId.HTML, input, state.getCurrentState(),
                        factory); // return CL token
            }
            if (charIsAcceptable(currentChar)) {
                state.setCurrentState(States.ISI_PHP);
                input.backup(1); // backup the third character, it is a part
                // of
                // the php
                return token(PhpTokenId.DELIMITER2, input, state
                        .getCurrentState(), factory);
            }
            else {
                state.setCurrentState(States.ISI_PHP);
                input.backup(4); // backup the 'php@' characters
                return token(PhpTokenId.DELIMITER1, input, state
                        .getCurrentState(), factory);
            }
        }

        private boolean charIsAcceptable( int actChar ) {
            return Character.isWhitespace((char)actChar);
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.php.lexer.StateStrategy#handleFinalState(org.netbeans.spi.lexer.LexerInput, org.netbeans.modules.php.lexer.State, org.netbeans.spi.lexer.TokenFactory)
         */
        public Token<PhpTokenId> handleFinalState( LexerInput input, State state, 
                TokenFactory<PhpTokenId> factory ) 
        {
            state.setCurrentState( States.INIT );
            return token(PhpTokenId.DELIMITER2 , input , state.getCurrentState() 
                    , factory );
        }

    }

    static class AfterPhpEndStateStrategy extends AbstractStateStrategy implements
            StateStrategy
    {

        /*
         * (non-Javadoc)
         * 
         * @see org.netbeans.modules.php.lexer.StateStrategy#getState()
         */
        public States getState() {
            return States.ISP_PHP;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.netbeans.modules.php.lexer.StateStrategy#getToken(int,
         *      org.netbeans.spi.lexer.LexerInput,
         *      org.netbeans.modules.php.lexer.State,
         *      org.netbeans.spi.lexer.TokenFactory)
         */
        public Token<PhpTokenId> getToken( int currentChar, LexerInput input,
                State state, TokenFactory<PhpTokenId> factory )
        {
            switch (currentChar) {
                case '>':
                    if (input.readLength() == 2) {
                        // just the '?>' symbol read
                        state.setCurrentState(States.INIT);
                        return token(PhpTokenId.DELIMITER_END, input, state
                                .getCurrentState(), factory);
                    }
                    else {
                        // return the scriptlet content
                        input.backup(2); // backup '?>' we will read JUST
                        // them again
                        state.setCurrentState(States.ISI_PHP);
                        return token(PhpTokenId.PHP, input, state
                                .getCurrentState(), factory);
                    }
                default:
                    state.setCurrentState(States.ISI_PHP);
                    break;
            }
            return null;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.php.lexer.StateStrategy#handleFinalState(org.netbeans.spi.lexer.LexerInput, org.netbeans.modules.php.lexer.State, org.netbeans.spi.lexer.TokenFactory)
         */
        public Token<PhpTokenId> handleFinalState( LexerInput input, State state, 
                TokenFactory<PhpTokenId> factory ) 
        {
            state.setCurrentState( States.INIT );
            return token(PhpTokenId.DELIMITER1 , input , state.getCurrentState() ,
                    factory );
        }

    }

    static class AfterPHStateStrategy extends AbstractStateStrategy implements
            StateStrategy
    {

        /*
         * (non-Javadoc)
         * 
         * @see org.netbeans.modules.php.lexer.StateStrategy#getState()
         */
        public States getState() {
            return States.ISA_LT_QPH;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.netbeans.modules.php.lexer.StateStrategy#getToken(int,
         *      org.netbeans.spi.lexer.LexerInput,
         *      org.netbeans.modules.php.lexer.State,
         *      org.netbeans.spi.lexer.TokenFactory)
         */
        public Token<PhpTokenId> getToken( int currentChar, LexerInput input,
                State state, TokenFactory<PhpTokenId> factory )
        {
            if (currentChar == 'p') {
                state.setCurrentState(States.ISA_LT_QPHP);
            }
            else {
                if (input.readLength() == 5) {
                    input.backup(3); // put back 'ph@' symbols that are
                    // followed after <?
                    state.setCurrentState(States.ISI_PHP);
                    return token(PhpTokenId.DELIMITER1, input, state
                            .getCurrentState(), factory);
                }
                else {
                    input.backup(5); // put back '<?ph@' symbols
                    state.setCurrentState(States.INIT);
                    return token(PhpTokenId.HTML, input, state
                            .getCurrentState(), factory);
                }
            }
            return null;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.php.lexer.StateStrategy#handleFinalState(org.netbeans.spi.lexer.LexerInput, org.netbeans.modules.php.lexer.State, org.netbeans.spi.lexer.TokenFactory)
         */
        public Token<PhpTokenId> handleFinalState( LexerInput input, State state, 
                TokenFactory<PhpTokenId> factory ) 
        {
            state.setCurrentState( States.INIT );
            return token(PhpTokenId.DELIMITER2 , input , state.getCurrentState() 
                    , factory );
        }

    }

    static class AfterScriptletsEndStrategy extends AbstractStateStrategy implements
            StateStrategy
    {

        /*
         * (non-Javadoc)
         * 
         * @see org.netbeans.modules.php.lexer.StateStrategy#getState()
         */
        public States getState() {
            return States.ISP_SCRIPTLET_PC;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.netbeans.modules.php.lexer.StateStrategy#getToken(int,
         *      org.netbeans.spi.lexer.LexerInput,
         *      org.netbeans.modules.php.lexer.State,
         *      org.netbeans.spi.lexer.TokenFactory)
         */
        public Token<PhpTokenId> getToken( int currentChar, LexerInput input,
                State state, TokenFactory<PhpTokenId> factory )
        {
            switch (currentChar) {
                case '>':
                    if (input.readLength() == 2) {
                        // just the '%>' symbol read
                        state.setCurrentState(States.INIT);
                        return token(PhpTokenId.DELIMITER, input, state
                                .getCurrentState(), factory);
                    }
                    else {
                        // return the scriptlet content
                        input.backup(2); // backup '%>' we will read JUST
                        // them again
                        state.setCurrentState(States.ISI_SCRIPTLET);
                        return token(PhpTokenId.PHP, input, state
                                .getCurrentState(), factory);
                    }
                default:
                    state.setCurrentState(States.ISI_SCRIPTLET);
                    break;
            }
            return null;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.php.lexer.StateStrategy#handleFinalState(org.netbeans.spi.lexer.LexerInput, org.netbeans.modules.php.lexer.State, org.netbeans.spi.lexer.TokenFactory)
         */
        public Token<PhpTokenId> handleFinalState( LexerInput input, State state, 
                TokenFactory<PhpTokenId> factory ) 
        {
            state.setCurrentState( States.INIT );
            return token(PhpTokenId.DELIMITER , input , state.getCurrentState() , 
                    factory );
        }

    }

    static class AfterScriptletStateStrategy extends AbstractStateStrategy implements
            StateStrategy
    {

        /*
         * (non-Javadoc)
         * 
         * @see org.netbeans.modules.php.lexer.StateStrategy#getState()
         */
        public States getState() {
            return States.ISA_LT_PC;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.netbeans.modules.php.lexer.StateStrategy#getToken(int,
         *      org.netbeans.spi.lexer.LexerInput,
         *      org.netbeans.modules.php.lexer.State,
         *      org.netbeans.spi.lexer.TokenFactory)
         */
        public Token<PhpTokenId> getToken( int currentChar, LexerInput input,
                State state, TokenFactory<PhpTokenId> factory )
        {
            switch (currentChar) {
                case '=':
                    if (input.readLength() == 3) {
                        // just <%= read
                        state.setCurrentState(States.ISI_SCRIPTLET);
                        return token(PhpTokenId.DELIMITER, input, state
                                .getCurrentState(), factory);
                    }
                    else {
                        // jsp symbol, but we also have content language in the
                        // buffer
                        input.backup(3); // backup <%=
                        state.setCurrentState(States.INIT);
                        return token(PhpTokenId.HTML, input, state
                                .getCurrentState(), factory); // return CL
                        // token
                    }
                default: // scriptlet delimiter '<%'
                    if (input.readLength() == 3) {
                        // just <% + something not '=' read
                        state.setCurrentState(States.ISI_SCRIPTLET);
                        input.backup(1); // backup the third character, it is
                        // a part of the scriptlet
                        return token(PhpTokenId.DELIMITER, input, state
                                .getCurrentState(), factory);
                    }
                    else {
                        // jsp symbol, but we also have content language in the
                        // buffer
                        input.backup(3); // backup <%@
                        state.setCurrentState(States.INIT);
                        return token(PhpTokenId.HTML, input, state
                                .getCurrentState(), factory); // return CL
                        // token
                    }
            }
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.php.lexer.StateStrategy#handleFinalState(org.netbeans.spi.lexer.LexerInput, org.netbeans.modules.php.lexer.State, org.netbeans.spi.lexer.TokenFactory)
         */
        public Token<PhpTokenId> handleFinalState( LexerInput input, State state, 
                TokenFactory<PhpTokenId> factory ) 
        {
            return token(PhpTokenId.DELIMITER , input , state.getCurrentState() , 
                    factory);
        }

    }

    static class AfterShortPhpStartStrategy extends AbstractStateStrategy implements
            StateStrategy
    {

        /*
         * (non-Javadoc)
         * 
         * @see org.netbeans.modules.php.lexer.StateStrategy#getState()
         */
        public States getState() {
            return States.ISA_LT_Q;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.netbeans.modules.php.lexer.StateStrategy#getToken(int,
         *      org.netbeans.spi.lexer.LexerInput,
         *      org.netbeans.modules.php.lexer.State,
         *      org.netbeans.spi.lexer.TokenFactory)
         */
        public Token<PhpTokenId> getToken( int currentChar, LexerInput input,
                State state, TokenFactory<PhpTokenId> factory )
        {
            switch (currentChar) {
                case '=':
                    if (input.readLength() == 3) {
                        // just <?= read
                        state.setCurrentState(States.ISI_PHP);
                        return token(PhpTokenId.DELIMITER1, input, state
                                .getCurrentState(), factory);
                    }
                    else {
                        // <? symbol, but we also have content language in the
                        // buffer
                        input.backup(3); // backup <?=
                        state.setCurrentState(States.INIT);
                        return token(PhpTokenId.HTML, input, state
                                .getCurrentState(), factory); // return CL
                        // token
                    }
                case 'p':
                    state.setCurrentState(States.ISA_LT_QP);
                    break;
                default: // php delimiter '<?'
                    if (input.readLength() == 3) {
                        // just <? + something not '=' read
                        state.setCurrentState(States.ISI_PHP);
                        input.backup(1); // backup the third character, it is
                        // a part of the php
                        return token(PhpTokenId.DELIMITER1, input, state
                                .getCurrentState(), factory);
                    }
                    else {
                        // <? symbol, but we also have content language in the
                        // buffer
                        input.backup(3); // backup <?@
                        state.setCurrentState(States.INIT);
                        return token(PhpTokenId.HTML, input, state
                                .getCurrentState(), factory); // return CL
                        // token
                    }
            }
            return null;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.php.lexer.StateStrategy#handleFinalState(org.netbeans.spi.lexer.LexerInput, org.netbeans.modules.php.lexer.State, org.netbeans.spi.lexer.TokenFactory)
         */
        public Token<PhpTokenId> handleFinalState( LexerInput input, State state, 
                TokenFactory<PhpTokenId> factory ) 
        {
            state.setCurrentState( States.INIT );
            return token(PhpTokenId.DELIMITER1 , input , state.getCurrentState() ,
                    factory );
        }

    }

    static class InitStateStrategy extends AbstractStateStrategy 
        implements StateStrategy 
    {

        /*
         * (non-Javadoc)
         * 
         * @see org.netbeans.modules.php.lexer.StateStrategy#getState()
         */
        public States getState() {
            return States.INIT;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.netbeans.modules.php.lexer.StateStrategy#getToken(int,
         *      org.netbeans.spi.lexer.LexerInput,
         *      org.netbeans.modules.php.lexer.State,
         *      org.netbeans.spi.lexer.TokenFactory)
         */
        public Token<PhpTokenId> getToken( int currentChar, LexerInput input,
                State state, TokenFactory<PhpTokenId> factory )
        {
            if (currentChar == '<') {
                state.setCurrentState(States.ISA_LT);
            }
            return null;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.php.lexer.StateStrategy#handleFinalState(org.netbeans.spi.lexer.LexerInput, org.netbeans.modules.php.lexer.State, org.netbeans.spi.lexer.TokenFactory)
         */
        public Token<PhpTokenId> handleFinalState( LexerInput input, State state, 
                TokenFactory<PhpTokenId> factory ) 
        {
            if (input.readLength() == 0) {
                return null;
            } else {
                return token( PhpTokenId.HTML , input , state.getCurrentState() ,
                        factory );
            }
        }

    }
    
    static class InsidePhpStrategy extends InsidePhpStateStrategy 
    {
    }
    
    static class InsideScriptletStrategy extends AbstractStateStrategy 
        implements StateStrategy 
    {

        /* (non-Javadoc)
         * @see org.netbeans.modules.php.lexer.StateStrategy#getState()
         */
        public States getState() {
            return States.ISI_SCRIPTLET;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.php.lexer.StateStrategy#getToken(int, org.netbeans.spi.lexer.LexerInput, org.netbeans.modules.php.lexer.State, org.netbeans.spi.lexer.TokenFactory)
         */
        public Token<PhpTokenId> getToken( int currentChar, LexerInput input,
                State state, TokenFactory<PhpTokenId> factory )
        {
            if ( currentChar == '%' ) {
                state.setCurrentState( States.ISP_SCRIPTLET_PC ); 
            }
            return null;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.php.lexer.StateStrategy#handleFinalState(org.netbeans.spi.lexer.LexerInput, org.netbeans.modules.php.lexer.State, org.netbeans.spi.lexer.TokenFactory)
         */
        public Token<PhpTokenId> handleFinalState( LexerInput input, State state, 
                TokenFactory<PhpTokenId> factory ) 
        {
            state.setCurrentState( States.INIT );
            return token(PhpTokenId.PHP , input , state.getCurrentState() , 
                    factory);
        }

    }


}
