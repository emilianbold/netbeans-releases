/*
 * LanguageTest.java
 * JUnit based test
 *
 * Created on March 19, 2007, 9:26 AM
 */

package org.netbeans.modules.languages;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser;
import org.netbeans.modules.languages.parser.Parser;


/**
 *
 * @author Jan Jancura
 */
public class NBSLanguageReaderTest extends TestCase {
    
    public NBSLanguageReaderTest(String testName) {
        super(testName);
    }
    
    
    public void testOK () throws ParseException {
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
        List<TokenType> tokenTypes = reader.getTokenTypes ();
        assertEquals (10, tokenTypes.size ());
        assertEquals (0, reader.getFeatures ().size ());
        Map<Integer,String> tokensMap = new HashMap<Integer,String> ();
        Iterator<TokenType> it = tokenTypes.iterator ();
        while (it.hasNext ()) {
            TokenType tokenType = it.next ();
            tokensMap.put (tokenType.getTypeID (), tokenType.getType ());
        }
        Language language = Language.create ("test/test", tokensMap, reader.getFeatures (), Parser.create (tokenTypes));
        assertEquals (21, reader.getRules (language).size ());
    }
    
    public void testUnexpectedToken () {
        try {
            Language language = TestUtils.createLanguage (
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
            assert (false);
        } catch (ParseException ex) {
            assertEquals ("test.nbs 11,10: Unexpected token <operator,'='>. Expecting <operator,';'>", ex.getMessage ());
        }
    }
    
    public void testUnexpectedCharacter () {
        try {
            Language language = TestUtils.createLanguage (
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
            assert (false);
        } catch (ParseException ex) {
            assertEquals ("test.nbs 5,29: Unexpected character 'w'.", ex.getMessage ());
        }
    }
    
    public void testNoRule () {
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
    
    public void testUndefinedNT () throws ParseException {
        NBSLanguageReader reader = NBSLanguageReader.create (
            "TOKEN:TAG:( '<' ['a'-'z']+ )\n" +
            "TOKEN:SYMBOL:( '>' | '=')\n" +
            "TOKEN:ENDTAG:( '</' ['a'-'z']+ )\n" +
            "TOKEN:ATTRIBUTE:( ['a'-'z']+ )\n" +
            "TOKEN:ATTR_VALUE:( '\\\"' [^ '\\n' '\\r' '\\\"']+ '\\\"' )\n" +
            "TOKEN:VALUE:( '\\\"' [^ '\\n' '\\r' '\\\"']+ '\\\"' )\n" +
            "TOKEN:TEXT:( [^'<']+ )\n" +
            "\n" +
            "S = tags;\n" +
            "tags = (startTag | endTag | etext)*;\n" +
            "startTag = <TAG> (attribute)* ( <SYMBOL, '>'> | <SYMBOL, '/>'> );\n" +
            "attribute = <ATTRIBUTE>;\n" +
            "attribute = <ATTR_VALUE>; \n" +
            "attribute = <ATTRIBUTE> <SYMBOL,'='> <VALUE>; \n" +
            "etext = (<TEXT>)*;\n",
            "test.nbs", 
            "text/x-test"
        );
        try {
            List<TokenType> tokenTypes = reader.getTokenTypes ();
            assertEquals (10, tokenTypes.size ());
            assertEquals (0, reader.getFeatures ().size ());
            Map<Integer,String> tokensMap = new HashMap<Integer,String> ();
            Iterator<TokenType> it = tokenTypes.iterator ();
            while (it.hasNext ()) {
                TokenType tokenType = it.next ();
                tokensMap.put (tokenType.getTypeID (), tokenType.getType ());
            }
            Language language = Language.create ("test/test", tokensMap, reader.getFeatures (), Parser.create (tokenTypes));
            List<Rule> grammarRules = reader.getRules (language);
            assertEquals (20, grammarRules.size ());
            LLSyntaxAnalyser.create (language, grammarRules, Collections.<Integer>emptySet ());
            assert (false);
        } catch (ParseException ex) {
            assertEquals ("endTag grammar rule not defined!", ex.getMessage ());
        }
    }
}

