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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.InternalException;
import com.sun.jdi.Location;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.StepRequest;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.api.debugger.jpda.MonitorInfo;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.Java6Methods;
import org.netbeans.modules.debugger.jpda.SingleThreadWatcher;
import org.netbeans.modules.debugger.jpda.util.Executor;
import org.netbeans.modules.debugger.jpda.util.Operator;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * The implementation of JPDAThread.
 */
public final class JPDAThreadImpl implements JPDAThread, Customizer {

    private static final String PROP_LOCKER_THREADS = "lockerThreads"; // NOI18N
    private static final String PROP_STEP_SUSPENDED_BY_BREAKPOINT = "stepSuspendedByBreakpoint"; // NOI18N
    
    private ThreadReference     threadReference;
    private JPDADebuggerImpl    debugger;
    private boolean             suspended;
    private int                 suspendCount;
    private Operation           currentOperation;
    private List<Operation>     lastOperations;
    private boolean             doKeepLastOperations;
    private ReturnVariableImpl  returnVariable;
    private PropertyChangeSupport pch = new PropertyChangeSupport(this);
    private CallStackFrame[]    cachedFrames;
    private int                 cachedFramesFrom = -1;
    private int                 cachedFramesTo = -1;
    private final Object        cachedFramesLock = new Object();
    private JPDABreakpoint      currentBreakpoint;
    private String              threadName;
    private final Object        lockerThreadsLock = new Object();
    //private Map<JPDAThread, Variable> lockerThreads;
    //private Map<ThreadReference, ObjectReference> lockerThreads2;
    private ObjectReference     lockerThreadsMonitor;
    private List<JPDAThread>    lockerThreadsList;
    private List<ThreadReference> resumedBlockingThreads;
    private final Object        stepBreakpointLock = new Object();
    private JPDABreakpoint      stepSuspendedByBreakpoint;

    public JPDAThreadImpl (
        ThreadReference     threadReference,
        JPDADebuggerImpl    debugger
    ) {
        this.threadReference = threadReference;
        this.debugger = debugger;
        boolean initFailed = false;
        threadName = "";
        try {
            threadName = threadReference.name();
            suspended = threadReference.isSuspended();
            suspendCount = threadReference.suspendCount();
        } catch (IllegalThreadStateException itsex) {
            initFailed = true;
        } catch (ObjectCollectedException ex) {
            initFailed = true;
        } catch (VMDisconnectedException ex) {
            initFailed = true;
        }
        if (initFailed) {
            suspended = false;
            suspendCount = 0;
        }
    }

    /**
     * Getter for the name of thread property.
     *
     * @return name of thread.
     */
    public String getName () {
        return threadName;
    }
    
    /**
    * Returns parent thread group.
    *
    * @return parent thread group.
    */
    public JPDAThreadGroup getParentThreadGroup () {
        try {
            ThreadGroupReference tgr = threadReference.threadGroup ();
            if (tgr == null) return null;
            return debugger.getThreadGroup(tgr);
        } catch (IllegalThreadStateException ex) {
            return null; // Thrown when thread has exited
        } catch (ObjectCollectedException ex) {
            return null;
        } catch (VMDisconnectedException ex) {
            return null;
        }
    }

    /**
     * Returns line number of the location this thread stopped at.
     * The thread should be suspended at the moment this method is called.
     *
     * @return  line number of the current location if the thread is suspended,
     *          contains at least one frame and the topmost frame does not
     *          represent a native method invocation; <CODE>-1</CODE> otherwise
     * @see  CallStackFrame
    */
    public int getLineNumber (String stratum) {
        try {
            if (threadReference.frameCount () < 1) return -1;
            return threadReference.frame (0).location ().lineNumber (stratum);
        } catch (ObjectCollectedException ex) {
        } catch (InvalidStackFrameException ex) {
        } catch (IncompatibleThreadStateException ex) {
        } catch (IllegalThreadStateException ex) {
            // Thrown when thread has exited
        } catch (VMDisconnectedException ex) {
        }
        return -1;
    }
    
    public synchronized Operation getCurrentOperation() {
        return currentOperation;
    }
    
    public synchronized void setCurrentOperation(Operation operation) { // Set the current operation for the default stratum.
        this.currentOperation = operation;
    }

    public synchronized List<Operation> getLastOperations() {
        return lastOperations;
    }
    
    public synchronized void addLastOperation(Operation operation) {
        if (lastOperations == null) {
            lastOperations = new ArrayList<Operation>();
        }
        lastOperations.add(operation);
    }
    
    public synchronized void clearLastOperations() {
        if (lastOperations != null) {
            for (Operation last : lastOperations) {
                last.setReturnValue(null); // reset the returned value.
                // Operation might be reused, but the execution path is gone.
            }
        }
        lastOperations = null;
    }
    
    public synchronized void holdLastOperations(boolean doHold) {
        doKeepLastOperations = doHold;
    }

    public synchronized JPDABreakpoint getCurrentBreakpoint() {
        return currentBreakpoint;
    }

    public void setCurrentBreakpoint(JPDABreakpoint currentBreakpoint) {
        JPDABreakpoint oldBreakpoint;
        synchronized (this) {
            oldBreakpoint = this.currentBreakpoint;
            this.currentBreakpoint = currentBreakpoint;
        }
        pch.firePropertyChange(PROP_BREAKPOINT, oldBreakpoint, currentBreakpoint);
    }


    /**
     * Returns current state of this thread.
     *
     * @return current state of this thread
     */
    public int getState () {
        try {
            return threadReference.status ();
        } catch (IllegalThreadStateException ex) {
            // Thrown when thread has exited
        } catch (ObjectCollectedException ex) {
        } catch (VMDisconnectedException ex) {
        }
        return STATE_UNKNOWN;
    }
    
