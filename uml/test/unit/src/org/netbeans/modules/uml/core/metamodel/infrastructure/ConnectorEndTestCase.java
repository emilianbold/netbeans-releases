package org.netbeans.modules.uml.core.metamodel.infrastructure;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;

/**
 * Test cases for ConnectorEnd.
 */
public class ConnectorEndTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ConnectorEndTestCase.class);
    }

    private IConnectorEnd end;
        
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        end = factory.createConnectorEnd(null);
        project.addElement(end);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        end.delete();
    }

    public void testSetConnector()
    {
        IConnector con = factory.createConnector(null);
        project.addElement(con);
        
        end.setConnector(con);
        assertEquals(con.getXMIID(), end.getConnector().getXMIID());
    }
    
    public void testGetConnector()
    {
        // Tested by testSetConnector.
    }
    
    public void testSetDefiningEnd()
    {
        IAssociationEnd ae = factory.createAssociationEnd(null);
        project.addElement(ae);
        
        end.setDefiningEnd(ae);
        // This code is currently stubbed.
//        assertEquals(ae.getXMIID(), end.getDefiningEnd().getXMIID());
    }
    
    public void testGetDefiningEnd()
    {
        // Tested by testSetDefiningEnd.
    }
    
    public void testSetInitialCardinality()
    {
        end.setInitialCardinality(10);
        assertEquals(10, end.getInitialCardinality());
        
        end.setInitialCardinality(1);
        assertEquals(1, end.getInitialCardinality());
    }
    
    public void testGetInitialCardinality()
    {
        // Tested by testSetInitialCardinality.
    }
    
    public void testSetMultiplicity()
    {
        IMultiplicity mul = factory.createMultiplicity(null);
        end.setMultiplicity(mul);
        assertEquals(mul.getXMIID(), end.getMultiplicity().getXMIID());
    }
    
    public void testGetMultiplicity()
    {
        // Tested by testSetMultiplicity.
    }
    
    public void testSetPart()
    {
        IConnectableElement cel = (IConnectableElement)FactoryRetriever.instance().createType("Actor", null);
        //cel.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(cel);
        
        end.setPart(cel);
        assertEquals(cel.getXMIID(), end.getPart().getXMIID());
    }
    
    public void testGetPart()
    {
        // Tested by testSetPart.
    }
    
    public void testSetPort()
    {
        IPort port = factory.createPort(null);
        project.addElement(port);
        end.setPort(port);
        assertEquals(port.getXMIID(), end.getPort().getXMIID());
    }
    
    public void testGetPort()
    {
        // Tested by testSetPort.
    }
}