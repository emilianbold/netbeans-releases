package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;

/**
 * Test cases for LoopAction.
 */
public class LoopActionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(LoopActionTestCase.class);
    }

    private ILoopAction action;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        action = (ILoopAction)FactoryRetriever.instance().createType("LoopAction", null);
        //action.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(action);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        action.delete();
    }

    public void testAddToBody()
    {
        IAction act = (IAction)FactoryRetriever.instance().createType("CreateAction", null);
        //act.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(act);
        action.addToBody(act);
        assertEquals(1, action.getBody().size());
        assertEquals(act.getXMIID(), action.getBody().get(0).getXMIID());
    }

    public void testRemoveFromBody()
    {
        testAddToBody();
        action.removeFromBody(action.getBody().get(0));
        assertEquals(0, action.getBody().size());
    }

    public void testGetBody()
    {
        // Tested by testAddToBody
    }

    public void testAddToTest()
    {
        IAction act = (IAction)FactoryRetriever.instance().createType("CreateAction", null);
        //act.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(act);
        
        action.addToTest(act);
        assertEquals(1, action.getTest().size());
        assertEquals(act.getXMIID(), action.getTest().get(0).getXMIID());
    }
    
    public void testRemoveFromTest()
    {
        testAddToTest();
        action.removeFromTest(action.getTest().get(0));
        assertEquals(0, action.getTest().size());
    }

    public void testGetTest()
    {
        // Tested by testAddToTest
    }

    public void testSetIsTestedFirst()
    {
        action.setIsTestedFirst(true);
        assertTrue(action.getIsTestedFirst());
        action.setIsTestedFirst(false);
        assertFalse(action.getIsTestedFirst());
    }

    public void testGetIsTestedFirst()
    {
        // Tested by testSetIsTestedFirst.
    }

    public void testSetTestOutput()
    {
        IExpression expr = factory.createExpression(null);
        project.addElement(expr);
        action.setTestOutput(expr);
        assertEquals(expr.getXMIID(), action.getTestOutput().getXMIID());
    }

    public void testGetTestOutput()
    {
        // Tested by testSetTestOutput.
    }
}