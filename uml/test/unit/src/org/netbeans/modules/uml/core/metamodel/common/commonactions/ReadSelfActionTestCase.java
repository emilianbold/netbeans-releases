package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IOutputPin;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;

/**
 * Test cases for ReadSelfAction.
 */
public class ReadSelfActionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ReadSelfActionTestCase.class);
    }

    private IReadSelfAction act;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        act = (IReadSelfAction)FactoryRetriever.instance().createType("ReadSelfAction", null);
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
}