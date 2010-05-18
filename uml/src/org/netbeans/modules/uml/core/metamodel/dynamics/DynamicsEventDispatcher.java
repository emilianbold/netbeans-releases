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

