/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.debugger.jpda;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.StepRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.ClassType;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDAStep;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;

import org.netbeans.modules.debugger.jpda.breakpoints.MethodBreakpointImpl;
import org.netbeans.modules.debugger.jpda.util.Executor;
import org.netbeans.modules.debugger.jpda.actions.StepIntoActionProvider;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.models.ReturnVariableImpl;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;
import org.openide.ErrorManager;


public class JPDAStepImpl extends JPDAStep implements Executor {
    
    private static Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.step"); // NOI18N

    private static final String INIT = "<init>"; // NOI18N

    /** The source tree with location info of this step */
    //private ASTL stepASTL;
    private Operation[] currentOperations;
    private Operation lastOperation;
    private MethodExitBreakpointListener lastMethodExitBreakpointListener;
    private Set<BreakpointRequest> operationBreakpoints;
    private StepRequest boundaryStepRequest;
    //private SingleThreadedStepWatch stepWatch;
    private Set<EventRequest> requestsToCancel = new HashSet<EventRequest>();
    
    private Session session;
    
    public JPDAStepImpl(JPDADebugger debugger, Session session, int size, int depth) {
        super(debugger, size, depth);
        this.session = session;
    }
    
    public void addStep(JPDAThread tr) {
        JPDADebuggerImpl debuggerImpl = (JPDADebuggerImpl) debugger;
        JPDAThreadImpl trImpl = (JPDAThreadImpl) tr;
        VirtualMachine vm = debuggerImpl.getVirtualMachine();
        if (vm == null) {
            return ; // The session has finished
        }
        SourcePath sourcePath = ((JPDADebuggerImpl) debugger).getEngineContext();
        boolean[] setStoppedStateNoContinue = new boolean[] { false };
        synchronized (((JPDADebuggerImpl) debugger).LOCK) {
        // synchronize on debugger LOCK first so that it can not happen that we
        // take locks in the oposite order
        synchronized (tr) {
            ((JPDAThreadImpl) tr).waitUntilMethodInvokeDone();
            EventRequestManager erm = vm.eventRequestManager();
            //Remove all step requests -- TODO: Do we want it?
            List<StepRequest> stepRequests = erm.stepRequests();
            erm.deleteEventRequests(stepRequests);
            for (StepRequest stepRequest : stepRequests) {
                //SingleThreadedStepWatch.stepRequestDeleted(stepRequest);
                debuggerImpl.getOperator().unregister(stepRequest);
            }
            int size = getSize();
            boolean stepAdded = false;
            logger.log(Level.FINE, "Step "+((size == JPDAStep.STEP_OPERATION) ? "operation" : "line")
                       +" "+((getDepth() == JPDAStep.STEP_INTO) ? "into" :
                           ((getDepth() == JPDAStep.STEP_OVER) ? "over" : "out"))
                       +" in thread "+tr.getName());
            if (size == JPDAStep.STEP_OPERATION) {
                stepAdded = addOperationStep(trImpl, false, sourcePath,
                                             setStoppedStateNoContinue);
                if (!stepAdded) {
                    size = JPDAStep.STEP_LINE;
                    logger.log(Level.FINE, "Operation step changed to line step");
                }
            }
            if (!stepAdded) {
                StepRequest stepRequest = vm.eventRequestManager().createStepRequest(
                    trImpl.getThreadReference(),
                    size,
                    getDepth()
                );
                //stepRequest.addCountFilter(1); - works bad with exclusion filters!
                String[] exclusionPatterns = debuggerImpl.getSmartSteppingFilter().getExclusionPatterns();
                for (int i = 0; i < exclusionPatterns.length; i++) {
                    stepRequest.addClassExclusionFilter(exclusionPatterns [i]);
                    logger.finer("   add pattern: "+exclusionPatterns[i]);
                }
                debuggerImpl.getOperator().register(stepRequest, this);
                stepRequest.setSuspendPolicy(debugger.getSuspend());

                try {
                    stepRequest.enable ();
                    trImpl.setInStep(true, stepRequest);
                    requestsToCancel.add(stepRequest);
                } catch (IllegalThreadStateException itsex) {
                    // the thread named in the request has died.
                    debuggerImpl.getOperator().unregister(stepRequest);
                    stepRequest = null;
                }
            }
        }
        }
        if (setStoppedStateNoContinue[0]) {
            debuggerImpl.setStoppedStateNoContinue(trImpl.getThreadReference());
        }
    }
    
