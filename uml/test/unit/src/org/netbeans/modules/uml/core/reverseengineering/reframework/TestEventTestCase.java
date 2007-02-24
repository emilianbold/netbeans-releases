package org.netbeans.modules.uml.core.reverseengineering.reframework;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction;
import org.netbeans.modules.uml.core.metamodel.common.commonactions.IClause;
import org.netbeans.modules.uml.core.metamodel.common.commonactions.IConditionalAction;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;

/**
 * Test cases for TestEvent.
 */
public class TestEventTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(TestEventTestCase.class);
    }

    private TestEvent te;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        te = new TestEvent();
        
        IConditionalAction act = createType("ConditionalAction");
        IClause cl = createType("Clause");
        
        IAction da = createType("DestroyObjectAction");
        da.getElementNode().addAttribute("representation", "Ajax");
        cl.addToTest(da);
        
        act.addClause(cl);
        
        te.setEventData(act.getNode());
    }
    
    public void testGetStringRepresentation()
    {
        assertNotNull( te.getStringRepresentation() );
    }
}
