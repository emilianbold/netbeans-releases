package org.netbeans.modules.uml.core.metamodel.common.commonactions;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;

/**
 * Test cases for VariableAction.
 */
public class VariableActionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(VariableActionTestCase.class);
    }

    private IVariableAction act;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        act = (IVariableAction)FactoryRetriever.instance().createType("ClearVariableAction", null);
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

    
    public void testSetVariable()
    {
        IVariable v = (IVariable)FactoryRetriever.instance().createType("Variable", null);
        //v.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(v);
        act.setVariable(v);
        assertEquals(v.getXMIID(), act.getVariable().getXMIID());
    }

    public void testGetVariable()
    {
        // Tested by testSetVariable.
    }
}