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

import java.util.ArrayList;

import org.netbeans.modules.uml.core.eventframework.EventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.eventframework.EventManager;
import org.netbeans.modules.uml.core.eventframework.EventFunctor;

/**
 * @author sumitabhk
 *
 */
public class WorkspaceEventDispatcher extends EventDispatcher
   implements IWorkspaceEventDispatcher
{
	/** Handles the actual deployment of events to Workspace listeners. */
	private EventManager< IWorkspaceEventsSink > m_WSEventManager = null;

	/** Handles the actual deployment of events to WSProject listeners. */
	private EventManager< IWSProjectEventsSink > m_WSProjectEventManager = null;

	/** Handles the actual deployment of events to WSElement listeners. */
	private EventManager< IWSElementEventsSink > m_WSProjElementEventManager = null;

	/** Handles the actual deployment of events to WSElement modify listeners. */
	private EventManager< IWSElementModifiedEventsSink > m_WSElementModEventManager = null;
	
   public WorkspaceEventDispatcher()
   {
      super();
      
		m_WSEventManager = new EventManager< IWorkspaceEventsSink >();
		m_WSProjectEventManager = new EventManager< IWSProjectEventsSink >();
		m_WSProjElementEventManager = new EventManager< IWSElementEventsSink >();
		m_WSElementModEventManager = new EventManager< IWSElementModifiedEventsSink >();
   }

	/**
	 *
	 * Registers the passed in event sink with this dispatcher.
	 *
	 * @param sink The actual sink that will recieve notifications.
	 * @param cookie The unique identifier to be used when removing the listener.
	 *
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#registerForWorkspaceEvents(org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink)
	 * 
	 */
   public void registerForWorkspaceEvents(IWorkspaceEventsSink sink)
   {
		m_WSEventManager.addListener(sink, null);
   }
   
	/**
	 * Removes a listener from the current list.
	 *
	 * @param sink[in] The listener to be removed.
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#revokeWorkspaceSink(org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink)
	 */
	public void revokeWorkspaceSink(IWorkspaceEventsSink sink)
	{
		m_WSEventManager.removeListener(sink); 
	}

   /**
	 *
	 * Registers the passed in event sink with this dispatcher.
	 *
	 * @param sink The actual sink that will recieve notifications.
	 * @param cookie The unique identifier to be used when removing the listener.
	 *
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#registerForWSProjectEvents(org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink)
	 * 
	 */
   public void registerForWSProjectEvents(IWSProjectEventsSink handler)
   {
		m_WSProjectEventManager.addListener(handler, null);      
   }

	/**
	 * Removes a listener from the current list.
	 *
	 * @param sink[in] The listener to be removed.
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#revokeWSProjectSink(org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink)
	 */
	public void revokeWSProjectSink(IWSProjectEventsSink sink)
	{
		m_WSProjectEventManager.removeListener(sink); 
	}
	
   /**
	 *
	 * Registers the passed in event sink with this dispatcher.
	 *
	 * @param sink The actual sink that will recieve notifications.
	 * @param cookie The unique identifier to be used when removing the listener.
	 *
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#registerForWSElementEvents(org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink)
	 * 
	 */
   public void registerForWSElementEvents(IWSElementEventsSink sink)
   {
		m_WSProjElementEventManager.addListener( sink, null );
   }

	/**
	 * Removes a listener from the current list.
	 *
	 * @param sink[in] The listener to be removed.
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#revokeWSElementSink(org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink)
	 */
	public void revokeWSElementSink(IWSElementEventsSink sink)
	{
		m_WSProjElementEventManager.removeListener(sink);
	}
	
	/**
	 *
	 * Registers the passed in event sink with this dispatcher.
	 *
	 * @param sink The actual sink that will recieve notifications.
	 * @param cookie The unique identifier to be used when removing the listener.
	 *
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#registerForWSElementModifiedEvents(org.netbeans.modules.uml.core.workspacemanagement.IWSElementModifiedEventsSink)
	 * 
	 */
   public void registerForWSElementModifiedEvents(IWSElementModifiedEventsSink sink)
   {
		m_WSElementModEventManager.addListener( sink, null );
   }	

	/**
	 * Removes a listener from the current list.
	 *
	 * @param sink[in] The listener to be removed.
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#revokeWSElementModifiedSink(org.netbeans.modules.uml.core.workspacemanagement.IWSElementModifiedEventsSink)
	 */
	public void revokeWSElementModifiedSink(IWSElementModifiedEventsSink sink)
	{
		m_WSElementModEventManager.removeListener(sink);
	}

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWorkspacePreCreate(org.netbeans.modules.uml.core.workspacemanagement.IWorkspacePreCreateEventPayload, boolean)
    */
   public boolean fireWorkspacePreCreate(IWorkspacePreCreateEventPayload payLoad)
   {
		boolean proceed = true;
		
		if (validateEvent("WorkspacePreCreate", payLoad))
		{
			IResultCell cell = prepareResultCell(payLoad);
			EventFunctor workspacePreCreateFunc = new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink", 
                                                        "onWorkspacePreCreate");
			
			Object[] parms = new Object[2];
			parms[0] = payLoad;
			parms[1] = cell;
			workspacePreCreateFunc.setParameters(parms);
			m_WSEventManager.notifyListenersWithQualifiedProceed(workspacePreCreateFunc);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}
		}
		return proceed;
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWorkspaceCreated(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireWorkspaceCreated(IWorkspace space, IEventPayload payLoad)
   {
		if (validateEvent("WorkspaceCreated", space))
		{
			IResultCell cell = prepareResultCell(payLoad);
			EventFunctor workspaceCreatedFunc = new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink", 
                                            "onWorkspaceCreated");
			
			Object[] parms = new Object[2];
			parms[0] = space;
			parms[1] = cell;
			workspaceCreatedFunc.setParameters(parms);
			m_WSEventManager.notifyListeners(workspaceCreatedFunc);
		}
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWorkspacePreOpen(java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean fireWorkspacePreOpen(String fileName, IEventPayload payLoad)
   {
      boolean proceed = true;
      
      if (validateEvent("WorkspacePreOpen", fileName))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor workspacePreOpenFunc = new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink", 
                                "onWorkspacePreOpen");
         

         Object[] parms = new Object[2];
         parms[0] = fileName;
         parms[1] = cell;
			workspacePreOpenFunc.setParameters(parms);
			m_WSEventManager.notifyListenersWithQualifiedProceed(workspacePreOpenFunc);
         if (cell != null)
         {
            proceed = cell.canContinue();
         }
      }
      return proceed;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWorkspaceOpened(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireWorkspaceOpened(IWorkspace space, IEventPayload payLoad)
   {
      if (validateEvent("WorkspaceOpened", space))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor workspaceOpenedFunc = new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink", 
                                                     "onWorkspaceOpened");
         
         Object[] parms = new Object[2];
         parms[0] = space;
         parms[1] = cell;
         workspaceOpenedFunc.setParameters(parms);
			m_WSEventManager.notifyListeners(workspaceOpenedFunc);
      }      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWorkspacePreSave(java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean fireWorkspacePreSave(String location, IEventPayload payLoad)
   {
      boolean proceed = true;
      
      if (validateEvent("WorkspacePreSave", location))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor workspacePreSaveFunc = new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink", 
																		"onWorkspacePreSave");
         

         Object[] parms = new Object[2];
         parms[0] = location;
         parms[1] = cell;
         workspacePreSaveFunc.setParameters(parms);
         m_WSEventManager.notifyListenersWithQualifiedProceed(workspacePreSaveFunc);
         if (cell != null)
         {
            proceed = cell.canContinue();
         }
      }
      return proceed;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWorkspaceSaved(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireWorkspaceSaved(IWorkspace space, IEventPayload payLoad)
   {
      if (validateEvent("WorkspaceSaved", space))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor workspaceSavedFunc = new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink",
                                                    "onWorkspaceSaved");
         
         Object[] parms = new Object[2];
         parms[0] = space;
         parms[1] = cell;
         workspaceSavedFunc.setParameters(parms);
         m_WSEventManager.notifyListeners(workspaceSavedFunc);
      }
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWorkspacePreClose(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.eventframework.IEventPayload, boolean)
    */
   public boolean fireWorkspacePreClose(IWorkspace space, IEventPayload payLoad)
   {
      boolean proceed = true;
      
      //if (validateAndPrepareResultCell("WorkdspacePreClose", space, payLoad, cell))
      if(validateEvent("WorkdspacePreClose", space) == true)
      {
			IResultCell cell = prepareResultCell(payLoad);
         EventFunctor workspacePreCloseFunc = 
               new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink", 
                                "onWorkspacePreClose");
         

         Object[] parms = new Object[2];
         parms[0] = space;
         parms[1] = cell;
         workspacePreCloseFunc.setParameters(parms);
         m_WSEventManager.notifyListenersWithQualifiedProceed(workspacePreCloseFunc);
         if (cell != null)
         {
            proceed = cell.canContinue();
         }
      }
      return proceed;      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWorkspaceClosed(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireWorkspaceClosed(IWorkspace space, IEventPayload payLoad)
   {
      if (validateEvent("WorkspaceClosed", space))
      {
      	IResultCell cell = prepareResultCell(payLoad);
      	
         EventFunctor workspaceClosedFunc = new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink",
                                                     "onWorkspaceClosed");
         
         Object[] parms = new Object[2];
         parms[0] = space;
         parms[1] = cell;
         workspaceClosedFunc.setParameters(parms);
         m_WSEventManager.notifyListeners(workspaceClosedFunc);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSProjectPreCreate(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public IResultCell fireWSProjectPreCreate(IWorkspace space, String projectName, IEventPayload payLoad)
   {
		IResultCell retVal = null;
      
      ArrayList < Object > collection = new ArrayList < Object >();
      
      collection.add(space);
      collection.add(projectName);
      
      if (validateEvent("WSProjectPreCreate", collection) == true)
      {
      	retVal = prepareResultCell(payLoad);
      	
         EventFunctor wsProjectPreCreateFunc =
               new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink", 
                                "onWSProjectPreCreate");
         

         Object[] parms = new Object[3];
         parms[0] = space;
			parms[1] = projectName;
         parms[2] = retVal;
			wsProjectPreCreateFunc.setParameters(parms);
         m_WSProjectEventManager.notifyListenersWithQualifiedProceed(wsProjectPreCreateFunc);
         
//         if (retVal != null)
//         {
//            proceed = retVal.canContinue();
//         }
      }
      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSProjectCreated(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireWSProjectCreated(IWSProject project, IEventPayload payLoad)
   {
      if (validateEvent("WSProjectCreated", project))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsProjectCreatedFunc = new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink", 
                                                      "onWSProjectCreated");
         
         Object[] parms = new Object[2];
         parms[0] = project;
         parms[1] = cell;
         wsProjectCreatedFunc.setParameters(parms);
         m_WSProjectEventManager.notifyListeners(wsProjectCreatedFunc);
      }      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSProjectPreOpen(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean fireWSProjectPreOpen(IWorkspace project, 
                                       String projName, 
                                       IEventPayload payLoad)
   {
      boolean proceed = true;
      
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(project);
		collection.add(projName);
				
      if (validateEvent("WSProjectPreOpen", collection))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsProjectPreOpenFunc =
               new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink", 
                                "onWSProjectPreOpen");
         

         Object[] parms = new Object[3];
         parms[0] = project;
			parms[1] = projName;
         parms[2] = cell;
			wsProjectPreOpenFunc.setParameters(parms);
         m_WSProjectEventManager.notifyListenersWithQualifiedProceed(wsProjectPreOpenFunc);
         if (cell != null)
         {
            proceed = cell.canContinue();
         }
      }
      return proceed;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSProjectOpened(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireWSProjectOpened(IWSProject project, IEventPayload payLoad)
   {
      if (validateEvent("WSProjectOpened", project))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsProjectOpenedFunc = new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink", 
                                                     "onWSProjectOpened");
         
         Object[] parms = new Object[2];
         parms[0] = project;
         parms[1] = cell;
         wsProjectOpenedFunc.setParameters(parms);
         m_WSProjectEventManager.notifyListeners(wsProjectOpenedFunc);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSProjectPreRemove(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean fireWSProjectPreRemove(IWSProject project, IEventPayload payLoad)
   {
      boolean proceed = true;
      if (validateEvent("WSProjectPreRemove", project))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsProjectPreRemovedFunc =
               new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink", 
                                "onWSProjectPreRemove");
         

         Object[] parms = new Object[2];
         parms[0] = project;
         parms[1] = cell;
			wsProjectPreRemovedFunc.setParameters(parms);
			m_WSProjectEventManager.notifyListenersWithQualifiedProceed(wsProjectPreRemovedFunc);
         if (cell != null)
         {
            proceed = cell.canContinue();
         }
      }
      return proceed;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSProjectRemoved(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireWSProjectRemoved(IWSProject project, IEventPayload payLoad)
   {
      if (validateEvent("WSProjectRemoved", project))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsProjectRemovedFunc = new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink", 
                                                      "onWSProjectRemoved");
         
         Object[] parms = new Object[2];
         parms[0] = project;
         parms[1] = cell;
         wsProjectRemovedFunc.setParameters(parms);
         m_WSProjectEventManager.notifyListeners(wsProjectRemovedFunc);
      }
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSProjectPreInsert(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean fireWSProjectPreInsert(IWorkspace space, String projectName, IEventPayload payLoad)
   {
      boolean proceed = true;
      
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(space	);
		collection.add(projectName);
				
      if (validateEvent("WSProjectPreInsert", collection))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsProjectPreInsertFunc =
               new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink", 
                                "onWSProjectPreInsert");
         

         Object[] parms = new Object[3];
         parms[0] = space;
			parms[1] = projectName;
         parms[2] = cell;
         wsProjectPreInsertFunc.setParameters(parms);
			m_WSProjectEventManager.notifyListenersWithQualifiedProceed(wsProjectPreInsertFunc);
         if (cell != null)
         {
            proceed = cell.canContinue();
         }
      }
      return proceed;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSProjectInserted(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireWSProjectInserted(IWSProject project, IEventPayload payLoad)
   {
      if (validateEvent("WSProjectInserted", project))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsProjectInsertedFunc = new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink", 
                                                       "onWSProjectInserted");
         
         Object[] parms = new Object[2];
         parms[0] = project;
         parms[1] = cell;
         wsProjectInsertedFunc.setParameters(parms);
         m_WSProjectEventManager.notifyListeners(wsProjectInsertedFunc);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSProjectPreRename(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean fireWSProjectPreRename(IWSProject project, String newName, IEventPayload payLoad)
   {
      boolean proceed = true;
      
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(project);
		collection.add(newName);
				
      if (validateEvent("WSProjectPreRename", collection))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsProjectPreRenameFunc =
               new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink",
                                "onWSProjectPreRename");
         

         Object[] parms = new Object[3];
         parms[0] = project;
         parms[1] = newName;
         parms[2] = cell;
         wsProjectPreRenameFunc.setParameters(parms);
			m_WSProjectEventManager.notifyListenersWithQualifiedProceed(wsProjectPreRenameFunc);
         if (cell != null)
         {
            proceed = cell.canContinue();
         }
      }
      return proceed;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSProjectRenamed(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireWSProjectRenamed(IWSProject project, String oldName, IEventPayload payLoad)
   {
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(project);
		collection.add(oldName);
				
      if (validateEvent("WSProjectRenamed", collection))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsProjectRenamedFunc = new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink", 
                                                      "onWSProjectRenamed");
         
         Object[] parms = new Object[3];
         parms[0] = project;
         parms[1] = oldName;
         parms[2] = cell;
         wsProjectRenamedFunc.setParameters(parms);
			m_WSProjectEventManager.notifyListeners(wsProjectRenamedFunc);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSProjectPreClose(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean fireWSProjectPreClose(IWSProject project, IEventPayload payLoad)
   {
      boolean proceed = true;
      if (validateEvent("WSProjectPreClose", project))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsProjectPreCloseFunc =
               new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink",
                                "onWSProjectPreClose");
         

         Object[] parms = new Object[2];
         parms[0] = project;
         parms[1] = cell;
         wsProjectPreCloseFunc.setParameters(parms);
         m_WSProjectEventManager.notifyListenersWithQualifiedProceed(wsProjectPreCloseFunc);
         if (cell != null)
         {
            proceed = cell.canContinue();
         }
      }
      return proceed;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSProjectClosed(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireWSProjectClosed(IWSProject project, IEventPayload payLoad)
   {
      if (validateEvent("WSProjectClosed", project))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsProjectClosedFunc = new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink",
                                                     "onWSProjectClosed");
         
         Object[] parms = new Object[2];
         parms[0] = project;
         parms[1] = cell;
         wsProjectClosedFunc.setParameters(parms);
         m_WSProjectEventManager.notifyListeners(wsProjectClosedFunc);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSProjectPreSave(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean fireWSProjectPreSave(IWSProject project, IEventPayload payLoad)
   {
      boolean proceed = true;
      if (validateEvent("WSProjectPreSave", project))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsProjectPreSaveFunc =
               new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink", 
                                "onWSProjectPreSave");
         

         Object[] parms = new Object[2];
         parms[0] = project;
         parms[1] = cell;
         wsProjectPreSaveFunc.setParameters(parms);
         m_WSProjectEventManager.notifyListenersWithQualifiedProceed(wsProjectPreSaveFunc);
         if (cell != null)
         {
            proceed = cell.canContinue();
         }
      }
      return proceed;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSProjectSaved(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireWSProjectSaved(IWSProject project, IEventPayload payLoad)
   {
      if (validateEvent("WSProjectSaved", project))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsProjectSavedFunc = new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink", 
                                                    "onWSProjectSaved");
         
         Object[] parms = new Object[2];
         parms[0] = project;
         parms[1] = cell;
         wsProjectSavedFunc.setParameters(parms);
         m_WSProjectEventManager.notifyListeners(wsProjectSavedFunc);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSElementPreCreate(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, java.lang.String, java.lang.String, java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean fireWSElementPreCreate(IWSProject wsProject,
                                         String location, 
                                         String Name, 
                                         String data, 
                                         IEventPayload payLoad)
   {
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(wsProject);
		collection.add(location);
		collection.add(Name);
		collection.add(data);
		
		boolean proceed = true;
      if (validateEvent("WSElementPreCreate", collection))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsElementPreCreateFunc =
               new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink", 
                                "onWSElementPreCreate");
         

         Object[] parms = new Object[5];
         parms[0] = wsProject;
			parms[1] = location;
			parms[2] = Name;
			parms[3] = data;
         parms[4] = cell;
         wsElementPreCreateFunc.setParameters(parms);
         m_WSProjElementEventManager.notifyListenersWithQualifiedProceed(wsElementPreCreateFunc);
         if (cell != null)
         {
            proceed = cell.canContinue();
         }
      }
      return proceed;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSElementCreated(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireWSElementCreated(IWSElement element, IEventPayload payLoad)
   {
      if (validateEvent("WSElementCreated", element))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsElementCreatdFunc = new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink", 
                                                     "onWSElementCreated");
         
         Object[] parms = new Object[2];
         parms[0] = element;
         parms[1] = cell;
         wsElementCreatdFunc.setParameters(parms);
			m_WSProjElementEventManager.notifyListeners(wsElementCreatdFunc);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSElementPreSave(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean fireWSElementPreSave(IWSElement wsProject, IEventPayload payLoad)
   {
     boolean proceed = true;
   if (validateEvent("WSElementPreSave", wsProject))
   {
   	IResultCell cell = prepareResultCell(payLoad);
      EventFunctor wsElementPreSaveFunc =
            new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink", 
                             "onWSElementPreSave");
      

      Object[] parms = new Object[2];
      parms[0] = wsProject;
      parms[1] = cell;
      wsElementPreSaveFunc.setParameters(parms);
      m_WSProjElementEventManager.notifyListenersWithQualifiedProceed(wsElementPreSaveFunc);
      if (cell != null)
      {
         proceed = cell.canContinue();
      }
   }
   return proceed;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSElementSaved(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireWSElementSaved(IWSElement element, IEventPayload payLoad)
   {
      if (validateEvent("WSElementSaved", element))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsElementSavedFunc = new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink", 
                                                    "onWSElementSaved");
         
         Object[] parms = new Object[2];
         parms[0] = element;
         parms[1] = cell;
         wsElementSavedFunc.setParameters(parms);
         m_WSProjElementEventManager.notifyListeners(wsElementSavedFunc);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSElementPreRemove(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean fireWSElementPreRemove(IWSElement wsProject, IEventPayload payLoad)
   {
      boolean proceed = true;
      if (validateEvent("WSElementPreRemove", wsProject))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsElementPreRemoveFunc =
               new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink", 
                                "onWSElementPreRemove");
         

         Object[] parms = new Object[2];
         parms[0] = wsProject;
         parms[1] = cell;
         wsElementPreRemoveFunc.setParameters(parms);
         m_WSProjElementEventManager.notifyListenersWithQualifiedProceed(wsElementPreRemoveFunc);
         if (cell != null)
         {
            proceed = cell.canContinue();
         }
      }
      return proceed;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSElementRemoved(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireWSElementRemoved(IWSElement element, IEventPayload payLoad)
   {      
   	if (validateEvent("WSElementRemoved", element))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsElementRemovedFunc = new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink", 
                                                      "onWSElementRemoved");
         
         Object[] parms = new Object[2];
         parms[0] = element;
         parms[1] = cell;
         wsElementRemovedFunc.setParameters(parms);
			m_WSProjElementEventManager.notifyListeners(wsElementRemovedFunc);
      }  
   }
   	
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSElementPreNameChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean fireWSElementPreNameChanged(IWSElement element, 
                                              String proposedValue, 
                                              IEventPayload payLoad)
   {
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(element);
		collection.add(proposedValue);
		
      boolean proceed = true;
      if (validateEvent("WSElementPreNameChanged", collection))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsElementPreNameChangedFunc =
               new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink", 
                                "onWSElementPreNameChanged");
         

         Object[] parms = new Object[3];
         parms[0] = element;
         parms[1] = proposedValue;
			parms[2] = cell;
         wsElementPreNameChangedFunc.setParameters(parms);
         m_WSProjElementEventManager.notifyListenersWithQualifiedProceed(wsElementPreNameChangedFunc);
         if (cell != null)
         {
            proceed = cell.canContinue();
         }
      }
      return proceed;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSElementNameChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireWSElementNameChanged(IWSElement element, IEventPayload payLoad)
   {
      if (validateEvent("WSElementNameChanged", element))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsElementNameChangedFunc = new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink", 
                                                          "onWSElementNameChanged");
         
         Object[] parms = new Object[2];
         parms[0] = element;
         parms[1] = cell;
         wsElementNameChangedFunc.setParameters(parms);
         m_WSProjElementEventManager.notifyListeners(wsElementNameChangedFunc);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSElementPreOwnerChange(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean fireWSElementPreOwnerChange(IWSElement element, 
                                              IWSProject newOwner, 
                                              IEventPayload payLoad)
   {
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(element);
		collection.add(newOwner);
	
		boolean proceed = true;
      if (validateEvent("WSElementPreOwnerChange", collection))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsElementPreOwnerChangeFunc =
               new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink", 
                                "onWSElementPreOwnerChange");
         

         Object[] parms = new Object[3];
         parms[0] = element;
         parms[1] = newOwner;
         parms[2] = cell;
         wsElementPreOwnerChangeFunc.setParameters(parms);
			m_WSProjElementEventManager.notifyListenersWithQualifiedProceed(wsElementPreOwnerChangeFunc);
         if (cell != null)
         {
            proceed = cell.canContinue();
         }
      }
      return proceed;	
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSElementOwnerChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireWSElementOwnerChanged(IWSElement element, IEventPayload payLoad)
   {
      if (validateEvent("WSElementOwnerChanged", element))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsElementOwnerChangedFunc = new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink", 
                                                           "onWSElementOwnerChanged");
         
         Object[] parms = new Object[2];
         parms[0] = element;
         parms[1] = cell;
         wsElementOwnerChangedFunc.setParameters(parms);
			m_WSProjElementEventManager.notifyListeners(wsElementOwnerChangedFunc);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSElementPreLocationChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean fireWSElementPreLocationChanged(IWSElement element, 
                                                  String proposedLocation, 
                                                  IEventPayload payLoad)
   {
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(element);
		collection.add(proposedLocation);
		
      boolean proceed = true;
      if (validateEvent("WSElementPreLocationChanged", collection))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsElementPreLocationChangedFunc =
               new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink", 
                                "onWSElementPreLocationChanged");
         

         Object[] parms = new Object[3];
         parms[0] = element;
         parms[1] = proposedLocation;
			parms[2] = cell;
         wsElementPreLocationChangedFunc.setParameters(parms);
         m_WSProjElementEventManager.notifyListenersWithQualifiedProceed(wsElementPreLocationChangedFunc);
         if (cell != null)
         {
            proceed = cell.canContinue();
         }
      }
      return proceed;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSElementLocationChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireWSElementLocationChanged(IWSElement element, IEventPayload payLoad)
   {
      if (validateEvent("WSElementLocationChanged", element))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsElementLocationChangedFunc = new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink", 
                                                              "onWSElementLocationChanged");
         
         Object[] parms = new Object[2];
         parms[0] = element;
         parms[1] = cell;
         wsElementLocationChangedFunc.setParameters(parms);
			m_WSProjElementEventManager.notifyListeners(wsElementLocationChangedFunc);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSElementPreDataChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean fireWSElementPreDataChanged(IWSElement element, 
                                              String newData, 
                                              IEventPayload payLoad)
   {
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(element);
		collection.add(newData);
		
		boolean proceed = true;
      if (validateEvent("WSElementPreDataChanged", collection))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsElemetnPreDataChangedFunc =
               new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink", 
                                "onWSElementPreDataChanged");
         

         Object[] parms = new Object[3];
         parms[0] = element;
         parms[1] = newData;
			parms[2] = cell;
         wsElemetnPreDataChangedFunc.setParameters(parms);
         m_WSProjElementEventManager.notifyListenersWithQualifiedProceed(wsElemetnPreDataChangedFunc);
         if (cell != null)
         {
            proceed = cell.canContinue();
         }
      }
      return proceed;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSElementDataChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireWSElementDataChanged(IWSElement element, IEventPayload payLoad)
   {
      if (validateEvent("WSElementDataChanged", element))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsElementDataChangedFunc = new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink", 
                                                          "onWSElementDataChanged");
         
         Object[] parms = new Object[2];
         parms[0] = element;
         parms[1] = cell;
         wsElementDataChangedFunc.setParameters(parms);
         m_WSProjElementEventManager.notifyListeners(wsElementDataChangedFunc);
      }                            
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSElementPreDocChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean fireWSElementPreDocChanged(IWSElement element, 
                                             String doc, 
                                             IEventPayload payLoad)
   {
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(element);
		collection.add(doc);
		
		boolean proceed = true;
      if (validateEvent("WSElementPreDocChanged", collection))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsElemetnPreDocChangedFunc =
               new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink", 
                                "onWSElementPreDocChanged");
         

         Object[] parms = new Object[3];
         parms[0] = element;
         parms[1] = doc;
			parms[2] = cell;
         wsElemetnPreDocChangedFunc.setParameters(parms);
         m_WSProjElementEventManager.notifyListenersWithQualifiedProceed(wsElemetnPreDocChangedFunc);
         if (cell != null)
         {
            proceed = cell.canContinue();
         }
      }
      return proceed;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSElementDocChanged(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireWSElementDocChanged(IWSElement element, IEventPayload payLoad)
   {
      if (validateEvent("WSElementDocChanged", element))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsElementDocChangedFunc = new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink", 
                                                         "onWSElementDocChanged");
         
         Object[] parms = new Object[2];
         parms[0] = element;
         parms[1] = cell;
         wsElementDocChangedFunc.setParameters(parms);
         m_WSProjElementEventManager.notifyListeners(wsElementDocChangedFunc);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSElementPreModify(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean fireWSElementPreModify(IWSElement wsProject, IEventPayload payLoad)
   {
      boolean proceed = true;
      if (validateEvent("WSElementPreModify", wsProject))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsElementPreModifyFunc =
               new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSElementModifiedEventsSink", 
                                "onWSElementPreModify");
        

         Object[] parms = new Object[2];
         parms[0] = wsProject;
         parms[1] = cell;
         wsElementPreModifyFunc.setParameters(parms);
			m_WSElementModEventManager.notifyListenersWithQualifiedProceed(wsElementPreModifyFunc);
         if (cell != null)
         {
            proceed = cell.canContinue();
         }
      }
      return proceed;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher#fireWSElementModified(org.netbeans.modules.uml.core.workspacemanagement.IWSElement, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireWSElementModified(IWSElement element, IEventPayload payLoad)
   {
      if (validateEvent("WSElementModified", element))
      {
      	IResultCell cell = prepareResultCell(payLoad);
         EventFunctor wsElementModifiedFunc = new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSElementModifiedEventsSink", 
                                                       "onWSElementModified");
         
         Object[] parms = new Object[2];
         parms[0] = element;
         parms[1] = cell;
         wsElementModifiedFunc.setParameters(parms);
         m_WSElementModEventManager.notifyListeners(wsElementModifiedFunc);
      }
   }
   
   /**
	*
	* Dispatches the OnWSElementPreAliasChanged event.
	*
	* @param element[in] The WSElement that is about to be removed.
	* @param proposedValue[in] The new alias of the element.
	* @param payload[in] The payload to deliver with the event.
	* @param proceed[out] true if the event was fully dispatched, else
	*                     false if a listener cancelled full dispatch.
	*
	* @return HRESULT
	* 
	*/
   public boolean fireWSElementPreAliasChanged(IWSElement element, String newVal, IEventPayload payload)
   {
		boolean proceed = true;
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(element);
		collection.add(newVal);
		
	  	if (validateEvent("WSElementPreAliasChanged", collection))
	  	{
			IResultCell cell = prepareResultCell(payload);
		 	EventFunctor wsElementPreAliasChangeFunc =
			   		new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink", 
								"onWSElementPreAliasChanged");
		 	
	
			 Object[] parms = new Object[3];
			 parms[0] = element;
			 parms[1] = newVal;
			 parms[2] = cell;
			 wsElementPreAliasChangeFunc.setParameters(parms);
		 	 m_WSProjElementEventManager.notifyListenersWithQualifiedProceed(wsElementPreAliasChangeFunc);
		 	 if (cell != null)
		 	 {
				proceed = cell.canContinue();
		 	 }
	  	}
   	
   		return proceed;
   }
   
   /**
	*
	* Dispatches the OnWSElementAliasChanged event.
	*
	* @param element[in] The WSElement whose alias just changed.
	* @param payload[in] The EventPayload to include with the event.
	*
	* @return HRESULT
	* 
	*/
   public void fireWSElementAliasChanged(IWSElement element, IEventPayload payload)
   {
	if (validateEvent("WSElementAliasChanged", element))
	{
	  IResultCell cell = prepareResultCell(payload);
	   EventFunctor wsElementAliasChangeFunc = new EventFunctor("org.netbeans.modules.uml.core.workspacemanagement.IWSElementEventsSink", 
													 "onWSElementAliasChanged");
	   
	   Object[] parms = new Object[2];
	   parms[0] = element;
	   parms[1] = cell;
	   wsElementAliasChangeFunc.setParameters(parms);
	   m_WSProjElementEventManager.notifyListeners(wsElementAliasChangeFunc);
	}
   }
   
}


