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

import com.sun.jdi.*;
import com.sun.jdi.Method;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.lang.reflect.*;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.LookupProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.AbstractDICookie;
import org.netbeans.api.debugger.jpda.AttachingDICookie;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.DebuggerStartException;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.Variable;

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

import org.netbeans.spi.viewmodel.NoInformationException;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
* Representation of a debugging session.
*
* @author   Jan Jancura
*/
public class JPDADebuggerImpl extends JPDADebugger {


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
    private int                         suspend = SUSPEND_EVENT_THREAD;
    public final Object                 LOCK = new Object ();
    public final Object                 LOCK2 = new Object ();
    private JavaEngineProvider          javaEngineProvider;
    private Set                         languages;
    private String                      lastStratumn;
    private LookupProvider              lookupProvider;



    // init ....................................................................

    public JPDADebuggerImpl (LookupProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        pcs = new PropertyChangeSupport (this);
        List l = lookupProvider.lookup (DebuggerEngineProvider.class);
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
        return getVirtualMachine ().canRedefineClasses ();
    }

    /**
     * Returns <code>true</code> if this debugger supports fix & continue 
     * (HotSwap).
     *
     * @return <code>true</code> if this debugger supports fix & continue
     */
    public boolean canFixClasses () {
        return getVirtualMachine ().canPopFrames ();
    }

