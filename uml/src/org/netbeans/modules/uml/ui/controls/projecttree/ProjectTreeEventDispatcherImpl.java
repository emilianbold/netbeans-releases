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

import org.netbeans.modules.uml.core.eventframework.IEventContext;
import org.netbeans.modules.uml.core.eventframework.IEventFrameworkEventsSink;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.ui.products.ad.projecttreedefaultengine.FilteredItemManager;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;

import org.netbeans.modules.uml.core.eventframework.EventDispatcher;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.eventframework.EventManager;
import org.netbeans.modules.uml.core.eventframework.EventFunctor;

import java.awt.datatransfer.Transferable;
import java.util.ArrayList;

/**
 * 
 * @author Trey Spiva
 */
public class ProjectTreeEventDispatcherImpl extends EventDispatcher
   implements IProjectTreeEventDispatcher
{
	/** Handles the actual deployment of events to Workspace listeners. */
	private EventManager< IProjectTreeContextMenuEventsSink > m_ProjectTreeContextMenuEventManager = null;
	private EventManager< IProjectTreeEventsSink >            m_ProjectTreeEventManager = null;
	
	public ProjectTreeEventDispatcherImpl()
	{
		m_ProjectTreeContextMenuEventManager = new EventManager< IProjectTreeContextMenuEventsSink >();
		m_ProjectTreeEventManager = new EventManager< IProjectTreeEventsSink >();
	}
	
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventDispatcher#registerProjectTreeContextMenuEvents(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeContextMenuEventsSink)
    */
   public void registerProjectTreeContextMenuEvents(IProjectTreeContextMenuEventsSink handler)
   {
		m_ProjectTreeContextMenuEventManager.addListener(handler, null);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventDispatcher#revokeProjectTreeContextMenuSink(int)
    */
   public void revokeProjectTreeContextMenuSink(IProjectTreeContextMenuEventsSink handler)
   {
		m_ProjectTreeContextMenuEventManager.removeListener(handler);
   }
   
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.eventframework.IEventDispatcher#registerForEventFrameworkEvents(org.netbeans.modules.uml.core.eventframework.IEventFrameworkEventsSink)
	 */
	public void registerProjectTreeEvents(IProjectTreeEventsSink handler)
	{
		m_ProjectTreeEventManager.addListener(handler, null);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.eventframework.IEventDispatcher#revokeEventFrameworkSink(org.netbeans.modules.uml.core.eventframework.IEventFrameworkEventsSink)
	 */
	public void revokeProjectTreeSink(IProjectTreeEventsSink handler)
	{
		m_ProjectTreeEventManager.removeListener(handler);
	}

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventDispatcher#fireProjectTreeContextMenuPrepare(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireProjectTreeContextMenuPrepare(IProjectTreeControl pParentControl,
                                                 IProductContextMenu contextMenu,
                                                 IEventPayload payload)
   {
      boolean proceed = true;
      
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(pParentControl);
		collection.add(contextMenu);
				
      if (validateEvent("ProjectTreeContextMenuPrepare", collection))
      {
         IResultCell cell = prepareResultCell(payload);
         EventFunctor projectTreeContextMenuPrepareFunc =
               new EventFunctor("org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeContextMenuEventsSink", 
                                "onProjectTreeContextMenuPrepare");
         

         Object[] parms = new Object[3];
         parms[0] = pParentControl;
         parms[1] = contextMenu;
         parms[2] = cell;
         projectTreeContextMenuPrepareFunc.setParameters(parms);
         m_ProjectTreeContextMenuEventManager.notifyListenersWithQualifiedProceed(projectTreeContextMenuPrepareFunc);
         if (cell != null)
         {
            proceed = cell.canContinue();
         }
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventDispatcher#fireProjectTreeContextMenuPrepared(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireProjectTreeContextMenuPrepared(IProjectTreeControl pParentControl,
                                                  IProductContextMenu contextMenu,
                                                  IEventPayload payload)
   {
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(pParentControl);
		collection.add(contextMenu);
		
      if (validateEvent("ProjectTreeContextMenuPrepared", collection))
      {
         IResultCell cell = prepareResultCell(payload);
         EventFunctor projectTreeContextMenuPreparedFunc = new EventFunctor("org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeContextMenuEventsSink", 
                                                                    "onProjectTreeContextMenuPrepared");
         
         Object[] parms = new Object[3];
			parms[0] = pParentControl;
			parms[1] = contextMenu;
			parms[2] = cell;
         projectTreeContextMenuPreparedFunc.setParameters(parms);
         m_ProjectTreeContextMenuEventManager.notifyListeners(projectTreeContextMenuPreparedFunc);
      }                
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventDispatcher#fireProjectTreeContextMenuHandleDisplay(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireProjectTreeContextMenuHandleDisplay(IProjectTreeControl pParentControl,
                                                       IProductContextMenu contextMenu,
                                                       IEventPayload payload)
   {
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(pParentControl);
		collection.add(contextMenu);
      if (validateEvent("ProjectTreeContextMenuHandleDisplay", collection))
      {
         IResultCell cell = prepareResultCell(payload);
         EventFunctor projectTreeContextMenuHandleDisplayFunc = new EventFunctor("org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeContextMenuEventsSink",
                                                                         "onProjectTreeContextMenuHandleDisplay");
         
         Object[] parms = new Object[3];
			parms[0] = pParentControl;
			parms[1] = contextMenu;
			parms[2] = cell;
         projectTreeContextMenuHandleDisplayFunc.setParameters(parms);
			m_ProjectTreeContextMenuEventManager.notifyListeners(projectTreeContextMenuHandleDisplayFunc);
      }

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventDispatcher#fireProjectTreeContextMenuSelected(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireProjectTreeContextMenuSelected(IProjectTreeControl pParentControl,
                                                  IProductContextMenu contextMenu,
                                                  IProductContextMenuItem selectedItem,
                                                  IEventPayload payload)
   {
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(pParentControl);
		collection.add(contextMenu);
				
      if (validateEvent("ProjectTreeContextMenuSelected", collection))
      {
         IResultCell cell = prepareResultCell(payload);
         EventFunctor projectTreeContextMenuSelectedFunc = new EventFunctor("org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeContextMenuEventsSink",
                                                                         "onProjectTreeContextMenuSelected");
         
         Object[] parms = new Object[3];
			parms[0] = pParentControl;
			parms[1] = contextMenu;
			parms[2] = cell;
			projectTreeContextMenuSelectedFunc.setParameters(parms);
			m_ProjectTreeContextMenuEventManager.notifyListeners(projectTreeContextMenuSelectedFunc);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventDispatcher#fireItemExpanding(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
    public void fireItemExpanding(IProjectTreeControl pParentControl, 
            IProjectTreeExpandingContext context, 
            IEventPayload payload)
    {
        ArrayList<Object> collection = new ArrayList<Object>();
        collection.add(pParentControl);
        collection.add(context);

        if (validateEvent("ItemExpanding", collection)) 
        {
            IResultCell cell = prepareResultCell(payload);
            EventFunctor itemExpandingFunc = 
                    new EventFunctor("org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink", 
                    "onItemExpanding");

            Object[] parms = new Object[3];
            parms[0] = pParentControl;
            parms[1] = context;
            parms[2] = cell;
            itemExpandingFunc.setParameters(parms);
            m_ProjectTreeEventManager.notifyListeners(itemExpandingFunc);
        }
    }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventDispatcher#fireItemExpanding(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireItemExpandingWithFilter(IProjectTreeControl          pParentControl,
	                                        IProjectTreeExpandingContext context,
                                           FilteredItemManager          manager,
                                           IEventPayload                payload)
   {
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(pParentControl);
		collection.add(context);
		
      if (validateEvent("ItemExpanding", collection))
      {
         IResultCell cell = prepareResultCell(payload);
         EventFunctor itemExpandingFunc = new EventFunctor("org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink",
                                                           "onItemExpandingWithFilter");
         
         Object[] parms = new Object[4];
			parms[0] = pParentControl;
			parms[1] = context;
         parms[2] = manager;
			parms[3] = cell;
         itemExpandingFunc.setParameters(parms);
			m_ProjectTreeEventManager.notifyListeners(itemExpandingFunc);
      }

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventDispatcher#fireBeforeEdit(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEditVerify, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireBeforeEdit(IProjectTreeControl pParentControl,
                              IProjectTreeItem pItem,
                              IProjectTreeEditVerify pVerify,
                              IEventPayload payload)
   {
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(pParentControl);
		collection.add(pItem);
		collection.add(pVerify);
				
      if (validateEvent("BeforeEdit", collection))
      {
         IResultCell cell = prepareResultCell(payload);
         EventFunctor beforeEditFunc = new EventFunctor("org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink",
                                                "onBeforeEdit");
         
         Object[] parms = new Object[4];
         parms[0] = pParentControl;
         parms[1] = pItem;
			parms[0] = pVerify;
			parms[1] = cell;
         beforeEditFunc.setParameters(parms);
			m_ProjectTreeEventManager.notifyListeners(beforeEditFunc);
      }

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventDispatcher#fireAfterEdit(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEditVerify, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireAfterEdit(IProjectTreeControl pParentControl,
                             IProjectTreeItem pItem,
                             IProjectTreeEditVerify pVerify,
                             IEventPayload payload)
   {
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(pParentControl);
		collection.add(pItem);
		collection.add(pVerify);
		
      if (validateEvent("AfterEdit", collection))
      {
         IResultCell cell = prepareResultCell(payload);
         EventFunctor afterEditFunc = new EventFunctor("org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink", 
                                               "onAfterEdit");
         
         Object[] parms = new Object[4];
         parms[0] = pParentControl;
         parms[1] = pItem;
         parms[2] = pVerify;
         parms[3] = cell;
         afterEditFunc.setParameters(parms);
         m_ProjectTreeEventManager.notifyListeners(afterEditFunc);
      }

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventDispatcher#fireDoubleClick(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireDoubleClick(IProjectTreeControl pParentControl,
                               IProjectTreeItem    pItem, 
                               boolean             isControl, 
                               boolean             isShift, 
                               boolean             isAlt, 
                               boolean             isMeta,
                               IEventPayload payload)
   {
      Boolean isControlObj = new Boolean(isControl);
      Boolean isShiftObj = new Boolean(isShift);
      Boolean isAltObj = new Boolean(isAlt);
      Boolean isMetaObj = new Boolean(isMeta);
       
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(pParentControl);
		collection.add(isControlObj);
      collection.add(isShiftObj);
      collection.add(isAltObj);
      collection.add(isMetaObj);
      collection.add(pItem);
		
      if (validateEvent("DoubleClick", collection))
      {
         IResultCell cell = prepareResultCell(payload);
         EventFunctor doubleClickFunc = new EventFunctor("org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink", 
                                                 "onDoubleClick");
         
         Object[] parms = new Object[7];
			parms[0] = pParentControl;
			parms[1] = pItem;
         parms[2] = isControlObj;
         parms[3] = isShiftObj;
         parms[4] = isAltObj;
         parms[5] = isMetaObj;
         parms[6] = cell;
         doubleClickFunc.setParameters(parms);
			m_ProjectTreeEventManager.notifyListeners(doubleClickFunc);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventDispatcher#fireSelChanged(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem[], org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireSelChanged(IProjectTreeControl pParentControl,
                              IProjectTreeItem[] pItem,
                              IEventPayload payload)
   {
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(pParentControl);
		collection.add(pItem);
		
		if (validateEvent("SelChanged", collection))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor selChangedFunc = new EventFunctor("org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink",
				                                    "onSelChanged");
			
			Object[] parms = new Object[3];
			parms[0] = pParentControl;
			parms[1] = pItem;
			parms[2] = cell;
			selChangedFunc.setParameters(parms);
			m_ProjectTreeEventManager.notifyListeners(selChangedFunc);
		}

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventDispatcher#fireRightButtonDown(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeHandled, int, int, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireRightButtonDown(IProjectTreeControl pParentControl,
                                   IProjectTreeItem pItem,
                                   IProjectTreeHandled pHandled,
                                   int nScreenLocX,
                                   int nScreenLocY,
                                   IEventPayload payload)
   {
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(pParentControl);
		collection.add(pItem);
		collection.add(pHandled);
		collection.add(new Integer(nScreenLocX));
		collection.add(new Integer(nScreenLocY));

		if (validateEvent("RightButtonDown", collection))
      {
         IResultCell cell = prepareResultCell(payload);
         EventFunctor rightButtonDownFunc = new EventFunctor("org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink", 
                                                     "onRightButtonDown");
         
         Object[] parms = new Object[6];
         parms[0] = pParentControl;
         parms[1] = pItem;
			parms[2] = pHandled;
			parms[3] = new Integer(nScreenLocX);
			parms[4] = new Integer(nScreenLocY);
			parms[5] = cell;
         rightButtonDownFunc.setParameters(parms);
			m_ProjectTreeEventManager.notifyListeners(rightButtonDownFunc);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventDispatcher#fireBeginDrag(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem[], org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireBeginDrag(IProjectTreeControl pParentControl,
                             IProjectTreeItem[] pItem,
                             IProjectTreeDragVerify pVerify,
                             IEventPayload payload)
   {
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(pParentControl);
		collection.add(pItem);
		collection.add(pVerify);
		
		if (validateEvent("BeginDrag", collection))
      {
         IResultCell cell = prepareResultCell(payload);
         EventFunctor beginDragFunc = new EventFunctor("org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink", 
                                               "onBeginDrag");
         
         Object[] parms = new Object[4];
         parms[0] = pParentControl;
         parms[1] = pItem;
			parms[2] = pVerify;
			parms[3] = cell;
         beginDragFunc.setParameters(parms);
			m_ProjectTreeEventManager.notifyListeners(beginDragFunc);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventDispatcher#fireMoveDrag(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IDataObject, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireMoveDrag(IProjectTreeControl pParentControl,
                            Transferable pItem,
                            IProjectTreeDragVerify pVerify,
                            IEventPayload payload)
   {
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(pParentControl);
		collection.add(pItem);
		collection.add(pVerify);
      if (validateEvent("MoveDrag", collection))
      {
         IResultCell cell = prepareResultCell(payload);
         EventFunctor moveDragFunc = new EventFunctor("org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink", 
                                              "onMoveDrag");
         
         Object[] parms = new Object[4];
			parms[0] = pParentControl;
			parms[1] = pItem;
			parms[2] = pVerify;
			parms[3] = cell;
         moveDragFunc.setParameters(parms);
			m_ProjectTreeEventManager.notifyListeners(moveDragFunc);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventDispatcher#fireEndDrag(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IDataObject, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireEndDrag(IProjectTreeControl    pParentControl,
                           Transferable           pItem,
                           int                    action,
                           IProjectTreeDragVerify pVerify,
                           IEventPayload          payload)
   {
      Object actionObject = new Integer(action);
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(pParentControl);
		collection.add(pItem);
      collection.add(actionObject);
		collection.add(pVerify);
				
      if (validateEvent("EndDrag", collection))
      {
         IResultCell cell = prepareResultCell(payload);
         EventFunctor endDragFunc = new EventFunctor("org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink", 
                                             "onEndDrag");
         
         Object[] parms = new Object[5];
			parms[0] = pParentControl;
			parms[1] = pItem;
         parms[2] = actionObject;
			parms[3] = pVerify;
			parms[4] = cell;
         endDragFunc.setParameters(parms);
			m_ProjectTreeEventManager.notifyListeners(endDragFunc);
      }
   }
 }
