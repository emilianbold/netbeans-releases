/*
 * LanguageTest.java
 * JUnit based test
 *
 * Created on March 19, 2007, 9:26 AM
 */

package org.netbeans.modules.languages;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;

import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser;


/**
 *
 * @author Jan Jancura
 */
public class NBSLanguageReaderTest extends TestCase {
    
    public NBSLanguageReaderTest(String testName) {
        super(testName);
    }
    
    
    public void testOK () throws ParseException, IOException {
        NBSLanguageReader reader = NBSLanguageReader.create (
            "TOKEN:TAG:( '<' ['a'-'z']+ )\n" +
            "TOKEN:SYMBOL:( '>' | '=')\n" +
            "TOKEN:ENDTAG:( '</' ['a'-'z']+ )\n" +
            "TOKEN:ATTRIBUTE:( ['a'-'z']+ )\n" +
            "TOKEN:ATTR_VALUE:( '\\\"' [^ '\\n' '\\r' '\\\"' '\\uffff']+ '\\\"' )\n" +
            "TOKEN:VALUE:( '\\\"' [^ '\\n' '\\r' '\\\"']+ '\\\"' )\n" +
            "TOKEN:TEXT:( [^'<']+ )\n" +
            "\n" +
            "S = tags;\n" +
            "tags = (startTag | endTag | etext)*;\n" +
            "startTag = <TAG> (attribute)* ( <SYMBOL, '>'> | <SYMBOL, '/>'> );\n" +
            "endTag = <ENDTAG> <SYMBOL, '>'>; \n" +
            "attribute = <ATTRIBUTE>;\n" +
            "attribute = <ATTR_VALUE>; \n" +
            "attribute = <ATTRIBUTE> <SYMBOL,'='> <VALUE>; \n" +
            "etext = (<TEXT>)*;\n",
            "test.nbs", 
            "text/x-test"
        );
        LanguageImpl language = new LanguageImpl ("test/test", reader);
        language.read ();
        assertEquals (21, reader.getRules (language).size ());
    }
    
    public void testUnexpectedToken () throws IOException {
        try {
            LanguageImpl language = TestUtils.createLanguage (
                "TOKEN:TAG:( '<' ['a'-'z']+ )\n" +
                "TOKEN:SYMBOL:( '>' | '=')\n" +
                "TOKEN:ENDTAG:( '</' ['a'-'z']+ )\n" +
                "TOKEN:ATTRIBUTE:( ['a'-'z']+ )\n" +
                "TOKEN:ATTR_VALUE:( '\\\"' [^ '\\n' '\\r' '\\\"']+ '\\\"' )\n" +
                "TOKEN:VALUE:( '\\\"' [^ '\\n' '\\r' '\\\"']+ '\\\"' )\n" +
                "TOKEN:TEXT:( [^'<']+ )\n" +
                "\n" +
                "S = tags;\n" +
                "tags = (startTag | endTag | etext)*\n" +
                "startTag = <TAG> (attribute)* ( <SYMBOL, '>'> | <SYMBOL, '/>'> );\n" +
                "endTag = <ENDTAG> <SYMBOL, '>'>; \n" +
                "attribute = <ATTRIBUTE>;\n" +
                "attribute = <ATTR_VALUE>; \n" +
                "attribute = <ATTRIBUTE> <SYMBOL,'='> <VALUE>; \n" +
                "etext = (<TEXT>)*;\n"
            );
            language.read ();
            assert (false);
        } catch (ParseException ex) {
            assertEquals ("test.nbs 11,10: Unexpected token <operator,'='>. Expecting <operator,';'>", ex.getMessage ());
        }
    }
    
    public void testUnexpectedCharacter () throws IOException {
        try {
            LanguageImpl language = TestUtils.createLanguage (
                "TOKEN:TAG:( '<' ['a'-'z']+ )\n" +
                "TOKEN:SYMBOL:( '>' | '=')\n" +
                "TOKEN:ENDTAG:( '</' ['a'-'z']+ )\n" +
                "TOKEN:ATTRIBUTE:( ['a'-'z']+ )\n" +
                "TOKEN:ATTR_VALUE:( '\\\"' [^ '\\nw' '\\r' '\\\"']+ '\\\"' )\n" +
                "TOKEN:VALUE:( '\\\"' [^ '\\n' '\\r' '\\\"']+ '\\\"' )\n" +
                "TOKEN:TEXT:( [^'<']+ )\n" +
                "\n" +
                "S = tags;\n" +
                "tags = (startTag | endTag | etext)*;\n" +
                "startTag = <TAG> (attribute)* ( <SYMBOL, '>'> | <SYMBOL, '/>'> );\n" +
                "endTag = <ENDTAG> <SYMBOL, '>'>; \n" +
                "attribute = <ATTRIBUTE>;\n" +
                "attribute = <ATTR_VALUE>; \n" +
                "attribute = <ATTRIBUTE> <SYMBOL,'='> <VALUE>; \n" +
                "etext = (<TEXT>)*;\n"
            );
            language.read ();
            assert (false);
        } catch (ParseException ex) {
            assertEquals ("test.nbs 5,29: Unexpected character 'w'.", ex.getMessage ());
        }
    }
    
    public void testNoRule () throws IOException {
        try {
            NBSLanguageReader reader = NBSLanguageReader.create (
                "TOKEN:TAG:( '<' ['a'-'z']+ )\n" +
                "TOKEN:SYMBOL:( '>' | '=')\n" +
                "TOKEN:ENDTAG:( '</' ['a'-'z']+ )\n" +
                "TOKEN:ATTRIBUTE:( ['a'-'z']+ )\n" +
                "TOKEN:ATTR_VALUE:( a '\\\"' [^ '\\n' '\\r' '\\\"']+ '\\\"' )\n" +
                "TOKEN:VALUE:( '\\\"' [^ '\\n' '\\r' '\\\"']+ '\\\"' )\n" +
                "TOKEN:TEXT:( [^'<']+ )\n" +
                "\n" +
                "S = tags;\n" +
                "tags = (startTag | endTag | etext)*;\n" +
                "startTag = <TAG> (attribute)* ( <SYMBOL, '>'> | <SYMBOL, '/>'> );\n" +
                "endTag = <ENDTAG> <SYMBOL, '>'>; \n" +
                "attribute = <ATTRIBUTE>;\n" +
                "attribute = <ATTR_VALUE>; \n" +
                "attribute = <ATTRIBUTE> <SYMBOL,'='> <VALUE>; \n" +
                "etext = (<TEXT>)*;\n",
                "test.nbs", 
                "text/x-test"
            );
            reader.getTokenTypes ();
            assert (false);
        } catch (ParseException ex) {
            assertEquals ("test.nbs 5,20: Syntax error (nt: rePart, tokens: <identifier,'a'> <whitespace,' '>.", ex.getMessage ());
        }
    }
}