    /**
     * Returns true if this thread is suspended by debugger.
     *
     * @return true if this thread is suspended by debugger
     */
    public synchronized boolean isSuspended () {
        return suspended;
    }
    
    /**
     * Returns true if this thread is suspended by debugger.
     *
     * @return true if this thread is suspended by debugger
     */
    public boolean isThreadSuspended () {
        try {
            return threadReference.isSuspended ();
        } catch (IllegalThreadStateException ex) {
            // Thrown when thread has exited
        } catch (ObjectCollectedException ex) {
        } catch (VMDisconnectedException ex) {
        }
        return false;
    }

    /**
    * If this thread is suspended returns class name where this thread is stopped.
    *
    * @return class name where this thread is stopped.
    */
    public String getClassName () {
        try {
            if (threadReference.frameCount () < 1) return "";
            return threadReference.frame (0).location ().declaringType ().name ();
        } catch (ObjectCollectedException ex) {
        } catch (InvalidStackFrameException ex) {
        } catch (IncompatibleThreadStateException ex) {
        } catch (IllegalThreadStateException ex) {
            // Thrown when thread has exited
        } catch (VMDisconnectedException ex) {
        }
        return "";
    }

    /**
    * If this thread is suspended returns method name where this thread is stopped.
    *
    * @return method name where this thread is stopped.
    */
    public String getMethodName () {
        try {
            if (threadReference.frameCount () < 1) return "";
            return threadReference.frame (0).location ().method ().name ();
        } catch (ObjectCollectedException ex) {
        } catch (InvalidStackFrameException ex) {
        } catch (IncompatibleThreadStateException ex) {
        } catch (IllegalThreadStateException ex) {
            // Thrown when thread has exited
        } catch (VMDisconnectedException ex) {
        }
        return "";
    }
    
    /**
    * Returns name of file of this frame or null if thread has no frame.
    *
    * @return Returns name of file of this frame.
    */
    public String getSourceName (String stratum) throws AbsentInformationException {
        try {
            if (threadReference.frameCount () < 1) return "";
            return threadReference.frame (0).location ().sourceName (stratum);
        } catch (ObjectCollectedException ex) {
        } catch (InvalidStackFrameException ex) {
        } catch (IncompatibleThreadStateException ex) {
        } catch (IllegalThreadStateException ex) {
            // Thrown when thread has exited
        } catch (VMDisconnectedException ex) {
        }
        return "";
    }
    
    /**
    * Returns name of file of this frame or null if thread has no frame.
    *
    * @return Returns name of file of this frame.
    */
    public String getSourcePath (String stratum) 
    throws AbsentInformationException {
        try {
            if (threadReference.frameCount () < 1) return "";
            return threadReference.frame (0).location ().sourcePath (stratum);
        } catch (ObjectCollectedException ex) {
        } catch (InvalidStackFrameException ex) {
        } catch (IncompatibleThreadStateException ex) {
        } catch (IllegalThreadStateException ex) {
            // Thrown when thread has exited
        } catch (VMDisconnectedException ex) {
        }
        return "";
    }
    
    /**
     * Returns call stack for this thread.
     *
     * @throws AbsentInformationException if the thread is running or not able
     *         to return callstack. If the thread is in an incompatible state
     *         (e.g. running), the AbsentInformationException has
     *         IncompatibleThreadStateException as a cause.
     *         If the thread is collected, the AbsentInformationException has
     *         ObjectCollectedException as a cause.
     * @return call stack
     */
    public CallStackFrame[] getCallStack () throws AbsentInformationException {
        return getCallStack (0, getStackDepth ());
    }
    
    /**
     * Returns call stack for this thread on the given indexes.
     *
     * @param from a from index, inclusive
     * @param to a to index, exclusive
     * @throws AbsentInformationException if the thread is running or not able
     *         to return callstack. If the thread is in an incompatible state
     *         (e.g. running), the AbsentInformationException has
     *         IncompatibleThreadStateException as a cause.
     *         If the thread is collected, the AbsentInformationException has
     *         ObjectCollectedException as a cause.
     * @return call stack
     */
    public CallStackFrame[] getCallStack (int from, int to) 
    throws AbsentInformationException {
        try {
            List l;
            CallStackFrame[] theCachedFrames = null;
            synchronized (this) {
                int max = threadReference.frameCount();
                from = Math.min(from, max);
                to = Math.min(to, max);
                if (to - from > 1) {  /*TODO: Frame caching cause problems with invalid frames. Some fix is necessary...
                 *  as a workaround, frames caching is disabled.*/
                    synchronized (cachedFramesLock) {
                        if (from == cachedFramesFrom && to == cachedFramesTo) {
                            return cachedFrames;
                        }
                        if (from >= cachedFramesFrom && to <= cachedFramesTo) {
                            // TODO: Arrays.copyOfRange(cachedFrames, from - cachedFramesFrom, to);
                            return copyOfRange(cachedFrames, from - cachedFramesFrom, to - cachedFramesFrom);
                        }
                        if (cachedFramesFrom >= 0 && cachedFramesTo > cachedFramesFrom) {
                            int length = to - from;
                            theCachedFrames = new CallStackFrame[length];
                            for (int i = 0; i < length; i++) {
                                if (i >= cachedFramesFrom && i < cachedFramesTo) {
                                    theCachedFrames[i] = cachedFrames[i - cachedFramesFrom];
                                } else {
                                    theCachedFrames[i] = null;
                                }
                            }
                        }
                    }
                }
                if (from < 0) {
                    throw new IndexOutOfBoundsException("from = "+from);
                }
                if (from == to) {
                    return new CallStackFrame[0];
                }
                if (from >= max) {
                    throw new IndexOutOfBoundsException("from = "+from+" is too high, frame count = "+max);
                }
                int length = to - from;
                if (length < 0 || (from+length) > max) {
                    throw new IndexOutOfBoundsException("from = "+from+", to = "+to+", frame count = "+max);
                }
                try {
                    l = threadReference.frames (from, length);
                } catch (IndexOutOfBoundsException ioobex) {
                    ioobex = Exceptions.attachMessage(ioobex, "from = "+from+", to = "+to+", frame count = "+max+", length = "+length+", fresh frame count = "+threadReference.frameCount());
                    throw ioobex;
                }
            }
            int n = l.size();
            CallStackFrame[] frames = new CallStackFrame[n];
            for (int i = 0; i < n; i++) {
                if (theCachedFrames != null && theCachedFrames[i] != null) {
                    frames[i] = theCachedFrames[i];
                } else {
                    frames[i] = new CallStackFrameImpl(this, (StackFrame) l.get(i), from + i, debugger);
                }
                if (from == 0 && i == 0 && currentOperation != null) {
                    ((CallStackFrameImpl) frames[i]).setCurrentOperation(currentOperation);
                }
            }
            //if (to - from > 1) {
                synchronized (cachedFramesLock) {
                    cachedFrames = frames;
                    cachedFramesFrom = from;
                    cachedFramesTo = to;
                }
            //}
            return frames;
        } catch (IncompatibleThreadStateException ex) {
            AbsentInformationException aiex = new AbsentInformationException(ex.getLocalizedMessage());
            aiex.initCause(ex);
            throw aiex;
        } catch (InvalidStackFrameException ex) {
            AbsentInformationException aiex = new AbsentInformationException(ex.getLocalizedMessage());
            aiex.initCause(ex);
            throw aiex;
        } catch (ObjectCollectedException ocex) {
            AbsentInformationException aiex = new AbsentInformationException(ocex.getLocalizedMessage());
            aiex.initCause(ocex);
            throw aiex;
        } catch (IllegalThreadStateException itsex) {
            // Thrown when thread has exited
            AbsentInformationException aiex = new AbsentInformationException(itsex.getLocalizedMessage());
            aiex.initCause(itsex);
            throw aiex;
        } catch (VMDisconnectedException ex) {
            return new CallStackFrame [0];
        }
    }
    