    /**
     * Implements fix & continue (HotSwap). Map should contain class names
     * as a keys, and byte[] arrays as a values.
     *
     * @param classes a map from class names to be fixed to byte[] 
     */
    public void fixClasses (Map classes) {
        synchronized (LOCK2) {
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

            // pop obsoleted frames
            JPDAThread t = getCurrentThread ();
            CallStackFrame frame = getCurrentCallStackFrame ();
            if (t.getStackDepth () < 2) return;
            try {
                if (!frame.equals (t.getCallStack () [0])) return;
            } catch (NoInformationException ex) {
                return;
            }
            if (!frame.isObsolete ()) return;
            frame.popFrame ();
            setState (STATE_RUNNING);
            updateCurrentCallStackFrame (t);
            setState (STATE_STOPPED);
        }
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
        synchronized (LOCK2) {
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

    public Value evaluateIn(String expression) throws InvalidExpressionException {
        Expression expr = null;
        try {
            expr = Expression.parse(expression, Expression.LANGUAGE_JAVA_1_5);
            return evaluateIn(expr);
        } catch (ParseException e) {
            InvalidExpressionException iee = new InvalidExpressionException(e.getMessage());
            iee.initCause(e);
            throw iee;
        }
    }

    public Value evaluateIn (Expression expression) 
    throws InvalidExpressionException {
        if (getCurrentCallStackFrame () == null) {
            throw new InvalidExpressionException("No current context (stack frame)");
        }
        synchronized (LOCK) {
            return evaluateIn (
                expression, 
                ((CallStackFrameImpl) getCurrentCallStackFrame ()).
                    getStackFrame ()
            );
        }
    }

    public Value evaluateIn (Expression expression, StackFrame frame) throws InvalidExpressionException {
        if (frame == null)
            throw new InvalidExpressionException ("No current context");

        // TODO: get imports from the source file
        List imports = new ArrayList();
        List staticImports = new ArrayList();

        try {
            org.netbeans.modules.debugger.jpda.expr.Evaluator evaluator = expression.evaluator(
                    new EvaluationContext(frame, imports, staticImports));
            synchronized (LOCK) {
                return evaluator.evaluate();
            }
        } catch (Throwable e) {
            InvalidExpressionException iee = new InvalidExpressionException(e.getMessage());
            iee.initCause(e);
            throw iee;
        }
    }

    public String getGenericSignature(TypeComponent component) {
        if (tcGenericSignatureMethod == null) return null;
        try {
            return (String) tcGenericSignatureMethod.invoke(component, new Object[0]);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;    // should not happen
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;    // should not happen
        }
    }

    public String getGenericSignature(LocalVariable component) {
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
    }

    private static final java.util.regex.Pattern jvmVersionPattern =
            java.util.regex.Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(_\\d+)?(-\\w+)?");
    private java.lang.reflect.Method  tcGenericSignatureMethod;
    private java.lang.reflect.Method  lvGenericSignatureMethod;

    public void setRunning (VirtualMachine vm, Operator o) {
        this.virtualMachine = vm;
        tcGenericSignatureMethod = null;
        if (Bootstrap.virtualMachineManager().minorInterfaceVersion() >= 5) {
            java.util.regex.Matcher m = jvmVersionPattern.matcher(virtualMachine.version());
            if (m.matches()) {
                int minor = Integer.parseInt(m.group(2));
                if (minor >= 5) {
                    try {
                        tcGenericSignatureMethod = TypeComponent.class.getMethod("genericSignature", new Class[0]);
                        lvGenericSignatureMethod = LocalVariable.class.getMethod("genericSignature", new Class[0]);
                    } catch (NoSuchMethodException e) {
                        // the method is not available, ignore generics
                    }
                }
            }
        }
        operator = o;
        Iterator i = vm.allThreads ().iterator ();
        while (i.hasNext ()) {
            ThreadReference tr = (ThreadReference) i.next ();
            if (tr.isSuspended ()) {
                setState (STATE_RUNNING);
                virtualMachine.resume ();
                synchronized (LOCK2) {
                    LOCK2.notify ();
                }
                return;
            }
        }
        setState (STATE_RUNNING);
        synchronized (LOCK2) {
            LOCK2.notify ();
        }
    }

    /**
    * Performs stop action.
    */
    public void setStoppedState (ThreadReference thread) {
        //S ystem.err.println("setStoppedState");
        JPDAThread t = getThread (thread);

        checkJSR45Languages (t);
        setCurrentThread (t);
        if (getState () == STATE_STOPPED) {
            System.err.println("already stopped!!");
            Thread.dumpStack();
        }
        setState (STATE_STOPPED);
        //S ystem.err.println("setStoppedState end");
    }

    public void finish () {
        synchronized (LOCK) {
            AbstractDICookie di = (AbstractDICookie) lookupProvider.lookupFirst 
                (AbstractDICookie.class);
            if (getState () == STATE_DISCONNECTED) return;
            startingThread.interrupt ();
            startingThread = null;
            try {
                if (virtualMachine != null) {
                    if (di instanceof AttachingDICookie)
                        virtualMachine.dispose ();
                    else
                        virtualMachine.exit (0);
                }
            } catch (VMDisconnectedException e) {
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
        }
    }

    /**
     * Suspends the target virtual machine (if any).
     *
     * @see  com.sun.jdi.ThreadReference#suspend
     */
    public void suspend () {
        synchronized (LOCK) {
            //S ystem.err.println("suspend");
            if (getState () == STATE_STOPPED) {
                System.err.println("already suspend!!");
                Thread.dumpStack();
            }
            if (virtualMachine != null)
                virtualMachine.suspend ();
            setState (STATE_STOPPED);
            //S ystem.err.println("suspend end");
        }
    }

    public void resume () {
        synchronized (LOCK) {
            //S ystem.err.println("resume");
            if (getState () == STATE_RUNNING) {
                System.err.println("already resumed!!");
                Thread.dumpStack();
            }
            if (virtualMachine != null)
                virtualMachine.resume ();
            setState (STATE_RUNNING);
            //S ystem.err.println("resume end");
        }
    }

    public Value invokeMethod (
        ObjectReference reference,
        Method method,
        Value[] arguments
    ) throws InvalidExpressionException {
        if (currentThread == null)
            throw new InvalidExpressionException ("No current context");
        try {
            synchronized (LOCK) {
                return org.netbeans.modules.debugger.jpda.expr.Evaluator.invokeVirtual (
                    reference,
                    method,
                    getEvaluationThread (),
                    Arrays.asList (arguments)
                );
            }
        } catch (org.netbeans.modules.debugger.jpda.expr.Evaluator.TimeoutException e) {
            throw new InvalidExpressionException(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new InvalidExpressionException(e.getMessage());
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

    private void setState (int state) {
        if (state == this.state) return;
        int o = this.state;
        this.state = state;
        firePropertyChange (PROP_STATE, new Integer (o), new Integer (state));
    }

    /**
    * Fires property change.
    */
    private void firePropertyChange (String name, Object o, Object n) {
        pcs.firePropertyChange (name, o, n);
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
            setCurrentCallStackFrame (thread.getCallStack () [0]);
        } catch (NoInformationException e) {
            setCurrentCallStackFrame (null);
        }
    }

    private void checkJSR45Languages (JPDAThread t) {
        if (t.getStackDepth () > 0)
            try {
                CallStackFrame f = t.getCallStack () [0];
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
            } catch (NoInformationException e) {
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
