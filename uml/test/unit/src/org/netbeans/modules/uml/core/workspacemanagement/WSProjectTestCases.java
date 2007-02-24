package org.netbeans.modules.uml.core.workspacemanagement;

import java.io.File;

import junit.framework.TestCase;

import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;

/**
 *
 * @author Trey Spiva
 */
public class WSProjectTestCases extends TestCase
{
    private WorkspaceEventDispatcher m_Dispatcher        = new WorkspaceEventDispatcher();
    private TestWorkspaceListener    m_WorkspaceListener = new TestWorkspaceListener();
    private TestWSProjectListener    m_WSProjectListener = new TestWSProjectListener();
    /**
     * Constructor for WSProjectTestCases.
     * @param arg0
     */
    public WSProjectTestCases(String arg0)
    {
        super(arg0);
    }
    
    public void testProjectCreation()
    {
        WorkspaceManager manager = new WorkspaceManager();
        manager.setEventDispatcher(m_Dispatcher);
        
        File dir = new File("c:\\temp\\workspacetest\\testworkspace");
        dir.mkdirs();
        
        IWorkspace ws = createWorkspaceTest(manager, "c:\\temp\\workspacetest\\testworkspace\\TestWSProjects.etw", "TestWSProject",
            false);
        try
        {
            ws.createWSProject("c:\\temp\\workspacetest\\testworkspace", "TreyProject");
            ws.createWSProject("c:\\temp\\workspacetest\\testworkspace", "TreyRelativeProject");
            ws.save();
        }
        catch (WorkspaceManagementException e)
        {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
        catch(Exception e)
        {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
        
    }
    
    public void testGetProjects()
    {
        WorkspaceManager manager = new WorkspaceManager();
        manager.setEventDispatcher(m_Dispatcher);
        
        try
        {
            IWorkspace ws = manager.openWorkspace("c:\\temp\\workspacetest\\testworkspace\\TestWSProjects.etw");
            ETList<IWSProject> projects = ws.getWSProjects();
            if(projects.size() != 2)
            {
                fail("The wrong number of projects");
            }
            else
            {
                if(projects.get(0).getName().equals("TreyProject") == false)
                {
                    fail("The first project name is not correct.");
                }
                else if(projects.get(1).getName().equals("TreyRelativeProject") == false)
                {
                    fail("The second project name is not correct.");
                }
            }
        }
        catch (InvalidArguments e)
        {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
        catch (WorkspaceManagementException e)
        {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
    }
    
    public void testGetProjectByName()
    {
        WorkspaceManager manager = new WorkspaceManager();
        manager.setEventDispatcher(m_Dispatcher);
        
        try
        {
            IWorkspace ws = manager.openWorkspace("c:\\temp\\workspacetest\\testworkspace\\TestWSProjects.etw");
            
            // Test trying to find a project that already exist.
            IWSProject project = ws.getWSProjectByName("TreyProject");
            if(project == null)
            {
                fail("The TreyProject project was not found");
            }
            
            // Now test trying to find a project that does not exist.
            // In this case it should be created.
            IWSProject project2 = ws.getWSProjectByName("TreyProject2");
            if(project2 != null)
            {
                fail("The TreyProject2 should not have been found.");
            }
        }
        catch (InvalidArguments e)
        {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
        catch (WorkspaceManagementException e)
        {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
    }
    
    public void testOpenProject()
    {
        WorkspaceManager manager = new WorkspaceManager();
        manager.setEventDispatcher(m_Dispatcher);
        
        try
        {
            IWorkspace ws = manager.openWorkspace("c:\\temp\\workspacetest\\testworkspace\\TestWSProjects.etw");
            
            // Test trying to find a project that already exist.
            IWSProject project = ws.getWSProjectByName("TreyProject");
            if(project != null)
            {
                project.open();
                if(project.isOpen() == false)
                {
                    fail("The project was not opened.");
                }
            }
            else
            {
                fail("The TreyProject project was not found");
            }
        }
        catch (InvalidArguments e)
        {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
        catch (WorkspaceManagementException e)
        {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
    }
    
    public void testCloseProject()
    {
        WorkspaceManager manager = new WorkspaceManager();
        manager.setEventDispatcher(m_Dispatcher);
        
        try
        {
            IWorkspace ws = manager.openWorkspace("c:\\temp\\workspacetest\\testworkspace\\TestWSProjects.etw");
            
            // Test trying to find a project that already exist.
            IWSProject project = ws.openWSProjectByName("TreyProject");
            if((project == null) || (project.isOpen() == false))
            {
                fail("The project was not opened.");
                
            }
            else
            {
                project.close(true);
                if(project.isOpen() == false)
                {
                    fail("The project was not closed.");
                }
            }
        }
        catch (InvalidArguments e)
        {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
        catch (WorkspaceManagementException e)
        {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
    }
    
    public void testRemoveProject()
    {
        WorkspaceManager manager = new WorkspaceManager();
        manager.setEventDispatcher(m_Dispatcher);
        
        try
        {
            IWorkspace ws = manager.openWorkspace("c:\\temp\\workspacetest\\testworkspace\\TestWSProjects.etw");
            
            ws.removeWSProjectByName("TreyProject");
            ETList<IWSProject> projects = ws.getWSProjects();
            if(projects.size() != 1)
            {
                fail("Wrong number of projects after remove.");
            }
            ws.setIsDirty(true);
            ws.save("c:\\temp\\workspacetest\\testworkspace\\TestWSProjectsRemoved.etw");
        }
        catch (InvalidArguments e)
        {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
        catch (WorkspaceManagementException e)
        {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
    }
    
    public void testElementData()
    {
        WorkspaceManager manager = new WorkspaceManager();
        manager.setEventDispatcher(m_Dispatcher);
        
        boolean testSucceed = true;
        
        try
        {
            IWorkspace ws = manager.openWorkspace("c:\\temp\\workspacetest\\testworkspace\\TestWSProjects.etw");
            
            ws.verifyUniqueLocation("c:\\temp\\workspacetest\\testworkspace\\foobar");
            
            // I have to add an element to set up for the next test.
            IWSProject project = ws.openWSProjectByName("TreyRelativeProject");
            if(project != null)
            {
                project.addElement("c:\\temp\\workspacetest\\testworkspace", "_TESTDATA__", "34");
                ws.setIsDirty(true);
                ws.save("c:\\temp\\workspacetest\\testworkspace\\TestWSProjectsElementAdded.etw");
            }
            
            // Test the failure condition.
            testSucceed = false;
            if(ws.verifyUniqueElementLocation("c:\\temp\\workspacetest\\testworkspace") == true)
            {
                fail("The workspace is not unique.");
            }
        }
        catch (WorkspaceManagementException e)
        {
            if(testSucceed == true)
            {
                e.printStackTrace();
                fail(e.getLocalizedMessage());
            }
        }
        catch (InvalidArguments e)
        {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
    }
    
    public void testOpenWSProjectByData()
    {
        WorkspaceManager manager = new WorkspaceManager();
        manager.setEventDispatcher(m_Dispatcher);
        
        boolean testSucceed = true;
        
        try
        {
            IWorkspace ws = manager.openWorkspace("c:\\temp\\workspacetest\\testworkspace\\TestWSProjectsElementAdded.etw");
            IWSProject proj = ws.openWSProjectByData("34");
            
            if(proj == null)
            {
                fail("Unable to find the projected associated to the data \"34\"");
            }
        }
        catch (WorkspaceManagementException e)
        {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
        catch (InvalidArguments e)
        {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
    }
    
    public void testRemoveByLocation()
    {
        //
        WorkspaceManager manager = new WorkspaceManager();
        manager.setEventDispatcher(m_Dispatcher);
        
        boolean testSucceed = true;
        
        try
        {
            IWorkspace ws = manager.openWorkspace("c:\\temp\\workspacetest\\testworkspace\\TestWSProjectsElementAdded.etw");
            IWSProject proj = ws.openWSProjectByName("TreyRelativeProject");
            
            if(proj.getElementByLocation("c:\\temp\\workspacetest\\testworkspace") == null)
            {
                fail("Unable to find the WSElement by location.");
            }
            
            ws.removeWSElementByLocation("c:\\temp\\workspacetest\\testworkspace");
            
            if(proj.getElementByLocation("c:\\temp\\workspacetest\\testworkspace") != null)
            {
                fail("removeElementByLocation failed to remove the project.");
            }
            //ws.save("c:\\temp\\workspacetest\\testworkspace\\TestWSProjectsRemovedByLoc.etw");
        }
        catch (WorkspaceManagementException e)
        {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
        catch (InvalidArguments e)
        {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
    }
    
    /**
     * @param string
     * @param string2
     * @param b
     */
    protected IWorkspace createWorkspaceTest(WorkspaceManager manager,
        String wsFilename,
        String wsName,
        boolean throwExpected)
    {
        IWorkspace retVal = null;
        try
        {
            retVal = manager.createWorkspace(wsFilename, wsName);
            
            if(throwExpected == true)
            {
                fail("An exception was expected to be thrown.");
            }
        }
        catch (WorkspaceManagementException e2)
        {
            if(throwExpected == false)
            {
                e2.printStackTrace();
                fail(e2.getLocalizedMessage());
            }
        }
        catch (InvalidArguments e)
        {
            if(throwExpected == false)
            {
                e.printStackTrace();
                fail(e.getLocalizedMessage());
            }
        }
        
        return retVal;
    }
    
        /*
         * @see TestCase#setUp()
         */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        m_Dispatcher.registerForWorkspaceEvents(m_WorkspaceListener);
        m_Dispatcher.registerForWSProjectEvents(m_WSProjectListener);
    }
    
        /*
         * @see TestCase#tearDown()
         */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        m_Dispatcher.revokeWorkspaceSink(m_WorkspaceListener);
        m_Dispatcher.revokeWSProjectSink(m_WSProjectListener);
    }
}
