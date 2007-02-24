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

import org.netbeans.modules.uml.core.eventframework.EventDispatchHelper;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;
/**
 * @author sumitabhk
 *
 */
public class WorkspaceEventDispatchHelper extends EventDispatchHelper
	implements IWorkspaceEventDispatchHelper
{

	/**
	 *
	 */
	public WorkspaceEventDispatchHelper() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatchHelper#dispatchWorkspacePreCreate(java.lang.String, java.lang.String)
	 */
	public boolean dispatchWorkspacePreCreate(String fileName, String name) 
		throws InvalidArguments
	{
		boolean retVal = true;		
		
		if ((fileName.length() > 0) && (name.length() > 0))
      {
         IWorkspacePreCreateEventPayload payload =
            (IWorkspacePreCreateEventPayload)preparePayload("WorkspacePreCreate");
         if (getEventDispatcher() instanceof IWorkspaceEventDispatcher)
         {
            IWorkspaceEventDispatcher disp =
               (IWorkspaceEventDispatcher)getEventDispatcher();
            retVal = disp.fireWorkspacePreCreate(payload);
         }
      }
		else
		{
			throw new InvalidArguments();
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatchHelper#dispatchWorkspaceCreated(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace)
	 */
	public void dispatchWorkspaceCreated(IWorkspace space)
		throws InvalidArguments 
	{
		if (space != null)
      {
         IEventPayload payload = preparePayload("WorkspaceCreated");
         if (getEventDispatcher() instanceof IWorkspaceEventDispatcher)
         {
            IWorkspaceEventDispatcher disp = (IWorkspaceEventDispatcher)getEventDispatcher();
            disp.fireWorkspaceCreated(space, payload);
         }
      }
      else
      {
         throw new InvalidArguments();
      }
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatchHelper#dispatchWorkspacePreOpen(java.lang.String)
	 */
	public boolean dispatchWorkspacePreOpen(String fileName) 
		throws InvalidArguments
	{
		boolean retVal = true;
		
		if (fileName.length() > 0)
      {
         IEventPayload payload = preparePayload("WorkspacePreOpen");
         if (getEventDispatcher() instanceof IWorkspaceEventDispatcher)
         {
				IWorkspaceEventDispatcher disp = (IWorkspaceEventDispatcher)getEventDispatcher();
            retVal = disp.fireWorkspacePreOpen(fileName, payload);
         }
      }
      else
      {
         throw new InvalidArguments();
      }
		
		return retVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatchHelper#dispatchWorkspaceOpened(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace)
	 */
	public void dispatchWorkspaceOpened(IWorkspace space) 
		throws InvalidArguments
	{
		if (space != null)
      {
         IEventPayload payload = preparePayload("WorkspaceOpened");
         if (getEventDispatcher() instanceof IWorkspaceEventDispatcher)
         {
            IWorkspaceEventDispatcher disp = (IWorkspaceEventDispatcher)getEventDispatcher();
            disp.fireWorkspaceOpened(space, payload);
         }
      }
      else
      {
         throw new InvalidArguments();
      }
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatchHelper#dispatchWorkspacePreClose(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace)
	 */
	public boolean dispatchWorkspacePreClose(IWorkspace space) 
		throws InvalidArguments
	{
		boolean retVal = true;
		
		if (space != null)
      {
         IEventPayload payload = preparePayload("WorkspacePreClose");
         if (getEventDispatcher() instanceof IWorkspaceEventDispatcher)
         {
            IWorkspaceEventDispatcher disp = (IWorkspaceEventDispatcher)getEventDispatcher();
            retVal = disp.fireWorkspacePreClose(space, payload);
         }
      }
      else
      {
         throw new InvalidArguments();
      }
		
		return retVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatchHelper#dispatchWorkspaceClosed(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace)
	 */
	public void dispatchWorkspaceClosed(IWorkspace space) 
		throws InvalidArguments
	{
		if (space != null)
      {
         IEventPayload payload = preparePayload("WorkspaceClosed");
         if (getEventDispatcher() instanceof IWorkspaceEventDispatcher)
         {
            IWorkspaceEventDispatcher disp = (IWorkspaceEventDispatcher)getEventDispatcher();
            disp.fireWorkspaceClosed(space, payload);
         }
      }
      else
      {
         throw new InvalidArguments();
      }
	}

}



