package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IProtocolStateMachine;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * Test cases for Interface.
 */
public class InterfaceTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(InterfaceTestCase.class);
    }

    private IInterface intf;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        intf = factory.createInterface(null);
        intf.setName("Igloo");
        project.addOwnedElement(intf);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        intf.delete();
    }
    
    public void testSetProtocolStateMachine()
    {
        IProtocolStateMachine psm = (IProtocolStateMachine)FactoryRetriever.instance().createType("ProtocolStateMachine", null);
        //psm.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(psm);
        
        intf.setProtocolStateMachine(psm);
        assertEquals(psm.getXMIID(), intf.getProtocolStateMachine().getXMIID());
    }
    
    public void testGetProtocolStateMachine()
    {
        // Tested by setProtocolStateMachine.
    }

    public void testAddReception()
    {
        IReception rec = factory.createReception(null);
        project.addElement(rec);
        intf.addReception(rec);
        
        ETList<IReception> recs = intf.getReceptions();
        assertEquals(1, recs.size());
        assertEquals(rec.getXMIID(), recs.get(0).getXMIID());
    }
    
    public void testRemoveReception()
    {
        testAddReception();
        intf.removeReception(intf.getReceptions().get(0));
        assertEquals(0, intf.getReceptions().size());
    }
    
    public void testGetReceptions()
    {
        // Tested by testAddReception and testRemoveReception
    }
}