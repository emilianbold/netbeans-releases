/*
 * LLAnalyserTest.java
 * JUnit based test
 *
 * Created on March 26, 2006, 9:57 AM
 */

package org.netbeans.modules.languages.parser;

import java.util.ArrayList;
import java.util.Map;
import junit.framework.TestCase;

import org.netbeans.api.languages.CharInput;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.TokenInput;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.NBSLanguageReader;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.languages.Rule;
import org.netbeans.modules.languages.TestUtils;
import org.netbeans.modules.languages.TokenType;
import org.netbeans.modules.languages.parser.TokenInputUtils;


/**
 *
 * @author Jan Jancura
 */
public class AnalyserTest extends TestCase {
    
    public AnalyserTest (String testName) {
        super (testName);
    }

    private static String mimeType = "text/test";

    public void test1 () throws ParseException {
        Map<Integer,String> tokensMap = new HashMap<Integer,String> ();
        tokensMap.put (0, "identifier");
        tokensMap.put (1, "operator");
        Language language = Language.create ("test/test", tokensMap, Collections.<Feature>emptyList (), null);
        List<Rule> rules = new ArrayList<Rule> ();
        rules.add (Rule.create ("S", Arrays.asList (new Object[] {
            ASTToken.create (language, "identifier", null, 0, "identifier".length (), null), 
            "S"
        })));
        rules.add (Rule.create ("S", Arrays.asList (new Object[] {
            ASTToken.create (language, "operator", "{", 0, "operator".length (), null), 
            "S", 
            ASTToken.create (language, "operator", "}", 0, "operator".length (), null), 
            "S"
        })));
        rules.add (Rule.create ("S", Arrays.asList (new Object[] {
        })));
        LLSyntaxAnalyser a = LLSyntaxAnalyser.create (language, rules, Collections.<Integer>emptySet ());
        
        //PetraTest.print (Petra.first (r, 5));
        TokenInput input = TokenInputUtils.create (new ASTToken[] {
            ASTToken.create (language, "identifier", "asd", 0, "asd".length (), null),
            ASTToken.create (language, "identifier", "ss", 0, "ss".length (), null),
            ASTToken.create (language, "operator", "{", 0, "{".length (), null),
            ASTToken.create (language, "identifier", "a", 0, "a".length (), null),
            ASTToken.create (language, "operator", "{", 0, "{".length (), null),
            ASTToken.create (language, "operator", "}", 0, "}".length (), null),
            ASTToken.create (language, "identifier", "asd", 0, "asd".length (), null),
            ASTToken.create (language, "operator", "}", 0, "}".length (), null),
        });
        assertNotNull (a.read (input, false, new boolean [] {false}));
        assert (input.eof ());
    }

//    public void test2 () throws ParseException {
//        Language l = NBSLanguageReader.readLanguage (
//            "test", 
//            "TOKEN:operator:( '{' | '}' | '.' | ';' | ',' | '(' | ')' )" +
//            "TOKEN:whitespace:( ['\\n' '\\r' ' ' '\\t']+ )" +
//            "TOKEN:keyword:( 'package' | 'class' | 'import' | 'static' | 'synchronized' | 'final' | 'abstract' | 'native' | 'import' | 'extends' | 'implements' | 'public' | 'protected' | 'private' )" +
//            "TOKEN:identifier:( ['a'-'z' 'A'-'Z' '0'-'9' '_' '-' '$' '%']+ )" +
//            "SKIP:whitespace " +
//            "S = packageDeclaration imports classOrInterface otherClasses;" +
//            "packageDeclaration = ;" +
//            "packageDeclaration = <keyword,'package'> dottedName <operator,';'>;" +
//            "dottedName = <identifier> dottedName1;" +
//            "dottedName1 = <operator,'.'> <identifier> dottedName1;" +
//            "dottedName1 = ;" +
//            "imports = <keyword,'import'> dottedName <operator,';'> imports;" +
//            "imports = ;" +
//            "modifiers = <keyword,'public'> modifiers;" +
//            "modifiers = <keyword,'private'> modifiers;" +
//            "modifiers = <keyword,'protected'> modifiers;" +
//            "modifiers = <keyword,'static'> modifiers;" +
//            "modifiers = <keyword,'synchronized'> modifiers;" +
//            "modifiers = <keyword,'final'> modifiers;" +
//            "modifiers = <keyword,'abstract'> modifiers;" +
//            "modifiers = <keyword,'native'> modifiers;" +
//            "modifiers = ;" +
//            "classOrInterface = modifiers classOrInterface1;" +
//            "classOrInterface1 = <keyword,'class'> class;" +
//            "classOrInterface1 = <keyword,'interface'> interface;" +
//            "class = <identifier> extendsList implementsList classOrInterfaceBody;" +
//            "interface = <identifier> extendsList implementsList classOrInterfaceBody;" +
//            "extendsList = <keyword,'extends'> dottedName extendsList1;" +
//            "extendsList1 = <operator,','> dottedName;" +
//            "extendsList1 = ;" +
//            "implementsList = <keyword,'implements'> dottedName implementsList1;" +
//            "implementsList1 = <operator,','> dottedName;" +
//            "implementsList1 = ;" +
//            "classOrInterfaceBody = <operator,'{'> members <operator,'}'>;" +
//            "members = modifiers members1;" +
//            "members1 = block members;" +
//            "members1 = <identifier> <identifier> <operator,'('> parametersList <operator,')'> block members;" +
//            "members = ;" +
//            "parametersList =;" +
//            "block = <operator,'{'> body <operator,'}'>;" +
//            "body = <identifier> body;" +
//            "body = block;" +
//            "body = ;" +
//            "otherClasses = ;",
//            mimeType
//        );
//        CharInput input = new StringInput (
//            "package org.test.foo;" +
//            "import a.bb.ccc;" +
//            "import qq.ww.ee;" +
//            "public static class Hanz extends aaa.Text implements a.XXX, b.YYY {" +
//            "  public static { aaa}" +
//            "  public final int test () {" +
//            "    test test" +
//            "  }" +
//            "}",
//            "source"
//        );
//        ASTNode n = l.getAnalyser ().read (
//            TokenInput.create (
//                "text/test",
//                l.getParser (),
//                input,
//                l.getSkipTokenTypes ()
//            ),
//            false
//        );
//        //System.out.println(n.print ());
//        assertNotNull (n);
//    }

