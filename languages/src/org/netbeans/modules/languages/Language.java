/*
 * Language.java
 *
 * Created on February 10, 2006, 11:15 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.languages;

import org.netbeans.modules.languages.parser.*;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.*;
import java.util.List;


/**
 *
 * @author Jan Jancura
 */
public class Language {

    
    public static final String ACTION = "ACTION";
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
    public static final String ANALYZE = "ANALYZE";
    public static final String PROPERTIES = "PROPERTIES";
    public static final String REFORMAT = "REFORMAT";
    public static final String SKIP = "SKIP";
    public static final String STORE = "STORE";
    public static final String TOOLTIP = "TOOLTIP";
    
    
    private Parser parser;
    private List parserRules = new ArrayList ();
    private Set skipTokenTypes = new HashSet ();
    private String mimeType;
    private LLSyntaxAnalyser analyser = null;
    private Map analyserRules = new HashMap ();

    
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
            parser = Parser.create (parserRules);
        return parser;
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
            analyser = LLSyntaxAnalyser.create (analyserRules, skipTokenTypes, getProperties (mimeType), this);
            return analyser;
        }
    }
    
    public Object getProperty (
        String mimeType, 
        String propertyName
    ) {
        if (!properties.containsKey (mimeType)) return null;
        Map m = (Map) properties.get (mimeType);
        return m.get (propertyName);
    }

    
    // package private interface ...............................................
    
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

    void addToken (
        String      startState,
        SToken       token,
        Pattern     pattern,
        String      endState
    ) {
        if (parser != null)
            throw new InternalError ();
        parserRules.add (Parser.create (
            startState,
            pattern,
            token,
            endState
        ));
    }
    
    void addSkipTokenType (String tokenType) {
        skipTokenTypes.add (tokenType);
    }
    
    void addRule (ASTNode rule) {
        if (analyser != null)
            throw new InternalError ();
        List r = (List) analyserRules.get (mimeType);
        if (r == null) {
            r = new ArrayList ();
            analyserRules.put (mimeType, r);
        }
        r.add (rule);
    }

    private Map properties = new HashMap ();
    
    void addProperties (Map properties) {
        Map m = (Map) this.properties.get (mimeType);
        if (m == null)
            this.properties.put (mimeType, properties);
        else
            m.putAll (properties);
    }
    
    void addProperty (String key, Object value) {
        Map m = (Map) properties.get (mimeType);
        if (m == null) {
            m = new HashMap ();
            properties.put (mimeType, m);
        }
        m.put (key, value);
    }
    
    void importLanguage (
        String name,
        Map properties
    ) throws ParseException {
        String state = (String) ((Evaluator) properties.get ("state")).evaluate ();
        String mimeType = (String) ((Evaluator) properties.get ("mimeType")).evaluate ();
        Language l = LanguagesManager.getDefault ().getLanguage (mimeType);
        
        // import tokens
        Iterator it = l.parserRules.iterator ();
        while (it.hasNext ()) {
            Parser.Rule r = (Parser.Rule) it.next ();
            String startState = r.getStartState ();
            Pattern pattern = r.getPattern ().clonePattern ();
            SToken token = (SToken) r.getToken ();
            String endState = r.getEndState ();
            if (startState == null || Parser.DEFAULT_STATE.equals (startState)) 
                startState = state;
            else
                startState = name + '-' + startState;
            if (endState == null || Parser.DEFAULT_STATE.equals (endState)) 
                endState = state;
            else
                endState = name + '-' + endState;
            addToken (startState, token, pattern, endState);
        }
        
        // import grammar rues
        analyserRules.putAll (l.analyserRules);
        // import colorings
        importColorings (l);
        // import other features
        importFeature (ACTION, l);
        importFeature (COLOR, l);
        //importFeature (COMPLETE, l);
        importFeature (COMPLETION, l);
        importFeature (FOLD, l);
        importFeature (HYPERLINK, l);
        importFeature (INDENT, l);
        importFeature (MARK, l);
        importFeature (NAVIGATOR, l);
        importFeature (PARSE, l);
        importFeature (ANALYZE, l);
        // import properties
        this.properties.putAll (l.properties);
        importFeature (REFORMAT, l);
        // import skip tokens
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
    
    private Map getProperties (
        String mimeType
    ) {
        if (!properties.containsKey (mimeType)) return null;
        return (Map) properties.get (mimeType);
    }
    
    private Map features = new HashMap ();
    
    public Object getFeature (String featureName, ASTNode node) {
        Map m = (Map) features.get (featureName);
        if (m == null) return null;
        Map mm = (Map) m.get (node.getMimeType ());
        if (mm == null) return null;
        while (true) {
            Object value = mm.get (node.getNT ());
            if (value == null) return mm.get ("");
            if (value instanceof MMap) {
                mm = (Map) value;
                node = node.getParent ();
                if (node == null) return null;
                continue;
            }
            return value;
        }
    }
    
    public Object getFeature (String featureName, String mimeType, String nt) {
        Map m = (Map) features.get (featureName);
        if (m == null) return null;
        Map mm = (Map) m.get (mimeType);
        if (mm == null) return null;
        Object value = mm.get (nt);
        if (value == null) return mm.get ("");
        if (value instanceof MMap) {
            mm = (Map) value;
            return mm.get ("");
        }
        return value;
    }
    
    public Object getFeature (String featureName, SToken token) {
        Map m = (Map) features.get (featureName);
        if (m == null) return null;
        Map mm = (Map) m.get (token.getMimeType ());
        if (mm == null) return null;
        Object result = mm.get (token.getType ());
        if (result instanceof MMap) return null;
        return result;
    }
    
    public boolean supportsFeature (String featureName) {
        return features.get (featureName) != null;
    }

    void addFeature (String featureName, Identifier id, Object feature) {
        Map m = getFolder (featureName, mimeType);
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
    
    private Map getFolder (String featureName, String mimeType) {
        Map m = (Map) features.get (featureName);
        if (m == null) {
            m = new HashMap ();
            features.put (featureName, m);
        }
        Map mm = (Map) m.get (mimeType);
        if (mm == null) {
            mm = new HashMap ();
            m.put (mimeType, mm);
        }
        return mm;
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
}


