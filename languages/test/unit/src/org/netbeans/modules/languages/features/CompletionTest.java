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
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.Context;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.support.CompletionSupport;
import org.netbeans.modules.languages.*;
import junit.framework.TestCase;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Selector;
import org.netbeans.modules.languages.lexer.SLanguageHierarchy;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser.Rule;
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
        Language l = createTestLanguage ();
        
        Map<String,String> calls = new HashMap<String,String> ();
        calls.put ("text1", "org.netbeans.modules.languages.features.CompletionTest.completion1");
        l.addFeature (Feature.create (
            "COMPLETION", 
            Selector.create ("keyword"),
            Collections.<String,String> emptyMap (),
            calls,
            Collections.<String,Pattern> emptyMap ()
        ));
        JEditorPane editor = createTestComponent (l.getMimeType ());
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
        Language l = createTestLanguage ();
        
        Map<String,String> calls = new HashMap<String,String> ();
        calls.put ("text1", "while");
        calls.put ("text2", "who");
        calls.put ("text3", "if");
        l.addFeature (Feature.create (
            "COMPLETION", 
            Selector.create ("keyword"),
            calls,
            Collections.<String,String> emptyMap (),
            Collections.<String,Pattern> emptyMap ()
        ));
        JEditorPane editor = createTestComponent (l.getMimeType ());
        editor.setCaretPosition (27);
        CompletionProviderImpl cc = new CompletionProviderImpl ();
        CompletionTask ct = cc.createTask (0, editor);
        List<CompletionItem> list = cc.query (editor);
        assertEquals (2, list.size ());
        assertEquals ("while", list.get (0).getInsertPrefix ());
        assertEquals ("who", list.get (1).getInsertPrefix ());
    }
    
    public void testASTBased () throws ParseException, BadLocationException {
        Language l = createTestLanguage ();
        
        Map<String,String> calls = new HashMap<String,String> ();
        calls.put ("text1", "org.netbeans.modules.languages.features.CompletionTest.completion1");
        l.addFeature (Feature.create (
            "COMPLETION", 
            Selector.create ("WhileStatement"),
            Collections.<String,String> emptyMap (),
            calls,
            Collections.<String,Pattern> emptyMap ()
        ));
        
        JEditorPane editor = createTestComponent (l.getMimeType ());
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
        Language l = createTestLanguage ();
        
        Map<String,String> calls = new HashMap<String,String> ();
        calls.put ("text1", "while");
        calls.put ("text2", "who");
        calls.put ("text3", "if");
        l.addFeature (Feature.create (
            "COMPLETION", 
            Selector.create ("WhileStatement"),
            calls,
            Collections.<String,String> emptyMap (),
            Collections.<String,Pattern> emptyMap ()
        ));
        
        JEditorPane editor = createTestComponent (l.getMimeType ());
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
        Language l = createTestLanguage ();
        
        Map<String,String> calls = new HashMap<String,String> ();
        calls.put ("text1", "org.netbeans.modules.languages.features.CompletionTest.completion1");
        l.addFeature (Feature.create (
            "COMPLETION", 
            Selector.create ("keyword"),
            Collections.<String,String> emptyMap (),
            calls,
            Collections.<String,Pattern> emptyMap ()
        ));
        calls.put ("text1", "org.netbeans.modules.languages.features.CompletionTest.completion2");
        l.addFeature (Feature.create (
            "COMPLETION", 
            Selector.create ("WhileStatement"),
            Collections.<String,String> emptyMap (),
            calls,
            Collections.<String,Pattern> emptyMap ()
        ));
        
        JEditorPane editor = createTestComponent (l.getMimeType ());
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
        Language l = createTestLanguage ();
        
        Map<String,String> calls = new HashMap<String,String> ();
        calls.put ("text1", "while");
        calls.put ("text2", "who");
        calls.put ("text3", "if");
        l.addFeature (Feature.create (
            "COMPLETION", 
            Selector.create ("keyword"),
            calls,
            Collections.<String,String> emptyMap (),
            Collections.<String,Pattern> emptyMap ()
        ));
        calls.put ("text1", "www");
        calls.put ("text2", "wma");
        calls.put ("text3", "iff");
        l.addFeature (Feature.create (
            "COMPLETION", 
            Selector.create ("WhileStatement"),
            calls,
            Collections.<String,String> emptyMap (),
            Collections.<String,Pattern> emptyMap ()
        ));
        
        JEditorPane editor = createTestComponent (l.getMimeType ());
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
        contextPosition = context.getTokenSequence ().offset ();
        if (context instanceof SyntaxContext) 
            contextPath = ((SyntaxContext) context).getASTPath ();
        else
            contextPath = null;
        
        List result = new ArrayList ();
        result.add (CompletionSupport.createCompletionItem ("while"));
        result.add (CompletionSupport.createCompletionItem ("who"));
        result.add (CompletionSupport.createCompletionItem ("if"));
        return result;
    }
    
    public static List completion2 (SyntaxContext context) {
        contextDocument = context.getDocument ();
//        contextComponent = context.getJTextComponent ();
        contextPosition = context.getTokenSequence ().offset ();
        contextPath = ((SyntaxContext) context).getASTPath ();
        
        List result = new ArrayList ();
        result.add (CompletionSupport.createCompletionItem ("www"));
        result.add (CompletionSupport.createCompletionItem ("wma"));
        result.add (CompletionSupport.createCompletionItem ("iff"));
        return result;
    }
    
    private static Language createTestLanguage () throws ParseException {
        Language l = new Language ("text/mt");
        l.addToken (null, "keyword", Pattern.create ("'if' | 'while'"), null, null);
        l.addToken (null, "identifier", Pattern.create ("['a'-'z']+"), null, null);
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
    
        l.addRule (Rule.create ("S", Arrays.asList (new Object[] {"Statement", "S"})));
        l.addRule (Rule.create ("S", Arrays.asList (new Object[] {})));
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
        
        LanguagesManager.getDefault ().addLanguage (l);
        return l;
    }
    
    private static JEditorPane createTestComponent (String mimeType) {
        
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
        editor.getDocument ().putProperty ("mimeType", mimeType);
        editor.getDocument ().putProperty (org.netbeans.api.lexer.Language.class, new SLanguageHierarchy (mimeType).language ());
        editor.setText (text);
        return editor;
    }
}




