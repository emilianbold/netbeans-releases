package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.dom4j.Element;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
/**
 * Test cases for ReferenceEvent.
 */
public class ReferenceEventTestCase extends AbstractRETestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ReferenceEventTestCase.class);
    }

    private ReferenceEvent re;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        re = new ReferenceEvent();
        createBaseElement(re, "UML:ReferenceEvent");
        attr("name", "Franca");
        attr("type", "Type");
        Element el = XMLManip.createElement(element, "ReferenceVariable");
        el.addAttribute("name", "Lingua");
        
        IClass c = createClass("Monolith");
        c.getElementNode().detach();
        element.add(c.getElementNode());
    }

    public void testGetFullName()
    {
        assertEquals("Lingua::Franca", re.getFullName());
    }

    public void testGetName()
    {
        assertEquals("Franca", re.getName());
    }

    public void testGetParentReference()
    {
        assertEquals("Lingua", re.getParentReference().getName());
    }

    public void testGetREClass()
    {
        assertEquals("Monolith", re.getREClass().getName());
    }

    public void testGetType()
    {
        assertEquals("Type", re.getType());
    }
}