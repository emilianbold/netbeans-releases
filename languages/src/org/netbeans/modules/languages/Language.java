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

import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.languages.LanguagesManager;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.SToken;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.editor.settings.storage.api.FontColorSettingsFactory;
import org.netbeans.modules.languages.Language.Identifier;
import org.netbeans.modules.languages.LanguagesManagerImpl;
import org.netbeans.modules.languages.parser.*;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.*;
import java.util.List;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser;


/**
 *
 * @author Jan Jancura
 */
public class Language {

    
    public static final String ACTION = "ACTION";
    public static final String AST = "AST";
    public static final String BRACE = "BRACE";
    public static final String COLOR = "COLOR";
    public static final String COMPLETE = "COMPLETE";
    public static final String COMPLETION = "COMPLETION";
    public static final String FOLD = "FOLD";
    public static final String HYPERLINK = "HYPERLINK";
    public static final String IMPORT = "IMPORT";
    public static final String INDENT = "INDENT";
    public static final String MARK = "MARK";
    public static final String NAVIGATOR = "NAVIGATOR";
    public static final String PARSE = "PARSE";
    public static final String PROPERTIES = "PROPERTIES";
    public static final String REFORMAT = "REFORMAT";
    public static final String SKIP = "SKIP";
    public static final String STORE = "STORE";
    public static final String TOKEN = "TOKEN";
    public static final String TOOLTIP = "TOOLTIP";
    
    
    private Parser              parser;
    private List                tokenTypes = new ArrayList ();
    private Set                 skipTokenTypes = new HashSet ();
    private String              mimeType;
    private LLSyntaxAnalyser    analyser = null;
    private List                analyserRules = new ArrayList ();
    private List                analyserRules2;
    private List                importedLangauges = new ArrayList ();

    
    /** Creates a new instance of Language */
    public Language (String mimeType) {
        this.mimeType = mimeType;
    }
    
    
    // public methods ..........................................................
    
    public String getMimeType () {
        return mimeType;
    }

    public Parser getParser () {
        if (parser == null)
            parser = Parser.create (tokenTypes);
        return parser;
    }
    
    public List getTokens () {
        return Collections.unmodifiableList (tokenTypes);
    }
    
    public Set getSkipTokenTypes () {
        return skipTokenTypes;
    }
    
    public boolean hasAnalyser () {
        return !analyserRules.isEmpty ();
    }
    
    public LLSyntaxAnalyser getAnalyser () {
        if (analyser != null) return analyser;
        synchronized (this) {
            if (analyser != null) return analyser;
            analyser = LLSyntaxAnalyser.create (this);
            return analyser;
        }
    }
    
    public List getImportedLanguages () {
        return importedLangauges;
    }
    
    public Object getProperty (
        String propertyName
    ) {
        return properties.get (propertyName);
    }
    
    public static Identifier createIdentifier (List name) {
        Identifier result = new Identifier ();
        result.name = name;
        return result;
    }
    
    public static Identifier createIdentifier (String name) {
        List l = new ArrayList ();
        int s = 0, e = name.indexOf ('.');
        while (e >= 0) {
            l.add (name.substring (s, e));
            s = e + 1;
            e = name.indexOf ('.', s);
        }
        l.add (name.substring (s));
        Identifier result = new Identifier ();
        result.name = l;
        return result;
    }
    
    public static TokenType createTokenType (
        String      startState,
        Pattern     pattern,
        String      type,
        String      endState,
        int         priority,
        Map         properties
    ) {
        return new TokenType (
            startState,
            pattern,
            type,
            endState,
            priority,
            properties
        );
    }

    
    // package private interface ...............................................

    void addToken (
        String      startState,
        String      type,
        Pattern     pattern,
        String      endState,
        Map         properties
    ) {
        if (parser != null)
            throw new InternalError ();
        tokenTypes.add (createTokenType (
            startState,
            pattern,
            type,
            endState,
            tokenTypes.size (),
            properties
        ));
    }
    
    void addSkipTokenType (String tokenType) {
        skipTokenTypes.add (tokenType);
    }
    
    void addRule (ASTNode rule) {
        if (analyser != null)
            throw new InternalError ();
        analyserRules.add (rule);
    }
    
    public void addRule (LLSyntaxAnalyser.Rule rule) {
        if (analyserRules2 == null) analyserRules2 = new ArrayList ();
        analyserRules2.add (rule);
    }
    
