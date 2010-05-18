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



