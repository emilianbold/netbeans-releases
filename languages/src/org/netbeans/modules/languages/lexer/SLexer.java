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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.languages.CharInput;
import org.netbeans.api.languages.SToken;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.languages.SToken;
import org.netbeans.modules.languages.parser.Pattern;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.TokenFactory;
import org.netbeans.modules.languages.Evaluator;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.parser.Parser;
import org.netbeans.spi.lexer.TokenPropertyProvider;
import org.openide.ErrorManager;


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
    private Object          state;
    
    
    SLexer (
        Language        language, 
        LexerInput      input, 
        TokenFactory    tokenFactory,
        Map             tokensMap,
        Object          state
    ) {
        this.language = language;
        this.input = createInputBridge (input, language);
        this.tokenFactory = tokenFactory;
        this.tokensMap = tokensMap;
        this.state = state;
        parser = language.getParser ();
    }
    
    public Token<STokenId> nextToken () {
        if (state instanceof Marenka) {
            return createToken ((Marenka) state);
        }
        if (input.eof ()) return null;
        int index = input.getIndex ();
        SToken token = null;
        Evaluator.Method evaluator = null;
        token = parser.read (this, input);
        if (language != null && properties != null) {
            evaluator = (Evaluator.Method) properties.get ("call");
        }
        if (evaluator != null) {
            input.setIndex (index);
            Object[] r = (Object[]) evaluator.evaluate (new Object[] {input});
            token = (SToken) r [0];
            if (r [1] != null)
                setState (((Integer) r [1]).intValue ());
        }
        
        if (token == null) {
            try {
                if (input.getIndex () > (index + 1))
                    input.setIndex (index + 1);
                else
                if (input.getIndex () == index)
                    input.read ();
                return createToken ("error", index);
            } catch (AssertionError ex) {
                ErrorManager.getDefault ().notify (ex);
                System.out.println(input.getIndex ());
            }
        }
        if (!tokensMap.containsKey (token.getType ())) {
            System.out.println("SLexer:unknown token: " + token.getType ());
            return null;
        }
        return createToken (token.getType (), index);
    }

    public Object state () {
        return state;
    }

    public void release() {
    }

    
    // Cookie implementation ...................................................
    
    private Map         properties;
    
    public int getState () {
        if (state == null) return -1;
        return ((Integer) state).intValue ();
    }

    public void setState (int state) {
        this.state = new Integer (state);
    }

    public void setProperties (Map properties) {
        this.properties = properties;
    }
    
    
    // other methods ...........................................................
    
    private static CharInput createInputBridge (
        LexerInput input, 
        Language language
    ) {
        Pattern start = null, end = null;
        String tokenType = null;
        Map m = language.getFeature (Language.IMPORT);
        if (m != null) {
            Iterator it = m.keySet ().iterator ();
            while (it.hasNext ()) {
                String name = (String) it.next ();
                Map properties = (Map) m.get (name);
                if (!properties.containsKey ("start"))
                    continue;
                start = (Pattern) properties.get ("start");
                end = (Pattern) properties.get ("end");
                tokenType = name;
            }
        }
        if (start == null) 
            return new InputBridge (input);
        return new DelegatingInputBridge (
            new InputBridge (input),
            start,
            end,
            tokenType
        );
    }
    
    private Token createToken (String type, int start) {
        if (!(input instanceof DelegatingInputBridge))
            return tokenFactory.createToken ((STokenId) tokensMap.get (type));
        List embeddings = ((DelegatingInputBridge) input).getEmbeddings ();
        if (embeddings.isEmpty ())
            return tokenFactory.createToken ((STokenId) tokensMap.get (type));
        Marenka marenka = new Marenka ((Integer) state);
        String property = "S";
        Iterator it = embeddings.iterator ();
        while(it.hasNext ()) {
            Vojta v = (Vojta) it.next ();
            if (start < v.startOffset) {
                marenka.add (new Vojta (type, start, v.startOffset, property));
                property = "C";
            }
            marenka.add (v);
            start = v.endOffset;
        }
        if (start < input.getIndex ())
            marenka.add (new Vojta (type, start, input.getIndex (), property));
        return createToken (marenka);
    }
    
    private Token createToken (Marenka marenka) {
        Vojta v = marenka.removeFirst ();
        input.setIndex (v.endOffset);
        if (marenka.isEmpty ())
            this.state = marenka.getState ();
        else
            this.state = marenka;
        return tokenFactory.createPropertyToken (
            (STokenId) tokensMap.get (v.type),
            v.endOffset - v.startOffset,
            tokenPropertyProvider,
            v.property
        );
    }
    
    private static TokenPropertyProvider tokenPropertyProvider = new TokenPropertyProvider () {
        
        public Object getValue (Token token, Object key) {
            return null;
        }

        public Object getValue (Token token, Object tokenStoreKey, Object tokenStoreValue) {
            if (tokenStoreKey.equals ("type"))
                return tokenStoreValue;
            return null;
        }

        public Object tokenStoreKey() {
            return "type";
        }
    };
    
    
    // innerclasses ............................................................
    
    static class Vojta {
        
        String      type;
        int         startOffset;
        int         endOffset;
        Object      property;
        
        Vojta (
            String  type, 
            int     startOffset, 
            int     endOffset,
            Object  property
        ) {
            this.type =         type;
            this.startOffset =  startOffset;
            this.endOffset =    endOffset;
            this.property =     property;
        }
        
        int size () {
            return endOffset - startOffset;
        }
    }
    
    static class Marenka {
        
        Integer state;
        LinkedList vojta = new LinkedList ();
        
        Marenka (Integer state) {
            this.state = state;
        }
        
        void add (Vojta vojta) {
            this.vojta.add (vojta);
        }
        
        Vojta removeFirst () {
            return (Vojta) vojta.removeFirst ();
        }
        
        boolean isEmpty () {
            return vojta.isEmpty ();
        }
        
        Integer getState () {
            return state;
        }
    }
}


