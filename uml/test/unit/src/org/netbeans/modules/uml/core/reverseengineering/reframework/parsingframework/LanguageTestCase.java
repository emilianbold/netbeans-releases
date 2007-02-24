package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;

import org.dom4j.Element;


/**
 * Test cases for Language.
 */
public class LanguageTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(LanguageTestCase.class);
    }

    private ILanguage l = product.getLanguageManager().getLanguage("Java");

    public void testGetCodeGenerationScripts()
    {
        assertTrue(l.getCodeGenerationScripts().size() > 0);
    }

	// String should not be considered as java data type #79093
//    public void testIsDataType()
//    {
//        assertTrue(l.isDataType("String"));
//    }

    public void testGetDataType()
    {
        assertEquals("int", l.getDataType("int").getName());
    }

    public void testGetExpansionVariables()
    {
        assertTrue(l.getExpansionVariables().size() > 0);
    }

    public void testIsFeatureSupported()
    {
        assertTrue(l.isFeatureSupported("Operation Reverse Engineering"));
    }

    public void testGetFormatDefinition()
    {
        assertEquals(
                ((Element) l.getFormatDefinition("NamedElement"))
                            .attributeValue("xmi.id"),
                ((Element) l.getFormatDefinition("Unknown")
                 .selectSingleNode("aDefinition")).attributeValue("pdref"));
    }

    public void testIsKeyword()
    {
        assertTrue(l.isKeyword("if"));
    }

//    public void testGetLibraryDefinition()
//    {
//        assertEquals(
//                new File(product.getLanguageManager().getConfigLocation(),
//                        "libraries/java13").toString(), 
//                l.getLibraryDefinition("JDK 1.3"));
//    }

    public void testGetLibraryNames()
    {
        assertEquals("JDK 1.6", l.getLibraryNames().get(0));
    }
    
    public void testIsPrimitive()
    {
        assertTrue(l.isPrimitive("int"));
    }
}
