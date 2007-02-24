package org.netbeans.modules.uml.core.typemanagement;

import java.io.File;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * Test cases for TypeManager.
 */
public class TypeManagerTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(TypeManagerTestCase.class);
    }
    
    private ITypeManager typeManager;
    private IClass       c;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        typeManager = project.getTypeManager();
        
        c = createClass("Thanatos");
    }
    
    public void testGetLocalCachedTypesByName()
    {
        ETList<INamedElement> nels = typeManager.getLocalCachedTypesByName("Thanatos");
        assertEquals(1, nels.size());
        assertEquals(c.getXMIID(), nels.get(0).getXMIID());
        assertEquals(c.getName(), nels.get(0).getName());
        
        String xmi = c.getXMIID();
        String name = c.getName();
        
        //Now save and reload the project and see if we still have the type
        product.save();
        
        product.getApplication().closeAllProjects(false);
        workspace.close(false);
        
        File dir = new File(".", "test");
        workspace = getWorkspace(dir, "test");
        project = getProject("A");
        
        assertEquals(1, project.getOwnedElementsByName("Thanatos").size());
        
        typeManager = project.getTypeManager();
        nels = typeManager.getLocalCachedTypesByName("Thanatos");
        assertEquals(1, nels.size());
        assertEquals(xmi, nels.get(0).getXMIID());
        assertEquals(name, nels.get(0).getName());
    }
}