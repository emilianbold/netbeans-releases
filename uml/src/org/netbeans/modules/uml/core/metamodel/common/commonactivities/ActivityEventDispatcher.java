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

package org.netbeans.modules.uml.core.metamodel.common.commonactivities;

import java.util.Vector;

import org.netbeans.modules.uml.core.eventframework.EventDispatcher;
import org.netbeans.modules.uml.core.eventframework.EventFunctor;
import org.netbeans.modules.uml.core.eventframework.EventManager;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

/**
 * @author sumitabhk
 */
public class ActivityEventDispatcher extends EventDispatcher
   implements IActivityEventDispatcher
{
    private EventManager<IActivityEdgeEventsSink> manager = 
        new EventManager<IActivityEdgeEventsSink>();

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEventDispatcher#registerForActivityEdgeEvents(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdgeEventsSink)
     */
    public void registerForActivityEdgeEvents(IActivityEdgeEventsSink sink)
    {
        manager.addListener(sink, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEventDispatcher#revokeActivityEdgeSink(int)
     */
    public void revokeActivityEdgeSink(IActivityEdgeEventsSink sink)
    {
        manager.removeListener(sink);
    }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEventDispatcher#firePreWeightModified(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge, java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
    public boolean firePreWeightModified(
        IActivityEdge pEdge,
        String newValue,
        IEventPayload payload)
    {
        boolean proceed = true;

        Vector<Object> vect = new Vector<Object>();
        vect.add(pEdge);
        vect.add(newValue);        
        Object var = prepareVariant(vect);

        if (validateEvent("PreWeightModified", var))
        {
            IResultCell cell = prepareResultCell( payload );
            EventFunctor preWeightModified = new EventFunctor(
                    "org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdgeEventsSink", 
                    "onPreWeightModified");

            Object[] parms = new Object[2];
            parms[0] = var;
            parms[1] = cell;
            preWeightModified.setParameters(parms);
            manager.notifyListenersWithQualifiedProceed(preWeightModified);
            if (cell != null)
            {
                proceed = cell.canContinue();
            }
        }
        return proceed; 
    }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEventDispatcher#fireWeightModified(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireWeightModified(IActivityEdge pEdge, IEventPayload payload)
   {
       Vector<Object> vect = new Vector<Object>();
       vect.add(pEdge);
       Object var = prepareVariant(vect);

       if (validateEvent("WeightModified", var))
       {
           IResultCell cell = prepareResultCell( payload );
           EventFunctor weightModified = new EventFunctor(
                   "org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdgeEventsSink", 
                   "onWeightModified");

           Object[] parms = new Object[2];
           parms[0] = var;
           parms[1] = cell;
           weightModified.setParameters(parms);
           manager.notifyListenersWithQualifiedProceed(weightModified);
       }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEventDispatcher#firePreGuardModified(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge, java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean firePreGuardModified(
      IActivityEdge pEdge,
      String newValue,
      IEventPayload payload)
   {
       boolean proceed = true;

       Vector<Object> vect = new Vector<Object>();
       vect.add(pEdge);
       vect.add(newValue);
       Object var = prepareVariant(vect);

       if (validateEvent("PreGuardModified", var))
       {
           IResultCell cell = prepareResultCell( payload );
           EventFunctor preGuardModified = new EventFunctor(
                   "org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdgeEventsSink", 
                   "onPreGuardModified");

           Object[] parms = new Object[2];
           parms[0] = var;
           parms[1] = cell;
           preGuardModified.setParameters(parms);
           manager.notifyListenersWithQualifiedProceed(preGuardModified);
           if (cell != null)
           {
               proceed = cell.canContinue();
           }
       }
       return proceed; 
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEventDispatcher#fireGuardModified(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireGuardModified(IActivityEdge pEdge, IEventPayload payload)
   {
       Vector<Object> vect = new Vector<Object>();
       vect.add(pEdge);
       Object var = prepareVariant(vect);

       if (validateEvent("GuardModified", var))
       {
           IResultCell cell = prepareResultCell( payload );
           EventFunctor guardModified = new EventFunctor(
                   "org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdgeEventsSink", 
                   "onGuardModified");

           Object[] parms = new Object[2];
           parms[0] = var;
           parms[1] = cell;
           guardModified.setParameters(parms);
           manager.notifyListenersWithQualifiedProceed(guardModified);
       }
   }
}