    private boolean addOperationStep(JPDAThreadImpl tr, boolean lineStepExec,
                                     SourcePath sourcePath,
                                     boolean[] setStoppedStateNoContinue) {
        ThreadReference trRef = tr.getThreadReference();
        StackFrame sf;
        try {
            sf = trRef.frame(0);
        } catch (IncompatibleThreadStateException itsex) {
            return false;
        }
        Location loc = sf.location();
        Session currentSession = DebuggerManager.getDebuggerManager().getCurrentSession();
        String language = currentSession == null ? null : currentSession.getCurrentLanguage();
        String url = sourcePath.getURL(loc, language);
        ExpressionPool exprPool = ((JPDADebuggerImpl) debugger).getExpressionPool();
        ExpressionPool.Expression expr = exprPool.getExpressionAt(loc, url);
        if (expr == null) {
            return false;
        }
        Operation[] ops = expr.getOperations();
        
        //Operation operation = null;
        int opIndex = -1;
        int codeIndex = (int) loc.codeIndex();
        if (codeIndex <= ops[0].getBytecodeIndex()) {
            if (!lineStepExec) {
                tr.clearLastOperations();
            }
            // We're at the beginning. Just take the first operation
            if (!ops[0].equals(tr.getCurrentOperation())) {
                opIndex = expr.findNextOperationIndex(codeIndex - 1);
                if (opIndex >= 0 && ops[opIndex].getBytecodeIndex() == codeIndex) {
                    tr.setCurrentOperation(ops[opIndex]);
                    if (lineStepExec) {
                        return false;
                    }
                    if (! getHidden()) {
                        setStoppedStateNoContinue[0] = true;
                    }
                    return true;
                }
            }
        }
        Operation currentOp = tr.getCurrentOperation();
        if (currentOp != null) {
            Operation theLastOperation = null;
            java.util.List<Operation> lastOperations = tr.getLastOperations();
            if (lastOperations != null && lastOperations.size() > 0) {
                theLastOperation = lastOperations.get(lastOperations.size() - 1);
            }
            if (theLastOperation == currentOp) {
                // We're right after some operation
                // Check, whether there is some other operation directly on this
                // position. If yes, it must be executed next.
                for (Operation op : ops) {
                    if (op.getBytecodeIndex() == codeIndex) {
                        tr.setCurrentOperation(op);
                        if (! getHidden()) {
                            setStoppedStateNoContinue[0] = true;
                        }
                        return true;
                    }
                }
            }
        }
        this.lastOperation = currentOp;
        VirtualMachine vm = loc.virtualMachine();
        if (lastOperation != null) {
             // Set the method exit breakpoint to get the return value
            String methodName = lastOperation.getMethodName();
            // We can not get return values from constructors. Do not submit method exit breakpoint.
            if (methodName != null && !INIT.equals(methodName) &&
                MethodBreakpointImpl.canGetMethodReturnValues(vm)) {

                // TODO: Would be nice to know which ObjectReference we're executing the method on
                MethodBreakpoint mb = MethodBreakpoint.create(lastOperation.getMethodClassType(), methodName);
                mb.setClassFilters(createClassFilters(vm, lastOperation.getMethodClassType(), methodName));
                mb.setThreadFilters(debugger, new JPDAThread[] { tr });
                //mb.setMethodName(methodName);
                mb.setBreakpointType(MethodBreakpoint.TYPE_METHOD_EXIT);
                mb.setHidden(true);
                mb.setSuspend(JPDABreakpoint.SUSPEND_NONE);
                lastMethodExitBreakpointListener = new MethodExitBreakpointListener(mb);
                mb.addJPDABreakpointListener(lastMethodExitBreakpointListener);
                // TODO: mb.setSession(debugger);
                try {
                    java.lang.reflect.Method setSessionMethod = JPDABreakpoint.class.getDeclaredMethod("setSession", JPDADebugger.class);
                    setSessionMethod.setAccessible(true);
                    setSessionMethod.invoke(mb, debugger);
                } catch (Exception ex) {
                    org.openide.util.Exceptions.printStackTrace(ex);
                }
                DebuggerManager.getDebuggerManager().addBreakpoint(mb);
            }
        }
        tr.holdLastOperations(true);
        ExpressionPool.OperationLocation[] nextOperationLocations;
        if (opIndex < 0) {
            nextOperationLocations = expr.findNextOperationLocations(codeIndex);
        } else {
            Location[] locations = expr.getLocations();
            nextOperationLocations = new ExpressionPool.OperationLocation[] {
                new ExpressionPool.OperationLocation(ops[opIndex], locations[opIndex], opIndex) };
        }
        boolean isNextOperationFromDifferentExpression = false;
        if (nextOperationLocations != null) {
            //Location[] locations = expr.getLocations();
            /*if (opIndex < 0) {
                // search for an operation on the next line
                expr = exprPool.getExpressionAt(locations[locations.length - 1], url);
                if (expr == null) {
                    logger.log(Level.FINE, "No next operation is available.");
                    return false;
                }
                ops = expr.getOperations();
                opIndex = 0;
                locations = expr.getLocations();
            }*/
            this.operationBreakpoints = new HashSet<BreakpointRequest>();
            // We need to submit breakpoints on the desired operation and all subsequent ones,
            // because some might be skipped due to conditional execution.
            for (int ni = 0; ni < nextOperationLocations.length; ni++) {
                Location nloc = nextOperationLocations[ni].getLocation();
                if (nextOperationLocations[ni].getIndex() < 0) {
                    isNextOperationFromDifferentExpression = true;
                    Operation[] newOps = new Operation[ops.length + 1];
                    System.arraycopy(ops, 0, newOps, 0, ops.length);
                    newOps[ops.length] = nextOperationLocations[ni].getOperation();
                    ops = newOps;
                }
                BreakpointRequest brReq = vm.eventRequestManager().createBreakpointRequest(nloc);
                operationBreakpoints.add(brReq);
                ((JPDADebuggerImpl) debugger).getOperator().register(brReq, this);
                brReq.setSuspendPolicy(debugger.getSuspend());
                brReq.addThreadFilter(trRef);
                brReq.putProperty("thread", trRef); // NOI18N
                brReq.enable();
                tr.setInStep(true, brReq);
                requestsToCancel.add(brReq);
            }
        } else if (lineStepExec) {
            return false;
        }
        
        // We need to also submit a step request so that we're sure that we end up at least on the next execution line
        boundaryStepRequest = vm.eventRequestManager().createStepRequest(
            trRef,
            StepRequest.STEP_LINE,
            StepRequest.STEP_OVER
        );
        if (isNextOperationFromDifferentExpression) {
            boundaryStepRequest.addCountFilter(2);
        } else {
            boundaryStepRequest.addCountFilter(1);
        }
        ((JPDADebuggerImpl) debugger).getOperator().register(boundaryStepRequest, this);
        boundaryStepRequest.setSuspendPolicy(debugger.getSuspend());
        try {
            boundaryStepRequest.enable ();
            requestsToCancel.add(boundaryStepRequest);
        } catch (IllegalThreadStateException itsex) {
            // the thread named in the request has died.
            ((JPDADebuggerImpl) debugger).getOperator().unregister(boundaryStepRequest);
            boundaryStepRequest = null;
            return false;
        }
        
        this.currentOperations = ops;
        return true;
    }
    
