package org.netbeans.modules.uml.core.metamodel.common.commonactivities;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ISignal;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for SignalNode.
 */
public class SignalNodeTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(SignalNodeTestCase.class);
    }

    private ISignalNode node;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        node = (ISignalNode)FactoryRetriever.instance().createType("SignalNode", null);
        //node.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(node);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        node.delete();
    }

    
    public void testSetSignal()
    {
        ISignal sig = factory.createSignal(null);
        project.addElement(sig);
        node.setSignal(sig);
        assertEquals(sig.getXMIID(), node.getSignal().getXMIID());
    }

    public void testGetSignal()
    {
        // Tested by testSetSignal.
    }
}