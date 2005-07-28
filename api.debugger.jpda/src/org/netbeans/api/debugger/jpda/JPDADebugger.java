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

package org.netbeans.api.debugger.jpda;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.ListeningConnector;
import com.sun.jdi.request.EventRequest;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.Variable;


/**
 * Represents one JPDA debugger session (one 
 * {@link com.sun.jdi.VirtualMachine}). 
 *
 * <br><br>
 * <b>How to obtain it from DebuggerEngine:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    JPDADebugger jpdaDebugger = (JPDADebugger) debuggerEngine.lookup 
 *        (JPDADebugger.class);</pre>
 *
 * @author Jan Jancura
 */
public abstract class JPDADebugger {

    /** Name of property for state of debugger. */
    public static final String          PROP_STATE = "state";
    /** Name of property for current thread. */
    public static final String          PROP_CURRENT_THREAD = "currentThread";
    /** Name of property for current stack frame. */
    public static final String          PROP_CURRENT_CALL_STACK_FRAME = "currentCallStackFrame";
    /** Property name constant. */
    public static final String          PROP_SUSPEND = "suspend"; // NOI18N

    /** Suspend property value constant. */
    public static final int             SUSPEND_ALL = EventRequest.SUSPEND_ALL;
    /** Suspend property value constant. */
    public static final int             SUSPEND_EVENT_THREAD = EventRequest.SUSPEND_EVENT_THREAD;
    
    /** Debugger state constant. */
    public static final int             STATE_STARTING = 1;
    /** Debugger state constant. */
    public static final int             STATE_RUNNING = 2;
    /** Debugger state constant. */
    public static final int             STATE_STOPPED = 3;
    /** Debugger state constant. */
    public static final int             STATE_DISCONNECTED = 4;

    /** ID of JPDA Debugger Engine. */
    public static final String          ENGINE_ID = "netbeans-JPDASession/Java";
    /** ID of JPDA Debugger Engine. */
    public static final String          SESSION_ID = "netbeans-JPDASession";
    

    
    /**
     * This utility method helps to start a new JPDA debugger session. 
     * Its implementation use {@link LaunchingDICookie} and 
     * {@link org.netbeans.api.debugger.DebuggerManager#getDebuggerManager}.
     *
     * @param mainClassName a name or main class
     * @param args command line arguments
     * @param classPath a classPath
     * @param suspend if true session will be suspended
     */
    public static void launch (
        String          mainClassName,
        String[]        args,
        String          classPath,
        boolean         suspend
    ) {
        DebuggerManager.getDebuggerManager ().startDebugging (
            DebuggerInfo.create (
                LaunchingDICookie.ID,
                new Object[] {
                    LaunchingDICookie.create (
                        mainClassName,
                        args,
                        classPath,
                        suspend
                    )
                }
            )
        );
    }
    
    /**
     * This utility method helps to start a new JPDA debugger session. 
     * Its implementation use {@link ListeningDICookie} and 
     * {@link org.netbeans.api.debugger.DebuggerManager#getDebuggerManager}.
     *
     * @param name a name of shared memory block
     */
    public static JPDADebugger listen (
        ListeningConnector        connector,
        Map                       args,
        Object[]                  services
    ) throws DebuggerStartException {
        Object[] s = new Object [services.length + 1];
        System.arraycopy (services, 0, s, 1, services.length);
        s [0] = ListeningDICookie.create (
            connector,
            args
        );
        DebuggerEngine[] es = DebuggerManager.getDebuggerManager ().
            startDebugging (
                DebuggerInfo.create (
                    ListeningDICookie.ID,
                    s
                )
            );
        int i, k = es.length;
        for (i = 0; i < k; i++) {
            JPDADebugger d = (JPDADebugger) es [i].lookupFirst 
                (null, JPDADebugger.class);
            if (d == null) continue;
            d.waitRunning ();
            return d;
        }
        throw new DebuggerStartException (new InternalError ());
    }
    
    /**
     * This utility method helps to start a new JPDA debugger session. 
     * Its implementation use {@link ListeningDICookie} and 
     * {@link org.netbeans.api.debugger.DebuggerManager#getDebuggerManager}.
     *
     * @param name a name of shared memory block
     */
    public static void startListening (
        ListeningConnector        connector,
        Map                       args,
        Object[]                  services
    ) throws DebuggerStartException {
        Object[] s = new Object [services.length + 1];
        System.arraycopy (services, 0, s, 1, services.length);
        s [0] = ListeningDICookie.create (
            connector,
            args
        );
        DebuggerEngine[] es = DebuggerManager.getDebuggerManager ().
            startDebugging (
                DebuggerInfo.create (
                    ListeningDICookie.ID,
                    s
                )
            );
    }
    
