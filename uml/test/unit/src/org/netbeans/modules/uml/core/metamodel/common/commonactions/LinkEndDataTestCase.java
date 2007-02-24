package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IInputPin;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
/**
 * Test cases for LinkEndData.
 */
public class LinkEndDataTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(LinkEndDataTestCase.class);
    }

    private ILinkEndData data;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        data = (ILinkEndData)FactoryRetriever.instance().createType("LinkEndData", null);
        //data.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(data);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        data.delete();
    }

    
    public void testSetEnd()
    {
        IAssociationEnd end = factory.createAssociationEnd(null);
        project.addElement(end);
        data.setEnd(end);
        assertEquals(end.getXMIID(), data.getEnd().getXMIID());
    }

    public void testGetEnd()
    {
        // Tested by testSetEnd.
    }

    public void testSetValue()
    {
        IInputPin pin = (IInputPin)FactoryRetriever.instance().createType("InputPin", null);
        //pin.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(pin);
        data.setValue(pin);
        assertEquals(pin.getXMIID(), data.getValue().getXMIID());
    }

    public void testGetValue()
    {
        // Tested by testSetValue.
    }
}