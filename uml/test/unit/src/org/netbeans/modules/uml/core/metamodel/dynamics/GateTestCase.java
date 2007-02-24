package org.netbeans.modules.uml.core.metamodel.dynamics;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
/**
 * Test cases for Gate.
 */
public class GateTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(GateTestCase.class);
    }

    private IGate gate;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        gate = new TypedFactoryRetriever<IGate>()
                            .createType("Gate");
        project.addElement(gate);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        gate.delete();
    }

    public void testSetFromConnector()
    {
        IInterGateConnector gc = (IInterGateConnector)FactoryRetriever.instance().createType("InterGateConnector", null);
        //gc.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(gc);
        gate.setFromConnector(gc);
        assertEquals(gc.getXMIID(), gate.getFromConnector().getXMIID());
    }

    public void testGetFromConnector()
    {
        // Tested by testSetFromConnector.
    }

    public void testSetInteraction()
    {
        IInteraction inter = new TypedFactoryRetriever<IInteraction>()
                                .createType("Interaction");
        project.addElement(inter);
        gate.setInteraction(inter);
        assertEquals(inter.getXMIID(), gate.getInteraction().getXMIID());
    }

    public void testGetInteraction()
    {
        // Tested by testSetInteraction.
    }

    public void testSetToConnector()
    {
        IInterGateConnector gc = (IInterGateConnector)FactoryRetriever.instance().createType("InterGateConnector", null);
        //gc.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(gc);
        gate.setToConnector(gc);
        assertEquals(gc.getXMIID(), gate.getToConnector().getXMIID());
    }

    public void testGetToConnector()
    {
        // Tested by testSetToConnector.
    }
}