package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * Test cases for CollaborationOccurrence.
 */
public class CollaborationOccurrenceTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(CollaborationOccurrenceTestCase.class);
    }

    private ICollaborationOccurrence coll;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        coll = factory.createCollaborationOccurrence(null);
        project.addElement(coll);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        project.removeElement(coll);
        coll.delete();
    }

    public void testAddRoleBinding()
    {
        IRoleBinding rb = factory.createRoleBinding(null);
        coll.addRoleBinding(rb);
        
        ETList<IRoleBinding> rbs = coll.getRoleBindings();
        assertNotNull(rbs);
        assertEquals(1, rbs.size());
        assertEquals(rb.getXMIID(), rbs.get(0).getXMIID());
    }

    public void testRemoveRoleBinding()
    {
        testAddRoleBinding();
        coll.removeRoleBinding(coll.getRoleBindings().get(0));
        
        ETList<IRoleBinding> rbs = coll.getRoleBindings();
        assertNotNull(rbs);
        assertEquals(0, rbs.size());
    }
    
    public void testGetRoleBindings()
    {
        // Tested by testAddRoleBinding
    }
}