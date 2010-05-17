/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.uml.core.workspacemanagement;

import org.netbeans.modules.uml.core.eventframework.EventDispatchHelper;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;

/**
 *
 * @author Trey Spiva
 */
public class WSProjectEventDispatcher extends EventDispatchHelper
	implements IWSProjectEventDispatcher
{

	public WSProjectEventDispatcher()
	{
		super();
	}
	
	public WSProjectEventDispatcher(IWorkspaceEventDispatcher dispatcher)
	{
		setEventDispatcher(dispatcher);
	}
	
   /**
	 *
	 * Dispatches the WSProjectPreCreate event.
	 *
	 * @param space The Workspace that is about to create a new WSProject.
	 * @param projName The name of the WSProject to create.
	 * @param cell <i>[out]</i> The ResultCell used when firing the event.
	 *
	 * @return  <B>true</B> if the event was fully dispatched, else
	 *          <B>false</B> if a listener cancelled full dispatch.
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventDispatcher#dispatchWSProjectPreCreate(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 * 
	 */
   public IResultCell dispatchWSProjectPreCreate(IWorkspace space, String projName)
		throws InvalidArguments
   {
		IResultCell retVal = null;
      
      if((space != null) && (projName.length() > 0))
      {
      	IEventPayload payload = preparePayload("WSProjectPreCreate");
      	if (getEventDispatcher() instanceof IWorkspaceEventDispatcher)
         {
            IWorkspaceEventDispatcher disp = (IWorkspaceEventDispatcher)getEventDispatcher();
            retVal = disp.fireWSProjectPreCreate(space, projName, payload);
         }
      }
      else
      {
      	throw new InvalidArguments();
      }
      return retVal;
   }

   /**
	 *
	 * Dispatches the WSProjectCreated event.
	 *
	 * @param wsProject The WSProject that was just created.
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventDispatcher#dispatchWSProjectCreated(org.netbeans.modules.uml.core.workspacemanagement.IWSProject)
	 */
   public void dispatchWSProjectCreated(IWSProject wsProject)
		throws InvalidArguments
   {
      if (wsProject != null)
      {
			IEventPayload payload = preparePayload("WSProjectCreated");
         if (getEventDispatcher() instanceof IWorkspaceEventDispatcher)
         {
            IWorkspaceEventDispatcher disp = (IWorkspaceEventDispatcher)getEventDispatcher();
            disp.fireWSProjectCreated(wsProject, payload);
         }
      }
		else
		{
			throw new InvalidArguments();
		}
      
   }

   /**
	 *
	 * Dispatches the WSProjectPreOpen event.
	 *
	 * @param space[in] The Workspace that is about to open a particular WSProject.
	 * @param projectName[in] The name of the WSProject about to be opened.
	 * 
	 * @return  <B>true</B> if the event was fully dispatched, else
	 *          <B>false</B> if a listener cancelled full dispatch.
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventDispatcher#dispatchWSProjectPreOpen(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String)
	 * 
	 */
   public boolean dispatchWSProjectPreOpen(IWorkspace space, String projectName) 
   	throws InvalidArguments
   {
      boolean retVal = true;
      
		if((space != null) && (projectName.length() > 0))
      {
         IEventPayload payload = preparePayload("WSProjectPreOpen");
         if (getEventDispatcher() instanceof IWorkspaceEventDispatcher)
         {
            IWorkspaceEventDispatcher disp = (IWorkspaceEventDispatcher)getEventDispatcher();
            retVal = disp.fireWSProjectPreOpen(space, projectName, payload);
         }
      }
      else
      {
         throw new InvalidArguments();
      }
      
      return retVal;
   }

   /**
	 *
	 * Dispatches the WSProjectOpened event.
	 *
	 * @param wsProject[in] The WSProject about to be opend.
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventDispatcher#dispatchWSProjectOpened(org.netbeans.modules.uml.core.workspacemanagement.IWSProject)
	 * 
	 */
   public void dispatchWSProjectOpened(IWSProject wsProject)
   	throws InvalidArguments
   {
      if (wsProject != null)
      {
         IEventPayload payload = preparePayload("WSProjectOpened");
         if (getEventDispatcher() instanceof IWorkspaceEventDispatcher)
         {
            IWorkspaceEventDispatcher disp = (IWorkspaceEventDispatcher)getEventDispatcher();
            disp.fireWSProjectOpened(wsProject, payload);
         }
      }
      else
      {
         throw new InvalidArguments();
      }      
   }

   /**
	 *
	 * Dispatches the WSProjectPreRemove event.
	 *
	 * @param wsProject The WSProject about to be removed from the Workspace.
	 * 
	 * @return  <B>true</B> if the event was fully dispatched, else
	 *          <B>false</B> if a listener cancelled full dispatch.
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventDispatcher#dispatchWSProjectPreRemove(org.netbeans.modules.uml.core.workspacemanagement.IWSProject)
	 * 
	 */
   public boolean dispatchWSProjectPreRemove(IWSProject project) 
   	throws InvalidArguments
   {
      boolean retVal = true;
      
      if (project != null)
      {
         IEventPayload payload = preparePayload("WSProjectPreRemove");
         if (getEventDispatcher() instanceof IWorkspaceEventDispatcher)
         {
            IWorkspaceEventDispatcher disp = (IWorkspaceEventDispatcher)getEventDispatcher();
            retVal = disp.fireWSProjectPreRemove(project, payload);
         }
      }
      else
      {
         throw new InvalidArguments();
      }
      
      return retVal;
   }

   /**
	 * Dispatches the WSProjectRemoved event.
	 *
	 * @param wsProject[in] The WSProject removed from the Workspace.
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventDispatcher#dispatchWSProjectRemoved(org.netbeans.modules.uml.core.workspacemanagement.IWSProject)
	 */
   public void dispatchWSProjectRemoved(IWSProject project) 
   	throws InvalidArguments
   {
      if (project != null)
      {
         IEventPayload payload = preparePayload("WSProjectRemoved");
         if (getEventDispatcher() instanceof IWorkspaceEventDispatcher)
         {
            IWorkspaceEventDispatcher disp = (IWorkspaceEventDispatcher)getEventDispatcher();
            disp.fireWSProjectRemoved(project, payload);
         }
      }
      else
      {
         throw new InvalidArguments();
      }
      
   }

   /**
	 *
	 * Dispatches the WSProjectPreInsert event.
	 *
	 * @param project[in] The WSProject about to be inserted.
	 * @param projectName[in] The name of the WSProject that will be inserted.
	 * @return  <B>true</B> if the event was fully dispatched, else
	 *          <B>false</B> if a listener cancelled full dispatch.
	 *
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventDispatcher#dispatchWSProjectPreInsert(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String)
	 * 
	 */
   public boolean dispatchWSProjectPreInsert(IWorkspace space, String projectName)
   	throws InvalidArguments
   {
      boolean retVal = true;
      
      if ((space != null) && (projectName.length() > 0))
      {
         IEventPayload payload = preparePayload("WSProjectPreInsert");
         if (getEventDispatcher() instanceof IWorkspaceEventDispatcher)
         {
            IWorkspaceEventDispatcher disp = (IWorkspaceEventDispatcher)getEventDispatcher();
            retVal = disp.fireWSProjectPreInsert(space, projectName, payload);
         }
      }
      else
      {
         throw new InvalidArguments();
      }
      
      return retVal;
   }

   /**
	 * Dispatches the WSProjectInserted event.
	 *
	 * @param wsProject[in] The WSProject about to be inserted in the Workspace.
	 *
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventDispatcher#dispatchWSProjectInserted(org.netbeans.modules.uml.core.workspacemanagement.IWSProject)
	 * 
	 */
   public void dispatchWSProjectInserted(IWSProject project) 
   	throws InvalidArguments
   {
      if (project != null)
      {
         IEventPayload payload = preparePayload("WSProjectInserted");
         if (getEventDispatcher() instanceof IWorkspaceEventDispatcher)
         {
				IWorkspaceEventDispatcher disp = (IWorkspaceEventDispatcher)getEventDispatcher();
            disp.fireWSProjectInserted(project, payload);
         }
      }
      else
      {
         throw new InvalidArguments();
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventDispatcher#dispatchWSProjectPreRename(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, java.lang.String)
    */
	/**
	 *
	 * Dispatches the WSProjectPreRename event.
	 *
	 * @param project[in] The WSProject about to be renamed.
	 * @param newName[in] The name that the WSProject will be renamed to.
	 * @return  <B>true</B> if the event was fully dispatched, else
	 *          <B>false</B> if a listener cancelled full dispatch.
	 *
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventDispatcher#dispatchWSProjectPreRename(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, java.lang.String)
	 * 
	 */
   public boolean dispatchWSProjectPreRename(IWSProject project, String newName) 
   	throws InvalidArguments
   {
      boolean retVal = true;
      
      if ((project != null) && (newName.length() > 0))
      {
         IEventPayload payload = preparePayload("WSProjectPreRename");
         if (getEventDispatcher() instanceof IWorkspaceEventDispatcher)
         {
            IWorkspaceEventDispatcher disp = (IWorkspaceEventDispatcher)getEventDispatcher();
            retVal = disp.fireWSProjectPreRename(project, newName, payload);
         }
      }
      else
      {
         throw new InvalidArguments();
      }
      
      return retVal;
   }

   /**
	 *
	 * Dispatches the WSProjectRenamed event.
	 *
	 * @param wsProject[in] The WSProject that was just renamed.
	 * @param oldName[in] The previous value of the WSProject's name.
	 *
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventDispatcher#dispatchWSProjectRenamed(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, java.lang.String)
	 * 
	 */
   public void dispatchWSProjectRenamed(IWSProject project, String oldName) 
   	throws InvalidArguments
   {
      if ((project != null) && (oldName != null))
      {
         IEventPayload payload = preparePayload("WSProjectRenamed");
         if (getEventDispatcher() instanceof IWorkspaceEventDispatcher)
         {
            IWorkspaceEventDispatcher disp = (IWorkspaceEventDispatcher)getEventDispatcher();
            disp.fireWSProjectRenamed(project, oldName, payload);
         }
      }
      else
      {
         throw new InvalidArguments();
      }
   }

   /**
	 *
	 * Dispatches the WSProjectPreClose event.
	 *
	 * @param project[in] The WSProject about to be closed.
	 * @return  <B>true</B> if the event was fully dispatched, else
	 *          <B>false</B> if a listener cancelled full dispatch.
	 *
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventDispatcher#dispatchWSProjectPreClose(org.netbeans.modules.uml.core.workspacemanagement.IWSProject)
	 * 
	 */
   public boolean dispatchWSProjectPreClose(IWSProject project) 
   	throws InvalidArguments
   {
      boolean retVal = true;
      
      if (project != null)
      {
         IEventPayload payload = preparePayload("WSProjectPreClose");
         if (getEventDispatcher() instanceof IWorkspaceEventDispatcher)
         {
            IWorkspaceEventDispatcher disp = (IWorkspaceEventDispatcher)getEventDispatcher();
            retVal = disp.fireWSProjectPreClose(project, payload);
         }
      }
      else
      {
         throw new InvalidArguments();
      }
      
      return retVal;
   }

   /**
	 *
	 * Dispatches the WSProjectClosed event.
	 *
	 * @param projName[in] The name of the WSProject that was just closed.
	 *
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventDispatcher#dispatchWSProjectClosed(org.netbeans.modules.uml.core.workspacemanagement.IWSProject)
	 * 
	 */
   public void dispatchWSProjectClosed(IWSProject project) 
   	throws InvalidArguments
   {
      if (project != null)
      {
         IEventPayload payload = preparePayload("WSProjectClosed");
         if (getEventDispatcher() instanceof IWorkspaceEventDispatcher)
         {
				IWorkspaceEventDispatcher disp = (IWorkspaceEventDispatcher)getEventDispatcher();
            disp.fireWSProjectClosed(project, payload);
         }
      }
      else
      {
         throw new InvalidArguments();
      }
      
   }

   /**
	 * Dispatches the WSProjectPreSave event.
	 *
	 * @param wsProject[in] The WSProject about to be saved.
	 * @return  <B>true</B> if the event was fully dispatched, else
	 *          <B>false</B> if a listener cancelled full dispatch.
	 *
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventDispatcher#dispatchWSProjectPreSave(org.netbeans.modules.uml.core.workspacemanagement.IWSProject)
	 * 
	 */
   public boolean dispatchWSProjectPreSave(IWSProject project) 
   	throws InvalidArguments
   {
   	boolean retVal = true;
   	
   	if (project != null)
      {
         IEventPayload payload = preparePayload("WSProjectPreSave");
         if (getEventDispatcher() instanceof IWorkspaceEventDispatcher)
         {
            IWorkspaceEventDispatcher disp = (IWorkspaceEventDispatcher)getEventDispatcher();
            retVal = disp.fireWSProjectPreSave(project, payload);
         }
      }
      else
      {
         throw new InvalidArguments();
      }
   	
   	return retVal; 
   }

   /**
	 * Dispatches the WSProjectSaved event.
	 *
	 * @param wsProject[in] The WSProject just saved.
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventDispatcher#dispatchWSProjectSaved(org.netbeans.modules.uml.core.workspacemanagement.IWSProject)
	 */
   public void dispatchWSProjectSaved(IWSProject project) 
   	throws InvalidArguments
   {
      if (project != null)
      {
         IEventPayload payload = preparePayload("WSProjectSaved");
         if (getEventDispatcher() instanceof IWorkspaceEventDispatcher)
         {
            IWorkspaceEventDispatcher disp = (IWorkspaceEventDispatcher)getEventDispatcher();
            disp.fireWSProjectSaved(project, payload);
         }
      }
      else
      {
         throw new InvalidArguments();
      }
      
   }

   
}
