package org.netbeans.modules.uml.core.metamodel.infrastructure;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;

/**
 * Test cases for ConnectableElement.
 */
public class ConnectableElementTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ConnectableElementTestCase.class);
    }

    private IConnectableElement con;
        
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();

        con = (IConnectableElement)FactoryRetriever.instance().createType("Actor", null);
        //con.prepareNode(DocumentFactory.getInstance().createElement(""));
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

    public void testAddEnd()
    {
        IConnectorEnd cend = factory.createConnectorEnd(null);
        project.addElement(cend);
        con.addEnd(cend);
        assertEquals(1, con.getEnds().size());
        assertEquals(cend.getXMIID(), con.getEnds().get(0).getXMIID());
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
    
    public void testAddRoleContext()
    {
        IStructuredClassifier sc = createClass("MyClass");
        con.addRoleContext(sc);
        assertEquals(1, con.getRoleContexts().size());
        assertEquals(sc.getXMIID(), con.getRoleContexts().get(0).getXMIID());
    }

    public void testRemoveRoleContext()
    {
        testAddRoleContext();
        con.removeRoleContext(con.getRoleContexts().get(0));
        assertEquals(0, con.getRoleContexts().size());
    }
    
    public void testGetRoleContexts()
    {
        // Tested by testAddRoleContext.
    }
}