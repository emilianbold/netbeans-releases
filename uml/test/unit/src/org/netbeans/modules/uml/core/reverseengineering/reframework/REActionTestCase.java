package org.netbeans.modules.uml.core.reverseengineering.reframework;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IInputPin;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IPrimitiveAction;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;

/**
 * Test cases for REAction.
 */
public class REActionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(REActionTestCase.class);
    }

    private REAction rea;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        rea = new REAction();
        
        IPrimitiveAction pa = createType("DestroyObjectAction");
        IInputPin pin = createType("InputPin");
        pa.addInput(pin);
        
        rea.setEventData(pa.getNode());
        pa.getElementNode().addAttribute("recurrence", "10");
        pa.getElementNode().addAttribute("target", "cornuthaum");
    }
    
    public void testGetArguments()
    {
        ETList<IREArgument> args = rea.getArguments();
        assertNotNull(args);
        assertEquals(1, args.size());
    }

    public void testGetReceiver()
    {
        // There appears to be no way to produce the XML that REAction wants,
        // short of monkeying with the DOM tree directly.
    }

    public void testGetRecurrence()
    {
        assertEquals("10", rea.getRecurrence());
    }

    public void testGetSender()
    {
        // There appears to be no way to produce the XML that REAction wants,
        // short of monkeying with the DOM tree directly.
    }

    public void testGetTarget()
    {
        assertEquals("cornuthaum", rea.getTarget());
    }
}