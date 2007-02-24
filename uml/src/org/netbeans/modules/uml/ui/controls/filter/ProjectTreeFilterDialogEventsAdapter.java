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



package org.netbeans.modules.uml.ui.controls.filter;

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEngine;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel;
import org.netbeans.modules.uml.ui.controls.projecttree.ProductProjectTreeModel;
import org.netbeans.modules.uml.ui.products.ad.projecttreedefaultengine.ADProjectTreeEngine;
import org.netbeans.modules.uml.ui.products.ad.projecttreedefaultengine.FilteredItemManager;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.swing.projecttree.ProjectTreeSwingModel;

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
