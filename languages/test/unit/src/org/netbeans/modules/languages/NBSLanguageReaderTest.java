/*
 * LanguageTest.java
 * JUnit based test
 *
 * Created on March 19, 2007, 9:26 AM
 */

package org.netbeans.modules.languages;

import junit.framework.TestCase;

import org.netbeans.api.languages.ParseException;


/**
 *
 * @author Jan Jancura
 */
public class NBSLanguageReaderTest extends TestCase {
    
    public NBSLanguageReaderTest(String testName) {
        super(testName);
    }
    
    public void testUnexpectedToken () {
        try {
            Language l = NBSLanguageReader.readLanguage (
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
                "etext = (<TEXT>)*;\n",
                "test.nbs", 
                "text/x-test"
            );
            assert (false);
        } catch (ParseException ex) {
            assertEquals ("test.nbs 11,10: Unexpected token <operator,'='>. Expecting <operator,';'>", ex.getMessage ());
        }
    }
    
    public void testUnexpectedCharacter () {
        try {
            Language l = NBSLanguageReader.readLanguage (
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
                "etext = (<TEXT>)*;\n",
                "test.nbs", 
                "text/x-test"
            );
            assert (false);
        } catch (ParseException ex) {
            assertEquals ("test.nbs 5,31: Unexpected character 'w'.", ex.getMessage ());
        }
    }
    
    public void testNoRule () {
        try {
            Language l = NBSLanguageReader.readLanguage (
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
            assert (false);
        } catch (ParseException ex) {
            assertEquals ("test.nbs 5,20: No rule for <identifier,'a'> in rePart.", ex.getMessage ());
        }
    }
    
    public void testCycle () throws ParseException {
        Language l = NBSLanguageReader.readLanguage (
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
            "startTag = S <TAG> (attribute)* ( <SYMBOL, '>'> | <SYMBOL, '/>'> );\n" +
            "endTag = <ENDTAG> <SYMBOL, '>'>; \n" +
            "attribute = <ATTRIBUTE>;\n" +
            "attribute = <ATTR_VALUE>; \n" +
            "attribute = <ATTRIBUTE> <SYMBOL,'='> <VALUE>; \n" +
            "etext = (<TEXT>)*;\n",
            "test.nbs", 
            "text/x-test"
        );
        try {
            l.getAnalyser ();
            assert (false);
        } catch (ParseException ex) {
            assertEquals ("cycle detected! tags [tags, tags$1, tags$2, startTag, S]", ex.getMessage ());
        }
    }
    
    public void testUndefinedNT () throws ParseException {
        Language l = NBSLanguageReader.readLanguage (
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
            l.getAnalyser ();
            assert (false);
        } catch (ParseException ex) {
            assertEquals ("endTag grammar rule not defined!", ex.getMessage ());
        }
    }
    
    public void testOK () throws ParseException {
        Language l = NBSLanguageReader.readLanguage (
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
            "endTag = <ENDTAG> <SYMBOL, '>'>; \n" +
            "attribute = <ATTRIBUTE>;\n" +
            "attribute = <ATTR_VALUE>; \n" +
            "attribute = <ATTRIBUTE> <SYMBOL,'='> <VALUE>; \n" +
            "etext = (<TEXT>)*;\n",
            "test.nbs", 
            "text/x-test"
        );
        l.getAnalyser ();
    }
}

