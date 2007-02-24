package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.dom4j.Document;
import org.dom4j.Element;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
/**
 * Test cases for ParserData.
 */
public class ParserDataTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ParserDataTestCase.class);
    }

    private ParserData pd;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        pd = new ParserData();
        
        Document doc = XMLManip.getDOMDocument();
        Element el = XMLManip.createElement(doc, "UML:ParserData");
//        Element op = XMLManip.createElement(el, "UML:InputPin");
//        op.addAttribute("value", "Thurman");
//        el.addAttribute("classifier", "Zelazny");
        
        Element toks = XMLManip.createElement(el, "TokenDescriptors");

        Element tdp  = XMLManip.createElement(toks, "TDescriptor");
        tdp.addAttribute("value", "Xyz.java");
        tdp.addAttribute("type", "Filename");
        
        pd.setEventData(el);
    }

    public void testGetFilename()
    {
        assertEquals("Xyz.java", pd.getFilename());
    }

    public void testGetTokenDescriptor()
    {
        assertNotNull(pd.getTokenDescriptor("Filename"));
        assertNull(pd.getTokenDescriptor("gnihtoN"));
    }

    public void testGetTokenDescriptors()
    {
        assertEquals(1, pd.getTokenDescriptors().size());
    }
}
