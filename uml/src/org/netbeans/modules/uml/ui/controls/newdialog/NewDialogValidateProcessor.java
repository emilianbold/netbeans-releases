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



package org.netbeans.modules.uml.ui.controls.newdialog;

import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.ui.support.NewDialogTabKind;

/**
 * @author sumitabhk
 *
 */
public class NewDialogValidateProcessor implements INewDialogValidateProcessor
{

	/**
	 *
	 */
	public NewDialogValidateProcessor()
	{
		super();
	}

	/**
	 * Makes sure the tabs are valid for this diagram
	 *
	 * @param pParentDialog [in] The parent dialog for this validator
	 */
	public long validateTabs(INewDialog pParentDialog)
	{
		// Get the workspace off our context
		INewDialogContext pContext = new NewDialogContext();
		IWorkspace space = pContext.getWorkspace();
		if (space == null)
		{
			// Only new workspace is valid
			pParentDialog.addTab(NewDialogTabKind.NDTK_NONE);
			pParentDialog.addTab(NewDialogTabKind.NWIK_NEW_WORKSPACE);
		}
		else
		{
			// See if a project is open
			int numOpenProject = pContext.getNumOpenProjects();
			if (numOpenProject == 0)
			{
				boolean isProjTab = false;
				boolean isWorkTab = false;
				isProjTab = pParentDialog.isTab(NewDialogTabKind.NWIK_NEW_PROJECT);
				isWorkTab = pParentDialog.isTab(NewDialogTabKind.NWIK_NEW_WORKSPACE);
				
				// Only new workspace and project is valid
				pParentDialog.addTab(NewDialogTabKind.NDTK_NONE);
				if (isWorkTab)
				{
					pParentDialog.addTab(NewDialogTabKind.NWIK_NEW_WORKSPACE);
				}
				if (isProjTab)
				{
					pParentDialog.addTab(NewDialogTabKind.NWIK_NEW_PROJECT);
				}

				// If neither were specified then allow both tabs
				if (!isWorkTab && !isProjTab)
				{
					pParentDialog.addTab(NewDialogTabKind.NWIK_NEW_WORKSPACE);
					pParentDialog.addTab(NewDialogTabKind.NWIK_NEW_PROJECT);
				}
			}
		}
		return 0;
	}

}



