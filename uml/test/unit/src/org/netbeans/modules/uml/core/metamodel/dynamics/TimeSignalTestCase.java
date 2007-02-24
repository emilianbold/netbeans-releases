package org.netbeans.modules.uml.core.metamodel.dynamics;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for TimeSignal.
 */
public class TimeSignalTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(TimeSignalTestCase.class);
    }

    private ITimeSignal ts;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        ts = createType("TimeSignal");
    }
    
    public void testSetTimeExpression()
    {
        IExpression e = createType("Expression");
        ts.setTimeExpression(e);
        assertEquals(e.getXMIID(), ts.getTimeExpression().getXMIID());
    }

    public void testGetTimeExpression()
    {
        // Tested by testSetTimeExpression.
    }
}