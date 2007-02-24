package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;


/**
 * Test cases for RoleBinding.
 */
public class RoleBindingTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(RoleBindingTestCase.class);
    }
    
    private IRoleBinding roleb;
    
    protected void setUp() throws Exception
    {
        super.setUp();
        
        roleb = factory.createRoleBinding(null);
        project.addElement(roleb);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        roleb.delete();
    }

    public void testSetCollaboration()
    {
        ICollaborationOccurrence co = factory.createCollaborationOccurrence(null);
        project.addElement(co);
        roleb.setCollaboration(co);
        assertEquals(co.getXMIID(), roleb.getCollaboration().getXMIID());
    }
    
    public void testGetCollaboration()
    {
        // Tested by setCollaboration.
    }
    
    public void testSetFeature()
    {
        IClassifier c = createClass("Test");
        IAttribute attr = c.createAttribute("int", "a");
        c.addAttribute(attr);
        
        roleb.setFeature(attr);
        assertEquals(attr.getXMIID(), roleb.getFeature().getXMIID());
    }
    
    public void testGetFeature()
    {
        // Tested by setFeature.
    }
    
    public void testSetRole()
    {
        IClassifier c = createClass("Test");
        IAttribute attr = c.createAttribute("int", "a");
        c.addAttribute(attr);
        
        roleb.setRole(attr);
        assertEquals(attr.getXMIID(), roleb.getRole().getXMIID());
    }
    
    public void testGetRole()
    {
        // Tested by setRole.
    }
}