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

import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.CharInput;
import org.netbeans.api.languages.TokenInput;
import org.netbeans.api.languages.SToken;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.modules.languages.Evaluator;
import org.netbeans.modules.languages.Language.Identifier;
import org.netbeans.modules.languages.Language.Navigator;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser;
import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.languages.parser.Parser;
import org.netbeans.modules.languages.parser.Pattern;
import org.netbeans.api.languages.SToken;
import org.netbeans.api.languages.TokenInput;
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
                nbsLanguage.getParser (), 
                input, 
                nbsLanguage.getSkipTokenTypes ()
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
            if (o instanceof SToken) continue;
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
        Map properties = null;
        String name  = node.getTokenType ("identifier").getIdentifier ();
        ASTNode pnode = node.getNode ("token2.properties");
        if (pnode != null) {
            properties = readProperties (pnode, language);
            startState = getString (properties, "start_state", false);
            endState = getString (properties, "end_state", false);
            pattern = (Pattern) properties.get ("pattern");
        } else {
            String patternString = node.getNode ("token2.regularExpression").getAsText ();
            endState = node.getTokenTypeIdentifier ("token2.token3.state.identifier");
            pattern = Pattern.create (patternString, language.getMimeType ());
        }
        if (startState != null && state != null) 
            throw new ParseException ("Start state should not be specified inside token group block!");
        if (startState == null) startState = state;
        if (endState == null) endState = state;
        language.addToken (
            startState,
            language.getMimeType (),
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
            if (o instanceof SToken) continue;
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
        Identifier identifier = classNode != null ?
            Language.createIdentifier (classNode.getAsText ()):
            null;
        
        Object value = node.getNode ("command0.properties");
        if (value == null)
            value = node.getNode ("command0.command1.properties");
        if (value != null)
            value = readProperties ((ASTNode) value, language);
        
        if (value == null) {
            ASTNode n = node.getNode ("command0.command1.command2.class");
            if (n != null)
                value = Evaluator.createMethodEvaluator (n.getAsText ());
        }
        if (value == null) {
            String s = node.getTokenTypeIdentifier ("command0.command1.command2.string");
            if (s != null) {
                s = s.substring (1, s.length () - 1);
                value = Evaluator.createExpressionEvaluator (c (s));
            }
        }
        
        if (value == null) {
            ASTNode n = node.getNode ("command0.command2.class");
            if (n != null)
                value = Evaluator.createMethodEvaluator (n.getAsText ());
        }
        if (value == null) {
            String s = node.getTokenTypeIdentifier ("command0.command2.string");
            if (s != null) {
                s = s.substring (1, s.length () - 1);
                value = Evaluator.createExpressionEvaluator (c (s));
            }
        }
        
        addFeature (language, keyword, identifier, value);
    }
    
    private static Map readProperties (ASTNode node, Language language) throws ParseException {
        Map result = new HashMap ();
        Iterator it = node.getChildren ().iterator ();
        while (it.hasNext ()) {
            Object o = it.next ();
            if (o instanceof SToken) continue;
            ASTNode n = (ASTNode) o;
            String key = n.getTokenTypeIdentifier ("identifier");
            String value = n.getTokenTypeIdentifier ("propertyValue.string");
            if (value != null) {
                value = value.substring (1, value.length () - 1);
                Evaluator evaluator = Evaluator.createExpressionEvaluator (c (value));
                result.put (key, evaluator);
            } else 
            if (n.getNode ("propertyValue.class") != null) {
                value = n.getNode ("propertyValue.class").getAsText ();
                Evaluator evaluator = Evaluator.createMethodEvaluator (value);
                result.put (key, evaluator);
            } else {
                value = n.getNode ("propertyValue.regularExpression").getAsText ();
                Pattern pattern = Pattern.create (value, language.getMimeType ());
                result.put (key, pattern);
            }
        }
        return result;
    }
    
    private static void addFeature (
        Language    language,
        String      featureName, 
        Identifier  identifier,
        Object      feature
    ) throws ParseException {
        if (Language.ACTION.equals (featureName)) {
            if (identifier == null)
                throw new ParseException ("Syntax error.");
            if (!(feature instanceof Map))
                throw new ParseException ("Syntax error.");
            List[] l = (List[]) language.getProperty 
                (language.getMimeType (), featureName);
            if (l == null) {
                l = new List[] {new ArrayList (), new ArrayList ()};
                language.addProperty (featureName, l);
            }
            Map m = (Map) feature;
            Evaluator name = getEvaluator (m, "name", true);
            Evaluator performer = getEvaluator (m, "performer", true);
            Evaluator enabled = getEvaluator (m, "enabled", false);
            String explorer = getString (m, "explorer", false);
            if (explorer != null && explorer.equals ("true"))
                l [1].add (new Evaluator[] {
                    name, performer, enabled
                });
            else
                l [0].add (new Evaluator[] {
                    name, performer, enabled
                });
        } else
        if (Language.AST.equals (featureName)) {
            if (identifier != null)
                throw new ParseException ("Syntax error.");
            if (!(feature instanceof Map))
                throw new ParseException ("Syntax error.");
            language.addProperty (featureName, feature);
        } else
        if (Language.BRACE.equals (featureName)) {
            if (identifier != null)
                throw new ParseException ("Syntax error.");
            if (feature instanceof Evaluator.Method) {
                if (language.getProperty (language.getMimeType (), featureName) != null)
                    throw new ParseException ("Syntax error.");
                language.addProperty (featureName, feature);
            } else
            if (feature instanceof Evaluator.Expression) {
                Object ov = language.getProperty (language.getMimeType (), featureName);
                if (ov != null && !(ov instanceof Object[]))
                    throw new ParseException ("Syntax error.");
                Object[] ss = (Object[]) ov;
                if (ss == null) {
                    ss = new Object [] {new HashMap (), new HashMap ()};
                    language.addProperty (featureName, ss);
                }
                String s = (String) ((Evaluator.Expression) feature).evaluate ();
                int i = s.indexOf (':');
                if (i < 1 || i == s.length() - 1) {
                    throw new ParseException ("Syntax error.");
                } else {
                    String leftBrace = s.substring (0, i);
                    String rightBrace = s.substring (i + 1);
                    ((Map) ss [0]).put (leftBrace, rightBrace);
                    ((Map) ss [1]).put (rightBrace, leftBrace);
                }
            } else
                throw new ParseException ("Syntax error.");
        } else
        if (Language.COLOR.equals (featureName)) {
            if (identifier == null)
                throw new ParseException ("Syntax error.");
            if (!(feature instanceof Map))
                throw new ParseException ("Syntax error.");
            Map m = (Map) feature;
            String colorName = getString (m, "color_name", false);
            if (colorName == null)
                colorName = identifier.toString ();
            feature = createColoring (
                colorName,
                getString (m, "default_coloring", false),
                getString (m, "foreground_color", false),
                getString (m, "background_color", false),
                getString (m, "underline_color", false),
                getString (m, "wave_underline_color", false),
                getString (m, "strike_through_color", false),
                getString (m, "font_name", false),
                getString (m, "font_type", false),
                getEvaluator (m, "condition", false)
            );
            language.addFeature (featureName, identifier, feature);
        } else
        if (Language.COMPLETE.equals (featureName)) {
            if (identifier != null)
                throw new ParseException ("Syntax error.");
            Object ov = language.getProperty (language.getMimeType (), Language.COMPLETE);
            Object[] ss = (Object[]) ov;
            if (ss == null) {
                ss = new Object [] {new ArrayList (), new ArrayList (), null};
                language.addProperty (featureName, ss);
            }
            if (feature instanceof Evaluator.Method) {
                ss [2] = feature;
            } else
            if (feature instanceof Evaluator.Expression) {
                String s = (String) ((Evaluator.Expression) feature).evaluate ();
                int i = s.indexOf (':');
                if (i < 1) throw new ParseException ("Syntax error.");
                ((List) ss [0]).add (s.substring (0, i));
                ((List) ss [1]).add (s.substring (i + 1));
            } else
                throw new ParseException ("Syntax error.");
        } else
        if (Language.COMPLETION.equals (featureName)) {
            if (identifier == null)
                throw new ParseException ("Syntax error.");
            if (!(feature instanceof Map))
                throw new ParseException ("Syntax error.");
            language.addFeature (featureName, identifier, feature);
        } else
        if (Language.FOLD.equals (featureName)) {
            if (identifier == null)
                throw new ParseException ("Syntax error.");
            if (feature == null)
                feature = Evaluator.createExpressionEvaluator ("...");
            else
            if (!(feature instanceof Evaluator))
                throw new ParseException ("Syntax error.");
            language.addFeature (featureName, identifier, feature);
        } else
        if (Language.HYPERLINK.equals (featureName)) {
            if (identifier == null)
                throw new ParseException ("Syntax error.");
            if (!(feature instanceof Evaluator))
                throw new ParseException ("Syntax error.");
            language.addFeature (featureName, identifier, feature);
        } else
        if (Language.IMPORT.equals (featureName)) {
            if (identifier == null)
                throw new ParseException ("Syntax error.");
            if (!(feature instanceof Map))
                throw new ParseException ("Syntax error.");
            language.importLanguage (identifier.toString (), (Map) feature);
        } else
        if (Language.INDENT.equals (featureName)) {
            if (identifier != null)
                throw new ParseException ("Syntax error.");
            if (feature instanceof Evaluator.Method) {
                if (language.getProperty (language.getMimeType (), featureName) != null)
                    throw new ParseException ("Syntax error.");
                language.addProperty (featureName, feature);
            } else
            if (feature instanceof Evaluator.Expression) {
                Object ov = language.getProperty (language.getMimeType (), featureName);
                if (ov != null && !(ov instanceof Object[]))
                    throw new ParseException ("Syntax error.");
                Object[] ss = (Object[]) ov;
                if (ss == null) {
                    ss = new Object [] {new ArrayList (), new HashSet (), new HashSet (), new HashMap ()};
                    language.addProperty (featureName, ss);
                }
                String s = (String) ((Evaluator.Expression) feature).evaluate ();
                int i = s.indexOf (':');
                if (i < 1) 
                    ((List) ss [0]).add (java.util.regex.Pattern.compile (c (s)));
                else {
                    ((Set) ss [1]).add (s.substring (0, i));
                    ((Set) ss [2]).add (s.substring (i + 1));
                    ((Map) ss [3]).put (s.substring (i + 1), s.substring (0, i));
                } 
            } else
                throw new ParseException ("Syntax error.");
        } else
        if (Language.MARK.equals (featureName)) {
            if (identifier == null)
                throw new ParseException ("Syntax error.");
            if (!(feature instanceof Map))
                throw new ParseException ("Syntax error.");
            language.addFeature (featureName, identifier, feature);
        } else
        if (Language.NAVIGATOR.equals (featureName)) {
            if (identifier == null)
                throw new ParseException ("Syntax error.");
            if (!(feature instanceof Map))
                throw new ParseException ("Syntax error.");
            Map m = (Map) feature;
            feature = Navigator.create (
                getEvaluator (m, "display_name", true),
                getEvaluator (m, "tooltip", false),
                getEvaluator (m, "icon", false),
                getEvaluator (m, "isLeaf", false)
            );
            language.addFeature (featureName, identifier, feature);
        } else
        if (Language.PARSE.equals (featureName)) {
            if (identifier == null)
                throw new ParseException ("Syntax error.");
            if (feature == null)
                throw new ParseException ("Syntax error.");
            if (!(feature instanceof Evaluator))
                throw new ParseException ("Syntax error.");
            language.addFeature (featureName, identifier, feature);
        } else
        if (Language.PROPERTIES.equals (featureName)) {
            if (identifier != null)
                throw new ParseException ("Syntax error.");
            if (feature == null)
                throw new ParseException ("Properties not defined for PROPERTIES command!");
            if (!(feature instanceof Map))
                throw new ParseException ("Syntax error.");
            language.addProperties ((Map) feature);
        } else
        if (Language.REFORMAT.equals (featureName)) {
            if (identifier == null)
                throw new ParseException ("Syntax error.");
            if (!(feature instanceof Evaluator.Expression))
                throw new ParseException ("Syntax error.");
            String f = (String) ((Evaluator.Expression) feature).evaluate ();
            language.addFeature (featureName, identifier, feature);
        } else
        if (Language.SKIP.equals (featureName)) {
            if (identifier == null)
                throw new ParseException ("Token type not defined for SKIP command!");
            if (feature != null)
                throw new ParseException ("Syntax error.");
            language.addSkipTokenType (identifier.toString ());
        } else
        if (Language.STORE.equals (featureName)) {
            if (identifier == null)
                throw new ParseException ("Syntax error.");
            if (!(feature instanceof Map))
                throw new ParseException ("Syntax error.");
            
            String c = getString ((Map) feature, "context", true);
            String first = null;
            StringTokenizer st = new StringTokenizer (c, ",");
            Set cs = new HashSet ();
            while (st.hasMoreElements ())
                if (first == null)
                    first = st.nextToken ().trim ();
                else
                    cs.add (st.nextToken ().trim ());
            ((Map) feature).put ("context", new Object[] {first, cs});
            
            language.addFeature (featureName, identifier, feature);
//            Set s = (Set) language.getProperty (language.getMimeType (), "contexts");
//            if (s == null) {
//                s = new HashSet ();
//                language.addProperty ("contexts", s);
//            }
//            s.add (cs);
        } else
        if (Language.TOOLTIP.equals (featureName)) {
            if (identifier == null)
                throw new ParseException ("Syntax error.");
            if (!(feature instanceof Evaluator))
                throw new ParseException ("Syntax error.");
            language.addFeature (featureName, identifier, feature);
        } else
            throw new ParseException ("Unknown command: " + featureName);
    }

    private static Evaluator getEvaluator (
        Map m, 
        String key, 
        boolean required
    ) throws ParseException {
        Object o = m.get (key);
        if (o != null) return (Evaluator) o;
        if (required)
            throw new ParseException ("Syntax error.");
        return null;
    }
    
    private static String getString (Map m, String key, boolean required) throws ParseException {
        Evaluator e = (Evaluator) m.get (key);
        if (e == null) {
            if (required)
                throw new ParseException ("Syntax error.");
            else 
                return null;
        }
        if (!(e instanceof Evaluator.Expression))
            throw new ParseException ("Syntax error.");
        return (String) e.evaluate ();
    }
    
    private static AttributeSet createColoring (
        String colorName, 
        String defaultColor,
        String foreground,
        String background,
        String underline,
        String waveunderline,
        String strikethrough,
        String fontName,
        String fontType,
        Evaluator condition
    ) throws ParseException {
        SimpleAttributeSet coloring = new SimpleAttributeSet ();
        coloring.addAttribute (StyleConstants.NameAttribute, colorName);
        if (defaultColor != null)
            coloring.addAttribute (EditorStyleConstants.Default, defaultColor);
        if (foreground != null)
            coloring.addAttribute (StyleConstants.Foreground, convert (foreground));
        if (background != null)
            coloring.addAttribute (StyleConstants.Background, convert (background));
        if (strikethrough != null)
            coloring.addAttribute (StyleConstants.StrikeThrough, convert (strikethrough));
        if (underline != null)
            coloring.addAttribute (StyleConstants.Underline, convert (underline));
        if (waveunderline != null)
            coloring.addAttribute (EditorStyleConstants.WaveUnderlineColor, convert (waveunderline));
        if (fontName != null)
            coloring.addAttribute (StyleConstants.FontFamily, fontName);
        if (fontType != null) {
            if (fontType.toLowerCase ().indexOf ("bold") >= 0)
                coloring.addAttribute (StyleConstants.Bold, Boolean.TRUE);
            if (fontType.toLowerCase ().indexOf ("italic") >= 0)
                coloring.addAttribute (StyleConstants.Italic, Boolean.TRUE);
        }
        if (condition != null)
            coloring.addAttribute ("condition", condition);
        return coloring;
    }
    
    
    private static Map colors = new HashMap ();
    static {
        colors.put ("black", Color.black);
        colors.put ("blue", Color.blue);
        colors.put ("cyan", Color.cyan);
        colors.put ("darkGray", Color.darkGray);
        colors.put ("gray", Color.gray);
        colors.put ("green", Color.green);
        colors.put ("lightGray", Color.lightGray);
        colors.put ("magenta", Color.magenta);
        colors.put ("orange", Color.orange);
        colors.put ("pink", Color.pink);
        colors.put ("red", Color.red);
        colors.put ("white", Color.white);
        colors.put ("yellow", Color.yellow);
    }
    
    private static Color convert (String color) throws ParseException {
        Color result = (Color) colors.get (color);
        if (result == null)
            try {
                result = Color.decode (color);
            } catch (NumberFormatException ex) {
                throw new ParseException (ex.toString ());
            }
        return result;
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
