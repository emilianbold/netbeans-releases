package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.dom4j.Document;
import org.dom4j.Element;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
/**
 * Test cases for RECallAction.
 */
public class RECallActionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(RECallActionTestCase.class);
    }

    private RECallAction reca;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        reca = new RECallAction();
        
        Document doc = XMLManip.getDOMDocument();
        Element el = XMLManip.createElement(doc, "UML:CallAction");
        el.addAttribute("operation", "tumbleweed");
        
        Element toks = XMLManip.createElement(el, "TokenDescriptors");

        Element tdp  = XMLManip.createElement(toks, "TDescriptor");
        tdp.addAttribute("value", "Roger");
        tdp.addAttribute("type", "OperationOwner");
        
        tdp  = XMLManip.createElement(toks, "TDescriptor");
        tdp.addAttribute("value", "Zelazny");
        tdp.addAttribute("type", "Name");
        
        tdp  = XMLManip.createElement(toks, "TDescriptor");
        tdp.addAttribute("value", "Angband");
        tdp.addAttribute("type", "ContainingClass");
        
        tdp  = XMLManip.createElement(toks, "TDescriptor");
        tdp.addAttribute("value", "true");
        tdp.addAttribute("type", "StaticInstance");
        
        tdp  = XMLManip.createElement(toks, "TDescriptor");
        tdp.addAttribute("value", "true");
        tdp.addAttribute("type", "StaticOperation");

        tdp  = XMLManip.createElement(toks, "TDescriptor");
        tdp.addAttribute("value", "Zebra");
        tdp.addAttribute("type", "ReturnTypeClass");
        
        tdp  = XMLManip.createElement(toks, "TDescriptor");
        tdp.addAttribute("value", "ungulate");
        tdp.addAttribute("type", "ReturnTypePackage");
        
        tdp  = XMLManip.createElement(toks, "TDescriptor");
        tdp.addAttribute("value", "Name");
        tdp.addAttribute("type", "ReturnType");
        
        reca.setEventData(el);
    }
    
    public void testGetImplementingClass()
    {
        assertEquals("Roger", reca.getImplementingClass());
    }

    public void testGetInstanceName()
    {
        assertEquals("Zelazny", reca.getInstanceName());
    }

    public void testGetInstanceOwner()
    {
        assertEquals("Angband", reca.getInstanceOwner());
    }

    public void testGetIsInstanceStatic()
    {
        assertTrue(reca.getIsInstanceStatic());
    }

    public void testGetIsOperationStatic()
    {
        assertTrue(reca.getIsOperationStatic());
    }

    public void testGetOperationName()
    {
        assertEquals("tumbleweed", reca.getOperationName());
    }

    public void testGetReturnTypeClass()
    {
        assertEquals("Zebra", reca.getReturnTypeClass());
    }

    public void testGetReturnTypePackage()
    {
        assertEquals("ungulate", reca.getReturnTypePackage());
    }

    public void testGetReturnType()
    {
        assertEquals("Name", reca.getReturnType());
    }
}
