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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.List;
import java.util.Map;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import junit.framework.TestCase;

import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.Context;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.modules.languages.Selector;
import org.netbeans.modules.languages.TestLanguage;
import org.netbeans.modules.languages.lexer.SLanguageHierarchy;
import org.netbeans.modules.languages.parser.Pattern;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;


/**
 *
 * @author Jan Jancura
 */
public class CompletionTest extends TestCase {
    
    public CompletionTest(String testName) {
        super(testName);
    }
    
    public void testTokenBased () throws ParseException {
        List<Feature> features = new ArrayList<Feature> ();
        Map<String,String> calls = new HashMap<String,String> ();
        calls.put ("text1", "org.netbeans.modules.languages.features.CompletionTest.completion1");
        features.add (Feature.create (
            "COMPLETION", 
            Selector.create ("keyword"),
            Collections.<String,String> emptyMap (),
            calls,
            Collections.<String,Pattern> emptyMap ()
        ));
        Language language = createTestLanguage (features);
        
        JEditorPane editor = createTestComponent (language);
        editor.setCaretPosition (27);
        CompletionProviderImpl cc = new CompletionProviderImpl ();
        CompletionTask ct = cc.createTask (0, editor);
        List<CompletionItem> list = cc.query (editor);
        assertEquals (2, list.size ());
        assertEquals ("while", list.get (0).getInsertPrefix ());
        assertEquals ("who", list.get (1).getInsertPrefix ());
//        assertEquals (editor, contextComponent);
        assertEquals (editor.getDocument (), contextDocument);
        assertEquals (26, contextPosition);
        assertEquals (null, contextPath);
    }
    
    public void testTokenBasedDirect () throws ParseException {
        List<Feature> features = new ArrayList<Feature> ();
        Map<String,String> calls = new HashMap<String,String> ();
        calls.put ("text1", "while");
        calls.put ("text2", "who");
        calls.put ("text3", "if");
        features.add (Feature.create (
            "COMPLETION", 
            Selector.create ("keyword"),
            calls,
            Collections.<String,String> emptyMap (),
            Collections.<String,Pattern> emptyMap ()
        ));
        Language language = createTestLanguage (features);
        
        JEditorPane editor = createTestComponent (language);
        editor.setCaretPosition (27);
        CompletionProviderImpl cc = new CompletionProviderImpl ();
        CompletionTask ct = cc.createTask (0, editor);
        List<CompletionItem> list = cc.query (editor);
        assertEquals (2, list.size ());
        assertEquals ("while", list.get (0).getInsertPrefix ());
        assertEquals ("who", list.get (1).getInsertPrefix ());
    }
    
    public void testASTBased () throws ParseException, BadLocationException {
        List<Feature> features = new ArrayList<Feature> ();
        Map<String,String> calls = new HashMap<String,String> ();
        calls.put ("text1", "org.netbeans.modules.languages.features.CompletionTest.completion1");
        features.add (Feature.create (
            "COMPLETION", 
            Selector.create ("WhileStatement"),
            Collections.<String,String> emptyMap (),
            calls,
            Collections.<String,Pattern> emptyMap ()
        ));
        Language language = createTestLanguage (features);
        
        JEditorPane editor = createTestComponent (language);
        ParserManager pm = ParserManager.get (editor.getDocument ());
        editor.getDocument ().insertString(editor.getDocument ().getLength (), " ", null);
        
        editor.setCaretPosition (27);
        CompletionProviderImpl cc = new CompletionProviderImpl ();
        CompletionTask ct = cc.createTask (0, editor);
        List<CompletionItem> list = cc.query (editor);
        assertEquals (2, list.size ());
        assertEquals ("while", list.get (0).getInsertPrefix ());
        assertEquals ("who", list.get (1).getInsertPrefix ());
//        assertEquals (editor, contextComponent);
        assertEquals (editor.getDocument (), contextDocument);
        assertEquals (26, contextPosition);
        assertEquals (7, contextPath.size ());
    }
    
