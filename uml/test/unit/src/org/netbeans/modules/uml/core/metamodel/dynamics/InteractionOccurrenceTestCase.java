package org.netbeans.modules.uml.core.metamodel.dynamics;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for InteractionOccurrence.
 */
public class InteractionOccurrenceTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(InteractionOccurrenceTestCase.class);
    }

    private IInteractionOccurrence occ;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        occ = createType("InteractionOccurrence");
    }
    
    public void testSetBehavior()
    {
        IBehavior beh = createType("Activity");
        occ.setBehavior(beh);
        assertEquals(beh.getXMIID(), occ.getBehavior().getXMIID());
    }

    public void testGetBehavior()
    {
        // Tested by testSetBehavior.
    }

    public void testAddGate()
    {
        IGate g = createType("Gate");
        occ.addGate(g);
        assertEquals(1, occ.getGates().size());
        assertEquals(g.getXMIID(), occ.getGates().get(0).getXMIID());
    }

    public void testRemoveGate()
    {
        testAddGate();
        occ.removeGate(occ.getGates().get(0));
        assertEquals(0, occ.getGates().size());
    }

    public void testGetGates()
    {
        // Tested by testAddGate.
    }

    public void testSetInteraction()
    {
        IInteraction i = createType("Interaction");
        occ.setInteraction(i);
        assertEquals(i.getXMIID(), occ.getInteraction().getXMIID());
    }

    public void testGetInteraction()
    {
        // Tested by testSetInteraction.
    }
}