    private static CallStackFrame[] copyOfRange(CallStackFrame[] original, int from, int to) {
        // TODO: Use Arrays.copyOfRange(cachedFrames, from, to);
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        CallStackFrame[] copy = new CallStackFrame[newLength];
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }

    private void cleanCachedFrames() {
        synchronized (cachedFramesLock) {
            cachedFrames = null;
            cachedFramesFrom = -1;
            cachedFramesTo = -1;
        }
    }
    
    /**
     * Returns length of current call stack.
     *
     * @return length of current call stack
     */
    public int getStackDepth () {
        try {
            return threadReference.frameCount ();
        } catch (IllegalThreadStateException ex) {
            // Thrown when thread has exited
        } catch (ObjectCollectedException ex) {
        } catch (IncompatibleThreadStateException e) {
        } catch (VMDisconnectedException e) {
        }
        return 0;
    }
    
    public void popFrames(StackFrame sf) throws IncompatibleThreadStateException {
        try {
            notifyToBeResumed();
            threadReference.popFrames(sf);
            cleanCachedFrames();
            setReturnVariable(null); // Clear the return var
        } catch (IllegalThreadStateException ex) {
            throw new IncompatibleThreadStateException("Thread exited.");
        } catch (ObjectCollectedException ex) {
            throw new IncompatibleThreadStateException("Thread died.");
        } catch (NativeMethodException nmex) {
            cleanCachedFrames();
            ErrorManager.getDefault().notify(
                    ErrorManager.getDefault().annotate(nmex,
                        NbBundle.getMessage(JPDAThreadImpl.class, "MSG_NativeMethodPop")));
        } catch (InternalException iex) {
            cleanCachedFrames();
            if (iex.errorCode() == 32) {
                ErrorManager.getDefault().notify(
                        ErrorManager.getDefault().annotate(iex,
                            NbBundle.getMessage(JPDAThreadImpl.class, "MSG_NativeMethodPop")));
            } else {
                throw iex;
            }
        } finally {
            notifySuspended();
        }
    }
    
    /**
     * Suspends thread.
     */
    public void suspend () {
        Boolean suspendedToFire = null;
        synchronized (this) {
            try {
                if (!isSuspended ()) {
                    threadReference.suspend ();
                    suspendedToFire = Boolean.TRUE;
                    suspendCount++;
                    threadName = threadReference.name();
                }
                //System.err.println("suspend("+getName()+") suspended = true");
                suspended = true;
            } catch (IllegalThreadStateException ex) {
                // Thrown when thread has exited
            } catch (ObjectCollectedException ex) {
            } catch (VMDisconnectedException ex) {
            }
        }
        if (suspendedToFire != null) {
            pch.firePropertyChange(PROP_SUSPENDED,
                    Boolean.valueOf(!suspendedToFire.booleanValue()),
                    suspendedToFire);
        }
    }
    
