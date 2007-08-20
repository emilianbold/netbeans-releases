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

import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.List;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.TokenInput;
import org.netbeans.api.languages.TokenInput;
import org.netbeans.modules.languages.Language.TokenType;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.modules.languages.Utils;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser.Rule;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.modules.languages.parser.Parser;
import org.netbeans.modules.languages.parser.Pattern;
import org.netbeans.modules.languages.parser.Petra;
import org.netbeans.modules.languages.parser.StringInput;
import org.netbeans.modules.languages.parser.TokenInputUtils;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Jancura
 */
public class Language extends org.netbeans.api.languages.Language {

    public static final String ACTION = "ACTION";
    public static final String AST = "AST";
    public static final String BRACE = "BRACE";
    public static final String BUNDLE = "BUNDLE";
    public static final String COLOR = "COLOR";
    public static final String COMMENT_LINE = "COMMENT_LINE";
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
    public static final String SELECTION = "SELECTION";
    public static final String SKIP = "SKIP";
    public static final String STORE = "STORE";
    public static final String TOKEN = "TOKEN";
    public static final String TOOLTIP = "TOOLTIP";
    
    private Parser              parser;
    private List<TokenType>     tokenTypes = new ArrayList<TokenType> ();
    private Set<String>         skipTokenTypes;
    private String              mimeType;
    private ParseException      analyserException;
    private LLSyntaxAnalyser    analyser;
    private List<ASTNode>       grammarASTNodes = new ArrayList<ASTNode> ();
    private List<Rule>          grammarRules;
    private List<Language>      importedLangauges = new ArrayList<Language> ();
    private boolean             bundleResolved = false;
    private ResourceBundle      bundle;

    
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
    
    public List<TokenType> getTokenTypes () {
        return Collections.unmodifiableList (tokenTypes);
    }
    
    public Set<String> getSkipTokenTypes () {
        if (skipTokenTypes == null) {
            skipTokenTypes = new HashSet<String> ();
            List<Feature> ss = getFeatures ("SKIP");
            Iterator<Feature> it = ss.iterator ();
            while (it.hasNext ()) {
                Feature s = it.next ();
                skipTokenTypes.add (s.getSelector ().getAsString ());
            }
        }
        return skipTokenTypes;
    }
    
    public boolean hasAnalyser () {
        return !grammarASTNodes.isEmpty ();
    }
    
    public LLSyntaxAnalyser getAnalyser () throws ParseException {
        if (analyserException != null) throw analyserException;
        if (analyser != null) return analyser;
        synchronized (this) {
            if (analyserException != null) throw analyserException;
            if (analyser != null) return analyser;
            try {
                analyser = LLSyntaxAnalyser.create (this);
                return analyser;
            } catch (ParseException ex) {
                analyserException = ex;
                throw ex;
            }
        }
    }
    
    public List<Language> getImportedLanguages () {
        return importedLangauges;
    }
    
