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

import com.sun.jdi.Bootstrap;
import com.sun.jdi.ClassType;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.LookupProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;

import org.netbeans.modules.debugger.jpda.evaluator.Evaluator;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.models.LocalsTreeModel;
import org.netbeans.modules.debugger.jpda.models.ThreadsTreeModel;
import org.netbeans.modules.debugger.jpda.util.JPDAUtils;
import org.netbeans.modules.debugger.jpda.util.Operator;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.debugger.DelegatingDebuggerEngineProvider;
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
    private int                         state = STATE_DISCONNECTED;
    private ArrayList                   breakpointImpls;
    private Operator                    operator;
    private PropertyChangeSupport       pcs;
    private JPDAThreadImpl              currentThread;
    private CallStackFrame              currentCallStackFrame;
    private int                         suspend = SUSPEND_EVENT_THREAD;
    public final Object                 LOCK = new Object ();
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
     * Returns excerption if initialization of VirtualMachine has failed.
     *
     * @returns excerption if initialization of VirtualMachine has failed
     * @see AbstractDICookie#getVirtualMachine()
     */
    public Exception getException () {
        return exception;
    }

    
    // other methods ...........................................................

    public void setException (Exception e) {
        exception = e;
    }
    
    public void setCurrentThread (JPDAThread thread) {
        updateCurrentCallStackFrame (thread);
        if (thread == currentThread) return;
        Object oldT = currentThread;
        CallStackFrame oldCSF = currentCallStackFrame;
        currentThread = (JPDAThreadImpl) thread;
        
        pcs.firePropertyChange (PROP_CURRENT_THREAD, oldT, currentThread);
        pcs.firePropertyChange (
            PROP_CURRENT_CALL_STACK_FRAME, 
            oldCSF, 
            currentCallStackFrame
        );
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
    
    private void updateCurrentCallStackFrame (JPDAThread thread) {
        if ( (thread == null) ||
             (thread.getStackDepth () < 1))
            currentCallStackFrame = null;
        else
        try {
            currentCallStackFrame = thread.getCallStack () [0];
        } catch (NoInformationException e) {
            currentCallStackFrame = null;
        }
    }
     
    public Value evaluateIn (
        String expression
    ) throws InvalidExpressionException {
        return evaluateIn (expression, getEvaluationThread ());
    }
     
    public Value evaluateIn (
        String expression,
        ThreadReference thread
    ) throws InvalidExpressionException {
        if (thread == null) 
            throw new InvalidExpressionException ("No current context");
        return Evaluator.evaluate (
            expression, 
            virtualMachine,
            thread,
            0,
            new ArrayList ()
        );
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
    
    public void setRunning (VirtualMachine vm, Operator o) {
        this.virtualMachine = vm;
        operator = o;
        Iterator i = vm.allThreads ().iterator ();
        int s = 0;
        while (i.hasNext ()) {
            ThreadReference tr = (ThreadReference) i.next ();
            if (tr.isSuspended ()) {
                setState (STATE_RUNNING);
                virtualMachine.resume ();
                return;
            }
        }
        setState (STATE_RUNNING);
    }

    /**
     * Change statefrom stopped or starting to running.
     */
    public void setRunning () {
        if (getState () == STATE_RUNNING) {
            System.err.println("already resumed!!");
            Thread.dumpStack();
        }
        setState (STATE_RUNNING);
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
            if (getState () == STATE_DISCONNECTED) return;
            startingThread.interrupt ();
            startingThread = null;
            try {
                if (virtualMachine != null) {
                    Process process = virtualMachine.process ();
                    if (process != null)
                        virtualMachine.exit (0);
                    else
                        virtualMachine.dispose ();
                }
            } catch (VMDisconnectedException e) {
            }
            virtualMachine = null;
            setState (STATE_DISCONNECTED);
            javaEngineProvider.getDestructor ().killEngine ();
        }
    }
    
    // other methods ....
    
    private void setState (int state) {
        if (state == this.state) return;
        int o = this.state;
        this.state = state;
        firePropertyChange (PROP_STATE, new Integer (o), new Integer (state));
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

    public void fireBadConditionalBreakpoint (final Breakpoint b) {
//        System.err.println (
//            NbBundle.getMessage (
//                JPDADebuggerImpl.class, 
//                "CTL_Incorrect_condition"
//            ) + 
//            ": " + // NOI18N
//            NbBundle.getMessage (
//                JPDADebuggerImpl.class, 
//                "CTL_breakpoint_at"
//            ) + // NOI18N
//            " " + 
//            b + "."
//            //IOManager.DEBUGGER_OUT
//        );
    }
    
    public Value invokeMethod (
        ObjectReference reference,
        Method method,
        Value[] arguments
    ) throws InvalidExpressionException {
        synchronized (LOCK) {
            if (currentThread == null) 
                throw new InvalidExpressionException ("No current context");
            return Evaluator.invokeMethod (
                reference, 
                method,
                getEvaluationThread (),
                Arrays.asList (arguments)
            );
        }
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
    
    /**
    * Fires property change.
    */
    protected void firePropertyChange (String name, Object o, Object n) {
        pcs.firePropertyChange (name, o, n);
    }

    
    // helper methods ..........................................................

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
    
    private JPDAThread getThread (ThreadReference tr) {
        try {
            return (JPDAThread) getThreadsTreeModel ().translate (tr);
        } catch (UnknownTypeException e) {
            e.printStackTrace ();
            return null;
        }
    }
    
    private ThreadReference getEvaluationThread () {
        //if (currentThread != null) return currentThread.getThreadReference ();
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
    
    private void checkJSR45Languages (JPDAThread t) {
        if (t.getStackDepth () > 0)
            try {
                CallStackFrame f = t.getCallStack () [0];
                String ds = f.getDefaultStratum ();
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
    
    private DebuggerInfo createJSR45DI (final String language) {
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
                new DebuggerEngineProvider () {
                    public String[] getLanguages () {
                        return new String[] {language};
                    }

                    public String getEngineTypeID () {
                        return "netbeans-JPDASession/" + language;
                    }

                    public Object[] getServices () {
                        return new Object [0];
                    }

                    public void setDestructor (DebuggerEngine.Destructor desctuctor) {
                    }
                }
//                new DelegatingDebuggerEngineProvider () {
//                    public DebuggerEngine getEngine () {
//                        return debuggerEngine;
//                    }
//                    public String[] getLanguages () {
//                        return new String[] {language};
//                    }
//                    public void setDestructor (DebuggerEngine.Destructor desctuctor) {
//                    }
//                }
            }
        );
    }
}
