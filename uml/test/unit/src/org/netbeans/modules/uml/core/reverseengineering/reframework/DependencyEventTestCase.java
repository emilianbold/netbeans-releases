package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.dom4j.Document;
import org.dom4j.Element;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
/**
 * Test cases for DependencyEvent.
 */
public class DependencyEventTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(DependencyEventTestCase.class);
    }

    private IDependencyEvent de;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        de = new DependencyEvent();

        Document doc = XMLManip.getDOMDocument();
        Element el = XMLManip.createElement(doc, "UML:DependencyEvent");
        el.addAttribute("classifier", "Zelazny");
        el.addAttribute("client", "Inca");
        el.addAttribute("supplier", "Navy::Seal");
        
        XMLManip.createElement(el, "UML:Enumeration");
        
        Element toks = XMLManip.createElement(el, "TokenDescriptors");
        Element td   = XMLManip.createElement(toks, "TDescriptor");
        td.addAttribute("value", "true");
        td.addAttribute("type", "Class Dependency");

        de.setEventData(el);
    }
    
    public void testGetClient()
    {
        assertEquals("Inca", de.getClient());
    }

    public void testGetIsClassDependency()
    {
        assertTrue(de.getIsClassDependency());
    }

    public void testIsSameClass()
    {
        assertTrue(de.isSameClass("Seal"));
        assertFalse(de.isSameClass("Laes"));
    }

    public void testGetSupplierClassName()
    {
        assertEquals("Seal", de.getSupplierClassName());
    }

    public void testGetSupplierPackage()
    {
        assertEquals("Navy", de.getSupplierPackage());
    }

    public void testGetSupplier()
    {
        assertEquals("Navy::Seal", de.getSupplier());
    }
}