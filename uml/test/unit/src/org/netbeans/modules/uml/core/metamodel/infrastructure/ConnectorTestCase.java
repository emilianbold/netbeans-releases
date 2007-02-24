package org.netbeans.modules.uml.core.metamodel.infrastructure;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior;

/**
 * Test cases for Connector.
 */
public class ConnectorTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ConnectorTestCase.class);
    }

    private IConnector con;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        con = factory.createConnector(null);
        project.addElement(con);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        con.delete();
    }
    
    public void testAddBehavior()
    {
        IBehavior b = (IBehavior)FactoryRetriever.instance().createType("Procedure", null);
        //b.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(b);
        
        con.addBehavior(b);
        assertEquals(1, con.getBehaviors().size());
        assertEquals(b.getXMIID(), con.getBehaviors().get(0).getXMIID());
    }

    public void testRemoveBehavior()
    {
        testAddBehavior();
        con.removeBehavior(con.getBehaviors().get(0));
        assertEquals(0, con.getBehaviors().size());
    }
    
    public void testGetBehaviors()
    {
        // Tested by testAddBehavior.
    }

    public void testAddEnd()
    {
        IConnectorEnd end = factory.createConnectorEnd(null);
        project.addElement(end);
        con.addEnd(end);
        assertEquals(1, con.getEnds().size());
        assertEquals(end.getXMIID(), con.getEnds().get(0).getXMIID());
    }
    
    public void testRemoveEnd()
    {
        testAddEnd();
        con.removeEnd(con.getEnds().get(0));
        assertEquals(0, con.getEnds().size());
    }
    
    public void testGetEnds()
    {
        // Tested by testAddEnd.
    }
    
    public void testSetFrom()
    {
        IConnectorEnd end = factory.createConnectorEnd(null);
        project.addElement(end);
        con.setFrom(end);
        assertEquals(end.getXMIID(), con.getFrom().getXMIID());
    }
    
    public void testGetFrom()
    {
        // Tested by testSetFrom.
    }
    
    public void testSetTo()
    {
        IConnectorEnd from = factory.createConnectorEnd(null);
        project.addElement(from);
        
        IConnectorEnd to = factory.createConnectorEnd(null);
        project.addElement(to);
        con.setFrom(from);
        con.setTo(to);
        
        assertEquals(to.getXMIID(), con.getTo().getXMIID());
    }
    
    public void testGetTo()
    {
        // Tested by testSetTo.
    }
}