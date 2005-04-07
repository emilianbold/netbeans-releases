/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.TypeComponent;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.InvalidRequestStateException;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.IllegalThreadStateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.debugger.DebuggerEngine;


import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.api.debugger.Properties;

import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadGroupImpl;
import org.netbeans.modules.debugger.jpda.util.JPDAUtils;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.AbstractDICookie;
import org.netbeans.api.debugger.jpda.AttachingDICookie;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.DebuggerStartException;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.breakpoints.BreakpointsEngineListener;


import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.models.LocalsTreeModel;
import org.netbeans.modules.debugger.jpda.models.ThreadsTreeModel;
import org.netbeans.modules.debugger.jpda.models.CallStackFrameImpl;
import org.netbeans.modules.debugger.jpda.util.Operator;
import org.netbeans.modules.debugger.jpda.expr.Expression;
import org.netbeans.modules.debugger.jpda.expr.EvaluationContext;
import org.netbeans.modules.debugger.jpda.expr.ParseException;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.debugger.DelegatingSessionProvider;

import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.RequestProcessor;


/**
* Representation of a debugging session.
*
* @author   Jan Jancura
*/
public class JPDADebuggerImpl extends JPDADebugger implements Runnable {
    
    private static final boolean startVerbose = 
        System.getProperty ("netbeans.debugger.start") != null;


    // variables ...............................................................

    //private DebuggerEngine              debuggerEngine;
    private VirtualMachine              virtualMachine = null;
    private Exception                   exception;
    private Thread                      startingThread;
    private int                         state = 0;
    private Operator                    operator;
    private PropertyChangeSupport       pcs;
    private JPDAThreadImpl              currentThread;
    private CallStackFrame              currentCallStackFrame;
    private int                         suspend = SUSPEND_ALL;
    public final Object                 LOCK = new Object ();
    public final Object                 LOCK2 = new Object ();
    private JavaEngineProvider          javaEngineProvider;
    private Set                         languages;
    private String                      lastStratumn;
    private ContextProvider             lookupProvider;

    private StackFrame      altCSF = null;  //PATCH 48174

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
        languages = new HashSet ();
        languages.add ("Java");
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
    public CallStackFrame getCurrentCallStackFrame () {
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
    public void fixClasses (Map classes) {
        synchronized (LOCK) {
            
            // 1) redefine classes
            Map map = new HashMap ();
            Iterator i = classes.keySet ().iterator ();
            while (i.hasNext ()) {
                String className = (String) i.next ();
                List classRefs = getVirtualMachine ().classesByName (className);
                int j, jj = classRefs.size ();
                for (j = 0; j < jj; j++)
                    map.put (
                        (ReferenceType) classRefs.get (j), 
                        classes.get (className)
                    );
            }
            getVirtualMachine ().redefineClasses (map);

            // 2) pop obsoleted frames
            JPDAThread t = getCurrentThread ();
            if (t == null) return;
            CallStackFrame frame = getCurrentCallStackFrame ();
            if (t.getStackDepth () < 2) return;
            try {
                if (!frame.equals (t.getCallStack (0, 1) [0])) return;
            } catch (AbsentInformationException ex) {
                return;
            }
            if (!frame.isObsolete ()) return;
            frame.popFrame ();
            setState (STATE_RUNNING);
            updateCurrentCallStackFrame (t);
            setState (STATE_STOPPED);
            
            // update breakpoints
            Session s = (Session) 
                lookupProvider.lookupFirst (null, Session.class);
            DebuggerEngine de = s.getEngineForLanguage ("Java");
            BreakpointsEngineListener bel = (BreakpointsEngineListener) 
                de.lookupFirst (null, LazyActionsManagerListener.class);
            bel.fixBreakpointImpls ();
        }
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
            try {
                thread.popFrames (frame);
                setState (STATE_RUNNING);
                updateCurrentCallStackFrame (getThread (thread));
                setState (STATE_STOPPED);
            } catch (IncompatibleThreadStateException ex) {
                ex.printStackTrace ();
            }
        }
    }

    public void setException (Exception e) {
        synchronized (LOCK2) {
            exception = e;
            LOCK2.notify ();
        }
    }

    public void setCurrentThread (JPDAThread thread) {
        Object oldT = currentThread;
        currentThread = (JPDAThreadImpl) thread;
        if (thread != oldT)
            pcs.firePropertyChange (PROP_CURRENT_THREAD, oldT, currentThread);
        updateCurrentCallStackFrame (thread);
    }

