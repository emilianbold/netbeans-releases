package org.netbeans.modules.uml.core.metamodel.dynamics;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for ProcedureOccurrence.
 */
public class ProcedureOccurrenceTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ProcedureOccurrenceTestCase.class);
    }

    private IProcedureOccurrence po;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        po = createType("ProcedureOccurrence");
    }

    public void testSetOperation()
    {
        IOperation op = createType("Operation");
        po.setOperation(op);
        assertEquals(op.getXMIID(), po.getOperation().getXMIID());
    }

    public void testGetOperation()
    {
        // Tested by testSetOperation.
    }
}