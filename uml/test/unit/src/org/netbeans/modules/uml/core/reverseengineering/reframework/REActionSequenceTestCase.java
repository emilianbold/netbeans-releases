package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.dom4j.Document;
import org.dom4j.Element;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
/**
 * Test cases for REActionSequence.
 */
public class REActionSequenceTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(REActionSequenceTestCase.class);
    }

    private IREActionSequence reas;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        reas = new REActionSequence();
        
        Document doc = XMLManip.getDOMDocument();
        Element el = XMLManip.createElement(doc, "UML:ActionSequence");
        el.addAttribute("break", "true");
        XMLManip.createElement(el, "UML:CallAction");
        XMLManip.createElement(el, "UML:DestroyAction");

        reas.setEventData(el);
    }

    public void testGetCount()
    {
        assertEquals(2, reas.getCount());
    }

    public void testGetIsBreakCalled()
    {
        assertTrue(reas.getIsBreakCalled());
    }

    public void testItem()
    {
        assertTrue(reas.item(0) instanceof RECallAction);
        assertTrue(reas.item(1) instanceof REDestroyAction);
    }
}