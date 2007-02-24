package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.dom4j.DocumentFactory;

/**
 * Test cases for ActionEvent.
 */
public class ActionEventTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ActionEventTestCase.class);
    }

    private IActionEvent ae;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        ae = new ActionEvent();
        ae.setEventData(
                DocumentFactory.getInstance().createElement("UML:CallAction"));
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testGetAction()
    {
        assertTrue( ae.getAction() instanceof IRECallAction );
    }
}