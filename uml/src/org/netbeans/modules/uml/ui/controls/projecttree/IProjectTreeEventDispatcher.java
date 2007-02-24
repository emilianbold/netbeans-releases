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

import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;

/**
 * 
 * @author Trey Spiva
 */
public interface IProjectTreeEventDispatcher extends IEventDispatcher
{
   /**
    * 
   */
   public void registerProjectTreeContextMenuEvents(IProjectTreeContextMenuEventsSink handler);

   /**
    * 
    */
   public void revokeProjectTreeContextMenuSink(IProjectTreeContextMenuEventsSink handler);

   /**
    * 
    */
   public void registerProjectTreeEvents(IProjectTreeEventsSink handler);

   /**
    * 
    */
   public void revokeProjectTreeSink(IProjectTreeEventsSink handler);
   /**
    * 
    */
   public void fireProjectTreeContextMenuPrepare(IProjectTreeControl pParentControl,
                                                 IProductContextMenu contextMenu,
                                                 IEventPayload payload);

   /**
    * 
    */
   public void fireProjectTreeContextMenuPrepared(IProjectTreeControl pParentControl,
                                                  IProductContextMenu contextMenu,
                                                  IEventPayload payload);

   /**
    * 
    */
   public void fireProjectTreeContextMenuHandleDisplay(IProjectTreeControl pParentControl,
                                                       IProductContextMenu contextMenu,
                                                       IEventPayload payload);

   /**
    * 
    */
   public void fireProjectTreeContextMenuSelected(IProjectTreeControl pParentControl,
                                                  IProductContextMenu contextMenu,
                                                  IProductContextMenuItem selectedItem,
                                                  IEventPayload payload);

   /**
    * A project node is being expanded
    */
   public void fireItemExpanding(IProjectTreeControl          pParentControl,
	                              IProjectTreeExpandingContext context,
                                 IEventPayload                payload);

   /**
    * A project node is being expanded.  The expanded item will be populated
    * with only the items specified by the filter manager.
    */
   public void fireItemExpandingWithFilter(IProjectTreeControl          pParentControl,
	                                        IProjectTreeExpandingContext context,
                                           FilteredItemManager          manager,
                                           IEventPayload                payload);
   /**
    * A project node is about to be edited
    */
   public void fireBeforeEdit(IProjectTreeControl    pParentControl,
                              IProjectTreeItem       pItem,
                              IProjectTreeEditVerify pVerify,
                              IEventPayload          payload);

   /**
    * A project node has been edited
    */
   public void fireAfterEdit(IProjectTreeControl pParentControl,
                             IProjectTreeItem pItem,
                             IProjectTreeEditVerify pVerify,
                             IEventPayload payload);

   /**
    * A project node has been double clicked on
    */
   public void fireDoubleClick(IProjectTreeControl pParentControl,
                               IProjectTreeItem    pItem, 
                               boolean             isControl, 
                               boolean             isShift, 
                               boolean             isAlt, 
                               boolean             isMeta,
                               IEventPayload       payload);

   /**
    * The tree's selection has changed
    */
   public void fireSelChanged(IProjectTreeControl pParentControl,
                              IProjectTreeItem[] pItem,
                              IEventPayload payload);

   /**
    * A project node has been right clicked on
    */
   public void fireRightButtonDown(IProjectTreeControl pParentControl,
                                   IProjectTreeItem pItem,
                                   IProjectTreeHandled pHandled,
                                   int nScreenLocX,
                                   int nScreenLocY,
                                   IEventPayload payload);

   /**
    * A project node in beginning a drag operation
    */
   public void fireBeginDrag(IProjectTreeControl pParentControl,
                             IProjectTreeItem[] pItem,
                             IProjectTreeDragVerify pVerify,
                             IEventPayload payload);

   /**
    * A dataobject is proposed for dropping
    */
   public void fireMoveDrag(IProjectTreeControl pParentControl,
                            Transferable pItem,
                            IProjectTreeDragVerify pVerify,
                            IEventPayload payload);

   /**
    * A dataobject has been dropped
    */
   public void fireEndDrag(IProjectTreeControl    pParentControl,
                           Transferable           pItem,
                           int                    action,
                           IProjectTreeDragVerify pVerify,
                           IEventPayload          payload);
}