    /**
     * This utility method helps to start a new JPDA debugger session. 
     * Its implementation use {@link AttachingDICookie} and 
     * {@link org.netbeans.api.debugger.DebuggerManager#getDebuggerManager}.
     *
     * @param hostName a name of computer to attach to
     * @param portNumber a potr number
     */
    public static JPDADebugger attach (
        String          hostName,
        int             portNumber,
        Object[]        services
    ) throws DebuggerStartException {
        Object[] s = new Object [services.length + 1];
        System.arraycopy (services, 0, s, 1, services.length);
        s [0] = AttachingDICookie.create (
            hostName,
            portNumber
        );
        DebuggerEngine[] es = DebuggerManager.getDebuggerManager ().
            startDebugging (
                DebuggerInfo.create (
                    AttachingDICookie.ID,
                    s
                )
            );
        int i, k = es.length;
        for (i = 0; i < k; i++) {
            JPDADebugger d = (JPDADebugger) es [i].lookupFirst 
                (null, JPDADebugger.class);
            if (d == null) continue;
            d.waitRunning ();
            return d;
        }
        throw new DebuggerStartException (new InternalError ());
    }
    
    /**
     * This utility method helps to start a new JPDA debugger session. 
     * Its implementation use {@link AttachingDICookie} and 
     * {@link org.netbeans.api.debugger.DebuggerManager#getDebuggerManager}.
     *
     * @param hostName a name of computer to attach to
     * @param portNumber a potr number
     */
    public static JPDADebugger attach (
        String          name,
        Object[]        services
    ) throws DebuggerStartException {
        Object[] s = new Object [services.length + 1];
        System.arraycopy (services, 0, s, 1, services.length);
        s [0] = AttachingDICookie.create (
            name
        );
        DebuggerEngine[] es = DebuggerManager.getDebuggerManager ().
            startDebugging (
                DebuggerInfo.create (
                    AttachingDICookie.ID,
                    s
                )
            );
        int i, k = es.length;
        for (i = 0; i < k; i++) {
            JPDADebugger d = (JPDADebugger) es [i].lookupFirst 
                (null, JPDADebugger.class);
            d.waitRunning ();
            if (d == null) continue;
            return d;
        }
        throw new DebuggerStartException (new InternalError ());
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
    public abstract int getState ();
    
    /**
     * Gets value of suspend property.
     *
     * @return value of suspend property
     */
    public abstract int getSuspend ();

    /**
     * Sets value of suspend property.
     *
     * @param s a new value of suspend property
     */
    public abstract void setSuspend (int s);
    
    /**
     * Returns current thread or null.
     *
     * @return current thread or null
     */
    public abstract JPDAThread getCurrentThread ();
    
    /**
     * Returns current stack frame or null.
     *
     * @return current stack frame or null
     */
    public abstract CallStackFrame getCurrentCallStackFrame ();
    
    /**
     * Evaluates given expression in the current context.
     *
     * @param expression a expression to be evaluated
     *  
     * @return current value of given expression
     */
    public abstract Variable evaluate (String expression) 
    throws InvalidExpressionException;

    /**
     * Waits till the Virtual Machine is started and returns 
     * {@link DebuggerStartException} if some problem occurres.
     *
     * @throws DebuggerStartException is some problems occurres during debugger 
     *         start
     *
     * @see AbstractDICookie#getVirtualMachine()
     */
    public abstract void waitRunning () throws DebuggerStartException;

    /**
     * Returns <code>true</code> if this debugger supports fix & continue 
     * (HotSwap).
     *
     * @return <code>true</code> if this debugger supports fix & continue
     */
    public abstract boolean canFixClasses ();

    /**
     * Returns <code>true</code> if this debugger supports Pop action.
     *
     * @return <code>true</code> if this debugger supports Pop action
     */
    public abstract boolean canPopFrames ();

    /**
     * Implements fix & continue (HotSwap). Map should contain class names
     * as a keys, and byte[] arrays as a values.
     *
     * @param map a map from class names to be fixed to byte[] 
     */
    public abstract void fixClasses (Map classes);
    
    /** 
     * Returns instance of SmartSteppingFilter.
     *
     * @return instance of SmartSteppingFilter
     */
    public abstract SmartSteppingFilter getSmartSteppingFilter ();

    /**
     * Helper method that fires JPDABreakpointEvent on JPDABreakpoints.
     *
     * @param breakpoint a breakpoint to be changed
     * @param event a event to be fired
     */
    protected void fireBreakpointEvent (
        JPDABreakpoint breakpoint, 
        JPDABreakpointEvent event
    ) {
        breakpoint.fireJPDABreakpointChange (event);
    }
    
    /**
     * Adds property change listener.
     *
     * @param l new listener.
     */
    public abstract void addPropertyChangeListener (PropertyChangeListener l);

    /**
     * Removes property change listener.
     *
     * @param l removed listener.
     */
    public abstract void removePropertyChangeListener (
        PropertyChangeListener l
    );

    /**
     * Adds property change listener.
     *
     * @param propertyName a name of property to listen on
     * @param l new listener.
     */
    public abstract void addPropertyChangeListener (
        String propertyName, 
        PropertyChangeListener l
    );
    
    /**
     * Removes property change listener.
     *
     * @param propertyName a name of property to listen on
     * @param l removed listener.
     */
    public abstract void removePropertyChangeListener (
        String propertyName, 
        PropertyChangeListener l
    );
    
    /**
     * Creates a new {@link JPDAStep}. 
     * Parameters correspond to {@link JPDAStep} constructor.
     * 
     * @return {@link JPDAStep} 
     * @throws {@link java.lang.UnsupportedOperationException} If not overridden
     */
    public JPDAStep createJPDAStep(int size, int depth) {
        throw new UnsupportedOperationException("This method must be overridden."); 
    } 
}
