package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IInputPin;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;

/**
 * Test cases for WriteAttributeAction.
 */
public class WriteAttributeActionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(WriteAttributeActionTestCase.class);
    }

    private IWriteAttributeAction act;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        act = (IWriteAttributeAction)FactoryRetriever.instance().createType("AddAttributeValueAction", null);
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

    
    public void testSetValue()
    {
        IInputPin pin = (IInputPin)FactoryRetriever.instance().createType("InputPin", null);
        //pin.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(pin);
        act.setValue(pin);
        assertEquals(pin.getXMIID(), act.getValue().getXMIID());
    }

    public void testGetValue()
    {
        // Tested by testSetValue.
    }
}