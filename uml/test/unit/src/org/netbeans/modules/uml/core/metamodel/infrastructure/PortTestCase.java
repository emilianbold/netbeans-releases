package org.netbeans.modules.uml.core.metamodel.infrastructure;

import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IProtocolStateMachine;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;

/**
 * Test cases for Port.
 */
public class PortTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(PortTestCase.class);
    }

    private IPort port;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        port = factory.createPort(null);
        project.addElement(port);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        port.delete();
    }
    
    public void testAddEnd()
    {
        IConnectorEnd end = factory.createConnectorEnd(null);
        project.addElement(end);
        port.addEnd(end);
        assertEquals(1, port.getEnds().size());
        assertEquals(end.getXMIID(), port.getEnds().get(0).getXMIID());
    }

    public void testRemoveEnd()
    {
        testAddEnd();
        port.removeEnd(port.getEnds().get(0));
        assertEquals(0, port.getEnds().size());
    }

    public void testGetEnds()
    {
        // Tested by testAddEnd.
    }

    public void testSetIsService()
    {
        assertFalse(port.getIsService());
        port.setIsService(true);
        assertTrue(port.getIsService());
        port.setIsService(false);
        assertFalse(port.getIsService());
    }

    public void testGetIsService()
    {
        // Tested by testSetIsService.
    }

    public void testSetIsSignal()
    {
        assertFalse(port.getIsSignal());
        port.setIsSignal(true);
        assertTrue(port.getIsSignal());
        port.setIsSignal(false);
        assertFalse(port.getIsSignal());
    }

    public void testGetIsSignal()
    {
        // Tested by testSetIsSignal.
    }

    public void testSetProtocol()
    {
        IProtocolStateMachine psm = factory.createProtocolStateMachine(null);
        project.addElement(psm);
        port.setProtocol(psm);
        assertEquals(psm.getXMIID(), port.getProtocol().getXMIID());
    }

    public void testGetProtocol()
    {
        // Tested by testSetProtocol.
    }

    public void testRemoveProvidedInterface()
    {
        testAddProvidedInterface();
        port.removeProvidedInterface(port.getProvidedInterfaces().get(0));
        assertEquals(0, port.getProvidedInterfaces().size());
    }

    public void testAddProvidedInterface()
    {
        IInterface intf = factory.createInterface(null);
        project.addOwnedElement(intf);
        port.addProvidedInterface(intf);
        assertEquals(1, port.getProvidedInterfaces().size());
        assertEquals(intf.getXMIID(), 
            port.getProvidedInterfaces().get(0).getXMIID());
    }
    
    public void testGetIsProvidedInterface()
    {
        testAddProvidedInterface();
        assertTrue(port.getIsProvidedInterface(port.getProvidedInterfaces().get(0)));
    }

    public void testGetProvidedInterfaces()
    {
        // Tested by testAddProvidedInterface.
    }

    public void testAddRequiredInterface()
    {
        IInterface intf = factory.createInterface(null);
        project.addOwnedElement(intf);
        port.addRequiredInterface(intf);
        assertEquals(1, port.getRequiredInterfaces().size());
        assertEquals(intf.getXMIID(), 
            port.getRequiredInterfaces().get(0).getXMIID());
    }
    
    public void testGetIsRequiredInterface()
    {
        testAddRequiredInterface();
        assertTrue(port.getIsRequiredInterface(port.getRequiredInterfaces().get(0)));
    }

    public void testRemoveRequiredInterface()
    {
        testAddRequiredInterface();
        port.removeRequiredInterface(port.getRequiredInterfaces().get(0));
        assertEquals(0, port.getRequiredInterfaces().size());
    }

    public void testGetRequiredInterfaces()
    {
        // Tested by testAddRequiredInterface.
    }
}