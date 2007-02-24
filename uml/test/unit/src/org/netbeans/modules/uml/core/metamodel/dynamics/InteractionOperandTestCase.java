package org.netbeans.modules.uml.core.metamodel.dynamics;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for InteractionOperand.
 */
public class InteractionOperandTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(InteractionOperandTestCase.class);
    }

    private IInteractionOperand op;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        op = createType("InteractionOperand");
    }

    public void testGetCoveredMessages()
    {
        IMessage m = createType("Message");
        IEventOccurrence eo = createType("EventOccurrence");
        eo.setSendMessage(m);
        
        IAtomicFragment frag = createType("AtomicFragment");
        frag.setEvent(eo);
        
        op.addFragment(frag);
        
        assertEquals(1, op.getCoveredMessages().size());
        assertEquals(m.getXMIID(), op.getCoveredMessages().get(0).getXMIID());
    }

    public void testAddFragment()
    {
        IInteractionFragment frag = createType("AtomicFragment");
        op.addFragment(frag);
        assertEquals(1, op.getFragments().size());
        assertEquals(frag.getXMIID(), op.getFragments().get(0).getXMIID());
    }

    public void testRemoveFragment()
    {
        testAddFragment();
        op.removeFragment(op.getFragments().get(0));
        assertEquals(0, op.getFragments().size());
    }

    public void testGetFragments()
    {
        // Tested by testAddFragment.
    }

    public void testCreateGuard()
    {
        assertNotNull(op.createGuard());
    }

    public void testSetGuard()
    {
        IInteractionConstraint guard = op.createGuard();
        op.setGuard(guard);
        assertEquals(guard.getXMIID(), op.getGuard().getXMIID());
    }

    public void testGetGuard()
    {
        // Tested by testSetGuard.
    }
}