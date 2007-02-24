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


package org.netbeans.modules.uml.ui.support.applicationmanager;

import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.ui.controls.newdialog.INewDialogProjectDetails;
import java.util.ArrayList;

public interface IProductProjectManager
{
	/**
	 * Returns the currently active project.
	*/
	public IProject getCurrentProject();

        /**
         * Returns all of the open projects.
         */
        public ArrayList < IProject > getOpenProjects();

	/**
	 * Brings up the insert project dialog.
	*/
	public void displayInsertProjectDialog( IWorkspace pWorkspace );

	/**
	 * Brings up the new project dialog.
	*/
	public void displayNewProjectDialog();

	/**
	 * Brings up the new project dialog.
	*/
	public void displayNewProjectDialog( INewDialogProjectDetails pDetails );

}