    /**
     * Unsuspends thread.
     */
    public void resume () {
        if (this == debugger.getCurrentThread()) {
            boolean can = debugger.currentThreadToBeResumed();
            if (!can) return ;
        }
        Boolean suspendedToFire = null;
        synchronized (this) {
            waitUntilMethodInvokeDone();
            setReturnVariable(null); // Clear the return var on resume
            setCurrentOperation(null);
            currentBreakpoint = null;
            if (!doKeepLastOperations) {
                clearLastOperations();
            }
            try {
                if (isSuspended ()) {
                    int count = threadReference.suspendCount ();
                    while (count > 0) {
                        threadReference.resume (); count--;
                    }
                    suspendedToFire = Boolean.FALSE;
                }
                suspendCount = 0;
                //System.err.println("resume("+getName()+") suspended = false");
                suspended = false;
                methodInvokingDisabledUntilResumed = false;
            } catch (IllegalThreadStateException ex) {
                // Thrown when thread has exited
            } catch (ObjectCollectedException ex) {
            } catch (VMDisconnectedException ex) {
            }
        }
        JPDABreakpoint brkp = null;
        synchronized (stepBreakpointLock) {
            if (stepSuspendedByBreakpoint != null) {
                brkp = stepSuspendedByBreakpoint;
            }
        }
        if (brkp != null) {
            pch.firePropertyChange(PROP_STEP_SUSPENDED_BY_BREAKPOINT, brkp, null);
        }
        cleanCachedFrames();
        if (suspendedToFire != null) {
            pch.firePropertyChange(PROP_SUSPENDED,
                    Boolean.valueOf(!suspendedToFire.booleanValue()),
                    suspendedToFire);
        }
    }
    
    public void notifyToBeResumed() {
        //System.err.println("notifyToBeResumed("+getName()+")");
        List<PropertyChangeEvent> evts = notifyToBeRunning(true, true);
        for (PropertyChangeEvent evt : evts) {
            pch.firePropertyChange(evt);
        }
    }
    
    private List<PropertyChangeEvent> notifyToBeRunning(boolean clearVars, boolean resumed) {
        Boolean suspendedToFire = null;
        synchronized (this) {
            if (resumed) {
                waitUntilMethodInvokeDone();
            }
            //System.err.println("notifyToBeRunning("+getName()+"), resumed = "+resumed+", suspendCount = "+suspendCount+", thread's suspendCount = "+threadReference.suspendCount());
            if (resumed && (--suspendCount > 0)) return Collections.emptyList();
            //System.err.println("  suspendCount = 0, var suspended = "+suspended);
            suspendCount = 0;
            if (clearVars) {
                setCurrentOperation(null);
                setReturnVariable(null); // Clear the return var on resume
                currentBreakpoint = null;
                if (!doKeepLastOperations) {
                    clearLastOperations();
                }
            }
            if (suspended) {
                //System.err.println("notifyToBeRunning("+getName()+") suspended = false");
                suspended = false;
                suspendedToFire = Boolean.FALSE;
                methodInvokingDisabledUntilResumed = false;
            }
        }
        cleanCachedFrames();
        PropertyChangeEvent stepBrkpEvt = null;
        synchronized (stepBreakpointLock) {
            if (stepSuspendedByBreakpoint != null) {
                stepBrkpEvt = new PropertyChangeEvent(this, PROP_STEP_SUSPENDED_BY_BREAKPOINT,
                        stepSuspendedByBreakpoint, null);
                stepSuspendedByBreakpoint = null;
            }
        }
        if (suspendedToFire != null) {
            PropertyChangeEvent suspEvt = new PropertyChangeEvent(this, PROP_SUSPENDED,
                    Boolean.valueOf(!suspendedToFire.booleanValue()),
                    suspendedToFire);
            if (stepBrkpEvt != null) {
                return Arrays.asList(new PropertyChangeEvent[] {stepBrkpEvt, suspEvt});
            } else {
                return Collections.singletonList(suspEvt);
            }
        } else {
            if (stepBrkpEvt != null) {
                return Collections.singletonList(stepBrkpEvt);
            }
            return Collections.emptyList();
        }
    }
    
    public void notifySuspended() {
        notifySuspended(true);
    }

    private void notifySuspended(boolean doFire) {
        Boolean suspendedToFire = null;
        synchronized (this) {
            try {
                suspendCount = threadReference.suspendCount();
            } catch (IllegalThreadStateException ex) {
                return ; // Thrown when thread has exited
            } catch (ObjectCollectedException ocex) {
                return ; // The thread is gone
            }
            //System.err.println("notifySuspended("+getName()+") suspendCount = "+suspendCount+", var suspended = "+suspended);
            if (!suspended && isThreadSuspended()) {
                //System.err.println("  setting suspended = true");
                suspended = true;
                suspendedToFire = Boolean.TRUE;
                threadName = threadReference.name();
            }
        }
        if (suspendedToFire != null && doFire) {
            pch.firePropertyChange(PROP_SUSPENDED,
                    Boolean.valueOf(!suspendedToFire.booleanValue()),
                    suspendedToFire);
        }
    }

    private SingleThreadWatcher watcher = null;
    
    private boolean methodInvoking;
    private boolean methodInvokingDisabledUntilResumed;
    private boolean resumedToFinishMethodInvocation;
    private boolean unsuspendedStateWhenInvoking;
    
    public void notifyMethodInvoking() throws PropertyVetoException {
        List<PropertyChangeEvent> evts;
        synchronized (this) {
            if (methodInvokingDisabledUntilResumed) {
                throw new PropertyVetoException(
                        NbBundle.getMessage(JPDAThreadImpl.class, "MSG_DisabledUntilResumed"), null);
            }
            if (methodInvoking) {
                throw new PropertyVetoException(
                        NbBundle.getMessage(JPDAThreadImpl.class, "MSG_AlreadyInvoking"), null);
            }
            if (!isThreadSuspended()) {
                throw new PropertyVetoException(
                        NbBundle.getMessage(JPDAThreadImpl.class, "MSG_NoCurrentContext"), null);
            }
            methodInvoking = true;
            unsuspendedStateWhenInvoking = !isSuspended();
            if (unsuspendedStateWhenInvoking) {
                // Do not notify running state when was not suspended.
                evts = Collections.emptyList();
            } else {
                evts = notifyToBeRunning(false, false);
            }
            watcher = new SingleThreadWatcher(this);
        }
        for (PropertyChangeEvent evt : evts) {
            pch.firePropertyChange(evt);
        }
    }
    
