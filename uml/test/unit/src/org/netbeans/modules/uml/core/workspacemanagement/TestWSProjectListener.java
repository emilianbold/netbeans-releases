package org.netbeans.modules.uml.core.workspacemanagement;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

/**
 *
 * @author Trey Spiva
 */
public class TestWSProjectListener implements IWSProjectEventsSink
{
    
   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectPreCreate(com.embarcadero.describe.workspacemanagement.IWorkspace, java.lang.String, com.embarcadero.describe.umlsupport.IResultCell)
    */
    public void onWSProjectPreCreate( IWorkspace space, String projectName, IResultCell cell )
    {
        ETSystem.out.println("onWSProjectPreCreate: " + space.getName() + " Project Name: " + projectName);
    }
    
   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectCreated(com.embarcadero.describe.workspacemanagement.IWSProject, com.embarcadero.describe.umlsupport.IResultCell)
    */
    public void onWSProjectCreated( IWSProject project, IResultCell cell )
    {
        ETSystem.out.println("onWSProjectCreated: " + project.getName());
        
    }
    
   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectPreOpen(com.embarcadero.describe.workspacemanagement.IWorkspace, java.lang.String, com.embarcadero.describe.umlsupport.IResultCell)
    */
    public void onWSProjectPreOpen(IWorkspace space, String projName, IResultCell cell)
    {
        ETSystem.out.println("onWSProjectPreOpen: " + space.getName() + " Project Name: " + projName);
        
    }
    
   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectOpened(com.embarcadero.describe.workspacemanagement.IWSProject, com.embarcadero.describe.umlsupport.IResultCell)
    */
    public void onWSProjectOpened(IWSProject project, IResultCell cell)
    {
        ETSystem.out.println("onWSProjectOpened: " + project.getName());
        
    }
    
   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectPreRemove(com.embarcadero.describe.workspacemanagement.IWSProject, com.embarcadero.describe.umlsupport.IResultCell)
    */
    public void onWSProjectPreRemove(IWSProject project, IResultCell cell)
    {
        ETSystem.out.println("onWSProjectPreRemove: " + project.getName());
        
    }
    
   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectRemoved(com.embarcadero.describe.workspacemanagement.IWSProject, com.embarcadero.describe.umlsupport.IResultCell)
    */
    public void onWSProjectRemoved(IWSProject project, IResultCell cell)
    {
        ETSystem.out.println("onWSProjectRemoved: " + project.getName());
        
    }
    
   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectPreInsert(com.embarcadero.describe.workspacemanagement.IWorkspace, java.lang.String, com.embarcadero.describe.umlsupport.IResultCell)
    */
    public void onWSProjectPreInsert(IWorkspace space, String projectName, IResultCell cell)
    {
        ETSystem.out.println("onWSProjectPreInsert: " + space.getName() + " Project Name: " + projectName);
        
    }
    
   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectInserted(com.embarcadero.describe.workspacemanagement.IWSProject, com.embarcadero.describe.umlsupport.IResultCell)
    */
    public void onWSProjectInserted(IWSProject project, IResultCell cell)
    {
        ETSystem.out.println("onWSProjectInserted: " + project.getName());
        
    }
    
   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectPreRename(com.embarcadero.describe.workspacemanagement.IWSProject, java.lang.String, com.embarcadero.describe.umlsupport.IResultCell)
    */
    public void onWSProjectPreRename(IWSProject project, String newName, IResultCell cell)
    {
        ETSystem.out.println("onWSProjectPreRename: " + project.getName() + " New Project Name: " + newName);
        
    }
    
   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectRenamed(com.embarcadero.describe.workspacemanagement.IWSProject, java.lang.String, com.embarcadero.describe.umlsupport.IResultCell)
    */
    public void onWSProjectRenamed( IWSProject project, String oldName, IResultCell cell )
    {
        ETSystem.out.println("onWSProjectRenamed: " + project.getName() + " Project Old Name: " + oldName);
        
    }
    
   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectPreClose(com.embarcadero.describe.workspacemanagement.IWSProject, com.embarcadero.describe.umlsupport.IResultCell)
    */
    public void onWSProjectPreClose( IWSProject project, IResultCell cell )
    {
        ETSystem.out.println("onWSProjectPreClose: " + project.getName());
        
    }
    
   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectClosed(com.embarcadero.describe.workspacemanagement.IWSProject, com.embarcadero.describe.umlsupport.IResultCell)
    */
    public void onWSProjectClosed( IWSProject project, IResultCell cell )
    {
        ETSystem.out.println("onWSProjectClosed: " + project.getName());
        
    }
    
   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectPreSave(com.embarcadero.describe.workspacemanagement.IWSProject, com.embarcadero.describe.umlsupport.IResultCell)
    */
    public void onWSProjectPreSave(IWSProject project, IResultCell cell)
    {
        ETSystem.out.println("onWSProjectPreSave: " + project.getName());
        
    }
    
   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectSaved(com.embarcadero.describe.workspacemanagement.IWSProject, com.embarcadero.describe.umlsupport.IResultCell)
    */
    public void onWSProjectSaved(IWSProject project, IResultCell cell)
    {
        ETSystem.out.println("onWSProjectSaved: " + project.getName());
        
    }
    
}
