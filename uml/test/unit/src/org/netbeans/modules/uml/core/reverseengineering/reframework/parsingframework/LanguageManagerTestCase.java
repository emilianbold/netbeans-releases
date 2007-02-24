package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.reverseengineering.parsers.javaparser.REJavaParser;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * Test cases for LanguageManager.
 */
public class LanguageManagerTestCase extends AbstractUMLTestCase
{
   
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(LanguageManagerTestCase.class);
    }

    private ILanguageManager lm = product.getLanguageManager();

    public void testGetAttributeDefaultType()
    {
        assertEquals("int", 
                lm.getAttributeDefaultType(createClass("A")).getName());
    }

    public void testRetrieveContextForFile()
    {
        writeFile(null, null);
        assertEquals("org.netbeans.modules.uml.core.roundtripframework." +
                        "requestprocessors.javarpcomponent.JavaRequestProcessor", 
                        lm.retrieveContextForFile("Xyz.java", "RoundTrip"));
    }

    public void testRetrieveContextForLanguage()
    {
        assertEquals("org.netbeans.modules.uml.core.roundtripframework." +
                "requestprocessors.javarpcomponent.JavaRequestProcessor", 
                lm.retrieveContextForLanguage("Java", "RoundTrip"));
    }

    public void testGetDefaultForLanguage()
    {
        assertEquals(
                "null",
                lm.getDefaultForLanguage("Java", 
                                         "UnknownDataType Initialization"));
    }

    public void testGetDefaultLanguage()
    {
        assertEquals("Java", lm.getDefaultLanguage(createClass("Foo")).getName());
    }

    public void testGetDefaultSourceFileExtensionForLanguage()
    {
        assertEquals("java", lm.getDefaultSourceFileExtensionForLanguage("Java"));
    }

    public void testGetFileExtensionFilters()
    {
        ILanguageFilter fil = lm.getFileExtensionFilters("Java").get(0);
        assertEquals("Source Files", fil.getName());
        assertEquals("*.java", fil.getFilter());
    }

    public void testGetFileExtensionsForLanguage()
    {
        IStrings s = lm.getFileExtensionsForLanguage("Java");
        assertEquals(1, s.size());
        assertEquals("java", s.get(0));
    }

    public void testGetLanguageForFile()
    {
        assertEquals("Java", lm.getLanguageForFile("Xyz.java").getName());
    }

    public void testGetLanguage()
    {
        assertEquals("Java", lm.getLanguage("Java").getName());
    }

    public void testGetLanguagesWithCodeGenSupport()
    {
        ETList<ILanguage> langs = lm.getLanguagesWithCodeGenSupport();
        assertEquals(1, langs.size());
    }

    public void testGetOperationDefaultType()
    {
        assertEquals("void", 
                lm.getOperationDefaultType(createClass("Yak")).getName());
    }

    public void testGetParserForFile()
    {
        assertTrue(
                lm.getParserForFile("Xyz.java", "Default") 
                    instanceof REJavaParser);
        
    }

    public void testRetrieveParserForLanguage()
    {
        assertTrue(
                lm.retrieveParserForLanguage("Java", "Default") 
                    instanceof REJavaParser);
    }

    public void testGetSupportedLanguages2()
    {
        assertEquals(2, lm.getSupportedLanguages2().size());
    }

    public void testGetSupportedLanguagesAsString()
    {
        assertEquals("Java|UML", 
                lm.getSupportedLanguagesAsString());
    }

    public void testGetSupportedLanguages()
    {
        assertEquals(2, lm.getSupportedLanguages().size());
    }
}