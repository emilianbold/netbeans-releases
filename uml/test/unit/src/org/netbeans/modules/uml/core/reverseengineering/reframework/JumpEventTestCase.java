package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.dom4j.Document;
import org.dom4j.Element;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
/**
 * Test cases for JumpEvent.
 */
public class JumpEventTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(JumpEventTestCase.class);
    }

    private IJumpEvent je;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        je = new JumpEvent();
        
        Document doc = XMLManip.getDOMDocument();
        Element el = XMLManip.createElement(doc, "UML:Event");
        Element op = XMLManip.createElement(el, "UML:InputPin");
        op.addAttribute("value", "Thurber");
        el.addAttribute("type", "Continue");
        je.setEventData(el);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testGetDestination()
    {
        assertEquals("Thurber", je.getDestination());
    }

    public void testGetJumpType()
    {
        assertEquals(IJumpEvent.JE_CONTINUE, je.getJumpType());
    }
}