    public boolean exec (Event event) {
        stepDone(event.request());
        // TODO: Check the location, follow the smart-stepping logic!
        SourcePath sourcePath = ((JPDADebuggerImpl) debugger).getEngineContext();
        boolean stepAdded = false;
        boolean[] setStoppedStateNoContinue = new boolean[] { false };
        JPDADebuggerImpl debuggerImpl = (JPDADebuggerImpl)debugger;
        JPDAThreadImpl tr;
        synchronized (debuggerImpl.LOCK) {
            tr = (JPDAThreadImpl)debuggerImpl.getCurrentThread();
            VirtualMachine vm = debuggerImpl.getVirtualMachine();
            if (vm == null) {
                return false; // The session has finished
            }
            if (lastMethodExitBreakpointListener != null) {
                Variable returnValue = lastMethodExitBreakpointListener.getReturnValue();
                lastMethodExitBreakpointListener.destroy();
                lastMethodExitBreakpointListener = null;
                lastOperation.setReturnValue(returnValue);
            } else if (lastOperation != null && INIT.equals(lastOperation.getMethodName())) {
                // Set Void as a return value of constructor:
                lastOperation.setReturnValue(new ReturnVariableImpl((JPDADebuggerImpl) debugger, /*vm.mirrorOfVoid() - JDK 6 and newer only!!!*/null, "", INIT));
            }
            if (lastOperation != null) {
                tr.addLastOperation(lastOperation);
            }
            Operation currentOperation = null;
            boolean addExprStep = false;
            if (currentOperations != null) {
                if (event.request() instanceof BreakpointRequest) {
                    long codeIndex = ((BreakpointRequest) event.request()).location().codeIndex();
                    for (int i = 0; i < currentOperations.length; i++) {
                        if (currentOperations[i].getBytecodeIndex() == codeIndex) {
                            currentOperation = currentOperations[i];
                            break;
                        }
                    }
                } else {
                    // A line step was finished, the execution of current expression
                    // has finished, we need to check the expression on this line.
                    addExprStep = true;
                }
                this.currentOperations = null;
            }
            tr.setCurrentOperation(currentOperation);
            EventRequestManager erm = vm.eventRequestManager();
            EventRequest eventRequest = event.request();
            erm.deleteEventRequest(eventRequest);
            debuggerImpl.getOperator().unregister(eventRequest);
            /*if (eventRequest instanceof StepRequest) {
                SingleThreadedStepWatch.stepRequestDeleted((StepRequest) eventRequest);
            }*/
            removed(eventRequest); // Clean-up
            int suspendPolicy = debugger.getSuspend();
            if (addExprStep) {
                stepAdded = addOperationStep(tr, true, sourcePath,
                                             setStoppedStateNoContinue);
            }
            if (!stepAdded) {
                if ((event.request() instanceof StepRequest) && shouldNotStopHere(event)) {
                    return true; // Resume
                }
            }
        }
        if (stepAdded) {
            if (setStoppedStateNoContinue[0]) {
                debuggerImpl.setStoppedStateNoContinue(tr.getThreadReference());
            }
            return true; // Resume
        }
        firePropertyChange(PROP_STATE_EXEC, null, null);
        if (getHidden()) {
            return true; // Resume
        } else {
            tr.holdLastOperations(false);
            return false;
        }
    }
    
