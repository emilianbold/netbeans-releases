/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.TypeComponent;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.InvalidRequestStateException;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.api.debugger.Properties;

import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.modules.debugger.jpda.actions.CompoundSmartSteppingListener;
import org.netbeans.modules.debugger.jpda.expr.EvaluationException;
import org.netbeans.modules.debugger.jpda.models.ObjectTranslation;
import org.netbeans.modules.debugger.jpda.util.JPDAUtils;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.AbstractDICookie;
import org.netbeans.api.debugger.jpda.AttachingDICookie;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.DebuggerStartException;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.debugger.jpda.JPDAStep;
import org.netbeans.api.debugger.jpda.ListeningDICookie;

import org.netbeans.modules.debugger.jpda.breakpoints.BreakpointsEngineListener;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.models.LocalsTreeModel;
import org.netbeans.modules.debugger.jpda.models.ThreadsTreeModel;
import org.netbeans.modules.debugger.jpda.models.CallStackFrameImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAClassTypeImpl;
import org.netbeans.modules.debugger.jpda.util.Operator;
import org.netbeans.modules.debugger.jpda.expr.Expression;
import org.netbeans.modules.debugger.jpda.expr.EvaluationContext;
import org.netbeans.modules.debugger.jpda.expr.ParseException;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.debugger.DelegatingSessionProvider;

import org.netbeans.spi.viewmodel.TreeModel;
import org.openide.ErrorManager;

/**
* Representation of a debugging session.
*
* @author   Jan Jancura
*/
public class JPDADebuggerImpl extends JPDADebugger {
    
    private static final Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda");
    
    private static final boolean SINGLE_THREAD_STEPPING = Boolean.getBoolean("netbeans.debugger.singleThreadStepping");


    // variables ...............................................................

    //private DebuggerEngine              debuggerEngine;
    private VirtualMachine              virtualMachine = null;
    private Exception                   exception;
    private int                         state = 0;
    private Operator                    operator;
    private PropertyChangeSupport       pcs;
    private JPDAThreadImpl              currentThread;
    private CallStackFrame              currentCallStackFrame;
    private int                         suspend = (SINGLE_THREAD_STEPPING) ? SUSPEND_EVENT_THREAD : SUSPEND_ALL;
    public final Object                 LOCK = new Object ();
    private final Object                LOCK2 = new Object ();
    private boolean                     starting;
    private AbstractDICookie            attachingCookie;
    private JavaEngineProvider          javaEngineProvider;
    private Set<String>                 languages;
    private String                      lastStratumn;
    private ContextProvider             lookupProvider;
    private ObjectTranslation           threadsTranslation;
    private ObjectTranslation           localsTranslation;
    private ExpressionPool              expressionPool;

    private StackFrame      altCSF = null;  //PATCH 48174

    private boolean                     doContinue = true; // Whether resume() will actually resume
    private Boolean                     singleThreadStepResumeDecision = null;
    private Boolean                     stepInterruptByBptResumeDecision = null;

    // init ....................................................................

    public JPDADebuggerImpl (ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        pcs = new PropertyChangeSupport (this);
        List l = lookupProvider.lookup (null, DebuggerEngineProvider.class);
        int i, k = l.size ();
        for (i = 0; i < k; i++)
            if (l.get (i) instanceof JavaEngineProvider)
                javaEngineProvider = (JavaEngineProvider) l.get (i);
        if (javaEngineProvider == null)
            throw new IllegalArgumentException
                ("JavaEngineProvider have to be used to start JPDADebugger!");
        languages = new HashSet<String>();
        languages.add ("Java");
        threadsTranslation = ObjectTranslation.createThreadTranslation(this);
        localsTranslation = ObjectTranslation.createLocalsTranslation(this);
        this.expressionPool = new ExpressionPool();
    }


    // JPDADebugger methods ....................................................

    /**
     * Returns current state of JPDA debugger.
     *
     * @return current state of JPDA debugger
     * @see #STATE_STARTING
     * @see #STATE_RUNNING
     * @see #STATE_STOPPED
     * @see #STATE_DISCONNECTED
     */
    public int getState () {
        return state;
    }

    /**
     * Gets value of suspend property.
     *
     * @return value of suspend property
     */
    public int getSuspend () {
        return suspend;
    }

    /**
     * Sets value of suspend property.
     *
     * @param s a new value of suspend property
     */
    public void setSuspend (int s) {
        if (s == suspend) return;
        int old = suspend;
        suspend = s;
        firePropertyChange (PROP_SUSPEND, new Integer (old), new Integer (s));
    }

    /**
     * Returns current thread or null.
     *
     * @return current thread or null
     */
    public JPDAThread getCurrentThread () {
        return currentThread;
    }

    /**
     * Returns current stack frame or null.
     *
     * @return current stack frame or null
     */
    public synchronized CallStackFrame getCurrentCallStackFrame () {
        if (currentCallStackFrame != null) {
            try {
                if (!currentCallStackFrame.getThread().isSuspended()) {
                    currentCallStackFrame = null;
                }
            } catch (InvalidStackFrameException isfex) {
                currentCallStackFrame = null;
            }
        }
        if (currentCallStackFrame == null && currentThread != null) {
            try {
                currentCallStackFrame = currentThread.getCallStack(0, 1)[0];
            } catch (Exception ex) {}
        }
        return currentCallStackFrame;
    }

    /**
     * Evaluates given expression in the current context.
     *
     * @param expression a expression to be evaluated
     *
     * @return current value of given expression
     */
    public Variable evaluate (String expression)
    throws InvalidExpressionException {
        Value v = evaluateIn (expression);
        return getLocalsTreeModel ().getVariable (v);
    }

