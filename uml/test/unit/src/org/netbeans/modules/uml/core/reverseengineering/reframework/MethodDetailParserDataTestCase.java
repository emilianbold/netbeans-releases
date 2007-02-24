package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.dom4j.Document;
import org.dom4j.Element;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
/**
 * Test cases for MethodDetailParserData.
 */
public class MethodDetailParserDataTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(MethodDetailParserDataTestCase.class);
    }

    private IREMethodDetailData mdpd;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        mdpd = new MethodDetailParserData();
        
        Document doc = XMLManip.getDOMDocument();
        Element el = XMLManip.createElement(doc, "UML:Event");
        el.addAttribute("line", "237");
        mdpd.setEventData(el);
    }

    public void testGetLine()
    {
        assertEquals(237L, mdpd.getLine());
    }
}