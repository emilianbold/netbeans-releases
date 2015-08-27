/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.truffle;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
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
 * Handler of step into JavaScript code from Java.
 * 
 * @author Martin
 */
@LazyActionsManagerListener.Registration(path="netbeans-JPDASession/Java")
public class StepIntoScriptHandler extends LazyActionsManagerListener implements PropertyChangeListener {
    
    private static final Logger LOG = Logger.getLogger(StepIntoScriptHandler.class.getCanonicalName());
    
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
        return new String[] { ActionsManagerListener.PROP_ACTION_PERFORMED, "actionToBeRun" };
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
        if ("actionToBeRun".equals(evt.getPropertyName())) {
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
                        //Method setSteppingIntoMethod = serviceClass.concreteMethodByName("setSteppingInto", "()V");
                        //serviceClass.invokeMethod(RemoteServices.getServiceAccessThread(debugger), setSteppingIntoMethod, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
                        LOG.fine("StepIntoScriptHandler: isSteppingInto set to true.");
                    } catch (ClassNotLoadedException | ClassNotPreparedExceptionWrapper |
                             InternalExceptionWrapper | InvalidTypeException |
                             ObjectCollectedExceptionWrapper | VMDisconnectedExceptionWrapper ex) {
                        Exceptions.printStackTrace(ex);
                    //} catch (IncompatibleThreadStateException | InvocationException ex) {
                    //    Exceptions.printStackTrace(ex);
                    }
                } else {
                    // When the service is created, perform step into...
                    DebugManagerHandler.execStepInto(debugger, true);
                }
            }
            if (ActionsManager.ACTION_STEP_OVER.equals(action)) {
                
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
                    //Method unsetSteppingIntoMethod = serviceClass.concreteMethodByName("unsetSteppingInto", "()V");
                    //serviceClass.invokeMethod(RemoteServices.getServiceAccessThread(debugger), unsetSteppingIntoMethod, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
                    LOG.fine("StepIntoScriptHandler: isSteppingInto set to false.");
                } catch (InvalidTypeException | ClassNotLoadedException ex) {
                    Exceptions.printStackTrace(ex);
                //} catch (IncompatibleThreadStateException | InvocationException ex) {
                //    Exceptions.printStackTrace(ex);
                }
            } else {
                // Cancel step into when the service is created
                DebugManagerHandler.execStepInto(debugger, false);
            }
        }
    }
    
    /*
    private class ScriptBPListener implements JPDABreakpointListener {
        
        @Override
        public void breakpointReached(JPDABreakpointEvent event) {
            // Call MethodHandle mh = getGenericInvoker();
            // mh.member.clazz is the class that is going to be called
            // mh.member.name is the method name.
            logger.fine("ScriptBPListener.breakpointReached()");
            try {
                setAltCSF(event.getThread());
                Variable mh;
                if (event.getSource() == scriptAccessBPs[0]) {
                    mh = debugger.evaluate("getGenericInvoker()");
                } else {
                    mh = debugger.evaluate("getGenericConstructor()");
                }
                if (!(mh instanceof ObjectVariable)) {
                    logger.info("getGenericInvoker/Constructor returned "+mh+", which is not an object.");
                    return ;
                }
                ObjectVariable omh = (ObjectVariable) mh;
                ObjectVariable member = (ObjectVariable) omh.getField("member");
                if (!(member instanceof ObjectVariable)) {
                    logger.info("Variable "+mh+" does not have member field: "+member);
                    return ;
                }
                ObjectVariable clazz = (ObjectVariable) member.getField("clazz");
                if (!(clazz instanceof ClassVariable)) {
                    logger.info("Variable "+mh+" does not have clazz field: "+clazz);
                    return ;
                }
                //JPDAClassType classType = ((ClassVariable) clazz).getReflectedType();
                JPDAClassType classType;
                try {
                    classType = (JPDAClassType) clazz.getClass().getMethod("getReflectedType").invoke(clazz);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                    Exceptions.printStackTrace(ex);
                    return ;
                }
                String className = classType.getName();

                MethodBreakpoint mb = MethodBreakpoint.create(className, "");
                mb.setHidden(true);
                mb.setSuspend(debugger.getSuspend());
                mb.setSession(debugger);
                mb.addJPDABreakpointListener(new InScriptBPListener(mb));
                DebuggerManager.getDebuggerManager().addBreakpoint(mb);
                logger.log(Level.FINE, "Created {0} for any method in {1}", new Object[]{mb, className});
                
            } catch (InvalidExpressionException iex) {
                
            } finally {
                setAltCSF(null);
                event.resume();
            }
        }
        
        private void setAltCSF(JPDAThread thread) {
            try {
                StackFrame sf;
                if (thread != null) {
                    ThreadReference tr = (ThreadReference) thread.getClass().getMethod("getThreadReference").invoke(thread);
                    sf = tr.frame(0);
                } else {
                    sf = null;
                }
                debugger.getClass().getMethod("setAltCSF", StackFrame.class).invoke(debugger, sf);
            } catch (com.sun.jdi.IncompatibleThreadStateException e) {
            } catch (ObjectCollectedException e) {
            } catch (IllegalThreadStateException e) {
                // Let it go, the thread is dead.
            } catch (java.lang.IndexOutOfBoundsException e) {
                // No frame in case of Thread and "Main" class breakpoints, PATCH 56540
            } catch (VMDisconnectedException vmdex) {
            } catch (InternalException iex) {
            } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException ex) {
                
            }
        }
        
    }
    
    private class InScriptBPListener implements JPDABreakpointListener {
        
        private MethodBreakpoint mb;
        
        InScriptBPListener(MethodBreakpoint mb) {
            this.mb = mb;
        }

        @Override
        public void breakpointReached(JPDABreakpointEvent event) {
            logger.log(Level.FINE, "InScriptBPListener.breakpointReached(), removing {0}", mb);
            mb.disable();
            mb.removeJPDABreakpointListener(this);
            DebuggerManager.getDebuggerManager().removeBreakpoint(mb);
            // We're in the script.
            // Disable any pending step requests:
            disableStepRequests(event.getThread());
        }

        private void disableStepRequests(JPDAThread thread) {
            ThreadReference tr;
            try {
                tr = (ThreadReference) thread.getClass().getMethod("getThreadReference").invoke(thread);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException |
                     NoSuchMethodException | SecurityException ex) {
                Exceptions.printStackTrace(ex);
                return ;
            }
            try {
                VirtualMachine vm = tr.virtualMachine();
                if (vm == null) return;
                EventRequestManager erm = vm.eventRequestManager();
                List<StepRequest> l = erm.stepRequests();
                for (StepRequest stepRequest : l) {
                    if (stepRequest.thread().equals(tr)) {
                        try {
                            stepRequest.disable();
                        } catch (InvalidRequestStateException ex) {}
                    }
                }
            } catch (VMDisconnectedException | InternalException |
                     IllegalThreadStateException | InvalidRequestStateException e) {
            }
        }
        
    }
    */
    
}
