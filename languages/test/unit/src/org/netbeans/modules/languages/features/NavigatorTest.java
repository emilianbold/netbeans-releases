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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.languages.features;

import java.lang.reflect.InvocationTargetException;
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
import org.netbeans.modules.languages.features.LanguagesNavigator.Model;
import org.netbeans.modules.languages.features.LanguagesNavigator.NavigatorNode;
import org.netbeans.modules.languages.lexer.SLanguageHierarchy;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser.Rule;
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
        Language l = new Language (TEST_MIME_TYPE);
        l.addToken (null, "keyword", Pattern.create ("'if' | 'while' | 'function'"), null, null);
        l.addToken (null, "identifier", Pattern.create ("['a'-'z' '_']+"), null, null);
        l.addToken (null, "operator", Pattern.create ("'(' | ')' | '{' | '}'"), null, null);
        l.addToken (null, "whitespace", Pattern.create ("[' ' '\n' '\t' '\r']+"), null, null);
        
        l.addFeature (Feature.create ("SKIP", Selector.create ("whitespace")));
        
        ASTToken IDENTIFIER = ASTToken.create (null, "identifier", null, 0);
        ASTToken IF = ASTToken.create (null, "keyword", "if", 0);
        ASTToken WHILE = ASTToken.create (null, "keyword", "while", 0);
        ASTToken PARENTHESIS = ASTToken.create (null, "operator", "(", 0);
        ASTToken PARENTHESIS2 = ASTToken.create (null, "operator", ")", 0);
        ASTToken BRACE = ASTToken.create (null, "operator", "{", 0);
        ASTToken BRACE2 = ASTToken.create (null, "operator", "}", 0);
        ASTToken FUNCTION = ASTToken.create (null, "keyword", "function", 0);
    
        l.addRule (Rule.create ("S", Arrays.asList (new Object[] {"Function", "S"})));
        l.addRule (Rule.create ("S", Arrays.asList (new Object[] {})));
        l.addRule (Rule.create ("Function", Arrays.asList (new Object[] {FUNCTION, "FunctionName", PARENTHESIS, PARENTHESIS2, "Block"})));
        l.addRule (Rule.create ("FunctionName", Arrays.asList (new Object[] {IDENTIFIER})));
        l.addRule (Rule.create ("Statement", Arrays.asList (new Object[] {"IfStatement"})));
        l.addRule (Rule.create ("Statement", Arrays.asList (new Object[] {"WhileStatement"})));
        l.addRule (Rule.create ("Statement", Arrays.asList (new Object[] {"Block"})));
        l.addRule (Rule.create ("IfStatement", Arrays.asList (new Object[] {IF, PARENTHESIS, "ConditionalExpression", PARENTHESIS2, "Block"})));
        l.addRule (Rule.create ("WhileStatement", Arrays.asList (new Object[] {WHILE, PARENTHESIS, "ConditionalExpression", PARENTHESIS2, "Block"})));
        l.addRule (Rule.create ("ConditionalExpression", Arrays.asList (new Object[] {IDENTIFIER})));
        l.addRule (Rule.create ("Block", Arrays.asList (new Object[] {BRACE, "Block1", BRACE2})));
        l.addRule (Rule.create ("Block1", Arrays.asList (new Object[] {IDENTIFIER, "Block1"})));
        l.addRule (Rule.create ("Block1", Arrays.asList (new Object[] {"Statement", "Block1"})));
        l.addRule (Rule.create ("Block1", Arrays.asList (new Object[] {})));
        
        Map<String,String> exprs = new HashMap<String,String> ();
        exprs.put ("display_name", "$FunctionName$");
        l.addFeature (Feature.create (
            "NAVIGATOR",
            Selector.create ("Function"),
            exprs,
            Collections.<String,String>emptyMap(),
            Collections.<String,Pattern>emptyMap ()
        ));
        
        LanguagesManager.getDefault ().addLanguage (l);
        
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
        doc.putProperty ("mimeType", l.getMimeType ());
        doc.putProperty (org.netbeans.api.lexer.Language.class, new SLanguageHierarchy (l.getMimeType ()).language ());

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
        Model model = new Model();
        model.setContext(root, null, doc);
        
        NavigatorNode rootNode = (NavigatorNode)model.getRoot();
        StringBuffer buf = new StringBuffer();
        treeToString(model, buf, rootNode);
        
        assertEquals (buf.toString(), "root{fnc_1, fnc_2}");
    }

    private void treeToString(Model model, StringBuffer buf, NavigatorNode node) {
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