    public List getRules () {
        if (analyserRules2 == null)
            analyserRules2 = Petra.convert (analyserRules);
        return analyserRules2;
    }

    private Map properties = new HashMap ();
    
    void addProperties (Map properties) {
        this.properties.putAll (properties);
    }
    
    void addProperty (String key, Object value) {
        properties.put (key, value);
    }
    
    void importLanguage (
        String name,
        Map properties
    ) throws ParseException {
        String mimeType = (String) ((Evaluator) properties.get ("mimeType")).evaluate ();
        Language l = ((LanguagesManagerImpl) LanguagesManager.getDefault ()).getLanguage (mimeType);
        if (properties.containsKey ("token")) {
            String token = (String) ((Evaluator) properties.get ("token")).evaluate ();
            addFeature (
                Language.IMPORT, 
                Language.createIdentifier (token), 
                properties
            );
            importedLangauges.add (l);
            return;
        }
        if (properties.containsKey ("start")) {
            addToken (
                null,
                name,
                null,
                null,
                null
            );
            addFeature (
                Language.IMPORT, 
                createIdentifier (name), 
                properties
            );
            addSkipTokenType (name);
            importedLangauges.add (l);
            return;
        }
        Evaluator stateEvaluator = (Evaluator) properties.get ("state");
        String state = stateEvaluator == null ? null : (String) stateEvaluator.evaluate ();
        
        // import tokenTypes
        Iterator it = l.tokenTypes.iterator ();
        while (it.hasNext ()) {
            TokenType tt = (TokenType) it.next ();
            String startState = tt.getStartState ();
            Pattern pattern = tt.getPattern ().clonePattern ();
            String endState = tt.getEndState ();
            if (startState == null || Parser.DEFAULT_STATE.equals (startState)) 
                startState = state;
            else
                startState = name + '-' + startState;
            if (endState == null || Parser.DEFAULT_STATE.equals (endState)) 
                endState = state;
            else
                endState = name + '-' + endState;
            addToken (startState, tt.getType (), pattern, endState, tt.getProperties ());
        }
        
        // import grammar rues
        analyserRules.addAll (l.analyserRules);
        // import colorings
        importColorings (l);
        // import other features
        importFeature (ACTION, l);
        importFeature (BRACE, l);
        importFeature (COLOR, l);
        //importFeature (COMPLETE, l);
        importFeature (COMPLETION, l);
        importFeature (FOLD, l);
        importFeature (HYPERLINK, l);
        importFeature (INDENT, l);
        importFeature (MARK, l);
        importFeature (NAVIGATOR, l);
        importFeature (PARSE, l);
        // import properties
        this.properties.putAll (l.properties);
        importFeature (REFORMAT, l);
        // import skip tokenTypes
        skipTokenTypes.addAll (l.skipTokenTypes);
        importFeature (STORE, l);
        importFeature (TOOLTIP, l);
    }
    
    private void importColorings (Language l) {
        if (!l.features.containsKey (COLOR)) return;
        Map m = (Map) features.get (COLOR);
        if (m == null) {
            m = new HashMap ();
            features.put (COLOR, m);
        }
        importColorings (
            (Map) l.features.get (COLOR),
            m
        );
    }

    private void importColorings (Map from, Map to) {
        Iterator it = from.keySet ().iterator ();
        while (it.hasNext ()) {
            String colorName = (String) it.next ();
            Object value = from.get (colorName);
            if (value instanceof AttributeSet) {
                SimpleAttributeSet as = new SimpleAttributeSet (
                    (AttributeSet) from.get (colorName)
                );
                as.addAttribute (StyleConstants.NameAttribute, colorName);
                to.put (colorName, as);
            } else {
                Map newTo = (Map) to.get (colorName);
                if (newTo == null) {
                    newTo = new HashMap ();
                    to.put (colorName, newTo);
                }
                importColorings ((Map) value, newTo);
            }
        }
    }
    
    private void importFeature (String feature, Language l) {
        if (!l.features.containsKey (feature)) return;
        Map m = (Map) features.get (feature);
        if (m == null) {
            m = new HashMap ();
            features.put (feature, m);
        }
        m.putAll ((Map) l.features.get (feature));
    }
    
    private Map hyperlinks = new HashMap ();
    
