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

import java.util.Collections;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.CharInput;
import org.netbeans.modules.languages.parser.TokenInput;
import org.netbeans.api.languages.ASTToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.languages.parser.Pattern;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.modules.languages.parser.TokenInput;
import org.netbeans.modules.languages.parser.StringInput;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

    
    
/**
 *
 * @author Jan Jancura
 */
public class NBSLanguageReader {
    
    public static Language readLanguage (
        FileObject  fo, 
        String      mimeType
    ) throws ParseException, IOException {
        BufferedReader reader = null;
        try {
            return readLanguage (fo.toString (), fo.getInputStream (), mimeType);
        } finally {
            if (reader != null)
                reader.close ();
        }
    }
    
    public static Language readLanguage (
        String      fileName, 
        InputStream inputStream,
        String      mimeType
    ) throws ParseException, IOException {
        BufferedReader reader = null;
        try {
            InputStreamReader r = new InputStreamReader (inputStream);
            reader = new BufferedReader (r);
            StringBuilder sb = new StringBuilder ();
            String line = reader.readLine ();
            while (line != null) {
                sb.append (line).append ('\n');
                line = reader.readLine ();
            }
            return readLanguage (fileName, sb.toString (), mimeType);
        } finally {
            if (reader != null)
                reader.close ();
        }
    }
    
    public static Language readLanguage (
        String      fo, 
        String      s, 
        String      mimeType
    ) {
        CharInput input = new StringInput (s, fo.toString ());
        try {
            Language language = new Language (mimeType);
            Language nbsLanguage = NBSLanguage.getNBSLanguage ();
            TokenInput tokenInput = TokenInput.create (
                mimeType,
                nbsLanguage.getParser (), 
                input, 
                Collections.EMPTY_SET //nbsLanguage.getSkipTokenTypes ()
            );
            ASTNode node = nbsLanguage.getAnalyser ().read (tokenInput, false);
            if (node == null) 
                System.out.println("Can not parse " + fo);
            else
            if (node.getChildren ().isEmpty ())
                System.out.println("Can not parse " + fo + " " + node.getNT ());
            readBody (node, language);
            return language;
        } catch (ParseException ex) {
            ErrorManager.getDefault ().notify (ex);
            return new Language (mimeType);
        }
    }
    
    private static void readBody (
        ASTNode root, 
        Language language
    ) throws ParseException {
        Iterator it = root.getChildren ().iterator ();
        while (it.hasNext ()) {
            Object o = it.next ();
            if (o instanceof ASTToken) continue;
            ASTNode node = (ASTNode) o;
            if (node.getNT ().equals ("token"))
                readToken (node, language, null);
            else
            if (node.getNT ().equals ("tokenState"))
                readTokenState (node, language);
            else
            if (node.getNT ().equals ("grammarRule"))
                readGrammarRule (node, language);
            else
            if (node.getNT ().equals ("command"))
                readCommand (node, language);
            else
                throw new ParseException (
                    "Unknown grammar rule (" + node.getNT () + ")."
                );
        }
    }
    
