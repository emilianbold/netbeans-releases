package org.netbeans.modules.uml.core.metamodel.common.commonactions;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
/**
 * Test cases for ConditionalAction.
 */
public class ConditionalActionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ConditionalActionTestCase.class);
    }

    private IConditionalAction act;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        act = (IConditionalAction)FactoryRetriever.instance().createType("ConditionalAction", null);
        //act.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(act);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        act.delete();
    }

    
    public void testAddClause()
    {
        IClause clause = (IClause)FactoryRetriever.instance().createType("Clause", null);
        //clause.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(clause);
        act.addClause(clause);
        assertEquals(1, act.getClauses().size());
        assertEquals(clause.getXMIID(), act.getClauses().get(0).getXMIID());
    }

    public void testRemoveClause()
    {
        testAddClause();
        act.removeClause(act.getClauses().get(0));
        assertEquals(0, act.getClauses().size());
    }

    public void testGetClauses()
    {
        // Tested by testAddClause.
    }

    public void testSetIsAssertion()
    {
        act.setIsAssertion(true);
        assertTrue(act.getIsAssertion());
        act.setIsAssertion(false);
        assertFalse(act.getIsAssertion());
    }

    public void testGetIsAssertion()
    {
        // Tested by testSetIsAssertion.
    }

    public void testSetIsDeterminate()
    {
        act.setIsDeterminate(true);
        assertTrue(act.getIsDeterminate());
        act.setIsDeterminate(false);
        assertFalse(act.getIsDeterminate());
    }

    public void testGetIsDeterminate()
    {
        // Tested by testSetIsDeterminate.
    }
}