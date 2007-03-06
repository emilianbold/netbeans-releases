/*
 * ParserTest.java
 * JUnit based test
 *
 * Created on January 30, 2006, 9:32 PM
 */

package org.netbeans.modules.languages.parser;

import java.util.Map;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.CharInput;
import org.netbeans.api.languages.ParseException;
import java.util.ArrayList;
import java.util.List;
import junit.framework.*;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.parser.Parser.Cookie;

/**
 *
 * @author Jan Jancura
 */
public class ParserTest extends TestCase {
    
    public ParserTest (String testName) {
        super (testName);
    }
    
//    public void testRead123 () throws ParseException {
//        Parser abc = Parser.create ("abc", "abc");
//        Parser axd = Parser.create ("a.d", "axd");
//        Parser p = abc.merge (axd);
//        Input in = Input.create (
//            "abd"
//        );
//        assertEquals ("state: " + p.getState () + " text: " + in, "axd", p.read (p.getState (), in));
//        assertEquals (0, in.current ());
//        assertEquals (true, in.eof ());
//    }
    
//    public void testRead () throws ParseException {
//        Parser abc = Parser.create ("(abc)*|(ab)*", "abc");
//        Parser abcd = Parser.create ("abcd", "abcd");
//        Parser whitespace = Parser.create ("[ \n\r\t]+", "whitespace");
//        Parser p = abc.merge (abcd).append (whitespace).star ();
//        Input in = Input.create (
//            "abab  abcd abcabcd abcd"
//        );
//        assertEquals ("state: " + p.getState () + " text: " + in, "abc", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "whitespace", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "abcd", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "whitespace", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "abc", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, null, p.read (p.getState (), in));
//        in.read ();
//        assertEquals ("state: " + p.getState () + " text: " + in, "whitespace", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "abcd", p.read (p.getState (), in));
//        assertEquals (0, in.current ());
//        assertEquals (true, in.eof ());
//    }
//    
//    public void testRead1 () throws ParseException {
//        Parser value = Parser.create ("[^\n]+", "VALUE");
//        Parser eol = Parser.create ("[\n]", "EOL");
//        Parser p = value.merge (eol).star ();
//        Input in = Input.create (
//            "test:ttt\n" +
//            "test\n" +
//            "#comment"
//        );
//        assertEquals ("state: " + p.getState () + " text: " + in, "VALUE", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "EOL", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "VALUE", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "EOL", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "VALUE", p.read (p.getState (), in));
//        assertEquals (0, in.current ());
//        assertEquals (true, in.eof ());
//    }
//    
//    public void testReadManifest () throws ParseException {
//        Parser p = Parser.create ();
//        p.add ("LINE_COMMENT", "#[^\n\r]*");
//        p.add ("EOL", "[\n\r][\n\r]?");
//        p.add (p.DEFAULT_STATE, "NAME", "[^:\n\r]*", "STATE_COLON");
//        p.add ("STATE_COLON", "COLON", ":", "STATE_VALUE");
//        p.add ("STATE_COLON", "EOL", "[\n\r][\n\r]?", p.DEFAULT_STATE);
//        p.add ("STATE_VALUE", "VALUE", "[^\n\r]*", p.DEFAULT_STATE);
//        
//        Input in = Input.create (
//            "#comment\n" +
//            "test:ttt\n" +
//            "#comment\n" +
//            "test\n" +
//            "t:t"
//        );
//        System.out.println(p);
//        assertEquals ("state: " + p.getState () + " text: " + in, "LINE_COMMENT", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "EOL", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "NAME", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "COLON", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "VALUE", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "EOL", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "LINE_COMMENT", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "EOL", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "NAME", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "EOL", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "NAME", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "COLON", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "VALUE", p.read (p.getState (), in));
//        assertEquals (0, in.current ());
//        assertEquals (true, in.eof ());
//    }
//    
    
    
    private static Parser create (String[] l) throws ParseException {
        List rules = new ArrayList ();
        int i, k = l.length;
        for (i = 0; i < k; i+=4) {
            String tokenName = (String) l [i + 1];
            String pattern = (String) l [i + 2];
            rules.add (Language.createTokenType (
                (String) l [i],
                Pattern.create (pattern),
                tokenName,
                (String) l [i + 3],
                i / 4,
                null
            ));
        }
        return Parser.create (rules);
    }
    