    private static void readToken (
        ASTNode node, 
        Language language, 
        String state
    ) throws ParseException {
        String startState = null;
        String endState = null;
        Pattern pattern = null;
        Feature properties = null;
        String name  = node.getTokenType ("identifier").getIdentifier ();
        ASTNode pnode = node.getNode ("token2.properties");
        if (pnode != null) {
            properties = readProperties (null, null, pnode);
//            startState = getString (properties, "start_state", false);
//            endState = getString (properties, "end_state", false);
//            pattern = (Pattern) properties.get ("pattern");
            startState = (String) properties.getValue ("start_state");
            endState = (String) properties.getValue ("end_state");
            pattern = properties.getPattern ("pattern");
        } else {
            String patternString = node.getNode ("token2.regularExpression").getAsText ().trim ();
            endState = node.getTokenTypeIdentifier ("token2.token3.state.identifier");
            pattern = Pattern.create (patternString);
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
        ASTNode node, 
        Language language
    ) throws ParseException {
//        String nt = node.getTokenTypeIdentifier ("identifier");
//        ASTNode rightSide = node.getNode ("rightSide");
        language.addRule (node);
    }
    
    private static void readTokenState (
        ASTNode node, 
        Language language
    ) throws ParseException {
        String startState = node.getTokenTypeIdentifier ("state.identifier");
        ASTNode n = node.getNode ("tokenState1.token");
        if (n != null)
            readToken (n, language, startState);
        else
            readTokenGroup (node.getNode ("tokenState1.tokenGroup"), language, startState);
    }
    
    private static void readTokenGroup (
        ASTNode node, 
        Language language,
        String startState
    ) throws ParseException {
        Iterator it = node.getNode ("tokensInGroup").getChildren ().iterator ();
        while (it.hasNext ()) {
            Object o = it.next ();
            if (o instanceof ASTToken) continue;
            ASTNode n = (ASTNode) o;
            readToken (n, language, startState);
        }
    }
    
    private static void readCommand (
        ASTNode node, 
        Language language
    ) throws ParseException {
        String keyword = node.getTokenTypeIdentifier ("keyword");
        ASTNode classNode = node.getNode ("command0.class");
        Selector selector = classNode != null ?
            Selector.create (classNode.getAsText ().trim ()):
            null;
        Feature feature = readFeature (keyword, selector, node);
        language.addFeature (feature);
    }
    
    private static Feature readFeature (
        String keyword,
        Selector selector,
        ASTNode node
    ) throws ParseException {
        Object value = node.getNode ("command0.properties");
        if (value == null)
            value = node.getNode ("command0.command1.properties");
        if (value != null)
            return readProperties (keyword, selector, (ASTNode) value);
        
        ASTNode n = node.getNode ("command0.command1.command2.class");
        if (n != null)
            return Feature.createMethodCallFeature (keyword, selector, n.getAsText ().trim ());
        
        String s = node.getTokenTypeIdentifier ("command0.command1.command2.string");
        if (s != null) {
            s = s.substring (1, s.length () - 1);
            return Feature.createExpressionFeature (keyword, selector, c (s));
        }
        
        n = node.getNode ("command0.command2.class");
        if (n != null)
            return Feature.createMethodCallFeature (keyword, selector, n.getAsText ().trim ());
        
        s = node.getTokenTypeIdentifier ("command0.command2.string");
        if (s != null) {
            s = s.substring (1, s.length () - 1);
            return Feature.createExpressionFeature (keyword, selector, c (s));
        }
        return Feature.create (keyword, selector);
    }
    
    private static Feature readProperties (
        String keyword,
        Selector selector,
        ASTNode node
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
                value = n.getNode ("propertyValue.class").getAsText ().trim ();
                methods.put (key, value);
            } else {
                value = n.getNode ("propertyValue.regularExpression").getAsText ().trim ();
                Pattern pattern = Pattern.create (value);
                patterns.put (key, pattern);
            }
        }
        return Feature.create (keyword, selector, expressions, methods, patterns);
    }
    