    /**
     * Waits till the Virtual Machine is started and returns 
     * {@link DebuggerStartException} if any.
     *
     * @throws DebuggerStartException is some problems occurres during debugger 
     *         start
     *
     * @see AbstractDICookie#getVirtualMachine()
     */
    public void waitRunning () throws DebuggerStartException {
        synchronized (LOCK2) {
            if (getState () == STATE_DISCONNECTED) {
                if (exception != null) 
                    throw new DebuggerStartException (exception);
                else 
                    return;
            }
            if (!starting && state != STATE_STARTING || exception != null) {
                return ; // We're already running
            }
            try {
                LOCK2.wait ();
            } catch (InterruptedException e) {
                 throw new DebuggerStartException (e);
            }
            
            if (exception != null) 
                throw new DebuggerStartException (exception);
            else 
                return;
        }
    }

    /**
     * Returns <code>true</code> if this debugger supports Pop action.
     *
     * @return <code>true</code> if this debugger supports Pop action
     */
    public boolean canPopFrames () {
        VirtualMachine vm = getVirtualMachine ();
        if (vm == null) return false;
        return vm.canPopFrames ();
    }

    /**
     * Returns <code>true</code> if this debugger supports fix & continue 
     * (HotSwap).
     *
     * @return <code>true</code> if this debugger supports fix & continue
     */
    public boolean canFixClasses () {
        VirtualMachine vm = getVirtualMachine ();
        if (vm == null) return false;
        return vm.canRedefineClasses ();
    }

    /**
     * Implements fix & continue (HotSwap). Map should contain class names
     * as a keys, and byte[] arrays as a values.
     *
     * @param classes a map from class names to be fixed to byte[] 
     */
    public void fixClasses (Map<String, byte[]> classes) {
        synchronized (LOCK) {
            
            // 1) redefine classes
            Map<ReferenceType, byte[]> map = new HashMap<ReferenceType, byte[]>();
            Iterator<String> i = classes.keySet ().iterator ();
            VirtualMachine vm = getVirtualMachine();
            if (vm == null) {
                return ; // The session has finished
            }
            while (i.hasNext ()) {
                String className = i.next ();
                List<ReferenceType> classRefs = vm.classesByName (className);
                int j, jj = classRefs.size ();
                for (j = 0; j < jj; j++)
                    map.put (
                        classRefs.get (j), 
                        classes.get (className)
                    );
            }
            vm.redefineClasses (map);

            // update breakpoints
            Session s = getSession();
            DebuggerEngine de = s.getEngineForLanguage ("Java");
            BreakpointsEngineListener bel = null;
            List lazyListeners = de.lookup(null, LazyActionsManagerListener.class);
            for (int li = 0; li < lazyListeners.size(); li++) {
                Object service = lazyListeners.get(li);
                if (service instanceof BreakpointsEngineListener) {
                    bel = (BreakpointsEngineListener) service;
                    break;
                }
            }
            bel.fixBreakpointImpls ();
            
            // 2) pop obsoleted frames
            JPDAThread t = getCurrentThread ();
            if (t != null && t.isSuspended()) {
                CallStackFrame frame = getCurrentCallStackFrame ();

                //PATCH #52209
                if (t.getStackDepth () < 2 && frame.isObsolete()) return;
                try {
                    if (!frame.equals (t.getCallStack (0, 1) [0])) return;
                } catch (AbsentInformationException ex) {
                    return;
                }

                //PATCH #52209
                if (frame.isObsolete () && ((CallStackFrameImpl) frame).canPop()) {
                    frame.popFrame ();
                    setState (STATE_RUNNING);
                    updateCurrentCallStackFrame (t);
                    setState (STATE_STOPPED);
                }
            }
            
        }
    }
    
    public Session getSession() {
        return (Session) lookupProvider.lookupFirst (null, Session.class);
    }
    
    private Boolean canBeModified;
    private Object canBeModifiedLock = new Object();
    
    public boolean canBeModified() {
        VirtualMachine vm = getVirtualMachine ();
        if (vm == null) return false;
        synchronized (canBeModifiedLock) {
            if (canBeModified == null) {
                try {
                    java.lang.reflect.Method canBeModifiedMethod =
                            com.sun.jdi.VirtualMachine.class.getMethod("canBeModified", new Class[] {});
                    Object modifiable = canBeModifiedMethod.invoke(vm, new Object[] {});
                    canBeModified = (Boolean) modifiable;
                } catch (NoSuchMethodException nsmex) {
                    // On JDK 1.4 we do not know... we suppose that can
                    canBeModified = Boolean.TRUE;
                } catch (IllegalAccessException iaex) {
                    canBeModified = Boolean.TRUE;
                } catch (InvocationTargetException itex) {
                    canBeModified = Boolean.TRUE;
                }
            }
            return canBeModified.booleanValue();
        }
        // return vm.canBeModified(); -- After we'll build on JDK 1.5
    }

    private SmartSteppingFilter smartSteppingFilter;

    /** 
     * Returns instance of SmartSteppingFilter.
     *
     * @return instance of SmartSteppingFilter
     */
    public SmartSteppingFilter getSmartSteppingFilter () {
        if (smartSteppingFilter == null) {
            smartSteppingFilter = (SmartSteppingFilter) lookupProvider.
                lookupFirst (null, SmartSteppingFilter.class);
            smartSteppingFilter.addExclusionPatterns (
                (Set) Properties.getDefault ().getProperties ("debugger").
                    getProperties ("sources").getProperties ("class_filters").
                    getCollection (
                        "enabled", 
                        Collections.EMPTY_SET
                    )
            );
        }
        return smartSteppingFilter;
    }

    CompoundSmartSteppingListener compoundSmartSteppingListener;
    
    private CompoundSmartSteppingListener getCompoundSmartSteppingListener () {
        if (compoundSmartSteppingListener == null)
            compoundSmartSteppingListener = (CompoundSmartSteppingListener) lookupProvider.
                lookupFirst (null, CompoundSmartSteppingListener.class);
        return compoundSmartSteppingListener;
    }
    
