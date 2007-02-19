/*
 * LLAnalyserTest.java
 * JUnit based test
 *
 * Created on March 26, 2006, 9:57 AM
 */

package org.netbeans.modules.languages.parser;

import junit.framework.TestCase;
import org.netbeans.api.languages.CharInput;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.SToken;
import org.netbeans.modules.languages.parser.TokenInput;
import org.netbeans.api.languages.ASTNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.NBSLanguageReader;


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
        Language l = new Language (mimeType);
        l.addRule (LLSyntaxAnalyser.Rule.create (mimeType, "S", Arrays.asList (new Object[] {
            SToken.create (mimeType, "identifier", null), 
            "S"
        })));
        l.addRule (LLSyntaxAnalyser.Rule.create (mimeType, "S", Arrays.asList (new Object[] {
            SToken.create (mimeType, "operator", "{"), 
            "S", 
            SToken.create (mimeType, "operator", "}"), 
            "S"
        })));
        l.addRule (LLSyntaxAnalyser.Rule.create (mimeType, "S", Arrays.asList (new Object[] {
        })));
        LLSyntaxAnalyser a = l.getAnalyser ();
        //PetraTest.print (Petra.first (r, 5));
        TokenInput input = TokenInput.create (new SToken[] {
            SToken.create (mimeType, "identifier", "asd"),
            SToken.create (mimeType, "identifier", "ss"),
            SToken.create (mimeType, "operator", "{"),
            SToken.create (mimeType, "identifier", "a"),
            SToken.create (mimeType, "operator", "{"),
            SToken.create (mimeType, "operator", "}"),
            SToken.create (mimeType, "identifier", "asd"),
            SToken.create (mimeType, "operator", "}"),
        });
        assertNotNull (a.read (input, false));
        assert (input.eof ());
    }

    public void test2 () throws ParseException {
        Language l = NBSLanguageReader.readLanguage (
            "test", 
            "TOKEN:operator:( '{' | '}' | '.' | ';' | ',' | '(' | ')' )" +
            "TOKEN:whitespace:( ['\\n' '\\r' ' ' '\\t']+ )" +
            "TOKEN:keyword:( 'package' | 'class' | 'import' | 'static' | 'synchronized' | 'final' | 'abstract' | 'native' | 'import' | 'extends' | 'implements' | 'public' | 'protected' | 'private' )" +
            "TOKEN:identifier:( ['a'-'z' 'A'-'Z' '0'-'9' '_' '-' '$' '%']+ )" +
            "SKIP:whitespace " +
            "S = packageDeclaration imports classOrInterface otherClasses;" +
            "packageDeclaration = ;" +
            "packageDeclaration = <keyword,'package'> dottedName <operator,';'>;" +
            "dottedName = <identifier> dottedName1;" +
            "dottedName1 = <operator,'.'> <identifier> dottedName1;" +
            "dottedName1 = ;" +
            "imports = <keyword,'import'> dottedName <operator,';'> imports;" +
            "imports = ;" +
            "modifiers = <keyword,'public'> modifiers;" +
            "modifiers = <keyword,'private'> modifiers;" +
            "modifiers = <keyword,'protected'> modifiers;" +
            "modifiers = <keyword,'static'> modifiers;" +
            "modifiers = <keyword,'synchronized'> modifiers;" +
            "modifiers = <keyword,'final'> modifiers;" +
            "modifiers = <keyword,'abstract'> modifiers;" +
            "modifiers = <keyword,'native'> modifiers;" +
            "modifiers = ;" +
            "classOrInterface = modifiers classOrInterface1;" +
            "classOrInterface1 = <keyword,'class'> class;" +
            "classOrInterface1 = <keyword,'interface'> interface;" +
            "class = <identifier> extendsList implementsList classOrInterfaceBody;" +
            "interface = <identifier> extendsList implementsList classOrInterfaceBody;" +
            "extendsList = <keyword,'extends'> dottedName extendsList1;" +
            "extendsList1 = <operator,','> dottedName;" +
            "extendsList1 = ;" +
            "implementsList = <keyword,'implements'> dottedName implementsList1;" +
            "implementsList1 = <operator,','> dottedName;" +
            "implementsList1 = ;" +
            "classOrInterfaceBody = <operator,'{'> members <operator,'}'>;" +
            "members = modifiers members1;" +
            "members1 = block members;" +
            "members1 = <identifier, type> <identifier, name> <operator,'('> parametersList <operator,')'> block members;" +
            "members = ;" +
            "parametersList =;" +
            "block = <operator,'{'> body <operator,'}'>;" +
            "body = <identifier> body;" +
            "body = block;" +
            "body = ;" +
            "otherClasses = ;",
            mimeType
        );
        CharInput input = new StringInput (
            "package org.test.foo;" +
            "import a.bb.ccc;" +
            "import qq.ww.ee;" +
            "public static class Hanz extends aaa.Text implements a.XXX, b.YYY {" +
            "  public static { aaa}" +
            "  public final int test () {" +
            "    test test" +
            "  }" +
            "}",
            "source"
        );
        ASTNode n = l.getAnalyser ().read (
            TokenInput.create (
                l.getParser (),
                input,
                l.getSkipTokenTypes ()
            ),
            false
        );
        //System.out.println(n.print ());
        assertNotNull (n);
    }

    public void test4 () throws ParseException {
        Language l = NBSLanguageReader.readLanguage (
            "test", 
            "TOKEN:operator:( '{' | '}' | '.' | ',' | '(' | ')' )" +
            "TOKEN:separator:( ';' )" +
            "TOKEN:whitespace:( ['\\n' '\\r' ' ' '\\t']+ )" +
            "TOKEN:keyword:( 'void' | 'public' )" +
            "TOKEN:identifier:( ['a'-'z' 'A'-'Z' '0'-'9' '_' '-' '$' '%']+ )" +
            "SKIP:whitespace " +
            "S = variable S;" +
            "S = ;" +
            "variable = modifiers <keyword> <identifier,name> <separator,';'>;" +
            "variable = modifiers <identifier,type> <identifier,name> <separator,';'>;" +
            "modifiers = <keyword,'public'> modifiers;" +
            "modifiers = ;",
            mimeType
        );
        CharInput input = new StringInput (
            "void a;" +
            "public ii name;",
            "source"
        );
        ASTNode n = l.getAnalyser ().read (
            TokenInput.create (
                l.getParser (),
                input,
                l.getSkipTokenTypes ()
            ),
            false
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
        Language l = NBSLanguageReader.readLanguage (
            "test", 
            "TOKEN:operator:( '{' | '}' | '.' | ';' | ',' | '(' | ')' )" +
            "TOKEN:whitespace:( ['\\n' '\\r' ' ' '\\t']+ )" +
            "TOKEN:keyword:( 'package' | 'class' | 'import' | 'static' | 'synchronized' | 'final' | 'abstract' | 'native' | 'import' | 'extends' | 'implements' | 'public' | 'protected' | 'private' )" +
            "TOKEN:identifier:( ['a'-'z' 'A'-'Z' '0'-'9' '_' '-' '$' '%']+ )" +
            "SKIP:whitespace " +
            "S = <identifier,'a'> SS <identifier,'b'>;" +
            "SS = <identifier,'if'> E <identifier,'then'> SS;" +
            "SS = <identifier,'if'> E <identifier,'then'> SS <identifier,'else'> SS;" +
            "SS = <identifier, 'b'>;" +
            "E = <identifier,'e'>;",
            mimeType
        );
        CharInput input = new StringInput (
            "a if e then if e then b else b b",
            "source"
        );
        ASTNode n = l.getAnalyser ().read (
            TokenInput.create (
                l.getParser (),
                input,
                l.getSkipTokenTypes ()
            ),
            false
        );
        assertTrue (input.eof ());
        assertEquals (3, n.getChildren ().size ());
        n = (ASTNode) n.getChildren ().get (1);
        assertEquals (4, n.getChildren ().size ());
        n = (ASTNode) n.getChildren ().get (3);
        assertEquals (6, n.getChildren ().size ());
    }
    
    public void test5 () throws ParseException {
        Language l = NBSLanguageReader.readLanguage (
            "test", 
            "TOKEN:operator:( '{' | '}' | '.' | ';' | ',' | '(' | ')' )" +
            "TOKEN:whitespace:( ['\\n' '\\r' ' ' '\\t']+ )" +
            "TOKEN:keyword:( 'package' | 'class' | 'import' | 'static' | 'synchronized' | 'final' | 'abstract' | 'native' | 'import' | 'extends' | 'implements' | 'public' | 'protected' | 'private' )" +
            "TOKEN:identifier:( ['a'-'z' 'A'-'Z' '0'-'9' '_' '-' '$' '%']+ )" +
            "SKIP:whitespace " +
            "S = <identifier,'a'> SS <identifier,'b'>;" +
            "SS = <identifier,'if'> E <identifier,'then'> SS;" +
            "SS = <identifier,'if'> E <identifier,'then'> SS <identifier,'else'> SS;" +
            "SS = <identifier, 'b'>;" +
            "E = <identifier,'e'>;",
            mimeType
        );
        CharInput input = new StringInput (
            "a if e then if e then b else b b",
            "source"
        );
        ASTNode n = l.getAnalyser ().read (
            TokenInput.create (
                l.getParser (),
                input,
                l.getSkipTokenTypes ()
            ),
            false
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
            if (next instanceof SToken) continue;
            print ((List) it.next (), indent + "  ");
        }
    }
}
