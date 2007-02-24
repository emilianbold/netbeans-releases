package org.netbeans.modules.uml.core.metamodel.dynamics;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;

/**
 * Test cases for InterGateConnector.
 */
public class InterGateConnectorTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(InterGateConnectorTestCase.class);
    }

    private IInterGateConnector igc;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        igc = createType("InterGateConnector");
    }

    public void testSetEventOccurrence()
    {
        IEventOccurrence eo = createType("EventOccurrence");
        igc.setEventOccurrence(eo);
        assertEquals(eo.getXMIID(), igc.getEventOccurrence().getXMIID());
    }

    public void testGetEventOccurrence()
    {
        // Tested by testSetEventOccurrence.
    }

    public void testSetFragment()
    {
        IInteractionFragment frag = createType("CombinedFragment");
        igc.setFragment(frag);
        assertEquals(frag.getXMIID(), igc.getFragment().getXMIID());
    }

    public void testGetFragment()
    {
        // Tested by testSetFragment.
    }

    public void testSetFromGate()
    {
        IGate g = createType("Gate");
        igc.setFromGate(g);
        assertEquals(g.getXMIID(), igc.getFromGate().getXMIID());
    }

    public void testGetFromGate()
    {
        // Tested by testSetFromGate.
    }

    public void testSetMessage()
    {
        IMessage m = createType("Message");
        igc.setMessage(m);
        assertEquals(m.getXMIID(), igc.getMessage().getXMIID());
    }

    public void testGetMessage()
    {
        // Tested by testSetMessage.
    }

    public void testSetToGate()
    {
        IGate g = createType("Gate");
        igc.setToGate(g);
        assertEquals(g.getXMIID(), igc.getToGate().getXMIID());
    }

    public void testGetToGate()
    {
        // Tested by testSetToGate.
    }
}