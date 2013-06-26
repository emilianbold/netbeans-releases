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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InternalException;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.ActionsManagerListener;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.JPDAStep;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.ExpressionPool.Expression;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.JPDAStepImpl;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidRequestStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocationWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StackFrameWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestWrapper;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.netbeans.modules.debugger.jpda.models.CallStackFrameImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.util.Executor;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;
import org.openide.ErrorManager;

import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author  Martin Entlicher
 */
@ActionsProvider.Registration(path="netbeans-JPDASession", actions={"runIntoMethod"})
public class RunIntoMethodActionProvider extends ActionsProviderSupport 
                                         implements PropertyChangeListener,
                                                    ActionsManagerListener {

    private static final Logger logger = Logger.getLogger(RunIntoMethodActionProvider.class.getName());

    private JPDADebuggerImpl debugger;
    private ActionsManager lastActionsManager;
    
    public RunIntoMethodActionProvider(ContextProvider lookupProvider) {
        debugger = (JPDADebuggerImpl) lookupProvider.lookupFirst 
                (null, JPDADebugger.class);
        debugger.addPropertyChangeListener (JPDADebuggerImpl.PROP_STATE, this);
        EditorContextBridge.getContext().addPropertyChangeListener (this);
    }
    
    private void destroy () {
        debugger.removePropertyChangeListener (JPDADebuggerImpl.PROP_STATE, this);
        EditorContextBridge.getContext().removePropertyChangeListener (this);
        if (lastActionsManager != null) {
            lastActionsManager.removeActionsManagerListener(ActionsManagerListener.PROP_ACTION_STATE_CHANGED, this);
            lastActionsManager = null;
        }
    }
    
    static ActionsManager getCurrentActionsManager () {
        return DebuggerManager.getDebuggerManager ().
            getCurrentEngine () == null ? 
            DebuggerManager.getDebuggerManager ().getActionsManager () :
            DebuggerManager.getDebuggerManager ().getCurrentEngine ().
                getActionsManager ();
    }
    
    private ActionsManager getActionsManager () {
        ActionsManager current = getCurrentActionsManager();
        if (current != lastActionsManager) {
            if (lastActionsManager != null) {
                lastActionsManager.removeActionsManagerListener(
                        ActionsManagerListener.PROP_ACTION_STATE_CHANGED, this);
            }
            current.addActionsManagerListener(
                    ActionsManagerListener.PROP_ACTION_STATE_CHANGED, this);
            lastActionsManager = current;
        }
        return current;
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        setEnabled (
            ActionsManager.ACTION_RUN_INTO_METHOD,
            getActionsManager().isEnabled(ActionsManager.ACTION_CONTINUE) &&
            (debugger.getState () == JPDADebugger.STATE_STOPPED) &&
            (debugger.getCurrentThread() != null) &&
            (EditorContextBridge.getContext().getCurrentLineNumber () >= 0) && 
            (EditorContextBridge.getContext().getCurrentURL ().endsWith (".java"))
        );
        if (debugger.getState () == JPDADebugger.STATE_DISCONNECTED) 
            destroy ();
    }
    
    @Override
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_RUN_INTO_METHOD);
    }
    
    @Override
    public void doAction (Object action) {
        final String[] methodPtr = new String[1];
        final String[] urlPtr = new String[1];
        final String[] classPtr = new String[1];
        final int[] linePtr = new int[1];
        final int[] offsetPtr = new int[1];
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    EditorContext context = EditorContextBridge.getContext();
                    methodPtr[0] = context.getSelectedMethodName ();
                    linePtr[0] = context.getCurrentLineNumber();
                    offsetPtr[0] = EditorContextBridge.getCurrentOffset();
                    urlPtr[0] = context.getCurrentURL();
                    classPtr[0] = context.getCurrentClassName();
                }
            });
        } catch (InvocationTargetException ex) {
            ErrorManager.getDefault().notify(ex.getTargetException());
            return;
        } catch (InterruptedException ex) {
            ErrorManager.getDefault().notify(ex);
            return;
        }
        final String method = methodPtr[0];
        if (method.length () < 1) {
            NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(
                NbBundle.getMessage(RunIntoMethodActionProvider.class,
                                    "MSG_Put_cursor_on_some_method_call")
            );
            DialogDisplayer.getDefault ().notify (descriptor);
            return;
        }
        final int methodLine = linePtr[0];
        final int methodOffset = offsetPtr[0];
        final String url = urlPtr[0];
        String className = classPtr[0]; //debugger.getCurrentThread().getClassName();
        VirtualMachine vm = debugger.getVirtualMachine();
        if (vm == null) return ;
        // Find the class where the thread is stopped at
        ReferenceType clazz = null;
        String clazzName = null;
        JPDAThreadImpl ct = (JPDAThreadImpl) debugger.getCurrentThread();
        if (ct != null) {
            ThreadReference threadReference = ct.getThreadReference();
            try {
                if (ThreadReferenceWrapper.frameCount(threadReference) < 1) return ;
                clazz = LocationWrapper.declaringType(
                        StackFrameWrapper.location(ThreadReferenceWrapper.frame(threadReference, 0)));
                clazzName = ReferenceTypeWrapper.name(clazz);
            } catch (InternalExceptionWrapper ex) {
                return ;
            } catch (ObjectCollectedExceptionWrapper ex) {
            } catch (InvalidStackFrameExceptionWrapper ex) {
            } catch (IncompatibleThreadStateException ex) {
            } catch (IllegalThreadStateExceptionWrapper ex) {
                // Thrown when thread has exited
                return ;
            } catch (VMDisconnectedExceptionWrapper ex) {
                return ;
            }
        }
        if (clazz != null && (className == null || className.equals(clazzName))) {
            doAction(url, clazz, methodLine, methodOffset, method, true);
        } else {
            try {
                List<ReferenceType> classes = VirtualMachineWrapper.classesByName(vm, className);
                if (classes.size() > 0) {
                    doAction(url, classes.get(0), methodLine, methodOffset, method, true);
                    return ;
                }
            } catch (InternalExceptionWrapper ex) {
                return ;
            } catch (VMDisconnectedExceptionWrapper ex) {
                return ;
            }

            final ClassLoadUnloadBreakpoint cbrkp = ClassLoadUnloadBreakpoint.create(className, false, ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED);
            cbrkp.setHidden(true);
            cbrkp.setSuspend(ClassLoadUnloadBreakpoint.SUSPEND_NONE);
            cbrkp.addJPDABreakpointListener(new JPDABreakpointListener() {
                @Override
                public void breakpointReached(JPDABreakpointEvent event) {
                    DebuggerManager.getDebuggerManager().removeBreakpoint(cbrkp);
                    doAction(url, event.getReferenceType(), methodLine, methodOffset, method, false);
                }
            });
            cbrkp.setSession(debugger);
            DebuggerManager.getDebuggerManager().addBreakpoint(cbrkp);
            resume(debugger);
        }
    }
    
    private static void resume(JPDADebugger debugger) {
        if (debugger.getSuspend() == JPDADebugger.SUSPEND_EVENT_THREAD) {
            debugger.getCurrentThread().resume();
            //((JPDADebuggerImpl) debugger).resumeCurrentThread();
        } else {
            //((JPDADebuggerImpl) debugger).resume();
            Session session = ((JPDADebuggerImpl) debugger).getSession();
            session.getEngineForLanguage ("Java").getActionsManager ().doAction (
                ActionsManager.ACTION_CONTINUE
            );
        }
    }
    
    private void doAction(String url, final ReferenceType clazz, int methodLine,
                          int methodOffset, final String methodName, boolean doResume) {
        List<Location> locations = java.util.Collections.emptyList();
        try {
            while (methodLine > 0 && (locations = ReferenceTypeWrapper.locationsOfLine(clazz, methodLine)).isEmpty()) {
                methodLine--;
            }
        } catch (InternalExceptionWrapper aiex) {
            return ;
        } catch (VMDisconnectedExceptionWrapper aiex) {
            return ;
        } catch (ObjectCollectedExceptionWrapper aiex) {
            return ;
        } catch (ClassNotPreparedExceptionWrapper aiex) {
        } catch (AbsentInformationException aiex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, aiex);
        }
        logger.log(Level.FINE, "doAction({0}, {1}, {2}, {3}) locations = {4}",
                   new Object[]{ url, clazz, methodLine, methodName, locations });
        if (locations.isEmpty()) {
            String message = NbBundle.getMessage(RunIntoMethodActionProvider.class,
                                                 "MSG_RunIntoMeth_absentInfo",
                                                 clazz.name());
            NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(message);
            DialogDisplayer.getDefault().notify(descriptor);
            return;
        }
        Expression expr = debugger.getExpressionPool().getExpressionAt(locations.get(0), url);
        Location bpLocation = null;
        if (expr != null) {
            Operation[] ops = expr.getOperations();
            for (int i = 0; i < ops.length; i++) {
                Operation op = ops[i];
                if (op.getMethodStartPosition().getOffset() <= methodOffset &&
                    methodOffset <= op.getMethodEndPosition().getOffset()) {
                    
                    bpLocation = expr.getLocations()[i];
                    break;
                }
            }
        }
        if (bpLocation == null) {
            bpLocation = locations.get(0);
        }
        doAction(debugger, methodName, bpLocation, false, doResume);
    }

    static boolean doAction(final JPDADebuggerImpl debugger,
                            final String methodName,
                            Location bpLocation,
                            // If it's important not to run far from the expression
                            boolean setBoundaryStep) {
        
        return doAction(debugger, methodName, bpLocation, setBoundaryStep, true);
    }

    private static boolean doAction(final JPDADebuggerImpl debugger,
                                    final String methodName,
                                    Location bpLocation,
                                    // If it's important not to run far from the expression
                                    boolean setBoundaryStep,
                                    boolean doResume) {
        final VirtualMachine vm = debugger.getVirtualMachine();
        if (vm == null) return false;
        final int line = LocationWrapper.lineNumber0(bpLocation, "Java");
        JPDAThreadImpl ct = (JPDAThreadImpl) debugger.getCurrentThread();
        if (ct == null) {
            return false; // No intelligent stepping without the current thread.
        }
        CallStackFrame[] topFramePtr;
        try {
            topFramePtr = ct.getCallStack(0, 1);
        } catch (AbsentInformationException ex) {
            logger.log(Level.FINE, "doAction() = false, ex = {0}", ex);
            return false;
        }
        if (topFramePtr.length < 1) {
            logger.fine("doAction() = false, no top frame.");
            return false;
        }
        CallStackFrameImpl csf = (CallStackFrameImpl) topFramePtr[0];
        final JPDAThreadImpl t;
        boolean areWeOnTheLocation;
        try {
            areWeOnTheLocation = LocationWrapper.equals(StackFrameWrapper.location(csf.getStackFrame()), bpLocation);
            t = (JPDAThreadImpl) csf.getThread();
        } catch (InvalidStackFrameExceptionWrapper e) {
            return false; // No intelligent stepping without the current stack frame.
        } catch (VMDisconnectedExceptionWrapper e) {
            return false; // No stepping without the connection.
        } catch (InternalExceptionWrapper e) {
            return false; // No stepping without the correct functionality.
        }
        final boolean doFinishWhenMethodNotFound = setBoundaryStep;
        logger.log(Level.FINE, "doAction() areWeOnTheLocation = {0}, methodName = {1}", new Object[]{areWeOnTheLocation, methodName});
        if (areWeOnTheLocation) {
            // We're on the line from which the method is called
            traceLineForMethod(debugger, ct.getThreadReference(), methodName, line, doFinishWhenMethodNotFound);
        } else {
            final JPDAStep[] boundaryStepPtr = new JPDAStep[] { null };
            // Submit the breakpoint to get to the point from which the method is called
            try {
                final BreakpointRequest brReq = EventRequestManagerWrapper.createBreakpointRequest(
                        VirtualMachineWrapper.eventRequestManager(vm),
                        bpLocation);
                final ThreadReference preferredThread = t.getThreadReference();
                Executor tracingExecutor = new Executor() {

                    @Override
                    public boolean exec(Event event) {
                        ThreadReference tr = ((BreakpointEvent) event).thread();
                        try {
                            if (!preferredThread.equals(tr)) {
                                logger.log(Level.FINE, "doAction: tracingExecutor.exec({0}) called with non-preferred thread.", event);
                                // Wait a while for the preferred thread to hit the breakpoint...
                                int i = 20;
                                while (!ThreadReferenceWrapper.isAtBreakpoint(preferredThread) && i > 0) {
                                    try {
                                        Thread.sleep(50);
                                    } catch (InterruptedException ex) {
                                        break;
                                    }
                                    i--;
                                }
                                if (ThreadReferenceWrapper.isAtBreakpoint(preferredThread)) {
                                    if (ThreadReferenceWrapper.frameCount(tr) > 0) {
                                        Location trLoc = StackFrameWrapper.location(ThreadReferenceWrapper.frame(tr, 0));
                                        if (ThreadReferenceWrapper.frameCount(preferredThread) > 0) {
                                            Location prLoc = StackFrameWrapper.location(ThreadReferenceWrapper.frame(preferredThread, 0));
                                            if (trLoc.equals(prLoc)) {
                                                logger.log(Level.FINE, "doAction: tracingExecutor - preferredThread {0} is at breakpoint, resuming hit thread {1}", new Object[]{preferredThread, tr});
                                                return true; // Resume this thread, the preferred thread has hit.
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (InternalExceptionWrapper iex) {
                        } catch (InternalException iex) {
                        } catch (VMDisconnectedExceptionWrapper vdex) {
                        } catch (VMDisconnectedException vdex) {
                        } catch (ObjectCollectedExceptionWrapper ocex) {
                        } catch (ObjectCollectedException ocex) {
                        } catch (IllegalThreadStateExceptionWrapper itex) {
                        } catch (IllegalThreadStateException itex) {
                        } catch (IncompatibleThreadStateException itex) {
                        } catch (InvalidStackFrameExceptionWrapper isex) {
                        }
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("doAction: tracingExecutor.exec("+event+") called with thread "+tr+" which is "+((preferredThread.equals(tr)) ? "" : "not ")+"preferred.");
                            logger.fine("Calling location reached, tracing for "+methodName+"()");
                        }
                        if (boundaryStepPtr[0] != null) {
                            ((JPDAStepImpl) boundaryStepPtr[0]).cancel();
                        }
                        try {
                            try {
                                EventRequestManagerWrapper.deleteEventRequest(
                                        VirtualMachineWrapper.eventRequestManager(vm),
                                        brReq);
                            } catch (InvalidRequestStateExceptionWrapper ex) {}
                            debugger.getOperator().unregister(brReq);
                        } catch (InternalExceptionWrapper e) {
                        } catch (VMDisconnectedExceptionWrapper e) {
                            return false;
                        }
                        traceLineForMethod(debugger, tr, methodName, line, doFinishWhenMethodNotFound);
                        return true;
                    }

                    @Override
                    public void removed(EventRequest eventRequest) {}
                };
                debugger.getOperator().register(brReq, tracingExecutor);
                //BreakpointRequestWrapper.addThreadFilter(brReq, t.getThreadReference()); - a different thread might run into the method
                EventRequestWrapper.setSuspendPolicy(brReq, debugger.getSuspend());
                //EventRequestWrapper.addCountFilter(brReq, 1); - Can be hit multiple times in multiple threads
                try {
                    EventRequestWrapper.enable(brReq);
                } catch (ObjectCollectedExceptionWrapper ocex) {
                    // Unlikely to be thrown.
                    debugger.getOperator().unregister(brReq);
                    return false;
                } catch (InvalidRequestStateExceptionWrapper irse) {
                    Exceptions.printStackTrace(irse);
                    debugger.getOperator().unregister(brReq);
                    return false;
                }
                if (setBoundaryStep) {
                    boundaryStepPtr[0] = setBoundaryStepRequest(debugger, t, brReq);
                }
            } catch (VMDisconnectedExceptionWrapper e) {
                return false;
            } catch (InternalExceptionWrapper e) {
                return false;
            }
        }
        if (doResume) {
            resume(debugger);
        }
        return true;
    }

    private static JPDAStep setBoundaryStepRequest(final JPDADebuggerImpl debugger,
                                                   JPDAThread tr,
                                                   final EventRequest request) {
        // We need to also submit a step request so that we're sure that we end up at least on the next execution line
        JPDAStep boundaryStep = debugger.createJPDAStep(JPDAStep.STEP_LINE, JPDAStep.STEP_OVER);
        boundaryStep.addPropertyChangeListener(JPDAStep.PROP_STATE_EXEC, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                VirtualMachine vm = debugger.getVirtualMachine();
                if (vm != null) {
                    try {
                        debugger.getOperator().unregister(request);
                        EventRequestManagerWrapper.deleteEventRequest(
                                VirtualMachineWrapper.eventRequestManager(vm),
                                request);
                    } catch (InternalExceptionWrapper ex) {
                    } catch (VMDisconnectedExceptionWrapper ex) {
                    } catch (InvalidRequestStateExceptionWrapper irex) {
                    }
                }
            }
        });
        boundaryStep.addStep(tr);
        return boundaryStep;
    }

    private static void traceLineForMethod(final JPDADebuggerImpl debugger,
                                           final ThreadReference tr,
                                           final String method,
                                           final int methodLine,
                                           final boolean finishWhenNotFound) {
        final JPDAThread jtr = debugger.getThread(tr);
        final int depth = jtr.getStackDepth();
        final JPDAStep step = debugger.createJPDAStep(JPDAStep.STEP_LINE, JPDAStep.STEP_INTO);
        step.setHidden(true);
        logger.log(Level.FINE, "Will traceLineForMethod({0}, {1}, {2})",
                   new Object[]{method, methodLine, finishWhenNotFound});
        step.addPropertyChangeListener(JPDAStep.PROP_STATE_EXEC, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("traceLineForMethod("+method+") step is at "+debugger.getCurrentThread().getClassName()+":"+debugger.getCurrentThread().getMethodName());
                }
                //System.err.println("RunIntoMethodActionProvider: Step fired, at "+
                //                   debugger.getCurrentThread().getMethodName()+"()");
                //JPDAThread t = debugger.getCurrentThread();
                int currentDepth = jtr.getStackDepth();
                logger.log(Level.FINE, "  depth = {0}, target = {1}", new Object[]{currentDepth, depth});
                if (currentDepth == depth) { // We're in the outer expression
                    try {
                        if (jtr.getCallStack()[0].getLineNumber("Java") != methodLine) {
                            // We've missed the method :-(
                            step.setHidden(false);
                        } else {
                            logger.fine("  back on the method invoaction line, setting additional step into.");
                            step.setDepth(JPDAStep.STEP_INTO);
                            step.addStep(debugger.getCurrentThread());
                        }
                    } catch (AbsentInformationException aiex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, aiex);
                        // We're somewhere strange...
                        step.setHidden(false);
                    }
                } else {
                    String threadMethod = jtr.getMethodName();
                    logger.log(Level.FINE, "  threadMethod = ''{0}'', tracing method = ''{1}'', equals = {2}",
                               new Object[]{threadMethod, method, threadMethod.equals(method)});
                    if (threadMethod.equals(method)) {
                        // We've found it :-)
                        step.setHidden(false);
                    } else if (threadMethod.equals("<init>") && (jtr.getClassName().endsWith("."+method) || jtr.getClassName().equals(method))) {
                        // The method can be a constructor
                        step.setHidden(false);
                    } else {
                        if (finishWhenNotFound) {
                            // We've missed the method, finish.
                            step.setHidden(false);
                            logger.fine("  stepping finished.");
                        } else {
                            logger.fine("  step out submitted.");
                            step.setDepth(JPDAStep.STEP_OUT);
                            step.addStep(debugger.getCurrentThread());
                        }
                    }
                }
            }
        });
        step.addStep(jtr);
    } 

    @Override
    public void actionPerformed(Object action) {
        // Is never called
    }

    @Override
    public void actionStateChanged(Object action, boolean enabled) {
        if (ActionsManager.ACTION_CONTINUE == action) {
            setEnabled (
                ActionsManager.ACTION_RUN_INTO_METHOD,
                enabled &&
                (debugger.getState () == JPDADebugger.STATE_STOPPED) &&
                (EditorContextBridge.getContext().getCurrentLineNumber () >= 0) && 
                (EditorContextBridge.getContext().getCurrentURL ().endsWith (".java"))
            );
        }
    }
}
