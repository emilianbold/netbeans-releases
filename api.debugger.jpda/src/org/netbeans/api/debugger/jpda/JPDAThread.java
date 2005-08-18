/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger.jpda;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ThreadReference;
import java.beans.PropertyChangeListener;


/**
 * Represents one Java thread in debugged process.
 * 
 * <pre style="background-color: rgb(255, 255, 102);">
 * Since JDI interfaces evolve from one version to another, it's strongly recommended
 * not to implement this interface in client code. New methods can be added to
 * this interface at any time to keep up with the JDI functionality.</pre>
 *
 * @author Jan Jancura
 */
public interface JPDAThread {

    /** Thread state constant. */
    public static final int STATE_UNKNOWN = ThreadReference.THREAD_STATUS_UNKNOWN;
    /** Thread state constant. */
    public static final int STATE_MONITOR = ThreadReference.THREAD_STATUS_MONITOR;
    /** Thread state constant. */
    public static final int STATE_NOT_STARTED = ThreadReference.THREAD_STATUS_NOT_STARTED;
    /** Thread state constant. */
    public static final int STATE_RUNNING = ThreadReference.THREAD_STATUS_RUNNING;
    /** Thread state constant. */
    public static final int STATE_SLEEPING = ThreadReference.THREAD_STATUS_SLEEPING;
    /** Thread state constant. */
    public static final int STATE_WAIT = ThreadReference.THREAD_STATUS_WAIT;
    /** Thread state constant. */
    public static final int STATE_ZOMBIE = ThreadReference.THREAD_STATUS_ZOMBIE;

    /** Property name constant. */
    public static final String PROP_CALLSTACK = "callStack";
    /** Property name constant. */
    public static final String PROP_VARIABLES = "variables";

    
    
    /**
     * Getter for the name of thread property.
     *
     * @return name of thread
     */
    public abstract String getName ();
    
    /**
     * Returns parent thread group.
     *
     * @return parent thread group
     */
    public abstract JPDAThreadGroup getParentThreadGroup ();

    /**
     * Returns line number of the location this thread stopped at.
     * The thread should be suspended at the moment this method is called.
     *
     * @return  line number of the current location if the thread is suspended,
     *          contains at least one frame and the topmost frame does not
     *          represent a native method invocation; <CODE>-1</CODE> otherwise
     * @see CallStackFrame
     */
    public abstract int getLineNumber (String stratum);

    /**
     * Returns current state of this thread.
     *
     * @return current state of this thread
     */
    public abstract int getState ();
    
    /**
     * Returns true if this thread is suspended by debugger.
     *
     * @return true if this thread is suspended by debugger
     */
    public abstract boolean isSuspended ();

    /**
     * If this thread is suspended returns class name this thread is 
     * stopped in.
     *
     * @return class name this thread is stopped in
     */
    public abstract String getClassName ();

    /**
     * If this thread is suspended returns method name this thread is 
     * stopped in.
     *
     * @return method name this thread is stopped in
     */
    public abstract String getMethodName ();
    
    /**
     * Suspends thread.
     */
    public abstract void suspend ();
    
    /**
     * Unsuspends thread.
     */
    public abstract void resume ();
    
    /**
     * Interrupts this thread unless the thread has been suspended.
     * @since 2.1
     */
    public abstract void interrupt();
    
    /**
     * Returns file name this frame is stopped in or null.
     *
     * @return file name this frame is stopped in
     */
    public abstract String getSourceName (String stratum) 
    throws AbsentInformationException;
    
    /**
     * Returns source path of file this frame is stopped in or null.
     *
     * @return source path of file this frame is stopped in or null
     */
    public abstract String getSourcePath (String stratum) 
    throws AbsentInformationException;
    
    /**
     * Returns call stack for this thread.
     *
     * @throws NoInformationException if the thread is running or not able
     *         to return callstack
     * @return call stack
     */
    public abstract CallStackFrame[] getCallStack () 
    throws AbsentInformationException;
    
    /**
     * Returns call stack for this thread on the given indexes.
     *
     * @param from a from index
     * @param to a to index
     * @throws NoInformationException if the thread is running or not able
     *         to return callstack
     * @return call stack
     */
    public abstract CallStackFrame[] getCallStack (int from, int to) 
    throws AbsentInformationException;
    
    /**
     * Returns length of current call stack.
     *
     * @return length of current call stack
     */
    public abstract int getStackDepth ();
    
    /**
     * Sets this thread current.
     *
     * @see JPDADebugger#getCurrentThread
     */
    public abstract void makeCurrent ();
    
    /**
     * Returns monitor this thread is waiting on.
     *
     * @return monitor this thread is waiting on
     */
    public abstract ObjectVariable getContendedMonitor ();
    
    /**
     * Returns monitors owned by this thread.
     *
     * @return monitors owned by this thread
     */
    public abstract ObjectVariable[] getOwnedMonitors ();
}
