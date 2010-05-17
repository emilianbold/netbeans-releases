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
 * File         : RoundTripOperationEventsSink.java
 * Version      : 1.2
 * Description  : Listener for operation change events.
 * Author       : Ashish
 */
package org.netbeans.modules.uml.integration.ide;

import java.util.Vector;

import org.netbeans.modules.uml.integration.ide.events.ClassInfo;
import org.netbeans.modules.uml.integration.ide.events.MethodInfo;
import org.netbeans.modules.uml.integration.ide.events.MethodParameterInfo;
import org.netbeans.modules.uml.integration.ide.listeners.IOperationChangeListener;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IMultipleParameterTypeChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IParameterChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IParameterTypeChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripOperationEventsSink;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
//import org.netbeans.modules.uml.integration.netbeans.NBEventProcessor;

/**
 *  Listens for changes to operations in the Describe model.
 *
 * Revision History
 * No.  Date        Who         What
 * ---  ----        ---         ----
 *   1  2002-04-23  Darshan     Added file and class comments.
 *   2  2002-04-25  Darshan     Added support for IDE integrations to add
 *                              secondary listeners to this.
 *   3  2002-04-26  Darshan     Removed diagnostic messages from second
 *                              (post-change) event functions.
 *   4  2002-05-03  Darshan     Incorporated Sumitabh's changes to make the
 *                              operation model-source event handler correctly
 *                              handle return type and parameter changes.
 *   5  2002-06-14  Darshan     Reintroduced the ChangeUtils.say() call.
 *   6  2002-06-19  Darshan     Fixed operation change events not correctly
 *                              processed for interfaces (bug 154).
 *   7  2002-08-06  Mukta       Added code to handle the
 *                              IMultipleParameterTypeChangeRequest
 *
 * @author  Ashish
 * @version 1.0
 */
