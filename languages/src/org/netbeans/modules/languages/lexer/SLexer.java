/*
 * SLanguageProvider.java
 *
 * Created on October 17, 2006, 10:10 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.languages.lexer;

import java.util.Map;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.Language.MethodEvaluator;
import org.netbeans.modules.languages.parser.Input;
import org.netbeans.modules.languages.parser.Parser;
import org.netbeans.modules.languages.parser.SToken;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.TokenFactory;


/**
 *
 * @author Jan Jancura
 */
public class SLexer implements Lexer<STokenId>, Parser.Cookie {
    
    private Language    language;
    private Input       input;
    private TokenFactory tokenFactory;
    private Map         tokensMap;
    private Parser      parser;
    private Integer     state = Integer.valueOf (-1);
    
    
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
        MethodEvaluator evaluator = null;
        token = parser.read(this, input);
        String stateName = parser.getState(state); // [PENDING] improve performance of parser.getState()
        if (language != null) {
            evaluator = (MethodEvaluator) language.getFeature(language.ANALYZE, language.getMimeType(), stateName);
        }
        if (evaluator != null) {
            input.setIndex(index);
            token = (SToken) evaluator.evaluate(new Object[] {this, language, input});
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

    
    // Cookie implementation ...................................................
    
    public int getState () {
        return state.intValue ();
    }

    public void setState (int state) {
        this.state = new Integer (state);
    }
    
    
    // innerclasses ............................................................
    
    private static class InputBridge extends Input {
        
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


