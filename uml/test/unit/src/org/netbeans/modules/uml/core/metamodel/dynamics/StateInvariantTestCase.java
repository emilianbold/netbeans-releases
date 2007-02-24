package org.netbeans.modules.uml.core.metamodel.dynamics;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for StateInvariant.
 */
public class StateInvariantTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(StateInvariantTestCase.class);
    }

    private IStateInvariant si;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        si = createType("StateInvariant");
    }
    
    public void testSetInvariant()
    {
        IConstraint c = createType("Constraint");
        si.setInvariant(c);
        assertEquals(c.getXMIID(), si.getInvariant().getXMIID());
    }

    public void testGetInvariant()
    {
        // Tested by testSetInvariant.
    }
}