    public void test4 () throws ParseException {
        Language language = TestUtils.createLanguage (
            "TOKEN:operator:( '{' | '}' | '.' | ',' | '(' | ')' )" +
            "TOKEN:separator:( ';' )" +
            "TOKEN:whitespace:( ['\\n' '\\r' ' ' '\\t']+ )" +
            "TOKEN:keyword:( 'void' | 'public' )" +
            "TOKEN:identifier:( ['a'-'z' 'A'-'Z' '0'-'9' '_' '-' '$' '%']+ )" +
            "SKIP:whitespace " +
            "S = variable S;" +
            "S = ;" +
            "variable = modifiers <keyword> <identifier> <separator,';'>;" +
            "variable = modifiers <identifier> <identifier> <separator,';'>;" +
            "modifiers = <keyword,'public'> modifiers;" +
            "modifiers = ;"
        );
        CharInput input = new StringInput (
            "void a;" +
            "public ii name;"
        );
        ASTNode n = language.getAnalyser ().read (
            TokenInputUtils.create (
                language,
                language.getParser (),
                input
            ),
            false,
            new boolean[] {false}
        );
        assertNotNull (n);
        assertTrue (input.eof ());
    }
//
//    public void testFollow1 () throws ParseException {
//        Language l = LanguageReader.readLanguage (
//            "test", 
//            "S = <identifier,'a'> B C <identifier,'b'>;" +
//            "B = <identifier,'b'>;" +
//            "B =;" +
//            "C = <identifier,'c'>;" +
//            "C =;",
//            mimeType
//        );
//        Analyser a = l.getAnalyser ();
//        Parser p = Parser.create ();
//        p.add ("operator", "'{' | '}' | '.' | ';' | ',' | '(' | ')'");
//        p.add ("whitespace", "['\\n' '\\r' ' ' '\\t']+");
//        p.add ("keyword", "'package' | 'class' | 'import' | 'static' | 'synchronized' | 'final' | 'abstract' | 'native' | 'import' | 'extends' | 'implements' | 'public' | 'protected' | 'private'");
//        p.add ("identifier", "['a'-'z' 'A'-'Z' '0'-'9' '_' '-' '$' '%']+");
//        p.addSkipToken (Token.create ("whitespace", null, false));
//        Input input = Input.create (
//            "b d"
//        );
//        Input input1 = p.readAll (input);
//        List ast = new ArrayList ();
//        try {
//            a.read (input1);
//            assertTrue (true);
//        } catch (ParseException ex) {
//            System.out.println(ex);
//        }
//        print (ast, "");
//    }
//
//    public void testFollow2 () throws ParseException {
//        Language l = LanguageReader.readLanguage (
//            "test", 
//            "S = <identifier,'a'> B C <identifier,'c'>;" +
//            "B = <identifier,'b'>;" +
//            "B =;" +
//            "C = <identifier,'c'>;" +
//            "C =;",
//            mimeType
//        );
//        Analyser a = l.getAnalyser ();
//        Parser p = Parser.create ();
//        p.add ("operator", "'{' | '}' | '.' | ';' | ',' | '(' | ')'");
//        p.add ("whitespace", "['\\n' '\\r' ' ' '\\t']+");
//        p.add ("keyword", "'package' | 'class' | 'import' | 'static' | 'synchronized' | 'final' | 'abstract' | 'native' | 'import' | 'extends' | 'implements' | 'public' | 'protected' | 'private'");
//        p.add ("identifier", "['a'-'z' 'A'-'Z' '0'-'9' '_' '-' '$' '%']+");
//        p.addSkipToken (Token.create ("whitespace", null, false));
//        Input input = Input.create (
//            "b d"
//        );
//        Input input1 = p.readAll (input);
//        List ast = new ArrayList ();
//        try {
//            a.read (input1);
//            assertTrue (true);
//        } catch (ParseException ex) {
//            System.out.println(ex);
//        }
//        print (ast, "");
//    }
//
//    public void testFollow3 () throws ParseException {
//        Language l = LanguageReader.readLanguage (
//            "test", 
//            "S = <identifier,'a'> B C B1 <identifier,'c'>;" +
//            "B = <identifier,'b'>;" +
//            "B =;" +
//            "C = <identifier,'c'>;" +
//            "C =;" +
//            "B1 = <identifier,'b'>;",
//            mimeType
//        );
//        Analyser a = l.getAnalyser ();
//        Parser p = Parser.create ();
//        p.add ("operator", "'{' | '}' | '.' | ';' | ',' | '(' | ')'");
//        p.add ("whitespace", "['\\n' '\\r' ' ' '\\t']+");
//        p.add ("keyword", "'package' | 'class' | 'import' | 'static' | 'synchronized' | 'final' | 'abstract' | 'native' | 'import' | 'extends' | 'implements' | 'public' | 'protected' | 'private'");
//        p.add ("identifier", "['a'-'z' 'A'-'Z' '0'-'9' '_' '-' '$' '%']+");
//        p.addSkipToken (Token.create ("whitespace", null, false));
//        Input input = Input.create (
//            "b d"
//        );
//        Input input1 = p.readAll (input);
//        List ast = new ArrayList ();
//        try {
//            a.read (input1);
//            assertTrue (true);
//        } catch (ParseException ex) {
//            System.out.println(ex);
//        }
//        print (ast, "");
//    }
    
    
    public void test3 () throws ParseException {
        Language l = TestUtils.createLanguage (
            "TOKEN:operator:( '{' | '}' | '.' | ';' | ',' | '(' | ')' )" +
            "TOKEN:whitespace:( ['\\n' '\\r' ' ' '\\t']+ )" +
            "TOKEN:keyword:( 'package' | 'class' | 'import' | 'static' | 'synchronized' | 'final' | 'abstract' | 'native' | 'import' | 'extends' | 'implements' | 'public' | 'protected' | 'private' )" +
            "TOKEN:identifier:( ['a'-'z' 'A'-'Z' '0'-'9' '_' '-' '$' '%']+ )" +
            "SKIP:whitespace " +
            "S = <identifier,'a'> SS <identifier,'b'>;" +
            "SS = <identifier,'if'> E <identifier,'then'> SS;" +
            "SS = <identifier,'if'> E <identifier,'then'> SS <identifier,'else'> SS;" +
            "SS = <identifier, 'b'>;" +
            "E = <identifier,'e'>;"
        );
        CharInput input = new StringInput (
            "a if e then if e then b else b b"
        );
        ASTNode n = l.getAnalyser ().read (
            TokenInputUtils.create (
                l,
                l.getParser (),
                input
            ),
            false,
            new boolean[] {false}
        );
        assertTrue (input.eof ());
        assertEquals (3, n.getChildren ().size ());
        n = (ASTNode) n.getChildren ().get (1);
        assertEquals (4, n.getChildren ().size ());
        n = (ASTNode) n.getChildren ().get (3);
        assertEquals (6, n.getChildren ().size ());
    }
    
