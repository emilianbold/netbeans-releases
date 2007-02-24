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


package org.netbeans.modules.uml.core.metamodel.dynamics;

import java.util.Vector;

import org.netbeans.modules.uml.core.eventframework.EventDispatcher;
import org.netbeans.modules.uml.core.eventframework.EventFunctor;
import org.netbeans.modules.uml.core.eventframework.EventManager;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
/**
 * @author sumitabhk
 *
 */
public class DynamicsEventDispatcher extends EventDispatcher 
	implements IDynamicsEventDispatcher
{
    private EventManager<ILifelineModifiedEventsSink> m_LifelineEvents =
                    new EventManager<ILifelineModifiedEventsSink>();
    
    /* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.dynamics.IDynamicsEventDispatcher#registerForLifelineModifiedEvents(org.netbeans.modules.uml.core.metamodel.dynamics.ILifelineModifiedEventsSink)
	 */
    public void registerForLifelineModifiedEvents(
            ILifelineModifiedEventsSink handler)
    {
        m_LifelineEvents.addListener(handler, null);
    }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.dynamics.IDynamicsEventDispatcher#revokeLifelineModifiedSink(org.netbeans.modules.uml.core.metamodel.dynamics.ILifelineModifiedEventsSink)
    */
    public void revokeLifelineModifiedSink(ILifelineModifiedEventsSink handler)
    {
        m_LifelineEvents.removeListener(handler);
    }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.dynamics.IDynamicsEventDispatcher#firePreChangeRepresentingClassifier(org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public boolean firePreChangeRepresentingClassifier(ILifeline pLifeline, ITypedElement pRepresents, IEventPayload payload)
   {
       boolean proceed = true;

       Vector<Object> vect = new Vector<Object>();
       vect.add(pLifeline);
       vect.add(pRepresents);
       Object var = prepareVariant(vect);

       if (validateEvent("PreChangeRepresentingClassifier", var))
       {
           IResultCell cell = prepareResultCell( payload );
           EventFunctor preChangeRepresentingClassifier = new EventFunctor(
                       "org.netbeans.modules.uml.core.metamodel.dynamics.ILifelineModifiedEventsSink", 
                       "onPreChangeRepresentingClassifier");
           
           Object[] parms = new Object[] { var, cell };
           preChangeRepresentingClassifier.setParameters(parms);
           m_LifelineEvents.notifyListenersWithQualifiedProceed(preChangeRepresentingClassifier);
           if (cell != null)
           {
               proceed = cell.canContinue();
           }
       }
       return proceed; 
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.dynamics.IDynamicsEventDispatcher#fireChangeRepresentingClassifier(org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.eventframework.IEventPayload)
    */
   public void fireChangeRepresentingClassifier(ILifeline pLifeline, ITypedElement pRepresents, IEventPayload payload)
   {
       Vector<Object> vect = new Vector<Object>();
       vect.add(pLifeline);
       vect.add(pRepresents);
       Object var = prepareVariant(vect);

       if (validateEvent("ChangeRepresentingClassifier", var))
       {
           IResultCell cell = prepareResultCell( payload );
           EventFunctor changeRepresentingClassifier = new EventFunctor(
                       "org.netbeans.modules.uml.core.metamodel.dynamics.ILifelineModifiedEventsSink", 
                       "onChangeRepresentingClassifier");
           
           Object[] parms = new Object[] { var, cell };
           changeRepresentingClassifier.setParameters(parms);
           m_LifelineEvents.notifyListeners(changeRepresentingClassifier);
       }
   }
}

