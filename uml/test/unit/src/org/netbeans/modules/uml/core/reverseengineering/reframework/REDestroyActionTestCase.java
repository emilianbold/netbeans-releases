package org.netbeans.modules.uml.core.reverseengineering.reframework;


/**
 * Test cases for REDestroyAction.
 */
public class REDestroyActionTestCase extends AbstractRETestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(REDestroyActionTestCase.class);
    }

    private REDestroyAction reda;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        reda = new REDestroyAction();
        createBaseElement(reda, "UML:DestroyAction");
        addToken("Name", "InstanceName");
    }

    public void testGetInstanceName()
    {
        assertEquals("InstanceName", reda.getInstanceName());
    }
}
