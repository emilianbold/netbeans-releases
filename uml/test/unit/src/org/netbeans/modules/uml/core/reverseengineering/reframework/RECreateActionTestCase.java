package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;

/**
 * Test cases for RECreateAction.
 */
public class RECreateActionTestCase extends AbstractRETestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(RECreateActionTestCase.class);
    }

    private RECreateAction reca;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        reca = new RECreateAction();
        createBaseElement(reca, "UML:CreateAction");
        addToken("Name", "Instance");
        attr("instantiation", "nil");
    }

    public void testGetInstanceName()
    {
        assertEquals("Instance", reca.getInstanceName());
    }

    public void testGetInstantiation()
    {
        assertEquals("nil", reca.getInstantiation());
    }
}