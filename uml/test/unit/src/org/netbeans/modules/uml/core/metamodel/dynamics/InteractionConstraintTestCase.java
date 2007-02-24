package org.netbeans.modules.uml.core.metamodel.dynamics;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for InteractionConstraint.
 */
public class InteractionConstraintTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(InteractionConstraintTestCase.class);
    }

    private IInteractionConstraint ic;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        ic = createType("InteractionConstraint");
    }
    
    public void testSetMaxInt()
    {
        IExpression ex = createType("Expression");
        ic.setMaxInt(ex);
        assertEquals(ex.getXMIID(), ic.getMaxInt().getXMIID());
    }

    public void testGetMaxInt()
    {
        // Tested by testSetMaxInt.
    }

    public void testSetMinInt()
    {
        IExpression ex = createType("Expression");
        ic.setMinInt(ex);
        assertEquals(ex.getXMIID(), ic.getMinInt().getXMIID());
    }

    public void testGetMinInt()
    {
        // Tested by testSetMinInt.
    }
}