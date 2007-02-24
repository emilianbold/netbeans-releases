package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.dom4j.Element;

import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
/**
 * Test cases for REParameter.
 */
public class REParameterTestCase extends AbstractRETestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(REParameterTestCase.class);
    }

    private REParameter rep;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        rep = new REParameter();
        
        IParameter par = createType("Parameter");
        par.setDefault2("99");
        par.setDirection(BaseElement.PDK_IN);
        
        rep.setEventData(par.getNode());

        element = par.getElementNode();
        Element el = XMLManip.createElement(element, 
                                "UML:Parameter.defaultValue");
        el = XMLManip.createElement(el, 
                "UML:Expression");
        el.addAttribute("body", "99");
    }
    
    public void testGetDefaultValue()
    {
        assertEquals("99", rep.getDefaultValue());
    }

    public void testGetKind()
    {
        assertEquals(BaseElement.PDK_IN, rep.getKind());
    }
}