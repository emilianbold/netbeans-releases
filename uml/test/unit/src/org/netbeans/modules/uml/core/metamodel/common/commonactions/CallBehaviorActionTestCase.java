package org.netbeans.modules.uml.core.metamodel.common.commonactions;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
/**
 * Test cases for CallBehaviorAction.
 */
public class CallBehaviorActionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(CallBehaviorActionTestCase.class);
    }

    private ICallBehaviorAction act;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        act = (ICallBehaviorAction)FactoryRetriever.instance().createType("CallBehaviorAction", null);
        //act.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(act);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        act.delete();
    }

    
    public void testSetIsSynchronous()
    {
        act.setIsSynchronous(true);
        assertTrue(act.getIsSynchronous());
        act.setIsSynchronous(false);
        assertFalse(act.getIsSynchronous());
    }

    public void testGetIsSynchronous()
    {
        // Tested by testSetIsSynchronous.
    }
}