/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.core.workspacemanagement;

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

/**
 *
 * @author Trey Spiva
 */
public class WSProjectEventsAdapter implements IWSProjectEventsSink
{

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectPreCreate(com.embarcadero.describe.workspacemanagement.IWorkspace, java.lang.String, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSProjectPreCreate(IWorkspace space, String projectName, IResultCell cell)
   {  
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectCreated(com.embarcadero.describe.workspacemanagement.IWSProject, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSProjectCreated(IWSProject project, IResultCell cell)
   {  
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectPreOpen(com.embarcadero.describe.workspacemanagement.IWorkspace, java.lang.String, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSProjectPreOpen(IWorkspace space, String projName, IResultCell cell)
   {  
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectOpened(com.embarcadero.describe.workspacemanagement.IWSProject, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSProjectOpened(IWSProject project, IResultCell cell)
   {  
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectPreRemove(com.embarcadero.describe.workspacemanagement.IWSProject, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSProjectPreRemove(IWSProject project, IResultCell cell)
   {  
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectRemoved(com.embarcadero.describe.workspacemanagement.IWSProject, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSProjectRemoved(IWSProject project, IResultCell cell)
   {  
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectPreInsert(com.embarcadero.describe.workspacemanagement.IWorkspace, java.lang.String, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSProjectPreInsert(IWorkspace space, String projectName, IResultCell cell)
   {  
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectInserted(com.embarcadero.describe.workspacemanagement.IWSProject, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSProjectInserted(IWSProject project, IResultCell cell)
   {  
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectPreRename(com.embarcadero.describe.workspacemanagement.IWSProject, java.lang.String, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSProjectPreRename(IWSProject project, String newName, IResultCell cell)
   {  
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectRenamed(com.embarcadero.describe.workspacemanagement.IWSProject, java.lang.String, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSProjectRenamed(IWSProject project, String oldName, IResultCell cell)
   {  
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectPreClose(com.embarcadero.describe.workspacemanagement.IWSProject, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSProjectPreClose(IWSProject project, IResultCell cell)
   {  
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectClosed(com.embarcadero.describe.workspacemanagement.IWSProject, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSProjectClosed(IWSProject project, IResultCell cell)
   {  
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectPreSave(com.embarcadero.describe.workspacemanagement.IWSProject, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSProjectPreSave(IWSProject project, IResultCell cell)
   {  
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.workspacemanagement.IWSProjectEventsSink#onWSProjectSaved(com.embarcadero.describe.workspacemanagement.IWSProject, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onWSProjectSaved(IWSProject project, IResultCell cell)
   {  
   }
}
