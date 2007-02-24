package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
/**
 * Test cases for GroupAction.
 */
public class GroupActionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(GroupActionTestCase.class);
    }

    private IGroupAction act;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        act = (IGroupAction)FactoryRetriever.instance().createType("GroupAction", null);
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

    public void testAddSubAction()
    {
        IAction action = (IAction)FactoryRetriever.instance().createType("CreateAction", null);
        //action.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(action);
        act.addSubAction(action);
        assertEquals(1, act.getSubActions().size());
        assertEquals(action.getXMIID(), act.getSubActions().get(0).getXMIID());
    }

    public void testRemoveSubAction()
    {
        testAddSubAction();
        act.removeSubAction(act.getSubActions().get(0));
        assertEquals(0, act.getSubActions().size());
    }

    public void testGetSubActions()
    {
        // Tested by testAddSubAction.
    }

    public void testAddVariable()
    {
        IVariable var = (IVariable)FactoryRetriever.instance().createType("Variable", null);
        //var.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(var);
        act.addVariable(var);
        assertEquals(1, act.getVariables().size());
        assertEquals(var.getXMIID(), act.getVariables().get(0).getXMIID());
    }

    public void testRemoveVariable()
    {
        testAddVariable();
        act.removeVariable(act.getVariables().get(0));
        assertEquals(0, act.getVariables().size());
    }

    public void testGetVariables()
    {
        // Tested by testAddVariable.
    }
}