    public void test5 () throws ParseException {
        Language language = TestUtils.createLanguage (
            "TOKEN:TAG:( '<' ['a'-'z']+ )" +
            "TOKEN:SYMBOL:( '>' | '=')" +
            "TOKEN:ENDTAG:( '</' ['a'-'z']+ )" +
            "TOKEN:ATTRIBUTE:( ['a'-'z']+ )" +
            "TOKEN:ATTR_VALUE:( '\\\"' [^ '\\n' '\\r' '\\\"']+ '\\\"' )" +
            "TOKEN:VALUE:( '\\\"' [^ '\\n' '\\r' '\\\"']+ '\\\"' )" +
            "TOKEN:TEXT:( [^'<']+ )" +
            "S = tags;" +
            "tags = (startTag | endTag | etext)*;" + 
            "startTag = <TAG> (attribute)* ( <SYMBOL, '>'> | <SYMBOL, '/>'> );" + 
            "endTag = <ENDTAG> <SYMBOL, '>'>;" + 
            "attribute = <ATTRIBUTE>;" + 
            "attribute = <ATTR_VALUE>;" + 
            "attribute = <ATTRIBUTE> <SYMBOL,'='> <VALUE>;" + 
            "etext = (<TEXT>)*;"
        );
        CharInput input = new StringInput (
            "<a></a>"
        );
        ASTNode n = language.getAnalyser ().read (
            TokenInputUtils.create (
                language,
                language.getParser (),
                input
            ),
            false,
            new boolean[] {false}
        );
        System.out.println(n.print ());
        assertTrue (input.eof ());
        assertEquals (1, n.getChildren ().size ());
        assertEquals ("S", n.getNT ());
        n = (ASTNode) n.getChildren ().get (0);
        assertEquals (2, n.getChildren ().size ());
        assertEquals ("tags", n.getNT ());
        assertEquals ("startTag", ((ASTNode) n.getChildren ().get (0)).getNT ());
        assertEquals ("endTag", ((ASTNode) n.getChildren ().get (1)).getNT ());
        n = (ASTNode) n.getChildren ().get (0);
        assertEquals (2, n.getChildren ().size ());
        assertEquals ("TAG", ((ASTToken) n.getChildren ().get (0)).getTypeName ());
        assertEquals ("SYMBOL", ((ASTToken) n.getChildren ().get (1)).getTypeName ());
    }
    