//    private static void addFeature (
//        Language    language,
//        String      featureName, 
//        Identifier  identifier,
//        Feature     feature
//    ) throws ParseException {
//        if (Language.BRACE.equals (featureName)) {
//            if (identifier != null)
//                throw new ParseException ("Syntax error.");
//            if (feature instanceof Evaluator.Method) {
//                if (language.getProperty (featureName) != null)
//                    throw new ParseException ("Syntax error.");
//                language.addProperty (featureName, feature);
//            } else
//            if (feature instanceof Evaluator.Expression) {
//                Object ov = language.getProperty (featureName);
//                if (ov != null && !(ov instanceof Object[]))
//                    throw new ParseException ("Syntax error.");
//                Object[] ss = (Object[]) ov;
//                if (ss == null) {
//                    ss = new Object [] {new HashMap (), new HashMap ()};
//                    language.addProperty (featureName, ss);
//                }
//                String s = (String) ((Evaluator.Expression) feature).evaluate ();
//                int i = s.indexOf (':');
//                if (i < 1 || i == s.length() - 1) {
//                    throw new ParseException ("Syntax error.");
//                } else {
//                    String leftBrace = s.substring (0, i);
//                    String rightBrace = s.substring (i + 1);
//                    ((Map) ss [0]).put (leftBrace, rightBrace);
//                    ((Map) ss [1]).put (rightBrace, leftBrace);
//                }
//            } else
//                throw new ParseException ("Syntax error.");
//        } else
//        if (Language.COLOR.equals (featureName)) {
//            if (identifier == null)
//                throw new ParseException ("Syntax error.");
//            if (!(feature instanceof Map))
//                throw new ParseException ("Syntax error.");
//            Map m = (Map) feature;
//            String colorName = getString (m, "color_name", false);
//            if (colorName == null)
//                colorName = identifier.toString ();
//            feature = createColoring (
//                colorName,
//                getString (m, "default_coloring", false),
//                getString (m, "foreground_color", false),
//                getString (m, "background_color", false),
//                getString (m, "underline_color", false),
//                getString (m, "wave_underline_color", false),
//                getString (m, "strike_through_color", false),
//                getString (m, "font_name", false),
//                getString (m, "font_type", false),
//                getEvaluator (m, "condition", false)
//            );
//            List list = (List) language.getFeature (featureName, identifier.toString ());
//            if (list == null)
//                list = new ArrayList ();
//            list.add (feature);
//            language.addFeature (featureName, identifier, list);
//        } else
//        if (Language.COMPLETE.equals (featureName)) {
//            if (identifier != null)
//                throw new ParseException ("Syntax error.");
//            Object ov = language.getProperty (Language.COMPLETE);
//            Object[] ss = (Object[]) ov;
//            if (ss == null) {
//                ss = new Object [] {new ArrayList (), new ArrayList (), null};
//                language.addProperty (featureName, ss);
//            }
//            if (feature instanceof Evaluator.Method) {
//                ss [2] = feature;
//            } else
//            if (feature instanceof Evaluator.Expression) {
//                String s = (String) ((Evaluator.Expression) feature).evaluate ();
//                int i = s.indexOf (':');
//                if (i < 1) throw new ParseException ("Syntax error.");
//                ((List) ss [0]).add (s.substring (0, i));
//                ((List) ss [1]).add (s.substring (i + 1));
//            } else
//                throw new ParseException ("Syntax error.");
//        } else
//        if (Language.COMPLETION.equals (featureName)) {
//            if (identifier == null)
//                throw new ParseException ("Syntax error.");
//            if (!(feature instanceof Map))
//                throw new ParseException ("Syntax error.");
//            language.addFeature (featureName, identifier, feature);
//        } else
//        if (Language.FOLD.equals (featureName)) {
//            if (identifier == null)
//                throw new ParseException ("Syntax error.");
//            if (feature == null)
//                feature = Evaluator.createExpressionEvaluator ("...");
//            language.addFeature (featureName, identifier, feature);
//        } else
//        if (Language.HYPERLINK.equals (featureName)) {
//            if (identifier == null)
//                throw new ParseException ("Syntax error.");
//            if (!(feature instanceof Evaluator))
//                throw new ParseException ("Syntax error.");
//            language.addFeature (featureName, identifier, feature);
//        } else
//        if (Language.IMPORT.equals (featureName)) {
//            if (identifier == null)
//                throw new ParseException ("Syntax error.");
//            if (!(feature instanceof Map))
//                throw new ParseException ("Syntax error.");
//            Map featureMap = (Map) feature;
//            if (featureMap.containsKey ("background_color"))
//                featureMap.put (
//                    "background_color", 
//                    readColor (
//                        (String) ((Evaluator) featureMap.get ("background_color")).
//                        evaluate ()
//                    )
//                );
//            language.importLanguage (identifier.toString (), (Map) feature);
//        } else
//        if (Language.INDENT.equals (featureName)) {
//            if (identifier != null)
//                throw new ParseException ("Syntax error.");
//            if (feature instanceof Evaluator.Method) {
//                if (language.getProperty (featureName) != null)
//                    throw new ParseException ("Syntax error.");
//                language.addProperty (featureName, feature);
//            } else
//            if (feature instanceof Evaluator.Expression) {
//                Object ov = language.getProperty (featureName);
//                if (ov != null && !(ov instanceof Object[]))
//                    throw new ParseException ("Syntax error.");
//                Object[] ss = (Object[]) ov;
//                if (ss == null) {
//                    ss = new Object [] {new ArrayList (), new HashSet (), new HashSet (), new HashMap ()};
//                    language.addProperty (featureName, ss);
//                }
//                String s = (String) ((Evaluator.Expression) feature).evaluate ();
//                int i = s.indexOf (':');
//                if (i < 1) 
//                    ((List) ss [0]).add (java.util.regex.Pattern.compile (c (s)));
//                else {
//                    ((Set) ss [1]).add (s.substring (0, i));
//                    ((Set) ss [2]).add (s.substring (i + 1));
//                    ((Map) ss [3]).put (s.substring (i + 1), s.substring (0, i));
//                } 
//            } else
//                throw new ParseException ("Syntax error.");
//        } else
//        if (Language.MARK.equals (featureName)) {
//            if (identifier == null)
//                throw new ParseException ("Syntax error.");
//            if (!(feature instanceof Map))
//                throw new ParseException ("Syntax error.");
//            language.addFeature (featureName, identifier, feature);
//        } else
//        if (Language.NAVIGATOR.equals (featureName)) {
//            if (identifier == null)
//                throw new ParseException ("Syntax error.");
//            if (!(feature instanceof Map))
//                throw new ParseException ("Syntax error.");
//            Map m = (Map) feature;
//            feature = Navigator.create (
//                getEvaluator (m, "display_name", true),
//                getEvaluator (m, "tooltip", false),
//                getEvaluator (m, "icon", false),
//                getEvaluator (m, "isLeaf", false)
//            );
//            language.addFeature (featureName, identifier, feature);
//        } else
//        if (Language.PARSE.equals (featureName)) {
//            if (identifier == null)
//                throw new ParseException ("Syntax error.");
//            if (feature == null)
//                throw new ParseException ("Syntax error.");
//            if (!(feature instanceof Evaluator))
//                throw new ParseException ("Syntax error.");
//            language.addFeature (featureName, identifier, feature);
//        } else
//        if (Language.PROPERTIES.equals (featureName)) {
//            if (identifier != null)
//                throw new ParseException ("Syntax error.");
//            if (feature == null)
//                throw new ParseException ("Properties not defined for PROPERTIES command!");
//            if (!(feature instanceof Map))
//                throw new ParseException ("Syntax error.");
//            language.addProperties ((Map) feature);
//        } else
//        if (Language.REFORMAT.equals (featureName)) {
//            if (identifier == null)
//                throw new ParseException ("Syntax error.");
//            if (!(feature instanceof Evaluator.Expression))
//                throw new ParseException ("Syntax error.");
//            String f = (String) ((Evaluator.Expression) feature).evaluate ();
//            language.addFeature (featureName, identifier, feature);
//        } else
//        if (Language.SKIP.equals (featureName)) {
//            if (identifier == null)
//                throw new ParseException ("Token type not defined for SKIP command!");
//            if (feature != null)
//                throw new ParseException ("Syntax error.");
//            language.addSkipTokenType (identifier.toString ());
//        } else
//        if (Language.STORE.equals (featureName)) {
//            if (identifier == null)
//                throw new ParseException ("Syntax error.");
//            if (!(feature instanceof Map))
//                throw new ParseException ("Syntax error.");
//            
//            String c = getString ((Map) feature, "context", true);
//            String first = null;
//            StringTokenizer st = new StringTokenizer (c, ",");
//            Set cs = new HashSet ();
//            while (st.hasMoreElements ())
//                if (first == null)
//                    first = st.nextToken ().trim ();
//                else
//                    cs.add (st.nextToken ().trim ());
//            ((Map) feature).put ("context", new Object[] {first, cs});
//            
//            language.addFeature (featureName, identifier, feature);
////            Set s = (Set) language.getProperty (language.getMimeType (), "contexts");
////            if (s == null) {
////                s = new HashSet ();
////                language.addProperty ("contexts", s);
////            }
////            s.add (cs);
//        } else
//        if (Language.TOOLTIP.equals (featureName)) {
//            if (identifier == null)
//                throw new ParseException ("Syntax error.");
//            if (!(feature instanceof Evaluator))
//                throw new ParseException ("Syntax error.");
//            language.addFeature (featureName, identifier, feature);
//        } else
//            throw new ParseException ("Unknown command: " + featureName);
//    }

//    private static Evaluator getEvaluator (
//        Map m, 
//        String key, 
//        boolean required
//    ) throws ParseException {
//        Object o = m.get (key);
//        if (o != null) return (Evaluator) o;
//        if (required)
//            throw new ParseException ("Syntax error.");
//        return null;
//    }
//    
//    private static String getString (Map m, String key, boolean required) throws ParseException {
//        Evaluator e = (Evaluator) m.get (key);
//        if (e == null) {
//            if (required)
//                throw new ParseException ("Syntax error.");
//            else 
//                return null;
//        }
//        if (!(e instanceof Evaluator.Expression))
//            throw new ParseException ("Syntax error.");
//        return (String) e.evaluate ();
//    }
    
    
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
