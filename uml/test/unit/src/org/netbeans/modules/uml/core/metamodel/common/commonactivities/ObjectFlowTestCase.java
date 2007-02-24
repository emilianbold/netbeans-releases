package org.netbeans.modules.uml.core.metamodel.common.commonactivities;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for ObjectFlow.
 */
public class ObjectFlowTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ObjectFlowTestCase.class);
    }

    private IObjectFlow flow;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        flow = (IObjectFlow)FactoryRetriever.instance().createType("ObjectFlow", null);
        //flow.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(flow);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        flow.delete();
    }

    
    public void testSetEffect()
    {
        // Try it in sequence
        flow.setEffect(BaseElement.OFE_CREATE);
        assertEquals(BaseElement.OFE_CREATE, flow.getEffect());
        flow.setEffect(BaseElement.OFE_DELETE);
        assertEquals(BaseElement.OFE_DELETE, flow.getEffect());
        flow.setEffect(BaseElement.OFE_READ);
        assertEquals(BaseElement.OFE_READ, flow.getEffect());
        flow.setEffect(BaseElement.OFE_UPDATE);
        assertEquals(BaseElement.OFE_UPDATE, flow.getEffect());

        // Now in reverse        
        flow.setEffect(BaseElement.OFE_UPDATE);
        assertEquals(BaseElement.OFE_UPDATE, flow.getEffect());
        flow.setEffect(BaseElement.OFE_READ);
        assertEquals(BaseElement.OFE_READ, flow.getEffect());
        flow.setEffect(BaseElement.OFE_DELETE);
        assertEquals(BaseElement.OFE_DELETE, flow.getEffect());
        flow.setEffect(BaseElement.OFE_CREATE);
        assertEquals(BaseElement.OFE_CREATE, flow.getEffect());
    }

    public void testGetEffect()
    {
        // Tested by testSetEffect.
    }

    public void testSetIsMultiReceive()
    {
        flow.setIsMultiReceive(true);
        assertTrue(flow.getIsMultiReceive());
        flow.setIsMultiReceive(false);
        assertFalse(flow.getIsMultiReceive());
    }

    public void testGetIsMultiReceive()
    {
        // Tested by testSetIsMultiReceive.
    }

    public void testSetIsMulticast()
    {
        flow.setIsMulticast(true);
        assertTrue(flow.getIsMulticast());
        flow.setIsMulticast(false);
        assertFalse(flow.getIsMulticast());
    }

    public void testGetIsMulticast()
    {
        // Tested by testSetIsMulticast.
    }

    public void testSetSelection()
    {
        IBehavior b = factory.createActivity(null);
        project.addElement(b);
        flow.setSelection(b);
        assertEquals(b.getXMIID(), flow.getSelection().getXMIID());
    }

    public void testGetSelection()
    {
        // Tested by testSetSelection.
    }

    public void testSetTransformation()
    {
        IBehavior b = factory.createActivity(null);
        project.addElement(b);
        flow.setTransformation(b);
        assertEquals(b.getXMIID(), flow.getTransformation().getXMIID());
    }

    public void testGetTransformation()
    {
        // Tested by testSetTransformation.
    }
}