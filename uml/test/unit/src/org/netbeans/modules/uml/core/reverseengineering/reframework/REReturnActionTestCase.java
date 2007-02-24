package org.netbeans.modules.uml.core.reverseengineering.reframework;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IInputPin;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IPrimitiveAction;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;

/**
 * Test cases for REReturnAction.
 */
public class REReturnActionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(REReturnActionTestCase.class);
    }

    private IREReturnAction rera;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        rera = new REReturnAction();
        
        IPrimitiveAction act = createType("DestroyObjectAction");
        IInputPin pin = createType("InputPin");
        pin.getElementNode().addAttribute("value", "fubar");
        act.addInput(pin);
        
        rera.setEventData(act.getNode());
    }
    
    public void testGetReturnValue()
    {
        assertEquals("fubar", rera.getReturnValue());
    }
}
