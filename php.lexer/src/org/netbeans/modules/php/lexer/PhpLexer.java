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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.php.lexer;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.netbeans.api.lexer.Token;
import org.netbeans.modules.php.lexer.State.States;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;


/**
 * @author ads
 *
 */
class PhpLexer implements Lexer<PhpTokenId> {
    
    PhpLexer(LexerRestartInfo<PhpTokenId> info) {
        myInput = info.input();
        myTokenFactory = info.tokenFactory();
        myState = new State();
        if (info.state() == null) {
            myState.setCurrentState( States.INIT );
        } else {
            myState.setCurrentState((States)info.state());
        }
    }
    
    public Object state() {
        return myState.getCurrentState();
    }
    
    public Token<PhpTokenId> nextToken() {
        int actChar;
        while (true) {
            actChar = myInput.read();
            
            if (actChar == LexerInput.EOF) {
                if(myInput.readLengthEOF() == 1) {
                    return null; //just EOL is read
                } else {
                    //there is something else in the buffer except EOL
                    //we will return last token now
                    myInput.backup(1); //backup the EOL, we will return null in next nextToken() call
                    break;
                }
            }
            
            StateStrategy strategy = STRATEGIES.get( myState.getCurrentState() );
            assert strategy!= null : "Not found strategy for state :" + 
                myState.getCurrentState();
            Token<PhpTokenId> token = strategy.getToken(actChar, myInput, 
                    myState, myTokenFactory);
            if ( token != null ) {
                return token;
            }
            
        }
        
        // At this stage there's no more text in the scanned buffer.
        // Scanner first checks whether this is completely the last
        // available buffer.
        
        StateStrategy strategy = STRATEGIES.get( myState.getCurrentState() );
        assert strategy!= null : "Not found strategy for state :" + 
            myState.getCurrentState();
        Token<PhpTokenId> token =  strategy.handleFinalState(myInput, 
                myState, myTokenFactory);
        if ( token != null ) {
            return token;
        }
        return null;
        
    }
    
    public void release() {
    }
    
    private static void init() {
        STRATEGIES = new HashMap<States, StateStrategy>();
        Class[] classes = StatesHolder.class.getDeclaredClasses();
        for (Class<?> clazz : classes) {
            if ( StateStrategy.class.isAssignableFrom(clazz)) {
                try {
                    int modifiers = clazz.getModifiers();
                    boolean isAbtract = Modifier.isAbstract( modifiers );
                    if ( isAbtract || clazz.isInterface()) {
                        continue;
                    }
                    StateStrategy strategy =
                        (StateStrategy) clazz.newInstance();
                    States state = strategy.getState();

                    STRATEGIES.put( state ,  strategy );
                }
                catch (InstantiationException e) {
                    throw new RuntimeException("Couldn't instantiate factory " +
                            "class "+clazz.getCanonicalName());         // NOI18N
                }
                catch (IllegalAccessException e) {
                    assert false;
                }
            }
        }
    }

    private static Map<States, StateStrategy> STRATEGIES; 
    
    static {
        init();
    }
    
    private LexerInput myInput;
    
    private TokenFactory<PhpTokenId> myTokenFactory;
    
    //main internal lexer state
    private State myState;

}
