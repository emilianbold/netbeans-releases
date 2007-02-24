package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IInputPin;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
/**
 * Test cases for LinkEndCreationData.
 */
public class LinkEndCreationDataTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(LinkEndCreationDataTestCase.class);
    }

    private ILinkEndCreationData data;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        data = (ILinkEndCreationData)FactoryRetriever.instance().createType("LinkEndCreationData", null);
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

    
    public void testSetInsertAt()
    {
        IInputPin pin = (IInputPin)FactoryRetriever.instance().createType("InputPin", null);
        //pin.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(pin);
        data.setInsertAt(pin);
        
        assertEquals(pin.getXMIID(), data.getInsertAt().getXMIID());
    }

    public void testGetInsertAt()
    {
        // Tested by testSetInsertAt.
    }

    public void testSetIsReplaceAll()
    {
        data.setIsReplaceAll(true);
        assertTrue(data.getIsReplaceAll());
        data.setIsReplaceAll(false);
        assertFalse(data.getIsReplaceAll());
    }

    public void testGetIsReplaceAll()
    {
        // Tested by testSetIsReplaceAll.
    }
}