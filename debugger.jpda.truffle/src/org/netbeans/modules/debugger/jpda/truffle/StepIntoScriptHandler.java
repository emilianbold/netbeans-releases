/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.truffle;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.InvalidTypeException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.ActionsManagerListener;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.Exceptions;

/**
 * Handler of step into language code from Java.
 * 
 * @author Martin
 */
@LazyActionsManagerListener.Registration(path="netbeans-JPDASession/Java")
public class StepIntoScriptHandler extends LazyActionsManagerListener implements PropertyChangeListener {
    
    private static final Logger LOG = Logger.getLogger(StepIntoScriptHandler.class.getCanonicalName());
    private static final String PROP_ACTION_TO_BE_RUN = "actionToBeRun";        // NOI18N
    
    private final JPDADebugger debugger;
    private ClassType serviceClass;
    private Field steppingField;
    
    public StepIntoScriptHandler(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        debugger.addPropertyChangeListener(JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME, new CurrentSFTracker());
    }

    @Override
    protected void destroy() {
        LOG.fine("\nStepIntoJSHandler.destroy()");
    }

    @Override
    public String[] getProperties() {
        return new String[] { ActionsManagerListener.PROP_ACTION_PERFORMED, PROP_ACTION_TO_BE_RUN };
    }

    @Override
    public void actionPerformed(Object action) {
        if (ActionsManager.ACTION_STEP_INTO.equals(action)) {
            //scriptBP.disable(); - no, the action may end too soon, some work
            //                      can continue on background
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (PROP_ACTION_TO_BE_RUN.equals(evt.getPropertyName())) {
            Object action = evt.getNewValue();
            if (ActionsManager.ACTION_STEP_INTO.equals(action)) {
                ClassObjectReference serviceClassRef = RemoteServices.getServiceClass(debugger);
                LOG.log(Level.FINE, "StepIntoScriptHandler.actionToBeRun: {0}, serviceClassRef = {1}", new Object[]{action, serviceClassRef});
                if (serviceClassRef != null) {
                    try {
                        serviceClass = (ClassType) ClassObjectReferenceWrapper.reflectedType(serviceClassRef);
                        steppingField = ReferenceTypeWrapper.fieldByName(serviceClass, "steppingIntoTruffle");
                        serviceClass.setValue(steppingField, serviceClass.virtualMachine().mirrorOf(1));
                        RemoteServices.interruptServiceAccessThread(debugger);
                        LOG.fine("StepIntoScriptHandler: isSteppingInto set to true.");
                    } catch (ClassNotLoadedException | ClassNotPreparedExceptionWrapper |
                             InternalExceptionWrapper | InvalidTypeException |
                             ObjectCollectedExceptionWrapper ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (VMDisconnectedExceptionWrapper ex) {}
                } else {
                    // When the service is created, perform step into...
                    DebugManagerHandler.execStepInto(debugger, true);
                }
            }
        }
    }
    
    private class CurrentSFTracker implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getNewValue() == null) {
                // Ignore resume.
                return ;
            }
            LOG.fine("Current frame changed>");
            if (steppingField != null) {
                try {
                    serviceClass.setValue(steppingField, serviceClass.virtualMachine().mirrorOf(-1));
                    steppingField = null;
                    RemoteServices.interruptServiceAccessThread(debugger);
                    LOG.fine("StepIntoScriptHandler: isSteppingInto set to false.");
                } catch (InvalidTypeException | ClassNotLoadedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                // Cancel step into when the service is created
                DebugManagerHandler.execStepInto(debugger, false);
            }
        }
    }

}
