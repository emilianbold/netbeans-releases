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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.languages.lexer;

import java.util.Map;

import org.netbeans.api.languages.CharInput;
import org.netbeans.api.languages.SToken;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.languages.SToken;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.TokenFactory;
import org.netbeans.modules.languages.Evaluator;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.parser.Parser;


/**
 *
 * @author Jan Jancura
 */
public class SLexer implements Lexer<STokenId>, Parser.Cookie {
    
    private Language        language;
    private CharInput       input;
    private TokenFactory    tokenFactory;
    private Map             tokensMap;
    private Parser          parser;
    
    
    SLexer (
        Language        language, 
        LexerInput      input, 
        TokenFactory    tokenFactory,
        Map             tokensMap,
        Object          state
    ) {
        this.language = language;
        this.input = new InputBridge (input);
        this.tokenFactory = tokenFactory;
        this.tokensMap = tokensMap;
        parser = language.getParser ();
        if (state != null)
            this.state = (Integer) state;
    }
    
    public Token<STokenId> nextToken () {
        if (input.eof ()) return null;
        int index = input.getIndex ();
        SToken token = null;
        Evaluator.Method evaluator = null;
        token = parser.read (this, input);
        String stateName = parser.getState (state); // [PENDING] improve performance of parser.getState()
        if (language != null && properties != null) {
            evaluator = (Evaluator.Method) properties.get ("call");
        }
        if (evaluator != null) {
            input.setIndex (index);
            Object[] r = (Object[]) evaluator.evaluate (new Object[] {input, language.getMimeType ()});
            token = (SToken) r [0];
            setState (((Integer) r [1]).intValue ());
        }
        
        if (token == null) {
            try {
                if (input.getIndex () > (index + 1))
                    input.setIndex (index + 1);
                else
                if (input.getIndex () == index)
                    input.read ();
                return tokenFactory.createToken ((STokenId) tokensMap.get ("error"));
            } catch (AssertionError ex) {
                System.out.println(input.getIndex ());
            }
        }
        if (!tokensMap.containsKey (token.getType ())) {
            System.out.println("SLexer:unknown token: " + token.getType ());
            return null;
        }
        return tokenFactory.createToken ((STokenId) tokensMap.get (token.getType ()));
    }

    public Object state () {
        return state;
    }

    public void release() {
    }

    
    // Cookie implementation ...................................................
    
    private Integer     state = Integer.valueOf (-1);
    private Map         properties;
    
    public int getState () {
        return state.intValue ();
    }

    public void setState (int state) {
        this.state = new Integer (state);
    }

    public void setProperties (Map properties) {
        this.properties = properties;
    }
    
    
    // innerclasses ............................................................
    
    private static class InputBridge extends CharInput {
        
        private LexerInput input;
        private int index = 0;
        
        InputBridge (LexerInput input) {
            this.input = input;
        }
        
        public char read () {
            index++;
            return (char) input.read ();
        }

        public void setIndex (int index) {
            input.backup (this.index - index);
            this.index = index;
        }

        public int getIndex () {
            return index;
        }

        public char next () {
            char ch = (char) input.read ();
            input.backup (1);
            return ch;
        }

        public boolean eof () {
            return next () == (char) input.EOF;
        }

        public String getString (int from, int to) {
            return input.readText ().toString ();
        }

        public String toString () {
            return input.readText ().toString ();
        }
    }
}


