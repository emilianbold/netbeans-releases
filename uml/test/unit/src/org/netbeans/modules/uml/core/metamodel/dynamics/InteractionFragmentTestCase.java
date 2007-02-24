package org.netbeans.modules.uml.core.metamodel.dynamics;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for InteractionFragment.
 */
public class InteractionFragmentTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(InteractionFragmentTestCase.class);
    }

    private IInteractionFragment frag;
    private IInteraction inter;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        frag = createType("AtomicFragment");
        
        inter = createType("Interaction");
        inter.addElement(frag);
    }
    
    public void testAddCoveredLifeline()
    {
        ILifeline life = createType("Lifeline");
        frag.addCoveredLifeline(life);
        assertEquals(1, frag.getCoveredLifelines().size());
        assertEquals(life.getXMIID(), frag.getCoveredLifelines().get(0).getXMIID());
    }

    public void testRemoveCoveredLifeline()
    {
        testAddCoveredLifeline();
        frag.removeCoveredLifeline(frag.getCoveredLifelines().get(0));
        assertEquals(0, frag.getCoveredLifelines().size());
    }

    public void testGetCoveredLifelines()
    {
        // Tested by testAddCoveredLifeline.
    }

    public void testGetEnclosingOperand()
    {
        assertEquals(inter.getXMIID(), frag.getEnclosingOperand().getXMIID());
    }

    public void testAddGateConnector()
    {
        IInterGateConnector co = createType("InterGateConnector");
        frag.addGateConnector(co);
        assertEquals(1, frag.getGateConnectors().size());
        assertEquals(co.getXMIID(), frag.getGateConnectors().get(0).getXMIID());
    }

    public void testRemoveGateConnector()
    {
        testAddGateConnector();
        frag.removeGateConnector(frag.getGateConnectors().get(0));
        assertEquals(0, frag.getGateConnectors().size());
    }

    public void testGetGateConnectors()
    {
        // Tested by testAddGateConnector.
    }
}