package org.netbeans.modules.uml.core.metamodel.dynamics;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
/**
 * Test cases for ActionOccurrence.
 */
public class ActionOccurrenceTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ActionOccurrenceTestCase.class);
    }

    private IActionOccurrence actionOccurrence;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        actionOccurrence = new TypedFactoryRetriever<IActionOccurrence>()
                            .createType("ActionOccurrence");
        project.addElement(actionOccurrence);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        actionOccurrence.delete();
    }

    public void testSetAction()
    {
        IAction act = new TypedFactoryRetriever<IAction>()
                            .createType("ConditionalAction");
        project.addElement(act);
        actionOccurrence.setAction(act);
        
        assertEquals(act.getXMIID(), actionOccurrence.getAction().getXMIID());
    }

    public void testGetAction()
    {
        // Tested by testSetAction.
    }

    public void testSetContainingExecOccurrence()
    {
        IExecutionOccurrence exec = 
            new TypedFactoryRetriever<IExecutionOccurrence>()
                .createType("ActionOccurrence");
        project.addElement(exec);
        actionOccurrence.setContainingExecOccurrence(exec);
        
        assertEquals(exec.getXMIID(), 
                actionOccurrence.getContainingExecOccurrence().getXMIID());
    }

    public void testGetContainingExecOccurrence()
    {
        // Tested by testSetContainingExecOccurrence.
    }
}