    public void notifyMethodInvokeDone() {
        SingleThreadWatcher watcherToDestroy = null;
        boolean wasUnsuspendedStateWhenInvoking;
        synchronized (this) {
            // HACK becuase of JDI, we've resumed this thread so that method invocation can be finished.
            // We need to suspend the thread immediately so that it does not continue after the invoke has finished.
            if (resumedToFinishMethodInvocation) {
                threadReference.suspend();
                //System.err.println("\""+getName()+"\""+":  Suspended after method invocation.");
                resumedToFinishMethodInvocation = false;
            }
            methodInvoking = false;
            wasUnsuspendedStateWhenInvoking = unsuspendedStateWhenInvoking;
            unsuspendedStateWhenInvoking = false;
            this.notifyAll();
            watcherToDestroy = watcher;
            watcher = null;
        }
        if (watcherToDestroy != null) {
            watcherToDestroy.destroy();
        }
        // Do not notify suspended state when was already unsuspended when started invoking.
        if (!wasUnsuspendedStateWhenInvoking) {
            notifySuspended();
        }
    }
    
    public synchronized boolean isMethodInvoking() {
        return methodInvoking;
    }
    
    public void waitUntilMethodInvokeDone() {
        synchronized (this) {
            while (methodInvoking) {
                try {
                    this.wait();
                } catch (InterruptedException iex) {
                    break;
                }
            }
        }
    }
    
    public synchronized void disableMethodInvokeUntilResumed() {
        methodInvokingDisabledUntilResumed = true;
    }

    private boolean inStep = false;

    public void setInStep(boolean inStep, EventRequest stepRequest) {
        SingleThreadWatcher watcherToDestroy = null;
        synchronized (this) {
            this.inStep = inStep;
            if (inStep) {
                if (stepRequest.suspendPolicy() == StepRequest.SUSPEND_EVENT_THREAD) {
                    watcher = new SingleThreadWatcher(this);
                }
            } else if (watcher != null) {
                watcherToDestroy = watcher;
                watcher = null;
            }
        }
        if (watcherToDestroy != null) {
            watcherToDestroy.destroy();
        }
    }

    public synchronized boolean isInStep() {
        return inStep;
    }

    public void interrupt() {
        try {
            if (isSuspended ()) return;
            threadReference.interrupt();
        } catch (IllegalThreadStateException ex) {
            // Thrown when thread has exited
        } catch (ObjectCollectedException ex) {
        } catch (VMDisconnectedException ex) {
        }
    }
    
    /**
     * Sets this thread current.
     *
     * @see JPDADebugger#getCurrentThread
     */
    public void makeCurrent () {
        debugger.setCurrentThread (this);
        Session session = debugger.getSession();
        DebuggerManager manager = DebuggerManager.getDebuggerManager();
        if (session != manager.getCurrentSession()) {
            manager.setCurrentSession(session);
        }
    }
    
    /**
     * Returns monitor this thread is waiting on.
     *
     * @return monitor this thread is waiting on
     */
    public ObjectVariable getContendedMonitor () {
        try {
            if (!threadReference.virtualMachine().canGetCurrentContendedMonitor()) {
                return null;
            }
        } catch (IllegalThreadStateException ex) {
            // Thrown when thread has exited
            return null;
        } catch (ObjectCollectedException ocex) {
            return null;
        }
        ObjectReference or;
        synchronized (this) {
            if (!isSuspended()) return null;
            if ("DestroyJavaVM".equals(threadReference.name())) {
                // See defect #6474293
                return null;
            }
            try {
                or = threadReference.currentContendedMonitor ();
            } catch (IllegalThreadStateException ex) {
                // Thrown when thread has exited
                return null;
            } catch (ObjectCollectedException ex) {
                return null;
            } catch (IncompatibleThreadStateException e) {
                String msg = "Thread '"+threadReference.name()+
                             "': status = "+threadReference.status()+
                             ", is suspended = "+threadReference.isSuspended()+
                             ", suspend count = "+threadReference.suspendCount()+
                             ", is at breakpoint = "+threadReference.isAtBreakpoint()+
                             ", internal suspend status = "+suspended;
                Logger.getLogger(JPDAThreadImpl.class.getName()).log(Level.INFO, msg, e);
                return null;
            } catch (com.sun.jdi.InternalException iex) {
                String msg = "Thread '"+threadReference.name()+
                             "': status = "+threadReference.status()+
                             ", is suspended = "+threadReference.isSuspended()+
                             ", suspend count = "+threadReference.suspendCount()+
                             ", is at breakpoint = "+threadReference.isAtBreakpoint()+
                             ", internal suspend status = "+suspended;
                Logger.getLogger(JPDAThreadImpl.class.getName()).log(Level.INFO, msg, iex);
                return null;
            }
        }
        if (or == null) return null;
        return new ThisVariable (debugger, or, "" + or.uniqueID());
    }
    
    public MonitorInfo getContendedMonitorAndOwner() {
        ObjectVariable monitor = getContendedMonitor();
        if (monitor == null) return null;
        // Search for the owner:
        MonitorInfo monitorInfo = null;
        JPDAThread thread = null;
        List<JPDAThread> threads = debugger.getThreadsCollector().getAllThreads();
        for (JPDAThread t : threads) {
            if (this == t) continue;
            ObjectVariable[] ms = t.getOwnedMonitors();
            for (ObjectVariable m : ms) {
                if (monitor.equals(m)) {
                    thread = t;
                    List<MonitorInfo> mf = t.getOwnedMonitorsAndFrames();
                    for (MonitorInfo mi : mf) {
                        if (monitor.equals(mi.getMonitor())) {
                            monitorInfo = mi;
                            break;
                        }
                    }
                    break;
                }
            }
            if (thread != null) {
                break;
            }
        }
        if (monitorInfo != null) {
            return monitorInfo;
        }
        return new MonitorInfoImpl(thread, null, monitor);
    }
    