    public static TokenType createTokenType (
        String              startState,
        Pattern             pattern,
        String              type,
        String              endState,
        int                 priority,
        Feature             properties
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

    public String localize(String str) {
        if (!bundleResolved) {
            Feature bundleFeature = getFeature("BUNDLE");
            if (bundleFeature != null) {
                String baseName = (String)bundleFeature.getValue();
                if (baseName != null) {
                    try {
                        bundle = NbBundle.getBundle(baseName);
                    } catch (MissingResourceException e) {
                        Utils.notify (e);
                    }
                }
            }
            bundleResolved = true;
        }
        if (str == null) {
            return null;
        }
        if (bundle != null) {
            try {
                return bundle.getString(str);
            } catch (MissingResourceException e) {
            }
        }
        return str;
    }
    
    // package private interface ...............................................

    public void addToken (
        String      startState,
        String      type,
        Pattern     pattern,
        String      endState,
        Feature     properties
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
    
    void addRule (ASTNode rule) {
        if (analyser != null)
            throw new InternalError ();
        grammarASTNodes.add (rule);
    }
    
    public void addRule (Rule rule) {
        if (grammarRules == null) grammarRules = new ArrayList<Rule> ();
        grammarRules.add (rule);
    }
    
    public List<Rule> getRules () {
        if (grammarRules == null)
            grammarRules = Petra.convert (grammarASTNodes, getMimeType ());
        return grammarRules;
    }
    
    private Feature preprocessorImport;
    
    public Feature getPreprocessorImport () {
        return preprocessorImport;
    }
    
    private Map<String,Feature> tokenImports = new HashMap<String,Feature> ();
    
    public Map<String,Feature> getTokenImports () {
        return tokenImports;
    }
    
    void importLanguage (
        Feature feature
    ) {
        try {
            String mimeType = (String) feature.getValue ("mimeType");
            Language language = LanguagesManager.getDefault ().getLanguage (mimeType);
            if (feature.getPattern ("start") != null) {
                //feature.put ("token", "PE");
                assert (preprocessorImport == null);
                preprocessorImport = feature;
                importedLangauges.add (language);
                return;
            }
            if (feature.getValue ("state") == null) {
                String tokenName = feature.getSelector ().getAsString ();
                assert (!tokenImports.containsKey (tokenName));
                tokenImports.put (tokenName, feature);
                importedLangauges.add (language);
                return;
            }

            String state = (String) feature.getValue ("state"); 
            String tokenName = feature.getSelector ().getAsString ();

            // import tokenTypes
            Iterator<TokenType> it = language.getTokenTypes ().iterator ();
            while (it.hasNext ()) {
                TokenType tt = it.next ();
                String startState = tt.getStartState ();
                Pattern pattern = tt.getPattern ().clonePattern ();
                String endState = tt.getEndState ();
                if (startState == null || Parser.DEFAULT_STATE.equals (startState)) 
                    startState = state;
                else
                    startState = tokenName + '-' + startState;
                if (endState == null || Parser.DEFAULT_STATE.equals (endState)) 
                    endState = state;
                else
                    endState = tokenName + '-' + endState;
                addToken (startState, tt.getType (), pattern, endState, tt.getProperties ());
            }

            // import grammar rues
            grammarASTNodes.addAll (language.grammarASTNodes);
            // import features
            importAllFeatures (language);
            importedLangauges.addAll (language.importedLangauges);
            tokenImports.putAll (language.tokenImports);
        } catch (LanguageDefinitionNotFoundException ex) {
            Utils.notify ("Editors/" + mimeType + "/language.nbs:", ex);
        }
    }

    
    // private helper methods ..................................................
    
    private void importAllFeatures (Language l) {
        Iterator<String> it = l.featureLists.keySet ().iterator ();
        while (it.hasNext ()) {
            String featureName = it.next ();
            List<Feature> features = l.getFeatures (featureName);
            Iterator<Feature> it2 = features.iterator ();
            while (it2.hasNext ()) {
                Feature f = it2.next ();
                addFeature (f);
            }
        }
    }

    private Map<String,List<Feature>> featureLists = new HashMap<String,List<Feature>> ();
    private Map<String,Object> featuresMap = new HashMap<String,Object> ();
    private static final Object BLA = new Object ();
    
    public void addFeature (Feature feature) {
        String featureName = feature.getFeatureName ();
        if (featureName.equals ("IMPORT")) {
            importLanguage (feature);
            return;
        }
        
        List<Feature> list = featureLists.get (featureName);
        if (list == null) {
            list = new ArrayList<Feature> ();
            featureLists.put (featureName, list);
        }
        list.add (feature);

        if (feature.getSelector () == null) {
            Object o = featuresMap.get (featureName);
            if (o == null)
                featuresMap.put (featureName, feature);
            else
            if (o instanceof List)
                ((List) o).add (feature);
            else {
                List<Feature> l = new ArrayList<Feature> ();
                l.add ((Feature) o);
                l.add (feature);
                featuresMap.put (featureName, l);
            }
            return;
        }
        Map m = (Map) featuresMap.get (featureName);
        if (m == null) {
            m = new HashMap ();
            featuresMap.put (featureName, m);
        }
        List<String> path = feature.getSelector ().getPath ();
        for (int i = path.size () - 1; i > 0; i--) {
            String name = path.get (i);
            Object o = m.get (name);
            if (o instanceof Map)
                m = (Map) o;
            else {
                Map mm = new HashMap ();
                if (o != null)
                    mm.put (BLA, o);
                m.put (name, mm);
                m = mm;
            }
        }
        String name = path.get (0);
        Object o = m.get (name);
        if (o instanceof List)
            ((List<Feature>) o).add (feature);
        else
        if (o instanceof Map) {
            m = (Map) o;
            o = m.get (BLA);
            if (o instanceof List)
                ((List) o).add (feature);
            else
            if (o == null)
                m.put (BLA, feature);
            else {
                List l = new ArrayList ();
                l.add (o);
                l.add (feature);
                m.put (BLA, l);
            }
        } else
        if (o == null) 
            m.put (name, feature);
        else {
            List l = new ArrayList ();
            l.add (o);
            l.add (feature);
            m.put (name, l);
        }
    }

    public List<Feature> getFeatures (String featureName) {
        List<Feature> r = featureLists.get (featureName);
        if (r != null) return r;
        return Collections.<Feature>emptyList ();
    }

    public Feature getFeature (String featureName) {
        List<Feature> r = featureLists.get (featureName);
        if (r == null) return null;
        if (r.size () == 1) return r.get (0);
        throw new IllegalArgumentException ();
    }
    
    public Feature getFeature (String featureName, ASTPath path) {
        List<Feature> r = getFeatures (featureName, path);
        if (r.isEmpty ()) return null;
        if (r.size () == 1) return r.get (0);
        throw new IllegalArgumentException ();
    }
    
    public Feature getFeature (String featureName, String id) {
        Map m = (Map) featuresMap.get (featureName);
        if (m == null) return null;
        Object o = m.get (id);
        if (o instanceof Map)
            o = ((Map) o).get (BLA);
        if (o == null) return null;
        if (o instanceof Feature)
            return (Feature) o;
        List<Feature> r = (List<Feature>) o;
        if (r.isEmpty ()) return null;
        if (r.size () == 1) return r.get (0);
        throw new IllegalArgumentException ();
    }
    
    public List<Feature> getFeatures (String featureName, String id) {
        Map m = (Map) featuresMap.get (featureName);
        if (m == null) return Collections.<Feature>emptyList ();
        Object o = m.get (id);
        if (o instanceof Map)
            o = ((Map) o).get (BLA);
        if (o == null) return Collections.<Feature>emptyList ();
        if (o instanceof Feature)
            return Collections.<Feature>singletonList ((Feature) o);
        return (List<Feature>) o;
    }

    public List<Feature> getFeatures (String featureName, ASTPath path) {
        Map m = (Map) featuresMap.get (featureName);
        if (m == null) return Collections.<Feature>emptyList ();
        Object last = null;
        int i = path.size () - 1;
        for (; i >= 0; i--) {
            ASTItem item = path.get (i);
            String name = item instanceof ASTToken ?
                ((ASTToken) item).getType () :
                ((ASTNode) item).getNT ();
            Object o = m.get (name);
            if (m.containsKey (BLA))
                last = m.get (BLA);
            if (o instanceof Map) {
                m = (Map) o;
                continue;
            }
            if (o instanceof List)
                return (List<Feature>) o;
            if (o != null)
                return Collections.<Feature>singletonList ((Feature) o);
            if (last != null) {
                if (last instanceof List)
                    return (List<Feature>) last;
                 return Collections.<Feature>singletonList ((Feature) last);
            }
            break;
        }
        return Collections.<Feature>emptyList ();
    }
    
//    public Object getFeature (String featureName, ASTPath path) {
//        Map m = (Map) features.get (featureName);
//        if (m == null) return null;
//        ListIterator<ASTItem> it = path.listIterator (path.size ());
//        while (it.hasPrevious ()) {
//            ASTItem item = it.previous ();
//            Object value = (item instanceof ASTToken) ? 
//                m.get (((ASTToken) item).getType ()) :
//                m.get (((ASTNode) item).getNT ());
//            if (value == null) return m.get ("");
//            if (value instanceof MMap)
//                m = (Map) value;
//            else
//                return value;
//        }
//        return null;
//    }
//    
//    public Object getFeature (String featureName, String id) {
//        Map m = (Map) features.get (featureName);
//        if (m == null) return null;
//        Object value = m.get (id);
//        if (value == null) return m.get ("");
//        if (value instanceof MMap) {
//            m = (Map) value;
//            return m.get ("");
//        }
//        return value;
//    }
//
//    public Collection getFeatures (String featureName) {
//        Map m = (Map) features.get (featureName);
//        if (m == null) return null;
//        return m.values ();
//    }
////    public Map getFeature (String featureName) {
////        return (Map) features.get (featureName);
////    }
//    
////    private Object getFeature (String featureName, ASTItem item) {
////        if (item instanceof ASTNode)
////            return getFeature (featureName, (ASTNode) item);
////        return getFeature (featureName, (ASTToken) item);
////    }
////    
////    private Object getFeature (String featureName, ASTToken token) {
////        Map m = (Map) features.get (featureName);
////        if (m == null) return null;
////        Object result = m.get (token.getType ());
////        if (result instanceof MMap) return null;
////        return result;
////    }
    
//    public boolean supportsFeature (String featureName) {
//        return features.get (featureName) != null;
//    }
//
//    void addFeature (String featureName, Identifier id, Object feature) {
//        Map m = getFolder (featureName);
//        for (int i = id.name.size () - 1; i > 0; i--) {
//            Object o = m.get (id.name.get (i));
//            if (o instanceof MMap)
//                m = (Map) o;
//            else {
//                Map mm = new MMap ();
//                if (o != null)
//                    mm.put ("", o);
//                m.put (id.name.get (i), mm);
//                m = mm;
//            }
//        }
//        Object o = m.get (id.name.get (0));
//        if (o != null && o instanceof Map)
//            ((Map) o).put ("", feature);
//        else
//            m.put (
//                id.name.get (0),
//                feature
//            );
//    }
    
//    private Map getFolder (String featureName) {
//        Map m = (Map) features.get (featureName);
//        if (m == null) {
//            m = new HashMap ();
//            features.put (featureName, m);
//        }
//        return m;
//    }

    public ASTNode parse (InputStream is) throws IOException, ParseException {
        BufferedReader br = new BufferedReader (new InputStreamReader (is));
        StringBuilder sb = new StringBuilder ();
        String ln = br.readLine ();
        while (ln != null) {
            sb.append (ln).append ('\n');
            ln = br.readLine ();
        }
        TokenInput ti = TokenInputUtils.create (
            getMimeType (),
            getParser (), 
            new StringInput (sb.toString ()),
            Collections.emptySet ()
        );
        ASTNode root = getAnalyser ().read (ti, true, new boolean[] {false});
        Feature astProperties = getFeature ("AST");
        if (astProperties != null && root != null) {
            ASTNode root1 = (ASTNode) astProperties.getValue (
                "process", 
                SyntaxContext.create (null, ASTPath.create (root))
            );
            if (root1 != null)
                root = root1;
        }
        return root;
    }
    
    void print () throws ParseException {
        System.out.println("\nPrint " + mimeType);
        System.out.println("Tokens:");
        Iterator<TokenType> it = getTokenTypes ().iterator ();
        while (it.hasNext ()) {
            TokenType r = it.next ();
            System.out.println("  " + r);
        }
        System.out.println("Grammar Rules:");
        Iterator<Rule> it2 = getAnalyser ().getRules ().iterator ();
        while (it.hasNext ()) {
            Rule r = it2.next ();
            System.out.println("  " + r);
        }
    }
    
    public String toString () {
        return "Language " + mimeType;
    }
    
    
    // innerclasses ............................................................

    public static final class TokenType {
        
        private String  startState;
        private Pattern pattern;
        private String  type;
        private String  endState;
        private int     priority;
        private Feature properties;
        
        private TokenType (
            String      startState,
            Pattern     pattern,
            String      type,
            String      endState,
            int         priority,
            Feature     properties
        ) {
            this.startState = startState == null ? Parser.DEFAULT_STATE : startState;
            this.pattern = pattern;
            this.type = type;
            this.endState = endState == null ? Parser.DEFAULT_STATE : endState;
            this.priority = priority;
            this.properties = properties;
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
        
        public Feature getProperties () {
            return properties;
        }
        
        public String toString () {
            return "Rule " + startState + " : type " + type + " : " + endState;
        }
    }
}


