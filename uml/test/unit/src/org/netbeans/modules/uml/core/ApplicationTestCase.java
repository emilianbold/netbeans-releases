
/*
 * Created on Sep 22, 2003
 *
 */
package org.netbeans.modules.uml.core;
import org.netbeans.modules.uml.core.coreapplication.CoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
/**
 * @author aztec
 *
 */
public class ApplicationTestCase extends AbstractUMLTestCase
{
    IApplication app = null;
    IWorkspace ws = null;
    /**
     *  Constructor
     */
    public ApplicationTestCase()
    {
        super();
    }
    
    protected void setUp()
    {
        ICoreProduct prod = CoreProductManager.instance().getCoreProduct();
        if (prod != null)
        {
            //app = prod.getApplication();
            app = new Application();
        }
    }
    
    public void testCreateWorkspace()
    {
        ws = app.createWorkspace("d:\\temp\\test\\SampleWS","MyWS");
        assertEquals("MyWS",ws.getName());
        assertTrue(ws.isOpen());
        
        app.closeWorkspace(ws,"d:\\temp\\test\\SampleWS",false);
        assertFalse(ws.isOpen());
    }
    
    public void testGetQueryManager()
    {
        //Getting query manager.
        assertNotNull(app.getQueryManager());
    }
    
    public void testProjectLifeCycle()
    {
        //Create Project
        String projFileName = "d:\\temp\\test\\TestProject.etd";
        String projName = "TestProject";
        IProject proj = app.createProject();
        proj.setFileName(projFileName);
        proj.setName(projName);
        proj.save(projFileName,true);
        
        //Open Project
        IProject proj1 = app.openProject(projFileName);
        assertNotNull(proj1);
        assertEquals(projName,proj1.getName());
        
        //Get Project [ 3 kinds of methods]
        IProject proj2 = app.getProjectByName("TestProject");
        assertEquals(projName,proj2.getName());
        
        proj2 = app.getProjectByFileName(projFileName);
        assertEquals(projFileName,proj2.getFileName());
        
//		IWorkspace ws1 = ProductRetriever.retrieveProduct().getCurrentWorkspace();
        IWorkspace ws1 = app.openWorkspace("d:\\temp\\test\\SampleWS");
        proj2 = app.getProjectByName(ws1,projName);
        assertEquals(projName,proj2.getName());
        
        //Destroy application. The destroy call will call "closeAllProjects()" also.
        app.destroy();
        assertEquals(0,app.getNumOpenedProjects());
    }
    
    
    public void testSample()
    {
/*		Application application = (Application)app;
                IWorkspace ws = application.createWorkspace("d:\\temp\\test\\SampleWS","MyWS");
 
                IProject proj = application.createProject();
                proj.setFileName("d:\\temp\\test\\Loveyou2.etd");
                proj.setName("Loveyou2");
                proj.save("d:\\temp\\test\\Loveyou2.etd",true);
 
                ArrayList<IProject> projs = application.m_Projects;
                for (int i=0;i<projs.size();i++)
                {
                        ETSystem.out.println("proj name "+i+" = "+projs.get(i).getFileName());
                }
//		ETSystem.out.println("Num of prjs "+projs);
                IProject proj1 = application.openProject("d:\\temp\\test\\Loveyou2.etd");
                ETSystem.out.println("Opened proj "+proj1);
//		ETSystem.out.println("num opened projects after "+application.getNumOpenedProjects());
 */	}
    
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ApplicationTestCase.class);
    }
}