    /**
     * Test whether we should stop here according to the smart-stepping rules.
     */
    boolean stopHere(JPDAThread t) {
        return getCompoundSmartSteppingListener ().stopHere 
                     (lookupProvider, t, getSmartSteppingFilter());
    }
    
    /**
     * Helper method that fires JPDABreakpointEvent on JPDABreakpoints.
     *
     * @param breakpoint a breakpoint to be changed
     * @param event a event to be fired
     */
    public void fireBreakpointEvent (
        JPDABreakpoint breakpoint, 
        JPDABreakpointEvent event
    ) {
        super.fireBreakpointEvent (breakpoint, event);
    }

    /**
    * Adds property change listener.
    *
    * @param l new listener.
    */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }

    /**
    * Removes property change listener.
    *
    * @param l removed listener.
    */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }

    /**
    * Adds property change listener.
    *
    * @param l new listener.
    */
    public void addPropertyChangeListener (String propertyName, PropertyChangeListener l) {
        pcs.addPropertyChangeListener (propertyName, l);
    }

    /**
    * Removes property change listener.
    *
    * @param l removed listener.
    */
    public void removePropertyChangeListener (String propertyName, PropertyChangeListener l) {
        pcs.removePropertyChangeListener (propertyName, l);
    }

    
    // internal interface ......................................................

    public void popFrames (ThreadReference thread, StackFrame frame) {
        synchronized (LOCK) {
            JPDAThreadImpl threadImpl = (JPDAThreadImpl) getThread(thread);
            setState (STATE_RUNNING);
            try {
                threadImpl.popFrames(frame);
                updateCurrentCallStackFrame (threadImpl);
            } catch (IncompatibleThreadStateException ex) {
                ErrorManager.getDefault().notify(ex);
            } finally {
                setState (STATE_STOPPED);
            }
        }
    }

    public void setException (Exception e) {
        synchronized (LOCK2) {
            exception = e;
            starting = false;
            LOCK2.notifyAll ();
        }
    }

    public void setCurrentThread (JPDAThread thread) {
        Object oldT = currentThread;
        currentThread = (JPDAThreadImpl) thread;
        if (thread != oldT)
            firePropertyChange (PROP_CURRENT_THREAD, oldT, currentThread);
        updateCurrentCallStackFrame (thread);
    }

    /**
     * Set the current thread and call stack, but do not fire changes.
     * @return The PropertyChangeEvent associated with this change, it can have
     *         attached other PropertyChangeEvents as a propagation ID.
     */
    private PropertyChangeEvent setCurrentThreadNoFire(JPDAThread thread) {
        Object oldT = currentThread;
        currentThread = (JPDAThreadImpl) thread;
        PropertyChangeEvent evt = null;
        if (thread != oldT)
            evt = new PropertyChangeEvent(this, PROP_CURRENT_THREAD, oldT, currentThread);
        PropertyChangeEvent evt2 = updateCurrentCallStackFrameNoFire(thread);
        if (evt == null) evt = evt2;
        else if (evt2 != null) evt.setPropagationId(evt2);
        return evt;
    }

    public void setCurrentCallStackFrame (CallStackFrame callStackFrame) {
        CallStackFrame old = setCurrentCallStackFrameNoFire(callStackFrame);
        if (old == callStackFrame) return ;
        firePropertyChange (
            PROP_CURRENT_CALL_STACK_FRAME,
            old,
            callStackFrame
        );
    }
    
    private CallStackFrame setCurrentCallStackFrameNoFire (CallStackFrame callStackFrame) {
        CallStackFrame old;
        synchronized (this) {
            if (callStackFrame == currentCallStackFrame) return callStackFrame;
            old = currentCallStackFrame;
            currentCallStackFrame = callStackFrame;
        }
        return old;
    }

    /**
     * Used by AbstractVariable.
     */
    public Value evaluateIn (String expression) throws InvalidExpressionException {
        Expression expr = null;
        try {
            expr = Expression.parse (expression, Expression.LANGUAGE_JAVA_1_5);
            return evaluateIn (expr);
        } catch (ParseException e) {
            InvalidExpressionException iee = new InvalidExpressionException(e.getMessage());
            iee.initCause(e);
            throw iee;
        }
    }
    
    //PATCH 48174
    public void setAltCSF(StackFrame sf) {
        altCSF = sf;
    }
    
    public StackFrame getAltCSF() {
        return altCSF;
    }
    
    /**
     * Used by WatchesModel & BreakpointImpl.
     */
    public Value evaluateIn (Expression expression) 
    throws InvalidExpressionException {
        synchronized (LOCK) {
            
            CallStackFrameImpl csf = (CallStackFrameImpl) 
                getCurrentCallStackFrame ();
            if (csf != null) {
                JPDAThread frameThread = csf.getThread();
                try {
                    Value value = evaluateIn (expression, csf.getStackFrame ());
                    try {
                        csf.getThread();
                    } catch (InvalidStackFrameException isfex) {
                        // The frame is invalidated, set the new current...
                        int depth = csf.getFrameDepth();
                        try {
                            CallStackFrame csf2 = frameThread.getCallStack(depth, depth + 1)[0];
                            setCurrentCallStackFrameNoFire(csf2);
                        } catch (AbsentInformationException aiex) {
                            setCurrentCallStackFrame(null);
                        }
                    }
                    return value;
                } catch (com.sun.jdi.VMDisconnectedException e) {
                    // Causes kill action when something is being evaluated. 
                    return null;
                }
            }
            //PATCH 48174
            if (altCSF != null) {
                try {
                    if (!altCSF.thread().isSuspended()) {
                        altCSF = null; // Already invalid
                    } else {
                        // TODO XXX : Can be resumed in the mean time !!!!
                        return evaluateIn (expression, altCSF);
                    }
                } catch (InvalidStackFrameException isfex) {
                    // Will be thrown when the altCSF is invalid
                    altCSF = null; // throw it
                } catch (com.sun.jdi.VMDisconnectedException e) {
                    // Causes kill action when something is being evaluated. 
                    return null;
                }
            }
            throw new InvalidExpressionException
                    ("No current context (stack frame)");
            
        }
    }

    private InvalidExpressionException methodCallsUnsupportedExc;

    /**
     * Used by BreakpointImpl.
     */
    public  Value evaluateIn (Expression expression, final StackFrame frame) 
    throws InvalidExpressionException {
        synchronized (LOCK) {
            if (frame == null)
                throw new InvalidExpressionException ("No current context");

            // TODO: get imports from the source file
            List<String> imports = new ArrayList<String>();
            List<String> staticImports = new ArrayList<String>();
            imports.add ("java.lang.*");
            try {
                imports.addAll (Arrays.asList (EditorContextBridge.getContext().getImports (
                    getEngineContext ().getURL (frame, "Java")
                )));
                final ThreadReference tr = frame.thread();
                final List<EventRequest>[] disabledBreakpoints =
                        new List[] { null };
                final JPDAThreadImpl[] resumedThread = new JPDAThreadImpl[] { null };
                EvaluationContext context;
                org.netbeans.modules.debugger.jpda.expr.Evaluator evaluator = 
                    expression.evaluator (
                        context = new EvaluationContext (
                            frame,
                            imports, 
                            staticImports,
                            methodCallsUnsupportedExc == null,
                            new Runnable() {
                                public void run() {
                                    if (disabledBreakpoints[0] == null) {
                                        try {
                                            resumedThread[0].notifyMethodInvoking();
                                        } catch (PropertyVetoException pvex) {
                                            throw new RuntimeException(
                                                new InvalidExpressionException (pvex.getMessage()));
                                        }
                                        disabledBreakpoints[0] = disableAllBreakpoints ();
                                        resumedThread[0] = (JPDAThreadImpl) getThread(tr);
                                    }
                                }
                            },
                            this
                        )
                    );
                try {
                    return evaluator.evaluate ();
                } finally {
                    if (methodCallsUnsupportedExc == null && !context.canInvokeMethods()) {
                        methodCallsUnsupportedExc =
                                new InvalidExpressionException(new UnsupportedOperationException());
                    }
                    if (disabledBreakpoints[0] != null) {
                        enableAllBreakpoints (disabledBreakpoints[0]);
                    }
                    if (resumedThread[0] != null) {
                        resumedThread[0].notifyMethodInvokeDone();
                    }
                }
            } catch (EvaluationException e) {
                InvalidExpressionException iee = new InvalidExpressionException (e);
                iee.initCause (e);
                throw iee;
            } catch (IncompatibleThreadStateException itsex) {
                ErrorManager.getDefault().notify(itsex);
                IllegalStateException isex = new IllegalStateException(itsex.getLocalizedMessage());
                isex.initCause(itsex);
                throw isex;
            } catch (RuntimeException rex) {
                Throwable cause = rex.getCause();
                if (cause instanceof InvalidExpressionException) {
                    throw (InvalidExpressionException) cause;
                } else {
                    throw rex;
                }
            }
        }
    }
    
    /**
     * Used by AbstractVariable.
     */
    public Value invokeMethod (
        ObjectReference reference,
        Method method,
        Value[] arguments
    ) throws InvalidExpressionException {
        if (currentThread == null)
            throw new InvalidExpressionException ("No current context");
        synchronized (LOCK) {
            if (methodCallsUnsupportedExc != null) {
                throw methodCallsUnsupportedExc;
            }
            boolean threadSuspended = false;
            JPDAThread frameThread = null;
            CallStackFrameImpl csf = null;
            JPDAThreadImpl thread = null;
            List<EventRequest> l = null;
            try {
                // Remember the current stack frame, it might be necessary to re-set.
                csf = (CallStackFrameImpl) getCurrentCallStackFrame ();
                if (csf != null) {
                    try {
                        frameThread = csf.getThread();
                    } catch (InvalidStackFrameException isfex) {}
                }
                ThreadReference tr = getEvaluationThread();
                thread = (JPDAThreadImpl) getThread(tr);
                synchronized (thread) {
                    threadSuspended = thread.isSuspended();
                    if (!threadSuspended) {
                        throw new InvalidExpressionException ("No current context");
                    }
                    try {
                        thread.notifyMethodInvoking();
                    } catch (PropertyVetoException pvex) {
                        throw new InvalidExpressionException (pvex.getMessage());
                    }
                }
                l = disableAllBreakpoints ();
                return org.netbeans.modules.debugger.jpda.expr.Evaluator.
                    invokeVirtual (
                        reference,
                        method,
                        tr,
                        Arrays.asList (arguments)
                    );
            } catch (InvalidExpressionException ieex) {
                if (ieex.getTargetException() instanceof UnsupportedOperationException) {
                    methodCallsUnsupportedExc = ieex;
                }
                throw ieex;
            } finally {
                if (l != null) {
                    enableAllBreakpoints (l);
                }
                if (threadSuspended) {
                    thread.notifyMethodInvokeDone();
                }
                if (frameThread != null) {
                    try {
                        csf.getThread();
                    } catch (InvalidStackFrameException isfex) {
                        // The current frame is invalidated, set the new current...
                        int depth = csf.getFrameDepth();
                        try {
                            CallStackFrame csf2 = frameThread.getCallStack(depth, depth + 1)[0];
                            setCurrentCallStackFrameNoFire(csf2);
                        } catch (AbsentInformationException aiex) {
                            setCurrentCallStackFrame(null);
                        }
                    }
                }
            }
        }
    }

    public static String getGenericSignature (TypeComponent component) {
        if (tcGenericSignatureMethod == null) return null;
        try {
            return (String) tcGenericSignatureMethod.invoke 
                (component, new Object[0]);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;    // should not happen
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;    // should not happen
        }
    }

    public static String getGenericSignature (LocalVariable component) {
        if (lvGenericSignatureMethod == null) return null;
        try {
            return (String) lvGenericSignatureMethod.invoke(component, new Object[0]);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;    // should not happen
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;    // should not happen
        }
    }

    public VirtualMachine getVirtualMachine () {
        return virtualMachine;
    }

    public Operator getOperator () {
        return operator;
    }

    public void setStarting () {
        setState (STATE_STARTING);
    }
    
    public synchronized void setAttaching(AbstractDICookie cookie) {
        this.attachingCookie = cookie;
    }
    
    public void setRunning (VirtualMachine vm, Operator o) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Start - JPDADebuggerImpl.setRunning ()");
            JPDAUtils.printFeatures (logger, vm);
        }
        synchronized (LOCK2) {
            starting = true;
        }
        synchronized (this) {
            virtualMachine = vm;
        }
        synchronized (canBeModifiedLock) {
            canBeModified = null; // Reset the can be modified flag
        }
        
        initGenericsSupport ();
        
        operator = o;
        