    public void removed(EventRequest eventRequest) {
        stepDone(eventRequest);
        if (lastMethodExitBreakpointListener != null) {
            lastMethodExitBreakpointListener.destroy();
            lastMethodExitBreakpointListener = null;
        }
        JPDADebuggerImpl debuggerImpl = (JPDADebuggerImpl)debugger;
        VirtualMachine vm = debuggerImpl.getVirtualMachine();
        if (vm == null) {
            return ; // The session has finished
        }
        EventRequestManager erm = vm.eventRequestManager();
        if (operationBreakpoints != null) {
            for (Iterator<BreakpointRequest> it = operationBreakpoints.iterator(); it.hasNext(); ) {
                BreakpointRequest br = it.next();
                erm.deleteEventRequest(br);
                debuggerImpl.getOperator().unregister(br);
            }
            this.operationBreakpoints = null;
        }
        if (boundaryStepRequest != null) {
            erm.deleteEventRequest(boundaryStepRequest);
            //SingleThreadedStepWatch.stepRequestDeleted(boundaryStepRequest);
            debuggerImpl.getOperator().unregister(boundaryStepRequest);
        }
        
    }

    private void stepDone(EventRequest er) {
        JPDAThreadImpl t;
        if (er instanceof StepRequest) {
            StepRequest sr = (StepRequest) er;
            t = (JPDAThreadImpl) ((JPDADebuggerImpl) debugger).getThread(sr.thread());
        } else {
            ThreadReference tr = (ThreadReference) er.getProperty("thread"); // NOI18N
            if (tr != null) {
                t = (JPDAThreadImpl) ((JPDADebuggerImpl) debugger).getThread(tr);
            } else {
                t = null;
            }
        }
        if (t != null) {
            t.setInStep(false, null);
        }
    }

