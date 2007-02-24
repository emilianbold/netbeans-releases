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