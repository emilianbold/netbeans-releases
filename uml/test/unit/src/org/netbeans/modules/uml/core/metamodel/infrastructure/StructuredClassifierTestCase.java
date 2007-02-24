package org.netbeans.modules.uml.core.metamodel.infrastructure;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;

/**
 * Test cases for StructuredClassifier.
 */
public class StructuredClassifierTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(StructuredClassifierTestCase.class);
    }

    private IStructuredClassifier sc;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        sc = createClass("StructuredClassifier");
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        sc.delete();
    }
    
    public void testAddConnector()
    {
        IConnector con = factory.createConnector(null);
        project.addElement(con);
        sc.addConnector(con);
        
        assertEquals(1, sc.getConnectors().size());
        assertEquals(con.getXMIID(), sc.getConnectors().get(0).getXMIID());
    }

    public void testRemoveConnector()
    {
        testAddConnector();
        sc.removeConnector(sc.getConnectors().get(0));
        assertEquals(0, sc.getConnectors().size());
    }

    public void testGetConnectors()
    {
        // Tested by testAddConnector.
    }

    public void testAddPart()
    {
        IPart part = factory.createPart(null);
        project.addElement(part);
        sc.addPart(part);
        
        assertEquals(1, sc.getParts().size());
        assertEquals(part.getXMIID(), sc.getParts().get(0).getXMIID());
    }

    public void testRemovePart()
    {
        testAddPart();
        sc.removePart(sc.getParts().get(0));
        assertEquals(0, sc.getParts().size());
    }

    public void testGetParts()
    {
        // Tested by testAddPart.
    }

    public void testAddRole()
    {
        IConnectableElement cel = factory.createActor(null);
        project.addElement(cel);

        sc.addRole(cel);
        
        assertEquals(1, sc.getRoles().size());
        assertEquals(cel.getXMIID(), sc.getRoles().get(0).getXMIID());
    }

    public void testRemoveRole()
    {
        testAddRole();
        sc.removeRole(sc.getRoles().get(0));
        assertEquals(0, sc.getRoles().size());
    }

    public void testGetRoles()
    {
        // Tested by testAddRole.
    }
}
