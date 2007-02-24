package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IInputPin;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
/**
 * Test cases for DestroyObjectAction.
 */
public class DestroyObjectActionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(DestroyObjectActionTestCase.class);
    }

    private IDestroyObjectAction act;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        act = (IDestroyObjectAction)FactoryRetriever.instance().createType("DestroyObjectAction", null);
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

    
    public void testSetInput()
    {
        IInputPin pin = (IInputPin)FactoryRetriever.instance().createType("InputPin", null);
        //pin.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(pin);
        act.setInput(pin);
        assertEquals(pin.getXMIID(), act.getInput().getXMIID());
    }

    public void testGetInput()
    {
        // Tested by testSetInput.
    }
}