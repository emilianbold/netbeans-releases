package org.netbeans.modules.uml.core.workspacemanagement;

import java.io.File;

import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;

import junit.framework.TestCase;
/**
 *
 * @author Trey Spiva
 */
public class WorkspaceDispatcherTestCase extends TestCase
{
    private WorkspaceEventDispatcher m_Dispatcher        = new WorkspaceEventDispatcher();
    private TestWorkspaceListener    m_WorkspaceListener = new TestWorkspaceListener();
    
    /**
     * Constructor for WorkspaceDispatcherTestCase.
     * @param arg0
     */
    public WorkspaceDispatcherTestCase(String arg0)
    {
        super(arg0);
    }
    
    public void testWorkspaceCreate()
    {
        WorkspaceManager manager = new WorkspaceManager();
        manager.setEventDispatcher(m_Dispatcher);
        
        File dir = new File("c:\\temp\\workspacetest\\testworkspace");
        deleteFolder(dir);
        
        createWorkspaceTest(manager, "c:\\temp\\workspacetest\\testworkspace\\TestWorkspace.etw", "TestWorkspace", true);
        createWorkspaceTest(manager, "c:\\temp\\workspacetest\\testworkspace\\TestWorkspace.etw", "", true);
        
        dir.mkdirs();
        createWorkspaceTest(manager, "c:\\temp\\workspacetest\\testworkspace\\TestWorkspace.etw", "TestWorkspace", false);
        
    }
    
    public void testWorkspaceOpen()
    {
        WorkspaceManager manager = new WorkspaceManager();
        manager.setEventDispatcher(m_Dispatcher);
        
        try
        {
            IWorkspace space = manager.openWorkspace("c:\\temp\\workspacetest\\testworkspace\\TestWorkspace.etw");
            manager.closeWorkspace(space, "c:\\temp\\workspacetest\\testworkspace\\TestOpenWorkspace.etw", true);
            
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
    
    /**
     * @param string
     * @param string2
     * @param b
     */
    protected void createWorkspaceTest(WorkspaceManager manager,
        String wsFilename,
        String wsName,
        boolean throwExpected)
    {
        
        try
        {
            manager.createWorkspace(wsFilename, wsName);
            
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
        
    }
    
    /**
     * @param dir
     */
    protected void deleteFolder(File dir)
    {
        if(dir.isDirectory() == true)
        {
            File[] contents = dir.listFiles();
            for(int index = 0; index < contents.length; index++)
            {
                if(contents[index].isDirectory() == true)
                {
                    deleteFolder(dir);
                    contents[index] = null;
                }
                else
                {
                    contents[index].delete();
                    contents[index] = null;
                }
            }
            
            dir.delete();
        }
    }
    
   /*
    * @see TestCase#setUp()
    */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        m_Dispatcher.registerForWorkspaceEvents(m_WorkspaceListener);
    }
    
   /*
    * @see TestCase#tearDown()
    */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        m_Dispatcher.revokeWorkspaceSink(m_WorkspaceListener);
    }
    
}