    void addHyperlink (
        String      mimeType,
        String      id, 
        Evaluator   value
    ) {
        Map m = (Map) hyperlinks.get (mimeType);
        if (m == null) {
            m = new HashMap ();
            hyperlinks.put (mimeType, m);
        }
        m.put (id, value);
    }

    
    // private helper methods ..................................................
    
    private Map features = new HashMap ();
    
    public Object getFeature (String featureName, ASTNode node) {
        Map m = (Map) features.get (featureName);
        if (m == null) return null;
        while (true) {
            Object value = m.get (node.getNT ());
            if (value == null) return m.get ("");
            if (value instanceof MMap) {
                m = (Map) value;
                node = node.getParent ();
                if (node == null) return null;
                continue;
            }
            return value;
        }
    }
    
    public Object getFeature (String featureName, String nt) {
        Map m = (Map) features.get (featureName);
        if (m == null) return null;
        Object value = m.get (nt);
        if (value == null) return m.get ("");
        if (value instanceof MMap) {
            m = (Map) value;
            return m.get ("");
        }
        return value;
    }
    
    public Map getFeature (String featureName) {
        return (Map) features.get (featureName);
    }
    
    public Object getFeature (String featureName, SToken token) {
        Map m = (Map) features.get (featureName);
        if (m == null) return null;
        Object result = m.get (token.getType ());
        if (result instanceof MMap) return null;
        return result;
    }
    
    public boolean supportsFeature (String featureName) {
        return features.get (featureName) != null;
    }

    void addFeature (String featureName, Identifier id, Object feature) {
        Map m = getFolder (featureName);
        for (int i = id.name.size () - 1; i > 0; i--) {
            Object o = m.get (id.name.get (i));
            if (o instanceof MMap)
                m = (Map) o;
            else {
                Map mm = new MMap ();
                if (o != null)
                    mm.put ("", o);
                m.put (id.name.get (i), mm);
                m = mm;
            }
        }
        Object o = m.get (id.name.get (0));
        if (o != null && o instanceof Map)
            ((Map) o).put ("", feature);
        else
            m.put (
                id.name.get (0),
                feature
            );
    }
    
    private Map getFolder (String featureName) {
        Map m = (Map) features.get (featureName);
        if (m == null) {
            m = new HashMap ();
            features.put (featureName, m);
        }
        return m;
    }
    
    void print () {
        System.out.println("\nPrint " + mimeType);
        System.out.println("Tokens:");
        Iterator it = tokenTypes.iterator ();
        while (it.hasNext ()) {
            TokenType r = (TokenType) it.next ();
            System.out.println("  " + r);
        }
        System.out.println("Grammar Rules:");
        it = getAnalyser ().getRules ().iterator ();
        while (it.hasNext ()) {
            LLSyntaxAnalyser.Rule r = (LLSyntaxAnalyser.Rule) it.next ();
            System.out.println("  " + r);
        }
    }
    
    
    // colors ..................................................................
    
    private static Map getDefaultColors () {
        Collection defaults = EditorSettings.getDefault ().
            getDefaultFontColorDefaults ("NetBeans");
        Map defaultsMap = new HashMap ();
        Iterator it = defaults.iterator (); // check if IDE Defaults module is installed
        while (it.hasNext ()) {
            AttributeSet as = (AttributeSet) it.next ();
            defaultsMap.put (
                as.getAttribute (StyleConstants.NameAttribute),
                as
            );
        }
        return defaultsMap;
    }
    
    private Map getCurrentColors () {
        // current colors
        FontColorSettingsFactory fcsf = EditorSettings.getDefault ().
            getFontColorSettings (new String[] {getMimeType ()});
        Collection colors = fcsf.getAllFontColors ("NetBeans");
        Map colorsMap = new HashMap ();
        Iterator it = colors.iterator ();
        while (it.hasNext ()) {
            AttributeSet as = (AttributeSet) it.next ();
            colorsMap.put (
                as.getAttribute (StyleConstants.NameAttribute),
                as
            );
        }
        return colorsMap;
    }
    
