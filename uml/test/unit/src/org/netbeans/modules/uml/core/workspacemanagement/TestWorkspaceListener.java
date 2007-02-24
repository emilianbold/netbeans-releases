package org.netbeans.modules.uml.core.workspacemanagement;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

/**
 *
 * @author Trey Spiva
 */
public class TestWorkspaceListener implements IWorkspaceEventsSink
    
{
    
    private boolean m_WorkspaceClosed    = false;
    private boolean m_WorkspacePreClose  = false;
    private boolean m_WorkspaceSaved     = false;
    private boolean m_WorkspacePreSave   = false;
    private boolean m_WorkspaceOpened    = false;
    private boolean m_WorkspaceCreated   = false;
    private boolean m_WorkspacePreCreate = false;
    private boolean m_WorkspacePreOpen   = false;
    
    public boolean GotAllEvents()
    {
        boolean retVal = false;
        
        return retVal;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspacePreCreate(org.netbeans.modules.uml.core.workspacemanagement.IWorkspacePreCreateEventPayload, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onWorkspacePreCreate(IWorkspacePreCreateEventPayload pEvent,
        IResultCell cell)
    {
        m_WorkspacePreCreate = true;
        
        String msg = "onWorkspacePreCreate";
        if(pEvent != null)
        {
            msg += pEvent.getName() + "[" + pEvent.getFileName() + "]";
        }
        ETSystem.out.println( msg );
        
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspaceCreated(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onWorkspaceCreated(IWorkspace space, IResultCell cell)
    {
        m_WorkspaceCreated = true;
        ETSystem.out.println("onWorkspaceCreated: " + space.getName());
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspacePreOpen(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onWorkspacePreOpen(String fileName, IResultCell cell)
    {
        m_WorkspacePreOpen = true;
        ETSystem.out.println("onWorkspacePreOpen: " + fileName);
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspaceOpened(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onWorkspaceOpened(IWorkspace space, IResultCell cell)
    {
        m_WorkspaceOpened = true;
        ETSystem.out.println("onWorkspaceOpened: " + space.getName());
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspacePreSave(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onWorkspacePreSave(String fileName, IResultCell cell)
    {
        m_WorkspacePreSave = true;
        ETSystem.out.println("onWorkspacePreSave: " + fileName);
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspaceSaved(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onWorkspaceSaved(IWorkspace space, IResultCell cell)
    {
        m_WorkspaceSaved = true;
        ETSystem.out.println("onWorkspaceSaved: " + space.getName());
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspacePreClose(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onWorkspacePreClose( IWorkspace space, IResultCell cell )
    {
        m_WorkspacePreClose = true;
        ETSystem.out.println("onWorkspacePreClose: " + space.getName());
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspaceClosed(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
    public void onWorkspaceClosed(IWorkspace space, IResultCell cell)
    {
        m_WorkspaceClosed = true;
        ETSystem.out.println("onWorkspaceClosed: " + space.getName());
    }
}
