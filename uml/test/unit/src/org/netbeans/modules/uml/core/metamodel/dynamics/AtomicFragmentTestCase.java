package org.netbeans.modules.uml.core.metamodel.dynamics;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;

/**
 * Test cases for AtomicFragment.
 */
public class AtomicFragmentTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(AtomicFragmentTestCase.class);
    }

    private IAtomicFragment atomicFragment;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        atomicFragment = new TypedFactoryRetriever<IAtomicFragment>()
                            .createType("AtomicFragment");
        project.addElement(atomicFragment);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        atomicFragment.delete();
    }

    public void testSetEvent()
    {
        IEventOccurrence ev = (IEventOccurrence)FactoryRetriever.instance().createType("EventOccurrence", null);
        //ev.prepareNode(DOMDocumentFactory.getInstance().createElement(""));
        project.addElement(ev);
        atomicFragment.setEvent(ev);
        assertEquals(ev.getXMIID(), atomicFragment.getEvent().getXMIID());
        ev.delete();
    }

    public void testGetEvent()
    {
        // Tested by testSetEvent.
    }

    public void testSetImplicitGate()
    {
        IGate g = (IGate)FactoryRetriever.instance().createType("Gate", null);
        //g.prepareNode(DocumentFactory.getInstance().createElement(""));
        atomicFragment.setImplicitGate(g);
        assertEquals(g.getXMIID(), atomicFragment.getImplicitGate().getXMIID());
    }

    public void testGetImplicitGate()
    {
        // Tested by testSetImplicitGate.
    }
}