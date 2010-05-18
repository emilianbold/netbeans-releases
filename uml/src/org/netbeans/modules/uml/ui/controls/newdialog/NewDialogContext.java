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


/*
 * Created on Jul 15, 2003
 *
 */
package org.netbeans.modules.uml.ui.controls.newdialog;

import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductProjectManager;

/**
 * @author sumitabhk
 *
 */
public class NewDialogContext implements INewDialogContext
{
	public static IWorkspace s_pWorkspace = null;
	public static String s_sProjectLocation = null;
	public static boolean s_bUseAllProjectExtensions = false;
	public static IProjectTreeControl s_pTreeControl = null;
	/**
	 * 
	 */
	public NewDialogContext()
	{
		super();
	}

	/**
	 * Sets / Gets the workspace that should be used for this instance of the new dialog.
	 *
	 * @param pVal [out,retval] The workspace that should be used for all new dialog operations.
	 */
	public IWorkspace getWorkspace()
	{
		IWorkspace retObj = null;
		if (s_pWorkspace != null)
		{
		   retObj = s_pWorkspace;
		}
		else
		{
		   // We have no override so just get the current workspace off the product.
		   retObj = ProductHelper.getWorkspace();
		}
		return retObj;
	}

	/**
	 * Sets / Gets the workspace that should be used for this instance of the new dialog.
	 *
	 * @param pVal [in] The workspace that should be used for all new dialog operations.
	 */
	public void setWorkspace(IWorkspace value)
	{
		s_pWorkspace = value;
	}

	/**
	 * Sets / Gets the project that should be used for this instance of the new dialog.
	 *
	 * @param pVal [out,retval] The project that should be used for all new dialog operations.
	 */
	public IProject getProject()
	{
		IProject retObj = null;
		if (s_sProjectLocation != null && s_sProjectLocation.length() > 0)
		{
			IApplication pApp = ProductHelper.getApplication();
			if (pApp != null)
			{
				retObj = pApp.getProjectByFileName(s_sProjectLocation);
			}
		}
		
		if (retObj == null)
		{
			// We have no override so just get the current project project manager
			IProductProjectManager pMan = ProductHelper.getProductProjectManager();
			if (pMan != null)
			{
				retObj = pMan.getCurrentProject();
			}
		}
		
		return retObj;
	}

	/**
	 * Sets / Gets the project that should be used for this instance of the new dialog.
	 *
	 * @param pVal [in] The project that should be used for all new dialog operations.
	 */
	public void setProject(IProject value)
	{
		s_sProjectLocation = null;
		if (value != null)
		{
			s_sProjectLocation = value.getFileName();
		}
	}

	/**
	 * TRUE to use all extensions when getting the number of open projects.
	 */
	public boolean getUseAllProjectExtensions()
	{
		return s_bUseAllProjectExtensions;
	}

	/**
	 * TRUE to use all extensions when getting the number of open projects.
	 */
	public void setUseAllProjectExtensions(boolean value)
	{
		s_bUseAllProjectExtensions = value;
	}

	/**
	 * Returns the number of open projects.
	 *
	 * @param pNumOpen [out,retval] The number of open projects
	 */
	public int getNumOpenProjects()
	{
		ProductHelper.getNumOpenProjects(s_bUseAllProjectExtensions);
		return 0;
	}

	/**
	 * Sets / Gets the project tree that should be used for this instance of the new dialog.
	 *
	 * @param pVal [out,retval] The workspace that should be used for all new dialog operations.
	 */
	public IProjectTreeControl getProjectTree()
	{
		IProjectTreeControl retObj = null;
		if (s_pTreeControl != null)
		{
			retObj = s_pTreeControl;
		}
		else
		{
			// We have no override so just get the current project tree off the product.
			retObj = ProductHelper.getProjectTree();
		}
		return retObj;
	}

	/**
	 * Sets / Gets the project tree that should be used for this instance of the new dialog.
	 *
	 * @param pVal [in] The workspace that should be used for all new dialog operations.
	 */
	public void setProjectTree( IProjectTreeControl value )
	{
		s_pTreeControl = value;
	}
}




