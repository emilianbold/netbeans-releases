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



package org.netbeans.modules.uml.ui.controls.filter;

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

/**
 * 
 * @author Trey Spiva
 */
public class ProjectTreeFilterDialogEventsAdapter
   implements IProjectTreeFilterDialogEventsSink
{

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.filter.IProjectTreeFilterDialogEventsSink#onProjectTreeFilterDialogInit(org.netbeans.modules.uml.ui.controls.filter.IFilterDialog, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onProjectTreeFilterDialogInit(IFilterDialog dialog,
                                             IResultCell cell)
   {
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.filter.IProjectTreeFilterDialogEventsSink#onProjectTreeFilterDialogOKActivated(org.netbeans.modules.uml.ui.controls.filter.IFilterDialog, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onProjectTreeFilterDialogOKActivated(IFilterDialog dialog,
                                                    IResultCell cell)
   {
//      if (dialog != null)
//      {
//      	IProjectTreeEngine pEngine = null;
//      	IProjectTreeModel model = dialog.getProjectTreeModel();
//      	if (model instanceof ProjectTreeSwingModel)
//      	{
//      		ProductProjectTreeModel p = (ProductProjectTreeModel)model;
//				pEngine = p.getFirstEngine();
//      	}
//      	else if (model instanceof DesignCenterSwingModel)
//      	{
//				DesignCenterSwingModel m = (DesignCenterSwingModel)model;
//				pEngine = m.getFirstEngine();
//      	}
//			if (pEngine != null)
//			{
//				if (pEngine instanceof ADProjectTreeEngine)
//				{
//					ADProjectTreeEngine pEng = (ADProjectTreeEngine)pEngine;
//					FilteredItemManager mgr = pEng.getFilterManager();
//					if (mgr != null)
//					{
//						mgr.saveToRegistry();
//						// refresh the project tree because some things may appear to be a top-level
//						// (relationships) when they really are listed in folders under their respected
//						// elements
//						IProjectTreeControl pControl = ProductHelper.getProjectTree();
//						if (pControl != null)
//						{
//							pControl.refresh(true);
//						}
//						IProjectTreeControl pControl2 = ProductHelper.getDesignCenterTree();
//						if (pControl2 != null)
//						{
//							pControl2.refresh(true);
//						}
//					}
//				}
//			}
//   	}
   }

}