    /**
     * Returns monitors owned by this thread.
     *
     * @return monitors owned by this thread
     */
    public ObjectVariable[] getOwnedMonitors () {
        try {
            if (!threadReference.virtualMachine().canGetOwnedMonitorInfo()) {
                return new ObjectVariable[0];
            }
        } catch (IllegalThreadStateException ex) {
            // Thrown when thread has exited
            return new ObjectVariable [0];
        } catch (ObjectCollectedException ocex) {
            return new ObjectVariable [0];
        }
        List l;
        synchronized (this) {
            if (!isSuspended()) return new ObjectVariable [0];
            if ("DestroyJavaVM".equals(threadReference.name())) {
                // See defect #6474293
                return new ObjectVariable[0];
            }
            try {
                l = threadReference.ownedMonitors ();
            } catch (IllegalThreadStateException ex) {
                // Thrown when thread has exited
                return new ObjectVariable [0];
            } catch (ObjectCollectedException ex) {
                return new ObjectVariable [0];
            } catch (IncompatibleThreadStateException e) {
                String msg = "Thread '"+threadReference.name()+
                             "': status = "+threadReference.status()+
                             ", is suspended = "+threadReference.isSuspended()+
                             ", suspend count = "+threadReference.suspendCount()+
                             ", is at breakpoint = "+threadReference.isAtBreakpoint()+
                             ", internal suspend status = "+suspended;
                Logger.getLogger(JPDAThreadImpl.class.getName()).log(Level.INFO, msg, e);
                return new ObjectVariable [0];
            } catch (com.sun.jdi.InternalException iex) {
                String msg = "Thread '"+threadReference.name()+
                             "': status = "+threadReference.status()+
                             ", is suspended = "+threadReference.isSuspended()+
                             ", suspend count = "+threadReference.suspendCount()+
                             ", is at breakpoint = "+threadReference.isAtBreakpoint()+
                             ", internal suspend status = "+suspended;
                Logger.getLogger(JPDAThreadImpl.class.getName()).log(Level.INFO, msg, iex);
                return new ObjectVariable [0];
            }
        }
        int i, k = l.size ();
        ObjectVariable[] vs = new ObjectVariable [k];
        for (i = 0; i < k; i++) {
            ObjectReference var = (ObjectReference) l.get (i);
            vs [i] = new ThisVariable (debugger, var, ""+var.uniqueID());
        }
        return vs;
    }
    
    public ThreadReference getThreadReference () {
        return threadReference;
    }
    
    public synchronized ReturnVariableImpl getReturnVariable() {
        return returnVariable;
    }
    
    public synchronized void setReturnVariable(ReturnVariableImpl returnVariable) {
        this.returnVariable = returnVariable;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pch.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pch.removePropertyChangeListener(l);
    }
    
    private void fireSuspended(boolean suspended) {
        pch.firePropertyChange(PROP_SUSPENDED,
                Boolean.valueOf(!suspended), Boolean.valueOf(suspended));
    }

    public void setObject(Object bean) {
        throw new UnsupportedOperationException("Not supported, do not call. Implementing Customizer interface just because of add/remove PropertyChangeListener.");
    }

    public List<MonitorInfo> getOwnedMonitorsAndFrames() {
        if (CallStackFrameImpl.IS_JDK_16) {
            try {
                java.lang.reflect.Method canGetMonitorFrameInfoMethod =
                        threadReference.virtualMachine().getClass().getMethod("canGetMonitorFrameInfo"); // NOTICES
                canGetMonitorFrameInfoMethod.setAccessible(true);
                synchronized(this) {
                    if (!isSuspended()) {
                        return Collections.emptyList();
                    }
                    boolean canGetMonitorFrameInfo = (Boolean) canGetMonitorFrameInfoMethod.invoke(threadReference.virtualMachine());
                    //boolean canGetMonitorFrameInfo = threadReference.virtualMachine().canGetMonitorFrameInfo();
                    if (canGetMonitorFrameInfo) {
                        java.lang.reflect.Method ownedMonitorsAndFramesMethod = threadReference.getClass().getMethod("ownedMonitorsAndFrames"); // NOI18N
                        List monitorInfos = (List) ownedMonitorsAndFramesMethod.invoke(threadReference, new java.lang.Object[]{});
                        if (monitorInfos.size() > 0) {
                            List<MonitorInfo> mis = new ArrayList<MonitorInfo>(monitorInfos.size());
                            for (Object monitorInfo : monitorInfos) {
                                mis.add(createMonitorInfo(monitorInfo));
                            }
                            return Collections.unmodifiableList(mis);
                        }
                    }
                }
            } catch (IllegalAccessException ex) {
                org.openide.ErrorManager.getDefault().notify(ex);
            } catch (InvocationTargetException ex) {
                String msg = ""; // NOI18N
                try {
                    msg = "Thread '"+threadReference.name()+
                                 "': status = "+threadReference.status()+
                                 ", is suspended = "+threadReference.isSuspended()+
                                 ", suspend count = "+threadReference.suspendCount()+
                                 ", is at breakpoint = "+threadReference.isAtBreakpoint()+
                                 ", internal suspend status = "+suspended;
                } catch (VMDisconnectedException e) {
                }
                Logger.getLogger(JPDAThreadImpl.class.getName()).log(Level.INFO, msg, ex);
            } catch (java.lang.NoSuchMethodException ex) {
                org.openide.ErrorManager.getDefault().notify(ex);
            } catch (IllegalThreadStateException ex) {
                // Thrown when thread has exited
            }
        }
        return Collections.emptyList();
    }