    public void testReadJava () throws ParseException {
        Parser parser = create (new String[] {
            null, "comment", "'/*'-'*/'", null,
            null, "comment", "'//'[^'\n''\r']*", null,
            null, "keyword", "'if'|'else'|'public'|'static'|'private'|'protected'|'class'|'extends'|'import'|'package'|'try'|'int'|'false'|'true'|'void'", null,
            null, "operator", "['\\\\''/''*''-''+''.'',''=''{''}''('')''!''@''#''$''%''^''&''~''|'';']", null,
            null, "string", "'\\\"'[^'\\\"']*'\\\"'", null,
            null, "char", "'\\\''[^'\\\'']'\\\''", null,
            null, "number", "['0'-'9']['0'-'9''.']*", null,
            null, "whitespace", "[' ''\t''\n''\r']+", null,
            null, "identifier", "['a'-'z''A'-'Z'][^' ''\\t''\\n''\\r''/''*''-''+''.'',''=''{''}''('')''!''@''#''$''%''^''&''~''|''\\\\'';']*", null
        });
        
        System.out.println (parser);
        CharInput in = new StringInput (
            "/** dsfsf\n" +
            " asd as * asdf */ " +
            "package ifa.vv.b;\n" +
            "import a.v;\n" +
            "\n" +
            "public class Asd extends sd {\n" +
            "    void main (String[] s) {\n" +
            "        return \"asd\" + \'a\' + 123;\n" +
            "    }\n" +
            "}",
            "test"
        );
        Cookie c = new MyCookie ();
        assertEquals ("state: " + c.getState () + " text: " + in, "comment", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertEquals ("state: " + c.getState () + " text: " + in, "whitespace", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertEquals ("state: " + c.getState () + " text: " + in, "keyword", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertEquals ("state: " + c.getState () + " text: " + in, "whitespace", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertEquals ("state: " + c.getState () + " text: " + in, "identifier", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertEquals ("state: " + c.getState () + " text: " + in, "operator", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertEquals ("state: " + c.getState () + " text: " + in, "identifier", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertEquals ("state: " + c.getState () + " text: " + in, "operator", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertEquals ("state: " + c.getState () + " text: " + in, "identifier", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertEquals ("state: " + c.getState () + " text: " + in, "operator", ((ASTToken) parser.read (c, in, "text/test")).getType ());
    }
    
    public void testReadHtml () throws ParseException {
        Parser parser = create (new String[] {
            null, "TEXT", "[^'<']*", null,
            null, "COMMENT", "'<!'-'-->'", null,
            Parser.DEFAULT_STATE, "ELEMENT", "'<'[^' ''>''\\t''\\n''\\r']+", "IN_ELEMENT",
            null, "END_ELEMENT", "'</'[^' ''>''\\t''\\n''\\r']+'>'", null,
            "IN_ELEMENT", "ELEMENT", "'/'?'>'", Parser.DEFAULT_STATE,
            "IN_ELEMENT", "ATTRIBUTE_NAME", "[^' ''>''=''\\t''\\n''\\r']+", "IN_ATTRIBUTE",
            "IN_ATTRIBUTE", "OPERATOR", "'='", "IN_ATTRIBUTE2",
            "IN_ATTRIBUTE2", "ATTRIBUTE_VALUE", "'\\\"'[^'\\\"']*'\\\"'", "IN_ELEMENT",
        
            "IN_ATTRIBUTE", "ELEMENT", "'/'?'>'", Parser.DEFAULT_STATE,
            "IN_ATTRIBUTE", "ATTRIBUTE_NAME", "[^' ''=''\\t''\\n''\\r']+", "IN_ATTRIBUTE",
            "IN_ELEMENT", "WHITESPACE", "[' ''\\t''\\n''\\r']*", "IN_ELEMENT",
            "IN_ATTRIBUTE", "WHITESPACE", "[' ''\\t''\\n''\\r']*", "IN_ATTRIBUTE",
            "IN_ATTRIBUTE2", "WHITESPACE", "[' ''\\t''\\n''\\r']*", "IN_ATTRIBUTE2"
        });
        
        CharInput in = new StringInput (
            "<test a=\"asz\"> \n" +
            " \" sdf\" = sdf" +
            "<!-- NewPage" +
            "  ?!?@? -- > sdf -> -->\n" +
            "<HTML><\\HTML> <test t>",
            "test"
        );
        Cookie c = new MyCookie ();
        assertEquals ("state: " + c + " text: " + in, "ELEMENT", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertEquals ("state: " + c + " text: " + in, "WHITESPACE", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertEquals ("state: " + c + " text: " + in, "ATTRIBUTE_NAME", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertEquals ("state: " + c + " text: " + in, "OPERATOR", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertEquals ("state: " + c + " text: " + in, "ATTRIBUTE_VALUE", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertEquals ("state: " + c + " text: " + in, "ELEMENT", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertEquals ("state: " + c + " text: " + in, "TEXT", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertEquals ("state: " + c + " text: " + in, "COMMENT", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertEquals ("state: " + c + " text: " + in, "TEXT", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertEquals ("state: " + c + " text: " + in, "ELEMENT", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertEquals ("state: " + c + " text: " + in, "ELEMENT", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertEquals ("state: " + c + " text: " + in, "ELEMENT", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertEquals ("state: " + c + " text: " + in, "ELEMENT", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertEquals ("state: " + c + " text: " + in, "TEXT", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertEquals ("state: " + c + " text: " + in, "ELEMENT", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertEquals ("state: " + c + " text: " + in, "WHITESPACE", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertEquals ("state: " + c + " text: " + in, "ATTRIBUTE_NAME", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertEquals ("state: " + c + " text: " + in, "ELEMENT", ((ASTToken) parser.read (c, in, "text/test")).getType ());
        assertTrue (in.eof ());
    }
    
    private static class MyCookie implements Parser.Cookie {
        
        private int state = -1;
        
        public int getState () {
            return state;
        }

        public void setState (int state) {
            this.state = state;
        }
    
        public void setProperties (Feature properties) {
        }
    }
}