    public void testASTBasedDirect () throws ParseException, BadLocationException {
        List<Feature> features = new ArrayList<Feature> ();
        Map<String,String> calls = new HashMap<String,String> ();
        calls.put ("text1", "while");
        calls.put ("text2", "who");
        calls.put ("text3", "if");
        features.add (Feature.create (
            "COMPLETION", 
            Selector.create ("WhileStatement"),
            calls,
            Collections.<String,String> emptyMap (),
            Collections.<String,Pattern> emptyMap ()
        ));
        Language language = createTestLanguage (features);
        
        JEditorPane editor = createTestComponent (language);
        ParserManager.get (editor.getDocument ());
        editor.getDocument ().insertString(editor.getDocument ().getLength (), " ", null);
        
        editor.setCaretPosition (27);
        CompletionProviderImpl cc = new CompletionProviderImpl ();
        CompletionTask ct = cc.createTask (0, editor);
        List<CompletionItem> list = cc.query (editor);
        assertEquals (2, list.size ());
        assertEquals ("while", list.get (0).getInsertPrefix ());
        assertEquals ("who", list.get (1).getInsertPrefix ());
    }
    
    public void testTokenAndASTBased () throws ParseException, BadLocationException {
        List<Feature> features = new ArrayList<Feature> ();
        Map<String,String> calls = new HashMap<String,String> ();
        calls.put ("text1", "org.netbeans.modules.languages.features.CompletionTest.completion1");
        features.add (Feature.create (
            "COMPLETION", 
            Selector.create ("keyword"),
            Collections.<String,String> emptyMap (),
            calls,
            Collections.<String,Pattern> emptyMap ()
        ));
        calls.put ("text1", "org.netbeans.modules.languages.features.CompletionTest.completion2");
        features.add (Feature.create (
            "COMPLETION", 
            Selector.create ("WhileStatement"),
            Collections.<String,String> emptyMap (),
            calls,
            Collections.<String,Pattern> emptyMap ()
        ));
        Language language = createTestLanguage (features);
        
        JEditorPane editor = createTestComponent (language);
        ParserManager.get (editor.getDocument ());
        editor.getDocument ().insertString(editor.getDocument ().getLength (), " ", null);
        
        editor.setCaretPosition (27);
        CompletionProviderImpl cc = new CompletionProviderImpl ();
        CompletionTask ct = cc.createTask (0, editor);
        List<CompletionItem> list = cc.query (editor);
        assertEquals (6, list.size ());
        assertEquals ("while", list.get (0).getInsertPrefix ());
        assertEquals ("who", list.get (1).getInsertPrefix ());
        assertEquals ("while", list.get (2).getInsertPrefix ());
        assertEquals ("who", list.get (3).getInsertPrefix ());
        assertEquals ("www", list.get (4).getInsertPrefix ());
        assertEquals ("wma", list.get (5).getInsertPrefix ());
//        assertEquals (editor, contextComponent);
        assertEquals (editor.getDocument (), contextDocument);
        assertEquals (26, contextPosition);
        assertEquals (7, contextPath.size ());
    }
    
    public void testTokenAndASTBasedDirect () throws ParseException, BadLocationException {
        List<Feature> features = new ArrayList<Feature> ();
        Map<String,String> calls = new HashMap<String,String> ();
        calls.put ("text1", "while");
        calls.put ("text2", "who");
        calls.put ("text3", "if");
        features.add (Feature.create (
            "COMPLETION", 
            Selector.create ("keyword"),
            calls,
            Collections.<String,String> emptyMap (),
            Collections.<String,Pattern> emptyMap ()
        ));
        calls.put ("text1", "www");
        calls.put ("text2", "wma");
        calls.put ("text3", "iff");
        features.add (Feature.create (
            "COMPLETION", 
            Selector.create ("WhileStatement"),
            calls,
            Collections.<String,String> emptyMap (),
            Collections.<String,Pattern> emptyMap ()
        ));
        Language language = createTestLanguage (features);
        
        JEditorPane editor = createTestComponent (language);
        ParserManager.get (editor.getDocument ());
        editor.getDocument ().insertString(editor.getDocument ().getLength (), " ", null);
        
        editor.setCaretPosition (27);
        CompletionProviderImpl cc = new CompletionProviderImpl ();
        CompletionTask ct = cc.createTask (0, editor);
        List<CompletionItem> list = cc.query (editor);
        assertEquals (6, list.size ());
        assertEquals ("while", list.get (0).getInsertPrefix ());
        assertEquals ("who", list.get (1).getInsertPrefix ());
        assertEquals ("while", list.get (2).getInsertPrefix ());
        assertEquals ("who", list.get (3).getInsertPrefix ());
        assertEquals ("www", list.get (4).getInsertPrefix ());
        assertEquals ("wma", list.get (5).getInsertPrefix ());
    }
    
    private static Document         contextDocument;
    private static JTextComponent   contextComponent;
    private static int              contextPosition;
    private static ASTPath          contextPath;
    
