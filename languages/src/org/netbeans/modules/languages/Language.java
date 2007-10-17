/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.languages;

import org.netbeans.api.languages.ASTEvaluator;
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
import org.netbeans.api.languages.ParserManager.State;
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
        if (skipTokenTypes == null)
            return Collections.<String>emptySet ();
        return skipTokenTypes;
    }
    
    void addSkipTokenType (String tokenType) {
        if (skipTokenTypes == null)
            skipTokenTypes = new HashSet<String> ();
        skipTokenTypes.add (tokenType);
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
            Feature bundleFeature = getFeature ("BUNDLE");
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
        featureList.importFeatures (l.featureList);
    }
    
    private FeatureList featureList = new FeatureList ();

    public void addFeature (Feature feature) {
        if (feature.getFeatureName().equals (SKIP))
            addSkipTokenType (feature.getSelector ().getAsString ());
        else
        if (feature.getFeatureName ().equals (IMPORT))
            importLanguage (feature);
        else
            featureList.add (feature);
    }

    public List<Feature> getFeatures (String featureName) {
        return featureList.getFeaturesRec (featureName);
    }

    public Feature getFeature (String featureName) {
        List<Feature> features = getFeatures (featureName);
        if (features.isEmpty ()) return null;
        return features.get (0);
    }
    
    public List<Feature> getFeatures (String featureName, String id) {
        return featureList.getFeatures (featureName, id);
    }

    public Feature getFeature (String featureName, String id) {
        List<Feature> features = getFeatures (featureName, id);
        if (features.isEmpty ()) return null;
        return features.get (0);
    }
    
    public List<Feature> getFeatures (String featureName, ASTPath path) {
        return featureList.getFeatures (featureName, path);
    }
    
    public Feature getFeature (String featureName, ASTPath path) {
        List<Feature> features = getFeatures (featureName, path);
        if (features.isEmpty ()) return null;
        return features.get (0);
    }
    
    void evaluate (
        State state, 
        List<ASTItem> path, 
        Map<String,Set<ASTEvaluator>> evaluatorsMap                             //,Map<Object,Long> times
    ) {
        featureList.evaluate (
            state, 
            path, 
            evaluatorsMap                                                       //,times
        );
    }
    

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


