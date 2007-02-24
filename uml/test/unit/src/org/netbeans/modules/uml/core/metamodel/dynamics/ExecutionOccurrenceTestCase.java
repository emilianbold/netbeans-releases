package org.netbeans.modules.uml.core.metamodel.dynamics;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
/**
 * Test cases for ExecutionOccurrence.
 */
public class ExecutionOccurrenceTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ExecutionOccurrenceTestCase.class);
    }

    private IExecutionOccurrence executionOccurrence;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        executionOccurrence = new TypedFactoryRetriever<IExecutionOccurrence>()
                            .createType("ActionOccurrence");
        project.addElement(executionOccurrence);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        executionOccurrence.delete();
    }

    public void testSetFinish()
    {
        IEventOccurrence occ = (IEventOccurrence)FactoryRetriever.instance().createType("EventOccurrence", null);
        //occ.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(occ);
        executionOccurrence.setFinish(occ);
        assertEquals(occ.getXMIID(), executionOccurrence.getFinish().getXMIID());
    }

    public void testGetFinish()
    {
        // Tested by testSetFinish.
    }

    public void testSetStart()
    {
        IEventOccurrence occ = (IEventOccurrence)FactoryRetriever.instance().createType("EventOccurrence", null);
        //occ.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(occ);
        executionOccurrence.setStart(occ);
        assertEquals(occ.getXMIID(), executionOccurrence.getStart().getXMIID());
    }

    public void testGetStart()
    {
        // Tested by testSetStart.
    }
}