    public static List completion1 (Context context) {
        contextDocument = context.getDocument ();
//        contextComponent = context.getJTextComponent ();
        contextPosition = context.getOffset ();
        if (context instanceof SyntaxContext) 
            contextPath = ((SyntaxContext) context).getASTPath ();
        else
            contextPath = null;
        
        List result = new ArrayList ();
        result.add (org.netbeans.api.languages.CompletionItem.create ("while"));
        result.add (org.netbeans.api.languages.CompletionItem.create ("who"));
        result.add (org.netbeans.api.languages.CompletionItem.create ("if"));
        return result;
    }
    
    public static List completion2 (SyntaxContext context) {
        contextDocument = context.getDocument ();
//        contextComponent = context.getJTextComponent ();
        contextPosition = context.getOffset ();
        contextPath = ((SyntaxContext) context).getASTPath ();
        
        List result = new ArrayList ();
        result.add (org.netbeans.api.languages.CompletionItem.create ("www"));
        result.add (org.netbeans.api.languages.CompletionItem.create ("wma"));
        result.add (org.netbeans.api.languages.CompletionItem.create ("iff"));
        return result;
    }
    
    private static Language createTestLanguage (List<Feature> features) throws ParseException {
        TestLanguage language = new TestLanguage ();
        language.addToken (0, "keyword", Pattern.create ("'if' | 'while'"), null, null, 0, null);
        language.addToken (1, "identifier", Pattern.create ("['a'-'z' '_']+"), null, null, 1, null);
        language.addToken (2, "operator", Pattern.create ("'(' | ')' | '{' | '}'"), null, null, 2, null);
        language.addToken (3, "whitespace", Pattern.create ("[' ' '\n' '\t' '\r']+"), null, null, 3, null);
        language.addFeature (Feature.create ("SKIP", Selector.create ("whitespace")));
        
        ASTToken IDENTIFIER = ASTToken.create (language, "identifier", null, 0, "identifier".length (), null);
        ASTToken IF = ASTToken.create (language, "keyword", "if", 0, "keyword".length (), null);
        ASTToken WHILE = ASTToken.create (language, "keyword", "while", 0, "keyword".length (), null);
        ASTToken PARENTHESIS = ASTToken.create (language, "operator", "(", 0, "operator".length (), null);
        ASTToken PARENTHESIS2 = ASTToken.create (language, "operator", ")", 0, "operator".length (), null);
        ASTToken BRACE = ASTToken.create (language, "operator", "{", 0, "operator".length (), null);
        ASTToken BRACE2 = ASTToken.create (language, "operator", "}", 0, "operator".length (), null);
        
        language.addRule ("S", Arrays.asList (new Object[] {"Statement", "S"}));
        language.addRule ("S", Arrays.asList (new Object[] {}));
        language.addRule ("Statement", Arrays.asList (new Object[] {"IfStatement"}));
        language.addRule ("Statement", Arrays.asList (new Object[] {"WhileStatement"}));
        language.addRule ("Statement", Arrays.asList (new Object[] {"Block"}));
        language.addRule ("IfStatement", Arrays.asList (new Object[] {IF, PARENTHESIS, "ConditionalExpression", PARENTHESIS2, "Block"}));
        language.addRule ("WhileStatement", Arrays.asList (new Object[] {WHILE, PARENTHESIS, "ConditionalExpression", PARENTHESIS2, "Block"}));
        language.addRule ("ConditionalExpression", Arrays.asList (new Object[] {IDENTIFIER}));
        language.addRule ("Block", Arrays.asList (new Object[] {BRACE, "Block1", BRACE2}));
        language.addRule ("Block1", Arrays.asList (new Object[] {IDENTIFIER, "Block1"}));
        language.addRule ("Block1", Arrays.asList (new Object[] {"Statement", "Block1"}));
        language.addRule ("Block1", Arrays.asList (new Object[] {}));
        
        LanguagesManager.getDefault ().addLanguage (language);
        return language;
    }
    
    private static JEditorPane createTestComponent (Language language) {
        
        String text = 
            "if (jedna) {\n" +
            "    dvje\n" +
            "    while (tri) {\n" +
            "        pet\n" +
            "        sest\n" +
            "    }\n" +
            "    if (sedum) {osum}\n" +
            "}";
        
        JEditorPane editor = new JEditorPane ();
        editor.getDocument ().putProperty ("mimeType", language.getMimeType ());
        editor.getDocument ().putProperty (org.netbeans.api.lexer.Language.class, new SLanguageHierarchy (language).language ());
        editor.setText (text);
        return editor;
    }
}




