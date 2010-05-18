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



package org.netbeans.modules.uml.ui.products.ad.diagramlisteners;

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;

/**
 * @author sumitabhk
 *
 */
public class DiagramBackupCleaner implements IDiagramBackupCleaner,
											 IWSProjectEventsSink
{

	/**
	 * 
	 */
	public DiagramBackupCleaner()
	{
		super();
		
		DispatchHelper helper = new DispatchHelper();
		helper.registerForWSProjectEvents(this);
	}

	/**
	 * Revokes the event sink from the drawing area dispatcher.
	 */
	public void revoke()
	{
		try
		{
			DispatchHelper helper = new DispatchHelper();
			helper.revokeWSProjectSink(this);
		}
		catch (InvalidArguments e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreCreate(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreCreate(IWorkspace space, String projectName, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectCreated(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectCreated(IWSProject project, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * When a WS project is opened we remove the files in the diagram backup directory.
	 *
	 * @parm space [in] Not Used
	 * @parm projName [in] The project that's about to be opened
	 * @parm cell [in] Not Used
	 */
	public void onWSProjectPreOpen(IWorkspace space, String projName, IResultCell cell)
	{
		if (projName != null && projName.length() > 0)
		{
			IProxyDiagramManager pDiaMan = ProxyDiagramManager.instance();
			pDiaMan.cleanDiagramBackupFolder(projName);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectOpened(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectOpened(IWSProject project, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreRemove(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreRemove(IWSProject project, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectRemoved(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectRemoved(IWSProject project, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreInsert(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreInsert(IWorkspace space, String projectName, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectInserted(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectInserted(IWSProject project, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreRename(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreRename(IWSProject project, String newName, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectRenamed(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectRenamed(IWSProject project, String oldName, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreClose(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreClose(IWSProject project, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectClosed(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectClosed(IWSProject project, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreSave(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectPreSave(IWSProject project, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectSaved(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onWSProjectSaved(IWSProject project, IResultCell cell)
	{
		// TODO Auto-generated method stub
		
	}

}


