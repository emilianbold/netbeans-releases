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

package org.netbeans.modules.languages;

import java.awt.Point;
import java.io.InputStream;
import java.util.List;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.CharInput;
import org.netbeans.api.languages.TokenInput;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.modules.languages.Feature.Type;
import org.netbeans.modules.languages.parser.Pattern;
import org.netbeans.modules.languages.parser.StringInput;
import org.netbeans.modules.languages.parser.TokenInputUtils;
import org.openide.filesystems.FileObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Collections;
import java.util.Iterator;
 
    
/**
 *
 * @author Jan Jancura
 */
public class NBSLanguageReader {
    
    public static Language readLanguage (
        FileObject  fo, 
        String      mimeType
    ) throws ParseException, IOException {
        return readLanguage (fo.getInputStream (), fo.getPath (), mimeType);
    }
    
    public static Language readLanguage (
        InputStream is, 
        String      sourceName, 
        String      mimeType
    ) throws ParseException, IOException {
        BufferedReader reader = null;
        try {
            InputStreamReader r = new InputStreamReader (is);
            reader = new BufferedReader (r);
            StringBuilder sb = new StringBuilder ();
            String line = reader.readLine ();
            while (line != null) {
                sb.append (line).append ('\n');
                line = reader.readLine ();
            }
            return readLanguage (sb.toString (), sourceName, mimeType);
        } finally {
            if (reader != null)
                reader.close ();
        }
    }
    
    public static Language readLanguage (
        String      source, 
        String      sourceName, 
        String      mimeType
    ) throws ParseException {
        CharInput input = new StringInput (source);
        Language language = new Language (mimeType);
        ASTNode node = null;
        TokenInput tokenInput = null;
        try {
            Language nbsLanguage = getNBSLanguage ();
            tokenInput = TokenInputUtils.create (
                mimeType,
                nbsLanguage.getParser (), 
                input, 
                Collections.EMPTY_SET
            );
            node = nbsLanguage.getAnalyser ().read (tokenInput, false, new boolean[] {false});
            if (node == null) 
                System.out.println ("Can not parse " + sourceName);
            else
            if (node.getChildren ().isEmpty ())
                System.out.println ("Can not parse " + sourceName + " " + node.getNT ());
        } catch (ParseException ex) {
            //ex.printStackTrace ();
            Point p = Utils.findPosition (source, tokenInput.getOffset ());
            throw new ParseException (
                sourceName + " " + 
                p.x + "," + p.y + ": " + 
                ex.getMessage ()
            );
        }
        readBody (source, sourceName, node, language);
        return language;
    }
    
    private static Language nbsLanguage;
    
    private static Language getNBSLanguage () throws ParseException {
        if (nbsLanguage == null)
            nbsLanguage = NBSLanguage.getNBSLanguage ();
        return nbsLanguage;
    }
    
    private static void readBody (
        String      source,
        String      sourceName,
        ASTNode     root, 
        Language    language
    ) throws ParseException {
        Iterator it = root.getChildren ().iterator ();
        while (it.hasNext ()) {
            Object o = it.next ();
            if (o instanceof ASTToken) continue;
            ASTNode node = (ASTNode) o;
            if (node.getNT ().equals ("token"))
                readToken (source, sourceName, node, language, null);
            else
            if (node.getNT ().equals ("tokenState"))
                readTokenState (source, sourceName, node, language);
            else
            if (node.getNT ().equals ("grammarRule"))
                readGrammarRule (node, language);
            else
            if (node.getNT ().equals ("command"))
                readCommand (source, sourceName, node, language);
            else
                throw new ParseException (
                    "Unknown grammar rule (" + node.getNT () + ")."
                );
        }
    }
    
