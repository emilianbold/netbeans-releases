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
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.languages.LanguagesManager;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManagerImpl;
import org.netbeans.modules.languages.parser.Pattern;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;
import org.netbeans.modules.languages.Evaluator;
import org.netbeans.modules.languages.parser.Parser;
import org.netbeans.spi.lexer.TokenPropertyProvider;
import org.openide.ErrorManager;


/**
 *
 * @author Jan Jancura
 */
public class SLexer implements Lexer<STokenId>, Parser.Cookie {
    
    private Language                language;
    private CharInput               input;
    private TokenFactory            tokenFactory;
    private Map<String,STokenId>    tokensMap;
    private Parser                  parser;
    private Object                  state;
    
    
    SLexer (
        Language                    language, 
        Map<String,STokenId>        tokensMap,
        LexerRestartInfo<STokenId>  info
    ) {
        this.language = language;
        this.tokenFactory = info.tokenFactory ();
        this.tokensMap = tokensMap;
        this.state = info.state ();
        parser = language.getParser ();
        String outerMimeType = info.languagePath ().language (0).mimeType ();
        try {
            Language outerLanguage = ((LanguagesManagerImpl) LanguagesManager.getDefault ()).
                getLanguage (outerMimeType);
            this.input = createInputBridge (info.input (), outerLanguage);
        } catch (ParseException ex) {
            this.input = createInputBridge (info.input (), new Language (outerMimeType));
        }
    }
    
    public Token<STokenId> nextToken () {
        Token t = nextTokenIn ();
//        if (t == null)
//            System.out.println("nextToken (" + language.getMimeType () + "): null");
//        else
//            System.out.println("nextToken (" + language.getMimeType () + "): " + t.id ().name ());
        return t;
    }
    
    private Token<STokenId> nextTokenIn () {
        if (state instanceof Marenka) {
            return createToken ((Marenka) state);
        }
        int index = input.getIndex ();
        if (input.eof ()) 
            return createToken (index);
        ASTToken token = null;
        Evaluator.Method evaluator = null;
        token = parser.read (this, input, language.getMimeType ());
        if (language != null && properties != null) {
            evaluator = (Evaluator.Method) properties.get ("call");
        }
        if (evaluator != null) {
            input.setIndex (index);
            Object[] r = (Object[]) evaluator.evaluate (new Object[] {input});
            token = (ASTToken) r [0];
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
        Map properties = (Map) language.getFeature (Language.IMPORT, Language.PREPROCESSOR_IMPORT);
        if (properties != null) {
            return new DelegatingInputBridge (
                new InputBridge (input),
                (Pattern) properties.get ("start"),
                (Pattern) properties.get ("end"),
                "PE"//(String) properties.get ("token")
            );
        }
        return new InputBridge (input);
    }
    
    private Token createToken (String type, int start) {
        STokenId tokenId = tokensMap.get (type);
        assert tokenId != null : "Unknown type " + type;
        if (!(input instanceof DelegatingInputBridge)) {
            return tokenFactory.createToken (tokenId);
        }
        List embeddings = ((DelegatingInputBridge) input).getEmbeddings ();
        if (embeddings.isEmpty ())
            return tokenFactory.createToken (tokenId);
        Map imports = (Map) language.getFeature (Language.IMPORT, Language.TOKEN_IMPORT);
        if (imports != null && 
            imports.containsKey (type)
        )   // no preprocessor imports in token import.
            return tokenFactory.createToken (tokenId);
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
    
    private Token createToken (int start) {
        if (!(input instanceof DelegatingInputBridge)) {
            return null;
        }
        List embeddings = ((DelegatingInputBridge) input).getEmbeddings ();
        if (embeddings.isEmpty ())
            return null;
        Marenka marenka = new Marenka ((Integer) state);
        String property = "S";
        Iterator it = embeddings.iterator ();
        while(it.hasNext ()) {
            Vojta v = (Vojta) it.next ();
            assert start == v.startOffset;
            marenka.add (v);
            start = v.endOffset;
        }
        assert start == input.getIndex ();
        return createToken (marenka);
    }
    
    private Token createToken (Marenka marenka) {
        Vojta v = marenka.removeFirst ();
        STokenId tokenId = tokensMap.get (v.type);
        assert tokenId != null : "Unknown type " + v.type;
        input.setIndex (v.endOffset);
        if (marenka.isEmpty ())
            this.state = marenka.getState ();
        else
            this.state = marenka;
        //S ystem.out.println("nextToken <" + v.type + "," + e (input.getString (v.startOffset, v.endOffset)) + "," + v.startOffset + "," + v.endOffset);
        if (v.property instanceof TokenProperties)
            return tokenFactory.createPropertyToken (
                tokenId,
                v.endOffset - v.startOffset,
                (TokenProperties) v.property,
                PartType.COMPLETE
            );
        else
            return tokenFactory.createPropertyToken (
                tokenId,
                v.endOffset - v.startOffset,
                new TokenPropProvider(v.property),
                PartType.COMPLETE
            );
    }
        
    private static String e (CharSequence t) {
        StringBuilder sb = new StringBuilder ();
        int i, k = t.length ();
        for (i = 0; i < k; i++) {
            if (t.charAt (i) == '\t')
                sb.append ("\\t");
            else
            if (t.charAt (i) == '\r')
                sb.append ("\\r");
            else
            if (t.charAt (i) == '\n')
                sb.append ("\\n");
            else
                sb.append (t.charAt (i));
        }
        return sb.toString ();
    }
    
    
    // innerclasses ............................................................
    
    private static final class TokenPropProvider implements TokenPropertyProvider {
        
        private final Object value;
        
        TokenPropProvider(Object value) {
            this.value = value;
        }
        
        public Object getValue (Token token, Object key) {
            if ("type".equals(key))
                return value;
            return null;
        }

    }
    
    static class TokenProperties implements TokenPropertyProvider {
        
        private String      type;
        private int         startSkipLength;
        private int         endSkipLength;
        
        TokenProperties (
            String          type,
            int             startSkipLength,
            int             endSkipLength
        ) {
            this.type =     type;
            this.startSkipLength = startSkipLength;
            this.endSkipLength = endSkipLength;
        }
        
        public Object getValue (Token token, Object key) {
            if ("type".equals (key)) return type;
            if ("startSkipLength".equals (key)) return new Integer (startSkipLength);
            if ("endSkipLength".equals (key)) return new Integer (endSkipLength);
            return null;
        }

    };
    
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


