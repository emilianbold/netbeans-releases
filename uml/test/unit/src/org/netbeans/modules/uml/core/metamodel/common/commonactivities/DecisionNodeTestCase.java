package org.netbeans.modules.uml.core.metamodel.common.commonactivities;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for DecisionNode.
 */
public class DecisionNodeTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(DecisionNodeTestCase.class);
    }

    private IDecisionNode node;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        node = (IDecisionNode)FactoryRetriever.instance().createType("DecisionNode", null);
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

    
    public void testSetDecisionInput()
    {
        IBehavior b = factory.createActivity(null);
        project.addElement(b);
        node.setDecisionInput(b);
        assertEquals(b.getXMIID(), node.getDecisionInput().getXMIID());
    }

    public void testGetDecisionInput()
    {
        // Tested by testSetDecisionInput.
    }
}