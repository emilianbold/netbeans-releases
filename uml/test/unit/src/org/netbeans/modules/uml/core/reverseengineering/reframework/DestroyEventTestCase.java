package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.dom4j.Document;
import org.dom4j.Element;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
/**
 * Test cases for DestroyEvent.
 */
public class DestroyEventTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(DestroyEventTestCase.class);
    }

    private IDestroyEvent de;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        de = new DestroyEvent();
        Document doc = XMLManip.getDOMDocument();
        Element el = XMLManip.createElement(doc, "UML:Event");
        Element op = XMLManip.createElement(el, "UML:InputPin");
        op.addAttribute("value", "Thurman");
        el.addAttribute("classifier", "Zelazny");
        
        Element toks = XMLManip.createElement(el, "TokenDescriptors");

        Element tdp  = XMLManip.createElement(toks, "TDescriptor");
        tdp.addAttribute("value", "true");
        tdp.addAttribute("type", "IsPrimitive");

        de.setEventData(el);
    }
    
    public void testGetInstanceName()
    {
        assertEquals("Thurman", de.getInstanceName());
    }

    public void testGetInstanceTypeName()
    {
        assertEquals("Zelazny", de.getInstanceTypeName());
    }

    public void testGetIsPrimitive()
    {
        assertTrue(de.getIsPrimitive());
    }
}