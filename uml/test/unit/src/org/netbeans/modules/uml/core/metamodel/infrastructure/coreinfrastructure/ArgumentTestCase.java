package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;

import junit.textui.TestRunner;
/**
 * Test cases for argument.
 *
 */
public class ArgumentTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        TestRunner.run(ArgumentTestCase.class);
    }

    private IArgument arg;
        
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        arg = factory.createArgument(createClass("Cassini"));
        project.addElement(arg);
    }
    
    public void testSetValue()
    {
        IExpression expr = factory.createExpression(project);
        arg.setValue(expr);
        assertNotNull(arg.getValue());
        assertEquals(expr.getXMIID(), arg.getValue().getXMIID());
    }
    
    public void testGetValue()
    {
        // Tested by testSetValue
    }
}
