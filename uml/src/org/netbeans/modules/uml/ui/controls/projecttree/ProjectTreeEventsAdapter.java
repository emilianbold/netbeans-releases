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


/*
 *
 * Created on Jun 10, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.controls.projecttree;

import org.netbeans.modules.uml.ui.products.ad.projecttreedefaultengine.FilteredItemManager;
import java.awt.datatransfer.Transferable;

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;


/**
 *
 * @author Trey Spiva
 */
public class ProjectTreeEventsAdapter implements IProjectTreeEventsSink
{

   /* (non-Javadoc)
    * @see com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeEventsSink#onBeforeEdit(com.embarcadero.describe.gui.axcontrols.axprojecttree.IAxProjectTreeControl, com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeItem, com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeEditVerify, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onBeforeEdit(IProjectTreeControl pParentControl,
                            IProjectTreeItem pItem,
                            IProjectTreeEditVerify pVerify,
                            IResultCell cell)
   {
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeEventsSink#onAfterEdit(com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeControl, com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeItem, com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeEditVerify, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onAfterEdit(IProjectTreeControl pParentControl,
                           IProjectTreeItem pItem,
                           IProjectTreeEditVerify pVerify,
                           IResultCell cell)
   {
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeEventsSink#onDoubleClick(com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeControl, com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeItem, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onDoubleClick(IProjectTreeControl pParentControl,
                             IProjectTreeItem    pItem, 
                             boolean             isControl, 
                             boolean             isShift, 
                             boolean             isAlt, 
                             boolean             isMeta,
                             IResultCell         cell)
   {
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeEventsSink#onSelChanged(com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeControl, com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeItems, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onSelChanged(IProjectTreeControl pParentControl,
	                         IProjectTreeItem[] pItem,
                            IResultCell cell)
   {
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeEventsSink#onRightButtonDown(com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeControl, com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeItem, com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeHandled, int, int, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onRightButtonDown(IProjectTreeControl pParentControl,
                                 IProjectTreeItem pItem,
                                 IProjectTreeHandled pHandled,
                                 int nScreenLocX,
                                 int nScreenLocY,
                                 IResultCell cell)
   {
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeEventsSink#onBeginDrag(com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeControl, com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeItems, com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeDragVerify, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onBeginDrag(IProjectTreeControl pParentControl,
	                        IProjectTreeItem[] pItem,
                           IProjectTreeDragVerify pVerify,
                           IResultCell cell)
   {
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeEventsSink#onMoveDrag(com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeControl, com.embarcadero.describe.gui.axcontrols.axprojecttree.IDataObject, com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeDragVerify, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onMoveDrag(IProjectTreeControl    pParentControl,
                          Transferable           pItem,
                          IProjectTreeDragVerify pVerify,
                          IResultCell            cell)
   {
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeEventsSink#onEndDrag(com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeControl, com.embarcadero.describe.gui.axcontrols.axprojecttree.IDataObject, com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeDragVerify, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onEndDrag(IProjectTreeControl    pParentControl,
                         Transferable           pItem,
                         int                    action,
                         IProjectTreeDragVerify pVerify,
                         IResultCell            cell)
   {
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onItemExpanding(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeExpandingContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onItemExpanding(IProjectTreeControl pParentControl, 
                               IProjectTreeExpandingContext pContext,
                               IResultCell cell)
   {
   }
   
   public void onItemExpandingWithFilter(IProjectTreeControl pParentControl,
            IProjectTreeExpandingContext pContext,
            FilteredItemManager manager, IResultCell cell)
   {
   }

}
