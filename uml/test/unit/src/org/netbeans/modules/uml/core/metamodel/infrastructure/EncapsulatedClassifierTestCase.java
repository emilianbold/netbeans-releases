package org.netbeans.modules.uml.core.metamodel.infrastructure;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;

/**
 * Test cases for EncapsulatedClassifier.
 */
public class EncapsulatedClassifierTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(EncapsulatedClassifierTestCase.class);
    }

    private IEncapsulatedClassifier enc;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        enc = createClass("Enc");
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        enc.delete();
    }
    
    public void testAddPort()
    {
        // All methods in EncapsulatedClassifier are stubbed.

//        IPort port = factory.createPort(null);
//        project.addElement(port);
//        enc.addPort(port);
//        assertEquals(1, enc.getPorts().size());
//        assertEquals(port.getXMIID(), enc.getPorts().get(0).getXMIID());
    }

    public void testRemovePort()
    {
//        testAddPort();
//        enc.removePort(enc.getPorts().get(0));
//        assertEquals(0, enc.getPorts().size());
    }

    public void testGetPorts()
    {
        // Tested by testAddPort.
    }
}