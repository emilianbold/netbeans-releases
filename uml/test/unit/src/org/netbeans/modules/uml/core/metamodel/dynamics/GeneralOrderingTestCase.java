package org.netbeans.modules.uml.core.metamodel.dynamics;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for GeneralOrdering.
 */
public class GeneralOrderingTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(GeneralOrderingTestCase.class);
    }

    private IGeneralOrdering ord;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        ord = createType("GeneralOrdering");
    }
    
    public void testSetAfter()
    {
        IEventOccurrence eo = createType("EventOccurrence");
        ord.setAfter(eo);
        assertEquals(eo.getXMIID(), ord.getAfter().getXMIID());
    }

    public void testGetAfter()
    {
        // Tested by testSetAfter.
    }

    public void testSetBefore()
    {
        IEventOccurrence eo = createType("EventOccurrence");
        ord.setBefore(eo);
        assertEquals(eo.getXMIID(), ord.getBefore().getXMIID());
    }

    public void testGetBefore()
    {
        // Tested by testSetBefore.
    }
}