package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;


/**
 * Test cases for SynchronizedAction.
 */
public class SynchronizedActionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(SynchronizedActionTestCase.class);
    }

    private ISynchronizedAction act;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        //act = (ISynchronizedAction)FactoryRetriever.instance().createType("SynchronizedAction", null);
        //act.prepareNode(DocumentFactory.getInstance().createElement(""));
        //project.addElement(act);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        //act.delete();
    }

    public void testAddSubAction()
    {
//        IAction action = (IAction)FactoryRetriever.instance().createType("CreateAction", null);
//        //action.prepareNode(DocumentFactory.getInstance().createElement(""));
//        project.addElement(action);
//        act.addSubAction(action);
//        assertEquals(1, act.getSubActions().size());
//        assertEquals(action.getXMIID(), act.getSubActions().get(0).getXMIID());
    }

    public void testRemoveSubAction()
    {
//        testAddSubAction();
//        act.removeSubAction(act.getSubActions().get(0));
//        assertEquals(0, act.getSubActions().size());
    }

    public void testGetSubActions()
    {
        // Tested by testAddSubAction.
    }
}