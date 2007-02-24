package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.dom4j.Element;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
/**
 * Test cases for REExceptionJumpHandlerEvent.
 */
public class REExceptionJumpHandlerEventTestCase extends AbstractRETestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(REExceptionJumpHandlerEventTestCase.class);
    }

    private REExceptionJumpHandlerEvent rej;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        rej = new REExceptionJumpHandlerEvent();
        createBaseElement(rej, "UML:ExceptionJumpHandler");
        attr("isDefault", "false");
        Element sig = XMLManip.createElement(element, "UML:Signal");
        sig.addAttribute("name", "Exception");
        sig.addAttribute("instanceName", "quark");
    }

    public void testGetExceptionName()
    {
        assertEquals("quark", rej.getExceptionName());
    }

    public void testGetExceptionType()
    {
        assertEquals("Exception", rej.getExceptionType());
    }

    public void testGetIsDefault()
    {
        assertFalse(rej.getIsDefault());
    }

    public void testGetStringRepresentation()
    {
        assertEquals("Exception quark", rej.getStringRepresentation());
    }
}