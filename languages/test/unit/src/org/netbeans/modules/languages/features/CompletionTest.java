/*
 * LanguageTest.java
 * JUnit based test
 *
 * Created on March 19, 2007, 9:26 AM
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
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.Context;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.support.CompletionSupport;
import org.netbeans.modules.languages.*;
import junit.framework.TestCase;
import org.netbeans.api.languages.ParserManagerListener;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Selector;
import org.netbeans.modules.languages.lexer.SLanguageHierarchy;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser.Rule;
import org.netbeans.modules.languages.parser.Pattern;
import org.netbeans.modules.languages.parser.TokenInput;
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
    
    public void testAST1 () throws ParseException {
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
        
        Map<String,String> calls = new HashMap<String,String> ();
        calls.put ("text1", "org.netbeans.modules.languages.features.CompletionTest.completion1");
        l.addFeature (Feature.create (
            "COMPLETION", 
            Selector.create ("keyword"),
            Collections.<String,String> emptyMap (),
            calls,
            Collections.<String,Pattern> emptyMap ()
        ));
        
        LanguagesManager.getDefault ().addLanguage (l);
        
        String text = 
            "if (jedna) {" +
            "    dvje" +
            "    while (tri) {" +
            "        pet" +
            "        sest" +
            "    }" +
            "    if (sedum) {osum}" +
            "}";
        
        JEditorPane editor = new JEditorPane ();
        editor.getDocument ().putProperty ("mimeType", l.getMimeType ());
        editor.getDocument ().putProperty (org.netbeans.api.lexer.Language.class, new SLanguageHierarchy (l.getMimeType ()).language ());
        editor.setText (text);
        editor.setCaretPosition (25);
        CompletionProviderImpl cc = new CompletionProviderImpl ();
        CompletionTask ct = cc.createTask (0, editor);
        List<CompletionItem> list = cc.query (editor);
        assertEquals (2, list.size ());
        assertEquals ("while", list.get (0).getInsertPrefix ());
        assertEquals ("who", list.get (1).getInsertPrefix ());
        
//        LLSyntaxAnalyser a = l.getAnalyser ();
//        TokenInput input = ParserManagerImpl.createTokenInput (editor.getDocument ());
//        ASTNode n = a.read (input, false);
//        System.out.println(n.findPath (25));
    }
    
    private static Context context;
    
    public static List completion1 (Context context) {
        List result = new ArrayList ();
        result.add (CompletionSupport.createCompletionItem ("while"));
        result.add (CompletionSupport.createCompletionItem ("who"));
        result.add (CompletionSupport.createCompletionItem ("if"));
        return result;
    }
}