    public void test6 () throws ParseException {
        Language language = TestUtils.createLanguage (
            "TOKEN:operator:( '{' | '}' | '.' | ';' | ',' | '(' | ')' )" +
            "TOKEN:whitespace:( ['\\n' '\\r' ' ' '\\t']+ )" +
            "TOKEN:keyword:( 'package' | 'class' | 'import' | 'static' | 'synchronized' | 'final' | 'abstract' | 'native' | 'import' | 'extends' | 'implements' | 'public' | 'protected' | 'private' )" +
            "TOKEN:identifier:( ['a'-'z' 'A'-'Z' '0'-'9' '_' '-' '$' '%']+ )" +
            "SKIP:whitespace " +
            "S = <identifier,'a'> SS <identifier,'b'>;" +
            "SS = <identifier,'if'> E <identifier,'then'> SS;" +
            "SS = <identifier,'if'> E <identifier,'then'> SS <identifier,'else'> SS;" +
            "SS = <identifier, 'b'>;" +
            "E = <identifier,'e'>;"
        );
        CharInput input = new StringInput (
            "a if e then if e then b else b b"
        );
        ASTNode n = language.getAnalyser ().read (
            TokenInputUtils.create (
                language,
                language.getParser (),
                input
            ),
            false,
            new boolean[] {false}
        );
        System.out.println(n.print ());
        assertTrue (input.eof ());
        assertEquals (3, n.getChildren ().size ());
        n = (ASTNode) n.getChildren ().get (1);
        assertEquals (4, n.getChildren ().size ());
        n = (ASTNode) n.getChildren ().get (3);
        assertEquals (6, n.getChildren ().size ());
    }
    
    private void print (List l, String indent) {
        Iterator it = l.iterator ();
        while (it.hasNext ()) {
            Object next = it.next ();
            System.out.println (indent + next);
            if (next instanceof ASTToken) continue;
            print ((List) it.next (), indent + "  ");
        }
    }
}