    private static void readToken (
        String      source,
        String      sourceName,
        ASTNode     node, 
        Language    language, 
        String      state
    ) throws ParseException {
        String startState = null;
        String endState = null;
        Pattern pattern = null;
        Feature properties = null;
        String name  = node.getTokenType ("identifier").getIdentifier ();
        ASTNode pnode = node.getNode ("token2.properties");
        if (pnode != null) {
            properties = readProperties (source, sourceName, null, null, pnode);
//            startState = getString (properties, "start_state", false);
//            endState = getString (properties, "end_state", false);
//            pattern = (Pattern) properties.get ("pattern");
            startState = (String) properties.getValue ("start_state");
            endState = (String) properties.getValue ("end_state");
            pattern = properties.getPattern ("pattern");
            if (pattern == null && properties.getType("call") == Type.METHOD_CALL)
                pattern = Pattern.create (".");
        } else {
            ASTNode regularExpressionNode = node.getNode ("token2.regularExpression");
            String patternString = regularExpressionNode.getAsText ().trim ();
            endState = node.getTokenTypeIdentifier ("token2.token3.state.identifier");
            pattern = readPattern (source, sourceName, patternString, regularExpressionNode.getOffset ());
        }
        if (startState != null && state != null) 
            throw new ParseException ("Start state should not be specified inside token group block!");
        if (startState == null) startState = state;
        if (endState == null) endState = state;
        language.addToken (
            startState,
            name,
            pattern,
            endState,
            properties
        );
    }
    
    private static void readGrammarRule (
        ASTNode     node, 
        Language    language
    ) {
        language.addRule (node);
    }
    
    private static void readTokenState (
        String      source,
        String      sourceName,
        ASTNode     node, 
        Language    language
    ) throws ParseException {
        String startState = node.getTokenTypeIdentifier ("state.identifier");
        ASTNode n = node.getNode ("tokenState1.token");
        if (n != null)
            readToken (source, sourceName, n, language, startState);
        else
            readTokenGroup (source, sourceName, node.getNode ("tokenState1.tokenGroup"), language, startState);
    }
    
    private static void readTokenGroup (
        String      source,
        String      sourceName,
        ASTNode     node, 
        Language    language,
        String      startState
    ) throws ParseException {
        Iterator it = node.getNode ("tokensInGroup").getChildren ().iterator ();
        while (it.hasNext ()) {
            Object o = it.next ();
            if (o instanceof ASTToken) continue;
            ASTNode n = (ASTNode) o;
            readToken (source, sourceName, n, language, startState);
        }
    }
    
    private static void readCommand (
        String      source,
        String      sourceName,
        ASTNode     commandNode, 
        Language    language
    ) throws ParseException {
        String keyword = commandNode.getTokenTypeIdentifier ("keyword");
        ASTNode command0Node = commandNode.getNode ("command0");
        ASTNode selectorNode = command0Node.getNode ("selector");
        if (selectorNode != null) {
            //ASTNode classNode = selectorNode.getNode ("class");
            Iterator<Selector> it = readSelector (selectorNode).iterator ();
            while (it.hasNext ()) {
                Selector selector =  it.next ();
                ASTNode command1Node = command0Node.getNode ("command1");
                ASTNode valueNode = command1Node.getNode ("value");
                if (valueNode != null)
                    language.addFeature (readValue (source, sourceName, keyword, selector, valueNode));
                else
                    language.addFeature (Feature.create (keyword, selector));
            }
        } else {
            ASTNode valueNode = command0Node.getNode ("value");
            language.addFeature (readValue (source, sourceName, keyword, null, valueNode));
        }
    }
    
