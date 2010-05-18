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