public class RoundTripOperationEventsSink
                        extends RoundTripSource
                        implements IRoundTripOperationEventsSink {

    public void onPreOperationChangeRequest(IChangeRequest newVal,
                                            IResultCell cell) {
    }

    public void onOperationChangeRequest(IChangeRequest newVal,
                                         IResultCell cell) {
        try {
//            if(NBEventProcessor.isUpdatingModel() == false)
//            {
                ChangeUtils.say(newVal);
                fireOperationChangeEvent(newVal, false);
//            }
        } catch (Exception e) {
            Log.stackTrace(e);
        }
    }

    protected void fireOperationChangeEvent(IChangeRequest newVal,
                                            boolean beforeChange) {
        // Get change type
        int changeType = newVal.getState();

        IElement eBefore = newVal.getBefore(),
                 eAfter  = newVal.getAfter();

        if (eBefore instanceof IOperation) {
            IOperation before = (IOperation)  eBefore,
                   after   = (IOperation)  eAfter;

            switch (changeType) {
              case ChangeUtils.CT_CREATE:
              {
                  if (isValidEvent(after)) {
                      MethodInfo oper = new MethodInfo(null, after);
                      fireOperationAddedEvent(oper, beforeChange);

                      if (!oper.isAccessor() && !oper.isMutator())
                          scheduleForNavigation(after);
                  }
                  break;
              }
              case ChangeUtils.CT_DELETE:
              {
                  if (isValidEvent(before)) {
                      MethodInfo clazz = new MethodInfo(null, before);
                      fireOperationDeletedEvent(clazz, beforeChange);
                  }
                  break;
              }
              case ChangeUtils.CT_MODIFY:
              {
                  if (isValidEvent(before) && isValidEvent(after)) {
                      MethodInfo oldC = new MethodInfo(null, before),
                                newC = new MethodInfo(null, after);
                      if (oldC.getContainingClass() == null &&
                            newC.getContainingClass() != null)
                          oldC.setContainingClass(newC.getContainingClass());

                      if(newVal.getRequestDetailType() == ChangeUtils.RDT_MULTIPLE_PARAMETER_TYPE_MODIFIED){
                        IMultipleParameterTypeChangeRequest req = (IMultipleParameterTypeChangeRequest)  newVal;
                        IOperation impactedOperation = req.getImpactedOperation();
                        oldC = new MethodInfo(null, impactedOperation);
                        newC = new MethodInfo(null, impactedOperation);
                        MethodParameterInfo[] paraminfo =  oldC.getParameters();
                        for(int i = 0; i < paraminfo.length; i++){
                            if(paraminfo[i].getType().equals(req.getNewTypeName()))
                                paraminfo[i].setType(req.getOldTypeName());
                        }
                        oldC.setParameters(paraminfo);
                      }
                      Log.out("Firing change event for " + oldC.getName() + " to " + newC.getName());
                      fireOperationChangedEvent(oldC, newC, beforeChange);
                  }
                  break;
              }
            }
        } else if (eBefore instanceof IParameter) {
            if(newVal instanceof IParameterChangeRequest)
            {    
            IParameterChangeRequest req = (IParameterChangeRequest)  newVal;

            IOperation before = req.getBeforeOperation(),
                       after  = req.getAfterOperation();

            if (!isValidEvent(before) || !isValidEvent(after))
                return;

            IElement clazz = before.getOwner();
            if (clazz == null)
                clazz = after.getOwner();
			IClassifier classEl = clazz instanceof IClassifier? 
						(IClassifier) clazz : null;

            if (clazz == null || classEl == null) {
                Log.out("E----- Can't find class owning operation");
                // We can't proceed if we don't know what class to change.
                return ;
            }
            ClassInfo ci = null;

            try {
                //ci = new ClassInfo(classEl);
                ci = ClassInfo.getRefClassInfo(classEl, true);
            } catch (Exception e) {
                Log.stackTrace(e);
            }

                MethodInfo oldM = new MethodInfo(ci, before);
                MethodInfo newM = new MethodInfo(ci, after);
    
                fireOperationChangedEvent(oldM, newM, beforeChange);
            } 
            else
            {
                IParameterTypeChangeRequest req = (IParameterTypeChangeRequest)newVal;
                IParameter pm  = req.getImpactedParameter();  
                IOperation before  = (IOperation)pm.getOwner();
                IOperation after  = (IOperation)pm.getOwner();
    
                if (!isValidEvent(before) || !isValidEvent(after))
                    return;
    
                IElement clazz = before.getOwner();
                if (clazz == null)
                    clazz = after.getOwner();
                IClassifier classEl = clazz instanceof IClassifier? 
                            (IClassifier) clazz : null;
    
                if (clazz == null || classEl == null) {
                    Log.out("E----- Can't find class owning operation");
                    // We can't proceed if we don't know what class to change.
                    return ;
                }
                ClassInfo ci = null;
    
                try {
                    //ci = new ClassInfo(classEl);
                    ci = ClassInfo.getRefClassInfo(classEl, true);
                } catch (Exception e) {
                    Log.stackTrace(e);
                }

            MethodInfo oldM = new MethodInfo(ci, before);
            MethodInfo newM = new MethodInfo(ci, after);

                MethodParameterInfo[] bList = oldM.getParameters();
                MethodParameterInfo parm = null;

                for (int i = 0; i < bList.length; i++) 
                {
                    if(bList[i].getName().equals(pm.getName()))  
                    {                
                        parm = bList[i];
                        parm.setType(req.getOldTypeName());
                    }
                }
            fireOperationChangedEvent(oldM, newM, beforeChange);
                
            }
        } else {
            Log.out("Unknown event object!");
        }
    }

    protected void fireOperationAddedEvent(final MethodInfo method,
                                           final boolean before) {
        if (!isValidEvent(method, method.getContainingClass()))
            return;

		Log.out("Operation added (M->S): " + method);
        for (int i = 0; i < changeListeners.size(); ++i) {
            final IOperationChangeListener listener = (IOperationChangeListener)
                                                changeListeners.elementAt(i);
            RoundtripThread r = new RoundtripThread() {
                public void work() {
                    setDefaultProject(method);
                    listener.operationAdded(method, before);
                }
            };
            queue(r);
        }
    }

    protected void fireOperationDeletedEvent(final MethodInfo method,
                                             final boolean before) {
        if (!isValidEvent(method, method.getContainingClass()))
            return;

		Log.out("Operation deleted (M->S): " + method);
        for (int i = 0; i < changeListeners.size(); ++i) {
            final IOperationChangeListener listener = (IOperationChangeListener)
                                                changeListeners.elementAt(i);
            RoundtripThread r = new RoundtripThread() {
                public void work() {
                    setDefaultProject(method);
                    listener.operationDeleted(method, before);
                }
            };
            queue(r);
        }
    }

    protected void fireOperationChangedEvent(final MethodInfo oldM,
                                             final MethodInfo newM,
                                             final boolean before) {
		Log.out("Operation changed (M->S) from " + oldM + " to " + newM);
        for (int i = 0; i < changeListeners.size(); ++i) {
            final IOperationChangeListener listener = (IOperationChangeListener)
                                                changeListeners.elementAt(i);
            RoundtripThread r = new RoundtripThread() {
                public void work() {
                    setDefaultProject(newM);
                    listener.operationChanged(oldM, newM, before);
                }
            };
            queue(r);
        }
    }

    public static void addOperationChangeListener(IOperationChangeListener listener) {
        if (listener != null && !changeListeners.contains(listener))
            changeListeners.add(listener);
    }

    public static void removeOperationChangeListener(IOperationChangeListener listener) {
        if (listener != null)
            changeListeners.remove(listener);
    }

    private static Vector changeListeners = new Vector();
}
