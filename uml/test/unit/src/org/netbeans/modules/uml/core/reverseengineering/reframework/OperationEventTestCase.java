package org.netbeans.modules.uml.core.reverseengineering.reframework;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;

/**
 * Test cases for OperationEvent.
 */
public class OperationEventTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(OperationEventTestCase.class);
    }

    private OperationEvent oe;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        oe = new OperationEvent();
        IOperation op = createType("Operation");
        op.setName("Aegon");
        oe.setEventData(op.getNode());
    }

    public void testGetREOperation()
    {
        assertEquals("Aegon", oe.getREOperation().getName());
    }
}
