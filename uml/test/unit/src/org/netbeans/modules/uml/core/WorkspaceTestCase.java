package org.netbeans.modules.uml.core;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import java.io.File;


/**
 */
public class WorkspaceTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(WorkspaceTestCase.class);
    }
    
    public void testSaveWorkspace()
    {
        // Add something to our project, save workspace, close workspace and
        // project, reload and check if it's still there.
        
        createClass("AlCapone");
        product.save();
        
        project.close();
        workspace.close(false);
        
        File dir = new File(".", "test");
        workspace = getWorkspace(dir, "test");
        project   = getProject("A");
        
        assertNotNull(project);
        //assertEquals(1, project.getOwnedElementsByName("AlCapone").size());
        
        createClass("DonJuan");
        product.save();
        
        project.close();
        workspace.close(false);
        
        workspace = getWorkspace(dir, "test");
        project   = getProject("A");
        assertNotNull(project);
        //assertEquals(1, project.getOwnedElementsByName("AlCapone").size());
        assertEquals(1, project.getOwnedElementsByName("DonJuan").size());
    }
}