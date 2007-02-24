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

import org.netbeans.modules.uml.core.eventframework.EventDispatcher;
import org.netbeans.modules.uml.core.eventframework.EventFunctor;
import org.netbeans.modules.uml.core.eventframework.EventManager;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.ResultCell;

/**
 * The implementation of the IProjectTreeFilterDialogEventDispatcher interface.
 *
 * @author Trey Spiva
 * @see IProjectTreeFilterDialogEventDispatcher
 */
public class ProjectTreeFilterDialogEventDispatcher extends EventDispatcher
   implements IProjectTreeFilterDialogEventDispatcher
{
   EventManager < IProjectTreeFilterDialogEventsSink > m_PTFDispatcher = null;
   
   public ProjectTreeFilterDialogEventDispatcher()
   {
      super();
      m_PTFDispatcher = new EventManager < IProjectTreeFilterDialogEventsSink >();
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.filter.IProjectTreeFilterDialogEventDispatcher#registerProjectTreeFilterDialogEvents(org.netbeans.modules.uml.ui.controls.filter.IProjectTreeFilterDialogEventsSink)
    */
   public void registerProjectTreeFilterDialogEvents(IProjectTreeFilterDialogEventsSink sink)
   {
      m_PTFDispatcher.addListener(sink, null);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.filter.IProjectTreeFilterDialogEventDispatcher#revokeProjectTreeFilterDialogEvents(org.netbeans.modules.uml.ui.controls.filter.IProjectTreeFilterDialogEventsSink)
    */
   public void revokeProjectTreeFilterDialogEvents(IProjectTreeFilterDialogEventsSink sink)
   {
      m_PTFDispatcher.removeListener(sink);
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.filter.IProjectTreeFilterDialogEventDispatcher#fireProjectTreeFilterDialogInit(org.netbeans.modules.uml.ui.controls.filter.IFilterDialog, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireProjectTreeFilterDialogInit(IFilterDialog dialog, IEventPayload payload)
   {
      if (validateEvent("ProjectTreeFilterDialogInit", dialog))
      {
         IResultCell cell = prepareJResultCell(payload);
         EventFunctor ptfDialogInitFunc = new EventFunctor("org.netbeans.modules.uml.ui.controls.filter.IProjectTreeFilterDialogEventsSink", 
                                                   "onProjectTreeFilterDialogInit");
         
         Object[] parms = new Object[2];
         parms[0] = dialog;
         parms[1] = cell;
         ptfDialogInitFunc.setParameters(parms);
         m_PTFDispatcher.notifyListeners(ptfDialogInitFunc);
      }
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.filter.IProjectTreeFilterDialogEventDispatcher#fireProjectTreeFilterDialogOKActivated(org.netbeans.modules.uml.ui.controls.filter.IFilterDialog, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireProjectTreeFilterDialogOKActivated(IFilterDialog dialog, IEventPayload payload)
   {
      if (validateEvent("onProjectTreeFilterDialogOKActivated", dialog))
      {
         IResultCell cell = prepareJResultCell(payload);
         EventFunctor ptfOKActivatedFunc = new EventFunctor("org.netbeans.modules.uml.ui.controls.filter.IProjectTreeFilterDialogEventsSink", 
                                                    "onProjectTreeFilterDialogOKActivated");
         
         Object[] parms = new Object[2];
         parms[0] = dialog;
         parms[1] = cell;
         ptfOKActivatedFunc.setParameters(parms);
         m_PTFDispatcher.notifyListeners(ptfOKActivatedFunc);
      }
   
   }
   /**
    * Creates and prepares the IResultCell object that will be included with the event.
    *
    * @param payload[in] The payload to include with the event cell. Can be 0
    * @param cell[out] The ResultCell
    *
    * @return HRESULT
    */
   protected IResultCell prepareJResultCell( IEventPayload payload)
   {
      IResultCell cell = new ResultCell(); 

      if( payload != null)
      {
         // Set the payLoad on the ResultCell
         cell.setContextData(payload);
      }
       return cell;
   }
}
