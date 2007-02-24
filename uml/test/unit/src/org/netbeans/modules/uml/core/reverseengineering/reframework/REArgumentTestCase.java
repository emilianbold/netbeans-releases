package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.dom4j.Document;
import org.dom4j.Element;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
/**
 * Test cases for REArgument.
 */
public class REArgumentTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(REArgumentTestCase.class);
    }

    private REArgument rea;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        rea = new REArgument();
        Document doc = XMLManip.getDOMDocument();
        Element el = XMLManip.createElement(doc, "UML:Argument");
        el.addAttribute("kind", "Val");
        el.addAttribute("value", "eon");
        el.addAttribute("name", "Omega");
        rea.setEventData(el);
    }
    
    public void testGetName()
    {
        assertEquals("Omega", rea.getName());
    }

    public void testGetValue()
    {
        assertEquals("eon", rea.getValue());
    }
    
    public void testGetType()
    {
        Element e = (Element) rea.getEventData();
        e.addAttribute("kind", "Type");
        assertEquals("eon", rea.getType());
    }
}