    private static Feature readValue (
        String      source,
        String      sourceName,
        String      keyword,
        Selector    selector,
        ASTNode     valueNode
    ) throws ParseException {
        ASTNode propertiesNode = valueNode.getNode ("properties");
        if (propertiesNode != null)
            return readProperties (source, sourceName, keyword, selector, propertiesNode);
        ASTNode classNode = valueNode.getNode ("class");
        if (classNode != null)
            return Feature.createMethodCallFeature (keyword, selector, readClass (classNode));
        ASTNode regExprNode = valueNode.getNode ("regularExpression");
        if (regExprNode != null) {
            Pattern pat = readPattern(source, sourceName, regExprNode.getAsText().trim(), regExprNode.getOffset());
            return Feature.createExpressionFeature (keyword, selector, pat);
        }
        String s = valueNode.getTokenTypeIdentifier ("string");
        s = s.substring (1, s.length () - 1);
        return Feature.createExpressionFeature (keyword, selector, c (s));
    }
    
    private static Feature readProperties (
        String      source,
        String      sourceName,
        String      keyword,
        Selector    selector,
        ASTNode     node
    ) throws ParseException {
        Map<String,String> methods = new HashMap<String,String> ();
        Map<String,String> expressions = new HashMap<String,String> ();
        Map<String,Pattern> patterns = new HashMap<String,Pattern> ();
        
        Iterator it = node.getChildren ().iterator ();
        while (it.hasNext ()) {
            Object o = it.next ();
            if (o instanceof ASTToken) continue;
            ASTNode n = (ASTNode) o;
            String key = n.getTokenTypeIdentifier ("identifier");
            String value = n.getTokenTypeIdentifier ("propertyValue.string");
            if (value != null) {
                value = value.substring (1, value.length () - 1);
                expressions.put (key, c (value));
            } else 
            if (n.getNode ("propertyValue.class") != null) {
                value = readClass (n.getNode ("propertyValue.class"));
                methods.put (key, value);
            } else {
                value = n.getNode ("propertyValue.regularExpression").getAsText ().trim ();
                Pattern pattern = readPattern (source, sourceName, value, n.getOffset ());
                patterns.put (key, pattern);
            }
        }
        return Feature.create (keyword, selector, expressions, methods, patterns);
    }
    
    
    private static List<Selector> readSelector (ASTNode selectorNode) {
        return readSelector (selectorNode, new ArrayList<Selector> ());
    }
    
    private static List<Selector> readSelector (ASTNode selectorNode, List<Selector> result) {
        Iterator<ASTItem> it = selectorNode.getChildren ().iterator ();
        while (it.hasNext ()) {
            ASTItem item =  it.next ();
            if (item instanceof ASTNode) {
                ASTNode node = (ASTNode) item;
                if (node.getNT ().equals ("class"))
                    result.add (Selector.create (readClass (node)));
                else
                if (node.getNT ().equals ("selector1"))
                    readSelector (node, result);
            }
        }
        return result;
    }
    
    private static String readClass (ASTNode cls) {
        StringBuilder sb = new StringBuilder ();
        sb.append (cls.getTokenTypeIdentifier ("identifier"));
        Iterator<ASTItem> it = cls.getNode ("class1").getChildren ().iterator ();
        while (it.hasNext ()) {
            ASTToken token = (ASTToken) it.next ();
            if (token.getIdentifier ().equals ("."))
                sb.append ('.');
            else
            if (token.getType ().equals ("identifier"))
                sb.append (token.getIdentifier ());
        }
        return sb.toString ();
    }
    
    private static Pattern readPattern (
        String      source,
        String      sourceName, 
        String      pattern, 
        int         offset
    ) throws ParseException {
        StringInput input = new StringInput (pattern);
        try {
            return Pattern.create (input);
        } catch (ParseException e) {
            Point p = Utils.findPosition (source, offset + input.getIndex ());
            throw new ParseException (
                sourceName + " " + 
                p.x + "," + p.y + ": " + 
                e.getMessage ()
            );
        }
    }
    
    private static String c (String s) {
        s = s.replace ("\\n", "\n");
        s = s.replace ("\\r", "\r");
        s = s.replace ("\\t", "\t");
        s = s.replace ("\\\"", "\"");
        s = s.replace ("\\\'", "\'");
        s = s.replace ("\\\\", "\\");
        return s;
    }
}
