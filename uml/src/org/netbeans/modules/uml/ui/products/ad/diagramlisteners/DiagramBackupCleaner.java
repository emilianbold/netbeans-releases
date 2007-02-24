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


