package org.netbeans.modules.uml.core.metamodel.common.commonactivities;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for JoinNode.
 */
public class JoinNodeTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(JoinNodeTestCase.class);
    }

    private IJoinNode node;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        node = (IJoinNode)FactoryRetriever.instance().createType("JoinNode", null);
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

    
    public void testSetJoinSpec()
    {
        IValueSpecification spec = factory.createExpression(null);
        project.addElement(spec);
        node.setJoinSpec(spec);
        assertEquals(spec.getXMIID(), node.getJoinSpec().getXMIID());
    }

    public void testGetJoinSpec()
    {
        // Tested by testSetJoinSpec.
    }
}