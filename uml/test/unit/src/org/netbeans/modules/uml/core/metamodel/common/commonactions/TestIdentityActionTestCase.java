package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IInputPin;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IOutputPin;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;

/**
 * Test cases for TestIdentityAction.
 */
public class TestIdentityActionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(TestIdentityActionTestCase.class);
    }

    private ITestIdentityAction act;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        act = (ITestIdentityAction)FactoryRetriever.instance().createType("TestIdentityAction", null);
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

    
    public void testSetFirst()
    {
        IInputPin pin = (IInputPin)FactoryRetriever.instance().createType("InputPin", null);
        //pin.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(pin);
        act.setFirst(pin);
        assertEquals(pin.getXMIID(), act.getFirst().getXMIID());
    }

    public void testGetFirst()
    {
        // Tested by testSetFirst.
    }

    public void testSetResult()
    {
        IOutputPin pin = (IOutputPin)FactoryRetriever.instance().createType("OutputPin", null);
        //pin.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(pin);
        act.setResult(pin);
        assertEquals(pin.getXMIID(), act.getResult().getXMIID());
    }

    public void testGetResult()
    {
        // Tested by testSetResult.
    }

    public void testSetSecond()
    {
        IInputPin pin = (IInputPin)FactoryRetriever.instance().createType("InputPin", null);
        //pin.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(pin);
        act.setSecond(pin);
        assertEquals(pin.getXMIID(), act.getSecond().getXMIID());
    }

    public void testGetSecond()
    {
        // Tested by testSetSecond.
    }
}