    /**
     * 
     * @param mi com.sun.jdi.MonitorInfo
     * @return monitor info
     */
    private MonitorInfo createMonitorInfo(Object mi) {
        //com.sun.jdi.MonitorInfo _mi = (com.sun.jdi.MonitorInfo) mi;
        try {
            java.lang.reflect.Method stackDepthMethod =
                    mi.getClass().getMethod("stackDepth"); // NOTICES
            int depth = (Integer) stackDepthMethod.invoke(mi);
            //int depth = _mi.stackDepth();
            CallStackFrame frame = null;
            if (depth >= 0) {
                try {
                    CallStackFrame[] frames = getCallStack(depth, depth + 1);
                    //frame = new CallStackFrameImpl(this, threadReference.frame(depth), depth, debugger);
                    if (frames.length > 0) {
                        frame = frames[0];
                    }
                //} catch (IncompatibleThreadStateException ex) {
                } catch (AbsentInformationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            java.lang.reflect.Method monitorMethod =
                    mi.getClass().getMethod("monitor"); // NOTICES
            ObjectReference or = (ObjectReference) monitorMethod.invoke(mi);
            //ObjectReference or = _mi.monitor();
            ObjectVariable monitor = new ThisVariable (debugger, or, "" + or.uniqueID());
            return new MonitorInfoImpl(this, frame, monitor);
        } catch (IllegalAccessException ex) {
            org.openide.ErrorManager.getDefault().notify(ex);
        } catch (InvocationTargetException ex) {
            org.openide.ErrorManager.getDefault().notify(ex);
        } catch (java.lang.NoSuchMethodException ex) {
            org.openide.ErrorManager.getDefault().notify(ex);
        }
        return null;
    }

    public boolean checkForBlockingThreads() {
        try {
            if (!threadReference.virtualMachine().canGetCurrentContendedMonitor() ||
                !threadReference.virtualMachine().canGetMonitorInfo()) {
                return false;
            }
            //System.err.println("\""+getName()+"\".checkForBlockingThreads()");
            VirtualMachine vm = threadReference.virtualMachine();
            Map<ThreadReference, ObjectReference> lockedThreadsWithMonitors = null;
            //synchronized (t.getDebugger().LOCK) { - can not synchronize on that - method invocation uses this lock.
            // TODO: Need to be freed up and method invocation flag needs to be used instead.
                List<JPDAThread> oldLockerThreadsList;
                List<JPDAThread> newLockerThreadsList;
                vm.suspend();
                try {
                    ObjectReference waitingMonitor = threadReference.currentContendedMonitor();
                    if (waitingMonitor != null) {
                        synchronized (lockerThreadsLock) {
                            if (waitingMonitor.equals(lockerThreadsMonitor)) {
                                // We're still blocked at the monitor
                                return true;
                            }
                        }
                        lockedThreadsWithMonitors = findLockPath(threadReference, waitingMonitor);
                    }
                    synchronized (lockerThreadsLock) {
                        oldLockerThreadsList = lockerThreadsList;
                        if (lockedThreadsWithMonitors != null) {
                            //lockerThreads2 = lockedThreadsWithMonitors;
                            lockerThreadsMonitor = waitingMonitor;
                            if (!submitMonitorEnteredFor(waitingMonitor)) {
                                submitCheckForMonitorEntered(waitingMonitor);
                            }
                            lockerThreadsList = new ThreadListDelegate(debugger, new ArrayList(lockedThreadsWithMonitors.keySet()));
                        } else {
                            //lockerThreads2 = null;
                            lockerThreadsMonitor = null;
                            lockerThreadsList = null;
                        }
                        newLockerThreadsList = lockerThreadsList;
                    }
                    //System.err.println("Locker threads list = "+newLockerThreadsList);
                } catch (IncompatibleThreadStateException ex) {
                    return false;
                } finally {
                    vm.resume();
                }
                if (oldLockerThreadsList != newLockerThreadsList) { // Not fire when both null
                    //System.err.println("Fire lockerThreads: "+(oldLockerThreadsList == null || !oldLockerThreadsList.equals(newLockerThreadsList)));
                    pch.firePropertyChange(PROP_LOCKER_THREADS, oldLockerThreadsList, newLockerThreadsList); // NOI18N
                }
            //}
            //setLockerThreads(lockedThreadsWithMonitors);
            return lockedThreadsWithMonitors != null;
        } catch (VMDisconnectedException e) {
        } catch (InternalException e) {
        } catch (ObjectCollectedException e) {
        } catch (IllegalThreadStateException ex) {
            // Thrown when thread has exited
        }
        return false;
    }

    private static Map<ThreadReference, ObjectReference> findLockPath(ThreadReference tr, ObjectReference waitingMonitor) throws IncompatibleThreadStateException {
        Map<ThreadReference, ObjectReference> threadsWithMonitors = new LinkedHashMap<ThreadReference, ObjectReference>();
        Map<ObjectReference, ThreadReference> monitorMap = new HashMap<ObjectReference, ThreadReference>();
        for (ThreadReference t : tr.virtualMachine().allThreads()) {
            List<ObjectReference> monitors = t.ownedMonitors();
            for (ObjectReference m : monitors) {
                monitorMap.put(m, t);
            }
        }
        while (tr != null && waitingMonitor != null) {
            tr = monitorMap.get(waitingMonitor);
            if (tr != null) {
                if (tr.suspendCount() > 1) { // Add it if it was suspended before
                    threadsWithMonitors.put(tr, waitingMonitor);
                }
                waitingMonitor = tr.currentContendedMonitor();
            }
        }
        if (threadsWithMonitors.size() > 0) {
            return threadsWithMonitors;
        } else {
            return null;
        }
    }

    public synchronized List<JPDAThread> getLockerThreads() {
        return lockerThreadsList;
    }

    public JPDADebuggerImpl getDebugger() {
        return debugger;
    }

    /*public void resumeToFreeMonitor(Variable monitor) {
        synchronized (this) {
            if (!isSuspended()) {
                return ; // Already resumed
            }
        }
        threadReference.virtualMachine().eventRequestManager();
    }*/

    public boolean resumeBlockingThreads() {
        List<JPDAThread> blockingThreads;
        synchronized (lockerThreadsLock) {
            if (lockerThreadsList == null) {
                return false;
            }
            blockingThreads = new ArrayList(lockerThreadsList);
        }
        List<ThreadReference> resumedThreads = new ArrayList<ThreadReference>(blockingThreads.size());
        for (JPDAThread t : blockingThreads) {
            if (t.isSuspended()) {
                t.resume();
                resumedThreads.add(((JPDAThreadImpl) t).getThreadReference());
            }
        }
        synchronized (lockerThreadsLock) {
            this.resumedBlockingThreads = resumedThreads;
        }
        return true;
    }

    private void submitMonitorEnteredRequest(EventRequest monitorEnteredRequest) {
        monitorEnteredRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        monitorEnteredRequest.putProperty(Operator.SILENT_EVENT_PROPERTY, Boolean.TRUE);
        debugger.getOperator().register(monitorEnteredRequest, new Executor() {

            public boolean exec(Event event) {
                //MonitorContendedEnteredEvent monitorEnteredEvent = (MonitorContendedEnteredEvent) event;
                threadReference.virtualMachine().eventRequestManager().deleteEventRequest(event.request());
                debugger.getOperator().unregister(event.request());
                List<JPDAThread> oldLockerThreadsList;
                List<ThreadReference> threadsToSuspend;
                synchronized (lockerThreadsLock) {
                    oldLockerThreadsList = lockerThreadsList;
                    //lockerThreads2 = null;
                    lockerThreadsMonitor = null;
                    lockerThreadsList = null;
                    threadsToSuspend = resumedBlockingThreads;
                }
                pch.firePropertyChange(PROP_LOCKER_THREADS, oldLockerThreadsList, null);
                //System.err.println("Monitor freed, threadsToSuspend = "+threadsToSuspend);
                if (threadsToSuspend != null) {
                    for (ThreadReference tr : threadsToSuspend) {
                        tr.suspend(); // Increases the suspend count to 2 so that it's not resumed by EventSet.resume()
                        JPDAThreadImpl t = (JPDAThreadImpl) debugger.getExistingThread(tr);
                        if (t != null) {
                            t.notifySuspended();
                        }
                        //System.err.println("  Suspending "+t.getName()+" after monitor obtained.");
                    }
                }
                if (isMethodInvoking()) {
                    // HACK because of JDI:
                    // When invoking a method, EventSet.resume() will not resume the invocation thread
                    // We have to do it explicitely a suspend the thread right after the invocation, 'resumedToFinishMethodInvocation' flag is used for that.
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            synchronized (JPDAThreadImpl.this) {
                                resumedToFinishMethodInvocation = true;
                                threadReference.resume();
                                //System.err.println("  Resuming "+getName()+" because of method invocation.");
                            }
                        }
                    }, 200);
                }
                return true;
            }

            public void removed(EventRequest eventRequest) {
            }
        });
        monitorEnteredRequest.enable();
    }

    private boolean submitMonitorEnteredFor(ObjectReference waitingMonitor) {
        if (!Java6Methods.isJDK6() || !Java6Methods.canRequestMonitorEvents(threadReference.virtualMachine())) {
            return false;
        }
        //MonitorContendedEnteredRequest monitorEnteredRequest = threadReference.virtualMachine().eventRequestManager().createMonitorContendedEnteredRequest();
        EventRequest monitorEnteredRequest = Java6Methods.createMonitorContendedEnteredRequest(threadReference.virtualMachine().eventRequestManager());
        //monitorEnteredRequest.addThreadFilter(threadReference);
        Java6Methods.addThreadFilter2MonitorContendedEnteredRequest(monitorEnteredRequest, threadReference);
        submitMonitorEnteredRequest(monitorEnteredRequest);
        return true;
    }

    private void submitCheckForMonitorEntered(ObjectReference waitingMonitor) {
        try {
            threadReference.suspend();
            ObjectReference monitor = threadReference.currentContendedMonitor();
            if (monitor == null) return ;
            Location loc = threadReference.frame(0).location();
            loc = loc.method().locationOfCodeIndex(loc.codeIndex() + 1);
            if (loc == null) return;
            BreakpointRequest br = threadReference.virtualMachine().eventRequestManager().createBreakpointRequest(loc);
            br.addThreadFilter(threadReference);
            submitMonitorEnteredRequest(br);
        } catch (IncompatibleThreadStateException itex) {
            Exceptions.printStackTrace(itex);
        } finally {
            threadReference.resume();
        }
    }

    public void setStepSuspendedBy(JPDABreakpoint breakpoint) {
        synchronized (stepBreakpointLock) {
            this.stepSuspendedByBreakpoint = breakpoint;
        }
        pch.firePropertyChange(PROP_STEP_SUSPENDED_BY_BREAKPOINT, null, breakpoint);
    }

    @Override
    public String toString() {
        return "'"+getName()+"' ("+Integer.toHexString(System.identityHashCode(this))+") from DBG("+Integer.toHexString(debugger.hashCode())+")";
    }



    private static class ThreadListDelegate extends AbstractList<JPDAThread> {

        private List<ThreadReference> threads;
        private JPDADebuggerImpl debugger;

        public ThreadListDelegate(JPDADebuggerImpl debugger, List<ThreadReference> threads) {
            this.debugger = debugger;
            this.threads = threads;
        }

        @Override
        public JPDAThread get(int index) {
            return debugger.getThread(threads.get(index));
        }

        @Override
        public int size() {
            return threads.size();
        }

    }

}