    /**
     * Returns all class names, which are subclasses of <code>className</code>
     * and contain method <code>methodName</code>
     */
    private static String[] createClassFilters(VirtualMachine vm, String className, String methodName) {
        return createClassFilters(vm, className, methodName, new ArrayList<String>()).toArray(new String[] {});
    }
    
    private static List<String> createClassFilters(VirtualMachine vm, String className, String methodName, List<String> filters) {
        List<ReferenceType> classTypes = vm.classesByName(className);
        for (ReferenceType type : classTypes) {
            List<Method> methods = type.methodsByName(methodName);
            boolean hasNonStatic = methods.isEmpty();
            for (Method method : methods) {
                if (!filters.contains(type.name())) {
                    filters.add(type.name());
                }
                if (!method.isStatic()) {
                    hasNonStatic = true;
                }
            }
            if (hasNonStatic && type instanceof ClassType) {
                ClassType clazz = (ClassType) type;
                ClassType superClass = clazz.superclass();
                if (superClass != null) {
                    createClassFilters(vm, superClass.name(), methodName, filters);
                }
            }
        }
        return filters;
    }
    
    /**
     * Checks for synthetic methods and smart-stepping...
     */
    private boolean shouldNotStopHere(Event ev) {
        JPDADebuggerImpl debuggerImpl = (JPDADebuggerImpl) debugger;
        synchronized (debuggerImpl.LOCK) {
            // 2) init info about current state
            LocatableEvent event = (LocatableEvent) ev;
            String className = event.location ().declaringType ().name ();
            ThreadReference tr = event.thread ();
            //JPDAThreadImpl ct = (JPDAThreadImpl) debuggerImpl.getCurrentThread();
            
            // Synthetic method?
            try {
                if (tr.frame(0).location().method().isSynthetic()) {
                    //S ystem.out.println("In synthetic method -> STEP OVER/OUT again");
                    
                    VirtualMachine vm = debuggerImpl.getVirtualMachine ();
                    if (vm == null) {
                        return false; // The session has finished
                    }
                    StepRequest stepRequest = vm.eventRequestManager ().createStepRequest (
                        tr,
                        StepRequest.STEP_LINE,
                        getDepth()
                    );
                    stepRequest.addCountFilter(1);
                    String[] exclusionPatterns = debuggerImpl.getSmartSteppingFilter().getExclusionPatterns();
                    for (int i = 0; i < exclusionPatterns.length; i++) {
                        stepRequest.addClassExclusionFilter(exclusionPatterns [i]);
                    }
                    debuggerImpl.getOperator ().register (stepRequest, this);
                    stepRequest.setSuspendPolicy (debugger.getSuspend ());
                    try {
                        stepRequest.enable ();
                        requestsToCancel.add(stepRequest);
                    } catch (IllegalThreadStateException itsex) {
                        // the thread named in the request has died.
                        debuggerImpl.getOperator ().unregister (stepRequest);
                    }
                    return true;
                }
            } catch (IncompatibleThreadStateException e) {
                ErrorManager.getDefault().notify(e);
            }
            
            // Not synthetic
            JPDAThread t = debuggerImpl.getThread (tr);
            if (debuggerImpl.stopHere(t)) {
                //S ystem.out.println("/nStepAction.exec end - do not resume");
                return false; // do not resume
            }

            // do not stop here -> start smart stepping!
            VirtualMachine vm = debuggerImpl.getVirtualMachine ();
            if (vm == null) {
                return false; // The session has finished
            }
            int depth;
            Map properties = session.lookupFirst(null, Map.class);
            if (properties != null && properties.containsKey (StepIntoActionProvider.SS_STEP_OUT)) {
                depth = StepRequest.STEP_OUT;
            } else {
                depth = StepRequest.STEP_INTO;
            }
            StepRequest stepRequest = vm.eventRequestManager ().createStepRequest (
                tr,
                StepRequest.STEP_LINE,
                depth
            );
            if (logger.isLoggable(Level.FINE)) {
                try {
                    logger.fine("Can not stop at "+tr.frame(0)+", smart-stepping. Submitting step = "+stepRequest+"; depth = "+depth);
                } catch (IncompatibleThreadStateException ex) {
                    logger.throwing(getClass().getName(), "shouldNotStopHere", ex);
                }
            }
            String[] exclusionPatterns = debuggerImpl.getSmartSteppingFilter().getExclusionPatterns();
            for (int i = 0; i < exclusionPatterns.length; i++) {
                stepRequest.addClassExclusionFilter(exclusionPatterns [i]);
                logger.finer("   add pattern: "+exclusionPatterns[i]);
            }
            
            debuggerImpl.getOperator ().register (stepRequest, this);
            stepRequest.setSuspendPolicy (debugger.getSuspend ());
            try {
                stepRequest.enable ();
                requestsToCancel.add(stepRequest);
            } catch (IllegalThreadStateException itsex) {
                // the thread named in the request has died.
                debuggerImpl.getOperator ().unregister (stepRequest);
            }
            return true; // resume
        }
    }

