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
