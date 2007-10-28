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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.languages.features;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import junit.framework.TestCase;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.languages.*;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Selector;
import org.netbeans.modules.languages.dataobject.LanguagesEditorKit;
import org.netbeans.modules.languages.features.LanguagesFoldManager.FoldItem;
import org.netbeans.modules.languages.features.LanguagesNavigatorModel;
import org.netbeans.modules.languages.features.LanguagesNavigatorModel.NavigatorNode;
import org.netbeans.modules.languages.lexer.SLanguageHierarchy;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser;
import org.netbeans.modules.languages.Rule;
import org.netbeans.modules.languages.parser.Parser;
import org.netbeans.modules.languages.parser.Pattern;

/**
 *
 * @author Daniel Prusa
 */
public class NavigatorTest extends TestCase {
    
    public static final String TEST_MIME_TYPE="text/mt";
    
    public NavigatorTest (String testName) {
        super(testName);
    }
    
    public void testAST1 () throws Exception {
        List<TokenType> tokenTypes = new ArrayList<TokenType> ();
        tokenTypes.add (new TokenType (null, Pattern.create ("'if' | 'while' | 'function'"), "keyword", 0, null, 0, null));
        tokenTypes.add (new TokenType (null, Pattern.create ("['a'-'z' '_']+"), "identifier", 1, null, 1, null));
        tokenTypes.add (new TokenType (null, Pattern.create ("'(' | ')' | '{' | '}'"), "operator", 2, null, 2, null));
        tokenTypes.add (new TokenType (null, Pattern.create ("[' ' '\n' '\t' '\r']+"), "whitespace", 3, null, 3, null));
        Map<Integer,String> tokensMap = new HashMap<Integer,String> ();
        tokensMap.put (0, "keyword");
        tokensMap.put (1, "identifier");
        tokensMap.put (2, "operator");
        tokensMap.put (3, "whitespace");
        List<Feature> features = new ArrayList<Feature> ();
        Map<String,String> exprs = new HashMap<String,String> ();
        exprs.put ("display_name", "$FunctionName$");
        features.add (Feature.create (
            "NAVIGATOR",
            Selector.create ("Function"),
            exprs,
            Collections.<String,String>emptyMap(),
            Collections.<String,Pattern>emptyMap ()
        ));
        Language language = Language.create (TEST_MIME_TYPE, tokensMap, features, Parser.create (tokenTypes));
        
        ASTToken IDENTIFIER = ASTToken.create (language, "identifier", null, 0, "identifier".length (), null);
        ASTToken IF = ASTToken.create (language, "keyword", "if", 0, "keyword".length (), null);
        ASTToken WHILE = ASTToken.create (language, "keyword", "while", 0, "keyword".length (), null);
        ASTToken PARENTHESIS = ASTToken.create (language, "operator", "(", 0, "operator".length (), null);
        ASTToken PARENTHESIS2 = ASTToken.create (language, "operator", ")", 0, "operator".length (), null);
        ASTToken BRACE = ASTToken.create (language, "operator", "{", 0, "operator".length (), null);
        ASTToken BRACE2 = ASTToken.create (language, "operator", "}", 0, "operator".length (), null);
        ASTToken FUNCTION = ASTToken.create (language, "keyword", "function", 0, "keyword".length (), null);
    
        List<Rule> rules = new ArrayList<Rule> ();
        rules.add (Rule.create ("S", Arrays.asList (new Object[] {"Function", "S"})));
        rules.add (Rule.create ("S", Arrays.asList (new Object[] {})));
        rules.add (Rule.create ("Function", Arrays.asList (new Object[] {FUNCTION, "FunctionName", PARENTHESIS, PARENTHESIS2, "Block"})));
        rules.add (Rule.create ("FunctionName", Arrays.asList (new Object[] {IDENTIFIER})));
        rules.add (Rule.create ("Statement", Arrays.asList (new Object[] {"IfStatement"})));
        rules.add (Rule.create ("Statement", Arrays.asList (new Object[] {"WhileStatement"})));
        rules.add (Rule.create ("Statement", Arrays.asList (new Object[] {"Block"})));
        rules.add (Rule.create ("IfStatement", Arrays.asList (new Object[] {IF, PARENTHESIS, "ConditionalExpression", PARENTHESIS2, "Block"})));
        rules.add (Rule.create ("WhileStatement", Arrays.asList (new Object[] {WHILE, PARENTHESIS, "ConditionalExpression", PARENTHESIS2, "Block"})));
        rules.add (Rule.create ("ConditionalExpression", Arrays.asList (new Object[] {IDENTIFIER})));
        rules.add (Rule.create ("Block", Arrays.asList (new Object[] {BRACE, "Block1", BRACE2})));
        rules.add (Rule.create ("Block1", Arrays.asList (new Object[] {IDENTIFIER, "Block1"})));
        rules.add (Rule.create ("Block1", Arrays.asList (new Object[] {"Statement", "Block1"})));
        rules.add (Rule.create ("Block1", Arrays.asList (new Object[] {})));
        
        language.setAnalyser (LLSyntaxAnalyser.create (language, rules, Collections.<Integer>singleton (3)));
        LanguagesManager.getDefault ().addLanguage (language);
        
        String text = 
            "function fnc_1() {\n" +
            "    if (true) {\n" +
            "        id_one\n" +
            "        while (false) {\n" +
            "            id_two\n" +
            "            id_three\n" +
            "        }\n" +
            "        if (true) {id_four}\n" +
            "    }\n" +
            "}\n" +
            "function fnc_2() {\n" +
            "    if (true) {\n" +
            "        id_five\n" +
            "        while (false) {\n" +
            "            id_six\n" +
            "            id_seven\n" +
            "        }\n" +
            "        if (true) {id_eight}\n" +
            "    }\n" +
            "}\n";
        
        final JEditorPane pane = new JEditorPane ();
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                pane.setEditorKit(new LanguagesEditorKit(TEST_MIME_TYPE));
            }
        });
        
        NbEditorDocument doc = (NbEditorDocument)pane.getDocument();
        doc.putProperty ("mimeType", language.getMimeType ());
        doc.putProperty (org.netbeans.api.lexer.Language.class, new SLanguageHierarchy (language.getMimeType ()).language ());

        ParserManager parserManager = ParserManager.get(doc);
        pane.setText (text);
        
        int counter = 0;
        try {
            while (((parserManager.getState() == ParserManager.State.NOT_PARSED) ||
                    (parserManager.getState() == ParserManager.State.PARSING))
                    && counter < 200) {
                Thread.sleep(100);
                counter++;
            }
        } catch (InterruptedException e) {
        }
        
        ASTNode root = parserManager.getAST();
        LanguagesNavigatorModel model = new LanguagesNavigatorModel ();
        model.setContext (doc);
        
        NavigatorNode rootNode = (NavigatorNode) model.getRoot();
        StringBuffer buf = new StringBuffer();
        treeToString(model, buf, rootNode);
        
        assertEquals ("root{fnc_1, fnc_2}", buf.toString ());
    }

    private void treeToString (LanguagesNavigatorModel model, StringBuffer buf, NavigatorNode node) {
        buf.append(node.displayName);
        int count = model.getChildCount(node);
        if (count == 0) {
            return;
        }
        buf.append('{');
        for (int x = 0; x < count; x++) {
            NavigatorNode n = (NavigatorNode)model.getChild(node, x);
            treeToString(model, buf, n);
            if (x < count - 1) {
                buf.append(", ");
            }
        }
        buf.append('}');
    }
    
}