    /** Cancel this step - remove all submitted event requests. */
    public void cancel() {
        JPDADebuggerImpl debuggerImpl = (JPDADebuggerImpl) debugger;
        VirtualMachine vm = debuggerImpl.getVirtualMachine();
        if (vm == null) return ;
        EventRequestManager erm = vm.eventRequestManager();
        for (EventRequest er : requestsToCancel) {
            erm.deleteEventRequest(er);
            debuggerImpl.getOperator ().unregister (er);
        }
    }
    
    public static final class MethodExitBreakpointListener implements JPDABreakpointListener {
        
        private MethodBreakpoint mb;
        private Variable returnValue;
        
        public MethodExitBreakpointListener(MethodBreakpoint mb) {
            this.mb = mb;
        }
        
        public void breakpointReached(JPDABreakpointEvent event) {
            returnValue = event.getVariable();
        }
        
        public Variable getReturnValue() {
            return returnValue;
        }
        
        public void destroy() {
            mb.removeJPDABreakpointListener(this);
            DebuggerManager.getDebuggerManager().removeBreakpoint(mb);
        }
        
    }
    
    /*public static final class SingleThreadedStepWatch implements Runnable {
        
        private static final int DELAY = 5000;
        
        private static final RequestProcessor stepWatchRP = new RequestProcessor("Debugger Step Watch", 1);
        
        private static final Map<StepRequest, SingleThreadedStepWatch> STEP_WATCH_POOL = new HashMap<StepRequest, SingleThreadedStepWatch>();
        
        private RequestProcessor.Task watchTask;
        private JPDADebuggerImpl debugger;
        private StepRequest request;
        private Dialog dialog;
        private List<JPDAThread> resumedThreads;
        
        public SingleThreadedStepWatch(JPDADebuggerImpl debugger, StepRequest request) {
            this.debugger = debugger;
            this.request = request;
            watchTask = stepWatchRP.post(this, DELAY);
            synchronized (STEP_WATCH_POOL) {
                STEP_WATCH_POOL.put(request, this);
            }
        }
        
        public static void stepRequestDeleted(StepRequest request) {
            SingleThreadedStepWatch stepWatch;
            synchronized (STEP_WATCH_POOL) {
                stepWatch = STEP_WATCH_POOL.remove(request);
            }
            if (stepWatch != null) stepWatch.done();
        }
        
        public void done() {
            synchronized (this) {
                if (watchTask == null) return;
                watchTask.cancel();
                watchTask = null;
                if (dialog != null) {
                    dialog.setVisible(false);
                }
                if (resumedThreads != null) {
                    synchronized (debugger.LOCK) {
                        suspendThreads(resumedThreads);
                    }
                    resumedThreads = null;
                }
            }
            synchronized (STEP_WATCH_POOL) {
                STEP_WATCH_POOL.remove(request);
            }
        }
    
        public void run() {
            synchronized (this) {
                if (watchTask == null) return ; // We're done
                try {
                    if (request.thread().isSuspended()) {
                        watchTask.schedule(DELAY);
                        return ;
                    }
                    if (request.thread().status() == ThreadReference.THREAD_STATUS_ZOMBIE) {
                        // Do not wait for zombie!
                        return ;
                    }
                } catch (VMDisconnectedException vmdex) {
                    // Do not wait for finished/disconnected threads
                    return ;
                }
                if (!request.isEnabled()) {
                    return ;
                }
                Boolean resumeDecision = debugger.getSingleThreadStepResumeDecision();
                if (resumeDecision != null) {
                    if (resumeDecision.booleanValue()) {
                        doResume();
                    }
                    return ;
                }
            }
            String message = NbBundle.getMessage(JPDAStepImpl.class, "SingleThreadedStepBlocked");
            JCheckBox cb = new JCheckBox(NbBundle.getMessage(JPDAStepImpl.class, "RememberDecision"));
            final boolean[] yes = new boolean[] { false, false };
            DialogDescriptor dd = new DialogDescriptor(
                    //message,
                    createDlgPanel(message, cb),
                    new NotifyDescriptor.Confirmation(message, NotifyDescriptor.YES_NO_OPTION).getTitle(),
                    true,
                    NotifyDescriptor.YES_NO_OPTION,
                    null,
                    new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            synchronized (yes) {
                                yes[0] = evt.getSource() == NotifyDescriptor.YES_OPTION;
                                yes[1] = evt.getSource() == NotifyDescriptor.NO_OPTION;
                            }
                        }
                    });
            dd.setMessageType(NotifyDescriptor.QUESTION_MESSAGE);
            Dialog theDialog;
            synchronized (this) {
                dialog = org.openide.DialogDisplayer.getDefault().createDialog(dd);
                theDialog = dialog;
            }
            theDialog.setVisible(true);
            boolean doResume;
            synchronized (yes) {
                doResume = yes[0];
            }
            synchronized (this) {
                dialog = null;
                if (watchTask == null) return ;
                if ((yes[0] || yes[1]) && cb.isSelected()) {
                    debugger.setSingleThreadStepResumeDecision(Boolean.valueOf(yes[0]));
                }
                if (doResume) {
                    doResume();
                }
            }
            /*
            Object option = org.openide.DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Confirmation(message, NotifyDescriptor.YES_NO_OPTION));
            if (NotifyDescriptor.YES_OPTION == option) {
                debugger.resume();
            }
             */  /*
        }
        
        private static JPanel createDlgPanel(String message, JCheckBox cb) {
            JPanel panel = new JPanel();
            panel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.WEST;
            JTextArea area = new JTextArea(message);
            Color color = UIManager.getColor("Label.background"); // NOI18N
            if (color != null) {
                area.setBackground(color);
            }
            //area.setLineWrap(true);
            //area.setWrapStyleWord(true);
            area.setEditable(false);
            area.setTabSize(4); // looks better for module sys messages than 8
            panel.add(area, c);
            c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 1;
            c.anchor = GridBagConstraints.WEST;
            c.insets = new java.awt.Insets(12, 0, 0, 0);
            panel.add(cb, c);
            return panel;
        }
        
        private void doResume() {
            synchronized (debugger.LOCK) {
                List<JPDAThread> suspendedThreads = new ArrayList<JPDAThread>();
                JPDAThreadGroup[] tgs = debugger.getTopLevelThreadGroups();
                for (JPDAThreadGroup tg: tgs) {
                    fillSuspendedThreads(tg, suspendedThreads);
                }
                resumeThreads(suspendedThreads);
                resumedThreads = suspendedThreads;
            }
        }
        
        private static void fillSuspendedThreads(JPDAThreadGroup tg, List<JPDAThread> sts) {
            for (JPDAThread t : tg.getThreads()) {
                if (t.isSuspended()) sts.add(t);
            }
            for (JPDAThreadGroup tgg : tg.getThreadGroups()) {
                fillSuspendedThreads(tgg, sts);
            }
        }
        
        private static void suspendThreads(List<JPDAThread> ts) {
            for (JPDAThread t : ts) {
                t.suspend();
            }
        }
        
        private static void resumeThreads(List<JPDAThread> ts) {
            for (JPDAThread t : ts) {
                t.resume();
            }
        }
        
    }*/

}
