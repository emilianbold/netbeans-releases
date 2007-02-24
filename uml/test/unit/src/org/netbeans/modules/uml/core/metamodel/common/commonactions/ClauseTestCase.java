package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
/**
 * Test cases for Clause.
 */
public class ClauseTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ClauseTestCase.class);
    }

    private IClause clause;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        clause = (IClause)FactoryRetriever.instance().createType("Clause", null);
        //clause.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(clause);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        clause.delete();
    }

    public void testAddToBody()
    {
        IAction act = (IAction)FactoryRetriever.instance().createType("CreateAction", null);
        //act.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(act);
        clause.addToBody(act);
        assertEquals(1, clause.getBody().size());
        assertEquals(act.getXMIID(), clause.getBody().get(0).getXMIID());
    }

    public void testRemoveFromBody()
    {
        testAddToBody();
        clause.removeFromBody(clause.getBody().get(0));
        assertEquals(0, clause.getBody().size());
    }
    
    public void testGetBody()
    {
        // Tested by testAddToBody
    }

    public void testAddPredecessor()
    {
        IClause pred = (IClause)FactoryRetriever.instance().createType("Clause", null);
        //pred.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(pred);
        clause.addPredecessor(pred);
        
        assertEquals(1, clause.getPredecessors().size());
        assertEquals(pred.getXMIID(), clause.getPredecessors().get(0).getXMIID());
    }

    public void testRemovePredecessor()
    {
        testAddPredecessor();
        clause.removePredecessor(clause.getPredecessors().get(0));
        assertEquals(0, clause.getPredecessors().size());
    }

    public void testGetPredecessors()
    {
        // Tested by testAddPredecessor.
    }

    public void testAddSuccessor()
    {
        IClause succ = (IClause)FactoryRetriever.instance().createType("Clause", null);
        //succ.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(succ);
        
        clause.addSuccessor(succ);
        assertEquals(1, clause.getSuccessors().size());
        assertEquals(succ.getXMIID(), clause.getSuccessors().get(0).getXMIID());
    }

    public void testRemoveSuccessor()
    {
        testAddSuccessor();
        clause.removeSuccessor(clause.getSuccessors().get(0));
        assertEquals(0, clause.getSuccessors().size());
    }

    public void testGetSuccessors()
    {
        // Tested by testAddSuccessor.
    }

    public void testSetTestOutput()
    {
        IValueSpecification spec = factory.createExpression(null);
        project.addElement(spec);
        clause.setTestOutput(spec);
        assertEquals(spec.getXMIID(), clause.getTestOutput().getXMIID());
    }

    public void testGetTestOutput()
    {
        // Tested by testSetTestOutput.
    }

    public void testAddToTest()
    {
        IAction act = (IAction)FactoryRetriever.instance().createType("CreateAction", null);
        //act.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(act);
        
        clause.addToTest(act);
        assertEquals(1, clause.getTest().size());
        assertEquals(act.getXMIID(), clause.getTest().get(0).getXMIID());
    }
    
    public void testRemoveFromTest()
    {
        testAddToTest();
        clause.removeFromTest(clause.getTest().get(0));
        assertEquals(0, clause.getTest().size());
    }
    
    public void testGetTest()
    {
        // Tested by testAddToTest and testRemoveFromTest
    }
}