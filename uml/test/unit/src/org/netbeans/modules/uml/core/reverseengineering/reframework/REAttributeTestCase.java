package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.dom4j.Element;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
/**
 * Test cases for REAttribute.
 */
public class REAttributeTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(REAttributeTestCase.class);
    }

    private REAttribute rea;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        rea = new REAttribute();
        IAttribute attr = createType("Attribute");
        attr.setType2("int");
        attr.setIsTransient(true);
        attr.setIsVolatile(true);
        
        rea.setEventData(attr.getNode());
        
        Element el = attr.getElementNode();
        Element toks = XMLManip.createElement(el, "TokenDescriptors");
        Element tdp  = XMLManip.createElement(toks, "TDescriptor");
        tdp.addAttribute("value", "true");
        tdp.addAttribute("type", "IsPrimitive");
        
        tdp  = XMLManip.createElement(toks, "TDescriptor");
        tdp.addAttribute("value", "32");
        tdp.addAttribute("type", "InitialValue");
    }
    
    public void testGetInitialValue()
    {
        assertEquals("32", rea.getInitialValue());
    }

    public void testGetIsPrimitive()
    {
        assertTrue(rea.getIsPrimitive());
    }

    public void testGetIsTransient()
    {
        assertTrue(rea.getIsTransient());
    }

    public void testGetIsVolatile()
    {
        assertTrue(rea.getIsVolatile());
    }
}