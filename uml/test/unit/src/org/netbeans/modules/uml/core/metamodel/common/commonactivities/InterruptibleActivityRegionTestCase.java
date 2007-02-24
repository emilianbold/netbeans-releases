package org.netbeans.modules.uml.core.metamodel.common.commonactivities;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for InterruptibleActivityRegion.
 */
public class InterruptibleActivityRegionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(InterruptibleActivityRegionTestCase.class);
    }

    private IInterruptibleActivityRegion reg;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        reg = (IInterruptibleActivityRegion)FactoryRetriever.instance().createType("InterruptibleActivityRegion" ,null);
        //reg.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(reg);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        reg.delete();
    }

    
    public void testAddInterruptingEdge()
    {
        IActivityEdge edge = (IActivityEdge)FactoryRetriever.instance().createType("ControlFlow", null);
        //edge.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(edge);
        reg.addInterruptingEdge(edge);
        
        assertEquals(1, reg.getInterruptingEdges().size());
        assertEquals(edge.getXMIID(), reg.getInterruptingEdges().get(0).getXMIID());
    }

    public void testRemoveInterruptingEdge()
    {
        testAddInterruptingEdge();
        reg.removeInterruptingEdge(reg.getInterruptingEdges().get(0));
        assertEquals(0, reg.getInterruptingEdges().size());
    }

    public void testGetInterruptingEdges()
    {
        // Tested by testAddInterruptingEdge.
    }
}