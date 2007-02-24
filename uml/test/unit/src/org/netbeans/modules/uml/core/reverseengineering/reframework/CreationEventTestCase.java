package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.dom4j.Document;
import org.dom4j.Element;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
/**
 * Test cases for CreationEvent.
 */
public class CreationEventTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(CreationEventTestCase.class);
    }

    private ICreationEvent ce;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        ce = new CreationEvent();
        Document doc = XMLManip.getDOMDocument();
        Element el = XMLManip.createElement(doc, "UML:Event");
        Element op = XMLManip.createElement(el, "UML:OutputPin");
        op.addAttribute("value", "Thurman");
        el.addAttribute("classifier", "Zelazny");
        
        XMLManip.createElement(el, "UML:Enumeration");
        
        Element toks = XMLManip.createElement(el, "TokenDescriptors");
        Element td   = XMLManip.createElement(toks, "TDescriptor");
        td.addAttribute("value", "Cthulhu");
        td.addAttribute("type", "InstantiatedTypeName");

        Element tdp  = XMLManip.createElement(toks, "TDescriptor");
        tdp.addAttribute("value", "true");
        tdp.addAttribute("type", "IsPrimitive");

        ce.setEventData(el);
    }

    public void testGetInstanceName()
    {
        assertEquals("Thurman", ce.getInstanceName());
    }

    public void testGetInstanceTypeName()
    {
        assertEquals("Zelazny", ce.getInstanceTypeName());
    }

    public void testGetInstantiatedTypeName()
    {
        assertEquals("Cthulhu", ce.getInstantiatedTypeName());
    }

    public void testGetIsPrimitive()
    {
        assertTrue(ce.getIsPrimitive());
    }

    public void testGetIsStatic()
    {
        assertFalse(ce.getIsStatic());
    }

    public void testGetREClass()
    {
        assertNotNull(ce.getREClass());
    }
}