    public Map getColorMap () {
        Map defaultsMap = getDefaultColors ();
        Map colorsMap = getCurrentColors ();
        Iterator it = getTokens ().iterator ();
        while (it.hasNext ()) {
            TokenType token = (TokenType) it.next ();
            Object obj = getFeature(Language.COLOR, token.getType ());
            if (obj != null) {
                for (Iterator iter = ((List)obj).iterator(); iter.hasNext(); ) {
                    SimpleAttributeSet as = (SimpleAttributeSet)iter.next();
                    String id = (String)as.getAttribute("color_name"); // NOI18N
                    if (id == null)
                        id = token.getType ();
                    addColor (id, as, colorsMap, defaultsMap);
                }
            }
        }
        Map m = getFeature (Language.COLOR);
        if (m == null)
            return Collections.EMPTY_MAP;
        it = m.keySet ().iterator ();
        while (it.hasNext ()) {
            String type = (String) it.next ();
            if (colorsMap.containsKey (type))
                continue;
            Object obj = m.get (type);
            if (obj != null) {
                for (Iterator iter = ((List)obj).iterator(); iter.hasNext(); ) {
                    SimpleAttributeSet as = (SimpleAttributeSet) iter.next();
                    addColor (type, as, colorsMap, defaultsMap);
                }
            }
        }
        addColor ("error", null, colorsMap, defaultsMap);
        return colorsMap;
    }
    
    private void addColor (
        String tokenType, 
        SimpleAttributeSet sas,
        Map colorsMap, 
        Map defaultsMap
    ) {
        if (sas == null)
            sas = new SimpleAttributeSet ();
        else
            sas = new SimpleAttributeSet (sas);
        String colorName = (String) sas.getAttribute (StyleConstants.NameAttribute);
        if (colorName == null)
            colorName = tokenType;
        sas.addAttribute (StyleConstants.NameAttribute, colorName);
        sas.addAttribute (EditorStyleConstants.DisplayName, colorName);
        if (!sas.isDefined (EditorStyleConstants.Default)) {
            String def = colorName;
            int i = def.lastIndexOf ('_');
            if (i > 0) def = def.substring (i + 1);
            if (defaultsMap.containsKey (def))
                sas.addAttribute (EditorStyleConstants.Default, def);
        }
        colorsMap.put (colorName, sas);
    }
    
    
    // innerclasses ............................................................

    private class MMap extends HashMap {
        
    }
    
    public static class Identifier {
        private List name;
        
        public String toString () {
            StringBuilder sb = new StringBuilder ();
            sb.append (name.get (0));
            Iterator it = name.iterator ();
            it.next ();
            while (it.hasNext ())
                sb.append ('.').append (it.next ());
            return sb.toString ();
        }
    }
    
    public static class Navigator {
        
        private Evaluator   displayName;
        private Evaluator   tooltip;
        private Evaluator   icon;
        private Evaluator   isLeaf;
        
        private Navigator () {}
        
        public static Navigator create (
            Evaluator       displayName,
            Evaluator       tooltip,
            Evaluator       icon,
            Evaluator       isLeaf
        ) {
            Navigator n = new Navigator ();
            n.displayName = displayName;
            n.tooltip = tooltip;
            n.icon = icon;
            n.isLeaf = isLeaf;
            return n;
        }
        
        public Evaluator getDisplayName () {
            return displayName;
        }
        
        public Evaluator getTooltip () {
            return tooltip;
        }
        
        public Evaluator getIcon () {
            return icon;
        }
        
        public Evaluator isLeaf () {
            return isLeaf;
        }
    }
    
    public static final class TokenType {
        
        private String  startState;
        private Pattern pattern;
        private String  type;
        private String  endState;
        private int     priority;
        private Map     properties;
        
        private TokenType (
            String      startState,
            Pattern     pattern,
            String      type,
            String      endState,
            int         priority,
            Map         properties
        ) {
            this.startState = startState == null ? Parser.DEFAULT_STATE : startState;
            this.pattern = pattern;
            this.type = type;
            this.endState = endState == null ? Parser.DEFAULT_STATE : endState;
            this.priority = priority;
            this.properties = properties == null ? Collections.EMPTY_MAP : properties;
        }
        
        public String getType () {
            return type;
        }
        
        public String getStartState () {
            return startState;
        }
        
        public String getEndState () {
            return endState;
        }
        
        public Pattern getPattern () {
            return pattern;
        }
        
        public int getPriority () {
            return priority;
        }
        
        public Map getProperties () {
            return properties;
        }
        
        public String toString () {
            return "Rule " + startState + " : type " + type + " : " + endState;
        }
    }
}


