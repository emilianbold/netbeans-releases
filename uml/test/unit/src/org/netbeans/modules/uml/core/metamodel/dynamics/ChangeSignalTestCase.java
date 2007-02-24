package org.netbeans.modules.uml.core.metamodel.dynamics;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
/**
 * Test cases for ChangeSignal.
 */
public class ChangeSignalTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ChangeSignalTestCase.class);
    }

    private IChangeSignal changeSignal;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        changeSignal = new TypedFactoryRetriever<IChangeSignal>()
                            .createType("ChangeSignal");
        project.addElement(changeSignal);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        changeSignal.delete();
    }

    public void testSetChangeExpression()
    {
        IExpression expr = factory.createExpression(null);
        project.addElement(expr);
        changeSignal.setChangeExpression(expr);
        assertEquals(expr.getXMIID(), 
                changeSignal.getChangeExpression().getXMIID());
    }

    public void testGetChangeExpression()
    {
        // Tested by testSetChangeExpression.
    }
}