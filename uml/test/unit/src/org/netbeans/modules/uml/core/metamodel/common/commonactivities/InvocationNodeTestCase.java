package org.netbeans.modules.uml.core.metamodel.common.commonactivities;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for InvocationNode.
 */
public class InvocationNodeTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(InvocationNodeTestCase.class);
    }

    private IInvocationNode node;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        node = (IInvocationNode)FactoryRetriever.instance().createType("InvocationNode", null);
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

    
    public void testCreateCondition()
    {
        assertNotNull(node.createCondition("yipe"));
    }

    public void testSetIsMultipleInvocation()
    {
        node.setIsMultipleInvocation(true);
        assertTrue(node.getIsMultipleInvocation());
        node.setIsMultipleInvocation(false);
        assertFalse(node.getIsMultipleInvocation());
    }

    public void testGetIsMultipleInvocation()
    {
        // Tested by testSetIsMultipleInvocation.
    }

    public void testSetIsSynchronous()
    {
        node.setIsSynchronous(true);
        assertTrue(node.getIsSynchronous());
        node.setIsSynchronous(false);
        assertFalse(node.getIsSynchronous());
    }

    public void testGetIsSynchronous()
    {
        // Tested by testSetIsSynchronous.
    }

    public void testAddLocalPostCondition()
    {
        IConstraint cond = node.createCondition("xyzzy");
        node.addLocalPostCondition(cond);
        assertEquals(1, node.getLocalPostConditions().size());
        assertEquals(cond.getXMIID(), node.getLocalPostConditions().get(0).getXMIID());
    }

    public void testRemoveLocalPostcondition()
    {
        testAddLocalPostCondition();
        node.removeLocalPostcondition(node.getLocalPostConditions().get(0));
        assertEquals(0, node.getLocalPostConditions().size());
    }
    
    public void testGetLocalPostConditions()
    {
        // Tested by testAddLocalPostCondition.
    }

    public void testAddLocalPrecondition()
    {
        IConstraint cond = node.createCondition("xyzzy");
        node.addLocalPrecondition(cond);
        assertEquals(1, node.getLocalPreconditions().size());
        assertEquals(cond.getXMIID(), node.getLocalPreconditions().get(0).getXMIID());
    }

    public void testRemoveLocalPrecondition()
    {
        testAddLocalPrecondition();
        node.removeLocalPrecondition(node.getLocalPreconditions().get(0));
        assertEquals(0, node.getLocalPreconditions().size());
    }

    public void testGetLocalPreconditions()
    {
        // Tested by testAddLocalPrecondition.
    }

    public void testSetMultiplicity()
    {
        IMultiplicity mul = factory.createMultiplicity(null);
        project.addElement(mul);
        node.setMultiplicity(mul);
        assertEquals(mul.getXMIID(), node.getMultiplicity().getXMIID());
    }

    public void testGetMultiplicity()
    {
        // Tested by testSetMultiplicity.
    }
}