    public void setCurrentCallStackFrame (CallStackFrame callStackFrame) {
        if (callStackFrame == currentCallStackFrame) return;
        CallStackFrame old = currentCallStackFrame;
        currentCallStackFrame = callStackFrame;
        pcs.firePropertyChange (
            PROP_CURRENT_CALL_STACK_FRAME,
            old,
            currentCallStackFrame
        );
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
            if (csf != null)
                return evaluateIn (expression, csf.getStackFrame ());
            //PATCH 48174
            if (altCSF != null)
                return evaluateIn (expression, altCSF);
            
            throw new InvalidExpressionException
                    ("No current context (stack frame)");
            
        }
    }

    /**
     * Used by BreakpointImpl.
     */
    public  Value evaluateIn (Expression expression, StackFrame frame) 
    throws InvalidExpressionException {
        synchronized (LOCK) {
            if (frame == null)
                throw new InvalidExpressionException ("No current context");

            // TODO: get imports from the source file
            List imports = new ArrayList ();
            List staticImports = new ArrayList ();
            imports.add ("java.lang.*");
            try {
                imports.addAll (Arrays.asList (EditorContextBridge.getImports (
                    getEngineContext ().getURL (frame, "Java")
                )));
                org.netbeans.modules.debugger.jpda.expr.Evaluator evaluator = 
                    expression.evaluator (
                        new EvaluationContext (
                            frame,
                            imports, 
                            staticImports
                        )
                    );
                List l = disableAllBreakpoints ();
                try {
                    return evaluator.evaluate ();
                } finally {
                    enableAllBreakpoints (l);
                }
            } catch (Throwable e) {
                InvalidExpressionException iee = new InvalidExpressionException (e);
                iee.initCause (e);
                throw iee;
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
        try {
            synchronized (LOCK) {
                List l = disableAllBreakpoints ();
                try {
                    return org.netbeans.modules.debugger.jpda.expr.Evaluator.
                        invokeVirtual (
                            reference,
                            method,
                            getEvaluationThread (),
                            Arrays.asList (arguments)
                        );
                } finally {
                    enableAllBreakpoints (l);
                }
            }
        } catch (org.netbeans.modules.debugger.jpda.expr.Evaluator.TimeoutException e) {
            throw new InvalidExpressionException(e.getMessage());
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

    public void setStarting (Thread startingThread) {
        this.startingThread = startingThread;
        setState (STATE_STARTING);
        RequestProcessor.getDefault ().post (this, 200);
    }

    public void setRunning (VirtualMachine vm, Operator o) {
        if (startVerbose) {
            System.out.println("\nS JPDADebuggerImpl.setRunning ()");
            JPDAUtils.printFeatures (vm);
        }
        virtualMachine = vm;
        
        initGenericsSupport ();
        
        operator = o;
        Iterator i = getVirtualMachine ().allThreads ().iterator ();
        while (i.hasNext ()) {
            ThreadReference tr = (ThreadReference) i.next ();
            if (tr.isSuspended ()) {
                if (startVerbose)
                    System.out.println("\nS JPDADebuggerImpl.setRunning () - " +
                        "thread supended"
                    );
                setState (STATE_RUNNING);
                virtualMachine.resume ();
                if (startVerbose)
                    System.out.println("\nS JPDADebuggerImpl.setRunning () - " +
                        "thread supended - VM resumed - end"
                    );
                synchronized (LOCK2) {
                    LOCK2.notify ();
                }
                return;
            }
        }
        
        setState (STATE_RUNNING);
        if (startVerbose)
            System.out.println("\nS JPDADebuggerImpl.setRunning () - end");
        synchronized (LOCK2) {
            LOCK2.notify ();
        }
    }

    /**
    * Performs stop action.
    */
    public void setStoppedState (ThreadReference thread) {
        synchronized (LOCK) {
            // this method can be called in stopped state to switch 
            // the current thread only
            JPDAThread t = getThread (thread);
            checkJSR45Languages (t);
            setCurrentThread (t);
            setState (STATE_STOPPED);
        }
    }

    /**
     * Used by KillActionProvider.
     */
    public void finish () {
        //Workaround for #56233
        //synchronized (LOCK) { 
            if (startVerbose)
                System.out.println("\nS StartActionProvider.finish ()");
            AbstractDICookie di = (AbstractDICookie) lookupProvider.lookupFirst 
                (null, AbstractDICookie.class);
            if (getState () == STATE_DISCONNECTED) return;
            if (startingThread != null) startingThread.interrupt ();
            startingThread = null;
            try {
                if (virtualMachine != null) {
                    if (di instanceof AttachingDICookie) {
                        if (startVerbose)
                            System.out.println ("\nS StartActionProvider." +
                                "finish () VM dispose"
                            );
                        virtualMachine.dispose ();
                    } else {
                        if (startVerbose)
                            System.out.println ("\nS StartActionProvider." +
                                "finish () VM exit"
                            );
                        virtualMachine.exit (0);
                    }
                }
            } catch (VMDisconnectedException e) {
                if (startVerbose)
                    System.out.println ("\nS StartActionProvider." +
                        "finish () VM exception " + e
                    );
                // debugee VM is already disconnected (it finished normally)
            }
            virtualMachine = null;
            setState (STATE_DISCONNECTED);
            if (jsr45EngineProviders != null) {
                for (Iterator i = jsr45EngineProviders.iterator(); i.hasNext();) {
                    JSR45DebuggerEngineProvider provider = (JSR45DebuggerEngineProvider) i.next();
                    provider.getDesctuctor().killEngine();
                }
                jsr45EngineProviders = null;
            }
            javaEngineProvider.getDestructor ().killEngine ();
            if (startVerbose)
                System.out.println ("\nS StartActionProvider." +
                    "finish () end "
                );
        //}
    }

    /**
     * Suspends the target virtual machine (if any).
     * Used by PauseActionProvider.
     *
     * @see  com.sun.jdi.ThreadReference#suspend
     */
    public void suspend () {
        synchronized (LOCK) {
            if (virtualMachine != null)
                virtualMachine.suspend ();
            setState (STATE_STOPPED);
        }
    }
    
    private void printThreads () {
        List l = virtualMachine.allThreads ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            ThreadReference tr = (ThreadReference) l.get (i);
            System.out.println("  " + tr.name () + " : " + tr.suspendCount () + " : " + tr.isSuspended ());
        }
    }

    /**
     * Used by ContinueActionProvider & StepActionProvider.
     */
    public void resume () {
        synchronized (LOCK) {
            if (virtualMachine != null)
                virtualMachine.resume ();
            setState (STATE_RUNNING);
        }
    }

    public JPDAThread getThread (ThreadReference tr) {
        try {
            return (JPDAThread) getThreadsTreeModel ().translate (tr);
        } catch (UnknownTypeException e) {
            e.printStackTrace ();
            return null;
        }
    }

    public Variable getVariable (Value value) {
        return getLocalsTreeModel ().getVariable (value);
    }


    // private helper methods ..................................................
    
    private static final java.util.regex.Pattern jvmVersionPattern =
            java.util.regex.Pattern.compile ("(\\d+)\\.(\\d+)\\.(\\d+)(_\\d+)?(-\\w+)?");
    private static java.lang.reflect.Method  tcGenericSignatureMethod;
    private static java.lang.reflect.Method  lvGenericSignatureMethod;


    private void initGenericsSupport () {
        tcGenericSignatureMethod = null;
        if (Bootstrap.virtualMachineManager ().minorInterfaceVersion () >= 5) {
            java.util.regex.Matcher m = jvmVersionPattern.matcher 
                (virtualMachine.version ());
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
    
    private void setState (int state) {
        if (state == this.state) return;
        int o = this.state;
        this.state = state;
        firePropertyChange (PROP_STATE, new Integer (o), new Integer (state));
        
        //PENDING HACK see issue 46287
        System.setProperty(
            "org.openide.awt.SwingBrowserImpl.do-not-block-awt",
            String.valueOf (state != STATE_DISCONNECTED)
        );
    }

    /**
    * Fires property change.
    */
    private void firePropertyChange (String name, Object o, Object n) {
        pcs.firePropertyChange (name, o, n);
    }

    private SourcePath engineContext;
    SourcePath getEngineContext () {
        if (engineContext == null)
            engineContext = (SourcePath) lookupProvider.
                lookupFirst (null, SourcePath.class);
        return engineContext;
    }

    private ThreadsTreeModel threadsTreeModel;
    ThreadsTreeModel getThreadsTreeModel () {
        if (threadsTreeModel == null)
            threadsTreeModel = (ThreadsTreeModel) lookupProvider.
                lookupFirst ("ThreadsView", TreeModel.class);
        return threadsTreeModel;
    }

    private LocalsTreeModel localsTreeModel;
    LocalsTreeModel getLocalsTreeModel () {
        if (localsTreeModel == null)
            localsTreeModel = (LocalsTreeModel) lookupProvider.
                lookupFirst ("LocalsView", TreeModel.class);
        return localsTreeModel;
    }

    private ThreadReference getEvaluationThread () {
        if (currentThread != null) return currentThread.getThreadReference ();
        if (virtualMachine == null) return null;
        List l = virtualMachine.allThreads ();
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
    
    private List disableAllBreakpoints () {
        List l = new ArrayList ();
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
        l.addAll (erm.stepRequests ());
        l.addAll (erm.threadDeathRequests ());
        l.addAll (erm.threadStartRequests ());
        int i = l.size () - 1;
        for (; i >= 0; i--)
            if (!((EventRequest) l.get (i)).isEnabled ())
                l.remove (i);
            else
                ((EventRequest) l.get (i)).disable ();
        return l;
    }
    
    private void enableAllBreakpoints (List l) {
        int i, k = l.size ();
        for (i = 0; i < k; i++)
            try {
                ((EventRequest) l.get (i)).enable ();
            } catch (IllegalThreadStateException ex) {
                // see #53163
                // this can occurre if there is some "old" StepRequest and
                // thread named in the request has died
            } catch (InvalidRequestStateException ex) {
                // workaround for #51176
            }
    }
    
    public void run () {
        if (getState () == JPDADebugger.STATE_DISCONNECTED)
            return;
        JPDAThreadImpl t = (JPDAThreadImpl) getCurrentThread ();
        if (t != null) {
            if ( t.getThreadReference ().isSuspended () &&
                 getState () == STATE_RUNNING
            )
                setState (STATE_STOPPED);
        }
        RequestProcessor.getDefault ().post (this, 200);
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

    private Set jsr45EngineProviders;

    private DebuggerInfo createJSR45DI (final String language) {
        if (jsr45EngineProviders == null) {
            jsr45EngineProviders = new HashSet(1);
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
}