//        Iterator i = getVirtualMachine ().allThreads ().iterator ();
//        while (i.hasNext ()) {
//            ThreadReference tr = (ThreadReference) i.next ();
//            if (tr.isSuspended ()) {
//                if (startVerbose)
//                    System.out.println("\nS JPDADebuggerImpl.setRunning () - " +
//                        "thread supended"
//                    );
//                setState (STATE_RUNNING);
//                synchronized (LOCK) {
//                    virtualMachine.resume ();
//                }
//                if (startVerbose)
//                    System.out.println("\nS JPDADebuggerImpl.setRunning () - " +
//                        "thread supended - VM resumed - end"
//                    );
//                synchronized (LOCK2) {
//                    LOCK2.notify ();
//                }
//                return;
//            }
//        }
        
        setState (STATE_RUNNING);
        synchronized (this) {
            vm = virtualMachine; // re-take the VM, it can be nulled by finish()
        }
        if (vm != null) {
            synchronized (LOCK) {
                vm.resume();
            }
        }
        
        logger.fine("   JPDADebuggerImpl.setRunning () finished, VM resumed.");
        synchronized (LOCK2) {
            starting = false;
            LOCK2.notifyAll ();
        }
    }

    /**
    * Performs stop action.
    */
    public void setStoppedState (ThreadReference thread) {
        PropertyChangeEvent evt;
        synchronized (LOCK) {
            // this method can be called in stopped state to switch 
            // the current thread only
            JPDAThread t = getThread (thread);
            checkJSR45Languages (t);
            evt = setCurrentThreadNoFire(t);
            PropertyChangeEvent evt2 = setStateNoFire(STATE_STOPPED);
            
            if (evt == null) evt = evt2;
            else if (evt2 != null) {
                PropertyChangeEvent evt3 = evt;
                while(evt3.getPropagationId() != null) evt3 = (PropertyChangeEvent) evt3.getPropagationId();
                evt3.setPropagationId(evt2);
            }
        }
        if (evt != null) {
            do {
                firePropertyChange(evt);
                evt = (PropertyChangeEvent) evt.getPropagationId();
            } while (evt != null);
        }
    }
    
    /**
     * Can be called if the current thread is resumed after stop.
     */
    public void setRunningState() {
        setState(STATE_RUNNING);
    }
    
    /**
    * Performs stop action and disable a next call to resume()
    */
    public void setStoppedStateNoContinue (ThreadReference thread) {
        PropertyChangeEvent evt;
        synchronized (LOCK) {
            // this method can be called in stopped state to switch 
            // the current thread only
            evt = setStateNoFire(STATE_RUNNING);
            JPDAThread t = getThread (thread);
            checkJSR45Languages (t);
            PropertyChangeEvent evt2 = setCurrentThreadNoFire(t);
            
            if (evt == null) evt = evt2;
            else if (evt2 != null) evt.setPropagationId(evt2);
            
            evt2 = setStateNoFire(STATE_STOPPED);
            
            if (evt == null) evt = evt2;
            else if (evt2 != null) {
                PropertyChangeEvent evt3 = evt;
                while(evt3.getPropagationId() != null) evt3 = (PropertyChangeEvent) evt3.getPropagationId();
                evt3.setPropagationId(evt2);
            }
            
            doContinue = false;
        }
        if (evt != null) {
            do {
                firePropertyChange(evt);
                evt = (PropertyChangeEvent) evt.getPropagationId();
            } while (evt != null);
        }
    }
    
    
    private boolean finishing;

    /**
     * Used by KillActionProvider.
     */
    public void finish () {
        //Workaround for #56233
        //synchronized (LOCK) { 
            synchronized (this) {
                if (finishing) {
                    // Can easily be called twice - from the operator termination
                    return ;
                }
                finishing = true;
            }
            logger.fine("StartActionProvider.finish ()");
            AbstractDICookie di = (AbstractDICookie) lookupProvider.lookupFirst 
                (null, AbstractDICookie.class);
            if (getState () == STATE_DISCONNECTED) return;
            Operator o = getOperator();
            if (o != null) o.stop();
            synchronized (this) {
                if (attachingCookie != null) {
                    if (attachingCookie instanceof ListeningDICookie) {
                        ListeningDICookie listeningCookie = (ListeningDICookie) attachingCookie;
                        try {
                            listeningCookie.getListeningConnector().stopListening(listeningCookie.getArgs());
                        } catch (java.io.IOException ioex) {
                        } catch (com.sun.jdi.connect.IllegalConnectorArgumentsException icaex) {
                        } catch (IllegalArgumentException iaex) {
                        }
                    }
                }
            }
            try {
                waitRunning(); // First wait till the debugger comes up
            } catch (DebuggerStartException dsex) {
                // We do not want to start it anyway when we're finishing - do not bother
            }
            VirtualMachine vm;
            synchronized (this) {
                vm = virtualMachine;
            }
            if (vm != null) {
                try {
                    if (di instanceof AttachingDICookie) {
                        logger.fine(" StartActionProvider.finish() VM dispose");
                        vm.dispose ();
                    } else {
                        logger.fine(" StartActionProvider.finish() VM exit");
                        vm.exit (0);
                    }
                } catch (VMDisconnectedException e) {
                    logger.fine(" StartActionProvider.finish() VM exception " + e);
                    // debugee VM is already disconnected (it finished normally)
                }
            }
            synchronized (this) {
                virtualMachine = null;
            }
            setState (STATE_DISCONNECTED);
            if (jsr45EngineProviders != null) {
                for (Iterator<JSR45DebuggerEngineProvider> i = jsr45EngineProviders.iterator(); i.hasNext();) {
                    JSR45DebuggerEngineProvider provider = i.next();
                    provider.getDesctuctor().killEngine();
                }
                jsr45EngineProviders = null;
            }
            javaEngineProvider.getDestructor ().killEngine ();
            logger.fine (" StartActionProvider.finish() end.");
            
            //Notify LOCK2 so that no one is waiting forever
            synchronized (LOCK2) {
                starting = false;
                LOCK2.notifyAll ();
            }
        //}
    }

    /**
     * Suspends the target virtual machine (if any).
     * Used by PauseActionProvider.
     *
     * @see  com.sun.jdi.ThreadReference#suspend
     */
    public void suspend () {
        VirtualMachine vm;
        synchronized (this) {
            vm = virtualMachine;
        }
        synchronized (LOCK) {
            if (getState () == STATE_STOPPED)
                return;
            if (vm != null) {
                logger.fine("VM suspend");
                vm.suspend ();
                // Check the suspended count
                List<ThreadReference> threads = vm.allThreads();
                for (ThreadReference t : threads) {
                    while (t.suspendCount() > 1) t.resume();
                }
            }
            setState (STATE_STOPPED);
        }
        notifySuspendAll();
    }
    
    public void notifySuspendAll() {
        Collection threads = threadsTranslation.getTranslated();
        for (Iterator it = threads.iterator(); it.hasNext(); ) {
            Object threadOrGroup = it.next();
            if (threadOrGroup instanceof JPDAThreadImpl) {
                int status = ((JPDAThreadImpl) threadOrGroup).getState();
                boolean invalid = (status == JPDAThread.STATE_NOT_STARTED ||
                                   status == JPDAThread.STATE_UNKNOWN ||
                                   status == JPDAThread.STATE_ZOMBIE);
                if (!invalid) {
                    try {
                        ((JPDAThreadImpl) threadOrGroup).notifySuspended();
                    } catch (ObjectCollectedException ocex) {
                        invalid = true;
                    }
                }
                if (invalid) {
                    threadsTranslation.remove(((JPDAThreadImpl) threadOrGroup).getThreadReference());
                }
            }
        }
    }
    
    /**
     * Used by ContinueActionProvider & StepActionProvider.
     */
    public void resume () {
        synchronized (LOCK) {
            if (!doContinue) {
                doContinue = true;
                // Continue the next time and do nothing now.
                return ;
            }
        }
        if (operator.flushStaledEvents()) {
            return ;
        }
        setState (STATE_RUNNING);
        notifyToBeResumedAll();
        VirtualMachine vm;
        synchronized (this) {
            vm = virtualMachine;
        }
        synchronized (LOCK) {
            if (vm != null) {
                logger.fine("VM resume");
                vm.resume ();
            }
        }
    }
    
    /** DO NOT CALL FROM ANYWHERE BUT JPDAThreadImpl.resume(). */
    public boolean currentThreadToBeResumed() {
        synchronized (LOCK) {
            if (!doContinue) {
                doContinue = true;
                // Continue the next time and do nothing now.
                return false;
            }
        }
        if (operator.flushStaledEvents()) {
            return false;
        }
        setState (STATE_RUNNING);
        return true;
    }
    
    public void resumeCurrentThread() {
        synchronized (LOCK) {
            if (!doContinue) {
                doContinue = true;
                // Continue the next time and do nothing now.
                return ;
            }
        }
        if (operator.flushStaledEvents()) {
            return ;
        }
        setState (STATE_RUNNING);
        currentThread.resume();
    }
    
    public void notifyToBeResumedAll() {
        Collection threads = threadsTranslation.getTranslated();
        for (Iterator it = threads.iterator(); it.hasNext(); ) {
            Object threadOrGroup = it.next();
            if (threadOrGroup instanceof JPDAThreadImpl) {
                int status = ((JPDAThreadImpl) threadOrGroup).getState();
                boolean invalid = (status == JPDAThread.STATE_NOT_STARTED ||
                                   status == JPDAThread.STATE_UNKNOWN ||
                                   status == JPDAThread.STATE_ZOMBIE);
                if (!invalid) {
                    ((JPDAThreadImpl) threadOrGroup).notifyToBeResumed();
                } else {
                    threadsTranslation.remove(((JPDAThreadImpl) threadOrGroup).getThreadReference());
                }
            }
        }
    }
    
    public JPDAThreadGroup[] getTopLevelThreadGroups() {
        VirtualMachine vm;
        synchronized (this) {
            vm = virtualMachine;
        }
        if (vm == null) {
            return new JPDAThreadGroup[0];
        }
        List groupList;
        synchronized (LOCK) {
            groupList = vm.topLevelThreadGroups();
        }
        JPDAThreadGroup[] groups = new JPDAThreadGroup[groupList.size()];
        for (int i = 0; i < groups.length; i++) {
            groups[i] = getThreadGroup((ThreadGroupReference) groupList.get(i));
        }
        return groups;
    }

    public JPDAThread getThread (ThreadReference tr) {
        return (JPDAThread) threadsTranslation.translate (tr);
    }

    public JPDAThreadGroup getThreadGroup (ThreadGroupReference tgr) {
        return (JPDAThreadGroup) threadsTranslation.translate (tgr);
    }
    
    public Variable getLocalVariable(LocalVariable lv, Value v) {
        return (Variable) localsTranslation.translate(lv, v);
    }
    
    public JPDAClassType getClassType(ReferenceType cr) {
        return (JPDAClassType) localsTranslation.translate (cr);
    }

    public Variable getVariable (Value value) {
        return getLocalsTreeModel ().getVariable (value);
    }
    
    public ExpressionPool getExpressionPool() {
        return expressionPool;
    }
    
    synchronized void setSingleThreadStepResumeDecision(Boolean decision) {
        singleThreadStepResumeDecision = decision;
    }
    
    synchronized Boolean getSingleThreadStepResumeDecision() {
        return singleThreadStepResumeDecision;
    }
    
    public synchronized void setStepInterruptByBptResumeDecision(Boolean decision) {
        stepInterruptByBptResumeDecision = decision;
    }
    
    public synchronized Boolean getStepInterruptByBptResumeDecision() {
        return stepInterruptByBptResumeDecision;
    }


    // private helper methods ..................................................
    
    private static final java.util.regex.Pattern jvmVersionPattern =
            java.util.regex.Pattern.compile ("(\\d+)\\.(\\d+)\\.(\\d+)(_\\d+)?(-\\w+)?");
    private static java.lang.reflect.Method  tcGenericSignatureMethod;
    private static java.lang.reflect.Method  lvGenericSignatureMethod;


    private void initGenericsSupport () {
        tcGenericSignatureMethod = null;
        if (Bootstrap.virtualMachineManager ().minorInterfaceVersion () >= 5) {
            VirtualMachine vm;
            synchronized (this) {
                vm = virtualMachine;
            }
            if (vm == null) return ;
            java.util.regex.Matcher m = jvmVersionPattern.matcher(vm.version ());
            if (m.matches ()) {
                int minor = Integer.parseInt (m.group (2));
                if (minor >= 5) {
                    try {
                        tcGenericSignatureMethod = TypeComponent.class.
                            getMethod ("genericSignature", new Class [0]);
                        lvGenericSignatureMethod = LocalVariable.class.
                            getMethod ("genericSignature", new Class [0]);
                    } catch (NoSuchMethodException e) {
                        // the method is not available, ignore generics
                    }
                }
            }
        }
    }
    
    private PropertyChangeEvent setStateNoFire (int state) {
        if (state == this.state) return null;
        int o = this.state;
        this.state = state;
        //PENDING HACK see issue 46287
        System.setProperty(
            "org.openide.awt.SwingBrowserImpl.do-not-block-awt",
            String.valueOf (state != STATE_DISCONNECTED)
        );
        return new PropertyChangeEvent(this, PROP_STATE, new Integer (o), new Integer (state));
    }

    private void setState (int state) {
        PropertyChangeEvent evt = setStateNoFire(state);
        if (evt != null) {
            firePropertyChange(evt);
        }
    }
    
    /**
     * Fires property change.
     */
    private void firePropertyChange (String name, Object o, Object n) {
        pcs.firePropertyChange (name, o, n);
        //System.err.println("ALL Change listeners count = "+pcs.getPropertyChangeListeners().length);
    }

    /**
     * Fires property change.
     */
    private void firePropertyChange (PropertyChangeEvent evt) {
        pcs.firePropertyChange (evt);
        //System.err.println("ALL Change listeners count = "+pcs.getPropertyChangeListeners().length);
    }

    private SourcePath engineContext;
    public synchronized SourcePath getEngineContext () {
        if (engineContext == null)
            engineContext = (SourcePath) lookupProvider.
                lookupFirst (null, SourcePath.class);
        return engineContext;
    }

    private LocalsTreeModel localsTreeModel;
    private LocalsTreeModel getLocalsTreeModel () {
        if (localsTreeModel == null)
            localsTreeModel = (LocalsTreeModel) lookupProvider.
                lookupFirst ("LocalsView", TreeModel.class);
        return localsTreeModel;
    }

    private ThreadReference getEvaluationThread () {
        if (currentThread != null) return currentThread.getThreadReference ();
        VirtualMachine vm;
        synchronized (this) {
            vm = virtualMachine;
        }
        if (vm == null) return null;
        List l = vm.allThreads ();
        if (l.size () < 1) return null;
        int i, k = l.size ();
        ThreadReference thread = null;
        for (i = 0; i < k; i++) {
            ThreadReference t = (ThreadReference) l.get (i);
            if (t.isSuspended ()) {
                thread = t;
                if (t.name ().equals ("Finalizer"))
                    return t;
            }
        }
        return thread;
    }

    private void updateCurrentCallStackFrame (JPDAThread thread) {
        if ( (thread == null) ||
             (thread.getStackDepth () < 1))
            setCurrentCallStackFrame (null);
        else
        try {
            setCurrentCallStackFrame (thread.getCallStack (0, 1) [0]);
        } catch (AbsentInformationException e) {
            setCurrentCallStackFrame (null);
        }
    }

    /**
     * @param thread The thread to take the top frame from
     * @return A PropertyChangeEvent or <code>null</code>.
     */
    private PropertyChangeEvent updateCurrentCallStackFrameNoFire(JPDAThread thread) {
        CallStackFrame old;
        CallStackFrame callStackFrame;
        if ( (thread == null) ||
             (thread.getStackDepth () < 1))
            old = setCurrentCallStackFrameNoFire(callStackFrame = null);
        else
        try {
            old = setCurrentCallStackFrameNoFire(callStackFrame = thread.getCallStack (0, 1) [0]);
        } catch (AbsentInformationException e) {
            old = setCurrentCallStackFrameNoFire(callStackFrame = null);
        }
        if (old == callStackFrame) return null;
        else return new PropertyChangeEvent(this, PROP_CURRENT_CALL_STACK_FRAME,
                                            old, callStackFrame);
    }
    
    private List<EventRequest> disableAllBreakpoints () {
        logger.fine ("disableAllBreakpoints() start.");
        List<EventRequest> l = new ArrayList<EventRequest>();
        VirtualMachine vm = getVirtualMachine ();
        if (vm == null) return l;
        EventRequestManager erm = vm.eventRequestManager ();
        l.addAll (erm.accessWatchpointRequests ());
        l.addAll (erm.breakpointRequests ());
        l.addAll (erm.classPrepareRequests ());
        l.addAll (erm.classUnloadRequests ());
        l.addAll (erm.exceptionRequests ());
        l.addAll (erm.methodEntryRequests ());
        l.addAll (erm.methodExitRequests ());
        l.addAll (erm.modificationWatchpointRequests ());
//        l.addAll (erm.stepRequests ());
        l.addAll (erm.threadDeathRequests ());
        l.addAll (erm.threadStartRequests ());
        int i = l.size () - 1;
        for (; i >= 0; i--)
            if (!l.get (i).isEnabled ())
                l.remove (i);
            else
                l.get (i).disable ();
        operator.breakpointsDisabled();
        logger.fine ("disableAllBreakpoints() end.");
        return l;
    }
    
    private void enableAllBreakpoints (List<EventRequest> l) {
        logger.fine ("enableAllBreakpoints() start.");
        operator.breakpointsEnabled();
        int i, k = l.size ();
        for (i = 0; i < k; i++)
            try {
                l.get (i).enable ();
            } catch (IllegalThreadStateException ex) {
                // see #53163
                // this can occurre if there is some "old" StepRequest and
                // thread named in the request has died
            } catch (InvalidRequestStateException ex) {
                // workaround for #51176
            }
        logger.fine ("enableAllBreakpoints() end.");
    }

    private void checkJSR45Languages (JPDAThread t) {
        if (t.getStackDepth () > 0)
            try {
                CallStackFrame f = t.getCallStack (0, 1) [0];
                List l = f.getAvailableStrata ();
                int i, k = l.size ();
                for (i = 0; i < k; i++) {
                    if (!languages.contains (l.get (i))) {
                        String language = (String) l.get (i);
                        DebuggerManager.getDebuggerManager ().startDebugging (
                            createJSR45DI (language)
                        );
                        languages.add (language);
                    }
                } // for
                String stratum = f.getDefaultStratum ();
                if ( (stratum != null) &&
                     (!stratum.equals (lastStratumn))
                )
                    javaEngineProvider.getSession ().setCurrentLanguage (stratum);
                lastStratumn = stratum;
            } catch (AbsentInformationException e) {
                System.out.println("NoInformationException");
            }
    }
 
    private Set<JSR45DebuggerEngineProvider> jsr45EngineProviders;

    private DebuggerInfo createJSR45DI (final String language) {
        if (jsr45EngineProviders == null) {
            jsr45EngineProviders = new HashSet<JSR45DebuggerEngineProvider>(1);
        }
        JSR45DebuggerEngineProvider provider = new JSR45DebuggerEngineProvider(language);
        jsr45EngineProviders.add(provider);
        return DebuggerInfo.create (
            "netbeans-jpda-JSR45DICookie-" + language,
            new Object[] {
                new DelegatingSessionProvider () {
                    public Session getSession (
                        DebuggerInfo debuggerInfo
                    ) {
                        return javaEngineProvider.getSession ();
                    }
                },
                provider
            }
        );
    }
    
    public JPDAStep createJPDAStep(int size, int depth) {
        Session session = (Session) lookupProvider.lookupFirst (null, Session.class);
        return new JPDAStepImpl(this, session, size, depth);
    }
    
    /*public synchronized Heap getHeap() {
        if (virtualMachine != null && canGetInstanceInfo(virtualMachine)) {
            return new HeapImpl(virtualMachine);
        } else {
            return null;
        }
    }*/
    
    public List<JPDAClassType> getAllClasses() {
        List<ReferenceType> classes;
        synchronized (this) {
            if (virtualMachine == null) {
                classes = Collections.emptyList();
            } else {
                classes = virtualMachine.allClasses();
            }
        }
        return new ClassTypeList(this, classes);
    }
    
    public List<JPDAClassType> getClassesByName(String name) {
        List<ReferenceType> classes;
        synchronized (this) {
            if (virtualMachine == null) {
                classes = Collections.emptyList();
            } else {
                classes = virtualMachine.classesByName(name);
            }
        }
        return new ClassTypeList(this, classes);
    }
    
    public long[] getInstanceCounts(List<JPDAClassType> classTypes) throws UnsupportedOperationException {
        if (Java6Methods.isJDK6()) {
            VirtualMachine vm;
            synchronized (this) {
                vm = virtualMachine;
            }
            if (vm == null) {
                throw new UnsupportedOperationException("No VM available.");
            }
            if (classTypes instanceof ClassTypeList) {
                ClassTypeList cl = (ClassTypeList) classTypes;
                return Java6Methods.instanceCounts(vm, cl.getTypes());
            } else {
                List<ReferenceType> types = new ArrayList<ReferenceType>(classTypes.size());
                for (JPDAClassType clazz : classTypes) {
                    types.add(((JPDAClassTypeImpl) clazz).getType());
                }
                return Java6Methods.instanceCounts(vm, types);
            }
        } else {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
    
    public synchronized boolean canGetInstanceInfo() {
        return virtualMachine != null && canGetInstanceInfo(virtualMachine);
    }
    
    private static boolean canGetInstanceInfo(VirtualMachine vm) {
        if (Java6Methods.isJDK6()) {
            try {
                java.lang.reflect.Method canGetInstanceInfoMethod = VirtualMachine.class.getMethod("canGetInstanceInfo", new Class[] {});
                Object canGetInstanceInfo = canGetInstanceInfoMethod.invoke(vm, new Object[] {});
                return Boolean.TRUE.equals(canGetInstanceInfo);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        return false;
    }
}
