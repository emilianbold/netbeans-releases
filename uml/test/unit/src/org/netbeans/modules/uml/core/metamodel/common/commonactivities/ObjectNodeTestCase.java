package org.netbeans.modules.uml.core.metamodel.common.commonactivities;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for ObjectNode.
 */
public class ObjectNodeTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ObjectNodeTestCase.class);
    }

    private IObjectNode node;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        node = (IObjectNode)FactoryRetriever.instance().createType("SignalNode", null);
        //node.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(node);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        node.delete();
    }

    
    public void testAddInState()
    {
        IState state = factory.createState(null);
        project.addElement(state);
        node.addInState(state);
        assertEquals(1, node.getInStates().size());
        assertEquals(state.getXMIID(), node.getInStates().get(0).getXMIID());
    }

    public void testRemoveInState()
    {
        testAddInState();
        node.removeInState(node.getInStates().get(0));
        assertEquals(0, node.getInStates().size());
    }
    
    public void testGetInStates()
    {
        // Tested by testAddInState.
    }
    

    public void testSetOrdering()
    {
        node.setOrdering(BaseElement.OOK_FIFO);
        assertEquals(BaseElement.OOK_FIFO, node.getOrdering());
        node.setOrdering(BaseElement.OOK_LIFO);
        assertEquals(BaseElement.OOK_LIFO, node.getOrdering());
        node.setOrdering(BaseElement.OOK_ORDERED);
        assertEquals(BaseElement.OOK_ORDERED, node.getOrdering());
        node.setOrdering(BaseElement.OOK_UNORDERED);
        assertEquals(BaseElement.OOK_UNORDERED, node.getOrdering());
    }

    public void testGetOrdering()
    {
        // Tested by testSetOrdering.
    }

    public void testSetSelection()
    {
        IBehavior b = factory.createActivity(null);
        project.addElement(b);
        node.setSelection(b);
        assertEquals(b.getXMIID(), node.getSelection().getXMIID());
    }

    public void testGetSelection()
    {
        // Tested by testSetSelection.
    }

    public void testSetUpperBound()
    {
        IValueSpecification spec = factory.createExpression(null);
        project.addElement(spec);
        node.setUpperBound(spec);
        assertEquals(spec.getXMIID(), node.getUpperBound().getXMIID());
    }

    public void testGetUpperBound()
    {
        // Tested by testSetUpperBound.
    }
}