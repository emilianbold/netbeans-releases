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
